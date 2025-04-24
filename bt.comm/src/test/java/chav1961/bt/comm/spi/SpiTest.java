package chav1961.bt.comm.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;

public class SpiTest {

	@Test
	public void enumTest() throws IOException {
		final URL	url = URI.create("comm://enumerate").toURL();
		final URLConnection	conn = url.openConnection();
		
		conn.connect();
		try(final InputStream		is = conn.getInputStream();
			final Reader			rdr = new InputStreamReader(is);
			final BufferedReader	brdr = new BufferedReader(rdr)) {
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				System.err.println("Line="+line);
			}
		}

		try(final InputStream		is = url.openStream();
			final Reader			rdr = new InputStreamReader(is);
			final BufferedReader	brdr = new BufferedReader(rdr)) {
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				System.err.println("Line="+line);
			}
		}
	}

	@Test
	public void outputTest() throws IOException {
		final URL	url = URI.create("comm://COM1").toURL();
		
		try(final CommURLConnection	conn = (CommURLConnection) url.openConnection()) {
			conn.connect();
			try(final OutputStream	os = conn.getOutputStream()) {
				os.write("test".getBytes());
				os.flush();
			}
		}
		
	}
	
}
