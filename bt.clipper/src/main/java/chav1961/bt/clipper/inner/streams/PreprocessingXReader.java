package chav1961.bt.clipper.inner.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.char2char.AbstractPreprocessingReader;

public class PreprocessingXReader extends AbstractPreprocessingReader {
	private static SyntaxTreeInterface<CommandType>	COMMANDS = new AndOrTree<>();
	private static final char[]						EMPTY = new char[0];
	
	private enum CommandType {
		CMD_UNKNOWN, CMD_COMMAND, CMD_DEFINE, CMD_ELSE, CMD_END, CMD_ERROR, CMD_IFDEF, CMD_IFNDEF, CMD_INCLUDE, CMD_STDOUT, CMD_TRANSLATE, CMD_UNDEF, CMD_XCOMMAND, CMD_XTRANSLATE  
	}

	static {
		COMMANDS.placeName("#command",CommandType.CMD_COMMAND);
		COMMANDS.placeName("#define",CommandType.CMD_DEFINE);
		COMMANDS.placeName("#else",CommandType.CMD_ELSE);
		COMMANDS.placeName("#end",CommandType.CMD_END);
		COMMANDS.placeName("#error",CommandType.CMD_ERROR);
		COMMANDS.placeName("#ifdef",CommandType.CMD_IFDEF);
		COMMANDS.placeName("#ifndef",CommandType.CMD_IFNDEF);
		COMMANDS.placeName("#include",CommandType.CMD_INCLUDE);
		COMMANDS.placeName("#stdout",CommandType.CMD_STDOUT);
		COMMANDS.placeName("#translate",CommandType.CMD_TRANSLATE);
		COMMANDS.placeName("#undef",CommandType.CMD_UNDEF);
		COMMANDS.placeName("#xcommand",CommandType.CMD_XCOMMAND);
		COMMANDS.placeName("#xtranslate",CommandType.CMD_XTRANSLATE);
	}

	private final GrowableCharArray<GrowableCharArray<?>>	collector = new GrowableCharArray<GrowableCharArray<?>>(false);
	private final int[]			forNames = new int[2], forStrings = new int[2];
	private final SyntaxTreeInterface<List<PatternAndSubstitutor>>	commands = new AndOrTree<>();
	private final SyntaxTreeInterface<List<PatternAndSubstitutor>>	translations = new AndOrTree<>();
	private long				nestedMask = ENABLED_OUTPUT_MASK, skipMask = ENABLED_OUTPUT_MASK;
	private boolean				insideMultiline = false;
	private boolean				insideCollector = false;
	private CommandType			collectorCommand = null;
	private int					currentDepth = -1;
	
	
	/**
	 * <p>Create reader with the nested source and default settings.</p>
	 * @param nestedReader reader to use as content source. Can't be null
	 */
	public PreprocessingXReader(final Reader nestedReader) {
		this(nestedReader,new HashMap<>());
	}
	
	/**
	 * <p>Create reader with the nested source and explicitly typed settings.</p>
	 * @param nestedReader reader to use as content source. Can't be null
	 * @param varsAndOptions explicitly typed options. Can't be null. Use static string keys defined in the class as key names for the map. 
	 * Changing it's content during processing data is not affected on processing behavior, but will take effect on all #include content 
	 */
	public PreprocessingXReader(final Reader nestedReader, final Map<String,Object> varsAndOptions) {
		this(nestedReader, varsAndOptions, new IncludeCallback(){
				@Override
				public Reader getIncludeStream(final URI streamRef) throws IOException {
					final URL			url = streamRef.toURL();
					final InputStream	is = url.openStream();
					
					return new InputStreamReader(is,"UTF-8"){@Override public void close() throws IOException {try{super.close();} finally {is.close();}}};
				}
			}
		);
	}

	/**
	 * <p>Create reader with the nested source, explicitly typed settings and special case to process #include statements</p>
	 * @param nestedReader reader to use as content source. Can't be null
	 * @param varsAndOptions explicitly typed options. Can't be null. Use static string keys defined in the class as key names for the map. 
	 * Changing it's content during processing data is not affected on processing behavior, but will take effect on all #include content 
	 * @param includeCallback include callback to get reader for the given include URI. Can't be null
	 */
	public PreprocessingXReader(final Reader nestedReader, final Map<String,Object> varsAndOptions, final IncludeCallback includeCallback) {
		this(null,nestedReader,varsAndOptions,includeCallback);
	}
	
	protected PreprocessingXReader(final URI nestedReaderURI, final Reader nestedReader, final Map<String,Object> varsAndOptions, final IncludeCallback includeCallback) {
		super(nestedReaderURI, nestedReader, varsAndOptions, includeCallback);
	}	
	
