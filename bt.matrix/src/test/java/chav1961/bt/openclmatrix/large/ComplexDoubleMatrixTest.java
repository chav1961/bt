package chav1961.bt.openclmatrix.large;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.ApplyDouble2;
import chav1961.purelib.matrix.interfaces.Matrix.Piece;
import chav1961.purelib.streams.DataInputAdapter;
import chav1961.purelib.streams.DataOutputAdapter;

public class ComplexDoubleMatrixTest {
	public static final int			LARGE_SIZE = 16384;
	public static final File		DIR_LOCATION = new File("d:/temp");

	@Test
	public void basicTest() throws IOException, CloneNotSupportedException {
		try(final ComplexDoubleMatrix	cdm = new ComplexDoubleMatrix(DIR_LOCATION, LARGE_SIZE, LARGE_SIZE)) {
			final long[]				counter = new long[1]; 
			
			Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, cdm.getType());
			Assert.assertEquals(LARGE_SIZE, cdm.numberOfRows());
			Assert.assertEquals(LARGE_SIZE, cdm.numberOfColumns());
			
			// Filling content
			
			cdm.fill(1, 1);
			
			try {
				cdm.fill(null, 1, 1);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				cdm.fill(Piece.of(0, 0, 2*LARGE_SIZE, 2*LARGE_SIZE), 1, 1);
				Assert.fail("Mandatory exception was not detected (1-st argument overlars matrix ranges)");
			} catch (IllegalArgumentException exc) {
			}
			
			// Extract double content
			
			counter[0] = 0;
			cdm.extractDoubles(new DataOutputAdapter() {
				@Override
				public void writeDouble(double v) throws IOException {
					Assert.assertEquals(1, v, 0.001f);
					counter[0]++;
				}
			});
			Assert.assertEquals(LARGE_SIZE*LARGE_SIZE*cdm.getType().getNumberOfItems(), counter[0]);
			
			try {
				cdm.extractDoubles((DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				cdm.extractDoubles((Piece)null, new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				cdm.extractDoubles(Piece.of(0, 0, 2*LARGE_SIZE, 2*LARGE_SIZE), new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (1-st argument overlaps matrix ranges)");
			} catch (IllegalArgumentException exc) {
			}
			try {
				cdm.extractDoubles(Piece.of(0, 0, 1, 1), (DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}

			for(double item : cdm.extractDoubles()) {
				Assert.assertEquals(1, item, 0.001f);
			}
			
			try {
				cdm.extractDoubles((Piece)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				cdm.extractDoubles(Piece.of(0, 0, 2*LARGE_SIZE, 2*LARGE_SIZE));
				Assert.fail("Mandatory exception was not detected (1-st argument overlaps matrix ranges)");
			} catch (IllegalArgumentException exc) {
			}
			
			// Extract float content			
			
			counter[0] = 0;
			cdm.extractFloats(new DataOutputAdapter() {
				@Override
				public void writeFloat(float v) throws IOException {
					Assert.assertEquals(1, v, 0.001f);
					counter[0]++;
				}
			});
			Assert.assertEquals(LARGE_SIZE*LARGE_SIZE*cdm.getType().getNumberOfItems(), counter[0]);
			
			try {
				cdm.extractFloats((DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				cdm.extractFloats((Piece)null, new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				cdm.extractFloats(Piece.of(0, 0, 2*LARGE_SIZE, 2*LARGE_SIZE), new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (1-st argument overlaps matrix ranges)");
			} catch (IllegalArgumentException exc) {
			}
			try {
				cdm.extractFloats(Piece.of(0, 0, 1, 1), (DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			for(float item : cdm.extractFloats()) {
				Assert.assertEquals(1, item, 0.001f);
			}

			try {
				cdm.extractFloats((Piece)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				cdm.extractFloats(Piece.of(0, 0, 2*LARGE_SIZE, 2*LARGE_SIZE));
				Assert.fail("Mandatory exception was not detected (1-st argument overlaps matrix ranges)");
			} catch (IllegalArgumentException exc) {
			}
			
			// Extract long content			
			
			counter[0] = 0;
			cdm.extractLongs(new DataOutputAdapter() {
				@Override
				public void writeLong(long v) throws IOException {
					Assert.assertEquals(1, v);
					counter[0]++;
				}
			});
			Assert.assertEquals(LARGE_SIZE*LARGE_SIZE*cdm.getType().getNumberOfItems(), counter[0]);

			try {
				cdm.extractLongs((DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				cdm.extractLongs((Piece)null, new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				cdm.extractLongs(Piece.of(0, 0, 2*LARGE_SIZE, 2*LARGE_SIZE), new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (1-st argument overlaps matrix ranges)");
			} catch (IllegalArgumentException exc) {
			}
			try {
				cdm.extractLongs(Piece.of(0, 0, 1, 1), (DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			for(long item : cdm.extractLongs()) {
				Assert.assertEquals(1, item);
			}

			try {
				cdm.extractLongs((Piece)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				cdm.extractLongs(Piece.of(0, 0, 2*LARGE_SIZE, 2*LARGE_SIZE));
				Assert.fail("Mandatory exception was not detected (1-st argument overlaps matrix ranges)");
			} catch (IllegalArgumentException exc) {
			}
			
			// Extract long content			
			
			counter[0] = 0;
			cdm.extractInts(new DataOutputAdapter() {
				@Override
				public void writeInt(int v) throws IOException {
					Assert.assertEquals(1, v);
					counter[0]++;
				}
			});
			Assert.assertEquals(LARGE_SIZE*LARGE_SIZE*cdm.getType().getNumberOfItems(), counter[0]);

			try {
				cdm.extractInts((DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				cdm.extractInts((Piece)null, new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				cdm.extractInts(Piece.of(0, 0, 2*LARGE_SIZE, 2*LARGE_SIZE), new DataOutputAdapter());
				Assert.fail("Mandatory exception was not detected (1-st argument overlaps matrix ranges)");
			} catch (IllegalArgumentException exc) {
			}
			try {
				cdm.extractInts(Piece.of(0, 0, 1, 1), (DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			for(int item : cdm.extractInts()) {
				Assert.assertEquals(1, item);
			}

			try {
				cdm.extractInts((Piece)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				cdm.extractInts(Piece.of(0, 0, 2*LARGE_SIZE, 2*LARGE_SIZE));
				Assert.fail("Mandatory exception was not detected (1-st argument overlaps matrix ranges)");
			} catch (IllegalArgumentException exc) {
			}
			
			// clone matrix
			
			try(final ComplexDoubleMatrix	clone = (ComplexDoubleMatrix) cdm.clone()) {
				
				Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, cdm.getType());
				Assert.assertEquals(LARGE_SIZE, cdm.numberOfRows());
				Assert.assertEquals(LARGE_SIZE, cdm.numberOfColumns());
				
				Assert.assertTrue(clone.deepEquals(cdm));
				Assert.assertEquals(cdm, clone);
				
				clone.fill(2, 2);
				Assert.assertFalse(clone.deepEquals(cdm));
			}
		}
		
		try{new ComplexDoubleMatrix(0, LARGE_SIZE).close();
			Assert.fail("Mandatore exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{new ComplexDoubleMatrix(LARGE_SIZE, 0).close();
			Assert.fail("Mandatore exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{new ComplexDoubleMatrix(null, LARGE_SIZE, LARGE_SIZE).close();
			Assert.fail("Mandatore exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new ComplexDoubleMatrix(new File("./"), 0, LARGE_SIZE).close();
			Assert.fail("Mandatore exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{new ComplexDoubleMatrix(new File("./"), LARGE_SIZE, 0).close();
			Assert.fail("Mandatore exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void assignAndApplyTest() throws IOException, CloneNotSupportedException {
		try(final ComplexDoubleMatrix	cdm = new ComplexDoubleMatrix(DIR_LOCATION, LARGE_SIZE, LARGE_SIZE)) {

			// assign integers
			
			cdm.assign(new DataInputAdapter() {
				@Override
				public int readInt() throws IOException {
					return 1;
				}
			}, Matrix.Type.REAL_INT);
			
			cdm.extractDoubles(new DataOutputAdapter() {
				@Override
				public void writeDouble(double v) throws IOException {
					Assert.assertEquals(1, v, 0.001f);
				}
			});
			
			cdm.assign(1, 2, 3, 4, 5);
			
			cdm.extractDoubles(new DataOutputAdapter() {
				long index = 1;
				@Override
				public void writeDouble(double v) throws IOException {
					Assert.assertEquals(index < 5 ? index : 1, v, 0.001f);	// last '5' will be lost because of odd numbers if assignment amount 
					index++;
				}
			});
			
			try{
				cdm.assign((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{
				cdm.assign((Piece)null, 1);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{
				cdm.assign(Piece.of(0, 0, 2*LARGE_SIZE, 2*LARGE_SIZE), 1);
				Assert.fail("Mandatory exception was not detected (1-st argument overlaps matrix ranges)");
			} catch (IllegalArgumentException exc) {
			}
			try{
				cdm.assign(Piece.of(0, 0, 1, 1), (int[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd)");
			} catch (NullPointerException exc) {
			}

			// assign longs
			
			cdm.assign(new DataInputAdapter() {
				@Override
				public long readLong() throws IOException {
					return 2;
				}
			}, Matrix.Type.REAL_LONG);
			
			cdm.extractDoubles(new DataOutputAdapter() {
				@Override
				public void writeDouble(double v) throws IOException {
					Assert.assertEquals(2, v, 0.001f);
				}
			});

			cdm.assign(1L, 2L, 3L, 4L, 5L);
			
			cdm.extractDoubles(new DataOutputAdapter() {
				long index = 1;
				@Override
				public void writeDouble(double v) throws IOException {
					Assert.assertEquals(index < 5 ? index : 2, v, 0.001f);
					index++;
				}
			});
			
			try{
				cdm.assign((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{
				cdm.assign((Piece)null, 1L);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{
				cdm.assign(Piece.of(0, 0, 2*LARGE_SIZE, 2*LARGE_SIZE), 1L);
				Assert.fail("Mandatory exception was not detected (1-st argument overlaps matrix ranges)");
			} catch (IllegalArgumentException exc) {
			}
			try{
				cdm.assign(Piece.of(0, 0, 1, 1), (long[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd)");
			} catch (NullPointerException exc) {
			}

			// assign floats
			
			cdm.assign(new DataInputAdapter() {
				@Override
				public float readFloat() throws IOException {
					return 3;
				}
			}, Matrix.Type.REAL_FLOAT);
			
			cdm.extractDoubles(new DataOutputAdapter() {
				@Override
				public void writeDouble(double v) throws IOException {
					Assert.assertEquals(3, v, 0.001f);
				}
			});

			cdm.assign(1f, 2f, 3f, 4f, 5f);
			
			cdm.extractDoubles(new DataOutputAdapter() {
				long index = 1;
				@Override
				public void writeDouble(double v) throws IOException {
					Assert.assertEquals(index < 5 ? index : 3, v, 0.001f);
					index++;
				}
			});
			
			try{
				cdm.assign((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{
				cdm.assign((Piece)null, 1f);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{
				cdm.assign(Piece.of(0, 0, 2*LARGE_SIZE, 2*LARGE_SIZE), 1f);
				Assert.fail("Mandatory exception was not detected (1-st argument overlaps matrix ranges)");
			} catch (IllegalArgumentException exc) {
			}
			try{
				cdm.assign(Piece.of(0, 0, 1, 1), (float[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd)");
			} catch (NullPointerException exc) {
			}

			// assign doubles
			
			cdm.assign(new DataInputAdapter() {
				@Override
				public double readDouble() throws IOException {
					return 4;
				}
			}, Matrix.Type.REAL_DOUBLE);
			
			cdm.extractDoubles(new DataOutputAdapter() {
				@Override
				public void writeDouble(double v) throws IOException {
					Assert.assertEquals(4, v, 0.001f);
				}
			});

			cdm.assign(1d, 2d, 3d, 4d, 5d);
			
			cdm.extractDoubles(new DataOutputAdapter() {
				long index = 1;
				@Override
				public void writeDouble(double v) throws IOException {
					Assert.assertEquals(index < 5 ? index : 4, v, 0.001f);
					index++;
				}
			});
			
			try{
				cdm.assign((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{
				cdm.assign((Piece)null, 1d);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{
				cdm.assign(Piece.of(0, 0, 2*LARGE_SIZE, 2*LARGE_SIZE), 1d);
				Assert.fail("Mandatory exception was not detected (1-st argument overlaps matrix ranges)");
			} catch (IllegalArgumentException exc) {
			}
			try{
				cdm.assign(Piece.of(0, 0, 1, 1), (double[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd)");
			} catch (NullPointerException exc) {
			}

			// Apply double2
			cdm.fill(5, 5);
			
			final ApplyDouble2 ad2 = (x, y, values)->{values[0] = -values[0]; values[1] = -values[1];}; 
			
			cdm.apply(ad2);

			cdm.extractDoubles(new DataOutputAdapter() {
				@Override
				public void writeDouble(double v) throws IOException {
					Assert.assertEquals(-5, v, 0.001f);
				}
			});
			
			try{cdm.apply((ApplyDouble2)null);
				Assert.fail("Mandatore exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}

			try{cdm.apply(null, ad2);
				Assert.fail("Mandatore exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{cdm.apply(Piece.of(0, 0, 2*LARGE_SIZE, 2*LARGE_SIZE), ad2);
				Assert.fail("Mandatore exception was not detected (1-st argument overlaps matrix ranges)");
			} catch (IllegalArgumentException exc) {
			}
			try{cdm.apply(Piece.of(0, 0, 1, 1), (ApplyDouble2)null);
				Assert.fail("Mandatore exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			// check assignment arguments
			
			try{
				cdm.assign((DataInput)null, Matrix.Type.REAL_INT);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{
				cdm.assign(new DataInputAdapter(), null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}

			try{
				cdm.assign(null, new DataInputAdapter(), Matrix.Type.REAL_INT);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{
				cdm.assign(Piece.of(0, 0, 2*LARGE_SIZE, 2*LARGE_SIZE), new DataInputAdapter(), Matrix.Type.REAL_INT);
				Assert.fail("Mandatory exception was not detected (1-st argument overlaps matrix range)");
			} catch (IllegalArgumentException exc) {
			}
			try{
				cdm.assign(Piece.of(0, 0, 1, 1), null, Matrix.Type.REAL_INT);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{
				cdm.assign(Piece.of(0, 0, 1, 1), new DataInputAdapter(), null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
		}
	}
 
//	@Test
	public void basicTransactionTest() throws IOException, CloneNotSupportedException {
		try(final ComplexDoubleMatrix	cdm1 = new ComplexDoubleMatrix(DIR_LOCATION, LARGE_SIZE, LARGE_SIZE);
			final ComplexDoubleMatrix	cdm2 = new ComplexDoubleMatrix(DIR_LOCATION, LARGE_SIZE, LARGE_SIZE)) {
			
			cdm1.apply((ApplyDouble2)(x,y,value)->{value[0] = x == y ? 1 : 0; value[1] = 0;});	// Identity matrix
			cdm2.apply((ApplyDouble2)(x,y,value)->{value[0] = x == y ? 1 : 0; value[1] = 0;});	// Identity matrix
			
			try(final ComplexDoubleMatrix	cdm3 = (ComplexDoubleMatrix) cdm1.add(cdm2)) {
				
			}
		}
	}
}
