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
		Assert.assertTrue(compareImageContent((BufferedImage) ImageUtils.cropImage(bi, rect, null), Color.WHITE));
	}

	@Test
	public void mirrorImageTest() {	
		// Fill 100x100 with BLACK, fill 50x50 at top/left with WHITE, mirror content horizontally/vertically and test mirrored content is WHITE
		final Rectangle		rect = new Rectangle(0,0,50,50); 
		final BufferedImage	biX = createImage(100,100,Color.BLACK);
		final Rectangle		rectX = new Rectangle(50,0,50,50); 
		
		fill(biX, rect, Color.WHITE);
		Assert.assertTrue(compareImageContent((BufferedImage) cutImage((BufferedImage)ImageUtils.mirrorImage(biX, false, null), rectX), Color.WHITE));

		final BufferedImage	biY = createImage(100,100,Color.BLACK);
		final Rectangle		rectY = new Rectangle(0,50,50,50); 
		
		fill(biY, rect, Color.WHITE);
		Assert.assertTrue(compareImageContent((BufferedImage) cutImage((BufferedImage)ImageUtils.mirrorImage(biY, true, null), rectY), Color.WHITE));
	}

	@Test
	public void resizeImageTest() {	
		// Fill 100x100 with WHITE, resize content with BLACK and test it's new size and left piece is WHITE
		// Fill 100x100 with WHITE, resize content with center and BLACK and test it's new size and center piece is WHITE
		// Fill 100x100 with WHITE, resize content with fill and test it's new size and all the image is WHITE
		final BufferedImage	bi = createImage(100,100,Color.WHITE);
		
		final BufferedImage	bi1 = (BufferedImage) ImageUtils.resizeImage(bi, 200, 300, Color.BLACK, false, false, null);
		
		Assert.assertEquals(200, bi1.getWidth());
		Assert.assertEquals(300, bi1.getHeight());
		Assert.assertTrue(compareImageContent((BufferedImage) cutImage(bi1, new Rectangle(0, 0, 100, 100)), Color.WHITE));

		final BufferedImage	bi2 = (BufferedImage) ImageUtils.resizeImage(bi, 200, 300, Color.BLACK, false, true, null);
		
		Assert.assertEquals(200, bi2.getWidth());
		Assert.assertEquals(300, bi2.getHeight());
		Assert.assertTrue(compareImageContent((BufferedImage) cutImage(bi2, new Rectangle(50, 100, 100, 100)), Color.WHITE));

		final BufferedImage	bi3 = (BufferedImage) ImageUtils.resizeImage(bi, 200, 300, Color.BLACK, true, false, null);
		
		Assert.assertEquals(200, bi3.getWidth());
		Assert.assertEquals(300, bi3.getHeight());
		Assert.assertTrue(compareImageContent((BufferedImage) cutImage(bi3, new Rectangle(0, 0, 200, 300)), Color.WHITE));
	}

	@Test
	public void rotateImageTest() {	
		// Fill 100x50 with BLACK, fill 50x50 at left with WHITE, rotate content, test it's new size and bottom piece is WHITE
		final Rectangle		rect = new Rectangle(50,0,50,25); 

		final BufferedImage	biC = createImage(100,25,Color.BLACK);
		final Rectangle		rectRC = new Rectangle(0,50,25,50); 
		
		fill(biC, rect, Color.WHITE);
		
		final BufferedImage	resultC = (BufferedImage) ImageUtils.rotateImage(biC, false, null);
		
		Assert.assertEquals(25, resultC.getWidth());
		Assert.assertEquals(100, resultC.getHeight());
		
		Assert.assertTrue(compareImageContent((BufferedImage) cutImage(resultC, rectRC), Color.WHITE));

		final BufferedImage	biCC = createImage(100,25,Color.BLACK);
		final Rectangle		rectRCC = new Rectangle(0,0,25,50); 
		
		fill(biCC, rect, Color.WHITE);
		
		final BufferedImage	resultCC = (BufferedImage) ImageUtils.rotateImage(biCC, true, null);
		
		Assert.assertEquals(25, resultCC.getWidth());
		Assert.assertEquals(100, resultCC.getHeight());
		
		Assert.assertTrue(compareImageContent((BufferedImage) cutImage(resultCC, rectRCC), Color.WHITE));
	}

	@Test
	public void grayScaleImageTest() {	
		// Fill 100x100 with GRAY, make grayscale and test it's content is GRAY
		// Fill 100x100 with GREEN, make grayscale and test it's content is #FF969696
		// Fill 100x100 with transparent GREEN, make grayscale and test it's content is 0
		final Rectangle		rect = new Rectangle(0, 0, 100, 100);
		
		Assert.assertTrue(compareImageContent((BufferedImage) cutImage((BufferedImage) ImageUtils.grayScaleImage(createImage(100, 100, Color.GRAY), null), rect), Color.GRAY));
		Assert.assertTrue(compareImageContent((BufferedImage) cutImage((BufferedImage) ImageUtils.grayScaleImage(createImage(100, 100, Color.GREEN), null), rect), new Color(0x969696)));
		Assert.assertTrue(compareImageContent((BufferedImage) cutImage((BufferedImage) ImageUtils.grayScaleImage(createImage(100, 100, new Color(0x0000FF00, true)), null), rect), new Color(0, true)));
	}

	@Test
	public void transparentImageTest() {	
		// Fill 100x50 with BLACK, fill left 50х50 with WHITE, make transparency without exclude WHITE and test left 50х50 is transparent WHITE
		// Fill 100x50 with BLACK, fill left 50х50 with WHITE, make transparency without exclude WHITE and test right 50х50 is transparent BLACK
		final Rectangle		rectWhite = new Rectangle(0, 0, 50, 50);
		final Rectangle		rectBlack = new Rectangle(50, 0, 50, 50);
		final BufferedImage	bi = createImage(100, 50, Color.BLACK);
		
		fill(bi, rectWhite, Color.WHITE);
		Assert.assertTrue(compareImageContent((BufferedImage) cutImage((BufferedImage) ImageUtils.transparentImage(bi, Color.WHITE, false, null), rectWhite), new Color(0x00, true)));
		Assert.assertTrue(compareImageContent((BufferedImage) cutImage((BufferedImage) ImageUtils.transparentImage(bi, Color.WHITE, true, null), rectBlack), new Color(0x00, true)));
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

		g2d.drawImage(image, 0, 0, rect.width, rect.height, rect.x, rect.y, rect.x+rect.width, rect.y+rect.height, null);
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
