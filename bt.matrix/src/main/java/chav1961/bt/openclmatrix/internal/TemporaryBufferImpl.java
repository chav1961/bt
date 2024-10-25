package chav1961.bt.openclmatrix.internal;

import java.io.IOException;
import java.util.function.Consumer;

import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryBuffer;

class TemporaryBufferImpl implements TemporaryBuffer {
	private final TemporaryStoreImpl			owner;
	private final Consumer<TemporaryBufferImpl>	onCloseCallback;
	
	TemporaryBufferImpl(final TemporaryStoreImpl owner, final Consumer<TemporaryBufferImpl> onCloseCallback) {
		this.owner = owner;
		this.onCloseCallback = onCloseCallback;
	}
	
	@Override
	public void close() throws IOException {
		onCloseCallback.accept(this);
	}

	@Override
	public int read(byte[] content, int from, int len) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void write(byte[] content, int from, int len) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
