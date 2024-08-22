package chav1961.bt.databaseutils.intern;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class OrmGeneratorTest {
	private static ContentMetadataInterface	mdi = null;
	
	@BeforeClass
	public static void prepare() throws IOException {
		try(final InputStream	is = OrmGeneratorTest.class.getResourceAsStream("model.json");
			final Reader		rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
			
			mdi = ContentModelFactory.forJsonDescription(rdr);
		}
	}

	@Test
	public void basicTest() throws UnsupportedEncodingException {
		final Map<String, ByteArrayOutputStream>	map = new HashMap<>();
		
		OrmGenerator.printEntity(mdi, (name)->{
			map.put(name, new ByteArrayOutputStream());
			return map.get(name);
		}, "test");
		Assert.assertEquals(5, map.size());
		System.err.println("-------------");
		System.err.println(new String(map.get("test/entities/BookSeries.java").toByteArray(), PureLibSettings.DEFAULT_CONTENT_ENCODING));
	}
}
