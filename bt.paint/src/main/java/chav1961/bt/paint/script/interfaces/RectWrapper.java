package chav1961.bt.paint.script.interfaces;

import java.awt.Rectangle;

import chav1961.bt.paint.interfaces.PaintScriptException;

public interface RectWrapper {
	Rectangle getRect();
	RectWrapper setRect(String rect) throws PaintScriptException;
	
	static RectWrapper of(final Rectangle rect) {
		return null;
	}
	
	static RectWrapper of(final String rect) throws PaintScriptException {
		return null;
	}
}