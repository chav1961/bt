package chav1961.bt.preproc.runtime;

import chav1961.bt.preproc.runtime.interfaces.MacrosRuntime;
import chav1961.purelib.basic.exceptions.CalculationException;

public class PushStack extends AbstractNonResumedCommand {
	public static final PushStack	SINGLETON = new PushStack(); 
	
	private PushStack() {
	}

	@Override
	public long execute(final MacrosRuntime rt) throws CalculationException {
		rt.getProgramStack().pushStackValue(null);
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
