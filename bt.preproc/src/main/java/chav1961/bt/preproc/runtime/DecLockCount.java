package chav1961.bt.preproc.runtime;

import chav1961.bt.preproc.runtime.interfaces.MacrosRuntime;
import chav1961.purelib.basic.exceptions.CalculationException;

public class DecLockCount extends AbstractNonResumedCommand {
	public static DecLockCount	SINGLETON = new DecLockCount(); 
	
	private DecLockCount() {
	}

	@Override
	public long execute(final MacrosRuntime rt) throws CalculationException {
		rt.decLockCount();
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
