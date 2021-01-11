package chav1961.bt.nlp.dictionary;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.SyntaxException;

public class DictionaryManagerTest {
	@Test
	public void basicTest() throws MalformedURLException, IOException, SyntaxException {
		try(final InputStream		is = new URL("root://chav1961.bt.nlp.dictionary.DictionaryManagerTest/dictionaries/words.zip").openStream();
			final ZipInputStream	zis = new ZipInputStream(is)) {
			final DictionaryManager	mgr = new DictionaryManager();
			
			ZipEntry	ze;
			
			while ((ze = zis.getNextEntry()) != null) {
				System.err.println("Part: "+ze.getName());
				mgr.load(new InputStreamReader(zis,"utf-8"));
			}
			System.err.println("The end!");
		}
	}
}
