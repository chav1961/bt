package chav1961.bt.installbuilder.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;

public class Settings {
	private static final String		SETTINGS_XPATH = "/scenario/settings/value";
	private static final String		PARAMETER_NAME = "name";
	
	private final Properties		settings = new Properties();
	
	private Settings() {
	}
	
	private void addParameter(final String name, final String value) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Settings name to add can't be null or empty");
		}
		else if (value == null) {
			throw new NullPointerException("Settings value to add can't be null");
		}
		else {
			settings.setProperty(name, value);
		}
	}
	
	public boolean hasParameter(final String name) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Settings name can't be null or empty");
		}
		else {
			return settings.containsKey(name);
		}
	}
	
	public String getValue(final String name) throws IOException {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Settings name can't be null or empty");
		}
		else if (!hasParameter(name)) {
			throw new IllegalArgumentException("Settings name ["+name+"] is missing in the parameters list");
		}
		else {
			return settings.getProperty(name);
		}
	}

	public String getValue(final String name, final String defaultValue) throws IOException {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Parameter name can't be null or empty");
		}
		else {
			return settings.getProperty(name, defaultValue);
		}
	}
	
	public Iterable<String> names() {
		final List<String>	result = new ArrayList<>();
		
		for(Entry<Object, Object> item  : settings.entrySet()) {
			result.add(item.getKey().toString());
		}
		return result;
	}
	
	public Properties toProperties() {
		return settings;
	}
	
	public InputStream getInputStream() throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			toProperties().store(baos, "");
			baos.flush();
			return new ByteArrayInputStream(baos.toByteArray());			
		}
	}
	
	public static Settings fromXML(final Document doc) throws ContentException {
		if (doc == null) {
			throw new NullPointerException("XML document can't be null");
		}
		else {
			try {
				final Settings	result = new Settings();
				final XPath 		xPath = XPathFactory.newInstance().newXPath();
				final NodeList		nodes = (NodeList) xPath.compile(SETTINGS_XPATH).evaluate(doc, XPathConstants.NODESET);
				
				for (int index = 0; index < nodes.getLength(); index++) {
					final Node		node = nodes.item(index);
					final String	name = node.getAttributes().getNamedItem(PARAMETER_NAME).getTextContent();
					final String	value = node.getTextContent();
					
					if (!result.hasParameter(name)) {
						result.addParameter(name, value);
					}
					else {
						throw new ContentException("Error in ["+SETTINGS_XPATH+"] section: duplicate settings value ["+name+"]");
					}
				}
				return result;
			} catch (XPathExpressionException | DOMException e) {
				throw new ContentException(e.getLocalizedMessage(), e);
			}
		}
	}
}
