package chav1961.bt.paint.script;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;

import chav1961.bt.paint.control.ImageUtils;
import chav1961.bt.paint.control.ImageUtils.DrawingType;
import chav1961.bt.paint.control.Predefines;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.CanvasWrapper;
import chav1961.bt.paint.script.interfaces.ColorWrapper;
import chav1961.bt.paint.script.interfaces.FontWrapper;
import chav1961.bt.paint.script.interfaces.StrokeWrapper;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.CharUtils.ArgumentType;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
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
class Console {
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

		ci = new CommandItem("ellipse", "ellipse <xFrom::int>,<yFrom::int> <xTo::int>,<yTo::int>", 
				(p,a)->drawEllipse(p,(Integer)a[0],(Integer)a[1],(Integer)a[2],(Integer)a[3]),
				ArgumentType.signedInt, ',', ArgumentType.signedInt, ArgumentType.signedInt, ',', ArgumentType.signedInt);
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

		ci = new CommandItem("fill", "fill <x::int>,<y::int> <color::color>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.raw);
		COMMANDS.placeName("fill", ci);

		ci = new CommandItem("rotate", "rotate <angle::int>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.signedInt);
		COMMANDS.placeName("rotate", ci);
		COMMANDS.placeName("rot", ci);
		
		ci = new CommandItem("mirror", "mirror {h|v}", (p,a)->drawPath(p,(String)a[0]), ArgumentType.name);
		COMMANDS.placeName("mirror", ci);
		COMMANDS.placeName("mirror", ci);

		ci = new CommandItem("scale", "scale <sx::int>[,<sy::int>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.signedInt);
		COMMANDS.placeName("scale", ci);

		ci = new CommandItem("resize", "resize <x::int>[,<y::int>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.signedInt);
		COMMANDS.placeName("resize", ci);

		ci = new CommandItem("gray", "gray", (p,a)->drawPath(p,(String)a[0]));
		COMMANDS.placeName("gray", ci);

		ci = new CommandItem("undo", "undo", (p,a)->drawPath(p,(String)a[0]));
		COMMANDS.placeName("undo", ci);
		COMMANDS.placeName("u", ci);

		ci = new CommandItem("redo", "redo", (p,a)->drawPath(p,(String)a[0]));
		COMMANDS.placeName("redo", ci);

		ci = new CommandItem("trans", "trans <x::int>,<y::int> <color::color>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.signedInt, ArgumentType.signedInt, ArgumentType.colorRepresentation);
		COMMANDS.placeName("trans", ci);

		ci = new CommandItem("select", "select <xFrom::int>,<yFrom::int> <xTo::int>,<yTo::int>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.signedInt, ArgumentType.signedInt, ArgumentType.signedInt, ArgumentType.signedInt);
		COMMANDS.placeName("select", ci);
		COMMANDS.placeName("sel", ci);

		ci = new CommandItem("copy", "copy", (p,a)->drawPath(p,(String)a[0]));
		COMMANDS.placeName("copy", ci);
		COMMANDS.placeName("cp", ci);

		ci = new CommandItem("erase", "erase", (p,a)->drawPath(p,(String)a[0]));
		COMMANDS.placeName("erase", ci);
		
		ci = new CommandItem("paste", "paste <xFrom::int>,<yFrom::int> [<xTo::int>,<yTo::int>] [<name::string>]", (p,a)->drawPath(p,(String)a[0]), ArgumentType.signedInt, ArgumentType.signedInt, ArgumentType.signedInt, ArgumentType.signedInt, ArgumentType.raw);
		COMMANDS.placeName("paste", ci);

		ci = new CommandItem("save", "save [<name::string>]", (p,a)->drawPath(p,(String)a[0]), ArgumentType.raw);
		COMMANDS.placeName("save", ci);

		ci = new CommandItem("load", "load <name::string>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.raw);
		COMMANDS.placeName("load", ci);

		ci = new CommandItem("new", "new <width::int>,<height::int> <type::any>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.signedInt, ArgumentType.signedInt, ArgumentType.raw);
		COMMANDS.placeName("new", ci);

		ci = new CommandItem("play", "play <name::string>", (p,a)->drawPath(p,(String)a[0]), ArgumentType.raw);
		COMMANDS.placeName("play", ci);

		ci = new CommandItem("quit", "quit", (p,a)->drawPath(p,(String)a[0]));
		COMMANDS.placeName("quit", ci);
		COMMANDS.placeName("q", ci);

		ci = new CommandItem("qquit", "qquit", (p,a)->drawPath(p,(String)a[0]));
		COMMANDS.placeName("qquit", ci);
		COMMANDS.placeName("qq", ci);
		
		ci = new CommandItem("foreground", "foreground <color::color>", (p,a)->setProperties(p,CanvasProperties.FORE_COLOR,(Color)a[0]), ArgumentType.colorRepresentation);
		COMMANDS.placeName("foreground", ci);
		COMMANDS.placeName("fore", ci);

		ci = new CommandItem("background", "background <color::colot>", (p,a)->setProperties(p,CanvasProperties.BACK_COLOR,(Color)a[0]), ArgumentType.colorRepresentation);
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
	
	
	static String processCommand(final String command, final Predefines predef) throws SyntaxException, PaintScriptException {
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
			
			if (CharUtils.tryExtract(cmd, from, ci.lexemas) >= 0) {
				from = CharUtils.extract(cmd, from, parameters, ci.lexemas);
				return ci.exec.process(predef, parameters);
			}
			else {
				throw new SyntaxException(0, from, "Illegal format for command ["+ci.command+"] : must be <"+ci.help+">");
			}
		}
		
	}
	
	private static String drawLine(final Predefines predef, final int xFrom, final int yFrom, final int xTo, final int yTo) throws PaintScriptException {
		ImageUtils.draw(DrawingType.LINE, predef.getPredefined("canvas", CanvasWrapper.class).getImage().getImage(), null
					, new Point(xFrom, yFrom), new Point(xTo, yTo)
					, predef.getPredefined("canvas", CanvasWrapper.class).getCanvasForeground().getColor()
					, predef.getPredefined("canvas", CanvasWrapper.class).getCanvasStroke().getStroke()); 
		return OK;
	}

	private static String drawRect(final Predefines predef, final int xFrom, final int yFrom, final int xTo, final int yTo) throws PaintScriptException {
		ImageUtils.draw(DrawingType.RECT, predef.getPredefined("canvas", CanvasWrapper.class).getImage().getImage(), null
				, new Rectangle(xFrom, yFrom, xTo-xFrom, yTo-yFrom) 
				, predef.getPredefined("canvas", CanvasWrapper.class).getCanvasForeground().getColor()
				, predef.getPredefined("canvas", CanvasWrapper.class).getCanvasStroke().getStroke()); 
		return OK;
	}

	private static String drawEllipse(final Predefines predef, final int xFrom, final int yFrom, final int xTo, final int yTo) throws PaintScriptException {
		ImageUtils.draw(DrawingType.ELLIPSE, predef.getPredefined("canvas", CanvasWrapper.class).getImage().getImage(), null
					, new Rectangle(xFrom, yFrom, xTo-xFrom, yTo-yFrom)
					, predef.getPredefined("canvas", CanvasWrapper.class).getCanvasForeground().getColor()
					, predef.getPredefined("canvas", CanvasWrapper.class).getCanvasStroke().getStroke()); 
		return OK;
	}
	
	private static String drawText(final Predefines predef, final int xFrom, final int yFrom, final int xTo, final int yTo, final String text) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String drawPath(final Predefines predef, final String path) throws PaintScriptException { 
		try{ImageUtils.draw(DrawingType.PEN, predef.getPredefined("canvas", CanvasWrapper.class).getImage().getImage(), null
						, SVGUtils.extractCommands(path)
						, predef.getPredefined("canvas", CanvasWrapper.class).getCanvasForeground().getColor()
						, predef.getPredefined("canvas", CanvasWrapper.class).getCanvasStroke().getStroke());
			return OK;
		} catch (SyntaxException e) {
			throw new PaintScriptException(e);
		} 
	}

	private static String setProperties(final Predefines predef, final CanvasProperties props, final Object content) throws PaintScriptException {
		switch(props) {
			case BACK_COLOR	:
				predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setCanvasBackground(ColorWrapper.of((Color)content));
				break;
			case FORE_COLOR	:
				predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setCanvasForeground(ColorWrapper.of((Color)content));
				break;
			case FONT		:
				predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setCanvasFont(FontWrapper.of((String)content));
				break;
			case STROKE		:
				predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setCanvasStroke(StrokeWrapper.of((String)content));
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
		final Executor	exec;
		
		public CommandItem(final String command, final String help, final Executor exec, final Object... lexemas) {
			this.command = command;
			this.lexemas = lexemas;
			this.help = help;
			this.exec = exec;
		}

		@Override
		public String toString() {
			return "CommandItem [command=" + command + ", lexemas=" + Arrays.toString(lexemas) + ", help=" + help + "]";
		}
	}
}
