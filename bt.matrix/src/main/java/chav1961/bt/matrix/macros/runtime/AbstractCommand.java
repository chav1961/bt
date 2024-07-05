package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.Command;
import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;
import chav1961.purelib.basic.exceptions.CalculationException;

public abstract class AbstractCommand implements Command {
	protected AbstractCommand() {
	}
	
	@Override public abstract long execute(MacrosRuntime rt) throws CalculationException;
	@Override public abstract boolean resumeRequired(MacrosRuntime rt);
	@Override public abstract ControlType getControlType();
}
