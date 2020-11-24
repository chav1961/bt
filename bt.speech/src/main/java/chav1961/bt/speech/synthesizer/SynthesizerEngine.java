package chav1961.bt.speech.synthesizer;

import javax.speech.AudioException;
import javax.speech.AudioManager;
import javax.speech.AudioSegment;
import javax.speech.Engine;
import javax.speech.EngineException;
import javax.speech.EngineMode;
import javax.speech.EnginePropertyEvent;
import javax.speech.EngineStateException;
import javax.speech.SpeechEventExecutor;
import javax.speech.VocabularyManager;
import javax.speech.synthesis.Speakable;
import javax.speech.synthesis.SpeakableEvent;
import javax.speech.synthesis.SpeakableException;
import javax.speech.synthesis.SpeakableListener;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerListener;
import javax.speech.synthesis.SynthesizerProperties;

import chav1961.purelib.concurrent.LightWeightListenerList;


public class SynthesizerEngine implements Synthesizer {
	private static final int	AVAILABLE_SPEAKABLE_MASK = SpeakableEvent.TOP_OF_QUEUE  
													| SpeakableEvent.SPEAKABLE_STARTED
													| SpeakableEvent.SPEAKABLE_ENDED
													| SpeakableEvent.SPEAKABLE_PAUSED
													| SpeakableEvent.SPEAKABLE_RESUMED
													| SpeakableEvent.SPEAKABLE_CANCELLED
													| SpeakableEvent.WORD_STARTED
													| SpeakableEvent.PHONEME_STARTED
													| SpeakableEvent.MARKER_REACHED
													| SpeakableEvent.VOICE_CHANGED
													| SpeakableEvent.SPEAKABLE_FAILED
													| SpeakableEvent.PROSODY_UPDATED
													| SpeakableEvent.ELEMENT_REACHED;
	private static final long	AVAILABLE_ENGINE_STATE_MASK = Engine.ALLOCATED
													| Engine.ALLOCATING_RESOURCES
													| Engine.DEALLOCATED
													| Engine.DEALLOCATING_RESOURCES
													| Engine.DEFOCUSED
													| Engine.ERROR_OCCURRED
													| Engine.FOCUSED
													| Engine.PAUSED
													| Engine.RESUMED;
			
	private enum ExecutorRequest {
		PAUSE,
		RESUME,
		CANCEL,
		CANCEL_ALL;
	}
	
	
	private final LightWeightListenerList<SpeakableListener>	speakableListener = new LightWeightListenerList<>(SpeakableListener.class); 
	private final LightWeightListenerList<SynthesizerListener>	synthesizerListener = new LightWeightListenerList<>(SynthesizerListener.class); 
	private final SynthesizerEngineFactory	parent;
	private final Object					engineStateSync = new Object();
	private final SynthesizerProperties		props = new SynthesizerEngineProperties();
	private final AndOrVocabularyManager	vocMgr = new AndOrVocabularyManager();
	
	private volatile SpeechEventExecutor	see = null;
	private volatile int					speakableMask = 0;
	private volatile int					engineMask = 0;
	private volatile long					engineState = 0;
	
	public SynthesizerEngine(final SynthesizerEngineFactory parent) {
		if (parent == null) {
			throw new NullPointerException("Synthesizer factory can't be null"); 
		}
		else {
			this.parent = parent;
			this.props.addEnginePropertyListener((e)->propChanged(e));
		}
	}

	@Override
	public void allocate() throws AudioException, EngineException, EngineStateException, SecurityException {
		allocate(Engine.IMMEDIATE_MODE);
	}

	@Override
	public void allocate(int mode) throws IllegalArgumentException, AudioException, EngineException, EngineStateException, SecurityException {
		if (mode != Engine.IMMEDIATE_MODE && mode != Engine.ASYNCHRONOUS_MODE) {
			throw new IllegalArgumentException("Mode ["+mode+"] is neither Engine.IMMEDIATE_MODE nor Engine.ASYNCHRONOUS_MODE");
		}
		else {
			allocateInternal(mode);
		}
	}

	@Override
	public void deallocate() throws AudioException, EngineException, EngineStateException {
		deallocate(Engine.IMMEDIATE_MODE);
	}

	@Override
	public void deallocate(int mode) throws IllegalArgumentException, AudioException, EngineException, EngineStateException {
		if (mode != Engine.IMMEDIATE_MODE && mode != Engine.ASYNCHRONOUS_MODE) {
			throw new IllegalArgumentException("Mode ["+mode+"] is neither Engine.IMMEDIATE_MODE nor Engine.ASYNCHRONOUS_MODE");
		}
		else {
			deallocateInternal(mode);
		}
	}

	@Override
	public boolean testEngineState(long state) throws IllegalArgumentException {
		final long	result = state & ~AVAILABLE_ENGINE_STATE_MASK;
		
		if (result != 0) {
			throw new IllegalArgumentException("Unknown flags ["+Long.toBinaryString(result)+"] in the engine mask");
		}
		else {
			synchronized (engineStateSync) {
				return (engineState & state) != 0;
			}
		}
	}

	@Override
	public long waitEngineState(long state) throws InterruptedException, IllegalArgumentException, IllegalStateException {
		final long	result = state & ~AVAILABLE_ENGINE_STATE_MASK;
		
		if (result != 0) {
			throw new IllegalArgumentException("Unknown flags ["+Long.toBinaryString(result)+"] in the engine mask");
		}
		else {
			synchronized (engineStateSync) {
				while ((engineState & state) == 0) {
					engineStateSync.wait();
				}
				return engineState;
			}
		}
	}

