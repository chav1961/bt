package chav1961.bt.paint.script.interfaces;

import java.awt.Rectangle;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.RectWrapperImpl;

public interface RectWrapper {
	Rectangle getRect();
	RectWrapper setRect(Rectangle rect) throws PaintScriptException;
	RectWrapper setRect(String rect) throws PaintScriptException;
	
	static RectWrapper of(final Rectangle rect) {
		if (rect == null) {
			throw new NullPointerException("Rectangel to wrap can't be null");
		}
		else {
			return new RectWrapperImpl(rect);
		}
	}
	
	static RectWrapper of(final String rect) throws PaintScriptException {
		if (rect == null || rect.isEmpty()) {
			throw new IllegalArgumentException("Rectangle string can't be null or empty"); 
		}
		else {
			return new RectWrapperImpl(rect);
		}
	}
}