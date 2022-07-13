package chav1961.bt.paint.control;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;

import javax.swing.JFrame;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.swing.AutoBuiltForm;

public class ImageUtils {
	public static enum ProcessType {
		CROP, RESIZE, SCALE, ROTATE_CLOCKWISE, ROTATE_COUNTERCLOCKWISE, MIRROR_HORIZONTAL, MIRROR_VERTICAL, TO_GRAYSCALE, TO_TRANSPARENT 
	}

	public static <T> boolean ask(final T instance, final Localizer localizer, final int width, final int height) throws ContentException {
		final ContentMetadataInterface	mdi = ContentModelFactory.forAnnotatedClass(instance.getClass());
		
		try(final AutoBuiltForm<T,?>	abf = new AutoBuiltForm<>(mdi, localizer, PureLibSettings.INTERNAL_LOADER, instance, (FormManager<?,T>)instance)) {
			
			((ModuleAccessor)instance).allowUnnamedModuleAccess(abf.getUnnamedModules());
			abf.setPreferredSize(new Dimension(width,height));
			return AutoBuiltForm.ask((JFrame)null,localizer,abf);
		}
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
					if (checkParameterTypes(parameters, Rectangle.class)) {
						return cropImage((BufferedImage)source, (Rectangle)parameters[0]);
					}
					else {
						throw new IllegalArgumentException("[CROP] mode must have rectangle item in the parameters list"); 
					}
				case MIRROR_HORIZONTAL	:
					if (checkParameterTypes(parameters)) {
						return mirrorImage((BufferedImage)source, false);
					}
					else {
						throw new IllegalArgumentException("[MIRROR_HORIZONTAL] mode must not have any content in the parameters list"); 
					}
				case MIRROR_VERTICAL	:
					if (checkParameterTypes(parameters)) {
						return mirrorImage((BufferedImage)source, true);
					}
					else {
						throw new IllegalArgumentException("[MIRROR_VERTICAL] mode must not have any content in the parameters list"); 
					}
				case RESIZE				:
					if (checkParameterTypes(parameters, Number.class, Number.class)) {
						return resizeImage((BufferedImage)source, ((Number)parameters[0]).intValue(), ((Number)parameters[1]).intValue(), false);
					}
					else {
						throw new IllegalArgumentException("[RESIZE] mode must have width anf height items in the parameters list"); 
					}
				case ROTATE_CLOCKWISE	:
					if (checkParameterTypes(parameters)) {
						return rotateImage((BufferedImage)source, -90);
					}
					else {
						throw new IllegalArgumentException("[ROTATE_CLOCKWISE] mode must not have any content in the parameters list"); 
					}
				case ROTATE_COUNTERCLOCKWISE:
					if (checkParameterTypes(parameters)) {
						return rotateImage((BufferedImage)source, 90);
					}
					else {
						throw new IllegalArgumentException("[ROTATE_COUNTERCLOCKWISE] mode must not have any content in the parameters list"); 
					}
				case SCALE				:
					if (checkParameterTypes(parameters, Number.class, Number.class)) {
						return resizeImage((BufferedImage)source, ((Number)parameters[0]).intValue(), ((Number)parameters[1]).intValue(), true);
					}
					else {
						throw new IllegalArgumentException("[RESIZE] mode must have width anf height items in the parameters list"); 
					}
				case TO_GRAYSCALE		:
					if (checkParameterTypes(parameters)) {
						return grayScaleImage((BufferedImage)source);
					}
					else {
						throw new IllegalArgumentException("[TO_GRAYSCALE] mode must not have any content in the parameters list"); 
					}
				case TO_TRANSPARENT		:
					if (checkParameterTypes(parameters, Color.class)) {
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

	static Image cropImage(final BufferedImage source, final Rectangle rectangle) {
		final BufferedImage		result = new BufferedImage(rectangle.width, rectangle.height, source.getType());
		final Graphics2D		g2d = (Graphics2D) result.getGraphics();
		final Rectangle			rect = new Rectangle(rectangle);
		
		rect.intersects(0, 0, source.getWidth(), source.getHeight());
		
		g2d.drawImage(source, 0, 0, rect.width, rect.height, rect.x, rect.y, rect.width, rect.height, null);
		return result;
	}
	
	static Image mirrorImage(final BufferedImage source, final boolean verticalMirror) {
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
}
