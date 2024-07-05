package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;

public class BackwardBrunch extends AbstractNonResumedCommand{
	private final long	label;

	protected BackwardBrunch(final long label) {
		this.label = label;
	}

	@Override
	public long execute(final MacrosRuntime rt) {
		return label;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.BACKWARD_BRUNCH;
	}
}
