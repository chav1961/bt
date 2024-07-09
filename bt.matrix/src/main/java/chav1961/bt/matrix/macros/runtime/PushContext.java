package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;

public class PushContext extends AbstractNonResumedCommand {
	public static final PushContext	SINGLETON = new PushContext(); 
	
	private PushContext() {
	}

	@Override
	public long execute(final MacrosRuntime rt) {
		rt.getProgramStack().pushBlock();
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
