package chav1961.bt.paint.script.interfaces;

import java.awt.Stroke;

import chav1961.bt.paint.interfaces.PaintScriptException;

public interface StrokeWrapper {
	Stroke getStroke();
	StrokeWrapper setStroke(String stroke) throws PaintScriptException;

	static StrokeWrapper of(final Stroke stroke) {
		return null;
	}
	
	static StrokeWrapper of(final String rect) throws PaintScriptException {
		return null;
	}
}