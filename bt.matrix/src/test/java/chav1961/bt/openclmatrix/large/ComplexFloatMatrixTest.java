package chav1961.bt.openclmatrix.large;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import chav1961.bt.openclmatrix.internal.GPUExecutor;
import chav1961.bt.openclmatrix.internal.InternalUtils;
import chav1961.bt.openclmatrix.spi.OpenCLDescriptor;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.AggregateDirection;
import chav1961.purelib.matrix.interfaces.Matrix.AggregateType;
import chav1961.purelib.matrix.interfaces.Matrix.ApplyFloat2;
import chav1961.purelib.matrix.interfaces.Matrix.Piece;
import chav1961.purelib.matrix.interfaces.Matrix.Type;
import chav1961.purelib.streams.DataInputAdapter;
import chav1961.purelib.streams.DataOutputAdapter;

public class ComplexFloatMatrixTest {
	private static final File	DIR = new File("d:/temp");
	
	private OpenCLDescriptor	desc;
	private GPUExecutor			exec;
	
	@Before
	public void prepare() throws ContentException {
		desc = new OpenCLDescriptor();
		exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION);
	}

	@After
	public void unprepare() {
		exec.close();
		desc.close();
	}
	
	@Test
	public void basicTest() throws RuntimeException, CloneNotSupportedException, IOException {
		final float[]	sum = new float[1];
		
		try(final ComplexFloatMatrix	m = new ComplexFloatMatrix(exec, 2, 3)) {
			Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
			Assert.assertEquals(2, m.numberOfRows());
			Assert.assertEquals(3, m.numberOfColumns()); 
			
			try{new ComplexFloatMatrix(null, 1, 2).close();
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{new ComplexFloatMatrix(exec, 0, 2).close();
				Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ComplexFloatMatrix(exec, 3, 0).close();
				Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
	
			// assign ints 
			
			m.assign(1,0,2,0,3,0,4,0,5,0,6,0);
			Assert.assertArrayEquals(new float[] {1,0,2,0,3,0,4,0,5,0,6,0}, m.extractFloats(), 0.001f);

			sum[0] = 0;
			m.extractInts(new DataOutputAdapter() {
				@Override
				public void writeInt(int v) throws IOException {
					sum[0] += v;
				}
			});
			Assert.assertEquals(21, sum[0], 0.001);

			try {m.extractInts(null, new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.extractInts(Piece.of(0, 0, 100, 100), new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (1-st argument overlap)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.extractInts(Piece.of(1, 1, 1, 1), (DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.add().extractInts(Piece.of(1, 1, 1, 1), new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (call inside transaction)");
			} catch (IllegalStateException exc) {
				m.done();
			}
			
			try {m.assign(null, 0);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 100, 100), 0);
				Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(1, 1, 1, 1), (int[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.add().assign(Piece.of(1, 1, 1, 1), 0);
				Assert.fail("Mandatory exception was not detected (call inside transaction)");
			} catch (IllegalStateException exc) {
				m.done();
			}
	
			m.assign(Piece.of(1,1,1,2), 10, 20, 30, 40);
			Assert.assertArrayEquals(new float[] {1,0,2,0,3,0,4,0,10,20,30,40}, m.extractFloats(), 0.001f);
			try {m.assign(null, 10, 20);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(1,1,1,2), (int[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			m.assign(Piece.of(1, 1, 1, 2), new DataInputAdapter() {
									@Override 
									public int readInt() throws IOException {
										return 100;
									}
								}, Type.REAL_INT);
			Assert.assertArrayEquals(new float[] {1,0,2,0,3,0,4,0,100,100,100,100}, m.extractFloats(), 0.001f);

			try {
				m.assign(null, new DataInputAdapter() {
						@Override 
						public int readInt() throws IOException {
							return 100;
						}
					}, Type.REAL_INT);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m.assign(Piece.of(0, 0, 100, 100), new DataInputAdapter() {
						@Override 
						public int readInt() throws IOException {
							return 100;
						}
					}, Type.REAL_INT);
				Assert.fail("Mandatory exception was not detected (1-st argument overlap)");
			} catch (IllegalArgumentException exc) {
			}
			try {
				m.assign(Piece.of(1, 1, 1, 2), null, Type.REAL_INT);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m.assign(Piece.of(1, 1, 1, 2), new DataInputAdapter() {
						@Override 
						public int readInt() throws IOException {
							return 100;
						}
					}, null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m.add().assign(Piece.of(1, 1, 1, 2), new DataInputAdapter() {
						@Override 
						public int readInt() throws IOException {
							return 100;
						}
					}, Type.REAL_INT);
				Assert.fail("Mandatory exception was not detected (call inside transaction)");
			} catch (IllegalStateException exc) {
				m.done();
			}
			
			// assign longs
			
			m.assign(6L,0L,5L,0L,4L,0L,3L,0L,2L,0L,1L,0L);
			Assert.assertArrayEquals(new float[] {6,0,5,0,4,0,3,0,2,0,1,0}, m.extractFloats(), 0.001f);
			
			sum[0] = 0;
			m.extractLongs(new DataOutputAdapter() {
				@Override
				public void writeLong(long v) throws IOException {
					sum[0] += v;
				}
			});
			Assert.assertEquals(21, sum[0], 0.001);

			try {m.extractLongs(null, new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.extractLongs(Piece.of(0, 0, 100, 100), new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (1-st argument overlap)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.extractLongs(Piece.of(1, 1, 1, 1), (DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.add().extractLongs(Piece.of(1, 1, 1, 1), new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (call inside transaction)");
			} catch (IllegalStateException exc) {
				m.done();
			}
			
			try {m.assign(null, 0L);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 100, 100), 0L);
				Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(1, 1, 1, 1), (long[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.add().assign(Piece.of(1, 1, 1, 1), 0L);
				Assert.fail("Mandatory exception was not detected (call inside transaction)");
			} catch (IllegalStateException exc) {
				m.done();
			}
	
			m.assign(Piece.of(1,1,1,2), 10L, 20L, 30L, 40L);
			Assert.assertArrayEquals(new float[] {6,0,5,0,4,0,3,0,10,20,30,40}, m.extractFloats(), 0.001f);
			try {m.assign(null, 10L, 20L);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(1,1,1,2), (long[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}

			m.assign(Piece.of(1, 1, 1, 2), new DataInputAdapter() {
							@Override 
							public long readLong() throws IOException {
								return 200;
							}
						}, Type.REAL_LONG);
			Assert.assertArrayEquals(new float[] {6,0,5,0,4,0,3,0,200,200,200,200}, m.extractFloats(), 0.001f);
			
			// assign floats
			
			m.assign(1f,0f,3f,0f,5f,0f,7f,0f,9f,0f,11f,0f);
			Assert.assertArrayEquals(new float[] {1,0,3,0,5,0,7,0,9,0,11,0}, m.extractFloats(), 0.001f);
			
			sum[0] = 0;
			m.extractFloats(new DataOutputAdapter() {
				@Override
				public void writeFloat(float v) throws IOException {
					sum[0] += v;
				}
			}); 
			Assert.assertEquals(36, sum[0], 0.001);

			try {m.extractFloats(null, new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.extractFloats(Piece.of(0, 0, 100, 100), new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (1-st argument overlap)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.extractFloats(Piece.of(1, 1, 1, 1), (DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.add().extractFloats(Piece.of(1, 1, 1, 1), new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (call inside transaction)");
			} catch (IllegalStateException exc) {
				m.done();
			}
			
			try {m.assign(null, 0f);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 100, 100), 0f);
				Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(1, 1, 1, 1), (float[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.add().assign(Piece.of(1, 1, 1, 1), 0f);
				Assert.fail("Mandatory exception was not detected (call inside transaction)");
			} catch (IllegalStateException exc) {
				m.done();
			}
	
			m.assign(Piece.of(1,1,1,2), 10f, 20f, 30f, 40f);
			Assert.assertArrayEquals(new float[] {1,0,3,0,5,0,7,0,10,20,30,40}, m.extractFloats(), 0.001f);
			try {m.assign(null, 10f, 20f);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(1,1,1,2), (float[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}

			m.assign(Piece.of(1, 1, 1, 2), new DataInputAdapter() {
							@Override 
							public float readFloat() throws IOException {
								return 300;
							}
						}, Type.REAL_FLOAT);
			Assert.assertArrayEquals(new float[] {1,0,3,0,5,0,7,0,300,300,300,300}, m.extractFloats(), 0.001f);
			
			// assign doubles
			
			m.assign(11d,0d,9d,0d,7d,0d,5d,0d,3d,0d,1d,0d);
			Assert.assertArrayEquals(new float[] {11,0,9,0,7,0,5,0,3,0,1,0}, m.extractFloats(), 0.001f);
			
			sum[0] = 0;
			m.extractDoubles(new DataOutputAdapter() {
				@Override
				public void writeDouble(double v) throws IOException {
					sum[0] += v;
				}
			});
			Assert.assertEquals(36, sum[0], 0.001); 

			try {m.extractDoubles(null, new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.extractDoubles(Piece.of(0, 0, 100, 100), new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (1-st argument overlap)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.extractDoubles(Piece.of(1, 1, 1, 1), (DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.add().extractDoubles(Piece.of(1, 1, 1, 1), new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (call inside transaction)");
			} catch (IllegalStateException exc) {
				m.done();
			}
			
			try {m.assign(null, 0d);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 100, 100), 0d);
				Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(1, 1, 1, 1), (double[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.add().assign(Piece.of(1, 1, 1, 1), 0d);
				Assert.fail("Mandatory exception was not detected (call inside transaction)");
			} catch (IllegalStateException exc) {
				m.done();
			}
	
			m.assign(Piece.of(1,1,1,2), 10d, 20d, 30d, 40d);
			Assert.assertArrayEquals(new float[] {11,0,9,0,7,0,5,0,10,20,30,40}, m.extractFloats(), 0.001f);
			try {m.assign(null, 10d, 20d);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(1,1,1,2), (double[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}

			m.assign(Piece.of(1, 1, 1, 2), new DataInputAdapter() {
							@Override 
							public double readDouble() throws IOException {
								return 400;
							}
						}, Type.REAL_DOUBLE);
			Assert.assertArrayEquals(new float[] {11,0,9,0,7,0,5,0,400,400,400,400}, m.extractFloats(), 0.001f);
			
			// extract and convert 
			
			m.assign(1,0,2,0,3,0,4,0,5,0,6,0);
			Assert.assertArrayEquals(new int[] {1,0,2,0,3,0,4,0,5,0,6,0}, m.extractInts());
			Assert.assertArrayEquals(new int[] {5,0,6,0}, m.extractInts(Piece.of(1,1,1,2)));
			try {m.extractInts((Piece)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.extractInts(Piece.of(0, 0, 100, 100));
				Assert.fail("Mandatory exception was not detected (1-st argument overlap)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.add().extractInts(Piece.of(1, 1, 1, 2));
				Assert.fail("Mandatory exception was not detected (call inside transaction)");
			} catch (IllegalStateException exc) {
				m.done();
			}
			
			Assert.assertArrayEquals(new long[] {1,0,2,0,3,0,4,0,5,0,6,0}, m.extractLongs());
			Assert.assertArrayEquals(new long[] {5,0,6,0}, m.extractLongs(Piece.of(1,1,1,2)));
			try {m.extractLongs((Piece)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.extractLongs(Piece.of(0, 0, 100, 100));
				Assert.fail("Mandatory exception was not detected (1-st argument overlap)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.add().extractLongs(Piece.of(1, 1, 1, 2));
				Assert.fail("Mandatory exception was not detected (call inside transaction)");
			} catch (IllegalStateException exc) {
				m.done();
			}
	
			Assert.assertArrayEquals(new float[] {1,0,2,0,3,0,4,0,5,0,6,0}, m.extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {5,0,6,0}, m.extractFloats(Piece.of(1,1,1,2)), 0.001f);
			try {m.extractFloats((Piece)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.extractFloats(Piece.of(0, 0, 100, 100));
				Assert.fail("Mandatory exception was not detected (1-st argument overlap)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.add().extractFloats(Piece.of(1, 1, 1, 2));
				Assert.fail("Mandatory exception was not detected (call inside transaction)");
			} catch (IllegalStateException exc) {
				m.done();
			}
	
			Assert.assertArrayEquals(new double[] {1,0,2,0,3,0,4,0,5,0,6,0}, m.extractDoubles(), 0.001);
			Assert.assertArrayEquals(new double[] {5,0,6,0}, m.extractDoubles(Piece.of(1,1,1,2)), 0.001d);
			try {m.extractDoubles((Piece)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.extractDoubles(Piece.of(0, 0, 100, 100));
				Assert.fail("Mandatory exception was not detected (1-st argument overlap)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.add().extractDoubles(Piece.of(1, 1, 1, 2));
				Assert.fail("Mandatory exception was not detected (call inside transaction)");
			} catch (IllegalStateException exc) {
				m.done();
			}
	
			// deep equals
			
			final ComplexFloatMatrix	m2 = new ComplexFloatMatrix(exec, 2, 3);
			final ComplexFloatMatrix	m3 = new ComplexFloatMatrix(exec, 3, 2);
			
			m2.assign(1,0,2,0,3,0,4,0,5,0,6,0);
			Assert.assertTrue(m2.deepEquals(m));
			m2.assign(6,0,5,0,4,0,3,0,2,0,1,0);
			Assert.assertFalse(m2.deepEquals(m));
			m3.assign(1,0,2,0,3,0,4,0,5,0,6,0);
			Assert.assertFalse(m3.deepEquals(m));
			Assert.assertTrue(m.deepEquals(m));
			Assert.assertFalse(m.deepEquals(null));
			
			try {
				m.add().deepEquals(m2);
				Assert.fail("Mandatory exception was not detected (calling method inside transaction)");
			} catch (IllegalStateException exc) {
				m.done();
			}

			// Test clone()
			
			try(final ComplexFloatMatrix	dcm = (ComplexFloatMatrix)m.clone()) {
				Assert.assertTrue(m.deepEquals(dcm));
			}
			
			// assign matrix
			
			m.assign(m2);
			Assert.assertArrayEquals(new float[] {6,0,5,0,4,0,3,0,2,0,1,0}, m.extractFloats(), 0.001f);
			
			try {m.assign((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			final ComplexFloatMatrix	m4 = new ComplexFloatMatrix(exec, 2, 1);
			
			m4.assign(10,20,30,40);
			m.assign(Piece.of(1, 1, 1, 2), m4);
			Assert.assertArrayEquals(new float[] {6,0,5,0,4,0,3,0,10,20,30,40}, m.extractFloats(), 0.001f);
			try {m.assign(null, m4);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(1, 1, 1, 3), m4);
				Assert.fail("Mandatory exception was not detected (1-st argument outside matrix)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(1, 1, 2, 2), m4);
				Assert.fail("Mandatory exception was not detected (1-st argument outside matrix)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(3, 1, 1, 2), m4);
				Assert.fail("Mandatory exception was not detected (1-st argument outside matrix)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(1, 3, 1, 2), m4);
				Assert.fail("Mandatory exception was not detected (1-st argument outside matrix)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(1, 1, 1, 2), (Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			// fill content
			
			m.assign(1,0,2,0,3,0,4,0,5,0,6,0);
			m.fill(10);
			Assert.assertArrayEquals(new float[] {10,0,10,0,10,0,10,0,10,0,10,0}, m.extractFloats(), 0.001f);
			m.fill(20L);
			Assert.assertArrayEquals(new float[] {20,0,20,0,20,0,20,0,20,0,20,0}, m.extractFloats(), 0.001f);
			m.fill(30f);
			Assert.assertArrayEquals(new float[] {30,0,30,0,30,0,30,0,30,0,30,0}, m.extractFloats(), 0.001f);
			m.fill(40d);
			Assert.assertArrayEquals(new float[] {40,0,40,0,40,0,40,0,40,0,40,0}, m.extractFloats(), 0.001f);
			m.fill(50f, 60f);
			Assert.assertArrayEquals(new float[] {50,60,50,60,50,60,50,60,50,60,50,60}, m.extractFloats(), 0.001f);
			m.fill(70d, 80d);
			Assert.assertArrayEquals(new float[] {70,80,70,80,70,80,70,80,70,80,70,80}, m.extractFloats(), 0.001f);
	
			m.assign(1,0,2,0,3,0,4,0,5,0,6,0);
			m.fill(Piece.of(1,1,1,2), 10, 20);
			Assert.assertArrayEquals(new float[] {1,0,2,0,3,0,4,0,10,20,10,20}, m.extractFloats(), 0.001f);
			try {m.fill(null, 10);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			m.fill(Piece.of(1,1,1,2), 20L, 30L);
			Assert.assertArrayEquals(new float[] {1,0,2,0,3,0,4,0,20,30,20,30}, m.extractFloats(), 0.001f);
			try {m.fill(null, 20L);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			m.fill(Piece.of(1,1,1,2), 30f, 40f);
			Assert.assertArrayEquals(new float[] {1,0,2,0,3,0,4,0,30,40,30,40}, m.extractFloats(), 0.001f);
			try {m.fill(null, 30f);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			m.fill(Piece.of(1,1,1,2), 40d, 50d);
			Assert.assertArrayEquals(new float[] {1,0,2,0,3,0,4,0,40,50,40,50}, m.extractFloats(), 0.001f);
			try {m.fill(null, 40d);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			// Apply test
			
			m.apply2((int x, int y, float[] val)->{val[0] = 0; val[1] = 0;});
			Assert.assertArrayEquals(new float[] {0,0,0,0,0,0,0,0,0,0,0,0}, m.extractFloats(), 0.001f);
			try {m.apply2((ApplyFloat2)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
		}
	}

//	@Test
	public void addTest() {
		try(final ComplexFloatMatrix	m1 = new ComplexFloatMatrix(exec, DIR, 16000, 16000);
			final ComplexFloatMatrix	m2 = new ComplexFloatMatrix(exec, DIR, 16000, 16000)) {
			
			m1.assign(1,0,2,0,3,0,4,0,5,0,6,0);

			// add ints
			
			Assert.assertArrayEquals(new float[] {2,1,4,1,6,1,8,1,10,1,12,1}, m1.add(1,1,2,1,3,1,4,1,5,1,6,1).done().extractFloats(Piece.of(0, 0, 1, 6)), 0.001f);
			try {
				m1.add((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.add(1,2,3,4,5,6).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {11,0,12,0,13,0,14,0,15,0,16,0}, m1.addValue(10).done().extractFloats(Piece.of(0, 0, 1, 6)), 0.001f);
			try {
				m1.addValue(10).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// add longs
			
			Assert.assertArrayEquals(new float[] {2,1,4,1,6,1,8,1,10,1,12,1}, m1.add(1L,1L,2L,1L,3L,1L,4L,1L,5L,1L,6L,1L).done().extractFloats(Piece.of(0, 0, 1, 6)), 0.001f);
			try {
				m1.add((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.add(1L,2L,3L,4L,5L,6L).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {11,0,12,0,13,0,14,0,15,0,16,0}, m1.addValue(10L).done().extractFloats(Piece.of(0, 0, 1, 6)), 0.001f);
			try {
				m1.addValue(10L).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// add floats
			
			Assert.assertArrayEquals(new float[] {2,1,4,1,6,1,8,1,10,1,12,1}, m1.add(1f,1f,2f,1f,3f,1f,4f,1f,5f,1f,6f,1f).done().extractFloats(Piece.of(0, 0, 1, 6)), 0.001f);
			try {
				m1.add((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.add(1f,2f,3f,4f,5f,6f).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {11,0,12,0,13,0,14,0,15,0,16,0}, m1.addValue(10f).done().extractFloats(Piece.of(0, 0, 1, 6)), 0.001f);
			Assert.assertArrayEquals(new float[] {11,20,12,20,13,20,14,20,15,20,16,20}, m1.addValue(10f, 20f).done().extractFloats(Piece.of(0, 0, 1, 6)), 0.001f);
			try {
				m1.addValue(10f).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// add doubles
			
			Assert.assertArrayEquals(new float[] {2,1,4,1,6,1,8,1,10,1,12,1}, m1.add(1d,1d,2d,1d,3d,1d,4d,1d,5d,1d,6d,1d).done().extractFloats(Piece.of(0, 0, 1, 6)), 0.001f);
			try {
				m1.add((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.add(1d,2d,3d,4d,5d,6d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {11,0,12,0,13,0,14,0,15,0,16,0}, m1.addValue(10d).done().extractFloats(Piece.of(0, 0, 1, 6)), 0.001f);
			Assert.assertArrayEquals(new float[] {11,20,12,20,13,20,14,20,15,20,16,20}, m1.addValue(10d, 20d).done().extractFloats(Piece.of(0, 0, 1, 6)), 0.001f);
			try {
				m1.addValue(10d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// add matrix

			m2.assign(1,1,2,1,3,1,4,1,5,1,6,1);
			Assert.assertArrayEquals(new float[] {2,1,4,1,6,1,8,1,10,1,12,1}, m1.add(m2).done().extractFloats(Piece.of(0, 0, 1, 6)), 0.001f);
//			Assert.assertArrayEquals(new float[] {2,1,4,1,6,1,8,1,10,1,12,1}, m1.add(m2.cast(Type.COMPLEX_DOUBLE)).done().extractFloats(Piece.of(0, 0, 1, 6)), 0.001f);

			try {
				m1.add((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.add(m2).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}

	@Test
	public void subtractTest() {
		try(final ComplexFloatMatrix	m1 = new ComplexFloatMatrix(exec, 2, 3);
			final ComplexFloatMatrix	m2 = new ComplexFloatMatrix(exec, 2, 3)) {
			
			m1.assign(1,0,2,0,3,0,4,0,5,0,6,0);

			// subtract ints
			
			Assert.assertArrayEquals(new float[] {0,-1,0,-1,0,-1,0,-1,0,-1,6,-1}, m1.subtract(1,1,2,1,3,1,4,1,5,1,0,1).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtract((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtract(1,2,3,4,5,0).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {-9,0,-8,0,-7,0,-6,0,-5,0,-4,0}, m1.subtractValue(10).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtractValue(10).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// subtract longs
			
			Assert.assertArrayEquals(new float[] {0,-1,0,-1,0,-1,0,-1,0,-1,6,-1}, m1.subtract(1L,1L,2L,1L,3L,1L,4L,1L,5L,1L,0L,1L).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtract((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtract(1L,2L,3L,4L,5L,0L).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {-9,0,-8,0,-7,0,-6,0,-5,0,-4,0}, m1.subtractValue(10L).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtractValue(10L).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// subtract floats
			
			Assert.assertArrayEquals(new float[] {0,-1,0,-1,0,-1,0,-1,0,-1,6,-1}, m1.subtract(1f,1f,2f,1f,3f,1f,4f,1f,5f,1f,0f,1f).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtract((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtract(1f,2f,3f,4f,5f,6f).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {-9,0,-8,0,-7,0,-6,0,-5,0,-4,0}, m1.subtractValue(10f).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			Assert.assertArrayEquals(new float[] {-9,-10,-8,-10,-7,-10,-6,-10,-5,-10,-4,-10}, m1.subtractValue(10f, 10f).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtractValue(10f).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// subtract doubles
			
			Assert.assertArrayEquals(new float[] {0,-1,0,-1,0,-1,0,-1,0,-1,6,-1}, m1.subtract(1d,1d,2d,1d,3d,1d,4d,1d,5d,1d,0d,1d).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtract((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtract(1d,2d,3d,4d,5d,6d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {-9,-10,-8,-10,-7,-10,-6,-10,-5,-10,-4,-10}, m1.subtractValue(10d, 10d).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtractValue(10d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// subtract matrix

			m2.assign(1,1,2,1,3,1,4,1,5,1,0,1);
			Assert.assertArrayEquals(new float[] {0,-1,0,-1,0,-1,0,-1,0,-1,6,-1}, m1.subtract(m2).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
//			Assert.assertArrayEquals(new float[] {0,-1,0,-1,0,-1,0,-1,0,-1,6,-1}, m1.subtract(m2.cast(Type.COMPLEX_DOUBLE)).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);

			try {
				m1.subtract((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtract(m2).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}

	@Test
	public void subtractFromTest() {
		try(final ComplexFloatMatrix	m1 = new ComplexFloatMatrix(exec, 2, 3);
			final ComplexFloatMatrix	m2 = new ComplexFloatMatrix(exec, 2, 3)) {
			
			m1.assign(1,0,2,0,3,0,4,0,5,0,6,0);

			// subtract ints
			
			Assert.assertArrayEquals(new float[] {9,1,8,1,7,1,6,1,5,1,4,1}, m1.subtractFrom(10,1,10,1,10,1,10,1,10,1,10,1).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtractFrom((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtractFrom(1,2,3,4,5,0).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {9,5,8,5,7,5,6,5,5,5,4,5}, m1.subtractFromValue(10, 5).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtractFromValue(10).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// subtract longs
			
			Assert.assertArrayEquals(new float[] {9,1,8,1,7,1,6,1,5,1,4,1}, m1.subtractFrom(10L,1L,10L,1L,10L,1L,10L,1L,10L,1L,10L,1L).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtractFrom((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtractFrom(1L,2L,3L,4L,5L,0L).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {9,0,8,0,7,0,6,0,5,0,4,0}, m1.subtractFromValue(10L).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtractFromValue(10L).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// subtract floats
			
			Assert.assertArrayEquals(new float[] {9,1,8,1,7,1,6,1,5,1,4,1}, m1.subtractFrom(10f,1f,10f,1f,10f,1f,10f,1f,10f,1f,10f,1f).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtractFrom((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtractFrom(1f,2f,3f,4f,5f,6f).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {9,0,8,0,7,0,6,0,5,0,4,0}, m1.subtractFromValue(10f).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			Assert.assertArrayEquals(new float[] {9,5,8,5,7,5,6,5,5,5,4,5}, m1.subtractFromValue(10f, 5f).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtractFromValue(10f).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// subtract doubles
			
			Assert.assertArrayEquals(new float[] {9,1,8,1,7,1,6,1,5,1,4,1}, m1.subtractFrom(10d,1d,10d,1d,10d,1d,10d,1d,10d,1d,10d,1d).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtractFrom((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtractFrom(1d,2d,3d,4d,5d,6d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {9,0,8,0,7,0,6,0,5,0,4,0}, m1.subtractFromValue(10d).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			Assert.assertArrayEquals(new float[] {9,5,8,5,7,5,6,5,5,5,4,5}, m1.subtractFromValue(10d, 5d).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.subtractFromValue(10d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// subtract matrix

			m2.assign(10,1,10,1,10,1,10,1,10,1,10,1);
			Assert.assertArrayEquals(new float[] {9,1,8,1,7,1,6,1,5,1,4,1}, m1.subtractFrom(m2).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
//			Assert.assertArrayEquals(new float[] {9,1,8,1,7,1,6,1,5,1,4,1}, m1.subtractFrom(m2.cast(Type.COMPLEX_DOUBLE)).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			
			try {
				m1.subtractFrom((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtractFrom(m2).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}

	@Test
	public void mulValueTest() {
		try(final ComplexFloatMatrix	m = new ComplexFloatMatrix(exec, 2, 3)) {
			m.assign(1,1,2,2,3,3,4,4,5,5,6,6);
			
			// int muls
			
			Assert.assertArrayEquals(new float[] {10,10,20,20,30,30,40,40,50,50,60,60}, m.mulValue(10).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m.mulValue(10).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {0.5f,0.5f,1f,1f,1.5f,1.5f,2f,2f,2.5f,2.5f,3f,3f}, m.divValue(2).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m.divValue(10).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {1f,-1f,0.5f,-0.5f,0.333333f,-0.333333f,0.25f,-0.25f,0.2f,-0.2f,0.166666f,-0.166666f}, m.divFromValue(2).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m.divFromValue(10).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// long muls
			
			Assert.assertArrayEquals(new float[] {10,10,20,20,30,30,40,40,50,50,60,60}, m.mulValue(10L).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m.mulValue(10L).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {0.5f,0.5f,1f,1f,1.5f,1.5f,2f,2f,2.5f,2.5f,3f,3f}, m.divValue(2L).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m.divValue(10L).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {1f,-1f,0.5f,-0.5f,0.333333f,-0.333333f,0.25f,-0.25f,0.2f,-0.2f,0.166666f,-0.166666f}, m.divFromValue(2L).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m.divFromValue(10L).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// float muls
			
			Assert.assertArrayEquals(new float[] {10,10,20,20,30,30,40,40,50,50,60,60}, m.mulValue(10f).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			Assert.assertArrayEquals(new float[] {0,10,0,20,0,30,0,40,0,50,0,60}, m.mulValue(5f, 5f).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m.mulValue(10f).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {0.5f,0.5f,1f,1f,1.5f,1.5f,2f,2f,2.5f,2.5f,3f,3f}, m.divValue(2f).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			Assert.assertArrayEquals(new float[] {0.5f,0f,1f,0f,1.5f,0,2f,0f,2.5f,0f,3f,0f}, m.divValue(2f, 2f).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m.divValue(10f).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {1f,-1f,0.5f,-0.5f,0.333333f,-0.333333f,0.25f,-0.25f,0.2f,-0.2f,0.166666f,-0.166666f}, m.divFromValue(2f).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			Assert.assertArrayEquals(new float[] {2f,0f,1f,0,0.666666f,0,0.5f,0f,0.4f,0f,0.333333f,0f}, m.divFromValue(2d, 2d).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m.divFromValue(10f).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// double muls			
			
			Assert.assertArrayEquals(new float[] {10,10,20,20,30,30,40,40,50,50,60,60}, m.mulValue(10d).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			Assert.assertArrayEquals(new float[] {0,10,0,20,0,30,0,40,0,50,0,60}, m.mulValue(5d, 5d).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m.mulValue(10d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {0.5f,0.5f,1f,1f,1.5f,1.5f,2f,2f,2.5f,2.5f,3f,3f}, m.divValue(2d).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			Assert.assertArrayEquals(new float[] {0.5f,0f,1f,0f,1.5f,0,2f,0f,2.5f,0f,3f,0f}, m.divValue(2d, 2d).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m.divValue(10d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {1f,-1f,0.5f,-0.5f,0.333333f,-0.333333f,0.25f,-0.25f,0.2f,-0.2f,0.166666f,-0.166666f}, m.divFromValue(2d).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			Assert.assertArrayEquals(new float[] {2f,0f,1f,0,0.666666f,0,0.5f,0f,0.4f,0f,0.333333f,0f}, m.divFromValue(2d, 2d).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m.divFromValue(10d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}

	@Test
	public void mulHadamardTest() {
		try(final ComplexFloatMatrix	m1 = new ComplexFloatMatrix(exec, 2, 3);
			final ComplexFloatMatrix	m2 = new ComplexFloatMatrix(exec, 2, 3)) {
			m1.assign(1,0,2,0,3,0,4,0,5,0,6,0);
			
			// int muls
			
			Assert.assertArrayEquals(new float[] {1,0,4,0,9,0,16,0,25,0,36,0}, m1.mulHadamard(1,0,2,0,3,0,4,0,5,0,6,0).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.mulHadamard((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulHadamard(1,2,3,4,5,6).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {1,0,1,0,1,0,1,0,1,0,1,0}, m1.mulInvHadamard(1,0,2,0,3,0,4,0,5,0,6,0).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.mulInvHadamard((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvHadamard(1,2,3,4,5,6).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {10,0,5,0,3.333333f,0,2.5f,0,2,0,1.666666f,0}, m1.mulInvFromHadamard(10,0,10,0,10,0,10,0,10,0,10,0).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.mulInvFromHadamard((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvFromHadamard(1,2,3,4,5,6).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// long muls
			
			Assert.assertArrayEquals(new float[] {1,0,4,0,9,0,16,0,25,0,36,0}, m1.mulHadamard(1L,0L,2L,0L,3L,0L,4L,0L,5L,0L,6L,0L).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.mulHadamard((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulHadamard(1L,2L,3L,4L,5L,6L).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {1,0L,1,0L,1,0L,1,0L,1,0L,1,0L}, m1.mulInvHadamard(1L,0L,2L,0L,3L,0L,4L,0L,5L,0L,6L,0L).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.mulInvHadamard((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvHadamard(1L,2L,3L,4L,5L,6L).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {10,0,5,0,3.333333f,0,2.5f,0,2,0,1.666666f,0}, m1.mulInvFromHadamard(10L,0L,10L,0L,10L,0L,10L,0L,10L,0L,10L,0L).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.mulInvFromHadamard((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvFromHadamard(1L,2L,3L,4L,5L,6L).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// float muls
			
			Assert.assertArrayEquals(new float[] {1,0,4,0,9,0,16,0,25,0,36,0}, m1.mulHadamard(1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.mulHadamard((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulHadamard(1f,2f,3f,4f,5f,6f).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {1,0,1,0,1,0,1,0,1,0,1,0}, m1.mulInvHadamard(1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.mulInvHadamard((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvHadamard(1f,2f,3f,4f,5f,6f).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {10,0,5,0,3.333333f,0,2.5f,0,2,0,1.666666f,0}, m1.mulInvFromHadamard(10f,0f,10f,0f,10f,0f,10f,0f,10f,0f,10f,0f).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.mulInvFromHadamard((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvFromHadamard(1f,2f,3f,4f,5f,6f).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// double muls
			
			Assert.assertArrayEquals(new float[] {1,0,4,0,9,0,16,0,25,0,36,0}, m1.mulHadamard(1d,0d,2d,0d,3d,0d,4d,0d,5d,0d,6d,0d).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.mulHadamard((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulHadamard(1d,2d,3d,4d,5d,6d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {1,0,1,0,1,0,1,0,1,0,1,0}, m1.mulInvHadamard(1d,0d,2d,0d,3d,0d,4d,0d,5d,0d,6d,0d).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.mulInvHadamard((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvHadamard(1d,2d,3d,4d,5d,6d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {10,0,5,0,3.333333f,0,2.5f,0,2,0,1.666666f,0}, m1.mulInvFromHadamard(10d,0d,10d,0d,10d,0d,10d,0d,10d,0d,10d,0d).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			try {
				m1.mulInvFromHadamard((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvFromHadamard(1d,2d,3d,4d,5d,6d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// matrix muls
			
			m2.assign(1,0,2,0,3,0,4,0,5,0,6,0);
			Assert.assertArrayEquals(new float[] {1,0,4,0,9,0,16,0,25,0,36,0}, m1.mulHadamard(m2).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
//			Assert.assertArrayEquals(new float[] {1,0,4,0,9,0,16,0,25,0,36,0}, m1.mulHadamard(m2.cast(Type.COMPLEX_DOUBLE)).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
			
			try {
				m1.mulHadamard((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulHadamard(m2).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {1,0,1,0,1,0,1,0,1,0,1,0}, m1.mulInvHadamard(m2).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
//			Assert.assertArrayEquals(new float[] {1,0,1,0,1,0,1,0,1,0,1,0}, m1.mulInvHadamard(m2.cast(Type.COMPLEX_DOUBLE)).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);

			try {
				m1.mulInvHadamard((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvHadamard(m2).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			m2.assign(10,0,10,0,10,0,10,0,10,0,10,0);
			Assert.assertArrayEquals(new float[] {10,0,5,0,3.333333f,0,2.5f,0,2,0,1.666666f,0}, m1.mulInvFromHadamard(m2).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
//			Assert.assertArrayEquals(new float[] {10,0,5,0,3.333333f,0,2.5f,0,2,0,1.666666f,0}, m1.mulInvFromHadamard(m2.cast(Type.COMPLEX_DOUBLE)).done().extractFloats(Piece.of(0, 0, 2, 3)), 0.001f);
	 		
			try {
				m1.mulInvFromHadamard((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvFromHadamard(m2).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}
	
	@Test
	public void ordinalMulTest() {
		try(final ComplexFloatMatrix	m1 = new ComplexFloatMatrix(exec, 2, 3);
			final ComplexFloatMatrix	m2 = new ComplexFloatMatrix(exec, 3, 2);
			final ComplexFloatMatrix	mX = new ComplexFloatMatrix(exec, 4, 4);) {
			Matrix	m3;
			
			m1.assign(1,0,2,0,3,0,4,0,5,0,6,0);
			m2.assign(10,0,20,0,30,0,40,0,50,0,60,0);
			m3 = m1.mul(m2).done();
			
			Assert.assertEquals(2, m3.numberOfRows());
			Assert.assertEquals(2, m3.numberOfColumns());
			Assert.assertArrayEquals(new float[] {220,0,280,0,490,0,640,0}, m3.extractFloats(), 0.001f);

			m3 = m1.mul(m2.cast(Type.COMPLEX_DOUBLE)).done();
			Assert.assertArrayEquals(new float[] {220,0,280,0,490,0,640,0}, m3.extractFloats(), 0.001f);
			
	 		try {
				m1.mul((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mul(m2).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			try {
				m1.mul(mX);
				Assert.fail("Mandatory exception was not detected (incompatible dimensions)");
			} catch (IllegalArgumentException exc) {
			}
			
			m3 = m1.mulFrom(m2).done();
			
			Assert.assertEquals(3, m3.numberOfRows());
			Assert.assertEquals(3, m3.numberOfColumns());
			Assert.assertArrayEquals(new float[] {90,0,120,0,150,0,190,0,260,0,330,0,290,0,400,0,510,0}, m3.extractFloats(), 0.001f);

			m3 = m1.mulFrom(m2.cast(Type.COMPLEX_DOUBLE)).done();
			Assert.assertArrayEquals(new float[] {90,0,120,0,150,0,190,0,260,0,330,0,290,0,400,0,510,0}, m3.extractFloats(), 0.001f);
			
	 		try {
				m1.mulFrom((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulFrom(m2).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			try {
				m1.mulFrom(mX);
				Assert.fail("Mandatory exception was not detected (incompatible dimensions)");
			} catch (IllegalArgumentException exc) {
			}
			
		}
	}
	
	@Test
	public void tensorMulTest() {
		try(final ComplexFloatMatrix	m1 = new ComplexFloatMatrix(exec, 3, 3);
			final ComplexFloatMatrix	m2 = new ComplexFloatMatrix(exec, 2, 2)) {
			Matrix	m3;
			
			m1.assign(1,0,2,0,3,0,4,0,5,0,6,0,7,0,8,0,9,0);
			m2.assign(10,0,20,0,30,0,40,0);
			
			m3 = m1.tensorMul(m2).done();
			
			Assert.assertEquals(6, m3.numberOfRows());
			Assert.assertEquals(6, m3.numberOfColumns());
			Assert.assertArrayEquals(new float[] {10,0,20,0,20,0,40,0,30,0,60,0,
												  30,0,40,0,60,0,80,0,90,0,120,0,
												  40,0,80,0,50,0,100,0,60,0,120,0,
												  120,0,160,0,150,0,200,0,180,0,240,0,
												  70,0,140,0,80,0,160,0,90,0,180,0,
												  210,0,280,0,240,0,320,0,270,0,360,0},
					                            m3.extractFloats(), 0.001f);
			m3 = m1.tensorMul(m2.cast(Type.COMPLEX_DOUBLE)).done();
			Assert.assertArrayEquals(new float[] {10,0,20,0,20,0,40,0,30,0,60,0,
												  30,0,40,0,60,0,80,0,90,0,120,0,
												  40,0,80,0,50,0,100,0,60,0,120,0,
												  120,0,160,0,150,0,200,0,180,0,240,0,
												  70,0,140,0,80,0,160,0,90,0,180,0,
												  210,0,280,0,240,0,320,0,270,0,360,0},
							                  m3.extractFloats(), 0.001f);
			
	 		try {
				m1.tensorMul((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.tensorMul(m2).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			m3 = m1.tensorMulFrom(m2).done();
			
			Assert.assertEquals(6, m3.numberOfRows());
			Assert.assertEquals(6, m3.numberOfColumns());
			Assert.assertArrayEquals(new float[] {10,0,20,0,30,0,20,0,40,0,60,0,
												  40,0,50,0,60,0,80,0,100,0,120,0,
												  70,0,80,0,90,0,140,0,160,0,180,0,
												  30,0,60,0,90,0,40,0,80,0,120,0,
												  120,0,150,0,180,0,160,0,200,0,240,0,
												  210,0,240,0,270,0,280,0,320,0,360,0}, 
					 							m3.extractFloats(), 0.001f);
			m3 = m1.tensorMulFrom(m2.cast(Type.COMPLEX_DOUBLE)).done();
			Assert.assertArrayEquals(new float[] {10,0,20,0,30,0,20,0,40,0,60,0,
												  40,0,50,0,60,0,80,0,100,0,120,0,
												  70,0,80,0,90,0,140,0,160,0,180,0,
												  30,0,60,0,90,0,40,0,80,0,120,0,
												  120,0,150,0,180,0,160,0,200,0,240,0,
												  210,0,240,0,270,0,280,0,320,0,360,0}, 
												m3.extractFloats(), 0.001f);

			try {
				m1.tensorMulFrom((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.tensorMulFrom(m2).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}

	@Test
	public void unaryTest() {
		try(final ComplexFloatMatrix	m = new ComplexFloatMatrix(exec, 3, 3)) {
				
			m.assign(1,0,2,0,3,0,4,0,5,0,6,0,7,0,8,0,10,0);
			Assert.assertArrayEquals(new float[] {1,0,4,0,7,0,2,0,5,0,8,0,3,0,6,0,10,0}, m.transpose().done().extractFloats(), 0.001f);
			try {
				m.transpose().extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {-0.666666f,0,-1.333333f,0,1,0,-0.666666f,0,3.66666f,0,-2,0,1,0,-2,0,1,0}, m.invert().done().extractFloats(), 0.001f);
			try {
				m.invert().extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			try{
				new ComplexFloatMatrix(exec, 3, 2).invert();
				Assert.fail("Mandatory exception was not detected (non-square matrix)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new Number[] {-3.0d, -0.0d}, m.det2());
			try{
				new ComplexFloatMatrix(exec, 3, 2).det2();
				Assert.fail("Mandatory exception was not detected (non-square matrix)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new Number[] {16.0d, 0.0d}, m.track2());
		}
	}

	@Test
	public void aggregateTest() {
		try(final ComplexFloatMatrix	m = new ComplexFloatMatrix(exec, 3, 3)) {
			
			m.assign(1,0,2,0,3,0,4,0,5,0,6,0,7,0,8,0,9,0);
			Assert.assertArrayEquals(new float[] {6,0, 15,0, 24,0}, m.aggregate(AggregateDirection.ByColumns, AggregateType.Sum).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {12,0, 15,0, 18,0}, m.aggregate(AggregateDirection.ByRows, AggregateType.Sum).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {45,0}, m.aggregate(AggregateDirection.Total, AggregateType.Sum).done().extractFloats(), 0.001f);

			Assert.assertArrayEquals(new float[] {1,0, 4,0, 7,0}, m.aggregate(AggregateDirection.ByColumns, AggregateType.Min).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {1,0, 2,0, 3,0}, m.aggregate(AggregateDirection.ByRows, AggregateType.Min).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {1,0}, m.aggregate(AggregateDirection.Total, AggregateType.Min).done().extractFloats(), 0.001f);

			Assert.assertArrayEquals(new float[] {3,0, 6,0, 9,0}, m.aggregate(AggregateDirection.ByColumns, AggregateType.Max).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {7,0, 8,0, 9,0}, m.aggregate(AggregateDirection.ByRows, AggregateType.Max).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {9,0}, m.aggregate(AggregateDirection.Total, AggregateType.Max).done().extractFloats(), 0.001f);

			Assert.assertArrayEquals(new float[] {2,0, 5,0, 8,0}, m.aggregate(AggregateDirection.ByColumns, AggregateType.Avg).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {4,0, 5,0, 6,0}, m.aggregate(AggregateDirection.ByRows, AggregateType.Avg).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {5,0}, m.aggregate(AggregateDirection.Total, AggregateType.Avg).done().extractFloats(), 0.001f);
			
			try{m.aggregate(null, AggregateType.Avg);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{m.aggregate(AggregateDirection.ByColumns, null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{m.aggregate(AggregateDirection.ByColumns, AggregateType.Avg).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}
}
