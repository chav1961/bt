package chav1961.bt.paint.control;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.RGBImageFilter;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

public class ImageUtils {
	public static enum ProcessType {
		CROP, RESIZE, SCALE, ROTATE_CLOCKWISE, ROTATE_COUNTERCLOCKWISE, MIRROR_HORIZONTAL, MIRROR_VERTICAL, TO_GRAYSCALE, TO_TRANSPARENT 
	}

	public static Image process(final ProcessType type, final Image source, final ImageObserver observer, final Object... parameters) {
		if (type == null) {
			throw new NullPointerException("Process type can't be null");
		}
		else if (!(source instanceof BufferedImage)) {
			throw new NullPointerException("Image to process can't be null and must be BufferedImage instance");
		}
		else {
			switch (type) {
				case CROP				:
					if (checkParameterTypes(parameters, Rectangle.class)) {
						return cropImage((BufferedImage)source, (Rectangle)parameters[0], observer);
					}
					else {
						throw new IllegalArgumentException("[CROP] mode must have rectangle item in the parameters list"); 
					}
				case MIRROR_HORIZONTAL	:
					if (checkParameterTypes(parameters)) {
						return mirrorImage((BufferedImage)source, false, observer);
					}
					else {
						throw new IllegalArgumentException("[MIRROR_HORIZONTAL] mode must not have any content in the parameters list"); 
					}
				case MIRROR_VERTICAL	:
					if (checkParameterTypes(parameters)) {
						return mirrorImage((BufferedImage)source, true, observer);
					}
					else {
						throw new IllegalArgumentException("[MIRROR_VERTICAL] mode must not have any content in the parameters list"); 
					}
				case RESIZE				:
					if (checkParameterTypes(parameters, Number.class, Number.class, Color.class, Boolean.class)) {
						return resizeImage((BufferedImage)source, ((Number)parameters[0]).intValue(), ((Number)parameters[1]).intValue(), ((Color)parameters[2]), false, ((Boolean)parameters[3]), observer);
					}
					else {
						throw new IllegalArgumentException("[RESIZE] mode must have width anf height items in the parameters list"); 
					}
				case ROTATE_CLOCKWISE	:
					if (checkParameterTypes(parameters)) {
						return rotateImage((BufferedImage)source, false, observer);
					}
					else {
						throw new IllegalArgumentException("[ROTATE_CLOCKWISE] mode must not have any content in the parameters list"); 
					}
				case ROTATE_COUNTERCLOCKWISE:
					if (checkParameterTypes(parameters)) {
						return rotateImage((BufferedImage)source, true, observer);
					}
					else {
						throw new IllegalArgumentException("[ROTATE_COUNTERCLOCKWISE] mode must not have any content in the parameters list"); 
					}
				case SCALE				:
					if (checkParameterTypes(parameters, Number.class, Number.class)) {
						return resizeImage((BufferedImage)source, ((Number)parameters[0]).intValue(), ((Number)parameters[1]).intValue(), Color.BLACK, true, true, observer);
					}
					else {
						throw new IllegalArgumentException("[RESIZE] mode must have width anf height items in the parameters list"); 
					}
				case TO_GRAYSCALE		:
					if (checkParameterTypes(parameters)) {
						return grayScaleImage((BufferedImage)source,observer);
					}
					else {
						throw new IllegalArgumentException("[TO_GRAYSCALE] mode must not have any content in the parameters list"); 
					}
				case TO_TRANSPARENT		:
					if (checkParameterTypes(parameters, Color.class, Boolean.class)) {
						return transparentImage((BufferedImage)source, (Color)parameters[0], (Boolean)parameters[1], observer);
					}
					else {
						throw new IllegalArgumentException("[TO_TRANSPARENT] mode must have transparent color intem in the parameters list"); 
					}
				default	:
					throw new UnsupportedOperationException("Process type ["+type+"] is not supported yet"); 
			}
		}
	}

	static Image cropImage(final BufferedImage source, final Rectangle rectangle, final ImageObserver observer) {
		final BufferedImage		result = new BufferedImage(rectangle.width, rectangle.height, source.getType());
		final Graphics2D		g2d = (Graphics2D) result.getGraphics();
		final Rectangle			rect = new Rectangle(rectangle);
		
		rect.intersects(0, 0, source.getWidth(), source.getHeight());
		
		g2d.drawImage(source, 0, 0, rect.width, rect.height, rect.x, rect.y, rect.width, rect.height, observer);
		return result;
	}
	
