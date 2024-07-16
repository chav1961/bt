package chav1961.bt.preproc.runtime;

import java.util.function.Function;

import chav1961.bt.preproc.runtime.interfaces.MacrosRuntime;
import chav1961.purelib.basic.exceptions.CalculationException;

public class PutSubstitution extends AbstractCommand {
	private final Function<char[], char[]>[]	callbacks;

	public PutSubstitution(final Function<char[], char[]>... callbacks) {
		this.callbacks = callbacks.clone();
	}

	@Override
	public long execute(final MacrosRuntime rt) throws CalculationException {
		for(Function<char[], char[]> item : callbacks) {
			rt.getBuffer().append(item.apply(null));
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
