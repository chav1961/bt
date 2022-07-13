package chav1961.bt.paint.script.interfaces;

import java.awt.Stroke;

public interface StrokeWrapper {
	Stroke getStroke();
	StrokeWrapper setStroke(String stroke) throws ScriptException;

	static StrokeWrapper of(final Stroke stroke) {
		return null;
	}
	
	static StrokeWrapper of(final String rect) throws ScriptException {
		return null;
	}
}