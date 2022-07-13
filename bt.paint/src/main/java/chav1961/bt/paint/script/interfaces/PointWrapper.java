package chav1961.bt.paint.script.interfaces;

import java.awt.Point;

public interface PointWrapper {
	Point getPoint();
	PointWrapper setPoint(String point) throws ScriptException;

	static PointWrapper of(final Point rect) {
		return null;
	}

	static PointWrapper of(final String rect) throws ScriptException {
		return null;
	}
}