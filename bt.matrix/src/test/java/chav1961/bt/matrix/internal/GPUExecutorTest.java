package chav1961.bt.matrix.internal;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.openclmatrix.internal.GPUExecutor;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUEvent;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUScheduler;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryStore;
import chav1961.bt.openclmatrix.internal.InternalUtils;
import chav1961.bt.openclmatrix.spi.OpenCLDescriptor;
import chav1961.purelib.basic.SimpleTimerTask;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.streams.DataInputAdapter;
import chav1961.purelib.streams.DataOutputAdapter;

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
			final String				testString = "test string"; 
			final byte[]				test = testString.getBytes();

			try(final GPUScheduler		sched = exec.startTransaction();
				final TemporaryStore	ts = sched.allocateTemporaryStore(1024 * 1024)) {
				
				try(final TemporaryBuffer 	buffer = ts.getBuffer(0, 1024)) {
					buffer.write(test, 0, test.length);
					
					try{buffer.write(null, 0, test.length);
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (NullPointerException exc) {
					}
					try{buffer.write(test, -1, test.length);
						Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
					} catch (IllegalArgumentException exc) {
					}
					try{buffer.write(test, 0, 2 * test.length);
						Assert.fail("Mandatory exception was not detected (2-nd+3-rd argument out of range)");
					} catch (IllegalArgumentException exc) {
					}
				}
				try(final TemporaryBuffer 	buffer = ts.getBuffer(0, 1024)) {
					final byte[]		temp = new byte[test.length];
							
					buffer.read(temp, 0, test.length);
					Assert.assertEquals(testString, new String(temp, 0, test.length));
					
					try{buffer.read(null, 0, test.length);
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (NullPointerException exc) {
					}
					try{buffer.read(temp, -1, test.length);
						Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
					} catch (IllegalArgumentException exc) {
					}
					try{buffer.read(temp, 0, 2 * test.length);
						Assert.fail("Mandatory exception was not detected (2-nd+3-rd argument out of range)");
					} catch (IllegalArgumentException exc) {
					}
				}
				
				try{ts.getBuffer(-1, 1024);
					Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				try{ts.getBuffer(0, 2 * 1024 * 1024);
					Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				
				try{sched.allocateTemporaryStore(0);
					Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
			}
		}
	}

	@Test
	public void gpuEventsTest() throws ContentException, IOException, InterruptedException {
		try(final OpenCLDescriptor		desc = new OpenCLDescriptor()) {
			final GPUExecutor			exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION);
			final boolean[]				marks = new boolean[] {false, false, false};

			try(final GPUScheduler		sched = exec.startTransaction();
				final GPUEvent			ev1 = sched.createEvent((ev, stat, cargo)->marks[0] = true);
				final GPUEvent			ev2 = sched.createEvent((ev, stat, cargo)->marks[1] = true);
				final GPUEvent			ev3 = sched.createEvent((ev, stat, cargo)->marks[2] = true)) {
				
				SimpleTimerTask.start(()->{
					ev1.post();
				}, 500);
				SimpleTimerTask.start(()->{
					ev2.post();
				}, 500);
				ev3.awaitAll(true, ev1, ev2);
				Assert.assertArrayEquals(new boolean[] {true,  true, true}, marks);
				
				try{sched.createEvent(null);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
			}
		}
	}	
	
	
	@Test
	public void gpuBufferTest() throws ContentException, IOException, InterruptedException {
		try(final OpenCLDescriptor		desc = new OpenCLDescriptor()) {
			final GPUExecutor			exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION);
			final int[]					temp = new int[1024 * 1024];

			try(final GPUScheduler		sched = exec.startTransaction();
				final GPUBuffer			buf = sched.allocateGPUBuffer(4 * 1024 * 1024)) {
				
				Arrays.fill(temp, 1);
				
				final GPUEvent 	e1 = buf.download(new DataInputAdapter() {
										int index = 0;
										
										@Override
										public int readInt() throws IOException {
											if (index >= temp.length) {
												throw new EOFException();
											}
											else {
												return temp[index++];
											}
										}
									}, Matrix.Type.REAL_INT);
				e1.awaitCurrent();
				e1.close();
				Arrays.fill(temp, 2);
				
				final GPUEvent 	e2 = buf.upload(new DataOutputAdapter() {
										int index = 0;
										
										@Override
										public void writeInt(int v) throws IOException {
											if (index >= temp.length) {
												throw new EOFException();
											}
											else {
												temp[index++] = v;
											}
										}
									}, Matrix.Type.REAL_INT);
				e2.awaitCurrent();
				e2.close();
				
				Assert.assertEquals(1, temp[temp.length-1]);
			}
		}
	}	
}
