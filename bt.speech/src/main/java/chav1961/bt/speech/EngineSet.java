package chav1961.bt.speech;

import javax.speech.EngineList;
import javax.speech.EngineMode;
import javax.speech.spi.EngineListFactory;
import javax.speech.synthesis.SynthesizerMode;

import chav1961.bt.speech.synthesizer.SynthesizerEngineFactory;

public class EngineSet implements EngineListFactory {
	@Override
	public EngineList createEngineList(final EngineMode require) {
		if (require instanceof SynthesizerMode) {
			final SynthesizerMode	mode = new SynthesizerEngineFactory();					
			
			if (mode.match(require)) {
				return new EngineList(new EngineMode[] {mode});
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
}
