package chav1961.bt.neuralnetwork.layers;

import org.junit.Assert;
import org.junit.Test;

public class ConvolutionLayerTest {
	@Test
	public void basicTest() {
		final ConvolutionLayer	cl = new ConvolutionLayer("test", 3, 3, 1, 0, 0,0,0,0,10,0,0,0,0);
		
		Assert.assertEquals("test", cl.getLayerName());
		Assert.assertEquals(3, cl.getWidth());
		Assert.assertEquals(3, cl.getHeight());
		Assert.assertEquals(8, cl.getTargetWidth(10));
		Assert.assertEquals(8, cl.getTargetHeight(10));
		Assert.assertArrayEquals(new float[] {10}, cl.process(3, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1), 0.001f);
		Assert.assertArrayEquals(new float[] {10, 10, 10, 10}, cl.process(4, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), 0.001f);
		
		Assert.assertEquals(new ConvolutionLayer("test", 3, 3, 1, 0, 0,0,0,0,10,0,0,0,0), cl);
		Assert.assertEquals(new ConvolutionLayer("test", 3, 3, 1, 0, 0,0,0,0,10,0,0,0,0).hashCode(), cl.hashCode());
		Assert.assertEquals(new ConvolutionLayer("test", 3, 3, 1, 0, 0,0,0,0,10,0,0,0,0).toString(), cl.toString());
		
		try{new ConvolutionLayer(null, 3, 3, 1, 0, 0,0,0,0,10,0,0,0,0);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new ConvolutionLayer("", 3, 3, 1, 0, 0,0,0,0,10,0,0,0,0);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{new ConvolutionLayer("test", 0, 3, 1, 0, 0,0,0,0,10,0,0,0,0);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{new ConvolutionLayer("test", 3, 0, 1, 0, 0,0,0,0,10,0,0,0,0);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{new ConvolutionLayer("test", 3, 3, 0, 0, 0,0,0,0,10,0,0,0,0);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{new ConvolutionLayer("test", 3, 3, 1, -1, 0,0,0,0,10,0,0,0,0);
			Assert.fail("Mandatory exception was not detected (5-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{new ConvolutionLayer("test", 3, 3, 1, 0, 0,0,0,0,10,0,0,0);
			Assert.fail("Mandatory exception was not detected (too few values in the 6-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{cl.getTargetWidth(0);
			Assert.fail("Mandatory exception was not detected (1-st argument must be positive)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{cl.getTargetHeight(0);
			Assert.fail("Mandatory exception was not detected (1-st argument must be positive)");
		} catch (IllegalArgumentException exc) {
		}

		try{cl.process(0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1);
			Assert.fail("Mandatory exception was not detected (1-st argument must be positive)");
		} catch (IllegalArgumentException exc) {
		}
		try{cl.process(3, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1);
			Assert.fail("Mandatory exception was not detected (2-nd argument must be positive)");
		} catch (IllegalArgumentException exc) {
		}
		try{cl.process(3, 3, 1, 1, 1, 1, 1, 1, 1, 1);
			Assert.fail("Mandatory exception was not detected (3-rd argument different size than width*height)");
		} catch (IllegalArgumentException exc) {
		}
		
	}
}
