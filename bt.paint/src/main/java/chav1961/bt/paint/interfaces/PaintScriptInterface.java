package chav1961.bt.paint.interfaces;

import java.awt.Image;

import chav1961.purelib.basic.exceptions.SyntaxException;

public interface PaintScriptInterface extends Appendable, CharSequence, AutoCloseable {
	void reset();
	void compile() throws SyntaxException;
	boolean isCompiled();
	Image processImage(Image image) throws PaintScriptException;
	void close() throws RuntimeException;
}
