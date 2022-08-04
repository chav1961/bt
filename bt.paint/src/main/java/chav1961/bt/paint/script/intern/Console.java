package chav1961.bt.paint.script.intern;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import chav1961.bt.paint.control.ImageUtils;
import chav1961.bt.paint.control.ImageUtils.DrawingType;
import chav1961.bt.paint.control.ImageUtils.ProcessType;
import chav1961.bt.paint.control.Predefines;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.AnchorPoint;
import chav1961.bt.paint.script.interfaces.CanvasWrapper;
import chav1961.bt.paint.script.interfaces.ClipboardWrapper;
import chav1961.bt.paint.script.interfaces.ColorWrapper;
import chav1961.bt.paint.script.interfaces.FontWrapper;
import chav1961.bt.paint.script.interfaces.ImageType;
import chav1961.bt.paint.script.interfaces.ImageWrapper;
import chav1961.bt.paint.script.interfaces.MirrorDirection;
import chav1961.bt.paint.script.interfaces.RectWrapper;
import chav1961.bt.paint.script.interfaces.RotateDirection;
import chav1961.bt.paint.script.interfaces.StrokeWrapper;
import chav1961.bt.paint.script.interfaces.SystemWrapper;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.CharUtils.ArgumentType;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.ui.ColorPair;
import chav1961.purelib.ui.swing.useful.svg.SVGUtils;

//* line (x,y) (x,y)
//* rect (x,y) (x,y)
//* ellipse (x,y) (x,y)
// text (x,y) (x,y) <text>
//* path <path>
//* fill (x,y) {#nnn | <name> }
//* stroke N [{solid|dashed|dotted}] [{butt|}] [{|}]
//* fore {(x,y) | #nnn |<name> }
//* back {(x,y) | #nnn |<name> }
//* font <name> N [{bold|italic}]
// ? <request>
//* rotate angle
//* mirror {h|v}
//* crop (x,y) (x,y) 
//* scale {(x,y) | N} 
//* resize {(x,y) | N}
//* gray
//* undo
//* redo
//* trans [!]{(x,y) | #nnn |<name> }
//* select (x,y) (x,y)
//* copy
//* erase
//* paste (x,y) [file]
//* save [<name>]
//* load <name>
//* new (x,y) {type}
//* play <name>
//* quit
//* qquit
public class Console {
	private static final String	KEY_UNDO_CROP = "chav1961.bt.paint.editor.ImageEditPanel.undo.crop";
	private static final String	KEY_REDO_CROP = "chav1961.bt.paint.editor.ImageEditPanel.redo.crop";
	private static final String	KEY_UNDO_RESIZE = "chav1961.bt.paint.editor.ImageEditPanel.undo.resize";
	private static final String	KEY_REDO_RESIZE = "chav1961.bt.paint.editor.ImageEditPanel.redo.resize";
	private static final String	KEY_UNDO_ROTATE = "chav1961.bt.paint.editor.ImageEditPanel.undo.rotate";
	private static final String	KEY_REDO_ROTATE = "chav1961.bt.paint.editor.ImageEditPanel.redo.rotate";
	private static final String	KEY_UNDO_REFLECT = "chav1961.bt.paint.editor.ImageEditPanel.undo.reflect.horizontal";
	private static final String	KEY_REDO_REFLECT = "chav1961.bt.paint.editor.ImageEditPanel.redo.reflect.horizontal";
	private static final String	KEY_UNDO_GRAYSCALE = "chav1961.bt.paint.editor.ImageEditPanel.undo.grayscale";
	private static final String	KEY_REDO_GRAYSCALE = "chav1961.bt.paint.editor.ImageEditPanel.redo.grayscale";
	private static final String	KEY_UNDO_TRANSPARENCY = "chav1961.bt.paint.editor.ImageEditPanel.undo.transparency";
	private static final String	KEY_REDO_TRANSPARENCY = "chav1961.bt.paint.editor.ImageEditPanel.redo.transparency";
	private static final String	KEY_UNDO_PASTE = "chav1961.bt.paint.editor.ImageEditPanel.undo.paste";
	private static final String	KEY_REDO_PASTE = "chav1961.bt.paint.editor.ImageEditPanel.redo.paste";
	private static final String	KEY_UNDO_DRAW_ELLIPSE = "chav1961.bt.paint.editor.ImageEditPanel.undo.draw.ellipse";
	private static final String	KEY_REDO_DRAW_ELLIPSE = "chav1961.bt.paint.editor.ImageEditPanel.redo.draw.ellipse";
	private static final String	KEY_UNDO_DRAW_LINE = "chav1961.bt.paint.editor.ImageEditPanel.undo.draw.line";
	private static final String	KEY_REDO_DRAW_LINE = "chav1961.bt.paint.editor.ImageEditPanel.redo.draw.line";
	private static final String	KEY_UNDO_DRAW_PATH = "chav1961.bt.paint.editor.ImageEditPanel.undo.draw.path";
	private static final String	KEY_REDO_DRAW_PATH = "chav1961.bt.paint.editor.ImageEditPanel.redo.draw.path";
	private static final String	KEY_UNDO_DRAW_RECT = "chav1961.bt.paint.editor.ImageEditPanel.undo.draw.rect";
	private static final String	KEY_REDO_DRAW_RECT = "chav1961.bt.paint.editor.ImageEditPanel.redo.draw.rect";
	private static final String	KEY_UNDO_DRAW_TEXT = "chav1961.bt.paint.editor.ImageEditPanel.undo.draw.text";
	private static final String	KEY_REDO_DRAW_TEXT = "chav1961.bt.paint.editor.ImageEditPanel.redo.draw.text";
	private static final String	KEY_UNDO_CHANGE_FOREGROUND = "chav1961.bt.paint.editor.ImageEditPanel.undo.change.foreground";
	private static final String	KEY_REDO_CHANGE_FOREGROUND = "chav1961.bt.paint.editor.ImageEditPanel.redo.change.foreground";
	private static final String	KEY_UNDO_CHANGE_BACKGROUND = "chav1961.bt.paint.editor.ImageEditPanel.undo.change.background";
	private static final String	KEY_REDO_CHANGE_BACKGROUND = "chav1961.bt.paint.editor.ImageEditPanel.redo.change.background";
	private static final String	KEY_UNDO_CHANGE_FONT = "chav1961.bt.paint.editor.ImageEditPanel.undo.change.font";
	private static final String	KEY_REDO_CHANGE_FONT = "chav1961.bt.paint.editor.ImageEditPanel.redo.change.font";
	private static final String	KEY_UNDO_CHANGE_STROKE = "chav1961.bt.paint.editor.ImageEditPanel.undo.change.stroke";
	private static final String	KEY_REDO_CHANGE_STROKE = "chav1961.bt.paint.editor.ImageEditPanel.redo.change.stroke";

	
	private static final SyntaxTreeInterface<CommandItem>	COMMANDS = new AndOrTree<>();
	private static final String	OK = "ok";

