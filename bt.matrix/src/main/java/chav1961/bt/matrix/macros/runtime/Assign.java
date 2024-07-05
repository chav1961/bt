package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;

public class Assign extends AbstractNonResumedCommand {
	private final int	name;

	protected Assign(final int name) {
		this.name = name;
	}

	@Override
	public long execute(final MacrosRuntime rt) {
		rt.getProgramStack().setVarValue(name, rt.getProgramStack().popStackValue());
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
