package chav1961.bt.paint.control;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.junit.Assert;
import org.junit.Test;

public class ImageUtilsTest {
	@Test
	public void cropImageTest() {	
		// Fill 100x100 with BLACK, fill 50x50 at center with WHITE, crop center and test it's content is WHITE
		final BufferedImage	bi = createImage(100,100,Color.BLACK);
		final Rectangle		rect = new Rectangle(25,25,50,50); 
		
		fill(bi, rect, Color.WHITE);
		Assert.assertTrue(compareImageContent((BufferedImage) ImageUtils.cropImage(bi, rect), Color.WHITE));
	}

	@Test
	public void mirrorImageTest() {	
	}

	@Test
	public void resizeImageTest() {	
	}

	@Test
	public void rotateImageTest() {	
	}

	@Test
	public void grayScaleImageTest() {	
	}

	@Test
	public void transparentImageTest() {	
	}

	@Test
	public void conversionImageTest() {	
	}
	
	private BufferedImage createImage(final int width, final int height, final Color filled) {
		final BufferedImage	result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D	g2d = (Graphics2D) result.getGraphics();
		
		g2d.setColor(filled);
		g2d.fillRect(0, 0, width, height);
		return result;
	}

	private void fill(final BufferedImage image, final Rectangle rect,  final Color filled) {
		final Graphics2D	g2d = (Graphics2D) image.getGraphics();
		
		g2d.setColor(filled);
		g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
	}
	
	
	private BufferedImage cutImage(final BufferedImage image, final Rectangle rect) {
		final BufferedImage	result = new BufferedImage(rect.width, rect.height, image.getType());
		final Graphics2D	g2d = (Graphics2D) result.getGraphics();

		g2d.drawImage(image, 0, 0, rect.width, rect.height, rect.x, rect.y, rect.width, rect.height, null);
		return result;
	}
	
	private boolean compareImageContent(final BufferedImage image, final Color color) {
		final int[]		content = new int[image.getWidth()*image.getHeight()];
		final int		colorValue = color.getRGB();
		
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), content, 0, image.getWidth());
		for (int item : content) {
			if (item != colorValue) {
				return false;
			}
		}
		return true;
	}
}
