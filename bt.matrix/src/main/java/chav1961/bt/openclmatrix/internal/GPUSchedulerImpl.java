package chav1961.bt.openclmatrix.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUEvent;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUExecutable;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUScheduler;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryStore;
import chav1961.bt.openclmatrix.spi.OpenCLDescriptor.OpenCLContext;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;

class GPUSchedulerImpl implements GPUScheduler {
	private final OpenCLContext			owner;
	private final File					contentDir;
	private final List<TemporaryStore>	stores = new ArrayList<>();
	private final List<GPUBuffer>		buffers = new ArrayList<>();
	private final List<GPUExecutable>	programs = new ArrayList<>();

	GPUSchedulerImpl(final OpenCLContext owner, final File contentDir) {
		this.owner = owner;
		this.contentDir = contentDir;
	}
	
	@Override
	public void close() throws RuntimeException {
		for (TemporaryStore item : stores) {
			try {
				item.close();
			} catch (IOException e) {
			}
		}
		for (GPUBuffer item : buffers) {
			item.close();
		}
		for (GPUExecutable item : programs) {
			item.close();
		}
	}

	@Override
	public GPUEvent createEvent() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public TemporaryStore allocateTemporaryStore(final long storeSize) throws IOException {
		final TemporaryStore	ts = new TemporaryStoreImpl(contentDir, storeSize, (t)->stores.remove(t));

		stores.add(ts);
		return ts;
	}

	@Override
	public GPUBuffer allocateGPUBuffer(final int bufferSize) throws ContentException {
		final GPUBuffer	buf = new GPUBufferImpl(owner, (b)->buffers.remove(b));
		
		buffers.add(buf);
		return buf;
	}

	@Override
	public GPUExecutable compile(final String gpuProgram) throws SyntaxException {
		final GPUExecutable	ex = new GPUExecutableImpl(owner, gpuProgram, (p)->programs.remove(p)); 
		
		programs.add(ex);
		return ex;
	}
}