	static Image mirrorImage(final BufferedImage source, final boolean verticalMirror, final ImageObserver observer) {
		final BufferedImage		result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		final Graphics2D		g2d = (Graphics2D) result.getGraphics();
		final AffineTransform	at = new AffineTransform();
		
		if (verticalMirror) {
			at.translate(0, source.getHeight());
			at.scale(1, -1);
		}
		else {
			at.translate(source.getWidth(), 0);
			at.scale(-1, 1);
		}
		g2d.drawImage(source, at, observer);
		
		return result;
	}

	static Image resizeImage(final BufferedImage source, final int newWidth, final int newHeight, final Color background, final boolean fill, final boolean center, final ImageObserver observer) {
		final BufferedImage		result = new BufferedImage(newWidth, newHeight, source.getType());
		final Graphics2D		g2d = (Graphics2D) result.getGraphics();
		
		if (fill) {
			g2d.drawImage(source, 0, 0, newWidth, newHeight, 0, 0, source.getWidth(), source.getHeight(), observer);
		}
		else {
			g2d.setColor(background);
			g2d.fillRect(0, 0, newWidth, newHeight);
			
			if (center) {
				g2d.drawImage(source, (newWidth - source.getWidth())/2, (newHeight - source.getHeight())/2, observer);
			}
			else {
				g2d.drawImage(source, 0, 0, observer);
			}
		}
		return result;
	}

	static Image rotateImage(final BufferedImage source, final boolean counterClockwise,final ImageObserver observer) {
		final BufferedImage		result = new BufferedImage(source.getHeight(), source.getWidth(), source.getType());
		final Graphics2D		g2d = (Graphics2D) result.getGraphics();
		final AffineTransform	at = new AffineTransform();
		
		if (counterClockwise) {
			at.quadrantRotate(-1, 0, 0);
			at.translate(-source.getWidth(), 0);
		}
		else {
			at.quadrantRotate(1, 0, 0);
			at.translate(0, -source.getHeight());
		}
//		printTransformation(at, new Rectangle(0, 0, source.getWidth(), source.getHeight()));
		g2d.drawImage(source, at, null);
		
		return result;
	}

	static Image grayScaleImage(final BufferedImage source, final ImageObserver observer) {
		final BufferedImage	result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		final ImageFilter 	filter = new RGBImageFilter() {
										private static final int	MULTIPLIER = (65536 + 256 + 1);
									
										public int filterRGB(final int x, final int y, final int rgb) {
							        	  // color = 0.3 * R + 0.59 * G + 0.11 * B
							        	  final int		val = (int) (Math.round((0.3 * ((rgb & 0xFF0000) >> 16) + 0.59 * ((rgb & 0xFF00) >> 8) + 0.11 * ((rgb & 0xFF) >> 0))) & 0xFF); 
							        	  
							        	  return (rgb & 0xFF000000) | val * MULTIPLIER;
										}
							       };
		final Graphics2D	g2d = (Graphics2D) result.getGraphics();
		
		g2d.drawImage(Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(source.getSource(), filter)), 0, 0, observer);
		return result;		
	}

	static Image transparentImage(final BufferedImage source, final Color transparentColor, final boolean except, final ImageObserver observer) {
		final BufferedImage	result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		final int			rgbColor = transparentColor.getRGB() & 0xFFFFFF;
		final ImageFilter 	filter = new RGBImageFilter() {
											public int filterRGB(final int x, final int y, final int rgb) {
												return ((rgb & 0xFFFFFF) == rgbColor) != except ? 0 : rgb; 
											}
								       };
		final Graphics2D	g2d = (Graphics2D) result.getGraphics();
		
		g2d.drawImage(Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(source.getSource(), filter)), 0, 0, observer);
		return result;		
	}
	
	private static boolean checkParameterTypes(final Object[] parameters, final Class<?>... types) {
		if (parameters.length < types.length) {
			return false;
		}
		else {
			for(int index = 0; index < types.length; index++) {
				if (!types[index].isInstance(parameters[index])) {
					return false;
				}
			}
			return true;
		}
	}
	
	private static void printTransformation(final AffineTransform at, final Rectangle rect) {
		final Point2D.Float[]	src = new Point2D.Float[] {new Point2D.Float(rect.x,  rect.y), new Point2D.Float(rect.x+rect.width, rect.y+rect.height)};
		final Point2D.Float[]	dst = new Point2D.Float[2];
		
		at.transform(src, 0, dst, 0,src.length);
		System.err.println("Transform "+src[0]+"->"+dst[0]+", "+src[1]+"->"+dst[1]);
	}
}
