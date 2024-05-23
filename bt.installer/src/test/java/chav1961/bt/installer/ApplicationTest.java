package chav1961.bt.installer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import chav1961.purelib.basic.exceptions.ContentException;

public class ApplicationTest {

	@Test
	public void parseScenarioTest() throws ContentException, SAXException, IOException, ParserConfigurationException {
		try(final InputStream	is = getClass().getResourceAsStream("scenario.xml");
			final OutputStream	os = new FileOutputStream("c:/tmp/x.jar")) {
			final DocumentBuilder	builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			final Document 			doc = builder.parse(is);
			final Repository		repo = new Repository();
			
			doc.getDocumentElement().normalize();
			
			Application.processScenario(doc, repo);
			Application.upload(repo, os);
		}
	}
}
