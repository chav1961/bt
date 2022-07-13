package chav1961.bt.paint.script.interfaces;

import java.awt.Point;

import chav1961.bt.paint.interfaces.PaintScriptException;

public interface PointWrapper {
	Point getPoint();
	PointWrapper setPoint(String point) throws PaintScriptException;

	static PointWrapper of(final Point rect) {
		return null;
	}

	static PointWrapper of(final String rect) throws PaintScriptException {
		return null;
	}
}