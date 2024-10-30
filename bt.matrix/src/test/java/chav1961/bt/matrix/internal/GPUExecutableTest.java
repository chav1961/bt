package chav1961.bt.matrix.internal;

import java.io.EOFException;
import java.io.IOException;

import org.jocl.Sizeof;
import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.openclmatrix.internal.GPUExecutor;
import chav1961.bt.openclmatrix.internal.InternalUtils;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUEvent;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUExecutable;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUScheduler;
import chav1961.bt.openclmatrix.spi.OpenCLDescriptor;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.streams.DataOutputAdapter;

public class GPUExecutableTest {
	private static String PROGRAM = "__kernel void test(const float val,"+
							        "             __global float *arr)"+
							        "{"+
							        "    int gid = get_global_id(0);"+
							        "    arr[gid] = val;"+
							        "}";	


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
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try {
				exec.hasProgram("");
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			
			Assert.assertEquals(pgm, exec.getProgram("test"));
			
			try {
				exec.getProgram(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try {
				exec.getProgram("");
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
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
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try{
					pgm.execute(e2, null, 1);
					Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
				} catch (IllegalArgumentException exc) {
				}
				try{
					pgm.execute(e2, new long[0], 1);
					Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
				} catch (IllegalArgumentException exc) {
				}
				try{
					pgm.execute(e2, new long[4], 1);
					Assert.fail("Mandatory exception was not detected (too long 2-nd argument)");
				} catch (IllegalArgumentException exc) {
				}
				try{
					pgm.execute(e2, new long[1], (Object[])null);
					Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
				} catch (IllegalArgumentException exc) {
				}
				try{
					pgm.execute(e2, new long[1]);
					Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
				} catch (IllegalArgumentException exc) {
				}
			}
		}
	}

}
