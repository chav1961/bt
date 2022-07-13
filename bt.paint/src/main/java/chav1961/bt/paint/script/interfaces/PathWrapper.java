package chav1961.bt.paint.script.interfaces;

import java.awt.geom.GeneralPath;

public interface PathWrapper {
	PathWrapper clear();
	PathWrapper cloneThis();
	PathWrapper setPath(String path) throws ScriptException;
	PathWrapper appendPath(String path) throws ScriptException;
	GeneralPath getPath();

	static PathWrapper of(final GeneralPath path) {
		return null;
	}
	
	static PathWrapper of(final String path) throws ScriptException {
		return null;
	}
}