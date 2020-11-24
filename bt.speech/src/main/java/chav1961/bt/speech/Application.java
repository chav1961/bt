package chav1961.bt.speech;

import javax.speech.EngineException;
import javax.speech.EngineManager;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.SynthesizerMode;

public class Application {
	
	public static void main(final String[] args) throws EngineException, SecurityException {
		EngineManager.registerEngineListFactory(EngineSet.class.getCanonicalName());
		
		System.err.println("Version="+EngineManager.getVersion());
		System.err.println("Speakers="+EngineManager.availableEngines(new SynthesizerMode(SpeechLocale.RUSSIAN)));
		System.err.println("Create="+EngineManager.createEngine(new SynthesizerMode(SpeechLocale.RUSSIAN)));
	}

}
