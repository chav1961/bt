package chav1961.bt.openclmatrix.internal;

import java.util.function.Consumer;

import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUEvent;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUExecutable;
import chav1961.bt.openclmatrix.spi.OpenCLDescriptor.OpenCLContext;
import chav1961.purelib.basic.exceptions.SyntaxException;

class GPUExecutableImpl implements GPUExecutable {
	private final OpenCLContext				owner;
	private final Consumer<GPUExecutable> 	onCloseCallback;

	GPUExecutableImpl(final OpenCLContext owner, final String program, final Consumer<GPUExecutable> onCloseCallback) throws SyntaxException {
		this.owner = owner;
		this.onCloseCallback = onCloseCallback;
	}
	
	@Override
	public void close() throws RuntimeException {
		// TODO Auto-generated method stub
		onCloseCallback.accept(this);
	}

	@Override
	public GPUEvent execute(Object... parameters) {
		// TODO Auto-generated method stub
		return null;
	}

}
