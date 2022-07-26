package chav1961.bt.paint.script.interfaces;

import java.awt.Font;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.FontWrapperImpl;

public interface FontWrapper {
	public static enum FontStyle {
		PLAIN(Font.PLAIN),
		BOLD(Font.BOLD),
		ITALIC(Font.ITALIC),
		BOLD_ITALIC(Font.BOLD | Font.ITALIC);
		
		private final int	style;
		
		private FontStyle(final int style) {
			this.style = style;
		}
		
		public int getStyle() {
			return style;
		}
		
		public static FontStyle of(final int style) {
			for (FontStyle item : FontStyle.values()) {
				if (item.getStyle() == style) {
					return item;
				}
			}
			throw new IllegalArgumentException("Font style ["+style+"] not found"); 
		}
	}
	
	FontWrapper reset();
	FontWrapper setFamily(String family);
	FontWrapper setSize(int size);
	FontWrapper setStyle(FontWrapper.FontStyle style);
	FontWrapper setFont(FontWrapper font) throws PaintScriptException;
	FontWrapper setFont(String font) throws PaintScriptException;
	Font getFont();

	static FontWrapper of(final Font font) {
		if (font == null) {
			throw new NullPointerException("Font to get font for can't be null");
		}
		else {
			return new FontWrapperImpl(font);
		}
	}
	
	static FontWrapper of(final String font) throws PaintScriptException {
		if (font == null || font.isEmpty()) {
			throw new IllegalArgumentException("String to get font for can't be null or empty");
		}
		else {
			return new FontWrapperImpl().setFont(font);
		}
	}
}