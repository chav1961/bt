package chav1961.bt.matrix.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

import org.jocl.Sizeof;
import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.openclmatrix.internal.GPUExecutor;
import chav1961.bt.openclmatrix.internal.InternalUtils;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUEvent;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUScheduler;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryStore;
import chav1961.bt.openclmatrix.spi.OpenCLDescriptor;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.Type;
import chav1961.purelib.streams.DataInputAdapter;
import chav1961.purelib.streams.DataOutputAdapter;

public class GPUBufferTest {
	@Test
	public void exceptionsTest() throws ContentException, IOException, InterruptedException, CalculationException, RuntimeException {
		try(final OpenCLDescriptor		desc = new OpenCLDescriptor();
			final GPUExecutor			exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION);
			final GPUScheduler			sched = exec.startTransaction()) {
			
			try{sched.allocateGPUBuffer(0);
				Assert.fail("Mandatory exception was not detected (1-st argument out of range");
			} catch (IllegalArgumentException exc) {
			}
			try{sched.allocateGPUBuffer(1024 * 1024 * 1024);
				Assert.fail("Mandatory exception was not detected (1-st argument out of range");
			} catch (IllegalArgumentException exc) {
			}
			
			try(final GPUBuffer			buf = sched.allocateGPUBuffer(Sizeof.cl_int * 1024 * 1024);
				final TemporaryStore	ts = sched.allocateTemporaryStore(Sizeof.cl_int * 1024 * 1024);
				final TemporaryBuffer	tb = ts.getBuffer(0, Sizeof.cl_int * 1024 * 1024)) {
				
				try {
					buf.download((DataInput)null, Type.REAL_INT);
					Assert.fail("Mandatory exception was not detected (null 1-st argument");
				} catch (NullPointerException exc) {
				}
				try {
					buf.download(new DataInputAdapter(), null);
					Assert.fail("Mandatory exception was not detected (null 2-nd argument");
				} catch (NullPointerException exc) {
				}
				
				try {
					buf.download((TemporaryBuffer)null, Type.REAL_INT);
					Assert.fail("Mandatory exception was not detected (null 1-st argument");
				} catch (NullPointerException exc) {
				}
				try {
					buf.download(tb, null);
					Assert.fail("Mandatory exception was not detected (null 2-nd argument");
				} catch (NullPointerException exc) {
				}
				
				try {
					buf.upload((DataOutput)null, Type.REAL_INT);
					Assert.fail("Mandatory exception was not detected (null 1-st argument");
				} catch (NullPointerException exc) {
				}
				try {
					buf.upload(new DataOutputAdapter(), null);
					Assert.fail("Mandatory exception was not detected (null 2-nd argument");
				} catch (NullPointerException exc) {
				}

				try {
					buf.upload((TemporaryBuffer)null, Type.REAL_INT);
					Assert.fail("Mandatory exception was not detected (null 1-st argument");
				} catch (NullPointerException exc) {
				}
				try {
					buf.upload(tb, null);
					Assert.fail("Mandatory exception was not detected (null 2-nd argument");
				} catch (NullPointerException exc) {
				}
			}
		}
	}
	
	@Test
	public void intBufferTest() throws ContentException, IOException, InterruptedException, CalculationException, RuntimeException {
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
			}
		}
	}	

	@Test
	public void longBufferTest() throws ContentException, IOException, InterruptedException, CalculationException, RuntimeException {
		try(final OpenCLDescriptor		desc = new OpenCLDescriptor();
			final GPUExecutor			exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION)) {

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

			// Long temp buffer
			
			try(final GPUScheduler		sched = exec.startTransaction();
				final GPUBuffer			buf = sched.allocateGPUBuffer(Sizeof.cl_long * 1024 * 1024);
				final TemporaryStore	ts = sched.allocateTemporaryStore(Sizeof.cl_long * 1024 * 1024);
				final TemporaryBuffer	tb = ts.getBuffer(0, Sizeof.cl_long * 1024 * 1024)) {
				final long[]			temp = new long[1024 * 1024];
				
				Arrays.fill(temp, 1);				
				tb.write(temp, 0, temp.length);
				
				final GPUEvent 	e1 = buf.download(tb, Matrix.Type.REAL_LONG);
				e1.awaitCurrent().close();
				
				final GPUEvent 	e2 = buf.upload(tb, Matrix.Type.REAL_LONG);
				e2.awaitCurrent().close();

				Arrays.fill(temp, 2);
				tb.read(temp, 0, temp.length);
				Assert.assertEquals(1, temp[temp.length-1]);
			}
		}
	}	

	@Test
	public void floatBufferTest() throws ContentException, IOException, InterruptedException, CalculationException, RuntimeException {
		try(final OpenCLDescriptor		desc = new OpenCLDescriptor();
			final GPUExecutor			exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION)) {

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

			// Float temp buffer
			
			try(final GPUScheduler		sched = exec.startTransaction();
				final GPUBuffer			buf = sched.allocateGPUBuffer(Sizeof.cl_float * 1024 * 1024);
				final TemporaryStore	ts = sched.allocateTemporaryStore(Sizeof.cl_float * 1024 * 1024);
				final TemporaryBuffer	tb = ts.getBuffer(0, Sizeof.cl_float * 1024 * 1024)) {
				final float[]			temp = new float[1024 * 1024];
				
				Arrays.fill(temp, 1);				
				tb.write(temp, 0, temp.length);
				
				final GPUEvent 	e1 = buf.download(tb, Matrix.Type.REAL_FLOAT);
				e1.awaitCurrent().close();
				
				final GPUEvent 	e2 = buf.upload(tb, Matrix.Type.REAL_FLOAT);
				e2.awaitCurrent().close();

				Arrays.fill(temp, 2);
				tb.read(temp, 0, temp.length);
				Assert.assertEquals(1, temp[temp.length-1], 0.001f);
			}
		}
	}	

	@Test
	public void doubleBufferTest() throws ContentException, IOException, InterruptedException, CalculationException, RuntimeException {
		try(final OpenCLDescriptor		desc = new OpenCLDescriptor();
			final GPUExecutor			exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION)) {

			// Float data stream
			
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
				
				Assert.assertEquals(1, temp[temp.length-1], 0.001f);
			}

			// Float temp buffer
			
			try(final GPUScheduler		sched = exec.startTransaction();
				final GPUBuffer			buf = sched.allocateGPUBuffer(Sizeof.cl_double * 1024 * 1024);
				final TemporaryStore	ts = sched.allocateTemporaryStore(Sizeof.cl_double * 1024 * 1024);
				final TemporaryBuffer	tb = ts.getBuffer(0, Sizeof.cl_double * 1024 * 1024)) {
				final double[]			temp = new double[1024 * 1024];
				
				Arrays.fill(temp, 1);				
				tb.write(temp, 0, temp.length);
				
				final GPUEvent 	e1 = buf.download(tb, Matrix.Type.REAL_DOUBLE);
				e1.awaitCurrent().close();
				
				final GPUEvent 	e2 = buf.upload(tb, Matrix.Type.REAL_DOUBLE);
				e2.awaitCurrent().close();

				Arrays.fill(temp, 2);
				tb.read(temp, 0, temp.length);
				Assert.assertEquals(1, temp[temp.length-1], 0.001f);
			}
		}
	}	
}
