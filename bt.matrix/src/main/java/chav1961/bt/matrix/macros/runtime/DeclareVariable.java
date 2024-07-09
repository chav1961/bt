package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;
import chav1961.bt.matrix.macros.runtime.interfaces.Value;

public class DeclareVariable extends AbstractNonResumedCommand {
	private final int 				name;
	private final Value.ValueType	type;

	protected DeclareVariable(final int name, final Value.ValueType type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public long execute(final MacrosRuntime rt) {
		rt.getProgramStack().declare(name, type);
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
