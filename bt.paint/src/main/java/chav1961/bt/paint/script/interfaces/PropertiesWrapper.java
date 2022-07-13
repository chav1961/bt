package chav1961.bt.paint.script.interfaces;

import java.util.Properties;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.purelib.basic.SubstitutableProperties;

public interface PropertiesWrapper extends ImmutablePropertiesWrapper {
	Properties getProperties();
	void set(String key, String value) throws PaintScriptException;
	<T> void set(String key, Class<T> awaited, T value) throws PaintScriptException;
	
	static PropertiesWrapper of(final Properties props) {
		final SubstitutableProperties	result = new SubstitutableProperties(props);
		return null;
	}
	
	static PropertiesWrapper of(final String props) throws PaintScriptException {
		return null;
	}
}