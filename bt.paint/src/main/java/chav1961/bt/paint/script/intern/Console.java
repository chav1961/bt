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
import chav1961.bt.paint.script.interfaces.CanvasWrapper;
import chav1961.bt.paint.script.interfaces.ColorWrapper;
import chav1961.bt.paint.script.interfaces.FontWrapper;
import chav1961.bt.paint.script.interfaces.ImageType;
import chav1961.bt.paint.script.interfaces.ImageWrapper;
import chav1961.bt.paint.script.interfaces.MirrorDirection;
import chav1961.bt.paint.script.interfaces.RotateDirection;
import chav1961.bt.paint.script.interfaces.StrokeWrapper;
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
//* filling {on|off}
//* font <name> N [{bold|italic}]
// ? <request>
//* rotate angle
//* mirror {h|v}
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
		CommandItem	ci = new CommandItem("line", "line <xFrom::int>,<yFrom::int> <xTo::int>,<yTo::int>", 
				(p,a)->drawLine(p,(Integer)a[0],(Integer)a[1],(Integer)a[2],(Integer)a[3]),
				ArgumentType.signedInt, ',', ArgumentType.signedInt, ArgumentType.signedInt, ',', ArgumentType.signedInt);
		COMMANDS.placeName("line", ci);
		COMMANDS.placeName("l", ci);

		ci = new CommandItem("rectangle", "rectangle <xFrom::int>,<yFrom::int> <xTo::int>,<yTo::int>", 
				(p,a)->drawRect(p,(Integer)a[0],(Integer)a[1],(Integer)a[2],(Integer)a[3]),
				ArgumentType.signedInt, ',', ArgumentType.signedInt, ArgumentType.signedInt, ',', ArgumentType.signedInt);
		COMMANDS.placeName("rectangle", ci);
		COMMANDS.placeName("rect", ci);

		ci = new CommandItem("ellipse", "ellipse <xFrom::int>,<yFrom::int> [to] <xTo::int>,<yTo::int> [fill]", 
				(p,a)->drawEllipse(p,(Integer)a[0],(Integer)a[1],(Integer)a[2],(Integer)a[3],a.length > 4 ? (String)a[4] : null),
				ArgumentType.signedInt, ',', ArgumentType.signedInt, new CharUtils.Optional("to"), ArgumentType.signedInt, ',', ArgumentType.signedInt, new CharUtils.Optional(ArgumentType.name));
		COMMANDS.placeName("ellipse", ci);
		COMMANDS.placeName("ell", ci);

		ci = new CommandItem("text", "text <xFrom::int>,<yFrom::int> <xTo::int>,<yTo::int> <content::any>", 
				(p,a)->drawText(p,(Integer)a[0],(Integer)a[1],(Integer)a[2],(Integer)a[3],(String)a[4]),
				ArgumentType.signedInt, ',', ArgumentType.signedInt, ArgumentType.signedInt, ',', ArgumentType.signedInt, ArgumentType.raw);
		COMMANDS.placeName("text", ci);
		COMMANDS.placeName("t", ci);

		ci = new CommandItem("path", "path <content::any>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.raw);
		COMMANDS.placeName("path", ci);
		COMMANDS.placeName("p", ci);

		ci = new CommandItem("fill", "fill <x::int>,<y::int> <color::color>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.signedInt, ',', ArgumentType.signedInt, ArgumentType.colorRepresentation);
		COMMANDS.placeName("fill", ci);

		ci = new CommandItem("rotate", "rotate {cw|ccw}", (p,a)->rotate(p,(RotateDirection)a[0]), RotateDirection.class);
		COMMANDS.placeName("rotate", ci);
		COMMANDS.placeName("rot", ci);
		
		ci = new CommandItem("mirror", "mirror {hor|vert}", (p,a)->mirror(p,(MirrorDirection)a[0]), MirrorDirection.class);
		COMMANDS.placeName("mirror", ci);
		COMMANDS.placeName("mirror", ci);

		ci = new CommandItem("scale", "scale <sx::int>[,<sy::int>]", (p,a)->drawPath(p,(String)a[0]), ArgumentType.signedInt, new CharUtils.Optional(',', ArgumentType.signedInt));
		COMMANDS.placeName("scale", ci);

		ci = new CommandItem("resize", "resize <x::int>,<y::int>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.signedInt, ',', ArgumentType.signedInt);
		COMMANDS.placeName("resize", ci);

		ci = new CommandItem("gray", "gray", (p,a)->drawPath(p,(String)a[0]));
		COMMANDS.placeName("gray", ci);

		ci = new CommandItem("undo", "undo", (p,a)->drawPath(p,(String)a[0]));
		COMMANDS.placeName("undo", ci);
		COMMANDS.placeName("u", ci);

		ci = new CommandItem("redo", "redo", (p,a)->drawPath(p,(String)a[0]));
		COMMANDS.placeName("redo", ci);

		ci = new CommandItem("transparent", "transparent <x::int>,<y::int> <color::color>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.signedInt, ',', ArgumentType.signedInt, ArgumentType.colorRepresentation);
		COMMANDS.placeName("transparent", ci);
		COMMANDS.placeName("trans", ci);

		ci = new CommandItem("select", "select <xFrom::int>,<yFrom::int> [to] <xTo::int>,<yTo::int>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.signedInt, ',', ArgumentType.signedInt, new CharUtils.Optional("to"), ArgumentType.signedInt, ',', ArgumentType.signedInt);
		COMMANDS.placeName("select", ci);
		COMMANDS.placeName("sel", ci);

		ci = new CommandItem("copy", "copy", (p,a)->drawPath(p,(String)a[0]));
		COMMANDS.placeName("copy", ci);
		COMMANDS.placeName("cp", ci);

		ci = new CommandItem("erase", "erase <color::color>", (p,a)->drawPath(p,(String)a[0]));
		COMMANDS.placeName("erase", ci);
		
		ci = new CommandItem("paste", "paste <xFrom::int>,<yFrom::int> [<xTo::int>,<yTo::int>] [<name::string>]", (p,a)->drawPath(p,(String)a[0]), ArgumentType.signedInt, ArgumentType.signedInt, ArgumentType.signedInt, ArgumentType.signedInt, ArgumentType.raw);
		COMMANDS.placeName("paste", ci);

		ci = new CommandItem("save", "save [<name::string>]", (p,a)->drawPath(p,(String)a[0]), new CharUtils.Optional(ArgumentType.raw));
		COMMANDS.placeName("save", ci);

		ci = new CommandItem("load", "load <name::string>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.raw);
		COMMANDS.placeName("load", ci);

		ci = new CommandItem("new", "new <width::int>,<height::int> <type::ImageType> [<color::color>] ", (p,a)->newImage(p,(Integer)a[0],(Integer)a[1],(ImageType)a[2],(Color)a[3]), ArgumentType.signedInt, ',', ArgumentType.signedInt, ImageType.class, new CharUtils.Optional(ArgumentType.colorRepresentation));
		COMMANDS.placeName("new", ci);

		ci = new CommandItem("play", "play <name::string>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.raw);
		COMMANDS.placeName("play", ci);

		ci = new CommandItem("quit", "quit", (p,a)->drawPath(p,(String)a[0]));
		COMMANDS.placeName("quit", ci);
		COMMANDS.placeName("q", ci);

		ci = new CommandItem("qquit", "qquit", (p,a)->drawPath(p,(String)a[0]));
		COMMANDS.placeName("qquit", ci);
		COMMANDS.placeName("qq", ci);
		
		ci = new CommandItem("foreground", "foreground {<color::color>|<x::int>,<y::int>}", (p,a)->setProperties(p,CanvasProperties.FORE_COLOR, a[0], a[1])
								, new CharUtils.Choise(new Object[] {ArgumentType.colorRepresentation}, new Object[] {ArgumentType.signedInt, ',', ArgumentType.signedInt}));
		COMMANDS.placeName("foreground", ci);
		COMMANDS.placeName("fore", ci);

		ci = new CommandItem("background", "background {<color::color>|<x::int>,<y::int>}", (p,a)->setProperties(p,CanvasProperties.BACK_COLOR, a[0], a[1])
								, new CharUtils.Choise(new Object[] {ArgumentType.colorRepresentation}, new Object[] {ArgumentType.signedInt, ',', ArgumentType.signedInt}));
		COMMANDS.placeName("background", ci);
		COMMANDS.placeName("back", ci);

		ci = new CommandItem("stroke", "stroke [<thickness::int>] {solid|dashed|dotted} [{butt|round|square}] [{miter|round|bevel}]", (p,a)->setProperties(p,CanvasProperties.STROKE,(String)a[0]), ArgumentType.raw);
		COMMANDS.placeName("stroke", ci);
		COMMANDS.placeName("str", ci);

		ci = new CommandItem("font", "font <family> <size::int> [bold] [italic]", (p,a)->setProperties(p,CanvasProperties.FONT,(String)a[0]), ArgumentType.raw);
		COMMANDS.placeName("font", ci);

		ci = new CommandItem("filling", "filling {on|off}", (p,a)->setProperties(p,CanvasProperties.FONT,(String)a[0]), ArgumentType.name);
		COMMANDS.placeName("filling", ci);
	}
		
	public static String processCommand(final String command, final Predefines predef) throws SyntaxException, PaintScriptException {
		final char[]	cmd = CharUtils.terminateAndConvert2CharArray(command, '\n');
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
					from = CharUtils.extract(cmd, from, parameters, ci.lexemas);
					return ci.exec.process(predef, parameters);
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

	private static String drawRect(final Predefines predef, final int xFrom, final int yFrom, final int xTo, final int yTo) throws PaintScriptException {
		ImageUtils.draw(DrawingType.RECT, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage().getImage(), null
				, new Rectangle(xFrom, yFrom, xTo-xFrom, yTo-yFrom) 
				, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasForeground().getColor()
				, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasStroke().getStroke()); 
		return OK;
	}

	private static String drawEllipse(final Predefines predef, final int xFrom, final int yFrom, final int xTo, final int yTo, final String fill) throws PaintScriptException {
		ImageUtils.draw(DrawingType.ELLIPSE, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage().getImage(), null
					, new Rectangle(xFrom, yFrom, xTo-xFrom, yTo-yFrom)
					, "fill".equals(fill) 
							? new ColorPair(predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasForeground().getColor(), predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasBackground().getColor())
							: predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasForeground().getColor()
					, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasStroke().getStroke()); 
		return OK;
	}
	
	private static String drawText(final Predefines predef, final int xFrom, final int yFrom, final int xTo, final int yTo, final String text) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String drawPath(final Predefines predef, final String path) throws PaintScriptException { 
		try{ImageUtils.draw(DrawingType.PEN, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage().getImage(), null
						, SVGUtils.extractCommands(path)
						, predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getCanvasForeground().getColor()
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
		final String	command;
		final Object[]	lexemas;
		final String	help;
		final String	undoDescriptor;
		final String	redoDescriptor;
		final Executor	exec;
		
		public CommandItem(final String command, final String help, final Executor exec, final Object... lexemas) {
			this.command = command;
			this.lexemas = lexemas;
			this.help = help;
			this.undoDescriptor = null;
			this.redoDescriptor = null;
			this.exec = exec;
		}

		@Override
		public String toString() {
			return "CommandItem [command=" + command + ", lexemas=" + Arrays.toString(lexemas) + ", help=" + help + ", undoDescriptor=" + undoDescriptor + ", redoDescriptor=" + redoDescriptor + "]";
		}
	}
}
