package chav1961.bt.paint.control;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;

public class ImageUtils {
	public static enum ProcessType {
		CROP, RESIZE, SCALE, ROTATE_CLOCKWISE, ROTATE_COUNTERCLOCKWISE, MIRROR_HORIZONTAL, MIRROR_VERTICAL, TO_GRAYSCALE, TO_TRANSPARENT 
	}
	
	public static Image process(final ProcessType type, final Image source, final Object... parameters) {
		if (type == null) {
			throw new NullPointerException("Process type can't be null");
		}
		else if (!(source instanceof BufferedImage)) {
			throw new NullPointerException("Image to process can't be null and must be BufferedImage instance");
		}
		else {
			switch (type) {
				case CROP				:
					if (parameters.length == 1 && (parameters[0] instanceof Rectangle)) {
						return cropImage((BufferedImage)source, (Rectangle)parameters[0]);
					}
					else {
						throw new IllegalArgumentException("[CROP] mode must have rectangle item in the parameters list"); 
					}
				case MIRROR_HORIZONTAL	:
					if (parameters.length == 0) {
						return mirrorImage((BufferedImage)source, false);
					}
					else {
						throw new IllegalArgumentException("[MIRROR_HORIZONTAL] mode must not have any content in the parameters list"); 
					}
				case MIRROR_VERTICAL	:
					if (parameters.length == 0) {
						return mirrorImage((BufferedImage)source, true);
					}
					else {
						throw new IllegalArgumentException("[MIRROR_VERTICAL] mode must not have any content in the parameters list"); 
					}
				case RESIZE				:
					if (parameters.length == 2 && (parameters[0] instanceof Number) && (parameters[1] instanceof Number)) {
						return resizeImage((BufferedImage)source, ((Number)parameters[0]).intValue(), ((Number)parameters[1]).intValue(), false);
					}
					else {
						throw new IllegalArgumentException("[RESIZE] mode must have width anf height items in the parameters list"); 
					}
				case ROTATE_CLOCKWISE	:
					if (parameters.length == 0) {
						return rotateImage((BufferedImage)source, -90);
					}
					else {
						throw new IllegalArgumentException("[ROTATE_CLOCKWISE] mode must not have any content in the parameters list"); 
					}
				case ROTATE_COUNTERCLOCKWISE:
					if (parameters.length == 0) {
						return rotateImage((BufferedImage)source, 90);
					}
					else {
						throw new IllegalArgumentException("[ROTATE_COUNTERCLOCKWISE] mode must not have any content in the parameters list"); 
					}
				case SCALE				:
					if (parameters.length == 2 && (parameters[0] instanceof Number) && (parameters[1] instanceof Number)) {
						return resizeImage((BufferedImage)source, ((Number)parameters[0]).intValue(), ((Number)parameters[1]).intValue(), true);
					}
					else {
						throw new IllegalArgumentException("[RESIZE] mode must have width anf height items in the parameters list"); 
					}
				case TO_GRAYSCALE		:
					if (parameters.length == 0) {
						return grayScaleImage((BufferedImage)source);
					}
					else {
						throw new IllegalArgumentException("[TO_GRAYSCALE] mode must not have any content in the parameters list"); 
					}
				case TO_TRANSPARENT		:
					if (parameters.length == 1 && (parameters[0] instanceof Color)) {
						return transparentImage((BufferedImage)source, (Color)parameters[0]);
					}
					else {
						throw new IllegalArgumentException("[TO_TRANSPARENT] mode must have transparent color intem in the parameters list"); 
					}
				default	:
					throw new UnsupportedOperationException("Process type ["+type+"] is not supported yet"); 
			}
		}
	}

	private static Image cropImage(final BufferedImage source, final Rectangle rectangle) {
		// TODO Auto-generated method stub
		final BufferedImage		result = new BufferedImage(rectangle.width, rectangle.height, source.getType());
		final Graphics2D		g2d = (Graphics2D) result.getGraphics();
		
		g2d.drawImage(source, 0, 0, rectangle.width, rectangle.height, rectangle.x, rectangle.y, rectangle.width, rectangle.height, null);
		return result;
	}
	
	private static Image mirrorImage(final BufferedImage source, final boolean verticalMirror) {
		// TODO Auto-generated method stub
		final BufferedImage		result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		final Graphics2D		g2d = (Graphics2D) result.getGraphics();
		final AffineTransform	at = new AffineTransform();
		
		if (verticalMirror) {
			at.scale(1, -1);
		}
		else {
			at.scale(-1, 1);
		}
		g2d.drawImage(source, at, null);
		
		return result;
	}

	private static Image resizeImage(final BufferedImage source, final int newWidth, final int newHeight, final boolean fill) {
		// TODO Auto-generated method stub
		final BufferedImage		result = new BufferedImage(newWidth, newHeight, source.getType());
		final Graphics2D		g2d = (Graphics2D) result.getGraphics();
		
		if (fill) {
			g2d.drawImage(source, 0, 0, null);
		}
		else {
			g2d.drawImage(source, 0, 0, null);
		}
		return result;
	}

	private static Image rotateImage(final BufferedImage source, final int angle) {
		// TODO Auto-generated method stub
		final BufferedImage		result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		final Graphics2D		g2d = (Graphics2D) result.getGraphics();
		final AffineTransform	at = new AffineTransform();
		
		at.rotate(Math.PI * angle / 180);
		g2d.drawImage(source, at, null);
		
		return result;
	}

	private static Image grayScaleImage(final BufferedImage source) {
		final ImageFilter 	filter = new RGBImageFilter() {
						          public int filterRGB(final int x, final int y, final int rgb) {
						        	  // 0.3 * R + 0.59 * G + 0.11 * B
						        	  final int		val = ((int) (0.3 * ((rgb & 0x00FF000000) >> 16) + 0.59 * ((rgb & 0x00FF000000) >> 8) + 0.11 * ((rgb & 0x00FF000000) >> 0))) & 0xFF; 
						        	  
						        	  return (rgb & 0xFF000000) | val * (65536 + 256 + 1);
						          }
						       };
		return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(source.getSource(), filter));		
	}

	private static Image transparentImage(final BufferedImage source, final Color transparentColor) {
		final int			rgbColor = transparentColor.getRGB();
		final ImageFilter 	filter = new RGBImageFilter() {
						          public int filterRGB(final int x, final int y, final int rgb) {
						              if ((rgb | 0xFF000000) == rgbColor) {
						                  	return 0x00FFFFFF & rgb;
						               } else {
						            	   return rgb;
						               }
						          }
						       };
	   return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(source.getSource(), filter));		
	}
}
