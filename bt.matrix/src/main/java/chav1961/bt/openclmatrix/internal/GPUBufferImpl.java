package chav1961.bt.openclmatrix.internal;

import java.io.DataInput;
import java.io.IOException;
import java.util.function.Consumer;

import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUEvent;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryBuffer;
import chav1961.bt.openclmatrix.spi.OpenCLDescriptor.OpenCLContext;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.Piece;

class GPUBufferImpl implements GPUBuffer {
	private final OpenCLContext			owner;
	private final Consumer<GPUBuffer>	onCloseCallback;
	
	GPUBufferImpl(final OpenCLContext owner, final Consumer<GPUBuffer> onCloseCallback) {
		this.owner = owner;
		this.onCloseCallback = onCloseCallback;
	}

	@Override
	public void close() throws RuntimeException {
		// TODO Auto-generated method stub
		onCloseCallback.accept(this);
	}

	@Override
	public GPUEvent download(byte[] content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GPUEvent download(DataInput content) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GPUEvent download(Piece arg0, Matrix arg1) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GPUEvent download(TemporaryBuffer buffer) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GPUEvent upload(byte[] content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GPUEvent upload(DataInput content) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GPUEvent upload(Piece arg0, Matrix arg1) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GPUEvent upload(TemporaryBuffer buffer) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
