package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;
import chav1961.bt.matrix.macros.runtime.interfaces.Value;

public class ForwardConditional extends AbstractNonResumedCommand {
	private final boolean	awaitedResult;
	private final long		label;
	
	public ForwardConditional(final boolean awaitedResult, final long label) {
		this.awaitedResult = awaitedResult;
		this.label = label;
	}

	@Override
	public long execute(final MacrosRuntime rt) {
		return RuntimeUtils.convert(rt.getProgramStack().popStackValue(), Value.ValueType.BOOLEAN).getValue(boolean.class) == awaitedResult ? label : 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.FORWARD_CONDITIONAL;
	}
}