	private static enum CanvasProperties {
		FORE_COLOR,
		BACK_COLOR,
		STROKE,
		FONT,
		FILLING;
	}
	
	@FunctionalInterface
	private static interface Executor {
		String process(final Predefines predef, Object... parameters) throws PaintScriptException;
	}

	static {
		CommandItem	ci = new CommandItem("line", CommandItem.CommandType.ImageAction
				, KEY_UNDO_DRAW_LINE
				, KEY_REDO_DRAW_LINE
				, "line <xFrom::int>,<yFrom::int> [to] <xTo::int>,<yTo::int>"
				, (p,a)->drawLine(p,(Integer)a[0],(Integer)a[1],(Integer)a[2],(Integer)a[3])
				, ArgumentType.signedInt, ',', ArgumentType.signedInt, new CharUtils.Optional("to"), ArgumentType.signedInt, ',', ArgumentType.signedInt);
		COMMANDS.placeName("line", ci);
		COMMANDS.placeName("l", ci);

		ci = new CommandItem("rectangle", CommandItem.CommandType.ImageAction
				, KEY_UNDO_DRAW_RECT
				, KEY_REDO_DRAW_RECT
				, "rectangle <rect::Rectangle> [on]"
				, (p,a)-> {
					if (a[1] instanceof Boolean) {
						return drawRect(p,(Rectangle)a[0],true);
					}
					else {
						return drawRect(p,(Rectangle)a[0],false);
					}
				}
				, ArgumentType.rectangleRepresentation, new CharUtils.Optional(ArgumentType.Boolean));
		COMMANDS.placeName("rectangle", ci);
		COMMANDS.placeName("rect", ci);

		ci = new CommandItem("ellipse", CommandItem.CommandType.ImageAction
				, KEY_UNDO_DRAW_ELLIPSE
				, KEY_REDO_DRAW_ELLIPSE
				, "ellipse <rect::Rectangle> [on]"
				, (p,a)-> {
					if (a[1] instanceof Boolean) {
						return drawEllipse(p,(Rectangle)a[0],true);
					}
					else {
						return drawEllipse(p,(Rectangle)a[0],false);
					}
				}
				, ArgumentType.rectangleRepresentation, new CharUtils.Optional(ArgumentType.Boolean));
		COMMANDS.placeName("ellipse", ci);
		COMMANDS.placeName("ell", ci);

		ci = new CommandItem("text", CommandItem.CommandType.ImageAction
				, KEY_UNDO_DRAW_TEXT
				, KEY_REDO_DRAW_TEXT
				, "text <rect::Rectangle> <foreground::color>[/<background::color>] <content::any>"
				, (p,a)->{
					if (a[2] instanceof Color) {
						return drawText(p, (Rectangle)a[0], (Color)a[1], (Color)a[2], (String)a[3]);
					}
					else {
						return drawText(p, (Rectangle)a[0], (Color)a[1], (String)a[2]);
					}
				}
				, ArgumentType.rectangleRepresentation, ArgumentType.colorRepresentation, new CharUtils.Optional('/', ArgumentType.colorRepresentation), ArgumentType.raw);
		COMMANDS.placeName("text", ci);
		COMMANDS.placeName("t", ci);

		ci = new CommandItem("path", CommandItem.CommandType.ImageAction
				, KEY_UNDO_DRAW_PATH
				, KEY_REDO_DRAW_PATH
				, "path [on] <content::any>"
				, (p,a)->{
					if (a[0] instanceof Boolean) {
						return drawPath(p,(String)a[1],((Boolean)a[0]));	
					}
					else {
						return drawPath(p,(String)a[0],false);	
					}
				}
				, new CharUtils.Optional(ArgumentType.Boolean), ArgumentType.raw);
		COMMANDS.placeName("path", ci);
		COMMANDS.placeName("p", ci);

		ci = new CommandItem("rotate", CommandItem.CommandType.ImageAction
				, KEY_UNDO_ROTATE
				, KEY_REDO_ROTATE
				, "rotate {cw|ccw}"
				, (p,a)->rotate(p,(RotateDirection)a[0])
				, RotateDirection.class);
		COMMANDS.placeName("rotate", ci);
		COMMANDS.placeName("rot", ci);
		
		ci = new CommandItem("mirror", CommandItem.CommandType.ImageAction
				, KEY_UNDO_REFLECT
				, KEY_REDO_REFLECT
				, "mirror {hor|vert}"
				, (p,a)->mirror(p,(MirrorDirection)a[0])
				, MirrorDirection.class);
		COMMANDS.placeName("mirror", ci);
		COMMANDS.placeName("mirror", ci);

		ci = new CommandItem("scale", CommandItem.CommandType.ImageAction
				, KEY_UNDO_RESIZE
				, KEY_REDO_RESIZE
				, "scale <newX::int>[,<newY::int>]"
				, (p,a)->scale(p,(Integer)a[0],a[1] instanceof Integer ? (Integer)a[1] : (Integer)a[0])
				, ArgumentType.signedInt, new CharUtils.Optional(',', ArgumentType.signedInt));
		COMMANDS.placeName("scale", ci);

		ci = new CommandItem("crop", CommandItem.CommandType.ImageAction
				, KEY_UNDO_CROP
				, KEY_REDO_CROP
				, "crop <rect::Rectangle>"
				, (p,a)->crop(p, (Rectangle)a[0])
				, ArgumentType.rectangleRepresentation);
		COMMANDS.placeName("crop", ci);
		
		ci = new CommandItem("resize", CommandItem.CommandType.ImageAction
				, KEY_UNDO_RESIZE
				, KEY_REDO_RESIZE
				, "resize <newX::int>,<newY::int> [center]"
				, (p,a)->resize(p,(Integer)a[0],(Integer)a[1],a[2] instanceof AnchorPoint ? ((AnchorPoint)a[2]) : AnchorPoint.unknown)
				, ArgumentType.signedInt, ',', ArgumentType.signedInt, new CharUtils.Optional(AnchorPoint.class));
		COMMANDS.placeName("resize", ci);

		ci = new CommandItem("gray", CommandItem.CommandType.ImageAction
				, KEY_UNDO_GRAYSCALE
				, KEY_REDO_GRAYSCALE
				, "gray"
				, (p,a)->gray(p));
		COMMANDS.placeName("gray", ci);

		ci = new CommandItem("transparent", CommandItem.CommandType.ImageAction
				, KEY_UNDO_TRANSPARENCY
				, KEY_REDO_TRANSPARENCY
				, "transparent [except] <color::color>"
				, (p,a)->transparent(p,a[0] instanceof CharUtils.Mark ? (Color)a[1] : (Color)a[0], a[0] instanceof CharUtils.Mark)
				, new CharUtils.Optional("except", new CharUtils.Mark(1)), ArgumentType.colorRepresentation);
		COMMANDS.placeName("transparent", ci);
		COMMANDS.placeName("trans", ci);

		ci = new CommandItem("fill", CommandItem.CommandType.ImageAction 
				, ""
				, ""
				, "fill <x::int>,<y::int> <color::color>"
				, (p,a)->fill(p,(Integer)a[0],(Integer)a[1],(Color)a[2])
				, ArgumentType.signedInt, ',', ArgumentType.signedInt, ArgumentType.colorRepresentation);
		COMMANDS.placeName("fill", ci);

		ci = new CommandItem("copy", CommandItem.CommandType.Silent
				, ""
				, ""
				, "copy [<xFrom::int>,<yFrom:int> {size <width::int>,<height::int>|[to] <xTo::int>,<yTo::int>}]"
				, (p,a)->a[0] instanceof Rectangle ? copyRange(p, (Rectangle)a[0]) : copyAll(p)
				, new CharUtils.Optional(ArgumentType.rectangleRepresentation)
				);
		COMMANDS.placeName("copy", ci);
		COMMANDS.placeName("cp", ci);

		ci = new CommandItem("paste", CommandItem.CommandType.ImageAction 
				, KEY_UNDO_PASTE
				, KEY_REDO_PASTE
				, "paste <xFrom::int>,<yFrom::int> [{size <width::int>,<height::int>|[to] <xTo::int>,<yTo::int>}] [from <name::string>]"
				, (p,a)->{
					if (a[0] instanceof Integer) {
						if (a[2] instanceof String) {
							return pasteFile(p, (Integer)a[0], (Integer)a[1], (String)a[3]);
						}
						else {
							return pasteClipboard(p, (Integer)a[0], (Integer)a[1]);
						}
					}
					else {
						if (a[1] instanceof String) {
							return pasteFileScaled(p, (Integer)a[0], (Integer)a[1], (Integer)a[3], (Integer)a[4], true, (String)a[6]);
						}
						else {
							return pasteClipboardScaled(p, (Integer)a[0], (Integer)a[1], (Integer)a[3], (Integer)a[4], true);
						}
					}
				}
				, new CharUtils.Choise(new Object[] {ArgumentType.signedInt, ',', ArgumentType.signedInt}, new Object[] {ArgumentType.rectangleRepresentation}), new CharUtils.Optional("from", ArgumentType.raw));
		COMMANDS.placeName("paste", ci);
		
//		ci = new CommandItem("undo", "undo", (p,a)->drawPath(p,(String)a[0]));
//		COMMANDS.placeName("undo", ci);
//		COMMANDS.placeName("u", ci);

//		ci = new CommandItem("redo", "redo", (p,a)->drawPath(p,(String)a[0]));
//		COMMANDS.placeName("redo", ci);

//		ci = new CommandItem("select", "select <xFrom::int>,<yFrom::int> [to] <xTo::int>,<yTo::int>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.signedInt, ',', ArgumentType.signedInt, new CharUtils.Optional("to"), ArgumentType.signedInt, ',', ArgumentType.signedInt);
//		COMMANDS.placeName("select", ci);
//		COMMANDS.placeName("sel", ci);


//		ci = new CommandItem("erase", "erase <color::color>", (p,a)->drawPath(p,(String)a[0]));
//		COMMANDS.placeName("erase", ci);
		

//		ci = new CommandItem("save", "save [<name::string>]", (p,a)->drawPath(p,(String)a[0]), new CharUtils.Optional(ArgumentType.raw));
//		COMMANDS.placeName("save", ci);

//		ci = new CommandItem("load", "load <name::string>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.raw);
//		COMMANDS.placeName("load", ci);

		ci = new CommandItem("new", CommandItem.CommandType.ImageAction 
				, ""
				, ""
				, "new <width::int>,<height::int> <type::ImageType> [<color::color>] "
				, (p,a)->newImage(p,(Integer)a[0],(Integer)a[1],(ImageType)a[2],(Color)a[3])
				, ArgumentType.signedInt, ',', ArgumentType.signedInt, ImageType.class, new CharUtils.Optional(ArgumentType.colorRepresentation));
		COMMANDS.placeName("new", ci);

		ci = new CommandItem("load", CommandItem.CommandType.ImageAction
				, ""
				, ""
				, "load <name::string>"
				, (p,a)->""
				, ArgumentType.raw);
		COMMANDS.placeName("load", ci);
		
		ci = new CommandItem("foreground", CommandItem.CommandType.PropertyAction
				, KEY_UNDO_CHANGE_FOREGROUND
				, KEY_REDO_CHANGE_FOREGROUND
				, "foreground {<color::color>|<x::int>,<y::int>}"
				, (p,a)->setProperties(p,CanvasProperties.FORE_COLOR, a[0], a[1])
				, new CharUtils.Choise(new Object[] {ArgumentType.colorRepresentation}, new Object[] {ArgumentType.signedInt, ',', ArgumentType.signedInt}));
		COMMANDS.placeName("foreground", ci);
		COMMANDS.placeName("fore", ci);

		ci = new CommandItem("background", CommandItem.CommandType.PropertyAction
				, KEY_UNDO_CHANGE_BACKGROUND
				, KEY_REDO_CHANGE_BACKGROUND
				, "background {<color::color>|<x::int>,<y::int>}"
				, (p,a)->setProperties(p,CanvasProperties.BACK_COLOR, a[0], a[1])
				, new CharUtils.Choise(new Object[] {ArgumentType.colorRepresentation}, new Object[] {ArgumentType.signedInt, ',', ArgumentType.signedInt}));
		COMMANDS.placeName("background", ci);
		COMMANDS.placeName("back", ci);

		ci = new CommandItem("font", CommandItem.CommandType.PropertyAction
				, KEY_UNDO_CHANGE_FONT
				, KEY_REDO_CHANGE_FONT
				, "font <family> <size::int> [bold] [italic]"
				, (p,a)->setProperties(p,CanvasProperties.FONT,(String)a[0])
				, ArgumentType.raw);
		COMMANDS.placeName("font", ci);

		ci = new CommandItem("stroke", CommandItem.CommandType.PropertyAction
				, KEY_UNDO_CHANGE_STROKE
				, KEY_REDO_CHANGE_STROKE
				, "stroke [<thickness::int>] {solid|dashed|dotted} [{butt|round|square} [{miter|round|bevel}]]"
				, (p,a)->setProperties(p,CanvasProperties.STROKE,(String)a[0])
				, ArgumentType.raw);
		COMMANDS.placeName("stroke", ci);
		COMMANDS.placeName("str", ci);
	}
		
