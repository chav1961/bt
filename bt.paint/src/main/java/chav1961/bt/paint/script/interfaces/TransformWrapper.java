package chav1961.bt.paint.script.interfaces;

import java.awt.geom.AffineTransform;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.TransformWrapperImpl;

public interface TransformWrapper extends ContentWrapper<AffineTransform> {
	TransformWrapper clear();
	TransformWrapper move(int x, int y);
	TransformWrapper scale(int sx, int sy);
	TransformWrapper rotate(int angle);
	TransformWrapper setTransform(AffineTransform transform) throws PaintScriptException;
	TransformWrapper setTransform(String transform) throws PaintScriptException;
	AffineTransform getTransform();
	
	static TransformWrapper of(final AffineTransform transform) {
		return new TransformWrapperImpl(transform);
	}
	
	static TransformWrapper of(final String transform) throws PaintScriptException {
		return new TransformWrapperImpl(transform);
	}
}