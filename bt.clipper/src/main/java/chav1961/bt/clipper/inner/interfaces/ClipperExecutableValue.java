package chav1961.bt.clipper.inner.interfaces;

import chav1961.bt.clipper.ClipperRuntime;
import chav1961.bt.clipper.inner.vm.PCodeExecutor;
import chav1961.purelib.basic.exceptions.ContentException;

public interface ClipperExecutableValue extends ClipperValue {
	int getLocalStackSize();
	ClipperValue invoke(ClipperValue... parameters) throws ContentException;
	default ClipperValue invoke(PCodeExecutor executor, ClipperRuntime runtime, StackFrame stackFrame, ClipperValue... parameters) throws ContentException {
		return invoke(parameters);
	}
}
