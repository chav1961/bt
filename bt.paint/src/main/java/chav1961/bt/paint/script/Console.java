package chav1961.bt.paint.script;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;

import chav1961.bt.paint.control.ImageUtils;
import chav1961.bt.paint.control.Predefines;
import chav1961.bt.paint.control.ImageUtils.DrawingType;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.CanvasWrapper;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.CharUtils.ArgumentType;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

// line (x,y) (x,y)
// rect (x,y) (x,y)
// ellipse (x,y) (x,y)
// text (x,y) (x,y) <text>
// path <path>
// fill (x,y) {#nnn | <name> }
// stroke N [{solid|dashed|dotted}] [{butt|}] [{|}]
// fore {(x,y) | #nnn |<name> }
// back {(x,y) | #nnn |<name> }
// filling {on|off}
// font <name> N [{bold|italic}]
// ? <request>
// rotate
// mirror {h|v}
// scale {(x,y) | N} 
// resize {(x,y) | N}
// gray
// undo
// redo
// trans [!]{(x,y) | #nnn |<name> }
// select (x,y) (x,y)
// copy
// erase
// paste (x,y)
// save [<name>]
// load <name>
// new (x,y) {type}
// quit [!]
// play <name>
class Console {
	private static final SyntaxTreeInterface<CommandItem>	COMMANDS = new AndOrTree<>();
	private static final String	OK = "ok";
	
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
	}
	
	
	static String processCommand(final String command, final Predefines predef) throws SyntaxException, PaintScriptException {
		final char[]	cmd = CharUtils.terminateAndConvert2CharArray(command, '\n');
		final int[]		bounds = new int[2];
		final Object[]	parameters = new Object[100];
		int				from = CharUtils.skipBlank(cmd, 0, true);
		
		from = CharUtils.parseName(cmd, from, bounds);
		final long		cmdId = COMMANDS.seekName(cmd, bounds[0], bounds[1]);
		
		if (cmdId < 0) {
			throw new SyntaxException(0, from, "Unknown command");
		}
		else {
			final CommandItem	ci = COMMANDS.getCargo(cmdId);
			
			if (CharUtils.tryExtract(cmd, from, ci.lexemas) >= 0) {
				from = CharUtils.extract(cmd, from, parameters, ci.lexemas);
				ci.exec.process(predef, parameters);
			}
			else {
				throw new SyntaxException(0, from, "Illegal format for command ["+ci.command+"] : must be");
			}
		}
		
		return null;
	}
	
	private static String drawLine(final Predefines predef, final int xFrom, final int yFrom, final int xTo, final int yTo) throws PaintScriptException {
		ImageUtils.draw(DrawingType.LINE, predef.getPredefined("canvas", CanvasWrapper.class).getImage().getImage(), null, new Point(xFrom, yFrom), new Point(xTo, yTo)); 
		return OK;
	}

	private static String drawRect(final Predefines predef, final int xFrom, final int yFrom, final int xTo, final int yTo) throws PaintScriptException {
		ImageUtils.draw(DrawingType.RECT, predef.getPredefined("canvas", CanvasWrapper.class).getImage().getImage(), null, new Rectangle(xFrom, yFrom, xTo-xFrom, yTo-yFrom)); 
		return OK;
	}

	private static String drawEllipse(final Predefines predef, final int xFrom, final int yFrom, final int xTo, final int yTo) throws PaintScriptException {
		ImageUtils.draw(DrawingType.ELLIPSE, predef.getPredefined("canvas", CanvasWrapper.class).getImage().getImage(), null, new Rectangle(xFrom, yFrom, xTo-xFrom, yTo-yFrom)); 
		return OK;
	}
	
	private static String drawText(final Predefines predef, final int xFrom, final int yFrom, final int xTo, final int yTo, final String text) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String drawPath(final Predefines predef, final String path) throws PaintScriptException { 
		ImageUtils.draw(DrawingType.PEN, predef.getPredefined("canvas", CanvasWrapper.class).getImage().getImage(), null); 
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
