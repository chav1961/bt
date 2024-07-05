package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;

public class DecLockCount extends AbstractNonResumedCommand {
	public static DecLockCount	SINGLETON = new DecLockCount(); 
	
	private DecLockCount() {
	}

	@Override
	public long execute(final MacrosRuntime rt) {
		rt.decLockCount();
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
