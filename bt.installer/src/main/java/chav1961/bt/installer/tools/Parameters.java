package chav1961.bt.installer.tools;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

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

public class Parameters {
	private static final String		PARAMETERS_XPATH = "/scenario/parameters/parameter";
	private static final String		PARAMETER_NAME = "name";
	private static final String		PARAMETER_TYPE = "type";
	
	private final Map<String, Object>	parameters = new HashMap<>();
	
	private Parameters() {
	}
	
	private void addParameter(final String name, final Object value) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Parameter name to add can't be null or empty");
		}
		else if (value == null) {
			throw new NullPointerException("Parameter value to add can't be null");
		}
		else {
			parameters.put(name, value);
		}
	}
	
	public boolean hasParameter(final String name) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Parameter name can't be null or empty");
		}
		else {
			return parameters.containsKey(name);
		}
	}
	
	public <T> T getValue(final String name, final Class<T> awaited) throws IOException {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Parameter name can't be null or empty");
		}
		else if (!hasParameter(name)) {
			throw new IllegalArgumentException("Parameter name ["+name+"] is missing in the parameters list");
		}
		else {
			return awaited.cast(parameters.get(name));
		}
	}

	public <T> T getValue(final String name, final Class<T> awaited, final T defaultValue) throws IOException {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Parameter name can't be null or empty");
		}
		else if (!hasParameter(name)) {
			return defaultValue;
		}
		else {
			return awaited.cast(parameters.get(name));
		}
	}
	
	public Iterable<String> names() {
		return parameters.keySet();
	}
	
	public static Parameters fromXML(final Document doc) throws ContentException {
		if (doc == null) {
			throw new NullPointerException("XML document can't be null");
		}
		else {
			try {
				final Parameters	result = new Parameters();
				final XPath 		xPath = XPathFactory.newInstance().newXPath();
				final NodeList		nodes = (NodeList) xPath.compile(PARAMETERS_XPATH).evaluate(doc, XPathConstants.NODESET);
				
				for (int index = 0; index < nodes.getLength(); index++) {
					final Node		node = nodes.item(index);
					final String	name = node.getAttributes().getNamedItem(PARAMETER_NAME).getTextContent();
					final String	type = node.getAttributes().getNamedItem(PARAMETER_TYPE).getTextContent();
					final String	value = node.getTextContent();
					
					if (!result.hasParameter(name)) {
						if (String.class.getName().equals(type)) {
							result.addParameter(name, value);
						}
						else {
							result.addParameter(name, Class.forName(type).getMethod("valueOf", String.class).invoke(null, value));
						}
					}
					else {
						throw new ContentException("Error in ["+PARAMETERS_XPATH+"] section: duplicate image name ["+name+"]");
					}
				}
				return result;
			} catch (XPathExpressionException | DOMException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
				throw new ContentException(e.getLocalizedMessage(), e);
			}
		}
	}
}