	@Override
	protected void internalProcessLine(final long displacement, final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
		final int	start = from, to = from + length;
		
		if (data[from] == '#') {
			from = CharUtils.skipBlank(data,CharUtils.parseName(data, from + 1, forNames),true);
			
			switch (getCommandType(data,forNames[0],forNames[1])) {
				case CMD_DEFINE		:
					from = CharUtils.skipBlank(data,CharUtils.parseName(data, CharUtils.skipBlank(data,from + 1, true), forNames),true);
					from = CharUtils.skipBlank(data,toEndOfLine(data, CharUtils.skipBlank(data, from + 1, true), forStrings),true);
					
					putDefinition(data, forNames[0], forNames[1], Arrays.copyOfRange(data, forStrings[0], forStrings[1]-forStrings[0]));
					break;
				case CMD_ELSE		:
					if ((nestedMask & (1L << currentDepth)) == 1) {
						throw new SyntaxException(lineNo, from-start, "#else without #ifdef/#ifndef");
					};
					skipMask ^= (1L << currentDepth);
					break;
				case CMD_END		:
					if (currentDepth < 0) {
						throw new SyntaxException(lineNo, from-start, "#end without #ifdef/#ifndef");
					}
					else {
						nestedMask ^= (1L << currentDepth);
						skipMask |= (1L << currentDepth);
						currentDepth--;
					}
					break;
				case CMD_ERROR		:
					from = CharUtils.skipBlank(data,toEndOfLine(data, CharUtils.skipBlank(data, from + 1, true), forStrings),true);
					
					throw new SyntaxException(lineNo, 0, new String(data, forStrings[0], forStrings[1]-forStrings[0]));
				case CMD_IFDEF		:
					from = CharUtils.skipBlank(data,CharUtils.parseName(data, CharUtils.skipBlank(data, from + 1,true), forNames),true);
					
					currentDepth++;
					nestedMask ^= (1L << currentDepth);
					if (!definitionExists(data, forNames[0], forNames[1])) {
						skipMask ^= (1L << currentDepth);
					}
					break;
				case CMD_IFNDEF		:
					from = CharUtils.skipBlank(data,CharUtils.parseName(data, CharUtils.skipBlank(data, from + 1,true), forNames),true);

					currentDepth++;
					nestedMask ^= (1L << currentDepth);
					if (definitionExists(data, forNames[0], forNames[1])) {
						skipMask ^= (1L << currentDepth);
					}
					break;
				case CMD_INCLUDE	:
					from = CharUtils.skipBlank(data,toEndOfLine(data, CharUtils.skipBlank(data, from + 1, true), forStrings),true);

					final URI	uri;
					
					if (data[forStrings[0]] == '<' || data[forStrings[0]] == '\"') {
						uri = URI.create("root:/"+this.getClass().getCanonicalName()+"/bt.clipper/includes/"+new String(data, forStrings[0]+1, forStrings[1]-forStrings[0]-2));
					}
					else {
						uri = URI.create(new String(data, forStrings[0], forStrings[1]-forStrings[0]));
					}
					pushReader(uri, getIncludeCallback().getIncludeStream(uri));
					break;
				case CMD_STDOUT		:
					from = CharUtils.skipBlank(data,toEndOfLine(data, CharUtils.skipBlank(data, from + 1, true), forStrings),true);
					
					putContent(data, forStrings[0], forStrings[1]-forStrings[0]);
					break;
				case CMD_UNDEF		:
					from = CharUtils.skipBlank(data,CharUtils.parseName(data, CharUtils.skipBlank(data, from + 1,true), forNames),true);
					
					removeDefinition(data, forNames[0], forNames[1]);
					break;
				case CMD_COMMAND	:
					insideCollector = true;
					collectorCommand = CommandType.CMD_COMMAND;
					internalProcessLine(displacement, lineNo, data, from, length);
					break;
				case CMD_XCOMMAND	:
					insideCollector = true;
					collectorCommand = CommandType.CMD_XCOMMAND;
					internalProcessLine(displacement, lineNo, data, from, length);
					break;
				case CMD_TRANSLATE	:
					insideCollector = true;
					collectorCommand = CommandType.CMD_TRANSLATE;
					internalProcessLine(displacement, lineNo, data, from, length);
					break;
				case CMD_XTRANSLATE	:
					insideCollector = true;
					collectorCommand = CommandType.CMD_XTRANSLATE;
					internalProcessLine(displacement, lineNo, data, from, length);
					break;
				case CMD_UNKNOWN	:
					putContent(data, start, length);
					break;
				default:
					throw new UnsupportedOperationException("Command type ["+getCommandType(data,forNames[0],forNames[1])+"] is not supprted yet");
			}			
		}
		else if (insideCollector) {
			from = CharUtils.skipBlank(data,toEndOfLine(data, CharUtils.skipBlank(data, from + 1,true), forStrings),true);
			if (data[forStrings[1]] == ';') {
				collector.append(data,forStrings[0],forStrings[1]-1);
			}
			else {
				collector.append(data,forStrings[0],forStrings[1]).append('\n');
				insideCollector = false;
				processCommand(collectorCommand,collector.extract());
				collector.length(0);
			}
		}
		else if (skipMask == ENABLED_OUTPUT_MASK) {
			switch (getHidingMethod()) {
				case EXCLUDE				:
					break;
				case SINGLE_LINE_COMMENTED	:
					putContent(getInlineComment());
					putContent(data,from,length);
					break;
				case MULTILINE_COMMENTED	:
					if (!insideMultiline) {
						insideMultiline = true;
						putContent(getStartMultilineComment());
					}
					putContent(data,from,length);
					break;
				default : throw new UnsupportedOperationException("Hiding method ["+getHidingMethod()+"] is not supported yet");
			}
		}
		else {
			if (isInlineSubstitution()) {
				substitute(data, from, length);
			}
			else {
				putContent(data,from,length);
			}
		}
	}

