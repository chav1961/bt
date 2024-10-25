package chav1961.bt.openclmatrix.internal;

import java.io.Closeable;
import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;

import chav1961.bt.openclmatrix.spi.OpenCLDescriptor.OpenCLContext;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.matrix.interfaces.Matrix;

public class GPUExecutor {
	public static interface TemporaryBuffer extends Closeable {
		int read(byte[] content, final int from, final int len) throws IOException;
		void write(byte[] content, final int from, final int len) throws IOException;
	}
	
	public static interface TemporaryStore extends Closeable {
		TemporaryBuffer getBuffer(final long address, final int size) throws IOException;
	}
	
	public static interface GPUEvent {
		void awaitAll(GPUEvent... events) throws InterruptedException;
		void awaitCurrent() throws InterruptedException;
		void onFired(BiConsumer<GPUEvent, Object> callback, Object cargo) throws InterruptedException;
	}
	
	public static interface GPUBuffer extends AutoCloseable {
		GPUEvent download(byte[] content);
		GPUEvent download(DataInput content) throws IOException;
		GPUEvent download(Matrix.Piece piece, Matrix content) throws IOException;
		GPUEvent download(TemporaryBuffer buffer) throws IOException;
		GPUEvent upload(byte[] content);
		GPUEvent upload(DataInput content) throws IOException;
		GPUEvent upload(Matrix.Piece piece, Matrix content) throws IOException;
		GPUEvent upload(TemporaryBuffer buffer) throws IOException;
		@Override void close() throws RuntimeException;
	}
	
	public static interface GPUExecutable extends AutoCloseable {
		GPUEvent execute(Object... parameters);
		@Override void close() throws RuntimeException;
	}
	
	public static interface GPUScheduler extends AutoCloseable {
		GPUEvent createEvent();
		TemporaryStore allocateTemporaryStore(long storeSize) throws IOException;
		GPUBuffer allocateGPUBuffer(int bufferSize) throws ContentException;
		GPUExecutable compile(String gpuProgram) throws SyntaxException;
		@Override void close() throws RuntimeException;
	}

	final OpenCLContext	context;
	private final File	contentDir;
	
	public GPUExecutor(final OpenCLContext context, final File contentDir) {
		if (context == null) {
			throw new NullPointerException("Context can't be null");
		}
		else {
			this.context = context;
			this.contentDir = contentDir;
		}
	}
	
	public GPUScheduler startTransaction() {
		return new GPUSchedulerImpl(context, contentDir);
	}
}
