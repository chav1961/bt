package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;

public class PushStack extends AbstractNonResumedCommand {
	public static final PushStack	SINGLETON = new PushStack(); 
	
	private PushStack() {
	}

	@Override
	public long execute(final MacrosRuntime rt) {
		rt.getProgramStack().push();
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
