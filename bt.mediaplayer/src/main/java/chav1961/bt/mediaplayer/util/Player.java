package chav1961.bt.mediaplayer.util;

import static org.jcodec.common.model.RationalLarge.R;
//import static org.jcodec.player.util.ThreadUtil.joinForSure;
//import static org.jcodec.player.util.ThreadUtil.sleepNoShit;
//import static org.jcodec.player.util.ThreadUtil.surePut;
//import static org.jcodec.player.util.ThreadUtil.take;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jcodec.common.AudioFormat;
import org.jcodec.common.model.AudioFrame;
import org.jcodec.common.model.Frame;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.RationalLarge;
import org.jcodec.common.model.Size;
import org.jcodec.common.model.TapeTimecode;
import org.jcodec.common.tools.Debug;
import org.jcodec.javase.common.SoundUtil;
import org.jcodec.scale.ColorUtil;

import chav1961.bt.mediaplayer.filters.AudioOut;
import chav1961.bt.mediaplayer.filters.MediaInfo;
import chav1961.bt.mediaplayer.filters.MediaInfo.AudioInfo;
import chav1961.bt.mediaplayer.filters.VideoOutput;
import chav1961.bt.mediaplayer.filters.VideoSource;
import chav1961.bt.mediaplayer.filters.audio.AudioSource;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * Media player engine
 * 
 * @author The JCodec project
 * 
 */
public class Player {
    public enum Status {
        STOPPED, PAUSED, BUFFERING, PLAYING
    }

    private static final int VIDEO_QUEUE_SIZE = 20;
    private static final int AUDIO_QUEUE_SIZE = 20;
    public static final int PACKETS_IN_BUFFER = 8;
    public static int TIMESCALE = 96000;

    private VideoSource videoSource;
    private AudioSource audioSource;
    private VideoOutput vo;
    private AudioOut ao;

    AtomicBoolean pause = new AtomicBoolean();
    private long clock;
    private long lastAudio;

    private List<Frame> video = Collections.synchronizedList(new ArrayList<Frame>());
    private BlockingQueue<byte[][]> videoDrain = new LinkedBlockingQueue<byte[][]>();

    private volatile boolean stop;

    private BlockingQueue<ByteBuffer> audio = new LinkedBlockingQueue<ByteBuffer>();
    private BlockingQueue<ByteBuffer> audioDrain = new LinkedBlockingQueue<ByteBuffer>();

    private AudioFormat af;
    private Picture dst;
    private Object seekLock = new Object();
    private Object pausedEvent = new Object();
    private MediaInfo.VideoInfo mi;

    private List<Listener> listeners = new ArrayList<Listener>();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private volatile boolean resume;
    private volatile boolean decodingLocked;

    Frame[] EMPTY = new Frame[0];
    private int curFrameNo = -1;

    private Thread resumeThread;
    private Thread videoPlaybackThread;
    private Thread audioDecodeThread;
    private Thread audioPlaybackThread;
    private Thread videoDecodeThread;

    public Player(VideoSource videoSource, AudioSource audioSource, VideoOutput vo, AudioOut ao) throws IOException {
        this.videoSource = videoSource;
        this.audioSource = audioSource;
        this.vo = vo;
        this.ao = ao;

        initPlayer();
    }

    private void initPlayer() throws IOException {
        Debug.println("Initializing player");

        pause.set(true);
        clock = 0;

        videoDrain.clear();
        audioDrain.clear();
        video.clear();
        audio.clear();

        AudioInfo ai = audioSource.getAudioInfo();
        af = ai.getFormat();
        ao.open(SoundUtil.toJavax(af), 1024 * PACKETS_IN_BUFFER);

        mi = videoSource.getMediaInfo();

        startAudioDecode();

        lastAudio = ao.playedMs();
        startAudioPlayback();

        startVideoDecode();

        for (int i = 0; i < VIDEO_QUEUE_SIZE; i++) {
        	ThreadUtil.surePut(videoDrain, createTarget());
        }

        for (int i = 0; i < AUDIO_QUEUE_SIZE; i++) {
        	ThreadUtil.surePut(audioDrain, ByteBuffer.allocate(af.getFrameSize() * 1034));
        }

        startVideoPlayback();

        startResumeThread();
    }

