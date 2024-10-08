package chav1961.bt.preproc.runtime;


import chav1961.bt.preproc.runtime.interfaces.MacrosRuntime;
import chav1961.bt.preproc.runtime.interfaces.Value;
import chav1961.purelib.basic.exceptions.CalculationException;

public class DeclareVariable extends AbstractNonResumedCommand {
	private final int 				name;
	private final Value.ValueType	type;

	protected DeclareVariable(final int name, final Value.ValueType type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public long execute(final MacrosRuntime rt) throws CalculationException {
		rt.getProgramStack().declare(name, type);
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
