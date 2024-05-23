package chav1961.bt.installer.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import chav1961.purelib.basic.exceptions.ContentException;

public class SplashScreenKeeper {
	private static final String		SPLASH_XPATH = "/scenario/splash";
	private static final String		SPLASH_TITLE = "title";
	private static final String		SPLASH_HREF = "href";

	private final String	title;
	private final URL		content;
	
	private SplashScreenKeeper(final String title, final URL content) {
		this.title = title;
		this.content = content;
	}

	public String getSplashScreenName() {
		return title;
	}
	
	public InputStream getInputStream() throws IOException {
		return content.openStream();
	}

	public static SplashScreenKeeper fromXML(final Document doc) throws ContentException {
		if (doc == null) {
			throw new NullPointerException("XML document can't be null");
		}
		else {
			try {
				final XPath 	xPath = XPathFactory.newInstance().newXPath();
				final Node 		node = (Node) xPath.compile(SPLASH_XPATH).evaluate(doc, XPathConstants.NODE);
				
				if (node == null) {
					return null;
				}
				else {
					return new SplashScreenKeeper(node.getAttributes().getNamedItem(SPLASH_TITLE).getTextContent(), new URL(node.getAttributes().getNamedItem(SPLASH_HREF).getTextContent()));
				}
			} catch (DOMException | MalformedURLException | XPathExpressionException e) {
				throw new ContentException(e.getLocalizedMessage(), e);
			}
		}
	}
	

}
