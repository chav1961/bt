package chav1961.bt.paint;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;

public class ApplicationTest {
	@Test
	public void batchTest() throws IOException {
		final InputStream	in = System.in;
		final PrintStream	out = System.out;
		
		try {
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
				System.setIn(this.getClass().getResourceAsStream("source.png"));
				System.setOut(new PrintStream(baos));
				
				Assert.assertEquals(0,Application.callMain(new String[] {"rot cw"}));
				System.out.flush();
				
				final Image	img = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
				
				Assert.assertEquals(16, img.getWidth(null));
				Assert.assertEquals(48, img.getHeight(null));
			}
		} finally {
			System.setIn(in);
			System.setOut(out);
		}
	}
}
