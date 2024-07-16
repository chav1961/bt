package chav1961.bt.preproc;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.io.IOException;
import java.io.Reader;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;

public class NestedReaderTest {

	@Test
	public void basicTest() throws IOException {
		try(final Reader			rdr = new StringReader("1\n2\n12345678910\n");
			final NestedReader		nested = new NestedReader(rdr, 10)) {
			final NestedReader.Line	l = new NestedReader.Line(); 
			
			Assert.assertTrue(nested.next(l));
			Assert.assertEquals("1\n", new String(l.content, l.from, l.len));
			Assert.assertEquals(1, l.lineNo);
			Assert.assertTrue(nested.next(l));
			Assert.assertEquals("2\n", new String(l.content, l.from, l.len));
			Assert.assertEquals(2, l.lineNo);
			Assert.assertTrue(nested.next(l));
			Assert.assertEquals("12345678910\n", new String(l.content, l.from, l.len));
			Assert.assertEquals(3, l.lineNo);
			Assert.assertFalse(nested.next(l));
			
			try{nested.next(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
		}

		try(final Reader			rdr = new StringReader("1\n2\n12345678910");
			final NestedReader		nested = new NestedReader(rdr, 10)) {
			final NestedReader.Line	l = new NestedReader.Line(); 
			
			Assert.assertTrue(nested.next(l));
			Assert.assertEquals("1\n", new String(l.content, l.from, l.len));
			Assert.assertEquals(1, l.lineNo);
			Assert.assertTrue(nested.next(l));
			Assert.assertEquals("2\n", new String(l.content, l.from, l.len));
			Assert.assertEquals(2, l.lineNo);
			Assert.assertTrue(nested.next(l));
			Assert.assertEquals("12345678910\n", new String(l.content, l.from, l.len));
			Assert.assertEquals(3, l.lineNo);
			Assert.assertFalse(nested.next(l));
		}
		
		try{new NestedReader(null, 10);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new NestedReader(new StringReader(""), 0);
			Assert.fail("Mandatory exception was not detected (2-nd argument is not greater than 0)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void pushTest() throws IOException {
		try(final Reader			rdr = new StringReader("1\n2\n12345678910\n");
			final NestedReader		nested = new NestedReader(rdr, 10)) {
			final NestedReader.Line	l = new NestedReader.Line(); 
			
			Assert.assertTrue(nested.next(l));
			Assert.assertEquals("1\n", new String(l.content, l.from, l.len));
			Assert.assertNull(l.source);
			Assert.assertEquals(1, l.lineNo);
			nested.pushSource(URIUtils.convert2selfURI("4\n5".toCharArray(), PureLibSettings.DEFAULT_CONTENT_ENCODING).toURL());

			Assert.assertTrue(nested.next(l));
			Assert.assertEquals("4\n", new String(l.content, l.from, l.len));
			Assert.assertEquals(1, l.lineNo);
			Assert.assertNotNull(l.source);

			Assert.assertTrue(nested.next(l));
			Assert.assertEquals("5\n", new String(l.content, l.from, l.len));
			Assert.assertEquals(2, l.lineNo);
			Assert.assertNotNull(l.source);
			
			Assert.assertTrue(nested.next(l));
			Assert.assertEquals("2\n", new String(l.content, l.from, l.len));
			Assert.assertEquals(2, l.lineNo);
			Assert.assertNull(l.source);
			
			Assert.assertTrue(nested.next(l));
			Assert.assertEquals("12345678910\n", new String(l.content, l.from, l.len));
			Assert.assertEquals(3, l.lineNo);
			Assert.assertFalse(nested.next(l));
			Assert.assertNull(l.source);
		}
	}
}
