package chav1961.bt.installbuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.jar.JarOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import chav1961.bt.installbuilder.tools.ImagesCollection;
import chav1961.bt.installbuilder.tools.Parameters;
import chav1961.bt.installbuilder.tools.Repository;
import chav1961.bt.installbuilder.tools.Settings;
import chav1961.bt.installbuilder.tools.SplashScreenKeeper;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ContentException;

public class Application {
	public static final String	ARG_SCENARIO_URI = "scenario";
	public static final String	ARG_SCENARIO_OUTPUT_FILE = "o";

	public static void main(String[] args) {
		final ArgParser	parser = new ApplicationArgParser();
		int		retcode = 0;

		try {
			final ArgParser		parsed = parser.parse(args);
			
			try(final InputStream		is = parsed.isTyped(ARG_SCENARIO_URI) ? parsed.getValue(ARG_SCENARIO_URI, URI.class).toURL().openStream() : System.in) {
				final DocumentBuilder	builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				final Document 			doc = builder.parse(is);
				final Repository		repo = new Repository();
				
				doc.getDocumentElement().normalize();
				
				processScenario(doc, repo);
				upload(repo, parsed.isTyped(ARG_SCENARIO_OUTPUT_FILE) ? new FileOutputStream(parsed.getValue(ARG_SCENARIO_OUTPUT_FILE, File.class)) : System.out);
			}
		} catch (CommandLineParametersException exc) {
			System.err.println(exc.getLocalizedMessage());
			System.err.println(parser.getUsage("bt.installer"));
			retcode = 128;
		} catch (IOException | ParserConfigurationException | SAXException | ContentException exc) {
			System.err.println(exc.getLocalizedMessage());
			retcode = 129;
		}
		System.exit(retcode);
	}

	static void processScenario(final Document doc, final Repository repo) throws ContentException {
		try {
			final SplashScreenKeeper	keeper = SplashScreenKeeper.fromXML(doc);
			
			if (keeper != null) {
				repo.addSplashScreen(keeper);
			}
			repo.addPureLibLocation(URI.create(doc.getDocumentElement().getAttribute("purelib")).toURL());
			repo.addInstallerLocation(URI.create(doc.getDocumentElement().getAttribute("installer")).toURL());
			repo.addLocalizingStrings(new String(URIUtils.loadCharsFromURI(URI.create(doc.getDocumentElement().getAttribute("i18n")))));
			repo.addSettings(Settings.fromXML(doc)); 
			repo.addParameters(Parameters.fromXML(doc)); 
			repo.addImagesCollection(ImagesCollection.fromXML(doc)); 
		} catch (IOException e) {
			throw new ContentException(e.getLocalizedMessage(), e);
		}
	}

	static void upload(final Repository repo, final OutputStream os) throws IOException {
		try(final JarOutputStream	jos = new JarOutputStream(os, repo.getManifest())) {
			
			repo.upload(jos);
			jos.finish();
			jos.flush();
		}
	}
	
	private static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new URIArg(ARG_SCENARIO_URI, false, true, "XML scenario file to build installer. If missing, System.in will be used"),
			new FileArg(ARG_SCENARIO_OUTPUT_FILE, false, false, "Jar file to store result. If missing, System.out will be used"),
		};
		
		private ApplicationArgParser() {
			super(KEYS);
		}
	}
}
