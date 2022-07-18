package chav1961.bt.paint.script;

import java.io.IOException;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.PropertiesWrapper;
import chav1961.bt.paint.script.interfaces.SystemWrapper;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class PropertiesWrapperTest {
	@Test
	public void propertiesTest() throws IOException, PaintScriptException {		
		final PropertiesWrapper		pw = PropertiesWrapper.of(Utils.mkProps("key1","100","key2","200"));

		try{pw.get("unknown");
			Assert.fail("Mandatory exception was not detected (unknown 1-st argument)");
		} catch (PaintScriptException exc) {
		}
		try{pw.get(null); 
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pw.get("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals("123", pw.get("unknown","123"));
		try{pw.get(null, "123");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pw.get("", "123");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pw.get("unknown", (String)null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertNotNull(pw.get("key1"));
		
		Assert.assertFalse(pw.contains("unknown"));
		Assert.assertTrue(pw.contains("key1"));
		try{pw.contains(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pw.contains("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}	

	@Test
	public void keysAndConversionTest() throws IOException, PaintScriptException {		
		final PropertiesWrapper			pw = PropertiesWrapper.of(Utils.mkProps("key1","100","key2","200"));
		
		Assert.assertArrayEquals(new String[] {"key1","key2"}, pw.getPropKeys());

		try{pw.get("unknown", int.class);
			Assert.fail("Mandatory exception was not detected (unknown 1-st argument)");
		} catch (PaintScriptException exc) {
		}
		try{pw.get(null, int.class);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pw.get("", int.class);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pw.get("key1", (Class<?>)null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{pw.get("key1", SubstitutableProperties.class);
			Assert.fail("Mandatory exception was not detected (unsupported 2-nd argument)");
		} catch (PaintScriptException exc) {
		}

		Assert.assertEquals(Integer.valueOf(100), pw.get("unknown", int.class, 100));
		try{pw.get(null, int.class, 100);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pw.get("", int.class, 100);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pw.get("key1", (Class<String>)null, "");
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{pw.get("key1", SubstitutableProperties.class, null);
			Assert.fail("Mandatory exception was not detected (unsupported 2-nd argument)");
		} catch (PaintScriptException exc) {
		}
	}

	@Test
	public void storesTest() throws IOException, PaintScriptException {		
		final PropertiesWrapper			pw = new PropertiesWrapperImpl();
		
		pw.set("key1", "value1");
		Assert.assertEquals("value1", pw.get("key1"));

		try{pw.set(null, "value1");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pw.set("", "value1");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pw.set("key1", null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		pw.set("key2", int.class, 100);
		Assert.assertEquals(Integer.valueOf(100), pw.get("key2",Integer.class));

		try{pw.set(null, String.class, "value1");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pw.set("", String.class, "value1");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pw.set("key2", null, "value1");
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{pw.set("key2", String.class, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}
}
