package chav1961.bt.speech.synthesizer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.speech.AudioEvent;
import javax.speech.AudioException;
import javax.speech.AudioListener;
import javax.speech.AudioManager;
import javax.speech.EngineStateException;

import chav1961.purelib.concurrent.LightWeightListenerList;

public class InnerAudioManager implements AudioManager {
	private static final int	AVAILABLE_AUDIO_MASK = AudioEvent.AUDIO_STARTED | AudioEvent.AUDIO_STOPPED | AudioEvent.AUDIO_CHANGED;

	private enum ExecutorRequest {
		START,
		STOP,
		PLAY;
	}
	
	
	private final LightWeightListenerList<AudioListener>	audioListener = new LightWeightListenerList<>(AudioListener.class); 

	
	
	private volatile int		audioMask = AudioEvent.DEFAULT_MASK;
	private volatile URI		playback = URI.create("playback:/audio");

	public InnerAudioManager() {
	}
	
	@Override
	public int getAudioMask() {
		return audioMask;
	}

	@Override
	public void setAudioMask(final int mask) {
		final int	result = mask & ~AVAILABLE_AUDIO_MASK;
		
		if (result != 0) {
			throw new IllegalArgumentException("Unknown flags ["+Integer.toBinaryString(result)+"] in the audio mask");
		}
		else {
			audioMask = mask;
		}
	}

	@Override
	public void addAudioListener(final AudioListener listener) {
		if (listener == null) {
			throw new NullPointerException("Audio listener to add can't be null"); 
		}
		else {
			audioListener.addListener(listener);
		}
	}

	@Override
	public void removeAudioListener(AudioListener listener) {
		if (listener == null) {
			throw new NullPointerException("Audio listener to remove can't be null"); 
		}
		else {
			audioListener.removeListener(listener);
		}
	}

	@Override
	public void audioStart() throws SecurityException, AudioException, EngineStateException {
		putRequest(ExecutorRequest.START);
	}

	@Override
	public void audioStop() throws SecurityException, AudioException, EngineStateException {
		putRequest(ExecutorRequest.STOP);
	}

	@Override
	public void setMediaLocator(String locator) throws AudioException, IllegalStateException, IllegalArgumentException, SecurityException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMediaLocator(String locator, InputStream stream) throws AudioException, IllegalStateException, IllegalArgumentException, SecurityException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMediaLocator(String locator, OutputStream stream) throws AudioException, IllegalStateException, IllegalArgumentException, SecurityException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getMediaLocator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSupportedMediaLocators(String mediaLocator) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSupportedMediaLocator(final String mediaLocator) throws IllegalArgumentException {
		if (mediaLocator == null || mediaLocator.isEmpty()) {
			throw new IllegalArgumentException("Media locator can't be null or empty string");
		}
		else {
			return getSupportedMediaLocators(mediaLocator).length > 0;
		}
	}

	@Override
	public boolean isSameChannel(final AudioManager audioManager) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private boolean match(final String locator, final String template) {
		return true;
	}

	private boolean putRequest(final ExecutorRequest request, final Object... parameters) {
		return false;
	}
}
