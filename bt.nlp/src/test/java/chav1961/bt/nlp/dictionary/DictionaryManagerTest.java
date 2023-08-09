package chav1961.bt.nlp.dictionary;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;

import chav1961.purelib.basic.exceptions.SyntaxException;

public class DictionaryManagerTest {
	@Test
	public void basicTest() throws MalformedURLException, IOException, SyntaxException {
		final DictionaryManager	dm;
		
		final long	start1 = System.currentTimeMillis();
		try(final InputStream	is = new FileInputStream("C:/tmp/dict.opcorpora.xml")) {
			dm = new DictionaryManager(is);

			dm.nextWord("дома ".toCharArray(), 0, (w)->System.err.println("W: "+w.getCurrentFormAsString()+", init="+w.getInitialFormAsString()));
		}
		System.err.println("Load: duration="+(System.currentTimeMillis() - start1));
		System.gc();
		
		final long	start2 = System.currentTimeMillis();
		try(final OutputStream		os = new FileOutputStream("C:/tmp/dict.opcorpora.bin.gz");
			final OutputStream		gzos = new GZIPOutputStream(os);
			final OutputStream		bos = new BufferedOutputStream(gzos, 8192*8);
			final DataOutputStream	dos = new DataOutputStream(bos)) {
			
			dm.upload(dos);
			dos.flush();
		}
		System.err.println("Dump: duration="+(System.currentTimeMillis() - start2));
		
		final long	start3 = System.currentTimeMillis();
		try(final InputStream		is = new FileInputStream("C:/tmp/dict.opcorpora.bin.gz");
			final InputStream		gzis = new GZIPInputStream(is);
			final InputStream		bis = new BufferedInputStream(gzis);
			final DataInputStream	dis = new DataInputStream(bis)) {
			
			final DictionaryManager	dm2 = new DictionaryManager(dis);
			
			dm2.nextWord("дома ".toCharArray(), 0, (w)->System.err.println("W: "+w.getCurrentFormAsString()+", init="+w.getInitialFormAsString()));
		}
		System.err.println("Restore: duration="+(System.currentTimeMillis() - start3));
		
	}
}
