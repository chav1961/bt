package chav1961.bt.paint.script.interfaces;

import java.awt.Point;

import chav1961.bt.paint.interfaces.PaintScriptException;

public interface SizeWrapper {
	Point getPoint();
	SizeWrapper setPoint(String point) throws PaintScriptException;

	static SizeWrapper of(final Point rect) {
		return null;
	}

	static SizeWrapper of(final String rect) throws PaintScriptException {
		return null;
	}
}