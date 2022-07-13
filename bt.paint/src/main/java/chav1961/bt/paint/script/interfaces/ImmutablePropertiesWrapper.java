package chav1961.bt.paint.script.interfaces;

public interface ImmutablePropertiesWrapper {
	String[] getPropKeys() throws ScriptException;
	boolean contains(String key) throws ScriptException;
	String get(String key) throws ScriptException;
	String get(String key, String defaultValue) throws ScriptException;
	<T> T get(String key, Class<T> awaited) throws ScriptException;
	<T> T get(String key, Class<T> awaited, T defaultValue) throws ScriptException;
}