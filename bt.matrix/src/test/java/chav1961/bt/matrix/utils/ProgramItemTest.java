package chav1961.bt.matrix.utils;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;


public class ProgramItemTest {
	@Test
	public void basicTest() throws IOException {
		final ProgramItem[]	items = ProgramItem.load("testfile.txt");
		
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.length);
		Assert.assertEquals("1", items[0].programName);
		Assert.assertEquals("11111\n", items[0].programBody);
		
		try{ProgramItem.load(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{ProgramItem.load("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{ProgramItem.load("unknown.txt");
			Assert.fail("Mandatory exception was not detected (1-st argument resource not found)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
