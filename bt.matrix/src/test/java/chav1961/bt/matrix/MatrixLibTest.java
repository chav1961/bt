package chav1961.bt.matrix;

import org.junit.Assert;
import org.junit.Test;

public class MatrixLibTest {
	@Test
	public void basicTest() {
		try(final MatrixLib	lib = MatrixLib.getInstance()) {
			Assert.assertNotNull(lib);
		}
	}
	
	@Test
	public void getMatrixTest() {
		try(final MatrixLib	lib = MatrixLib.getInstance()) {
			
			// test zero matrix
			
			final Matrix	zero = lib.getZeroMatrix(10, 10);
			
			Assert.assertEquals(10, zero.numberOfRows());
			Assert.assertEquals(10, zero.numberOfColumns());
			
			final float[]	content = zero.extract();
			
			Assert.assertEquals(100, content.length);
			
			for(float item : content) {
				Assert.assertEquals(0f,  item, 0.0001f);
			}
			
			zero.close();
			
			try {
				lib.getZeroMatrix(0, 10);
				Assert.fail("Mandatory exception was not detected (zero 1-st argument)");
			} catch (IllegalArgumentException exc) {				
			}
			try {
				lib.getZeroMatrix(10, 0);
				Assert.fail("Mandatory exception was not detected (zero 2-nd argument)");
			} catch (IllegalArgumentException exc) {				
			}
			
			// test identity matrix
			
			final Matrix	identity = lib.getIdentityMatrix(10, 10);
			
			Assert.assertEquals(10, identity.numberOfRows());
			Assert.assertEquals(10, identity.numberOfColumns());
			
			final float[]	content2 = identity.extract();
			
			Assert.assertEquals(100, content2.length);
			
			for(int i = 0; i < identity.numberOfRows(); i++) {
				for(int j = 0; j < identity.numberOfColumns(); j++) {
					final float	val = content2[i*identity.numberOfColumns() + j];
					
					Assert.assertEquals(i == j ? 1f : 0f,  val, 0.0001f);
				}
			}
			
			identity.close();

			try {
				lib.getIdentityMatrix(0, 10);
				Assert.fail("Mandatory exception was not detected (zero 1-st argument)");
			} catch (IllegalArgumentException exc) {				
			}
			try {
				lib.getIdentityMatrix(10, 0);
				Assert.fail("Mandatory exception was not detected (zero 2-nd argument)");
			} catch (IllegalArgumentException exc) {				
			}
			
			// test any matrix			
			
			final Matrix	any = lib.getIdentityMatrix(10, 10);
			
			Assert.assertEquals(10, any.numberOfRows());
			Assert.assertEquals(10, any.numberOfColumns());
			
			final float[]	content3 = any.extract();
			
			Assert.assertEquals(100, content3.length);
			
			any.assign(new float[] {10, 20, 30});
			
			final float[]	content4 = any.extract();
			
			Assert.assertEquals(10, content4[0], 0.0001f);
			Assert.assertEquals(20, content4[1], 0.0001f);
			Assert.assertEquals(30, content4[2], 0.0001f);
			Assert.assertEquals(0, content4[3], 0.0001f);
			
			any.close();
			
			try {
				lib.getMatrix(0, 10);
				Assert.fail("Mandatory exception was not detected (zero 1-st argument)");
			} catch (IllegalArgumentException exc) {				
			}
			try {
				lib.getMatrix(10, 0);
				Assert.fail("Mandatory exception was not detected (zero 2-nd argument)");
			} catch (IllegalArgumentException exc) {				
			}
			
		}
	}
}
