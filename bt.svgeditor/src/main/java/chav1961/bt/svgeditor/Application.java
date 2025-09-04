package chav1961.bt.svgeditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import chav1961.bt.svgeditor.internal.AppWindow;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;

public class Application {
	public static final String		ARG_COMMAND = "command";
	public static final String		ARG_INPUT_MASK = "inputMask";
	public static final String		ARG_RECURSION_FLAG = "recursion";
	public static final String		ARG_GUI_FLAG = "guiFlag";
	public static final String		ARG_PROPFILE_LOCATION = "prop";

	public static void main(String[] args) {
		final ArgParser				parser = new ApplicationArgParser();
		
		try{final ArgParser			parsed = parser.parse(args);
			final SubstitutableProperties	props = loadProps(parsed.getValue(ARG_PROPFILE_LOCATION, InputStream.class));
				
			if (parsed.isEmpty()) {
				try(final AppWindow	w = new AppWindow(PureLibSettings.PURELIB_LOCALIZER, props)) {
					
					w.setVisible(true);
					w.awaitingExit();
					w.setVisible(false);
					props.store(parsed.getValue(ARG_PROPFILE_LOCATION, File.class));
				}
			}
			else {
				
			}
		} catch (IOException | InterruptedException exc) {
			System.err.println(exc.getLocalizedMessage());
			System.exit(129);
		} catch (CommandLineParametersException exc) {
			System.err.println(exc.getLocalizedMessage());
			System.err.println(parser.getUsage("bt.svgeditor"));
			System.exit(128);
		}
	}

	private static SubstitutableProperties loadProps(final InputStream value) throws IOException {
		final SubstitutableProperties	result = new SubstitutableProperties();
		
		result.load(new InputStreamReader(value, PureLibSettings.DEFAULT_CONTENT_ENCODING));
		return result;
	}

	private static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new StringArg(ARG_COMMAND, false, true, "Command to process content. @<file> means to get command from <file>"),
			new StringArg(ARG_INPUT_MASK, false, false, "Mask for input files. If typed, all content will be get from file system instead of stdin stream"),
			new BooleanArg(ARG_RECURSION_FLAG, false, "Process input mask recursively, if types", false),
			new BooleanArg(ARG_GUI_FLAG, false, "Start GUI after processing. If any arguments are missing, starts GUI automatically", false),
			new FileArg(ARG_PROPFILE_LOCATION, FileType.FILE_ONLY, false, "Property file location", "./.bt.svgeditor.properties")
		};
		
		private ApplicationArgParser() {
			super(KEYS);
		}
		
		@Override
		protected String finalValidation(final ArgParser parser) throws CommandLineParametersException {
			final File	f = parser.getValue(ARG_PROPFILE_LOCATION, File.class);
			
			if (f.exists() && f.isFile() && f.canRead()) {
				return super.finalValidation(parser);
			}
			else {
				try(final FileOutputStream	fos = new FileOutputStream(f)) {
					return super.finalValidation(parser);
				} catch (IOException exc) {
					return exc.getLocalizedMessage();
				}
			}
		}
	}
}
