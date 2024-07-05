package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;

public class PutOrdinalCommand extends AbstractCommand {
	private final char[]	content;

	public PutOrdinalCommand(final char[] content) {
		this.content = content;
	}

	@Override
	public long execute(final MacrosRuntime rt) {
		if (rt.getLockCount() == 0) {
			rt.resetBuffer().append(content);
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
