package chav1961.bt.paint.script.interfaces;

import java.awt.geom.AffineTransform;

import chav1961.bt.paint.interfaces.PaintScriptException;

public interface TransformWrapper {
	TransformWrapper clear();
	TransformWrapper move(int x, int y);
	TransformWrapper scale(int sx, int sy);
	TransformWrapper rotate(int angle);
	TransformWrapper setTransform(String transform) throws PaintScriptException;
	AffineTransform getTransform();
	
	static TransformWrapper of(final AffineTransform transform) {
		return null;
	}
	
	static TransformWrapper of(final String transform) throws PaintScriptException {
		return null;
	}
}