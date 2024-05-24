package chav1961.bt.installer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;

public class Scenario {
	private final BufferedImage					splash;
	private final String						splashTitle;
	private final Map<String, Parameter>		parameters = new HashMap<>();
	private final Map<String, BufferedImage>	images = new HashMap<>();
	private final Map<String, String>[]			i18n = new Map[SupportedLanguages.values().length];
	
	public Scenario(final Reader scenario) throws IOException {
		if (scenario == null) {
			throw new NullPointerException("Scenario reader can't be null");
		}
		else {
			try {
				final SAXParserFactory	factory = SAXParserFactory.newInstance();
				final SAXParser 		saxParser = factory.newSAXParser();
				final ScenarioHandler 	handler = new ScenarioHandler();

				saxParser.parse(new InputSource(scenario), handler);
				parameters.putAll(handler.parameters);
				
				
				splash = null;
				splashTitle = null;
				
			} catch (ParserConfigurationException | SAXException e) {
				throw new IOException(e);
			}
		}		
	}
	
	public static class Parameter {
		public static enum Type {
			String,
			Integer
		}
		
		private final String	name;
		private final String	label;
		private final Type		type;
		private final Object	defaultVal;
		private Object			currentVal;
		
		public Parameter(String name, String label, Type type, Object defaultVal) {
			this.name = name;
			this.label = label;
			this.type = type;
			this.defaultVal = defaultVal;
		}

		public Object getCurrentVal() {
			return currentVal;
		}

		public void setCurrentVal(final Object currentVal) {
			this.currentVal = currentVal;
		}

		public String getName() {
			return name;
		}

		public String getLabel() {
			return label;
		}

		public Type getType() {
			return type;
		}

		public Object getDefaultVal() {
			return defaultVal;
		}

		@Override
		public String toString() {
			return "Parameter [name=" + name + ", label=" + label + ", type=" + type + ", defaultVal=" + defaultVal + ", currentVal=" + currentVal + "]";
		}
	}
	
	private static class ScenarioHandler extends DefaultHandler {
		private final Map<String, Parameter>		parameters = new HashMap<>();
		private final Map<String, BufferedImage>	images = new HashMap<>();
		private final StringBuilder					sb = new StringBuilder();
		private String		imageId;
		private String		imageHRef;
		
		@Override
		public void characters(final char[] ch, final int start, final int length) throws SAXException {
			sb.append(ch, start, length);
		}
		
		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
			switch (qName) {
				case "parameter"	:
					final String	pName = attributes.getValue("name");
					final String	pType = attributes.getValue("type");
					final String	pDefault = attributes.getValue("default");
					final String	pOptions = attributes.getValue("options");
					
					parameters.put(pName, new Parameter(pName, pOptions, Parameter.Type.valueOf(pType), pDefault));
					break;
				case "image"		:
					imageId = attributes.getValue("id");
					imageHRef = attributes.getValue("hRef");
					sb.setLength(0);
					break;
				default :
					break;
			}
		}
		
		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			try {
				switch (qName) {
					case "image"		:
						if (imageHRef != null) {
							images.put(imageId, ImageIO.read(new URL(imageHRef)));
						}
						else {
							images.put(imageId, ImageIO.read(URIUtils.convert2selfURI(Base64.getMimeDecoder().decode(sb.toString())).toURL()));
						}
						break;
				}
			} catch (IOException e) {
				throw new SAXException(e);
			}
		}
	}
}
