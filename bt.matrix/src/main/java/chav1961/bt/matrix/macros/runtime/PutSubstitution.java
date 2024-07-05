package chav1961.bt.matrix.macros.runtime;

import java.util.function.Function;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;

public class PutSubstitution extends AbstractCommand {
	private final Function<char[], char[]>[]	callbacks;

	public PutSubstitution(final MacrosRuntime rt, final Function<char[], char[]>[] callbacks) {
		this.callbacks = callbacks;
	}

	@Override
	public long execute(final MacrosRuntime rt) {
		rt.resetBuffer();
		for(Function<char[], char[]> item : callbacks) {
			rt.append(item.apply(null));
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