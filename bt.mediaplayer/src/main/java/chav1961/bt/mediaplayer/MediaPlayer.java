package chav1961.bt.mediaplayer;



import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;

import org.jcodec.common.io.IOUtils;
import org.jcodec.common.model.RationalLarge;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import chav1961.bt.mediaplayer.filters.JCodecVideoSource;
import chav1961.bt.mediaplayer.filters.JSoundAudioOut;
import chav1961.bt.mediaplayer.filters.audio.AudioMixer;
import chav1961.bt.mediaplayer.filters.audio.AudioMixer.Pin;
import chav1961.bt.mediaplayer.filters.audio.AudioSource;
import chav1961.bt.mediaplayer.filters.audio.JCodecAudioSource;
import chav1961.bt.mediaplayer.filters.http.HttpMedia;
import chav1961.bt.mediaplayer.filters.http.HttpPacketSource;
import chav1961.bt.mediaplayer.ui.SwingVO;
import chav1961.bt.mediaplayer.util.Player;
import chav1961.bt.mediaplayer.util.Stepper;
import chav1961.purelib.basic.Utils;

public class MediaPlayer implements KeyListener {
	public static final CountDownLatch	latch = new CountDownLatch(1);
	
    private Player player;
    private Stepper stepper;
    private JCodecVideoSource video;
    private SwingVO vo;
    private AudioMixer mixer;

    public MediaPlayer(final URL url) throws IOException {
        JFrame frame = new JFrame("Player");

        vo = new SwingVO();
        frame.getContentPane().add(vo, BorderLayout.CENTER);

        // Finish setting up the frame, and show it.
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	latch.countDown();
            	frame.setVisible(false);
            	frame.dispose();
            }
        });
        vo.setVisible(true);

        File cacheWhere = new File(System.getProperty("java.io.tmpdir"), "Library/JCodec");
        IOUtils.forceMkdir(cacheWhere);

        HttpMedia http = new HttpMedia(url, cacheWhere);

        final HttpPacketSource videoTrack = http.getVideoTrack();
        video = new JCodecVideoSource(videoTrack);

        List<HttpPacketSource> audioTracks = http.getAudioTracks();
        AudioSource[] audio = new AudioSource[audioTracks.size()];
        for (int i = 0; i < audioTracks.size(); i++) {
            audio[i] = new JCodecAudioSource(audioTracks.get(i));
        }
        mixer = new AudioMixer(2, audio);

        player = new Player(video, mixer, vo, new JSoundAudioOut());

        frame.addKeyListener(this);

        frame.pack();
        frame.setVisible(true);
        frame.setSize(new Dimension(768, 596));

        player.play();
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (player.getStatus() == Player.Status.PAUSED) {
                if (stepper != null) {
                    player.seek(stepper.getPos());
                    stepper = null;
                }
                player.play();
            } else
                player.pause();
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            RationalLarge pos = player.getPos();
            player.seek(new RationalLarge(pos.getNum() - pos.getDen() * 15, pos.getDen()));
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            RationalLarge pos = player.getPos();
            player.seek(new RationalLarge(pos.getNum() + pos.getDen() * 15, pos.getDen()));
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            // if (player.getStatus() != Player.Status.PAUSED) {
            // player.pause();
            // return;
            // }
            //
            // try {
            // if (stepper == null) {
            // stepper = new Stepper(video, mixer, vo, new JSoundAudioOut());
            // stepper.setListeners(player.getListeners());
            // stepper.gotoFrame(player.getFrameNo());
            // }
            // stepper.prev();
            // } catch (IOException e1) {
            // System.out.println("Couldn't step " + e1.getMessage());
            // }
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (player.getStatus() != Player.Status.PAUSED) {
                player.pause();
                return;
            }
            try {
                if (stepper == null) {
                    stepper = new Stepper(video, mixer, vo, new JSoundAudioOut());
                    stepper.setListeners(player.getListeners());
                    stepper.gotoFrame(player.getFrameNo());
                }
                stepper.next();
            } catch (IOException e1) {
                System.out.println("Couldn't step " + e1.getMessage());
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            player.destroy();
            System.exit(-1);
        } else if (e.getKeyChar() >= '0' && e.getKeyChar() < '9') {
            int ch = e.getKeyChar() - '0';
            for (Pin pin : mixer.getPins()) {
                if (ch < pin.getLabels().length) {
                    pin.toggle(ch);
                    break;
                } else
                    ch -= pin.getLabels().length;
            }
        }
    }
    
	public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException {
		HttpServer	server = HttpServer.create(new InetSocketAddress(10000), 0);
		server.createContext("/", new HttpHandler() {
			
			@Override
			public void handle(HttpExchange exchange) throws IOException {
				final File	f = new File("e:/chav1961/temp/video.mp4"); 

				System.err.println("Call ... "+exchange.getRequestMethod()+" "+exchange.getRequestURI());
				
				exchange.getResponseHeaders().add("Cache-Control", "no-cache");
				exchange.getResponseHeaders().add("Content-Type", "video/mp4");
				exchange.sendResponseHeaders(200, 0);
				final String answer = "[{\"type\":\"video\",\"info\":{\"par\":{\"num\":1,\"den\":1},\"dim\":{\"width\":100,\"height\":100}"
						+ "\"fourcc\":\"aaa\",\"timescale\":1,\"duration\":100,\"nFrames\":1000,\"name\":\"a\""
						+ "}}]";				
				try(final OutputStream	os = exchange.getResponseBody();
					final Writer		wr = new OutputStreamWriter(os);
//					final InputStream	is = new FileInputStream(f)) {
					final Reader		rdr = new StringReader(answer)) {
					
//					System.err.println("COPIED: "+Utils.copyStream(is, os));
					System.err.println("COPIED: "+Utils.copyStream(rdr, wr));
					wr.flush();
				}
				
			}
		});
        server.setExecutor(null);
        server.start();
		new MediaPlayer(new URL("http://localhost:10000/video.mp4"));
//		new MediaPlayer(new URL("https://www.youtube.com/watch?v=CFWtsXlvv9c"));
        latch.await();
        server.stop(0);
	}
}
