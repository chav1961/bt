package chav1961.bt.preproc.runtime;

import chav1961.bt.preproc.runtime.interfaces.MacrosRuntime;
import chav1961.purelib.basic.exceptions.CalculationException;

public class ForwardBrunch extends AbstractNonResumedCommand{
	private final long	label;

	protected ForwardBrunch(final long label) {
		this.label = label;
	}

	@Override
	public long execute(final MacrosRuntime rt) throws CalculationException {
		return label;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.FORWARD_BRUNCH;
	}
}
