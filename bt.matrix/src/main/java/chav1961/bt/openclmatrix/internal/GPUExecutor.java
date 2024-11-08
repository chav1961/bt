package chav1961.bt.openclmatrix.internal;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jocl.EventCallbackFunction;

import chav1961.bt.openclmatrix.spi.OpenCLDescriptor.OpenCLContext;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.matrix.interfaces.Matrix;

public class GPUExecutor implements AutoCloseable {
	public static interface TemporaryBuffer extends Closeable, Flushable {
		long getAddress();
		int getSize();
		int position();
		int seek(int newPos);
		int read(byte[] content, final int to, final int len) throws IOException;
		int write(byte[] content, final int from, final int len) throws IOException;
		int read(int[] content, final int to, final int len) throws IOException;
		int write(int[] content, final int from, final int len) throws IOException;
		int read(long[] content, final int to, final int len) throws IOException;
		int write(long[] content, final int from, final int len) throws IOException;
		int read(float[] content, final int to, final int len) throws IOException;
		int write(float[] content, final int from, final int len) throws IOException;
		int read(double[] content, final int to, final int len) throws IOException;
		int write(double[] content, final int from, final int len) throws IOException;
	}
	
	public static interface TemporaryStore extends Closeable {
		long getSize() throws IOException;
		TemporaryBuffer getBuffer(final long address, final int size) throws IOException;
	}
	
	public static interface GPUEvent extends AutoCloseable {
		GPUEvent awaitAll(boolean closeAfterComplete, GPUEvent... events) throws InterruptedException, CalculationException;
		GPUEvent awaitCurrent() throws InterruptedException, CalculationException;
		void post();
		void post(CalculationException exc);
		@Override void close() throws RuntimeException;
	}
	
	public static interface GPUBuffer extends AutoCloseable {
		int	MAX_GPU_BUFFER_SIZE = 64 * 1024 * 1024;	
		
		int getSize();
		GPUEvent download(DataInput content, Matrix.Type type) throws IOException;
		GPUEvent download(Matrix.Piece piece, Matrix content) throws IOException;
		GPUEvent download(TemporaryBuffer buffer, Matrix.Type type) throws IOException;
		GPUEvent upload(DataOutput content, Matrix.Type type) throws IOException;
		GPUEvent upload(Matrix.Piece piece, Matrix content) throws IOException;
		GPUEvent upload(TemporaryBuffer buffer, Matrix.Type type) throws IOException;
		@Override void close() throws RuntimeException;
	}
	
	public static interface GPUExecutable extends AutoCloseable {
		String getName();
		void executeAfter(GPUEvent[] events, GPUEvent event, long[] dimensions, Object... parameters);
		void execute(GPUEvent event, long[] dimensions, Object... parameters);
		@Override void close() throws RuntimeException;
	}
	
	public static interface GPUScheduler extends AutoCloseable {
		GPUEvent createEvent();
		GPUEvent createEvent(EventCallbackFunction callback);
		TemporaryStore allocateTemporaryStore(long storeSize) throws IOException;
		TemporaryStore allocateTemporaryStore(File storeDir, long storeSize) throws IOException;
		GPUBuffer allocateGPUBuffer(int bufferSize) throws ContentException;
		@Override void close() throws RuntimeException;
	}

	final OpenCLContext	context;
	private final File	contentDir;
	private final Map<String, GPUExecutable>	programs = new HashMap<>();
	
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

	public GPUExecutable compile(final String name, final String gpuProgram) throws SyntaxException {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Program name can't be null or empty");
		}
		else if (Utils.checkEmptyOrNullString(gpuProgram)) {
			throw new IllegalArgumentException("GPU program can't be null or empty");
		}
		else if (programs.containsKey(name)) {
			throw new IllegalArgumentException("GPU program ["+name+"] is already compiled");
		}
		else {
			final GPUExecutable	ex = new GPUExecutableImpl(context, name, gpuProgram, (p)->programs.remove(p.getName())); 
			
			programs.put(name, ex);
			return ex;
		}
	}

	public boolean hasProgram(final String name) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Program name can't be null or empty");
		}
		else {
			return programs.containsKey(name);
		}
	}
	
	public GPUExecutable getProgram(final String name) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Program name can't be null or empty");
		}
		else if (!hasProgram(name)) {
			throw new IllegalArgumentException("Program name ["+name+"] not found");
		}
		else {
			return programs.get(name);
		}
	}
	
	@Override
	public void close() throws RuntimeException {
		for(Map.Entry<String, GPUExecutable> item : programs.entrySet()) {
			item.getValue().close();
		}
	}
}