	@Override
	protected AbstractPreprocessingReader newDelegate(URI nestedReaderURI, Reader nestedReader, Map<String, Object> varsAndOptions, IncludeCallback includeCallback) throws IOException, SyntaxException {
		return new PreprocessingXReader(nestedReaderURI, nestedReader, varsAndOptions, includeCallback);
	}
	
	protected CommandType getCommandType(final char[] data, final int start, final int end) {
		final long	id;
		
		if (isIgnoreCase()) {
			final char[]	command = new char[end-start];
			
			for (int index = 0; index < command.length; index++) {
				command[index] = Character.toLowerCase(data[start+index]);
			}
			id = COMMANDS.seekName(command,0,command.length);
		}
		else {
			id = COMMANDS.seekName(data,start,end);
		}
		
		if (id >= 0) {
			return COMMANDS.getCargo(id);
		}
		else {
			return CommandType.CMD_UNKNOWN;
		}
	}

	private void processCommand(final CommandType command, final char[] content) throws SyntaxException {
		PatternAndSubstitutor	pas; 
		
		switch (command) {
			case CMD_COMMAND		:
				pas = CommandParser.build(content, 0, true);
				placePattern(commands, pas.getKeyword(), pas);
				if (pas.getKeyword().length > 4) {
					placePattern(commands, trunc2four(pas.getKeyword()), pas);
				}
				break;
			case CMD_TRANSLATE		:
				pas = CommandParser.build(content, 0, true);
				placePattern(translations, pas.getKeyword(), pas);
				if (pas.getKeyword().length > 4) {
					placePattern(translations, trunc2four(pas.getKeyword()), pas);
				}
				break;
			case CMD_XCOMMAND		:
				pas = CommandParser.build(content, 0, false);
				placePattern(commands, pas.getKeyword(), pas);
				break;
			case CMD_XTRANSLATE		:
				pas = CommandParser.build(content, 0, false);
				placePattern(translations, pas.getKeyword(), pas);
				break;
			default :
				throw new UnsupportedOperationException("Command type ["+command+"] is not supported yet");
		}
	}
	
	private static void placePattern(SyntaxTreeInterface<List<PatternAndSubstitutor>> where, char[] keyword, PatternAndSubstitutor pas) {
		final long	id = where.seekName(keyword, 0, keyword.length);
		List<PatternAndSubstitutor>	list;
		
		if (id < 0) {
			where.placeName(keyword, 0, keyword.length, list = new ArrayList<>());
		}
		else {
			list = where.getCargo(id);
		}
		list.add(pas);
	}

	private static char[] trunc2four(final char[] source) {
		if (source.length >= 4) {
			return Arrays.copyOfRange(source, 0, 4);
		}
		else {
			return source;
		}
	}
	
	private void putDefinition(final char[] data, final int from, final int to, final char[] value) {
		if (isIgnoreCase()) {
			final char[]	upperCase = new char[to - from];
			
			for (int index = 0; index < upperCase.length; index++) {
				upperCase[index] = Character.toUpperCase(data[from + index]);
			}
			for (int index = 0; index < upperCase.length; index++) {
				value[index] = Character.toUpperCase(value[index]);
			}
			definitions.placeName(upperCase, 0, upperCase.length, value);
		}
		else {
			definitions.placeName(data, from, to, value);
		}
	}

	private boolean definitionExists(final char[] data, final int from, final int to) {
		// TODO Auto-generated method stub
		return false;
	}

	private int toEndOfLine(final char[] data, int from, final int[] ranges) {
		ranges[0] = CharUtils.skipBlank(data, from, false);
		
		while (data[from] != '\r' && data[from] != '\n') {
			if (data[from] == '/' && data[from+1] == '/') {
				break;
			}
			else {
				from++;
			}
		}
		while (data[from] <= ' ') {
			from++;
		}
		if (from <= ranges[0]) {
			ranges[1] = ranges[0];
		}
		else {
			ranges[1] = from;
		}
		return from + 1;
	}
	
	private void removeDefinition(final char[] data, final int from, final int to) {
		final long	id;
		
		if (isIgnoreCase()) {
			final char[]	upperCase = new char[to - from];
			
			for (int index = 0; index < upperCase.length; index++) {
				upperCase[index] = Character.toUpperCase(data[from + index]);
			}
			id = definitions.seekName(upperCase,0,upperCase.length);
		}
		else {
			id = definitions.seekName(data, from, to);
		}
		if (id >= 0) {
			definitions.removeName(id);
		}
	}

	
}
