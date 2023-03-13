package chav1961.bt.creolenotepad;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;

import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.useful.JSimpleSplash;

public class Application extends JFrame implements AutoCloseable {
	private static final long 	serialVersionUID = 1L;
	
	public static final String	ARG_PROPFILE_LOCATION = "prop";

	
	public Application(final ContentMetadataInterface mdi, final CountDownLatch latch, final File props) {
		
		// TODO Auto-generated constructor stub
	}

	@Override
	public void close() throws RuntimeException {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		final ArgParser	parser = new ApplicationArgParser();
		int				retcode = 0;
		
		try(final JSimpleSplash		jss = new JSimpleSplash()) {
			final ArgParser			parsed = parser.parse(args);
			final CountDownLatch	latch = new CountDownLatch(1);
			final ContentMetadataInterface	mdi = ContentModelFactory.forXmlDescription(Application.class.getResourceAsStream("application.xml"));
			
			jss.start("", 2);
			try(final Application	app = new Application(mdi, latch, parsed.getValue(ARG_PROPFILE_LOCATION, File.class))) {
				
				app.setVisible(true);
				latch.await();
			} catch (InterruptedException e) {
			}
		} catch (CommandLineParametersException e) {
			System.err.println(e.getLocalizedMessage());
			System.err.println(parser.getUsage("creolenotepad"));
			retcode = 128;
		}
		System.exit(retcode);
	}

	private static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new FileArg(ARG_PROPFILE_LOCATION, false, "Property file location", "./.bt.creolenotepad.properties")
		};
		
		private ApplicationArgParser() {
			super(KEYS);
		}
	}


}
