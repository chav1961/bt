package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;

public class IncLockCount extends AbstractNonResumedCommand {
	public static IncLockCount	SINGLETON = new IncLockCount(); 
	
	private IncLockCount() {
	}

	@Override
	public long execute(final MacrosRuntime rt) {
		rt.incLockCount();
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
