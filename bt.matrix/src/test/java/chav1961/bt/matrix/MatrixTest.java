package chav1961.bt.matrix;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.matrix.Matrix.Type;

public class MatrixTest {

	@Test
	public void basicFloatTest() {
		try(final MatrixLib	lib =  MatrixLib.getInstance()) {
			final Matrix	matrix = lib.getZeroMatrix(3, 3);
			
			Assert.assertEquals(3, matrix.numberOfRows());
			Assert.assertEquals(3, matrix.numberOfColumns());
			
			Assert.assertArrayEquals(new float[] {0f,0f,0f,0f,0f,0f,0f,0f,0f}, matrix.extractFloats(),  0.0001f);
			
			matrix.assign(new float[] {1f,2f,3f,4f,5f,6f,7f,8f,9f});
			
			Assert.assertArrayEquals(new float[] {1f,2f,3f,4f,5f,6f,7f,8f,9f}, matrix.extractFloats(),  0.0001f);
			
			try{
				matrix.assign((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			try{
				matrix.assign((float[])null, 0, 8);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{
				matrix.assign(new float[] {1f,2f,3f,4f,5f,6f,7f,8f,9f}, -1, 8);
				Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{
				matrix.assign(new float[] {1f,2f,3f,4f,5f,6f,7f,8f,9f}, 10, 8);
				Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{
				matrix.assign(new float[] {1f,2f,3f,4f,5f,6f,7f,8f,9f}, 0, -1);
				Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{
				matrix.assign(new float[] {1f,2f,3f,4f,5f,6f,7f,8f,9f}, 0, 10);
				Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{
				matrix.assign(new float[] {1f,2f,3f,4f,5f,6f,7f,8f,9f}, 1, 0);
				Assert.fail("Mandatory exception was not detected (3-rd argument is less than 2-nd one)");
			} catch (IllegalArgumentException exc) {
			}
		}
	}

	@Test
	public void basicDoubleTest() {
		try(final MatrixLib	lib =  MatrixLib.getInstance(Type.REAL_FLOAT, Type.REAL_DOUBLE)) {
			if (lib.isTypeSupported(Type.REAL_DOUBLE)) {
				final Matrix	matrix = lib.getZeroMatrix(Type.REAL_DOUBLE, 3, 3);
				
				Assert.assertEquals(3, matrix.numberOfRows());
				Assert.assertEquals(3, matrix.numberOfColumns());
				
				Assert.assertArrayEquals(new double[] {0,0,0,0,0,0,0,0,0}, matrix.extractDoubles(),  0.0001);
				
				matrix.assign(new double[] {1,2,3,4,5,6,7,8,9});
				
				Assert.assertArrayEquals(new double[] {1,2,3,4,5,6,7,8,9}, matrix.extractDoubles(),  0.0001);
				
				try{
					matrix.assign((double[])null);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				
				try{
					matrix.assign((double[])null, 0, 8);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try{
					matrix.assign(new double[] {1,2,3,4,5,6,7,8,9}, -1, 8);
					Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				try{
					matrix.assign(new double[] {1,2,3,4,5,6,7,8,9}, 10, 8);
					Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				try{
					matrix.assign(new double[] {1,2,3,4,5,6,7,8,9}, 0, -1);
					Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				try{
					matrix.assign(new double[] {1,2,3,4,5,6,7,8,9}, 0, 10);
					Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				try{
					matrix.assign(new double[] {1,2,3,4,5,6,7,8,9}, 1, 0);
					Assert.fail("Mandatory exception was not detected (3-rd argument is less than 2-nd one)");
				} catch (IllegalArgumentException exc) {
				}
			}
		}
	}

	@Test
	public void basicComplexFloatTest() {
		try(final MatrixLib	lib =  MatrixLib.getInstance(Type.COMPLEX_FLOAT)) {
			final Matrix	matrix = lib.getZeroMatrix(Type.COMPLEX_FLOAT, 3, 3);
			
			Assert.assertEquals(3, matrix.numberOfRows());
			Assert.assertEquals(3, matrix.numberOfColumns());
			
			Assert.assertArrayEquals(new float[] {0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f}, matrix.extractFloats(),  0.0001f);
			
			matrix.assign(new float[] {1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f,7f,0f,8f,0f,9f,0f});
			
			Assert.assertArrayEquals(new float[] {1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f,7f,0f,8f,0f,9f,0f}, matrix.extractFloats(),  0.0001f);
			
			try{
				matrix.assign((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			try{
				matrix.assign((float[])null, 0, 8);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{
				matrix.assign(new float[] {1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f,7f,0f,8f,0f,9f,0f}, -1, 8);
				Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{
				matrix.assign(new float[] {1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f,7f,0f,8f,0f,9f,0f}, 10, 8);
				Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{
				matrix.assign(new float[] {1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f,7f,0f,8f,0f,9f,0f}, 0, -1);
				Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{
				matrix.assign(new float[] {1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f,7f,0f,8f,0f,9f,0f}, 0, 20);
				Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{
				matrix.assign(new float[] {1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f,7f,0f,8f,0f,9f,0f}, 1, 0);
				Assert.fail("Mandatory exception was not detected (3-rd argument is less than 2-nd one)");
			} catch (IllegalArgumentException exc) {
			}
		}
	}
	
	@Test
	public void simpleFloatArithmeticTest() {
		try(final MatrixLib	lib =  MatrixLib.getInstance()) {
			final Matrix	matrix1 = lib.getZeroMatrix(3, 3);
			final Matrix	matrix2 = lib.getZeroMatrix(3, 3);
			
			matrix1.assign(new float[] {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f});
			matrix2.assign(new float[] {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f});
			
			Assert.assertArrayEquals(new float[] {2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f}, matrix1.add(1).extractFloats(), 0.0001f);
			Assert.assertArrayEquals(new float[] {2f, 4f, 6f, 8f, 10f, 12f, 14f, 16f, 18f}, matrix1.add(matrix2).extractFloats(), 0.0001f);
			Assert.assertArrayEquals(new float[] {-1f, -2f, -3f, -4f, -5f, -6f, -7f, -8f, -9f}, matrix1.subtractFrom(0).extractFloats(), 0.0001f);
			Assert.assertArrayEquals(new float[] {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f}, matrix1.subtract(matrix2).extractFloats(), 0.0001f);
			Assert.assertArrayEquals(new float[] {2f, 4f, 6f, 8f, 10f, 12f, 14f, 16f, 18f}, matrix1.mul(2).extractFloats(), 0.0001f);
			Assert.assertArrayEquals(new float[] {30f, 36f, 42f, 66f, 81f, 96f, 102f, 126f, 150f}, matrix1.mul(matrix2).extractFloats(), 0.0001f);
			Assert.assertArrayEquals(new float[] {1f, 4f, 9f, 16f, 25f, 36f, 49f, 64f, 81f}, matrix1.mulH(matrix2).extractFloats(), 0.0001f);
			Assert.assertArrayEquals(new float[] {1f, 4f, 7f, 2f, 5f, 8f, 3f, 6f, 9f}, matrix1.trans().extractFloats(), 0.0001f);
		}
	}

	@Test
	public void simpleDoubleArithmeticTest() {
		try(final MatrixLib	lib =  MatrixLib.getInstance(Type.REAL_FLOAT, Type.REAL_DOUBLE)) {
			if (lib.isTypeSupported(Type.REAL_DOUBLE)) {
				final Matrix	matrix1 = lib.getZeroMatrix(3, 3);
				final Matrix	matrix2 = lib.getZeroMatrix(3, 3);
				
				matrix1.assign(new float[] {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f});
				matrix2.assign(new float[] {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f});
				
				Assert.assertArrayEquals(new float[] {2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f}, matrix1.add(1).extractFloats(), 0.0001f);
				Assert.assertArrayEquals(new float[] {2f, 4f, 6f, 8f, 10f, 12f, 14f, 16f, 18f}, matrix1.add(matrix2).extractFloats(), 0.0001f);
				Assert.assertArrayEquals(new float[] {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f}, matrix1.subtract(matrix2).extractFloats(), 0.0001f);
				Assert.assertArrayEquals(new float[] {2f, 4f, 6f, 8f, 10f, 12f, 14f, 16f, 18f}, matrix1.mul(2).extractFloats(), 0.0001f);
				Assert.assertArrayEquals(new float[] {1f, 4f, 9f, 16f, 25f, 36f, 49f, 64f, 81f}, matrix1.mulH(matrix2).extractFloats(), 0.0001f);
				Assert.assertArrayEquals(new float[] {1f, 4f, 7f, 2f, 5f, 8f, 3f, 6f, 9f}, matrix1.trans().extractFloats(), 0.0001f);
			}
		}
	}

	@Test
	public void simpleComplexFloatArithmeticTest() {
		try(final MatrixLib	lib =  MatrixLib.getInstance(Type.COMPLEX_FLOAT)) {
			final Matrix	matrix1 = lib.getZeroMatrix(Type.COMPLEX_FLOAT, 3, 3);
			final Matrix	matrix2 = lib.getZeroMatrix(Type.COMPLEX_FLOAT, 3, 3);
			
			matrix1.assign(new float[] {1f, 0f, 2f, 0f, 3f, 0f, 4f, 0f, 5f, 0f, 6f, 0f, 7f, 0f, 8f, 0f, 9f, 0f});
			matrix2.assign(new float[] {1f, 0f, 2f, 0f, 3f, 0f, 4f, 0f, 5f, 0f, 6f, 0f, 7f, 0f, 8f, 0f, 9f, 0f});
			
			Assert.assertArrayEquals(new float[] {2f, 0f, 3f, 0f, 4f, 0f, 5f, 0f, 6f, 0f, 7f, 0f, 8f, 0f, 9f, 0f, 10f, 0f}, matrix1.add(1, 0).extractFloats(), 0.0001f);
			Assert.assertArrayEquals(new float[] {2f, 0f, 4f, 0f, 6f, 0f, 8f, 0f, 10f, 0f, 12f, 0f, 14f, 0f, 16f, 0f, 18f, 0f}, matrix1.add(matrix2).extractFloats(), 0.0001f);
			Assert.assertArrayEquals(new float[] {-1f, 0f, -2f, 0f, -3f, 0f, -4f, 0f, -5f, 0f, -6f, 0f, -7f, 0f, -8f, 0f, -9f, 0f}, matrix1.subtractFrom(0, 0).extractFloats(), 0.0001f);
			Assert.assertArrayEquals(new float[] {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f}, matrix1.subtract(matrix2).extractFloats(), 0.0001f);
			Assert.assertArrayEquals(new float[] {2f, 0f, 4f, 0f, 6f, 0f, 8f, 0f, 10f, 0f, 12f, 0f, 14f, 0f, 16f, 0f, 18f, 0f}, matrix1.mul(2, 0).extractFloats(), 0.0001f);
			Assert.assertArrayEquals(new float[] {30f, 0f, 36f, 0f, 42f, 0f, 66f, 0f, 81f, 0f, 96f, 0f, 102f, 0f, 126f, 0f, 150f, 0f}, matrix1.mul(matrix2).extractFloats(), 0.0001f);
			Assert.assertArrayEquals(new float[] {1f, 0f, 4f, 0f, 9f, 0f, 16f, 0f, 25f, 0f, 36f, 0f, 49f, 0f, 64f, 0f, 81f, 0f}, matrix1.mulH(matrix2).extractFloats(), 0.0001f);
			Assert.assertArrayEquals(new float[] {1f, 0f, 4f, 0f, 7f, 0f, 2f, 0f, 5f, 0f, 8f, 0f, 3f, 0f, 6f, 0f, 9f, 0f}, matrix1.trans().extractFloats(), 0.0001f);
		}
	}
	
	@Test
	public void specialFloatArithmeticTest() {
		try(final MatrixLib	lib =  MatrixLib.getInstance()) {
			final Matrix	matrix1 = lib.getZeroMatrix(3, 3);
			final Matrix	matrix2 = lib.getZeroMatrix(3, 3);
			
			matrix1.assign(new float[] {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f});
			matrix2.assign(new float[] {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f});
			
			Assert.assertArrayEquals(new double[] {15}, matrix1.track(), 0.0001f);

			Assert.assertArrayEquals(new float[] {30, 36, 42, 66, 81, 96, 102, 126, 150}, matrix1.mul(matrix2).extractFloats(), 0.0001f);
			
			final Matrix	matrix3 = lib.getZeroMatrix(4, 4);
			
			matrix3.assign(new float[] {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 9f, 8f, 7f, 6f, 5f, 2f});
			Assert.assertArrayEquals(new double[] {-16}, matrix3.det(), 0.0001f);
			
			final Matrix	matrix4 = lib.getZeroMatrix(2, 2);
			final Matrix	matrix5 = lib.getZeroMatrix(2, 2);

			matrix4.assign(new float[] {1f, 2f, 3f, 4f});
			matrix5.assign(new float[] {0f, 5f, 6f, 7f});
			
			Assert.assertArrayEquals(new float[] {0f, 5f, 0f, 10f, 6f, 7f, 12f, 14f, 0f, 15f, 0f, 20f, 18f, 21f, 24f, 28f}, matrix4.mulK(matrix5).extractFloats(), 0.0001f);

			final Matrix	matrix6 = lib.getZeroMatrix(3, 3);
			
			matrix6.assign(new float[] {1f, 2f, 3f, 4f, 1f, 2f, 3f, 4f, 1f});

			Assert.assertArrayEquals(new float[] {-7f/36f, 5f/18f, 1f/36f, 1f/18f, -2f/9f, 5f/18f, 13f/36f, 1f/18f, -7f/36f}, matrix6.inv().extractFloats(), 0.0001f);
		}
	}

	@Test
	public void specialDoubleArithmeticTest() {
		try(final MatrixLib	lib =  MatrixLib.getInstance(Type.REAL_FLOAT, Type.REAL_DOUBLE)) {
			if (lib.isTypeSupported(Type.REAL_DOUBLE)) {
				final Matrix	matrix1 = lib.getZeroMatrix(3, 3);
				final Matrix	matrix2 = lib.getZeroMatrix(3, 3);
				
				matrix1.assign(new float[] {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f});
				matrix2.assign(new float[] {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f});
				
				Assert.assertArrayEquals(new double[] {15, 0}, matrix1.track(), 0.0001f);
	
				Assert.assertArrayEquals(new float[] {30, 36, 42, 66, 81, 96, 102, 126, 150}, matrix1.mul(matrix2).extractFloats(), 0.0001f);
				
				final Matrix	matrix3 = lib.getZeroMatrix(4, 4);
				
				matrix3.assign(new float[] {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 9f, 8f, 7f, 6f, 5f, 2f});
				Assert.assertArrayEquals(new double[] {-16, 0}, matrix3.det(), 0.0001f);
				
				final Matrix	matrix4 = lib.getZeroMatrix(2, 2);
				final Matrix	matrix5 = lib.getZeroMatrix(2, 2);
	
				matrix4.assign(new float[] {1f, 2f, 3f, 4f});
				matrix5.assign(new float[] {0f, 5f, 6f, 7f});
				
				Assert.assertArrayEquals(new float[] {0f, 5f, 0f, 10f, 6f, 7f, 12f, 14f, 0f, 15f, 0f, 20f, 18f, 21f, 24f, 28f}, matrix4.mulK(matrix5).extractFloats(), 0.0001f);
	
				final Matrix	matrix6 = lib.getZeroMatrix(3, 3);
				
				matrix6.assign(new float[] {1f, 2f, 3f, 4f, 1f, 2f, 3f, 4f, 1f});
	
				Assert.assertArrayEquals(new float[] {-7f/36f, 5f/18f, 1f/36f, 1f/18f, -2f/9f, 5f/18f, 13f/36f, 1f/18f, -7f/36f}, matrix6.inv().extractFloats(), 0.0001f);
			}
		}
	}

	@Test
	public void specialComplexFloatArithmeticTest() {
		try(final MatrixLib	lib =  MatrixLib.getInstance(Type.COMPLEX_FLOAT)) {
			final Matrix	matrix1 = lib.getZeroMatrix(Type.COMPLEX_FLOAT, 3, 3);
			final Matrix	matrix2 = lib.getZeroMatrix(Type.COMPLEX_FLOAT, 3, 3);
			
			matrix1.assign(new float[] {1f, 0f, 2f, 0f, 3f, 0f, 4f, 0f, 5f, 0f, 6f, 0f, 7f, 0f, 8f, 0f, 9f, 0f});
			matrix2.assign(new float[] {1f, 0f, 2f, 0f, 3f, 0f, 4f, 0f, 5f, 0f, 6f, 0f, 7f, 0f, 8f, 0f, 9f, 0f});
			
			Assert.assertArrayEquals(new double[] {15, 0}, matrix1.track(), 0.0001f);

			Assert.assertArrayEquals(new float[] {30f, 0f, 36f, 0f, 42f, 0f, 66f, 0f, 81f, 0f, 96f, 0f, 102f, 0f, 126f, 0f, 150f, 0f}, matrix1.mul(matrix2).extractFloats(), 0.0001f);
			
			final Matrix	matrix3 = lib.getZeroMatrix(Type.COMPLEX_FLOAT, 4, 4);
			
			matrix3.assign(new float[] {1f, 0f, 2f, 0f, 3f, 0f, 4f, 0f, 5f, 0f, 6f, 0f, 7f, 0f, 8f, 0f, 9f, 0f, 10f, 0f, 9f, 0f, 8f, 0f, 7f, 0f, 6f, 0f, 5f, 0f, 2f, 0f});
			Assert.assertArrayEquals(new double[] {-16, 0}, matrix3.det(), 0.0001f);
			
			final Matrix	matrix4 = lib.getZeroMatrix(Type.COMPLEX_FLOAT, 2, 2);
			final Matrix	matrix5 = lib.getZeroMatrix(Type.COMPLEX_FLOAT, 2, 2);

			matrix4.assign(new float[] {1f, 0f, 2f, 0f, 3f, 0f, 4f, 0f});
			matrix5.assign(new float[] {0f, 0f, 5f, 0f, 6f, 0f, 7f, 0f});
			
			Assert.assertArrayEquals(new float[] {0f, 0f, 5f, 0f, 0f, 0f, 10f, 0f, 6f, 0f, 7f, 0f, 12f, 0f, 14f, 0f, 0f, 0f, 15f, 0f, 0f, 0f, 20f, 0f, 18f, 0f, 21f, 0f, 24f, 0f, 28f, 0f}, matrix4.mulK(matrix5).extractFloats(), 0.0001f);

			final Matrix	matrix6 = lib.getZeroMatrix(Type.COMPLEX_FLOAT, 3, 3);
			
			matrix6.assign(new float[] {1f, 0f, 2f, 0f, 3f, 0f, 4f, 0f, 1f, 0f, 2f, 0f, 3f, 0f, 4f, 0f, 1f, 0f});

			Assert.assertArrayEquals(new float[] {-7f/36f, 0f, 5f/18f, 0f, 1f/36f, 0f, 1f/18f, 0f, -2f/9f, 0f, 5f/18f, 0f, 13f/36f, 0f, 1f/18f, 0f, -7f/36f, 0f}, matrix6.inv().extractFloats(), 0.0001f);
		}
	}
}
