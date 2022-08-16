package chav1961.bt.paint.script;

import java.awt.Font;
import java.awt.geom.AffineTransform;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.FontWrapper;

public class FontWrapperImpl implements FontWrapper {
	private Font	font;

	public FontWrapperImpl() {
		this(Font.decode(null));
	}
	
	public FontWrapperImpl(final Font font) {
		if (font == null) {
			throw new NullPointerException("Font can'tbe null");
		}
		else {
			this.font = font.deriveFont(new AffineTransform());
		}
	}

	public FontWrapperImpl(final String font) {
		if (font == null || font.isEmpty()) {
			throw new IllegalArgumentException("Font can'tbe null");
		}
		else {
			this.font = Font.decode(font);
		}
	}
	
	@Override
	public FontWrapper reset() {
		font = Font.decode(null);
		return this;
	}

	@Override
	public FontWrapper setFamily(final String family) {
		if (family == null || family.isEmpty()) {
			throw new IllegalArgumentException("Font family can't be null or empty");
		}
		else {
			font = new Font(family, font.getStyle(), font.getSize());
			return this;
		}
	}

	@Override
	public FontWrapper setSize(final int size) {
		if (size <= 0) {
			throw new IllegalArgumentException("Font size ["+size+"] must be positive"); 
		}
		else {
			font = font.deriveFont((float)size);
			return this;
		}
	}

	@Override
	public FontWrapper setStyle(final FontStyle style) {
		if (style== null) {
			throw new NullPointerException("Font style can't be null"); 
		}
		else {
			font = font.deriveFont(style.getStyle());
			return this;
		}
	}

	@Override
	public FontWrapper setFont(final FontWrapper fontWrapper) throws PaintScriptException {
		if (fontWrapper == null) {
			throw new NullPointerException("Font to set can't be null");
		}
		else {
			font = fontWrapper.getFont().deriveFont(new AffineTransform());
			return this;
		}
	}
	
	@Override
	public FontWrapper setFont(final String fontStr) throws PaintScriptException {
		if (fontStr == null || fontStr.isEmpty()) {
			throw new IllegalArgumentException("Font string can't be null or empty");
		}
		else {
			font = Font.decode(fontStr);
			return this;
		}
	}

	@Override
	public Font getFont() {
		return font;
	}

	@Override
	public Class<Font> getContentType() {
		return Font.class;
	}

	@Override
	public Font getContent() throws PaintScriptException {
		return getFont();
	}

	@Override
	public void setContent(final Font content) throws PaintScriptException {
		setFont(FontWrapper.of(content));
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new FontWrapperImpl(font);
	}

	@Override
	public String toString() {
		return "FontWrapperImpl [font=" + font + "]";
	}
}
