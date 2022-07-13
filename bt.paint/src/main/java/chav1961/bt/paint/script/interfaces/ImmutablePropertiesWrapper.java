package chav1961.bt.paint.script.interfaces;

import chav1961.bt.paint.interfaces.PaintScriptException;

public interface ImmutablePropertiesWrapper {
	String[] getPropKeys() throws PaintScriptException;
	boolean contains(String key) throws PaintScriptException;
	String get(String key) throws PaintScriptException;
	String get(String key, String defaultValue) throws PaintScriptException;
	<T> T get(String key, Class<T> awaited) throws PaintScriptException;
	<T> T get(String key, Class<T> awaited, T defaultValue) throws PaintScriptException;
}