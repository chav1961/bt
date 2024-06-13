package chav1961.bt.find;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Consumer;

import chav1961.bt.find.internal.ContentPattern;
import chav1961.bt.find.internal.FileMask;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class Application {
	public static final String		ARG_FILE_MASK = "fileMask";			
	public static final String		ARG_CONTENT_PATTERN = "contentPattern";
	public static final String		ARG_ENCODING = "enc";
	public static final String		ARG_OUTPUT_FILE = "f";			
	public static final String		ARG_PRINT_PATH = "p";			
	public static final String		ARG_PRINT_ABSOLUTE_PATH = "pp";			
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
			System.err.println("Calling args are: "+Arrays.toString(args));
			System.err.println(parser.getUsage("bt.find"));
			System.exit(128);
		} catch (Exception exc) {
			System.err.println(exc.getLocalizedMessage());
			exc.printStackTrace();
			System.exit(129);
		}
	}

	private static void seek(final ArgParser parsed, final PrintStream ps) throws Exception {
		final String			mask = unwrapQuotes(parsed.getValue(ARG_FILE_MASK, String.class));
		final File				root = mask.startsWith("/") ? new File("/") : new File("./");
		final FileMask			fileMask = FileMask.compile(mask);
		final ContentPattern	pattern = parsed.isTyped(ARG_CONTENT_PATTERN) ? ContentPattern.compile(getPatternDescriptor(parsed)) : null;
		final boolean			printPath = parsed.getValue(ARG_PRINT_PATH, boolean.class);
		final boolean			printAbsolutePath = parsed.getValue(ARG_PRINT_ABSOLUTE_PATH, boolean.class);
		final Consumer<File>	filePrinter = printAbsolutePath 
												? (f)->ps.println(f.getAbsolutePath()) 
												: printPath 
													? (f)->ps.println(f.getPath()) : (f)->ps.println(f.getName());
		
		seek(root, fileMask, pattern, filePrinter);
	}

	private static String getPatternDescriptor(final ArgParser parsed) throws CommandLineParametersException {
		String	pattern = parsed.getValue(ARG_CONTENT_PATTERN, String.class);
		
		if ("@".equals(pattern)) {
			try(final Reader	rdr = new InputStreamReader(System.in)) {
				
				pattern = Utils.fromResource(rdr);
			} catch (IOException e) {
				throw new CommandLineParametersException("I/O error reading pattern from System.in: "+e.getLocalizedMessage());
			}
		}
		return pattern;
	}

	private static String unwrapQuotes(final String value) {
		if (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'') {
			return value.substring(1, value.length()-1); 
		}
		else {
			return value;
		}
	}
	
	static void seek(final File root, final FileMask fileMask, final ContentPattern pattern, final Consumer<File> filePrinter) throws SyntaxException {
		fileMask.walk(root, (f)->{
			if (pattern != null) {
				try(final FileChannel 		fileChannel = (FileChannel) Files.newByteChannel(f.toPath(), EnumSet.of(StandardOpenOption.READ))) {
				    final MappedByteBuffer	mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

				    if (mappedByteBuffer != null) {
				        final CharBuffer 	charBuffer = Charset.forName(PureLibSettings.DEFAULT_CONTENT_ENCODING).decode(mappedByteBuffer);
				        
				        if (pattern.match(charBuffer)) {
							filePrinter.accept(f);
				        }
				    }
				} catch (IOException | SyntaxException exc) {
				}
			}
			else {
				filePrinter.accept(f);
			}
		});
	}

	private static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new StringArg(ARG_FILE_MASK, true, true, "File mask to seek (for example ./a/**/{*.txt[length>=1M]|*.doc[canRead=true]}. \n"
					+ "To escape '>' and '<' signs inside the expressions, use caret char (^) before.\n"
					+"It's strongly recommended to wrap the mask with apostrophes (')"),
			new StringArg(ARG_CONTENT_PATTERN, false, true, "Content pattern to seek inside the file or '@' to get pattern from System.in"),
			new FileArg(ARG_OUTPUT_FILE, false, false, "File to store find results. If missing, System.out will be used"),
			new StringArg(ARG_ENCODING, false, "File content encoding to see content pattern inside. If missing, default system encoding will be used", PureLibSettings.DEFAULT_CONTENT_ENCODING),
			new BooleanArg(ARG_PRINT_PATH, false, "Print full path instead of file name only", true),
			new BooleanArg(ARG_PRINT_ABSOLUTE_PATH, false, "Print absolute path instead of file name or path only", false),
			new BooleanArg(ARG_PRINT_LINE, false, "Print line inside the file where content pattern found", false)
		};
		
		ApplicationArgParser() {
			super(KEYS);
		}
	}
}
