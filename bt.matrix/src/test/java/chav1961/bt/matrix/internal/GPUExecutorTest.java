package chav1961.bt.matrix.internal;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

import org.jocl.Sizeof;
import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.openclmatrix.internal.GPUExecutor;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUEvent;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUExecutable;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUScheduler;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryStore;
import chav1961.bt.openclmatrix.internal.InternalUtils;
import chav1961.bt.openclmatrix.spi.OpenCLDescriptor;
import chav1961.purelib.basic.SimpleTimerTask;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.streams.DataInputAdapter;
import chav1961.purelib.streams.DataOutputAdapter;

public class GPUExecutorTest {
	
	private static String PROGRAM =
	        "__kernel void test(const float val,"+
	        "             __global float *arr)"+
	        "{"+
	        "    int gid = get_global_id(0);"+
	        "    arr[gid] = val;"+
	        "}";	

	@Test
	public void basicTest() throws ContentException {
		try(final OpenCLDescriptor	desc = new OpenCLDescriptor();
			final GPUExecutor		exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION)) {

			try(final GPUScheduler	sched = exec.startTransaction()) {
				
			}
		}
	}
	
	@Test
	public void temporaryStoreTest() throws ContentException, IOException {
		try(final OpenCLDescriptor		desc = new OpenCLDescriptor();
			final GPUExecutor			exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION)) {
			final String				testString = "test string"; 
			final byte[]				test = testString.getBytes();

			try(final GPUScheduler		sched = exec.startTransaction();
				final TemporaryStore	ts = sched.allocateTemporaryStore(1024 * 1024)) {
				
				try(final TemporaryBuffer 	buffer = ts.getBuffer(0, 1024)) {
					buffer.write(test, 0, test.length);
					
					try{buffer.write((byte[])null, 0, test.length);
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
					
					try{buffer.read((byte[])null, 0, test.length);
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
	public void gpuEventsTest() throws ContentException, IOException, InterruptedException, CalculationException {
		try(final OpenCLDescriptor		desc = new OpenCLDescriptor();
			final GPUExecutor			exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION)) {
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
	public void gpuBufferTest() throws ContentException, IOException, InterruptedException, CalculationException, RuntimeException {
		try(final OpenCLDescriptor		desc = new OpenCLDescriptor();
			final GPUExecutor			exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION)) {

			// Int data stream
			
			try(final GPUScheduler		sched = exec.startTransaction();
				final GPUBuffer			buf = sched.allocateGPUBuffer(Sizeof.cl_int * 1024 * 1024)) {
				final int[]				temp = new int[1024 * 1024];
				
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
				e1.awaitCurrent().close();
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
				e2.awaitCurrent().close();
				
				Assert.assertEquals(1, temp[temp.length-1]);
				
				try{sched.allocateGPUBuffer(0);
					Assert.fail("Mandatory exception was not detected (1-st argument out of range");
				} catch (IllegalArgumentException exc) {
				}
				try{sched.allocateGPUBuffer(1024 * 1024 * 1024);
					Assert.fail("Mandatory exception was not detected (1-st argument out of range");
				} catch (IllegalArgumentException exc) {
				}
			}
			
			// Long data stream
			
			try(final GPUScheduler		sched = exec.startTransaction();
				final GPUBuffer			buf = sched.allocateGPUBuffer(Sizeof.cl_long * 1024 * 1024)) {
				final long[]			temp = new long[1024 * 1024];
				
				Arrays.fill(temp, 1);
				
				final GPUEvent 	e1 = buf.download(new DataInputAdapter() {
										int index = 0;
										
										@Override
										public long readLong() throws IOException {
											if (index >= temp.length) {
												throw new EOFException();
											}
											else {
												return temp[index++];
											}
										}
									}, Matrix.Type.REAL_LONG);
				e1.awaitCurrent().close();
				Arrays.fill(temp, 2);
				
				final GPUEvent 	e2 = buf.upload(new DataOutputAdapter() {
										int index = 0;
										
										@Override
										public void writeLong(long v) throws IOException {
											if (index >= temp.length) {
												throw new EOFException();
											}
											else {
												temp[index++] = v;
											}
										}
									}, Matrix.Type.REAL_LONG);
				e2.awaitCurrent().close();
				
				Assert.assertEquals(1, temp[temp.length-1]);
			}

			// Float data stream
			
			try(final GPUScheduler		sched = exec.startTransaction();
				final GPUBuffer			buf = sched.allocateGPUBuffer(Sizeof.cl_float * 1024 * 1024)) {
				final float[]			temp = new float[1024 * 1024];
				
				Arrays.fill(temp, 1);
				
				final GPUEvent 	e1 = buf.download(new DataInputAdapter() {
										int index = 0;
										
										@Override
										public float readFloat() throws IOException {
											if (index >= temp.length) {
												throw new EOFException();
											}
											else {
												return temp[index++];
											}
										}
									}, Matrix.Type.REAL_FLOAT);
				e1.awaitCurrent().close();
				Arrays.fill(temp, 2);
				
				final GPUEvent 	e2 = buf.upload(new DataOutputAdapter() {
										int index = 0;
										
										@Override
										public void writeFloat(float v) throws IOException {
											if (index >= temp.length) {
												throw new EOFException();
											}
											else {
												temp[index++] = v;
											}
										}
									}, Matrix.Type.REAL_FLOAT);
				e2.awaitCurrent().close();
				
				Assert.assertEquals(1, temp[temp.length-1], 0.001f);
			}

			// Double data stream
			
			try(final GPUScheduler		sched = exec.startTransaction();
				final GPUBuffer			buf = sched.allocateGPUBuffer(Sizeof.cl_double * 1024 * 1024)) {
				final double[]			temp = new double[1024 * 1024];
				
				Arrays.fill(temp, 1);
				
				final GPUEvent 	e1 = buf.download(new DataInputAdapter() {
										int index = 0;
										
										@Override
										public double readDouble() throws IOException {
											if (index >= temp.length) {
												throw new EOFException();
											}
											else {
												return temp[index++];
											}
										}
									}, Matrix.Type.REAL_DOUBLE);
				e1.awaitCurrent().close();
				Arrays.fill(temp, 2);
				
				final GPUEvent 	e2 = buf.upload(new DataOutputAdapter() {
										int index = 0;
										
										@Override
										public void writeDouble(double v) throws IOException {
											if (index >= temp.length) {
												throw new EOFException();
											}
											else {
												temp[index++] = v;
											}
										}
									}, Matrix.Type.REAL_DOUBLE);
				e2.awaitCurrent().close();
				
				Assert.assertEquals(1, temp[temp.length-1], 0.001);
			}

			// Int temp buffer
			
			try(final GPUScheduler		sched = exec.startTransaction();
				final GPUBuffer			buf = sched.allocateGPUBuffer(Sizeof.cl_int * 1024 * 1024);
				final TemporaryStore	ts = sched.allocateTemporaryStore(Sizeof.cl_int * 1024 * 1024);
				final TemporaryBuffer	tb = ts.getBuffer(0, Sizeof.cl_int * 1024 * 1024)) {
				final int[]				temp = new int[1024 * 1024];
				
				Arrays.fill(temp, 1);				
				tb.write(temp, 0, temp.length);
				
				final GPUEvent 	e1 = buf.download(tb, Matrix.Type.REAL_INT);
				e1.awaitCurrent().close();
				
				final GPUEvent 	e2 = buf.upload(tb, Matrix.Type.REAL_INT);
				e2.awaitCurrent().close();

				Arrays.fill(temp, 2);
				tb.read(temp, 0, temp.length);
				Assert.assertEquals(1, temp[temp.length-1]);
				
				try{sched.allocateGPUBuffer(0);
					Assert.fail("Mandatory exception was not detected (1-st argument out of range");
				} catch (IllegalArgumentException exc) {
				}
				try{sched.allocateGPUBuffer(1024 * 1024 * 1024);
					Assert.fail("Mandatory exception was not detected (1-st argument out of range");
				} catch (IllegalArgumentException exc) {
				}
			}
		}
	}	

	@Test
	public void gpuProgramTest() throws ContentException, IOException, InterruptedException, CalculationException, RuntimeException {
		try(final OpenCLDescriptor	desc = new OpenCLDescriptor();
			final GPUExecutor		exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION);
			final GPUExecutable		pgm = exec.compile("test", PROGRAM)) {

			Assert.assertEquals("test", pgm.getName());
			Assert.assertTrue(exec.hasProgram("test"));
			Assert.assertFalse(exec.hasProgram("unknown"));
			
			try {
				exec.hasProgram(null);
				Assert.fail("Mandatoryt exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try {
				exec.hasProgram("");
				Assert.fail("Mandatoryt exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			
			Assert.assertEquals(pgm, exec.getProgram("test"));
			
			try {
				exec.getProgram(null);
				Assert.fail("Mandatoryt exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try {
				exec.getProgram("");
				Assert.fail("Mandatoryt exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try {
				exec.getProgram("unknown");
				Assert.fail("Mandatoryt exception was not detected (1-st argument not found)");
			} catch (IllegalArgumentException exc) {
			}
			
			try(final GPUScheduler	sched = exec.startTransaction();
				final GPUBuffer		buf = sched.allocateGPUBuffer(Sizeof.cl_float * 1024 * 1024)) {
				final float[]		temp = new float[1024 * 1024];
				final GPUEvent		e1 = sched.createEvent();
				
				pgm.execute(e1, new long[]{buf.getSize()/Sizeof.cl_float}, 100f, buf);
				e1.awaitCurrent().close();
				
				final GPUEvent 	e2 = buf.upload(new DataOutputAdapter() {
									int index = 0;
									
									@Override
									public void writeFloat(float v) throws IOException {
										if (index >= temp.length) {
											throw new EOFException();
										}
										else {
											temp[index++] = v;
										}
									}
								}, Matrix.Type.REAL_FLOAT);
				e2.awaitCurrent().close();
				
				Assert.assertEquals(100f, temp[temp.length-1], 0.001f);
				
				try{
					pgm.execute(null, new long[] {1}, 1);
					Assert.fail("Mandatoryt exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try{
					pgm.execute(e2, null, 1);
					Assert.fail("Mandatoryt exception was not detected (null 2-nd argument)");
				} catch (IllegalArgumentException exc) {
				}
				try{
					pgm.execute(e2, new long[0], 1);
					Assert.fail("Mandatoryt exception was not detected (empty 2-nd argument)");
				} catch (IllegalArgumentException exc) {
				}
				try{
					pgm.execute(e2, new long[4], 1);
					Assert.fail("Mandatoryt exception was not detected (too long 2-nd argument)");
				} catch (IllegalArgumentException exc) {
				}
				try{
					pgm.execute(e2, new long[1], (Object[])null);
					Assert.fail("Mandatoryt exception was not detected (null 3-rd argument)");
				} catch (IllegalArgumentException exc) {
				}
				try{
					pgm.execute(e2, new long[1]);
					Assert.fail("Mandatoryt exception was not detected (empty 3-rd argument)");
				} catch (IllegalArgumentException exc) {
				}
			}
		}
	}
}
