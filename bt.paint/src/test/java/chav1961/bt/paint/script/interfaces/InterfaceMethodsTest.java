package chav1961.bt.paint.script.interfaces;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.paint.interfaces.PaintScriptException;

public class InterfaceMethodsTest {

	@Test
	public void propertiesWrapperTest() throws PaintScriptException {
		final PropertiesWrapper	pw = PropertiesWrapper.of("key1=value1\nkey2=value2"); 
		
		Assert.assertArrayEquals(new String[] {"key1","key2"}, pw.getPropKeys());
	}
}
