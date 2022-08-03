package chav1961.bt.paint.control;



import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.RGBImageFilter;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.LinkedList;
import java.util.List;

import chav1961.bt.paint.script.interfaces.StrokeWrapper.LineCaps;
import chav1961.bt.paint.script.interfaces.StrokeWrapper.LineJoin;
import chav1961.bt.paint.script.interfaces.StrokeWrapper.LineStroke;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.ui.ColorPair;

public class ImageUtils {
	public static enum ProcessType {
		FILL, CROP, RESIZE, SCALE, ROTATE_CLOCKWISE, ROTATE_COUNTERCLOCKWISE, MIRROR_HORIZONTAL, MIRROR_VERTICAL, TO_GRAYSCALE, TO_TRANSPARENT, INSERT 
	}

	public static enum DrawingType {
		UNKNOWN, SELECT, PEN, BRUSH, TEXT, LINE, ELLIPSE, RECT, FILL 
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
				case FILL				:
					if (checkParameterTypes(parameters, Rectangle.class, Color.class)) {
						return fillImage((BufferedImage)source, (Rectangle)parameters[0], (Color)parameters[1], observer);
					}
					else {
						throw new IllegalArgumentException("[FILL] mode must have rectangle item and color in the parameters list"); 
					}
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
						throw new IllegalArgumentException("[RESIZE] mode must have width and height items in the parameters list"); 
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
				case INSERT				:
					if (checkParameterTypes(parameters, Rectangle.class, BufferedImage.class)) {
						return insertImage((BufferedImage)source, (Rectangle)parameters[0], (BufferedImage)parameters[1], observer);
					}
					else {
						throw new IllegalArgumentException("[INSERT] mode must have rectangle and image in the parameters list"); 
					}
				default	:
					throw new UnsupportedOperationException("Process type ["+type+"] is not supported yet"); 
			}
		}
	}
	
	public static void draw(final DrawingType type, final Image source, final ImageObserver observer, final Object... parameters) {
		if (type == null) {
			throw new NullPointerException("Process type can't be null");
		}
		else if (!(source instanceof BufferedImage)) {
			throw new NullPointerException("Image to process can't be null and must be BufferedImage instance");
		}
		else {
			switch (type) {
				case BRUSH		:
					if (checkParameterTypes(parameters, Rectangle.class, Color.class)) {
						brushDraw((BufferedImage)source, (Rectangle)parameters[0], (Color)parameters[1], observer);
					}
					else if (checkParameterTypes(parameters, Rectangle.class, ColorPair.class, Boolean.class)) {
						brushDraw((BufferedImage)source, (Rectangle)parameters[0], (ColorPair)parameters[1], (Boolean)parameters[2], observer);
					}
					else {
						throw new IllegalArgumentException("[BRUSH] mode must not have any content in the parameters list"); 
					}
					break;
				case ELLIPSE	:
					if (checkParameterTypes(parameters, Rectangle.class, Color.class, Stroke.class)) {
						ellipseDraw((BufferedImage)source, (Rectangle)parameters[0], (Color)parameters[1], (Stroke)parameters[2], observer);
					}
					else if (checkParameterTypes(parameters, Rectangle.class, ColorPair.class, Stroke.class)) {
						ellipseDraw((BufferedImage)source, (Rectangle)parameters[0], (ColorPair)parameters[1], (Stroke)parameters[2], observer);
					}
					else {
						throw new IllegalArgumentException("[ELLIPSE] mode must not have any content in the parameters list"); 
					}
					break;
				case FILL		:
					if (checkParameterTypes(parameters, Point.class, Color.class, Color.class)) {
						fillDraw((BufferedImage)source, (Point)parameters[0], (Color)parameters[1], (Color)parameters[2], observer);
					}
					else {
						throw new IllegalArgumentException("[FILL] mode must not have any content in the parameters list"); 
					}
					break;
				case LINE		:
					if (checkParameterTypes(parameters, Point.class, Point.class, Color.class, Stroke.class)) {
						lineDraw((BufferedImage)source, (Point)parameters[0], (Point)parameters[1], (Color)parameters[2], (Stroke)parameters[3], observer);
					}
					else {
						throw new IllegalArgumentException("[LINE] mode must not have any content in the parameters list"); 
					}
					break;
				case PEN		:
					if (checkParameterTypes(parameters, GeneralPath.class, Color.class, Stroke.class)) {
						pathDraw((BufferedImage)source, (GeneralPath)parameters[0], (Color)parameters[1], (Stroke)parameters[2], observer);
					}
					else if (checkParameterTypes(parameters, GeneralPath.class, ColorPair.class, Stroke.class)) {
						pathDraw((BufferedImage)source, (GeneralPath)parameters[0], (ColorPair)parameters[1], (Stroke)parameters[2], observer);
					}
					else {
						throw new IllegalArgumentException("[PEN] mode must not have any content in the parameters list"); 
					}
					break;
				case RECT		:
					if (checkParameterTypes(parameters, Rectangle.class, Color.class, Stroke.class)) {
						rectDraw((BufferedImage)source, (Rectangle)parameters[0], (Color)parameters[1], (Stroke)parameters[2], observer);
					}
					else if (checkParameterTypes(parameters, Rectangle.class, ColorPair.class, Stroke.class)) {
						rectDraw((BufferedImage)source, (Rectangle)parameters[0], (ColorPair)parameters[1], (Stroke)parameters[2], observer);
					}
					else {
						throw new IllegalArgumentException("[RECT] mode must not have any content in the parameters list"); 
					}
					break;
				case TEXT		:
					if (checkParameterTypes(parameters, String.class, Rectangle.class, Color.class, Font.class)) {
						textDraw((BufferedImage)source, (String)parameters[0], (Rectangle)parameters[1], (Color)parameters[2], (Font)parameters[3], observer);
					}
					else if (checkParameterTypes(parameters, String.class, Rectangle.class, ColorPair.class, Font.class)) {
						textDraw((BufferedImage)source, (String)parameters[0], (Rectangle)parameters[1], (ColorPair)parameters[2], (Font)parameters[3], observer);
					}
					else {
						throw new IllegalArgumentException("[TEXT] mode must not have any content in the parameters list"); 
					}
					break;
				case UNKNOWN	:
					break;
				default:
					throw new UnsupportedOperationException("Drawing type ["+type+"] is not supported yet"); 
			}
		}
	}	
	
	public static Stroke buildStroke(final int lineThickness, final LineStroke lineStroke, final LineCaps caps, final LineJoin join) {
		switch (lineStroke) {
			case DASHED	:
				return new BasicStroke(lineThickness, caps.getCapsType(), join.getJoinType(), lineThickness, new float[] {3 * lineThickness}, 0);
			case DOTTED	:
				return new BasicStroke(lineThickness, caps.getCapsType(), join.getJoinType(), lineThickness, new float[] {lineThickness}, 0);
			case SOLID	: 
				return new BasicStroke(lineThickness); 		
			default:
				throw new UnsupportedOperationException("LineStroke style ["+lineStroke+"] is not supported yet");
		}
	}

	// [N] {solid|dashed|dotted} [{butt|round|square} [{miter|round|bevel}]]
	public static Stroke buildStroke(final String stroke) throws SyntaxException {
		if (stroke == null || stroke.isEmpty()) {
			throw new IllegalArgumentException("Stroke string can't be null or  empty");
		}
		else {
			final char[]		content = CharUtils.terminateAndConvert2CharArray(stroke, '\n');
			final int[]			forBounds = new int[2];
			final LineStroke	lineStroke;
			final LineCaps		lineCaps;
			final LineJoin		lineJoin;
			int					from = CharUtils.skipBlank(content, 0, true), thickness = 1;
			
			if (Character.isDigit(content[from])) {
				from = CharUtils.skipBlank(content, CharUtils.parseInt(content, from, forBounds, true), true);
				thickness = forBounds[0];
			}
			if (Character.isLetter(content[from])) {
				from = CharUtils.skipBlank(content, CharUtils.parseName(content, from, forBounds), true);
				
				final String	strokeName = new String(content, forBounds[0], forBounds[1]-forBounds[0]); 
						
				try {lineStroke = LineStroke.valueOf(strokeName);
				} catch (IllegalArgumentException exc) {
					throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Illegal stroke type");
				}
			}
			else {
				throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Missing stroke type");
			}
			if (Character.isLetter(content[from])) {
				from = CharUtils.skipBlank(content, CharUtils.parseName(content, from, forBounds), true);
				
				final String	capsName = new String(content, forBounds[0], forBounds[1]-forBounds[0]);
				
				try {lineCaps = LineCaps.valueOf(capsName);
				} catch (IllegalArgumentException exc) {
					throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Illegal stroke caps");
				}
				
				if (Character.isLetter(content[from])) {
					from = CharUtils.skipBlank(content, CharUtils.parseName(content, from, forBounds), true);
					
					final String	joinName = new String(content, forBounds[0], forBounds[1]-forBounds[0]);
					
					try {lineJoin = LineJoin.valueOf(joinName);
					} catch (IllegalArgumentException exc) {
						throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Illegal stroke join");
					}
				}
				else {
					lineJoin = LineJoin.BEVEL;
				}
			}
			else {
				lineCaps = LineCaps.BUTT;
				lineJoin = LineJoin.BEVEL;
			}
			return buildStroke(thickness, lineStroke, lineCaps, lineJoin);
		}
	}
	
	static Image fillImage(final BufferedImage source, final Rectangle rectangle, final Color color, final ImageObserver observer) {
		final BufferedImage	result = new BufferedImage(rectangle.width, rectangle.height, source.getType());
		final Graphics2D	g2d = (Graphics2D) result.getGraphics();
		final Color			oldColor = g2d.getColor(); 
		
		g2d.setColor(color);
		g2d.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
		g2d.setColor(oldColor);
		g2d.dispose();
		return result;
	}
	
	static Image cropImage(final BufferedImage source, final Rectangle rectangle, final ImageObserver observer) {
		final BufferedImage		result = new BufferedImage(rectangle.width, rectangle.height, source.getType());
		final Graphics2D		g2d = (Graphics2D) result.getGraphics();
		final Rectangle			rect = new Rectangle(rectangle);
		
		rect.intersects(0, 0, source.getWidth(), source.getHeight());
		
		g2d.drawImage(source, 0, 0, rect.width, rect.height, rect.x, rect.y, rect.width, rect.height, observer);
		g2d.dispose();
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
		g2d.dispose();
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
		g2d.dispose();
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
		g2d.dispose();
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
		g2d.dispose();
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
		g2d.dispose();
		return result;		
	}
	
	static Image insertImage(final BufferedImage source, final Rectangle rectangle, final BufferedImage insertion, final ImageObserver observer) {
		final Graphics2D		g2d = (Graphics2D) source.getGraphics();
		final Rectangle			rect = new Rectangle(rectangle);
		
		rect.intersects(0, 0, source.getWidth(), source.getHeight());
		g2d.drawImage(insertion, rect.x, rect.y, rect.x+rect.width, rect.y+rect.height, 0, 0, insertion.getWidth(), insertion.getHeight(), observer);
		g2d.dispose();
		return source;
	}
	
	static void rectDraw(final BufferedImage source, final Rectangle rect, final Color color, final Stroke stroke, final ImageObserver observer) {
		final Graphics2D	g2d = (Graphics2D) source.getGraphics();
		final Color			oldColor = g2d.getColor();
		final Stroke		oldStroke = g2d.getStroke();
		
		g2d.setColor(color);
		g2d.setStroke(stroke);
		g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
		g2d.setStroke(oldStroke);
		g2d.setColor(oldColor);
		g2d.dispose();
	}

	static void rectDraw(final BufferedImage source, final Rectangle rect, final ColorPair colorPair, final Stroke stroke, final ImageObserver observer) {
		final Graphics2D	g2d = (Graphics2D) source.getGraphics();
		final Color			oldColor = g2d.getColor();
		final Stroke		oldStroke = g2d.getStroke();
		
		g2d.setColor(colorPair.getBackground());
		g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
		g2d.setColor(colorPair.getForeground());
		g2d.setStroke(stroke);
		g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
		g2d.setStroke(oldStroke);
		g2d.setColor(oldColor);
		g2d.dispose();
	}
	
	static void ellipseDraw(final BufferedImage source, final Rectangle rect, final Color color, final Stroke stroke, final ImageObserver observer) {
		final Graphics2D	g2d = (Graphics2D) source.getGraphics();
		final Color			oldColor = g2d.getColor();
		final Stroke		oldStroke = g2d.getStroke();
		
		g2d.setColor(color);
		g2d.setStroke(stroke);
		g2d.drawOval(rect.x, rect.y, rect.width, rect.height);
		g2d.setStroke(oldStroke);
		g2d.setColor(oldColor);
		g2d.dispose();
	}
	
	static void ellipseDraw(final BufferedImage source, final Rectangle rect, final ColorPair colorPair, final Stroke stroke, final ImageObserver observer) {
		final Graphics2D	g2d = (Graphics2D) source.getGraphics();
		final Color			oldColor = g2d.getColor();
		final Stroke		oldStroke = g2d.getStroke();
		
		g2d.setColor(colorPair.getBackground());
		g2d.fillOval(rect.x, rect.y, rect.width, rect.height);
		g2d.setColor(colorPair.getForeground());
		g2d.setStroke(stroke);
		g2d.drawOval(rect.x, rect.y, rect.width, rect.height);
		g2d.setStroke(oldStroke);
		g2d.setColor(oldColor);
		g2d.dispose();
	}

	static void textDraw(final BufferedImage source, final String text, final Rectangle rect, final Color color, final Font font, final ImageObserver observer) {
		final Graphics2D		g2d = (Graphics2D) source.getGraphics();
		final Color				oldColor = g2d.getColor();
		final Font				oldFont = g2d.getFont();
		final Shape				oldClip = g2d.getClip();
		final AttributedString 	as = new AttributedString(text);
		
		float 	x = (float) rect.getX(), y = (float) rect.getY();
		int 	w = rect.width - 0 - 0;
		
		g2d.setColor(color);
		g2d.setFont(font);
		g2d.clip(rect);
		as.addAttribute(TextAttribute.FONT, font);
		   
		final AttributedCharacterIterator 	aci = as.getIterator();
		final FontRenderContext 			frc = g2d.getFontRenderContext();
		final LineBreakMeasurer 			lbm = new LineBreakMeasurer(aci, frc);
		
		while (lbm.getPosition() < aci.getEndIndex() && y < rect.y + rect.height) {
			final TextLayout 	tl = lbm.nextLayout(w);
			
			tl.draw(g2d, x, y + tl.getAscent());
			y += tl.getDescent() + tl.getLeading() + tl.getAscent();
		}
		g2d.setClip(oldClip);
		g2d.setFont(oldFont);
		g2d.setColor(oldColor);
		g2d.dispose();
	}
	
	static void textDraw(final BufferedImage source, final String text, final Rectangle rect, final ColorPair colorPair, final Font font, final ImageObserver observer) {
		final Graphics2D		g2d = (Graphics2D) source.getGraphics();
		final Color				oldColor = g2d.getColor();
		final Font				oldFont = g2d.getFont();
		final Shape				oldClip = g2d.getClip();
		final AttributedString 	as = new AttributedString(text);
		
		float 	x = (float) rect.getX(), y = (float) rect.getY();
		int 	w = rect.width - 0 - 0;
		
		g2d.setColor(colorPair.getBackground());
		g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
		g2d.setColor(colorPair.getForeground());
		g2d.setFont(font);
		g2d.clip(rect);
		as.addAttribute(TextAttribute.FONT, font);
		   
		final AttributedCharacterIterator 	aci = as.getIterator();
		final FontRenderContext 			frc = g2d.getFontRenderContext();
		final LineBreakMeasurer 			lbm = new LineBreakMeasurer(aci, frc);
		
		while (lbm.getPosition() < aci.getEndIndex() && y < rect.y + rect.height) {
			final TextLayout 	tl = lbm.nextLayout(w);
			
			tl.draw(g2d, x, y + tl.getAscent());
			y += tl.getDescent() + tl.getLeading() + tl.getAscent();
		}
		g2d.setClip(oldClip);
		g2d.setFont(oldFont);
		g2d.setColor(oldColor);
		g2d.dispose();
	}

	static void lineDraw(final BufferedImage source, final Point from, final Point to, final Color color, final Stroke stroke, final ImageObserver observer) {
		final Graphics2D	g2d = (Graphics2D) source.getGraphics();
		final Color			oldColor = g2d.getColor();
		final Stroke		oldStroke = g2d.getStroke();

		g2d.setColor(color);
		g2d.setStroke(stroke);
		g2d.drawLine(from.x, from.y, to.x, to.y);
		g2d.setStroke(oldStroke);
		g2d.setColor(oldColor);
		g2d.dispose();
	}

	static void pathDraw(final BufferedImage source, final GeneralPath generalPath, final Color color, final Stroke stroke, final ImageObserver observer) {
		final Graphics2D	g2d = (Graphics2D) source.getGraphics();
		final Color			oldColor = g2d.getColor();
		final Stroke		oldStroke = g2d.getStroke();

		g2d.setColor(color);
		g2d.setStroke(stroke);
        g2d.draw(generalPath);
		g2d.setStroke(oldStroke);
		g2d.setColor(oldColor);
		g2d.dispose();
	}

	static void pathDraw(final BufferedImage source, final GeneralPath generalPath, final ColorPair colorPair, final Stroke stroke, final ImageObserver observer) {
		final Graphics2D	g2d = (Graphics2D) source.getGraphics();
		final Color			oldColor = g2d.getColor();
		final Stroke		oldStroke = g2d.getStroke();

		g2d.setColor(colorPair.getBackground());
        g2d.fill(generalPath);
		g2d.setColor(colorPair.getForeground());
		g2d.setStroke(stroke);
        g2d.draw(generalPath);
		g2d.setStroke(oldStroke);
		g2d.setColor(oldColor);
		g2d.dispose();
	}

	static void fillDraw(final BufferedImage source, final Point start, final Color from, final Color to, final ImageObserver observer) {
		floodFill(source, start.x, start.y, from.getRGB(), to.getRGB());
	}

	static void brushDraw(final BufferedImage source, final Rectangle rectangle, final Color color, final ImageObserver observer) {
		final Graphics2D	g2d = (Graphics2D) source.getGraphics();
		final Color			oldColor = g2d.getColor(); 

		g2d.setColor(color);
		g2d.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
		g2d.setColor(oldColor);
		g2d.dispose();
	}

	static void brushDraw(final BufferedImage source, final Rectangle rect, final ColorPair colorPair, final boolean except, final ImageObserver observer) {
		final BufferedImage	content = (BufferedImage) cropImage(source, rect, observer);
		final int			rgbFrom = colorPair.getForeground().getRGB();
		final int			rgbTo = colorPair.getForeground().getRGB();
		final ImageFilter 	filter = new RGBImageFilter() {
											public int filterRGB(final int x, final int y, final int rgb) {
												return (rgb == rgbFrom) != except ? rgbTo : rgb; 
											}
								       };
		final Graphics2D	g2d = (Graphics2D) source.getGraphics();
		
		g2d.drawImage(Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(content.getSource(), filter)), 0, 0, observer);
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
	
    private static void floodFill(final BufferedImage image, int sr, int sc, int oldColor, int newColor) {
        final List<XY>	queue = new LinkedList<>();
        
        queue.add(new XY(sr, sc));
        while (!queue.isEmpty()) {
            final XY 	p = queue.get(0);
            
            if (isInside(image, p.x, p.y, oldColor)) {
                image.setRGB(p.x, p.y, newColor);
            }
            if (isInside(image, p.x - 1, p.y, oldColor)) { // up -> row-1
                queue.add(new XY(p.x - 1, p.y));
            }
            if (isInside(image, p.x + 1, p.y, oldColor)) { // down -> row+1
                queue.add(new XY(p.x + 1, p.y));
            }
            if (isInside(image, p.x, p.y - 1, oldColor)) { // left -> col-1
                queue.add(new XY(p.x, p.y - 1));
            }
            if (isInside(image, p.x, p.y + 1, oldColor)) { // right -> col+1
                queue.add(new XY(p.x, p.y + 1));
            }
        }
    }

    private static boolean isInside(final BufferedImage image, int x, int y, int oldColor) {
        if (x > -1 && x < image.getWidth() && y > -1 && y < image.getHeight() && image.getRGB(x, y) == oldColor) {
            return true;
        }
        else {
            return false;
        }
    }
	
	private static void printTransformation(final AffineTransform at, final Rectangle rect) {
		final Point2D.Float[]	src = new Point2D.Float[] {new Point2D.Float(rect.x,  rect.y), new Point2D.Float(rect.x+rect.width, rect.y+rect.height)};
		final Point2D.Float[]	dst = new Point2D.Float[2];
		
		at.transform(src, 0, dst, 0,src.length);
		System.err.println("Transform "+src[0]+"->"+dst[0]+", "+src[1]+"->"+dst[1]);
	}
	
	private static class XY {
	    final int x;
	    final int y;

	    public XY(final int x, final int y) {
	        this.x = x;
	        this.y = y;
	    }
	}
}
