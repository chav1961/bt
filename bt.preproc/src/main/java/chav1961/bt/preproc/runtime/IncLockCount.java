package chav1961.bt.preproc.runtime;

import chav1961.bt.preproc.runtime.interfaces.MacrosRuntime;
import chav1961.purelib.basic.exceptions.CalculationException;

public class IncLockCount extends AbstractNonResumedCommand {
	public static IncLockCount	SINGLETON = new IncLockCount(); 
	
	private IncLockCount() {
	}

	@Override
	public long execute(final MacrosRuntime rt) throws CalculationException {
		rt.incLockCount();
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