    /**
     * Resumes player playback as soon as possible
     */
    public void play() {
        executor.submit(new Runnable() {
            public void run() {
                resume = true;
                notifyStatus();
            }
        });
    }

    /**
     * Pauses playback
     * 
     * Waits until player actually stops
     * 
     * @return Wheather playback was already paused
     */
    public void pause() {
        executor.submit(new Runnable() {
            public void run() {
                resume = false;
                pauseNoWait();
            }
        });
    }

    private void startResumeThread() {
        resumeThread = new Thread() {
            public void run() {
                while (!stop) {
                    if (resume && pause.get()) {
                        if (audio.size() >= AUDIO_QUEUE_SIZE / 2 && video.size() >= VIDEO_QUEUE_SIZE - 1) {
                            pause.set(false);
                            ao.resume();
                            notifyStatus();
                        }
                    }
                    ThreadUtil.sleepNoShit(500000);
                }
                Debug.println("Resume thread done");
            }
        };
        resumeThread.setDaemon(true);
        resumeThread.start();
    }

    private void startVideoPlayback() {
        videoPlaybackThread = new Thread() {
            public void run() {
                Debug.println("Starting video playback");
                try {
                    playVideo();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Debug.println("Playing video done");
            }
        };
        videoPlaybackThread.start();
    }

    private void playVideo() throws IOException {
        int late = 0;
        while (!stop) {
            if (!pause.get()) {
                long newAudio = ao.playedMs();
                clock += newAudio - lastAudio;
                lastAudio = newAudio;

                long pts = (clock * 96) / 1000;

                late += dropLateFrames(pts);
                Frame selected = selectFrame(pts);
                if (selected == null) {
                    if (late < 4) {
                    	ThreadUtil.sleepNoShit(2000000);
                    } else {
                        System.out.println("Video late, pausing");
                        pauseNoWait();
                    }
                } else {
                    late = 0;
                    show(selected);
                    ThreadUtil.surePut(videoDrain, selected.getPic().getData());
                }
            } else {
                synchronized (pausedEvent) {
                    pausedEvent.notifyAll();
                }
                ThreadUtil.sleepNoShit(200000);
            }
        }
    }

    private int dropLateFrames(long pts) {
        List<Frame> late = new ArrayList<Frame>();
        for (Frame frame : video.toArray(EMPTY)) {
            long frameEnd = frame.getPts().multiplyS(TIMESCALE) + frame.getDuration().multiplyS(TIMESCALE);
            if (pts > frameEnd)
                late.add(frame);
        }
        removeFrames(late);

        return late.size();
    }

    private Frame selectFrame(long pts) {
        List<Frame> junk = new ArrayList<Frame>();
        Frame found = null;
        for (Frame frame : video.toArray(EMPTY)) {
            long framePts = frame.getPts().multiplyS(TIMESCALE);
            long frameDuration = frame.getDuration().multiplyS(TIMESCALE);
            if (pts >= framePts && pts < framePts + frameDuration) {
                found = frame;
                break;
            }
            junk.add(frame);
        }
        if (found != null) {
            removeFrames(junk);
            video.remove(found);
        }

        return found;
    }

    private void removeFrames(List<Frame> remove1) {
        video.removeAll(remove1);
        for (Frame frame : remove1) {
        	ThreadUtil.surePut(videoDrain, frame.getPic().getData());
        }
    }

    private byte[][] createTarget() {
        Size dim = mi.getDim();
        int sz = 2 * dim.getWidth() * dim.getHeight();
        return new byte[][] { new byte[sz], new byte[sz], new byte[sz] };
    }

    private void startVideoDecode() {
        videoDecodeThread = new Thread() {
            public void run() {
                Debug.println("Starting video decode");
                try {
                    decodeVideo();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Debug.println("Decoding video done");
            }
        };
        videoDecodeThread.start();
    }

    private void startAudioDecode() {
        audioDecodeThread = new Thread() {
            public void run() {
                Debug.println("Starting audio decode");
                try {
                    decodeAudio();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Debug.println("Decoding audio done");
            }
        };
        audioDecodeThread.start();
    }

    private void decodeAudio() throws IOException {
        long predPts = Long.MIN_VALUE;
        while (!stop) {
            if (decodingLocked) {
            	ThreadUtil.sleepNoShit(500000);
                continue;
            }

            ByteBuffer buf = ThreadUtil.take(audioDrain, 20);
            buf.rewind();
            if (buf == null)
                continue;
            AudioFrame frame = audioSource.getFrame(buf);

            if (frame != null) {
                long pts = (frame.getPts() * TIMESCALE) / frame.getTimescale();

                if (Math.abs(predPts - pts) > TIMESCALE / 100) {
                    while (pause.get() != true)
                    	ThreadUtil.sleepNoShit(500000);
                    clock = (1000000 * frame.getPts()) / frame.getTimescale();
                    if (!seekVideo(R(pts, TIMESCALE)))
                        seekVideo(R(pts + TIMESCALE / 100, TIMESCALE));
                }

                predPts = (frame.getPts() * TIMESCALE) / frame.getTimescale() + (frame.getDuration() * TIMESCALE)
                        / frame.getTimescale();

                ThreadUtil.surePut(audio, frame.getData());
            } else {
            	ThreadUtil.surePut(audioDrain, buf);
            	ThreadUtil.sleepNoShit(500000);
            }
        }
    }

    private void decodeVideo() throws IOException {
        while (!stop) {
            if (decodingLocked) {
            	ThreadUtil.sleepNoShit(500000);
                continue;
            }

            decodeJustOneFrame();
        }
    }

    private void decodeJustOneFrame() throws IOException {
        byte[][] buf = ThreadUtil.take(videoDrain, 20);
        if (buf == null)
            return;
        Frame frame = videoSource.decode(buf);
        if (frame != null) {
            video.add(frame);
        } else {
        	ThreadUtil.surePut(videoDrain, buf);
        	ThreadUtil.sleepNoShit(500000);
        }
    }

    private void startAudioPlayback() {
        audioPlaybackThread = new Thread() {
            public void run() {
            	ThreadUtil.sleepNoShit(10000000);
                playAudio();
            }
        };

        audioPlaybackThread.start();
    }

    private void playAudio() {
        Debug.println("Starting audio playback");
        ByteBuffer pkt = null;
        while (!stop) {
            if (!pause.get()) {
                if (pkt == null) {
                    pkt = audio.poll();
                    if (pkt == null) {
                        Debug.println("Audio queue empty");
                        pauseNoWait();
                        continue;
                    }
                }
                ao.write(pkt);
                if (pkt.remaining() == 0) {
                	ThreadUtil.surePut(audioDrain, pkt);
                    pkt = null;
                }
            } else {
            	ThreadUtil.sleepNoShit(500000);
            }
        }
        Debug.println("Playing autio done");
    }

    private void pauseNoWait() {
        try {
            if (!pause.getAndSet(true)) {
                ao.pause();
                Debug.println("On pause: " + ao.playedMs());
            }
        } finally {
            notifyStatus();
        }
    }

    public boolean pauseWait() {
        try {
            if (!pause.getAndSet(true)) {
                ao.pause();
                synchronized (pausedEvent) {
                    sureWait(pausedEvent);
                }
                return false;
            }
            return true;
        } finally {
            notifyStatus();
        }
    }

    private void show(Frame frame) {
        Picture src = frame.getPic();

        notifyTime(frame);

        curFrameNo = frame.getFrameNo();

        if (src.getColor() != vo.getColorSpace()) {
            if (dst == null || dst.getWidth() != src.getWidth() || dst.getHeight() != src.getHeight())
                dst = Picture.createCropped(src.getWidth(), src.getHeight(), vo.getColorSpace(), src.getCrop());

            ColorUtil.getTransform(src.getColor(), vo.getColorSpace()).transform(src, dst);

            vo.show(dst, frame.getPixelAspect());
        } else {
            vo.show(src, frame.getPixelAspect());
        }
    }

    private boolean seekVideo(RationalLarge second) throws IOException {
        if (!videoSource.drySeek(second))
            return false;
        synchronized (seekLock) {

            decodingLocked = true;
            videoSource.seek(second);
            drainVideo();

            decodeJustOneFrame();
            if (video.size() > 0)
                show(video.get(0));

            decodingLocked = false;
        }
        return true;
    }

    public void seek(final RationalLarge where) {
        executor.submit(new Runnable() {
            public void run() {
                try {
                    seekInt(where);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void seekInt(RationalLarge second) throws IOException {
        if (second.lessThen(RationalLarge.ZERO) || !audioSource.drySeek(second))
            return;

        synchronized (seekLock) {
            boolean wasPlaying = resume;
            resume = false;
            pauseWait();
            decodingLocked = true;

            audioSource.seek(second);
            drainAudio();
            ao.flush();

            decodingLocked = false;

            resume = wasPlaying;
        }
    }

    private void drainVideo() {
        synchronized (video) {
            Frame[] copy = video.toArray(EMPTY);
            video.clear();
            for (Frame frame : copy) {
            	ThreadUtil.surePut(videoDrain, frame.getPic().getData());
            }
        }
    }

    private void drainAudio() {
        List<ByteBuffer> list = new LinkedList<ByteBuffer>();
        audio.drainTo(list);
        for (ByteBuffer frame : list) {
            audioDrain.add(frame);
        }
    }

    private void sureWait(Object monitor) {
        try {
            pausedEvent.wait();
        } catch (InterruptedException e) {
        }
    }

    public RationalLarge getPos() {
        return new RationalLarge((clock * 96) / 1000, TIMESCALE);
    }

    public void destroy() {
        stop = true;

        ThreadUtil.joinForSure(videoDecodeThread);
        ThreadUtil.joinForSure(audioDecodeThread);
        ThreadUtil.joinForSure(videoPlaybackThread);
        ThreadUtil.joinForSure(audioPlaybackThread);
        ThreadUtil.joinForSure(resumeThread);

        video = null;
        audio = null;
        videoDrain = null;
        audioDrain = null;

        Debug.println("Player destroyed");
    }

    private void notifyStatus() {
        final Status status = getStatus();
        executor.execute(new Runnable() {
            public void run() {
                for (Listener listener : listeners) {
                    try {
                        listener.statusChanged(status);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });
    }

    private void notifyTime(final Frame frame) {
        executor.execute(new Runnable() {
            public void run() {
                for (Listener listener : listeners) {
                    try {
                        listener.timeChanged(frame.getPts(), frame.getFrameNo(), frame.getTapeTimecode());
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });
    }

    public static interface Listener {
        void timeChanged(RationalLarge pts, int frameNo, TapeTimecode tapeTimecode);

        void statusChanged(Status status);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public VideoSource getVideoSource() {
        return videoSource;
    }

    public AudioSource getAudioSources() {
        return audioSource;
    }

    public int getFrameNo() {
        return curFrameNo;
    }

    public Status getStatus() {
        return pause.get() ? (resume ? Status.BUFFERING : Status.PAUSED) : Status.PLAYING;
    }

    public List<Listener> getListeners() {
        return listeners;
    }
}