package chav1961.bt.mnemort.entities.library;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.mnemort.entities.BasicEntity;
import chav1961.bt.mnemort.interfaces.DrawingCanvas;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class StaticItemsTest {
	@Test
	public void serializationTest() throws PrintingException, SyntaxException, IOException {
		copyAndTestEquals(new StaticCircle<DrawingCanvas>(null, UUID.randomUUID()), new StaticCircle<DrawingCanvas>(null, UUID.randomUUID()));
	}
	
	private static void copyAndTestEquals(final BasicEntity<DrawingCanvas,?> from, final BasicEntity<DrawingCanvas,?> to) throws IOException, PrintingException, SyntaxException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final Writer			wr = new OutputStreamWriter(baos);
				final JsonStaxPrinter	prn = new JsonStaxPrinter(wr)) {
				
				from.toJson(prn);
				prn.flush();
				
				try{from.toJson(null);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
			}
			
			try(final ByteArrayInputStream	bais = new ByteArrayInputStream(baos.toByteArray());
				final Reader				rdr = new InputStreamReader(bais);
				final JsonStaxParser		parser = new JsonStaxParser(rdr)) {
				
				parser.next();
				to.fromJson(parser);
				
				try{to.fromJson(null);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
			}
			
			Assert.assertEquals(from, to);
		}
	}
}
