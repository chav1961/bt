package chav1961.bt.speech.synthesizer;

import javax.speech.Engine;
import javax.speech.EngineException;
import javax.speech.SpeechLocale;
import javax.speech.spi.EngineFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

public class SynthesizerEngineFactory extends SynthesizerMode implements EngineFactory {
	public static final String		ENGINE_NAME = "chav1961";
	public static final String		MODE_NAME = "simple speaker";
	
	static final Voice 				VOICE = new Voice(SpeechLocale.RUSSIAN,"alexander",Voice.GENDER_DONT_CARE,Voice.AGE_DONT_CARE,Voice.VARIANT_DONT_CARE);
	static final Voice[] 			VOICES = new Voice[] {VOICE};

    public SynthesizerEngineFactory() {
    	super(ENGINE_NAME,MODE_NAME,false,true,false,VOICES);
    }

	@Override
	public Engine createEngine() throws IllegalArgumentException, EngineException {
		return new SynthesizerEngine(this);
	}	
}
