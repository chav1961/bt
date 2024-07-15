package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;
import chav1961.bt.matrix.macros.runtime.interfaces.ThreadedCommandRepo.CommandRepoExecutor;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;

public class Calculate extends AbstractNonResumedCommand {
	private final CommandRepoExecutor	cre;

	public Calculate(final CommandRepoExecutor cre) {
		this.cre = cre;
	}

	@Override
	public long execute(final MacrosRuntime rt) throws CalculationException {
		try {
			cre.execute(rt);
			return 1;
		} catch (ContentException e) {
			throw new CalculationException(e);
		}
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
