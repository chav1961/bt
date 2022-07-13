package chav1961.bt.paint.script.interfaces;

import java.awt.Color;

import chav1961.bt.paint.interfaces.PaintScriptException;

public interface ColorWrapper {
	Color getColor();
	ColorWrapper setColor(String color) throws PaintScriptException;

	static ColorWrapper of(final Color color) {
		return null;
	}
	
	static ColorWrapper of(final String color) throws PaintScriptException {
		return null;
	}
}