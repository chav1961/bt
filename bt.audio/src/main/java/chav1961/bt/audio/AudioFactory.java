package chav1961.bt.audio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;

public class AudioFactory implements AutoCloseable, ExecutionControl {
	public static final int	MAX_VOLUME = 255;
	
	public static enum AudioType {
		AUDIO, MIDI, UNKNOWN;
	}
	
	public enum Priority {
		
	}
	
	private final Map<String, RepoItem>	repo = new HashMap<>();
	private volatile boolean	started = false;
	private volatile boolean	suspended = false;
	private int 				sampleVolume = 64;
	private int 				midiVolume = 64;
	
	public AudioFactory() {
		this(true, true);
	}
	
	public AudioFactory(final boolean useMidi, final boolean useSamples) {
		if (!useMidi && !useSamples) {
			throw new IllegalArgumentException("Neither MIDI nor Samples support were selected");
		}
		else if (useMidi && !isMidiSupported()) {
			throw new IllegalStateException("Midi is not available on the system");
		}
		else if (useSamples && !isSamplesSupported()) {
			throw new IllegalStateException("Sampler is not available on the system");
		}
		else {
		}
	}

	@Override
	public synchronized void start() throws IllegalStateException {
		if (started) {
			throw new IllegalStateException("Audio factory instance is started already");
		}
		else {
			started = true;
		}
	}

	@Override
	public synchronized void suspend() throws IllegalStateException {
		if (!started) {
			throw new IllegalStateException("Audio factory instance is not started yet");
		}
		else if (suspended) {
			throw new IllegalStateException("Audio factory instance is suspended already");
		}
		else {
			suspended = true;
		}
	}

	@Override
	public synchronized void resume() throws IllegalStateException {
		if (!started) {
			throw new IllegalStateException("Audio factory instance is not started yet");
		}
		else if (!suspended) {
			throw new IllegalStateException("Audio factory instance is not suspended yet");
		}
		else {
			suspended = false;
		}
	}

	@Override
	public synchronized void stop() throws IllegalStateException {
		if (!started) {
			throw new IllegalStateException("Audio factory instance is not started yet");
		}
		else {
			suspended = false;
			started = false;
		}
	}

	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public boolean isSuspended() {
		return suspended;
	}
	
	@Override
	public void close() throws IOException {
		repo.clear();
	}
	
