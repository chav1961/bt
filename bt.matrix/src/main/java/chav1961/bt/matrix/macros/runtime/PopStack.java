package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;

public class PopStack extends AbstractNonResumedCommand {
	public static final PopStack	SINGLETON = new PopStack(); 
	
	private PopStack() {
	}

	@Override
	public long execute(final MacrosRuntime rt) {
		rt.getProgramStack().pop();
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}