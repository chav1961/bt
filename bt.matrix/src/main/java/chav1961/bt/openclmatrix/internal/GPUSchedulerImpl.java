package chav1961.bt.openclmatrix.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jocl.CL;
import org.jocl.EventCallbackFunction;

import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUEvent;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUExecutable;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUScheduler;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryStore;
import chav1961.bt.openclmatrix.spi.OpenCLDescriptor.OpenCLContext;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;



class GPUSchedulerImpl implements GPUScheduler {
	static final int					MAX_BUFFER_SIZE = 128 * 1024 * 1024;
	
	private final File					contentDir;
	private final List<TemporaryStore>	stores = new ArrayList<>();
	private final List<GPUBuffer>		buffers = new ArrayList<>();
	private final List<GPUExecutable>	programs = new ArrayList<>();
	private final List<GPUEvent>		events = new ArrayList<>();
	final OpenCLContext					owner;

	GPUSchedulerImpl(final OpenCLContext owner, final File contentDir) {
		this.owner = owner;
		this.contentDir = contentDir;
	}
	
	@Override
	public void close() throws RuntimeException {
		for (int index = stores.size()-1; index >=0; index--) {
			try {
				stores.get(index).close();
			} catch (IOException e) {
			}
		}
		for (GPUBuffer item : buffers) {
			item.close();
		}
		for (GPUExecutable item : programs) {
			item.close();
		}
		for (int index = events.size()-1; index >=0; index--) {
			events.get(index).close();
		}
	}

	@Override
	public GPUEvent createEvent() {
		return new GPUEventImpl(CL.clCreateUserEvent(owner.context, null), (t)->events.remove(t));
	}

	@Override
	public GPUEvent createEvent(final EventCallbackFunction callback) {
		if (callback == null) {
			throw new NullPointerException("Event callback can't be null");
		}
		else {
			final GPUEventImpl	event = (GPUEventImpl)createEvent();
			
			CL.clSetEventCallback(event.event, CL.CL_COMPLETE, (ev, stat, obj)->callback.function(ev, stat, obj), null);
			return event;
		}
	}
	
	@Override
	public TemporaryStore allocateTemporaryStore(final long storeSize) throws IOException {
		if (storeSize <= 0) {
			throw new IllegalArgumentException("Store size must be greater than 0");
		}
		else {
			final TemporaryStore	ts = new TemporaryStoreImpl(contentDir, storeSize, (t)->stores.remove(t));

			stores.add(ts);
			return ts;
		}
	}

	@Override
	public GPUBuffer allocateGPUBuffer(final int bufferSize) throws ContentException {
		if (bufferSize <= 0 || bufferSize > MAX_BUFFER_SIZE) {
			throw new IllegalArgumentException("Buffer size ["+bufferSize+"] out of range 1.."+MAX_BUFFER_SIZE);
		}
		else {
			final GPUBuffer	buf = new GPUBufferImpl(this, bufferSize, (b)->buffers.remove(b));
			
			buffers.add(buf);
			return buf;
		}
	}

	@Override
	public GPUExecutable compile(final String gpuProgram) throws SyntaxException {
		final GPUExecutable	ex = new GPUExecutableImpl(owner, gpuProgram, (p)->programs.remove(p)); 
		
		programs.add(ex);
		return ex;
	}

}
