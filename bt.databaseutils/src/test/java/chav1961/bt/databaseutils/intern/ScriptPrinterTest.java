package chav1961.bt.databaseutils.intern;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.print.event.PrintServiceAttributeEvent;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.streams.char2char.CodePrintStreamWrapper;

public class ScriptPrinterTest {
	private static ContentMetadataInterface	mdi = null;
	
	@BeforeClass
	public static void prepare() throws IOException {
		try(final InputStream	is = OrmGeneratorTest.class.getResourceAsStream("model.json");
			final Reader		rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
			
			mdi = ContentModelFactory.forJsonDescription(rdr);
		}
	}

	@Test
	public void basicTest() throws IOException, PrintingException {
		final ScriptPrinter	sp = new ScriptPrinter(mdi, "test");
		
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final PrintStream			ps = new PrintStream(baos);
			final CodePrintStreamWrapper	wrapper = new CodePrintStreamWrapper(ps)) {
			
			sp.print(wrapper);
			wrapper.flush();
			System.err.println(new String(baos.toByteArray()));
		}
		
		
		
	}

}