	@Override
	public long waitEngineState(long state, long timeout) throws InterruptedException, IllegalArgumentException, IllegalStateException {
		final long	result = state & ~AVAILABLE_ENGINE_STATE_MASK;
		
		if (result != 0) {
			throw new IllegalArgumentException("Unknown flags ["+Long.toBinaryString(result)+"] in the engine mask");
		}
		else if (timeout <= 0) {
			throw new IllegalArgumentException("Timeout to wait ["+timeout+"] must be greater than 0");
		}
		else {
			synchronized (engineStateSync) {
				while ((engineState & state) == 0) {
					engineStateSync.wait(timeout);
				}
				return engineState;
			}
		}
	}

	@Override
	public AudioManager getAudioManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EngineMode getEngineMode() {
		return parent;
	}

	@Override
	public long getEngineState() {
		return engineState;
	}

	@Override
	public VocabularyManager getVocabularyManager() {
		return vocMgr;
	}

	@Override
	public void setEngineMask(final int mask) {
		engineMask = mask;
	}

	@Override
	public int getEngineMask() {
		return engineMask;
	}

	@Override
	public SpeechEventExecutor getSpeechEventExecutor() {
		return see;
	}

	@Override
	public void setSpeechEventExecutor(final SpeechEventExecutor speechEventExecutor) {
		if (speechEventExecutor == null) {
			throw new NullPointerException("Speech event executor can't be null");
		}
		else {
			see = speechEventExecutor;
		}
	}

	@Override
	public void addSpeakableListener(final SpeakableListener listener) {
		if (listener == null) {
			throw new NullPointerException("Speakable listener to add can't be null"); 
		}
		else {
			speakableListener.addListener(listener);
		}
	}

	@Override
	public void removeSpeakableListener(final SpeakableListener listener) {
		if (listener == null) {
			throw new NullPointerException("Speakable listener to remove can't be null"); 
		}
		else {
			speakableListener.removeListener(listener);
		}
	}

	@Override
	public void addSynthesizerListener(final SynthesizerListener listener) {
		if (listener == null) {
			throw new NullPointerException("Synthesizer listener to add can't be null"); 
		}
		else {
			synthesizerListener.addListener(listener);
		}
	}

	@Override
	public void removeSynthesizerListener(SynthesizerListener listener) {
		if (listener == null) {
			throw new NullPointerException("Synthesizer listener to remove can't be null"); 
		}
		else {
			synthesizerListener.addListener(listener);
		}
	}

	@Override
	public boolean cancel() throws EngineStateException {
		return putRequest(ExecutorRequest.CANCEL);
	}

	@Override
	public boolean cancel(int id) throws EngineStateException {
		return putRequest(ExecutorRequest.CANCEL,id);
	}

	@Override
	public boolean cancelAll() throws EngineStateException {
		return putRequest(ExecutorRequest.CANCEL_ALL);
	}

	@Override
	public String getPhonemes(final String text) throws EngineStateException {
		if (text == null || text.isEmpty()) {
			throw new NullPointerException("Text string can't be null or empty"); 
		}
		else {
			return buildPhonemes(text);
		}
	}


	@Override
	public SynthesizerProperties getSynthesizerProperties() {
		return props;
	}

	@Override
	public void pause() throws EngineStateException {
		putRequest(ExecutorRequest.PAUSE);
	}

	@Override
	public boolean resume() throws EngineStateException {
		return putRequest(ExecutorRequest.RESUME);
	}

	@Override
	public void setSpeakableMask(final int mask) {
		final int	result = mask & ~AVAILABLE_SPEAKABLE_MASK; 
		
		if (result != 0) {
			throw new IllegalArgumentException("Unknown flags ["+Integer.toBinaryString(result)+"] in the speakable mask");
		}
		else {
			speakableMask = mask;
		}
	}

	@Override
	public int getSpeakableMask() {
		return speakableMask;
	}

	@Override
	public int speak(final AudioSegment audio, final SpeakableListener listener) throws SpeakableException, EngineStateException, IllegalArgumentException {
		throw new UnsupportedOperationException("Markup speech doesn't support, use speak(String,SpeakableListener) method instead");
	}

	@Override
	public int speak(final Speakable speakable, final SpeakableListener listener) throws SpeakableException, EngineStateException {
		throw new UnsupportedOperationException("Markup speech doesn't support, use speak(String,SpeakableListener) method instead");
	}

	@Override
	public int speak(final String text, final SpeakableListener listener) throws EngineStateException {
		if (text == null || text.isEmpty()) {
			throw new IllegalArgumentException("Text to speak can't be null or empty");
		}
		else if (listener == null){
			return internalSpeak(getPhonemes(text));
		}
		else {
			try{addSpeakableListener(listener);
				return internalSpeak(getPhonemes(text));
			} finally {
				removeSpeakableListener(listener);
			}
		}
	}

	@Override
	public int speakMarkup(final String synthesisMarkup, final SpeakableListener listener) throws SpeakableException, EngineStateException {
		throw new UnsupportedOperationException("Markup speech doesn't support, use speak(String,SpeakableListener) method instead");
	}

	private void setEngineState(final long state) {
		synchronized (engineStateSync) {
			engineState = state;
			engineStateSync.notifyAll();
		}
	}

	private void propChanged(final EnginePropertyEvent e) {
		// TODO Auto-generated method stub
	}

	private void allocateInternal(int mode) {
		// TODO Auto-generated method stub
		
	}

	private void deallocateInternal(int mode) {
		// TODO Auto-generated method stub
		
	}
	
	private String buildPhonemes(String text) {
		
		// TODO Auto-generated method stub
		return null;
	}
	
	private int internalSpeak(final String phonemes) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private boolean putRequest(final ExecutorRequest request, final Object... parameters) {
		return false;
	}
}
