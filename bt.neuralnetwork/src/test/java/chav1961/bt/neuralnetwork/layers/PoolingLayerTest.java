package chav1961.bt.neuralnetwork.layers;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.neuralnetwork.interfaces.PoolingType;
import chav1961.bt.neuralnetwork.layers.PoolingLayer.UserDefinedPoolingFunction;

public class PoolingLayerTest {
	@Test
	public void basicTest() {
		final PoolingLayer	pl = new PoolingLayer("test", 2, 2, PoolingType.SUM_POOLING);
		
		Assert.assertEquals("test", pl.getLayerName());
		Assert.assertEquals(2, pl.getWidth());
		Assert.assertEquals(2, pl.getHeight());
		Assert.assertEquals(2, pl.getTargetWidth(4));
		Assert.assertEquals(2, pl.getTargetHeight(4));
		Assert.assertArrayEquals(new float[]{4}, pl.process(2, 2, 1, 1, 1, 1), 0.001f);
		Assert.assertArrayEquals(new float[]{4, 4, 4, 4}, pl.process(4, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), 0.001f);
		Assert.assertArrayEquals(new float[]{4, 4, 4, 4}, pl.process(5, 5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), 0.001f);
		
		Assert.assertEquals(new PoolingLayer("test", 2, 2, PoolingType.SUM_POOLING), pl);
		Assert.assertEquals(new PoolingLayer("test", 2, 2, PoolingType.SUM_POOLING).hashCode(), pl.hashCode());
		Assert.assertEquals(new PoolingLayer("test", 2, 2, PoolingType.SUM_POOLING).toString(), pl.toString());
		
		try{new PoolingLayer(null, 2, 2, PoolingType.SUM_POOLING);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new PoolingLayer("", 2, 2, PoolingType.SUM_POOLING);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new PoolingLayer("test", 0, 2, PoolingType.SUM_POOLING);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{new PoolingLayer("test", 2, 0, PoolingType.SUM_POOLING);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{new PoolingLayer("test", 2, 2, (PoolingType)null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new PoolingLayer("test", 2, 2, (UserDefinedPoolingFunction)null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
		
		try{pl.getTargetWidth(0);
			Assert.fail("Mandatory exception was not detected (1-st argument must be positive)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{pl.getTargetHeight(0);
			Assert.fail("Mandatory exception was not detected (1-st argument must be positive)");
		} catch (IllegalArgumentException exc) {
		}

		try{pl.process(0, 2, 1, 1, 1, 1);
			Assert.fail("Mandatory exception was not detected (1-st argument must be positive)");
		} catch (IllegalArgumentException exc) {
		}
		try{pl.process(2, 0, 1, 1, 1, 1);
			Assert.fail("Mandatory exception was not detected (2-nd argument must be positive)");
		} catch (IllegalArgumentException exc) {
		}
		try{pl.process(2, 2, 1, 1, 1);
			Assert.fail("Mandatory exception was not detected (3-rd argument difference with width*height)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void funcTest() {
		PoolingLayer	pl = new PoolingLayer("test", 2, 2, PoolingType.SUM_POOLING);

		Assert.assertArrayEquals(new float[]{4}, pl.process(2, 2, 1, 1, 1, 1), 0.001f);

		pl = new PoolingLayer("test", 2, 2, PoolingType.MAX_POOLING);

		Assert.assertArrayEquals(new float[]{4}, pl.process(2, 2, 1, 2, 3, 4), 0.001f);

		pl = new PoolingLayer("test", 2, 2, PoolingType.AVERAGE_POOLING);

		Assert.assertArrayEquals(new float[]{1.5f}, pl.process(2, 2, 1, 2, 1, 2), 0.001f);
	}
}
