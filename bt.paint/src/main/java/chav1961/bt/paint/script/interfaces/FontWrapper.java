package chav1961.bt.paint.script.interfaces;

import java.awt.Font;

import chav1961.bt.paint.interfaces.PaintScriptException;

public interface FontWrapper {
	public static enum FontStyle {
		PLAIN
	}
	
	FontWrapper reset();
	FontWrapper setFamily(String family);
	FontWrapper setSize(int size);
	FontWrapper setStyle(FontWrapper.FontStyle style);
	FontWrapper setFont(String font) throws PaintScriptException;
	Font getFont();

	static FontWrapper of(final Font font) {
		return null;
	}
	
	static FontWrapper of(final String font) throws PaintScriptException {
		return null;
	}
}