	public static String processCommand(final String command, final Predefines predef) throws SyntaxException, PaintScriptException {
		final char[]	cmd = CharUtils.terminateAndConvert2CharArray(command.trim(), '\n');
		final int[]		bounds = new int[2];
		final Object[]	parameters = new Object[100];
		int				from = CharUtils.skipBlank(cmd, 0, true);
		
		from = CharUtils.parseName(cmd, from, bounds);
		final long		cmdId = COMMANDS.seekName(cmd, bounds[0], bounds[1]+1);
		
		if (cmdId < 0) {
			throw new SyntaxException(0, from, "Unknown command");
		}
		else {
			final CommandItem	ci = COMMANDS.getCargo(cmdId);
			int					stopColumn;
			
			if (ci.lexemas.length > 0) {
				if ((stopColumn = CharUtils.tryExtract(cmd, from, ci.lexemas)) >= 0) {
					if (stopColumn < command.length()-1) {
						throw new SyntaxException(0, stopColumn, "Unparsed tail for command ["+ci.command+"]: must be "+ci.help);
					}
					else {
						final CanvasWrapper	cw = predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class);
						
						from = CharUtils.extract(cmd, from, parameters, ci.lexemas);
						
						switch (ci.commandType) {
							case ImageAction	:
								cw.startImageAction(ci.undoDescriptor, ci.redoDescriptor);
								break;
							case PropertyAction	:
								cw.startPropertyAction(ci.undoDescriptor, ci.redoDescriptor);
								break;
							default:
								break;
						}
						
						final String	result = ci.exec.process(predef, parameters);
						
						switch (ci.commandType) {
							case ImageAction	:
								cw.endImageAction(ci.undoDescriptor, ci.redoDescriptor);
								break;
							case PropertyAction	:
								cw.endPropertyAction(ci.undoDescriptor, ci.redoDescriptor);
								break;
							default:
								break;
						}
						return result;
					}
				}
				else {
					throw new SyntaxException(0, -stopColumn, "Illegal format for command ["+ci.command+"]: must be "+ci.help);
				}
			}
			else {
				return ci.exec.process(predef, parameters);
			}
		}
	}
	
	private static String newImage(final Predefines predef, final int width, final int height, final ImageType imageType, final Color fill) throws PaintScriptException {
		final BufferedImage	image = new BufferedImage(width, height, imageType.getType());
		final BufferedImage	result;
		
		if (fill != null) {
			result = (BufferedImage) ImageUtils.process(ProcessType.FILL, image, null, new Rectangle(0, 0, width, height), fill);
		}
		else {
			result = image;
		}
		predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setImage(ImageWrapper.of(result));
		return OK;
	}

	private static String drawLine(final Predefines predef, final int xFrom, final int yFrom, final int xTo, final int yTo) throws PaintScriptException {
		ImageUtils.draw(DrawingType.LINE, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage().getImage(), null
					, new Point(xFrom, yFrom), new Point(xTo, yTo)
					, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasForeground().getColor()
					, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasStroke().getStroke()); 
		return OK;
	}

	private static String drawRect(final Predefines predef, final Rectangle rect, final boolean fill) throws PaintScriptException {
		ImageUtils.draw(DrawingType.RECT, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage().getImage(), null
				, rect
				, fill 
					? new ColorPair(predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasForeground().getColor(), predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasBackground().getColor())
					: predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasForeground().getColor()
				, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasStroke().getStroke()); 
		return OK;
	}
	
	private static String drawEllipse(final Predefines predef, final Rectangle rect, final boolean fill) throws PaintScriptException {
		ImageUtils.draw(DrawingType.ELLIPSE, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage().getImage(), null
				, rect
				, fill 
					? new ColorPair(predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasForeground().getColor(), predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasBackground().getColor())
					: predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasForeground().getColor()
				, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasStroke().getStroke()); 
		return OK;
	}
	
	private static String drawText(final Predefines predef, final Rectangle rect, final Color color, final String text) throws PaintScriptException {
		ImageUtils.draw(DrawingType.TEXT, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage().getImage(), null, text, rect, color, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasFont().getFont());
		return OK;
	}


	private static String drawText(final Predefines predef, final Rectangle rect, final Color foreground, final Color background, final String text) throws PaintScriptException {
		ImageUtils.draw(DrawingType.TEXT, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage().getImage(), null, text, rect, new ColorPair(foreground,background), predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasFont().getFont());
		return OK;
	}
	
	private static String drawPath(final Predefines predef, final String path, final boolean fill) throws PaintScriptException { 
		try{ImageUtils.draw(DrawingType.PEN, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage().getImage(), null
						, SVGUtils.extractCommands(path)
						, fill 
							? new ColorPair(predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasForeground().getColor(), predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasBackground().getColor())
							: predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasForeground().getColor()
						, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasStroke().getStroke());
			return OK;
		} catch (SyntaxException e) {
			throw new PaintScriptException(e);
		} 
	}

	private static String rotate(final Predefines predef, final RotateDirection dir) throws PaintScriptException {
		final ImageWrapper	iw = predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage();
		final ImageWrapper	result;
		
		switch (dir) {
			case cw		:
				result = ImageWrapper.of(ImageUtils.process(ProcessType.ROTATE_CLOCKWISE, iw.getImage(), null));
				break;
			case ccw	:
				result = ImageWrapper.of(ImageUtils.process(ProcessType.ROTATE_COUNTERCLOCKWISE, iw.getImage(), null));
				break;
			default		:
				throw new UnsupportedOperationException("Rotate direction ["+dir+"] is not supported yet");  
		}
		predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setImage(result);
		return OK;
	}
	
	private static String mirror(final Predefines predef, final MirrorDirection dir) throws PaintScriptException {
		final ImageWrapper	iw = predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage();
		final ImageWrapper	result;
		
		switch (dir) {
			case hor	:
				result = ImageWrapper.of(ImageUtils.process(ProcessType.MIRROR_HORIZONTAL, iw.getImage(), null));
				break;
			case vert	:
				result = ImageWrapper.of(ImageUtils.process(ProcessType.MIRROR_VERTICAL, iw.getImage(), null));
				break;
			default		:
				throw new UnsupportedOperationException("Mirror direction ["+dir+"] is not supported yet");  
		}
		predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setImage(result);
		return OK;
	}

	private static String crop(final Predefines predef, final Rectangle rect) throws PaintScriptException {
		final ImageWrapper	iw = predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage();
		final ImageWrapper	result = ImageWrapper.of(ImageUtils.process(ProcessType.CROP, (BufferedImage)iw.getImage(), null, rect));
		
		predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setImage(result);
		return OK;
	}

	private static String scale(final Predefines predef, final int newWidth, final int newHeight) throws PaintScriptException {
		final ImageWrapper	iw = predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage();
		final ImageWrapper	result = ImageWrapper.of(ImageUtils.process(ProcessType.SCALE, (BufferedImage)iw.getImage(), null, newWidth, newHeight));
		
		predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setImage(result);
		return OK;
	}

	private static String resize(final Predefines predef, final int newWidth, final int newHeight, final AnchorPoint anchor) throws PaintScriptException {
		final ImageWrapper	iw = predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage();
		final ColorWrapper	cw = predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasBackground();
		final ImageWrapper	result;
		
		switch (anchor) {
			case center		:
				result = ImageWrapper.of(ImageUtils.process(ProcessType.RESIZE, (BufferedImage)iw.getImage(), null, newWidth, newHeight, cw.getColor(), true));
				break;
			case unknown	:
				result = ImageWrapper.of(ImageUtils.process(ProcessType.RESIZE, (BufferedImage)iw.getImage(), null, newWidth, newHeight, cw.getColor(), false));
				break;
			default :
				throw new PaintScriptException("Anchor type [] doesn't support, 'center' in available only"); 
		
		}
		predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setImage(result);
		return OK;
	}
	
	private static String gray(final Predefines predef) throws PaintScriptException {
		final ImageWrapper	iw = predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage();
		final ImageWrapper	result = ImageWrapper.of(ImageUtils.process(ProcessType.TO_GRAYSCALE, (BufferedImage)iw.getImage(), null));
		
		predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setImage(result);
		return OK;
	}

	private static String transparent(final Predefines predef, final Color color, final boolean except) throws PaintScriptException {
		final ImageWrapper	iw = predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage();
		final ImageWrapper	result = ImageWrapper.of(ImageUtils.process(ProcessType.TO_TRANSPARENT, (BufferedImage)iw.getImage(), null, color, except));
		
		predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setImage(result);
		return OK;
	}

	private static String fill(final Predefines predef, final int x, final int y, final Color color) {
//		final ImageWrapper	iw = predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage();
//		final ImageWrapper	result = ImageWrapper.of(ImageUtils.process(ProcessType.FILL, (BufferedImage)iw.getImage(), null, color, except));
//		
//		predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setImage(result);
		return OK;
	}

	private static String copyRange(final Predefines predef, final Rectangle rect) throws PaintScriptException {
		final ImageWrapper		iw = predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage(RectWrapper.of(rect));
		final ClipboardWrapper	cbw = predef.getPredefined(Predefines.PREDEF_CLIPBOARD, ClipboardWrapper.class);
		
		cbw.setImage(iw);
		return OK;
	}

	private static String copyAll(final Predefines predef) throws PaintScriptException {
		final ImageWrapper		iw = predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage();
		final ClipboardWrapper	cbw = predef.getPredefined(Predefines.PREDEF_CLIPBOARD, ClipboardWrapper.class);
		
		cbw.setImage(iw);
		return OK;
	}

	private static String pasteClipboard(final Predefines predef, final int xTo, final int yTo) throws PaintScriptException {
		if (!predef.getPredefined(Predefines.PREDEF_CLIPBOARD, ClipboardWrapper.class).hasImage()) {
			throw new PaintScriptException("Clipboard doesn't contain any image to paste");
		}
		else {
			return pasteImage(predef, predef.getPredefined(Predefines.PREDEF_CLIPBOARD, ClipboardWrapper.class).getImage(), xTo, yTo);
		}
	}

	private static String pasteClipboardScaled(final Predefines predef, final int xFrom, final int yFrom, final int xToOrWidth, final int yToOrHeight, final boolean useAsSize) throws PaintScriptException {
		if (!predef.getPredefined(Predefines.PREDEF_CLIPBOARD, ClipboardWrapper.class).hasImage()) {
			throw new PaintScriptException("Clipboard doesn't contain any image to paste");
		}
		else {
			return pasteImageScaled(predef, predef.getPredefined(Predefines.PREDEF_CLIPBOARD, ClipboardWrapper.class).getImage(), xFrom, yFrom, xToOrWidth, yToOrHeight, useAsSize);
		}
	}
	
	private static String pasteFile(final Predefines predef, final int xTo, final int yTo, final String file) throws PaintScriptException {
		return pasteImage(predef, predef.getPredefined(Predefines.PREDEF_SYSTEM, SystemWrapper.class).loadImage(file), xTo, yTo);
	}

	private static String pasteFileScaled(final Predefines predef, final int xFrom, final int yFrom, final int xToOrWidth, final int yToOrHeight, final boolean useAsSize, final String file) throws PaintScriptException {
		return pasteImageScaled(predef, predef.getPredefined(Predefines.PREDEF_SYSTEM, SystemWrapper.class).loadImage(file), xFrom, yFrom, xToOrWidth, yToOrHeight, useAsSize);
	}

	private static String pasteImage(final Predefines predef, final ImageWrapper iwFrom, final int xTo, final int yTo) throws PaintScriptException {
		final ImageWrapper	iwTo = predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage();
		final Rectangle		rect = new Rectangle(xTo, yTo, iwFrom.getImage().getWidth(null), iwFrom.getImage().getHeight(null));
		final ImageWrapper	result = ImageWrapper.of(ImageUtils.process(ProcessType.INSERT, iwTo.getImage(), null, rect, iwFrom.getImage()));
		
		predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setImage(result);
		return OK;
	}

	private static String pasteImageScaled(final Predefines predef, final ImageWrapper iwFrom, final int xFrom, final int yFrom, final int xToOrWidth, final int yToOrHeight, final boolean useAsSize) throws PaintScriptException {
		final ImageWrapper	iwTo = predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage();
		final Rectangle		rect = new Rectangle(xFrom, yFrom, useAsSize ? xToOrWidth : xToOrWidth - xFrom, useAsSize ? yToOrHeight : yToOrHeight - yFrom);
		final ImageWrapper	result = ImageWrapper.of(ImageUtils.process(ProcessType.INSERT, iwTo.getImage(), null, rect, iwFrom.getImage()));
		
		predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setImage(result);
		return OK;
	}
	
	private static String setProperties(final Predefines predef, final CanvasProperties props, final Object... content) throws PaintScriptException {
		switch(props) {
			case BACK_COLOR	:
				if (content.length > 0 && (content[0] instanceof Color)) {
					predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setCanvasBackground(ColorWrapper.of((Color)content[0]));
				}
				else if (content.length > 1 && (content[0] instanceof Integer) && (content[1] instanceof Integer)) {
					final BufferedImage	image = (BufferedImage)predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage().getImage();
					final int			x = (Integer)content[0], y = (Integer)content[1]; 
					
					if (x < 0 || x >= image.getWidth()) {
						throw new PaintScriptException("Point coordinates to get color ("+x+","+y+") outside current image dimension ("+image.getWidth()+","+image.getHeight()+")");
					}
					else {
						predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setCanvasBackground(ColorWrapper.of(new Color(image.getRGB(x, y))));
					}
				}
				else {
					throw new IllegalArgumentException("Neither Color nor Integer,Integer in the content parameter"); 
				}
				break;
			case FORE_COLOR	:
				if (content.length > 0 && (content[0] instanceof Color)) {
					predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setCanvasForeground(ColorWrapper.of((Color)content[0]));
				}
				else if (content.length > 1 && (content[0] instanceof Integer) && (content[1] instanceof Integer)) {
					final BufferedImage	image = (BufferedImage)predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage().getImage();
					final int			x = (Integer)content[0], y = (Integer)content[1]; 
					
					if (x < 0 || x >= image.getWidth()) {
						throw new PaintScriptException("Point coordinates to get color ("+x+","+y+") outside current image dimension ("+image.getWidth()+","+image.getHeight()+")");
					}
					else {
						predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setCanvasForeground(ColorWrapper.of(new Color(image.getRGB(x, y))));
					}
				}
				else {
					throw new IllegalArgumentException("Neither Color nor Integer,Integer in the content parameter"); 
				}
				break;
			case FONT		:
				predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setCanvasFont(FontWrapper.of((String)content[0]));
				break;
			case STROKE		:
				predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setCanvasStroke(StrokeWrapper.of((String)content[0]));
				break;
			default:
				throw new UnsupportedOperationException("Canvas property ["+props+"] is not supported yet");
		}
		return OK;
	}	

	private static class CommandItem {
		private static enum CommandType {
			ImageAction,
			PropertyAction,
			Silent;
		}
		
		final String		command;
		final CommandType	commandType;
		final Object[]		lexemas;
		final String		help;
		final String		undoDescriptor;
		final String		redoDescriptor;
		final Executor		exec;
		
		public CommandItem(final String command, final CommandType commandType, final String undoDescriptor, final String redoDescriptor, final String help, final Executor exec, final Object... lexemas) {
			this.command = command;
			this.commandType = commandType;
			this.undoDescriptor = undoDescriptor;
			this.redoDescriptor = redoDescriptor;
			this.lexemas = lexemas;
			this.help = help;
			this.exec = exec;
		}

		@Override
		public String toString() {
			return "CommandItem [command=" + command + ", commandType=" + commandType + ", lexemas="
					+ Arrays.toString(lexemas) + ", help=" + help + ", undoDescriptor=" + undoDescriptor
					+ ", redoDescriptor=" + redoDescriptor + "]";
		}
	}
}
