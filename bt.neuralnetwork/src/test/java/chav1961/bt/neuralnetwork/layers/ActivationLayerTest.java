package chav1961.bt.neuralnetwork.layers;

import java.util.function.DoubleUnaryOperator;

import org.junit.Assert;
import org.junit.Test;

public class ActivationLayerTest {
	@Test
	public void basicTest() {
		final DoubleUnaryOperator	f1 = (x)->x*x, f2 = (x)->Math.sqrt(x);
		final ActivationLayer		al = new ActivationLayer("test", f1, f2);
		
		Assert.assertEquals("test", al.getLayerName());
		Assert.assertEquals(1, al.getWidth());
		Assert.assertEquals(1, al.getHeight());
		Assert.assertEquals(10, al.getTargetWidth(10));
		Assert.assertEquals(10, al.getTargetHeight(10));
		Assert.assertArrayEquals(new float[] {1, 4, 9, 16}, al.process(2, 2, 1, 2, 3, 4), 0.001f);
		
		Assert.assertEquals(new ActivationLayer("test", f1, f2), al);
		Assert.assertEquals(new ActivationLayer("test", f1, f2).hashCode(), al.hashCode());
		Assert.assertEquals(new ActivationLayer("test", f1, f2).toString(), al.toString());
		
		try{new ActivationLayer(null, f1, f2);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new ActivationLayer("", f1, f2);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new ActivationLayer("test", null, f2);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new ActivationLayer("test", f1, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{al.getTargetWidth(0);
			Assert.fail("Mandatory exception was not detected (1-st argument must be positive)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{al.getTargetHeight(0);
			Assert.fail("Mandatory exception was not detected (1-st argument must be positive)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{al.process(0, 1, 1);
			Assert.fail("Mandatory exception was not detected (1-st argument must be positive)");
		} catch (IllegalArgumentException exc) {
		}
		try{al.process(1, 0, 1);
			Assert.fail("Mandatory exception was not detected (2-nd argument must be positive)");
		} catch (IllegalArgumentException exc) {
		}
		try{al.process(1, 1, 1, 1);
			Assert.fail("Mandatory exception was not detected (3-rd argument differ than width*height)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
