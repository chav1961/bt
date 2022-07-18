package chav1961.bt.paint.script;

import java.util.Properties;
import java.util.Set;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.PropertiesWrapper;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.sql.SQLUtils;

public class PropertiesWrapperImpl implements PropertiesWrapper {
	private final SubstitutableProperties	props = new SubstitutableProperties();
	
	public PropertiesWrapperImpl() {
	}

	public PropertiesWrapperImpl(final SubstitutableProperties props) {
		if (props == null) {
			throw new NullPointerException("Properties to wrap can't be null");
		}
		else {
			for (String item : props.availableKeys()) {
				this.props.put(item, props.getProperty(item));
			}
		}
	}
	
	@Override
	public String[] getPropKeys() throws PaintScriptException {
		final Set<String>	result = props.availableKeys();
		
		return result.toArray(new String[result.size()]);
	}

	@Override
	public boolean contains(final String key) throws PaintScriptException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to test can't be null or empty"); 
		}
		else {
			return props.containsKey(key);
		}
	}

	@Override
	public String get(final String key) throws PaintScriptException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get can't be null or empty"); 
		}
		else if (!contains(key)) {
			throw new PaintScriptException("Key ["+key+"] doesn't exists"); 
		}
		else {
			return props.getProperty(key);
		}
	}

	@Override
	public String get(final String key, final String defaultValue) throws PaintScriptException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get can't be null or empty"); 
		}
		else if (defaultValue == null) {
			throw new NullPointerException("Defauly value can't be null"); 
		}
		else {
			return props.getProperty(key, defaultValue);
		}
	}

	@Override
	public <T> T get(final String key, final Class<T> awaited) throws PaintScriptException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get can't be null or empty"); 
		}
		else if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null"); 
		}
		else if (!contains(key)) {
			throw new PaintScriptException("Key ["+key+"] doesn't exists"); 
		}
		else {
			try {
				return props.getProperty(key, awaited);
			} catch (UnsupportedOperationException exc) {
				throw new PaintScriptException(exc.getLocalizedMessage(), exc); 
			}
		}
	}

	@Override
	public <T> T get(String key, Class<T> awaited, T defaultValue) throws PaintScriptException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get can't be null or empty"); 
		}
		else if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null"); 
		}
		else if (!contains(key)) {
			return defaultValue; 
		}
		else {
			try {
				return props.getProperty(key, awaited);
			} catch (UnsupportedOperationException exc) {
				throw new PaintScriptException(exc.getLocalizedMessage(), exc); 
			}
		}
	}

	@Override
	public Properties getProperties() {
		return props;
	}

	@Override
	public void set(final String key, final String value) throws PaintScriptException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to set can't be null or empty"); 
		}
		else if (value == null) {
			throw new NullPointerException("Value to set can't be null"); 
		}
		else {
			props.put(key, value);
		}
	}

	@Override
	public <T> void set(final String key, final Class<T> awaited, final T value) throws PaintScriptException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to set can't be null or empty"); 
		}
		else if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null"); 
		}
		else if (value == null) {
			throw new NullPointerException("Value to set can't be null"); 
		}
		else {
			try{props.put(key, SQLUtils.convert(String.class, value));
			} catch (ContentException exc) {
				throw new PaintScriptException("Error converting class ["+awaited.getCanonicalName()+"], instance value ["+value+"] to string"); 
			}
		}
		
	}
}
