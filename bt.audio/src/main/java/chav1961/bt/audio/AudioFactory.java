package chav1961.bt.audio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.spi.MidiFileReader;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;

public class AudioFactory implements AutoCloseable, ExecutionControl {
	public enum Priority {
		
	}
	
	private final Map<AudioFormat, ?>	audioChannels = new HashMap<>(); 
	
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
	public void start() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void suspend() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSuspended() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
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
				placeSampleContent(name, af, baos.toByteArray());
			}
		}
	}

	private void placeSampleContent(final String name, final AudioFormat af, final byte[] content) {
		// TODO Auto-generated method stub
		
	}


	public void add(final String name, final MidiFileReader rdr) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Name can't be null or empty"); 
		}
		else if (rdr == null) {
			throw new NullPointerException("MIDI reader can't be null"); 
		}
		else {
			
		}
	}

	public void add(final ZipInputStream zis) throws IOException {
		add("", "*.*", zis);
	}
	
	public void add(final String prefix, final String mask, final ZipInputStream zis) throws IOException {
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
				if (pattern.matcher(ze.getName()).matches()) {
					
				}
			}
		}
		
	}

	public void remove(final String pattern) {
		
	}

	public String[] getNames() {
		return null;
	}
	
	public boolean contains(final String name) {
		return false;
	}

	public void play(final String name) {
		
	}

	public void play(final String name, final int volume) {
		
	}
	
	public void play(final String name, final PlaySettings priority) {
		
	}

	public int getMidiVolume() {
		return 0;
	}

	public int getSampleVolume() {
		return 0;
	}

	public void setMidiVolume(final int volume) {
	}

	public void setSampleVolume(final int volume) {
	}
	
	public static boolean isMidiSupported() {
		final Info[] info = MidiSystem.getMidiDeviceInfo();
		
		return info.length > 0;
	}
	
	public static boolean isSamplesSupported() {
		return true;
	}
	
	public static class PlaySettings {
		
	}
}
