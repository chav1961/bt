package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;

public class Calculate extends AbstractNonResumedCommand {
	private final Object	tree;

	public Calculate(final MacrosRuntime rt, final Object tree) {
		this.tree = tree;
	}

	@Override
	public long execute(final MacrosRuntime rt) {
		rt.getProgramStack().pushStackValue(null);
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
