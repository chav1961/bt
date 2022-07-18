package chav1961.bt.paint.script.interfaces;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.PropertiesWrapperImpl;
import chav1961.purelib.basic.SubstitutableProperties;

public interface PropertiesWrapper extends ImmutablePropertiesWrapper {
	Properties getProperties();
	void set(String key, String value) throws PaintScriptException;
	<T> void set(String key, Class<T> awaited, T value) throws PaintScriptException;
	
	static PropertiesWrapper of(final Properties props) {
		if (props == null) {
			throw new NullPointerException("Properties can't be null"); 
		}
		else {
			return new PropertiesWrapperImpl(new SubstitutableProperties(props));
		}
	}
	
	static PropertiesWrapper of(final String props) throws PaintScriptException {
		if (props == null) {
			throw new NullPointerException("Properties can't be null"); 
		}
		else {
			final Properties	temp = new Properties();
			
			try(final Reader	rdr = new StringReader(props)) {
				
				temp.load(rdr);
			} catch (IOException e) {
				throw new PaintScriptException(e.getLocalizedMessage());
			}
			return of(temp);
		}
	}
}