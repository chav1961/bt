package chav1961.bt.paint.script.interfaces;

import java.awt.Color;

public interface ColorWrapper {
	Color getColor();
	ColorWrapper setColor(String color) throws ScriptException;

	static ColorWrapper of(final Color color) {
		return null;
	}
	
	static ColorWrapper of(final String color) throws ScriptException {
		return null;
	}
}