package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;

public class PopContext extends AbstractNonResumedCommand {
	public static final PopContext	SINGLETON = new PopContext(); 
	
	private PopContext() {
	}

	@Override
	public long execute(final MacrosRuntime rt) {
		rt.getProgramStack().popBlock();
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
