package chav1961.bt.matrix.internal;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.openclmatrix.internal.GPUExecutor;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUScheduler;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryStore;
import chav1961.bt.openclmatrix.internal.InternalUtils;
import chav1961.bt.openclmatrix.spi.OpenCLDescriptor;
import chav1961.purelib.basic.exceptions.ContentException;

public class GPUExecutorTest {

	@Test
	public void basicTest() throws ContentException {
		try(final OpenCLDescriptor	desc = new OpenCLDescriptor()) {
			final GPUExecutor		exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION);

			try(final GPUScheduler	sched = exec.startTransaction()) {
				
			}
		}
	}
	
	@Test
	public void temporaryStoreTest() throws ContentException, IOException {
		try(final OpenCLDescriptor		desc = new OpenCLDescriptor()) {
			final GPUExecutor			exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION);

			try(final GPUScheduler		sched = exec.startTransaction();
				final TemporaryStore	ts = sched.allocateTemporaryStore(1024 * 1024)) {
				
				
			}
		}
	}

}
