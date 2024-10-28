package chav1961.bt.openclmatrix.internal;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;

import org.jocl.EventCallbackFunction;

import chav1961.bt.openclmatrix.spi.OpenCLDescriptor.OpenCLContext;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.matrix.interfaces.Matrix;

public class GPUExecutor {
	public static interface TemporaryBuffer extends Closeable {
		long getAddress();
		int getSize();
		int position();
		int seek(int newPos);
		void read(byte[] content, final int to, final int len) throws IOException;
		void write(byte[] content, final int from, final int len) throws IOException;
	}
	
	public static interface TemporaryStore extends Closeable {
		long getSize() throws IOException;
		TemporaryBuffer getBuffer(final long address, final int size) throws IOException;
	}
	
	public static interface GPUEvent extends AutoCloseable {
		void awaitAll(boolean closeAfterComplete, GPUEvent... events) throws InterruptedException;
		void awaitCurrent() throws InterruptedException;
		void post();
		@Override void close() throws RuntimeException;
	}
	
	public static interface GPUBuffer extends AutoCloseable {
		GPUEvent download(DataInput content, Matrix.Type type) throws IOException;
		GPUEvent download(Matrix.Piece piece, Matrix content) throws IOException;
		GPUEvent download(TemporaryBuffer buffer) throws IOException;
		GPUEvent upload(DataOutput content, Matrix.Type type) throws IOException;
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
		GPUEvent createEvent(EventCallbackFunction callback);
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
