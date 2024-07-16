package chav1961.bt.preproc.runtime;

import chav1961.bt.preproc.runtime.interfaces.MacrosRuntime;
import chav1961.purelib.basic.exceptions.CalculationException;

public abstract class AbstractNonResumedCommand extends AbstractCommand {
	protected AbstractNonResumedCommand() {
	}

	@Override public abstract long execute(MacrosRuntime rt) throws CalculationException;
	@Override public abstract ControlType getControlType();

	@Override
	public boolean resumeRequired(MacrosRuntime rt) {
		return false;
	}
}
