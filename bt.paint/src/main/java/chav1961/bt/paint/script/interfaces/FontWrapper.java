package chav1961.bt.paint.script.interfaces;

import java.awt.Font;

public interface FontWrapper {
	public static enum FontStyle {
		PLAIN
	}
	
	FontWrapper reset();
	FontWrapper setFamily(String family);
	FontWrapper setSize(int size);
	FontWrapper setStyle(FontWrapper.FontStyle style);
	FontWrapper setFont(String font) throws ScriptException;
	Font getFont();

	static FontWrapper of(final Font font) {
		return null;
	}
	
	static FontWrapper of(final String font) throws ScriptException {
		return null;
	}
}