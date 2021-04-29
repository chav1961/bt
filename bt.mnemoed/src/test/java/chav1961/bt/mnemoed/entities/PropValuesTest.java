package chav1961.bt.mnemoed.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.cdb.CompilerUtils;

public class PropValuesTest {
	@Test
	public void primitiveConstantValuesTest() {
		PrimitiveConstantValueSource	pcvs;
		
		pcvs = new PrimitiveConstantValueSource(true);
		Assert.assertEquals(ValueSourceType.PRIMITIVE_CONST, pcvs.getSourceType());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_BOOLEAN, pcvs.getType());
		Assert.assertTrue(pcvs.getBooleanValue());

		pcvs = new PrimitiveConstantValueSource(Byte.MAX_VALUE);
		Assert.assertEquals(ValueSourceType.PRIMITIVE_CONST, pcvs.getSourceType());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_BYTE, pcvs.getType());
		Assert.assertEquals(Byte.MAX_VALUE,pcvs.getByteValue());
		
		pcvs = new PrimitiveConstantValueSource(Short.MAX_VALUE);
		Assert.assertEquals(ValueSourceType.PRIMITIVE_CONST, pcvs.getSourceType());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_SHORT, pcvs.getType());
		Assert.assertEquals(Short.MAX_VALUE,pcvs.getShortValue());
		
		pcvs = new PrimitiveConstantValueSource(Integer.MAX_VALUE);
		Assert.assertEquals(ValueSourceType.PRIMITIVE_CONST, pcvs.getSourceType());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT, pcvs.getType());
		Assert.assertEquals(Integer.MAX_VALUE,pcvs.getIntValue());

		pcvs = new PrimitiveConstantValueSource(Long.MAX_VALUE);
		Assert.assertEquals(ValueSourceType.PRIMITIVE_CONST, pcvs.getSourceType());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG, pcvs.getType());
		Assert.assertEquals(Long.MAX_VALUE,pcvs.getLongValue());

		pcvs = new PrimitiveConstantValueSource(Float.MAX_VALUE);
		Assert.assertEquals(ValueSourceType.PRIMITIVE_CONST, pcvs.getSourceType());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT, pcvs.getType());
		Assert.assertEquals(Float.MAX_VALUE,pcvs.getFloatValue(),0.0001);

		pcvs = new PrimitiveConstantValueSource(Double.MAX_VALUE);
		Assert.assertEquals(ValueSourceType.PRIMITIVE_CONST, pcvs.getSourceType());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE, pcvs.getType());
		Assert.assertEquals(Double.MAX_VALUE,pcvs.getDoubleValue(),0.0001);

		pcvs = new PrimitiveConstantValueSource(Character.MAX_VALUE);
		Assert.assertEquals(ValueSourceType.PRIMITIVE_CONST, pcvs.getSourceType());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_CHAR, pcvs.getType());
		Assert.assertEquals(Character.MAX_VALUE,pcvs.getCharValue());
		
		Assert.assertEquals(new PrimitiveConstantValueSource(Character.MAX_VALUE), pcvs);
		Assert.assertEquals(new PrimitiveConstantValueSource(Character.MAX_VALUE).hashCode(), pcvs.hashCode());
		Assert.assertEquals(new PrimitiveConstantValueSource(Character.MAX_VALUE).toString(), pcvs.toString());
	}

	@Test
	public void primitiveValuesTest() {
		PrimitiveSubscribableValueSource	psvs = new PrimitiveSubscribableValueSource("test");
		
		Assert.assertEquals(ValueSourceType.PRIMITIVE_SUBSCRIBABLE, psvs.getSourceType());
		Assert.assertEquals("test", psvs.getName());
		
		try{ new PrimitiveSubscribableValueSource(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{ new PrimitiveSubscribableValueSource("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertEquals(new PrimitiveSubscribableValueSource("test"), psvs);
		Assert.assertEquals(new PrimitiveSubscribableValueSource("test").hashCode(), psvs.hashCode());
		Assert.assertEquals(new PrimitiveSubscribableValueSource("test").toString(), psvs.toString());
		
		PrimitiveExpressionValueSource		pevs = new PrimitiveExpressionValueSource("test");
		
		Assert.assertEquals(ValueSourceType.PRIMITIVE_EXPRESSION, pevs.getSourceType());
		Assert.assertEquals("test", pevs.getExpression());
		
		try{new PrimitiveExpressionValueSource(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new PrimitiveExpressionValueSource("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertEquals(new PrimitiveExpressionValueSource("test"), pevs);
		Assert.assertEquals(new PrimitiveExpressionValueSource("test").hashCode(), pevs.hashCode());
		Assert.assertEquals(new PrimitiveExpressionValueSource("test").toString(), pevs.toString());
	}

	@Test
	public void objectValuesTest() {
		ObjectConstantValueSource<String>	ocvs = new ObjectConstantValueSource<String>("test");
		
		Assert.assertEquals(ValueSourceType.REF_CONST, ocvs.getSourceType());
		Assert.assertEquals("test", ocvs.getObjectValue());

		Assert.assertEquals(new ObjectConstantValueSource<String>("test"), ocvs);
		Assert.assertEquals(new ObjectConstantValueSource<String>("test").hashCode(), ocvs.hashCode());
		Assert.assertEquals(new ObjectConstantValueSource<String>("test").toString(), ocvs.toString());
		
		try{new ObjectConstantValueSource<String>(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		ObjectSubscribableValueSource		osvs = new ObjectSubscribableValueSource("test");
		
		Assert.assertEquals(ValueSourceType.REF_SUBSCRIBABLE, osvs.getSourceType());
		Assert.assertEquals("test", osvs.getName());
		
		Assert.assertEquals(new ObjectSubscribableValueSource("test"), osvs);
		Assert.assertEquals(new ObjectSubscribableValueSource("test").hashCode(), osvs.hashCode());
		Assert.assertEquals(new ObjectSubscribableValueSource("test").toString(), osvs.toString());

		try{ new ObjectSubscribableValueSource(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{ new ObjectSubscribableValueSource("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		ObjectExpressionValueSource			oevs = new ObjectExpressionValueSource("test");
		
		Assert.assertEquals(ValueSourceType.REF_EXPRESSION, oevs.getSourceType());
		Assert.assertEquals("test", oevs.getExpression());

		Assert.assertEquals(new ObjectExpressionValueSource("test"), oevs);
		Assert.assertEquals(new ObjectExpressionValueSource("test").hashCode(), oevs.hashCode());
		Assert.assertEquals(new ObjectExpressionValueSource("test").toString(), oevs.toString());
		
		try{new ObjectExpressionValueSource(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new ObjectExpressionValueSource("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
