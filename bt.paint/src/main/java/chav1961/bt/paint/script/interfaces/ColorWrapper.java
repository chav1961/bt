package chav1961.bt.paint.script.interfaces;

import java.awt.Color;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.ColorWrapperImpl;

public interface ColorWrapper {
	Color getColor();
	ColorWrapper setColor(ColorWrapper color);
	ColorWrapper setColor(String color) throws PaintScriptException;

	static ColorWrapper of(final Color color) {
		if (color == null) {
			throw new NullPointerException("Color to get wrapper for can't be null");
		}
		else {
			return new ColorWrapperImpl(color);
		}
	}
	
	static ColorWrapper of(final String colorStr) throws PaintScriptException {
		if (colorStr == null || colorStr.isEmpty()) {
			throw new IllegalArgumentException("Color string can't be null or empty");
		}
		else {
			return new ColorWrapperImpl().setColor(colorStr);
		}
	}
}