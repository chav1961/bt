package chav1961.bt.audio;

import java.io.IOException;
import java.util.zip.ZipInputStream;

import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.spi.MidiFileReader;
import javax.sound.sampled.AudioInputStream;

import chav1961.purelib.concurrent.interfaces.ExecutionControl;

public class AudioFactory implements AutoCloseable, ExecutionControl {
	public enum Priority {
		
	}
	
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
	
	public void add(final String name, final AudioInputStream ais) {
		
	}

	public void add(final String name, final MidiFileReader rdr) {
		
	}

	public void add(final ZipInputStream ais) {
		
	}
	
	public void add(final String prefix, final ZipInputStream zis) {
		
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
