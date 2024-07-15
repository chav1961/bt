package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;
import chav1961.purelib.basic.exceptions.CalculationException;

public class AssignVar extends AbstractNonResumedCommand {
	private final int	varName;
	
	public AssignVar(final int varName) {
		this.varName = varName;
	}

	@Override
	public long execute(MacrosRuntime rt) throws CalculationException {
		rt.getProgramStack().setVarValue(varName, rt.getProgramStack().popStackValue());
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
