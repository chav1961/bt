package chav1961.bt.paint.script.interfaces;

import java.awt.geom.GeneralPath;

import chav1961.bt.paint.interfaces.PaintScriptException;

public interface PathWrapper {
	PathWrapper clear();
	PathWrapper cloneThis();
	PathWrapper setPath(String path) throws PaintScriptException;
	PathWrapper appendPath(String path) throws PaintScriptException;
	GeneralPath getPath();

	static PathWrapper of(final GeneralPath path) {
		return null;
	}
	
	static PathWrapper of(final String path) throws PaintScriptException {
		return null;
	}
}