package chav1961.bt.matrix.internal;

import org.junit.Assert;
import java.io.IOException;

import org.junit.Test;

import chav1961.bt.openclmatrix.internal.GPUExecutor;
import chav1961.bt.openclmatrix.internal.InternalUtils;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUScheduler;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryStore;
import chav1961.bt.openclmatrix.spi.OpenCLDescriptor;
import chav1961.purelib.basic.exceptions.ContentException;

public class TemporaryStoreTest {

	@Test
	public void byteContentTest() throws ContentException, IOException {
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
	public void intContentTest() throws ContentException, IOException {
		try(final OpenCLDescriptor		desc = new OpenCLDescriptor();
			final GPUExecutor			exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION)) {
			final int[]					test = new int[] {1,2,3,4,5,6,7,8,9,10};

			try(final GPUScheduler		sched = exec.startTransaction();
				final TemporaryStore	ts = sched.allocateTemporaryStore(1024 * 1024)) {
				
				try(final TemporaryBuffer 	buffer = ts.getBuffer(0, 1024)) {
					buffer.write(test, 0, test.length);
					
					try{buffer.write((int[])null, 0, test.length);
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
					final int[]			temp = new int[test.length];
							
					buffer.read(temp, 0, test.length);
					Assert.assertArrayEquals(test, temp);
					
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
			}
		}
	}
	
	@Test
	public void longContentTest() throws ContentException, IOException {
		try(final OpenCLDescriptor		desc = new OpenCLDescriptor();
			final GPUExecutor			exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION)) {
			final long[]				test = new long[] {1,2,3,4,5,6,7,8,9,10};

			try(final GPUScheduler		sched = exec.startTransaction();
				final TemporaryStore	ts = sched.allocateTemporaryStore(1024 * 1024)) {
				
				try(final TemporaryBuffer 	buffer = ts.getBuffer(0, 1024)) {
					buffer.write(test, 0, test.length);
					
					try{buffer.write((long[])null, 0, test.length);
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
					final long[]		temp = new long[test.length];
							
					buffer.read(temp, 0, test.length);
					Assert.assertArrayEquals(test, temp);
					
					try{buffer.read((long[])null, 0, test.length);
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
			}
		}
	}

	@Test
	public void floatContentTest() throws ContentException, IOException {
		try(final OpenCLDescriptor		desc = new OpenCLDescriptor();
			final GPUExecutor			exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION)) {
			final float[]				test = new float[] {1,2,3,4,5,6,7,8,9,10};

			try(final GPUScheduler		sched = exec.startTransaction();
				final TemporaryStore	ts = sched.allocateTemporaryStore(1024 * 1024)) {
				
				try(final TemporaryBuffer 	buffer = ts.getBuffer(0, 1024)) {
					buffer.write(test, 0, test.length);
					
					try{buffer.write((float[])null, 0, test.length);
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
					final float[]		temp = new float[test.length];
							
					buffer.read(temp, 0, test.length);
					Assert.assertArrayEquals(test, temp, 0.001f);
					 
					try{buffer.read((float[])null, 0, test.length);
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
			}
		}
	}

	@Test
	public void doubleContentTest() throws ContentException, IOException {
		try(final OpenCLDescriptor		desc = new OpenCLDescriptor();
			final GPUExecutor			exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION)) {
			final double[]				test = new double[] {1,2,3,4,5,6,7,8,9,10};

			try(final GPUScheduler		sched = exec.startTransaction();
				final TemporaryStore	ts = sched.allocateTemporaryStore(1024 * 1024)) {
				
				try(final TemporaryBuffer 	buffer = ts.getBuffer(0, 1024)) {
					buffer.write(test, 0, test.length);
					
					try{buffer.write((double[])null, 0, test.length);
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
					final double[]		temp = new double[test.length];
							
					buffer.read(temp, 0, test.length);
					Assert.assertArrayEquals(test, temp, 0.001f);
					
					try{buffer.read((double[])null, 0, test.length);
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
			}
		}
	}
}
