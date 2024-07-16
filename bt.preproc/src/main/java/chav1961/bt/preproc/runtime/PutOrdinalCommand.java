package chav1961.bt.preproc.runtime;

import chav1961.bt.preproc.runtime.interfaces.MacrosRuntime;
import chav1961.purelib.basic.exceptions.CalculationException;

public class PutOrdinalCommand extends AbstractCommand {
	private final char[]	content;

	public PutOrdinalCommand(final char[] content) {
		this.content = content;
	}

	@Override
	public long execute(final MacrosRuntime rt) throws CalculationException {
		if (rt.getLockCount() == 0) {
			rt.getBuffer().append(content);
		}
		return 1;
	}

	@Override
	public boolean resumeRequired(final MacrosRuntime rt) {
		return rt.getLockCount() == 0;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
