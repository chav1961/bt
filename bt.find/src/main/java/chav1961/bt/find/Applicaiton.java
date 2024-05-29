package chav1961.bt.find;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import chav1961.bt.find.internal.FileMask;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;

public class Applicaiton {
	public static final String		ARG_FILE_MASK = "fileMask";			
	public static final String		ARG_CONTENT_PATTERN = "contentPattern";
	public static final String		ARG_ENCODING = "enc";
	public static final String		ARG_OUTPUT_FILE = "f";			
	public static final String		ARG_PRINT_PATH = "p";			
	public static final String		ARG_PRINT_ABSOLUTE_PATH = "P";			
	public static final String		ARG_PRINT_LINE = "l";			

	public static void main(String[] args) {
		final ArgParser			parser = new ApplicationArgParser();
		
		try{
			final ArgParser		parsed = parser.parse(args);
			final Charset		encoding = parsed.isTyped(ARG_ENCODING) ? Charset.forName(parsed.getValue(ARG_ENCODING, String.class)) : Charset.defaultCharset(); 
			
			if (parsed.isTyped(ARG_OUTPUT_FILE)) {
				try(final OutputStream	os = new FileOutputStream(parsed.getValue(ARG_OUTPUT_FILE, File.class));
					final PrintStream	ps = new PrintStream(ARG_CONTENT_PATTERN, encoding)) {
					
					seek(parsed, ps);						
				}
			}
			else {
				seek(parsed, System.out);						
			}
			System.exit(0);
		} catch (CommandLineParametersException exc) {
			System.err.println(exc.getLocalizedMessage());
			System.err.println(parser.getUsage("bt.find"));
			System.exit(128);
		} catch (Exception exc) {
			System.err.println(exc.getLocalizedMessage());
			exc.printStackTrace();
			System.exit(129);
		}
	}

	private static void seek(final ArgParser parsed, final PrintStream ps) throws Exception {
		// TODO Auto-generated method stub
		final String			mask = parsed.getValue(ARG_FILE_MASK, String.class);
		final File				root = mask.startsWith("/") ? new File("/") : new File("./");
		final FileMask			fileMask = FileMask.compile(mask);
		final Pattern			pattern = parsed.isTyped(ARG_CONTENT_PATTERN) ? parsed.getValue(ARG_CONTENT_PATTERN, Pattern.class) : null;
		final IntConsumer		linePrinter = parsed.isTyped(ARG_PRINT_LINE) ? (l)->ps.print(l) : (l)->{};
		final Consumer<File>	filePrinter = parsed.isTyped(ARG_PRINT_ABSOLUTE_PATH) 
												? (f)->ps.println(f.getAbsolutePath()) 
												: parsed.isTyped(ARG_PRINT_PATH) 
													? (f)->ps.println(f.getPath()) : (f)->ps.println(f.getName());
		seek(root, fileMask, pattern, linePrinter, filePrinter);
	}

	static void seek(final File root, final FileMask fileMask, final Pattern pattern, final IntConsumer linePrinter, final Consumer<File> filePrinter) {
		if (root.exists() && root.canRead()) {
			if (root.isFile() && fileMask.matches(root)) {
				if (pattern != null) {
					try(final Reader			rdr = new FileReader(root);
						final BufferedReader	brdr = 	new BufferedReader(rdr)) {
						int		lineNo = 1;						
						String	line;
						
						while ((line = brdr.readLine()) != null) {
							if (pattern.matcher(line).matches()) {
								linePrinter.accept(lineNo);
								filePrinter.accept(root);
							}
							lineNo++;
						}						
					} catch (IOException exc) {
					}
				}
				else {
					linePrinter.accept(0);
					filePrinter.accept(root);
				}
			}
			if (root.isDirectory() && fileMask.matches(root)) {
				final File[]	content = root.listFiles((f)->fileMask.matches(f));
				
				if (content != null) {
					for(File item : content) {
						seek(item, fileMask, pattern, linePrinter, filePrinter);
					}
				}
			}
		}
	}

	private static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new StringArg(ARG_FILE_MASK, true, true, "File mask to seek (for example ./a/**/{*.txt[@length>=1M]|*.doc[@canRead=true]}"),
			new PatternArg(ARG_CONTENT_PATTERN, false, true, "Content pattern to seek inside the file"),
			new FileArg(ARG_OUTPUT_FILE, false, false, "File to store find results. If missing, System.out will be used"),
			new StringArg(ARG_ENCODING, false, false, "File content encoding to see content pattern inside. If missing, default system encoding will be used"),
			new BooleanArg(ARG_PRINT_PATH, false, "Print full path instead of file name only", true),
			new BooleanArg(ARG_PRINT_ABSOLUTE_PATH, false, "Print absolute path instead of file name or path only", false),
			new BooleanArg(ARG_PRINT_LINE, false, "Print line inside the file where content pattern found", false)
		};
		
		ApplicationArgParser() {
			super(KEYS);
		}
	}
}
