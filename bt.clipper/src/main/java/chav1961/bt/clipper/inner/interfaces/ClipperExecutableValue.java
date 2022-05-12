package chav1961.bt.clipper.inner.interfaces;

import chav1961.bt.clipper.ClipperRuntime;
import chav1961.bt.clipper.inner.vm.PCodeExecutor;
import chav1961.purelib.basic.exceptions.ContentException;

public interface ClipperExecutableValue extends ClipperValue {
	int getLocalStackSize();
	
	ClipperValue invoke(final int parameterCount, final ClipperValue... parameters) throws ContentException;
	
	default ClipperValue invoke(final ClipperValue... parameters) throws ContentException {
		return invoke(parameters.length, parameters);
	}
	
	default ClipperValue invoke(final PCodeExecutor executor, final ClipperRuntime runtime, final StackFrame stackFrame, final ClipperValue... parameters) throws ContentException {
		return invoke(executor, runtime, stackFrame, parameters.length, parameters);
	}

	default ClipperValue invoke(final PCodeExecutor executor, final ClipperRuntime runtime, final StackFrame stackFrame, final int parameterCount, final ClipperValue... parameters) throws ContentException {
		return invoke(parameterCount, parameters);
	}
}
