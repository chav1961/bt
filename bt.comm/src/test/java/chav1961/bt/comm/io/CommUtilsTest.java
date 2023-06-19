package chav1961.bt.comm.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.PureLibSettings;

public class CommUtilsTest {
	// https://networkupstools.org/protocols/apcsmart.html
	@Test
	public void basicTest() throws IOException, InterruptedException {
//		for (String item : CommUtils.commPortsAvailable()) {
//			System.err.println(CommUtils.getCommPortProperties(item));
//		}
//		try(final CommPort 			port = new CommPort(URI.create("COM3:/?baudRate=38400&dataBits=7&parity=none"));
//			final InputStream		is = port.getInputStream();
//			final Reader			rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING);
//			final BufferedReader	brdr = new BufferedReader(rdr);
//			final OutputStream		os = port.getOutputStream();
//			final PrintWriter		pwr = new PrintWriter(os)) {
//			
//			pwr.println("::version");
//			pwr.flush();
//			System.err.println(brdr.readLine());
//		}
		try(final RandomAccessFile	raf = new RandomAccessFile("COM3:", "rw")) {
			raf.write("::version\n".getBytes());
			
			final byte[]	content = new byte[1024];
			
			Thread.sleep(5000);
			
			int len = raf.read(content);
			System.err.println("Read: "+new String(content, 0, len));
		}
	}
}
