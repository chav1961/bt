package chav1961.bt.matrix.macros.runtime.interfaces;

import chav1961.purelib.basic.exceptions.CalculationException;

public interface Command {
	public static enum ControlType {
		SEQUENCE,
		BACKWARD_BRUNCH,
		FORWARD_BRUNCH,
		BACKWARD_CONDITIONAL,
		FORWARD_CONDITIONAL
	}
	
	long execute(MacrosRuntime rt) throws CalculationException;
	boolean resumeRequired(MacrosRuntime rt);
	ControlType getControlType();
}