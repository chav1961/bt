package chav1961.bt.matrix;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class CalculatorTest {

	@Test
	public void basicTest() throws SyntaxException, CalculationException {
		try(final MatrixLib		lib = MatrixLib.getInstance();
			final Matrix		identity1 = lib.getIdentityMatrix(2, 2);
			final Matrix		identity2 = lib.getIdentityMatrix(2, 2);
			final Matrix		zero = lib.getZeroMatrix(2, 2);) {

			// term test
			
			testExec(lib, "%1", new float[] {-1, 0, 0, -1}, identity1);
			testExec(lib, "%1.t", new float[] {-1, 0, 0, -1}, identity1);
			testExec(lib, "%1.det", new float[] {-1, 0, 0, -1}, identity1);
			testExec(lib, "%1.inv", new float[] {-1, 0, 0, -1}, identity1);
			testExec(lib, "%1.sp", new float[] {-1, 0, 0, -1}, identity1);
			
			// unary test
			
			testExec(lib, "-%1", new float[] {-1, 0, 0, -1}, identity1);
			testExec(lib, "-1", new float[] {-1});
			testExec(lib, "%1^2", new float[] {-1, 0, 0, -1}, identity1);
			testExec(lib, "2^2", new float[] {-1});
			
			// mul test
			
			testExec(lib, "%1*%2", new float[] {1, 0, 0, 1}, identity1, identity2);
			testExec(lib, "2*%1", new float[] {2, 0, 0, 2}, identity1, identity2);
			testExec(lib, "%1*2", new float[] {2, 0, 0, 2}, identity1, identity2);
			testExec(lib, "1*2", new float[] {1, 0, 1, 0}, identity1, identity2);
			testExec(lib, "%1**%2", new float[] {1, 0, 0, 1}, identity1, identity2);
			testExec(lib, "%1***%2", new float[] {1, 0, 0, 1}, identity1, identity2);
			
			// add test
			
			testExec(lib, "%1+%2", new float[] {2, 0, 0, 2}, identity1, identity2);
			testExec(lib, "%1-%2", new float[] {0, 0, 0, 0}, identity1, identity2);
			testExec(lib, "%1+2", new float[] {3, 2, 3, 2}, identity1);
			testExec(lib, "1+2", new float[] {3}, identity1);
			testExec(lib, "%1-2", new float[] {-1, -2, -1, -2}, identity1);
			testExec(lib, "2+%1", new float[] {3, 2, 3, 2}, identity1);
			testExec(lib, "2-%1", new float[] {1, 2, 1, 2}, identity1);
			testExec(lib, "2-1", new float[] {1}, identity1);
		}
	}

	private void testExec(final MatrixLib lib, final String expr, final float[] result, final Matrix... oeprands) throws SyntaxException, CalculationException {
		try(final Calculator	calc = MatrixLib.compile("%1+%2")) {
			final Matrix		m = calc.calculate(lib.getIdentityMatrix(2, 2), lib.getIdentityMatrix(2, 2));
		
			Assert.assertArrayEquals(result, m.extract(), 0.001f);
		}
	}
	
}
