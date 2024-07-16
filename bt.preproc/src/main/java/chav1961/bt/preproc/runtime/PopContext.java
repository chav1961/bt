package chav1961.bt.preproc.runtime;

import chav1961.bt.preproc.runtime.interfaces.MacrosRuntime;
import chav1961.purelib.basic.exceptions.CalculationException;

public class PopContext extends AbstractNonResumedCommand {
	public static final PopContext	SINGLETON = new PopContext(); 
	
	private PopContext() {
	}

	@Override
	public long execute(final MacrosRuntime rt) throws CalculationException {
		rt.getProgramStack().popBlock();
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
