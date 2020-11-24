package chav1961.bt.speech.synthesizer;

import javax.speech.EnginePropertyEvent;
import javax.speech.EnginePropertyListener;
import javax.speech.synthesis.SynthesizerProperties;
import javax.speech.synthesis.Voice;

import chav1961.purelib.concurrent.LightWeightListenerList;

class SynthesizerEngineProperties implements SynthesizerProperties {
	public static final String	BASE = "base"; 
	public static final String	INTERRUPTIBILITY = "interruptibility"; 
	public static final String	PITCH = "pitch"; 
	public static final String	PITCH_RANGE = "pitchRange"; 
	public static final String	SPEAKING_RATE = "speakingRate"; 
	public static final String	VOLUME = "volume"; 
	public static final String	PRIORITY = "priority"; 
	public static final String	VOICE = "voice"; 

	private static final int	HERTZ_LOW = 20;
	private static final int	HERTZ_HIGH = 20000;
	private static final int	DEFAULT_PITCH = 1000;
	private static final int	DEFAULT_PITCH_RANGE = 500;
	
	private final LightWeightListenerList<EnginePropertyListener>	propertyListener = new LightWeightListenerList<>(EnginePropertyListener.class); 

	private volatile String		base = "unknown:/";
	private volatile int		interruptibility = QUEUE_LEVEL;
	private volatile int		pitch = DEFAULT_PITCH;
	private volatile int		pitchRange = DEFAULT_PITCH_RANGE;
	private volatile int		rate = DEFAULT_RATE;
	private volatile int		volume = DEFAULT_VOLUME;
	private volatile int		priority = NORM_TRUSTED_PRIORITY;
	private volatile Voice		voice = SynthesizerEngineFactory.VOICE;
	
	SynthesizerEngineProperties() {
		resetInternal();
	}
	
	@Override
	public void addEnginePropertyListener(final EnginePropertyListener listener) {
		if (listener == null) {
			throw new NullPointerException("Engine property listener to add can't be null"); 
		}
		else {
			propertyListener.addListener(listener);
		}
	}

	@Override
	public void removeEnginePropertyListener(final EnginePropertyListener listener) {
		if (listener == null) {
			throw new NullPointerException("Engine property listener to remove can't be null"); 
		}
		else {
			propertyListener.removeListener(listener);
		}
	}

	@Override
	public void reset() {
		resetInternal();
	}

	@Override
	public void setBase(final String uri) throws IllegalArgumentException {
		if (uri == null || uri.isEmpty()) {
			throw new IllegalArgumentException("URI to set can't be null or empty");
		}
		else {
			notify(BASE, base, base = uri);
		}
	}

	@Override
	public String getBase() {
		return base;
	}

	@Override
	public void setInterruptibility(final int level) throws IllegalArgumentException {
		if (level != WORD_LEVEL && level != OBJECT_LEVEL && level != QUEUE_LEVEL) {
			throw new IllegalArgumentException("Illegal interruptibility value [], must be SynthesizerProperties.WORD_LEVEL, SynthesizerProperties.OBJECT_LEVEL or SynthesizerProperties.QUEUE_LEVEL only");
		}
		else {
			notify(INTERRUPTIBILITY, interruptibility, interruptibility = level);
		}
	}

	@Override
	public int getInterruptibility() {
		return interruptibility;
	}

	@Override
	public void setPitch(final int hertz) throws IllegalArgumentException {
		if (hertz < HERTZ_LOW || hertz > HERTZ_HIGH) {
			throw new IllegalArgumentException("Hertz pitch ["+hertz+"] out of range "+HERTZ_LOW+".."+HERTZ_HIGH);
		}
		else {
			notify(PITCH, pitch, pitch = hertz);
		}
	}

	@Override
	public int getPitch() {
		return pitch;
	}

	@Override
	public void setPitchRange(int hertz) throws IllegalArgumentException {
		if (hertz < HERTZ_LOW || hertz > HERTZ_HIGH) {
			throw new IllegalArgumentException("Hertz pitch range ["+hertz+"] out of range "+HERTZ_LOW+".."+HERTZ_HIGH);
		}
		else {
			notify(PITCH_RANGE, pitchRange, pitchRange = hertz);
		}
	}

	@Override
	public int getPitchRange() {
		return pitchRange;
	}

	@Override
	public void setSpeakingRate(final int wpm) throws IllegalArgumentException {
		if (wpm < X_FAST_RATE || wpm > DEFAULT_RATE) {
			throw new IllegalArgumentException("Speaking rate ["+wpm+"] out of range "+X_FAST_RATE+".."+DEFAULT_RATE);
		}
		else {
			notify(SPEAKING_RATE, rate, rate = wpm);
		}
	}

	@Override
	public int getSpeakingRate() {
		return rate;
	}

	@Override
	public void setVoice(final Voice voice) throws IllegalArgumentException {
		if (voice == null) {
			throw new IllegalArgumentException("Voice to set can't be null");
		}
		else {
			for (Voice item : SynthesizerEngineFactory.VOICES) {
				if (item.match(voice)) {
					notify(VOICE, this.voice, this.voice = item);
					break;
				}
			}
			throw new IllegalArgumentException("Voice ["+voice+"] is not supported by this engine");
		}
	}

	@Override
	public Voice getVoice() {
		return voice;
	}

	@Override
	public void setVolume(int volume) throws IllegalArgumentException {
		if (volume < MIN_VOLUME || volume > MAX_VOLUME) {
			throw new IllegalArgumentException("Volume ["+volume+"] out of range "+MIN_VOLUME+".."+MAX_VOLUME);
		}
		else {
			notify(VOLUME, this.volume, this.volume = volume);
		}
	}

	@Override
	public int getVolume() {
		return volume;
	}

	@Override
	public void setPriority(int priority) throws IllegalArgumentException {
		if (priority < MIN_PRIORITY || priority > MAX_VOLUME) {
			throw new IllegalArgumentException("Priority ["+priority+"] out of range "+MIN_PRIORITY+".."+MAX_PRIORITY);
		}
		else {
			notify(PRIORITY, this.priority, this.priority = priority);
		}
	}

	@Override
	public int getPriority() {
		return priority;
	}
	
	private void resetInternal() {
		notify(BASE, base, base = "unknown:/");
		notify(INTERRUPTIBILITY, interruptibility, interruptibility = QUEUE_LEVEL);
		notify(PITCH, pitch, pitch = DEFAULT_PITCH);
		notify(PITCH_RANGE, pitchRange, pitchRange = DEFAULT_PITCH_RANGE);
		notify(SPEAKING_RATE, rate, rate = MEDIUM_RATE);
		notify(VOLUME, this.volume, this.volume = DEFAULT_VOLUME);
		notify(VOICE, this.voice, this.voice = SynthesizerEngineFactory.VOICE);
	}
	
	private void notify(final String name, final Object oldValue, final Object newValue) {
		if (!oldValue.equals(newValue)) {
			final EnginePropertyEvent	epe = new EnginePropertyEvent(this, name, oldValue, newValue);
			
			propertyListener.fireEvent((l)->{l.propertyUpdate(epe);});
		}
	}
}
