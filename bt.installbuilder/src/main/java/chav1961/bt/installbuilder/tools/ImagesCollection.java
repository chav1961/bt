package chav1961.bt.installbuilder.tools;

import java.awt.image.ImageConsumer;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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

public class ImagesCollection {
	private static final String		IMAGES_XPATH = "/scenario/images/image";
	private static final String		IMAGES_TITLE = "name";
	private static final String		IMAGES_HREF = "href";
	
	private final Map<String, URL>	images = new HashMap<>();
	
	private ImagesCollection() {
	}
	
	private void addImage(final String name, final URL url) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Image name to add can't be null or empty");
		}
		else if (url == null) {
			throw new NullPointerException("URL to add can't be null");
		}
		else {
			images.put(name, url);
		}
	}
	
	public boolean hasImage(final String name) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Image name can't be null or empty");
		}
		else {
			return images.containsKey(name);
		}
	}
	
	public InputStream getInputStream(final String name) throws IOException {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Image name can't be null or empty");
		}
		else if (!hasImage(name)) {
			throw new IllegalArgumentException("Image name ["+name+"] is missing in the repository");
		}
		else {
			return images.get(name).openStream();
		}
	}
	
	public Iterable<String> names() {
		return images.keySet();
	}
	
	public static ImagesCollection fromXML(final Document doc) throws ContentException {
		if (doc == null) {
			throw new NullPointerException("XML document can't be null");
		}
		else {
			try {
				final ImagesCollection	result = new ImagesCollection();
				final XPath 			xPath = XPathFactory.newInstance().newXPath();
				final NodeList			nodes = (NodeList) xPath.compile(IMAGES_XPATH).evaluate(doc, XPathConstants.NODESET);
				
				for (int index = 0; index < nodes.getLength(); index++) {
					final Node		node = nodes.item(index);
					final String	imageName = node.getAttributes().getNamedItem(IMAGES_TITLE).getTextContent();
					
					if (!result.hasImage(imageName)) {
						result.addImage(imageName, new URL(node.getAttributes().getNamedItem(IMAGES_HREF).getTextContent()));
					}
					else {
						throw new ContentException("Error in ["+IMAGES_XPATH+"] section: duplicate image name ["+imageName+"]");
					}
				}
				return result;
			} catch (XPathExpressionException | DOMException | MalformedURLException e) {
				throw new ContentException(e.getLocalizedMessage(), e);
			}
		}
	}
}
