package chav1961.bt.paint.script.interfaces;

import java.awt.Rectangle;

public interface RectWrapper {
	Rectangle getRect();
	RectWrapper setRect(String rect) throws ScriptException;
	
	static RectWrapper of(final Rectangle rect) {
		return null;
	}
	
	static RectWrapper of(final String rect) throws ScriptException {
		return null;
	}
}