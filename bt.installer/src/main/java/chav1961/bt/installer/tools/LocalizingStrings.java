package chav1961.bt.installer.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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

public class LocalizingStrings {
	private static final String		I18N_XPATH = "/scenario/i18n/lang";
	private static final String		I18N_LANG_XPATH = "/scenario/i18n/lang[@name='$']/item";
	private static final String		I18N_LANG_NAME = "name";
	private static final String		I18N_STRING_ID = "id";

	private final Map<String, Map<String, String>>	strings = new HashMap<>();
	
	private LocalizingStrings() {
		
	}

	private void addLang(final String lang) {
		if (Utils.checkEmptyOrNullString(lang)) {
			throw new IllegalArgumentException("Language to add can't be null or empty");
		}
		else {
			strings.put(lang, new HashMap<>());
		}
	}
	
	private void addItem(final String lang, final String id, final String value) {
		if (Utils.checkEmptyOrNullString(lang)) {
			throw new IllegalArgumentException("Language name to add can't be null or empty");
		}
		else if (Utils.checkEmptyOrNullString(id)) {
			throw new IllegalArgumentException("String id to add can't be null or empty");
		}
		else if (Utils.checkEmptyOrNullString(value)) {
			throw new IllegalArgumentException("String value to add can't be null or empty");
		}
		else {
			strings.get(lang).put(id, value);
		}
	}
	
	public boolean hasLanguage(final String lang) {
		if (Utils.checkEmptyOrNullString(lang)) {
			throw new IllegalArgumentException("Language can't be null or empty");
		}
		else {
			return strings.containsKey(lang);
		}
	}

	public Iterable<String> languages() {
		return strings.keySet();
	}
	
	public boolean hasString(final String lang, final String id) {
		if (Utils.checkEmptyOrNullString(lang)) {
			throw new IllegalArgumentException("Language can't be null or empty");
		}
		else if (Utils.checkEmptyOrNullString(id)) {
			throw new IllegalArgumentException("String id can't be null or empty");
		}
		else {
			return strings.containsKey(lang) && strings.get(lang).containsKey(id);
		}
	}

	public Iterable<String> ids(final String lang) {
		if (Utils.checkEmptyOrNullString(lang)) {
			throw new IllegalArgumentException("Language can't be null or empty");
		}
		else if (!hasLanguage(lang)) {
			throw new IllegalArgumentException("Language ["+lang+"] is missing in the parameters list");
		}
		else {
			return strings.get(lang).keySet();
		}
	}
	
	public boolean hasString(final String id) {
		if (Utils.checkEmptyOrNullString(id)) {
			throw new IllegalArgumentException("String id can't be null or empty");
		}
		else {
			for (String item : languages()) {
				if (hasString(item, id)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public String getValue(final String lang, final String id) throws IOException {
		if (Utils.checkEmptyOrNullString(lang)) {
			throw new IllegalArgumentException("Language can't be null or empty");
		}
		else if (Utils.checkEmptyOrNullString(id)) {
			throw new IllegalArgumentException("String id can't be null or empty");
		}
		else if (!hasLanguage(lang)) {
			throw new IllegalArgumentException("Language ["+lang+"] is missing in the parameters list");
		}
		else if (!hasString(lang, id)) {
			throw new IllegalArgumentException("String ["+id+"] for language ["+lang+"] is missing in the parameters list");
		}
		else {
			return strings.get(lang).get(id);
		}
	}

	public String getValue(final String id) throws IOException {
		if (Utils.checkEmptyOrNullString(id)) {
			throw new IllegalArgumentException("String id can't be null or empty");
		}
		else {
			for (String item : languages()) {
				if (hasString(item, id)) {
					return getValue(item, id);
				}
			}
			throw new IllegalArgumentException("String id ["+id+"] is missing in the parameters list");
		}
	}

	public InputStream getInputStream() throws IOException {
		final Properties	result = new Properties();
		
		for(String lang : languages()) {
			for(String id : ids(lang)) {
				result.setProperty(lang+":"+id, getValue(lang, id));
			}
		}
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			result.store(baos, "");
			baos.flush();
			
			return new ByteArrayInputStream(baos.toByteArray());
		}
	}
	
	
	public static LocalizingStrings fromXML(final Document doc) throws ContentException {
		if (doc == null) {
			throw new NullPointerException("XML document can't be null");
		}
		else {
			try {
				final LocalizingStrings	result = new LocalizingStrings();
				final XPath 		xPath = XPathFactory.newInstance().newXPath();
				final NodeList		nodes = (NodeList) xPath.compile(I18N_XPATH).evaluate(doc, XPathConstants.NODESET);
				final Set<String>[]	idSets = new Set[nodes.getLength()];
				final String[]		langs = new String[nodes.getLength()];						
				
				for (int index = 0; index < nodes.getLength(); index++) {
					final Node		node = nodes.item(index);
					final String	lang = node.getAttributes().getNamedItem(I18N_LANG_NAME).getTextContent();
					
					if (!result.hasLanguage(lang)) {
						result.addLang(lang);
						final NodeList	items = (NodeList) xPath.compile(I18N_LANG_XPATH.replace("$", lang)).evaluate(doc, XPathConstants.NODESET);
						
						for (int itemIndex = 0; itemIndex < items.getLength(); itemIndex++) {
							final Node		itemNode = items.item(itemIndex);
							final String	id  = itemNode.getAttributes().getNamedItem(I18N_STRING_ID).getTextContent();
							final String	value  = itemNode.getTextContent();
							
							if (!result.hasString(lang, id)) {
								result.addItem(lang, id, value);
							}
							else {
								throw new ContentException("Error in ["+I18N_LANG_XPATH.replace("$", lang)+"] section: duplicate string id ["+id+"]");
							}
						}						
					}
					else {
						throw new ContentException("Error in ["+I18N_XPATH+"] section: duplicate language name ["+lang+"]");
					}
					idSets[index] = toSet(result.ids(lang));
					langs[index] = lang;
				}
				if (idSets.length > 1) {
					boolean	allEquals = true;
					
					for(int index = 1; index < idSets.length; index++) {
						if (!idSets[0].equals(idSets[index])) {
							allEquals = false;
							break;
						}
					}
					if (!allEquals) {
						final StringBuilder	errors = new StringBuilder();
						final Set<String>	commonSet = new HashSet<>(idSets[0]);
						
						for(Set<String> item : idSets) {
							commonSet.retainAll(item);
						}
						for(int index = 0; index < idSets.length; index++) {
							idSets[index].removeAll(commonSet);
							errors.append("Lang: ["+langs[index]).append("], items ").append(idSets[index]).append('\n');
						}
						throw new ContentException("Error in ["+I18N_XPATH+"] section: some items are not defined in all the languages:\n"+errors);
					}
					else {
						return result;
					}
				}
				else {
					return result;
				}
			} catch (XPathExpressionException | DOMException e) {
				throw new ContentException(e.getLocalizedMessage(), e);
			}
		}
	}
	
	private static Set<String> toSet(final Iterable<String> content) {
		final Set<String>	result = new HashSet<>();
		
		for(String item : content) {
			result.add(item);
		}
		return result;
	}
}
