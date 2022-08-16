package chav1961.bt.paint.script.interfaces;

import java.awt.Point;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.PointWrapperImpl;

public interface PointWrapper extends ContentWrapper<Point> {
	Point getPoint();
	PointWrapper setPoint(String point) throws PaintScriptException;
	PointWrapper setPoint(Point point) throws PaintScriptException;

	static PointWrapper of(final Point point) {
		return new PointWrapperImpl(point);
	}

	static PointWrapper of(final String point) throws PaintScriptException {
		return new PointWrapperImpl(point);
	}
}