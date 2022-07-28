package chav1961.bt.paint.script.interfaces;

import chav1961.bt.paint.control.Predefines;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.purelib.basic.exceptions.SyntaxException;

@FunctionalInterface
public interface ConsoleInterface {
	String console(String command, final Predefines predef) throws SyntaxException, PaintScriptException;
}