	public void add(final String name, final AudioInputStream ais) throws IOException {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Name can't be null or empty"); 
		}
		else if (ais == null) {
			throw new NullPointerException("Audio input stream can't be null"); 
		}
		else {
			final AudioFormat	af = ais.getFormat();
			
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
				final GZIPOutputStream		gzos = new GZIPOutputStream(baos)) {
				
				Utils.copyStream(ais, gzos);
				if (repo.containsKey(name)) {
					throw new IOException("Duplicate name ["+name+"] to add audio content");
				}
				else {
					repo.put(name, new RepoItem(af, baos.toByteArray()));
				}
			}
		}
	}

	public void add(final String name, final Sequence seq) throws IOException {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Name can't be null or empty"); 
		}
		else if (seq == null) {
			throw new NullPointerException("MIDI sequence can't be null"); 
		}
		else {
			if (repo.containsKey(name)) {
				throw new IOException("Duplicate name ["+name+"] to add MIDI content");
			}
			else {
				repo.put(name, new RepoItem(seq));
			}
		}
	}

	public void add(final ZipInputStream zis) throws IOException {
		add("", "*.*", zis);
	}

	public void add(final String prefix, final String mask, final ZipInputStream zis) throws IOException {
		add(prefix, mask, zis, (name)->name.endsWith(".wav") ? AudioType.AUDIO : (name.endsWith(".mid") ? AudioType.MIDI : AudioType.UNKNOWN));
	}
	
	public void add(final String prefix, final String mask, final ZipInputStream zis, final Function<String, AudioType> detector) throws IOException {
		if (prefix == null) {
			throw new NullPointerException("Prefix string can't be null"); 
		}
		else if (Utils.checkEmptyOrNullString(mask)) {
			throw new IllegalArgumentException("Mask string can't be null or empty"); 
		}
		else if (zis == null) {
			throw new NullPointerException("ZIP input stream can't be null"); 
		}
		else {
			final Pattern	pattern = Pattern.compile(Utils.fileMask2Regex(mask));
			ZipEntry		ze;
			
			while ((ze = zis.getNextEntry()) != null) {
				final String	name = ze.getName(); 
				
				if (pattern.matcher(name).matches()) {
					switch (detector.apply(name)) {
						case AUDIO		:
							try {
								final AudioInputStream 	ais = AudioSystem.getAudioInputStream(zis);
								
								add(name, ais);
							} catch (UnsupportedAudioFileException e) {
								throw new IOException(e.getLocalizedMessage(), e);
							}
							break;
						case MIDI		:
							try {
								final Sequence 	seq = MidiSystem.getSequence(zis);
								
								add(name, seq);
							} catch (InvalidMidiDataException | IOException e) {
								throw new IOException(e.getLocalizedMessage(), e);
							}
							break;
						case UNKNOWN	:
							break;
						default:
							throw new UnsupportedOperationException("Audio type ["+detector.apply(name)+"] is not supported yet");
					}
				}
			}
		}
	}

	public void remove(final String pattern) {
		if (Utils.checkEmptyOrNullString(pattern)) {
			throw new IllegalArgumentException("Pattern string can't be null or empty"); 
		}
		else {
			final Pattern		p = Pattern.compile(Utils.fileMask2Regex(pattern));
			final Set<String>	toRemove = new HashSet<>();
			
			for(Entry<String, RepoItem> item : repo.entrySet()) {
				if (p.matcher(item.getKey()).matches()) {
					toRemove.add(item.getKey());
				}
			}
		}
	}

	public String[] getNames() {
		return repo.keySet().toArray(new String[repo.size()]);
	}
	
	public boolean contains(final String name) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Name can't be null or empty"); 
		}
		else {
			return repo.containsKey(name);
		}
	}

	public void play(final String name) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Name can't be null or empty"); 
		}
		else if (!contains(name)) {
			throw new IllegalArgumentException("Name ["+name+"] is missing in the factory"); 
		}
		else {
			final RepoItem	item = repo.get(name);
			
			switch (item.type) {
				case AUDIO		:
					play(name, getSampleVolume());
					break;
				case MIDI		:
					play(name, getMidiVolume());
					break;
				case UNKNOWN	:
					throw new IllegalArgumentException("Unknown item type ["+item.type+"]"); 
				default:
					throw new UnsupportedOperationException("Item type ["+item.type+"] is not supported yet");
			}
		}
	}

	public void play(final String name, final int volume) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Name can't be null or empty"); 
		}
		else if (!contains(name)) {
			throw new IllegalArgumentException("Name ["+name+"] is missing in the factory"); 
		}
		else {
			final RepoItem	item = repo.get(name);
			
			switch (item.type) {
				case AUDIO		:
					play(name, new PlaySettings(volume));
					break;
				case MIDI		:
					play(name, new PlaySettings(volume));
					break;
				case UNKNOWN	:
					throw new IllegalArgumentException("Unknown item type ["+item.type+"]"); 
				default:
					throw new UnsupportedOperationException("Item type ["+item.type+"] is not supported yet");
			}
		}
	}
	
	public void play(final String name, final PlaySettings priority) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Name can't be null or empty"); 
		}
		else if (!contains(name)) {
			throw new IllegalArgumentException("Name ["+name+"] is missing in the factory"); 
		}
		else {
			final RepoItem	item = repo.get(name);
			
			switch (item.type) {
				case AUDIO		:
					// TODO:
					break;
				case MIDI		:
					// TODO:
					break;
				case UNKNOWN	:
					throw new IllegalArgumentException("Unknown item type ["+item.type+"]"); 
				default:
					throw new UnsupportedOperationException("Item type ["+item.type+"] is not supported yet");
			}
		}
	}

	public int getMidiVolume() {
		return midiVolume;
	}

	public int getSampleVolume() {
		return sampleVolume;
	}

	public void setMidiVolume(final int volume) {
		if (volume < 0 || volume >= MAX_VOLUME) {
			throw new IllegalArgumentException("Volume ["+volume+"] out of range 0.."+MAX_VOLUME);
		}
		else {
			midiVolume = volume;
		}
	}

	public void setSampleVolume(final int volume) {
		if (volume < 0 || volume >= MAX_VOLUME) {
			throw new IllegalArgumentException("Volume ["+volume+"] out of range 0.."+MAX_VOLUME);
		}
		else {
			sampleVolume = volume;
		}
	}
	
	public static boolean isMidiSupported() {
		final Info[] info = MidiSystem.getMidiDeviceInfo();
		
		return info.length > 0;
	}
	
	public static boolean isSamplesSupported() {
		final Line.Info	info = new Line.Info(SourceDataLine.class);
		
		return AudioSystem.getSourceLineInfo(info).length > 0;
	}
	
	public static class PlaySettings {
		final int	volume;
		
		public PlaySettings(final int volume) {
			this.volume = volume;
		}
		
		
	}
	
	private static class RepoItem {
		private final AudioType		type;
		private final AudioFormat	audioFormat;
		private final byte[]		content;
		private final Sequence		seq;

		private RepoItem(final AudioFormat audioFormat, final byte[] content) {
			this.type = AudioType.AUDIO;
			this.audioFormat = audioFormat;
			this.content = content;
			this.seq = null;
		}
		
		private RepoItem(final Sequence seq) {
			this.type = AudioType.MIDI;
			this.audioFormat = null;
			this.content = null;
			this.seq = seq;
		}
	}
}
