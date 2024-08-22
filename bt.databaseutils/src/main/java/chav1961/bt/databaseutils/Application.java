package chav1961.bt.databaseutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.zip.ZipOutputStream;

import chav1961.bt.databaseutils.interfaces.PartManagerInterface;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class Application {
	public static final String	ARG_SOURCE = "source";
	public static final String	ARG_VERSION = "version";
	public static final String	ARG_PACK = "pack";
	public static final String	ARG_TARGET_DIR = "target";

	public Application() {
		
	}
	
	
	public static void main(final String[] args) {
		// TODO Auto-generated method stub
		final Args	parm = new Args();
		
		try {
			final ArgParser		parsed = parm.parse(args);
			
			try(final InputStream	is = parsed.isTyped(ARG_SOURCE) ? parsed.getValue(ARG_SOURCE, InputStream.class) : System.in;
				final Reader		rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
				final ContentMetadataInterface dbModel = ContentModelFactory.forJsonDescription(rdr);
				
				try(final PartManagerInterface	pmi = parsed.isAnyTyped(ARG_PACK) ? new ZipManager(parsed.getValue(ARG_PACK, File.class)) : new DirManager(parsed.getValue(ARG_TARGET_DIR, File.class))) {
					processModel(dbModel, pmi);
				}
			}
		} catch (IOException exc) {
			exc.printStackTrace();
			System.exit(129);
		} catch (CommandLineParametersException e) {
			System.err.println(e.getLocalizedMessage());
			System.err.println(parm.getUsage("bt.databaseutils"));
			System.exit(128);
		}
		
	}

	private static void processModel(final ContentMetadataInterface dbModel, final PartManagerInterface pmi) {
		// TODO Auto-generated method stub
		
	}

	private static class Args extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new FileArg(ARG_SOURCE, false, false, "JSON model file source location. In missing, model content will be read from System.in"),
			new BooleanArg(ARG_PACK, false, false, "Pack content generated and send it to System.out. Mutually exclusive with target"),
			new FileArg(ARG_TARGET_DIR, false, false, "Target directory to store generated content to. Mutually exclusive with pack"),
			new StringArg(ARG_VERSION, false, "Make content for version typed", "1.0"),
		};
		
		private Args() {
			super(KEYS);
		}
		
		@Override
		public ArgParser parse(final boolean ignoreExtra, final boolean ignoreUnknown, final String... args) throws CommandLineParametersException {
			final ArgParser	result = super.parse(ignoreExtra, ignoreUnknown, args);
			
			if (result.allAreTyped(ARG_PACK, ARG_TARGET_DIR)) {
				throw new CommandLineParametersException("Mutually exclusize parameters ["+ARG_PACK+"] and ["+ARG_TARGET_DIR+"] are typed");
			}
			else if (!result.isAnyTyped(ARG_PACK, ARG_TARGET_DIR)) {
				throw new CommandLineParametersException("Neither ["+ARG_PACK+"] nor ["+ARG_TARGET_DIR+"] parameters are typed");
			}
			else {
				return result;
			}
		}
	}
	
	private static class DirManager implements PartManagerInterface {
		private final File	rootDir;
		
		private DirManager(final File rootDir) throws IOException {
			this.rootDir = rootDir;
		}

		@Override
		public OutputStream getStream(final String name) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void close() throws IOException {
		}
	}
	
	private static class ZipManager implements PartManagerInterface {
		private final File				root;
		private final OutputStream		os;
		private final ZipOutputStream	zos;
		
		private ZipManager(final File root) throws IOException {
			this.root = root;
			this.os = new FileOutputStream(root);
			this.zos = new ZipOutputStream(os);
		}

		@Override
		public OutputStream getStream(final String name) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void close() throws IOException {
			zos.finish();
			zos.close();
			os.close();
		}
	}
}
