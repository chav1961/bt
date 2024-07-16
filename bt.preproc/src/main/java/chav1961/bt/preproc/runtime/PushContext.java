package chav1961.bt.preproc.runtime;

import chav1961.bt.preproc.runtime.interfaces.MacrosRuntime;
import chav1961.purelib.basic.exceptions.CalculationException;

public class PushContext extends AbstractNonResumedCommand {
	public static final PushContext	SINGLETON = new PushContext(); 
	
	private PushContext() {
	}

	@Override
	public long execute(final MacrosRuntime rt) throws CalculationException {
		rt.getProgramStack().pushBlock();
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
