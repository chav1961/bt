package chav1961.bt.clipper.inner.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.cdb.interfaces.RuleBasedParser;
import chav1961.purelib.streams.char2char.AbstractPreprocessingReader;

public class PreprocessingXReader extends AbstractPreprocessingReader {
	private static SyntaxTreeInterface<CommandType>	COMMANDS = new AndOrTree<>();
	private static final SimpleURLClassLoader		LOADER = new SimpleURLClassLoader(new URL[0]);
	private static final char[]						NL = "\n".toCharArray();
	private static final Class<RuleBasedParser<Expression, Object>>		EXPRESSION_SKIPPER;
	private static final Class<RuleBasedParser<XExpression, Object>>	X_EXPRESSION_SKIPPER;

	private static enum XReaderTerminals {
		START_INLINE, CONTINUATION, NL, START_MULTILINE, END_MULTILINE 
	}
	
	private static enum XReaderState {
		LINE, CONTINUE, IN_MILTILINE, IN_MILTILINE_CONTINUED, AWAITING_NL, AWAITING_CONTINUED_NL, AWAITING_CONTINUED_MULTILINE_NL
	}
	
	private static enum XReaderActions {
		PARSE_AS_IS, PARSE_TRUNCATED, CLEAN, COLLECT, COLLECT_PREFIX, EXTRACT_AND_PARSE, PARSE_TAIL
	}

	@FunctionalInterface
	private interface ActionsCallback {
		void process(XReaderActions[] actions) throws Exception;
	}
	
	private static final FSM.FSMLine<XReaderTerminals,XReaderState,XReaderActions>[]	FSM_TABLE = new FSM.FSMLine[]{
												new FSM.FSMLine<>(XReaderState.LINE,XReaderTerminals.NL,XReaderState.LINE,XReaderActions.PARSE_AS_IS),
												new FSM.FSMLine<>(XReaderState.LINE,XReaderTerminals.START_INLINE,XReaderState.AWAITING_NL,XReaderActions.PARSE_TRUNCATED),
												new FSM.FSMLine<>(XReaderState.LINE,XReaderTerminals.START_MULTILINE,XReaderState.IN_MILTILINE,XReaderActions.CLEAN,XReaderActions.COLLECT_PREFIX),
												new FSM.FSMLine<>(XReaderState.LINE,XReaderTerminals.CONTINUATION,XReaderState.AWAITING_CONTINUED_NL,XReaderActions.CLEAN,XReaderActions.COLLECT_PREFIX),
												
												new FSM.FSMLine<>(XReaderState.CONTINUE,XReaderTerminals.NL,XReaderState.LINE,XReaderActions.COLLECT,XReaderActions.EXTRACT_AND_PARSE),
												new FSM.FSMLine<>(XReaderState.CONTINUE,XReaderTerminals.START_INLINE,XReaderState.AWAITING_NL,XReaderActions.COLLECT_PREFIX,XReaderActions.EXTRACT_AND_PARSE),
												new FSM.FSMLine<>(XReaderState.CONTINUE,XReaderTerminals.CONTINUATION,XReaderState.AWAITING_CONTINUED_NL,XReaderActions.COLLECT_PREFIX),
												new FSM.FSMLine<>(XReaderState.CONTINUE,XReaderTerminals.START_MULTILINE,XReaderState.IN_MILTILINE_CONTINUED,XReaderActions.COLLECT_PREFIX),
									
												new FSM.FSMLine<>(XReaderState.IN_MILTILINE,XReaderTerminals.END_MULTILINE,XReaderState.CONTINUE,XReaderActions.PARSE_TAIL),
												new FSM.FSMLine<>(XReaderState.IN_MILTILINE,XReaderTerminals.NL,XReaderState.IN_MILTILINE),
									
												new FSM.FSMLine<>(XReaderState.IN_MILTILINE_CONTINUED,XReaderTerminals.END_MULTILINE,XReaderState.CONTINUE,XReaderActions.PARSE_TAIL),
												
												new FSM.FSMLine<>(XReaderState.AWAITING_NL,XReaderTerminals.NL,XReaderState.LINE),
												new FSM.FSMLine<>(XReaderState.AWAITING_CONTINUED_NL,XReaderTerminals.NL,XReaderState.CONTINUE),
												new FSM.FSMLine<>(XReaderState.AWAITING_CONTINUED_NL,XReaderTerminals.START_MULTILINE,XReaderState.AWAITING_CONTINUED_MULTILINE_NL),

												new FSM.FSMLine<>(XReaderState.AWAITING_CONTINUED_MULTILINE_NL,XReaderTerminals.END_MULTILINE,XReaderState.AWAITING_CONTINUED_NL),
												new FSM.FSMLine<>(XReaderState.AWAITING_CONTINUED_MULTILINE_NL,XReaderTerminals.NL,XReaderState.IN_MILTILINE_CONTINUED),
											};
	
	private enum CommandType {
		CMD_UNKNOWN, CMD_COMMAND, CMD_DEFINE, CMD_ELSE, CMD_END, CMD_ERROR, CMD_IFDEF, CMD_IFNDEF, CMD_INCLUDE, CMD_STDOUT, CMD_TRANSLATE, CMD_UNDEF, CMD_XCOMMAND, CMD_XTRANSLATE  
	}

	static {
		COMMANDS.placeName("command",CommandType.CMD_COMMAND);
		COMMANDS.placeName("define",CommandType.CMD_DEFINE);
		COMMANDS.placeName("else",CommandType.CMD_ELSE);
		COMMANDS.placeName("end",CommandType.CMD_END);
		COMMANDS.placeName("error",CommandType.CMD_ERROR);
		COMMANDS.placeName("ifdef",CommandType.CMD_IFDEF);
		COMMANDS.placeName("ifndef",CommandType.CMD_IFNDEF);
		COMMANDS.placeName("include",CommandType.CMD_INCLUDE);
		COMMANDS.placeName("stdout",CommandType.CMD_STDOUT);
		COMMANDS.placeName("translate",CommandType.CMD_TRANSLATE);
		COMMANDS.placeName("undef",CommandType.CMD_UNDEF);
		COMMANDS.placeName("xcommand",CommandType.CMD_XCOMMAND);
		COMMANDS.placeName("xtranslate",CommandType.CMD_XTRANSLATE);
		
		try{EXPRESSION_SKIPPER = CompilerUtils.buildRuleBasedParserClass(PreprocessingXReader.class.getPackageName()+".ExpressionSkipper", Expression.class, Utils.fromResource(PreprocessingXReader.class.getResource("expression.txt"), PureLibSettings.DEFAULT_CONTENT_ENCODING), LOADER);
			X_EXPRESSION_SKIPPER = CompilerUtils.buildRuleBasedParserClass(PreprocessingXReader.class.getPackageName()+".XExpressionSkipper", XExpression.class, Utils.fromResource(PreprocessingXReader.class.getResource("x_expression.txt"), PureLibSettings.DEFAULT_CONTENT_ENCODING), LOADER);
		} catch (SyntaxException | IOException e) {
			throw new PreparationException(e.getLocalizedMessage(), e);
		}
	}

	private final GrowableCharArray<GrowableCharArray<?>>			collector = new GrowableCharArray<GrowableCharArray<?>>(false);
	private final int[]												forNames = new int[2], forStrings = new int[2], forRanges = new int[2];
	private final SyntaxTreeInterface<List<PatternAndSubstitutor>>	commands = new AndOrTree<>();
	private final SyntaxTreeInterface<List<PatternAndSubstitutor>>	translations = new AndOrTree<>();
	private final RuleBasedParser<Expression, Object>				skipper;
	private final RuleBasedParser<XExpression, Object>				xSkipper;
	private final SyntaxTreeInterface<Object>						tree = new AndOrTree<>();
	private final StringBuilder										sb = new StringBuilder();
	private final FSM<XReaderTerminals,XReaderState,XReaderActions,ActionsCallback>	fsm = new FSM<>(this::processFSM, XReaderState.LINE, FSM_TABLE);
	private final char			inlineStart, multilineStart, multilineEnd;
	private long				nestedMask = ENABLED_OUTPUT_MASK, skipMask = ENABLED_OUTPUT_MASK;
	private boolean				insideMultiline = false;
	private int					currentDepth = -1;
	
	
	/**
	 * <p>Create reader with the nested source and default settings.</p>
	 * @param nestedReader reader to use as content source. Can't be null
	 * @throws IOException on any I/O errors 
	 */
	public PreprocessingXReader(final Reader nestedReader) throws IOException {
		this(nestedReader,Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.RECURSIVE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/"));
	}
	
	/**
	 * <p>Create reader with the nested source and explicitly typed settings.</p>
	 * @param nestedReader reader to use as content source. Can't be null
	 * @param varsAndOptions explicitly typed options. Can't be null. Use static string keys defined in the class as key names for the map. 
	 * Changing it's content during processing data is not affected on processing behavior, but will take effect on all #include content 
	 * @throws IOException on any I/O errors 
	 */
	public PreprocessingXReader(final Reader nestedReader, final Map<String,Object> varsAndOptions) throws IOException {
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
	 * @throws IOException on any I/O errors 
	 */
	public PreprocessingXReader(final Reader nestedReader, final Map<String,Object> varsAndOptions, final IncludeCallback includeCallback) throws IOException {
		this(null,nestedReader,varsAndOptions,includeCallback);
	}
	
	protected PreprocessingXReader(final URI nestedReaderURI, final Reader nestedReader, final Map<String,Object> varsAndOptions, final IncludeCallback includeCallback) throws IOException {
		super(nestedReaderURI, nestedReader, varsAndOptions, includeCallback);
		try{this.skipper = (RuleBasedParser<Expression, Object>)EXPRESSION_SKIPPER.getConstructor(Class.class,SyntaxTreeInterface.class).newInstance(Expression.class, tree);
			this.xSkipper = (RuleBasedParser<XExpression, Object>)X_EXPRESSION_SKIPPER.getConstructor(Class.class,SyntaxTreeInterface.class).newInstance(Expression.class, tree);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IOException(e.getLocalizedMessage(), e);
		}
		this.inlineStart = getInlineComment().length > 0 ? getInlineComment()[0] : '\uFFFF'; 
		this.multilineStart = getStartMultilineComment().length > 0 ? getStartMultilineComment()[0] : '\uFFFF'; 
		this.multilineEnd = getEndMultilineComment().length > 0 ? getEndMultilineComment()[0] : '\uFFFF'; 
//		fsm.debugEnable(PureLibSettings.CURRENT_LOGGER, false);
	}	

	@Override
	protected void internalProcessLine(final long displacement, final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
		final int	to = from + length;
		boolean		skipConstant = false;
		
		forRanges[0] = from;
		
		for(int index = from; index < to; index++) {
			forRanges[1] = index;
			
			if (data[index] == '\"') {
				skipConstant = !skipConstant;
			}
			else if (!skipConstant) {
				try{if (data[index] == ';') {
						fsm.processTerminal(XReaderTerminals.CONTINUATION, (a)->{
//							System.err.println("P1:");
							for (XReaderActions item : a) {
								switch(item) {
									case CLEAN					:
										sb.setLength(0);
										break;
									case COLLECT				:
										sb.append(data, forRanges[0], to-forRanges[0]);
										break;
									case COLLECT_PREFIX			:
										sb.append(data, forRanges[0], forRanges[1]-forRanges[0]);
										break;
									case EXTRACT_AND_PARSE		:
										internalProcessLine0(displacement, lineNo, sb.toString().toCharArray(), 0, sb.length());
										break;
									case PARSE_AS_IS			:
										internalProcessLine0(displacement, lineNo, data, forRanges[0], forRanges[1]-forRanges[0]);
										break;
									case PARSE_TAIL				:
										forRanges[0] = forRanges[1];
										break;
									case PARSE_TRUNCATED		:
										internalProcessLine0(displacement, lineNo, data, forRanges[0], forRanges[1]-forRanges[0]);
										putContent(NL);
										break;
									default:
										break;
									}
							}
						});
					}
					else if (data[index] == '\r' || data[index] == '\n') {
						fsm.processTerminal(XReaderTerminals.NL, (a)->{
//							System.err.println("P2:");
							for (XReaderActions item : a) {
								switch(item) {
									case CLEAN					:
										sb.setLength(0);
										break;
									case COLLECT				:
										sb.append(data, forRanges[0], forRanges[1]-forRanges[0]+1);
										break;
									case COLLECT_PREFIX			:
										sb.append(data, forRanges[0], forRanges[1]-forRanges[0]-1);
										break;
									case EXTRACT_AND_PARSE		:
										internalProcessLine0(displacement, lineNo, sb.toString().toCharArray(), 0, sb.length());
										break;
									case PARSE_AS_IS			:
										internalProcessLine0(displacement, lineNo, data, forRanges[0], forRanges[1]-forRanges[0]+1);
										break;
									case PARSE_TAIL				:
										forRanges[0] = forRanges[1];
										break;
									case PARSE_TRUNCATED		:
										internalProcessLine0(displacement, lineNo, data, forRanges[0], forRanges[1]-forRanges[0]);
										putContent(NL);
										break;
									default:
										break;
									}
							}
						});
					}
					else if (data[index] == inlineStart &&CharUtils.compare(data, index, getInlineComment())) {
						fsm.processTerminal(XReaderTerminals.START_INLINE, (a)->{
//							System.err.println("P3:");
							for (XReaderActions item : a) {
								switch(item) {
									case CLEAN					:
										sb.setLength(0);
										break;
									case COLLECT				:
										sb.append(data, forRanges[0], forRanges[1]-forRanges[0]);
										break;
									case COLLECT_PREFIX			:
										sb.append(data, forRanges[0], forRanges[1]-forRanges[0]);
										break;
									case EXTRACT_AND_PARSE		:
										internalProcessLine0(displacement, lineNo, sb.toString().toCharArray(), 0, sb.length());
										putContent(NL);
										break;
									case PARSE_AS_IS			:
										internalProcessLine0(displacement, lineNo, data, forRanges[0], forRanges[1]-forRanges[0]);
										break;
									case PARSE_TAIL				:
										forRanges[0] = forRanges[1];
										break;
									case PARSE_TRUNCATED		:
										internalProcessLine0(displacement, lineNo, data, forRanges[0], forRanges[1]-forRanges[0]);
										putContent(NL);
										break;
									default:
										break;
									}
							}
						});
					}
					else if (data[index] == multilineStart &&CharUtils.compare(data, index, getStartMultilineComment())) {
						fsm.processTerminal(XReaderTerminals.START_MULTILINE, (a)->{
//							System.err.println("P4:");
							for (XReaderActions item : a) {
								switch(item) {
									case CLEAN					:
										sb.setLength(0);
										break;
									case COLLECT				:
										sb.append(data, forRanges[0], forRanges[1]-forRanges[0]);
										break;
									case COLLECT_PREFIX			:
										sb.append(data, forRanges[0], forRanges[1]-forRanges[0]);
										break;
									case EXTRACT_AND_PARSE		:
										internalProcessLine0(displacement, lineNo, sb.toString().toCharArray(), 0, sb.length());
										break;
									case PARSE_AS_IS			:
										internalProcessLine0(displacement, lineNo, data, forRanges[0], forRanges[1]-forRanges[0]);
										break;
									case PARSE_TAIL				:
										forRanges[0] = forRanges[1];
										break;
									case PARSE_TRUNCATED		:
										internalProcessLine0(displacement, lineNo, data, forRanges[0], forRanges[1]-forRanges[0]);
										putContent(NL);
										break;
									default:
										break;
									}
							}
						});
					}
					else if (data[index] == multilineEnd &&CharUtils.compare(data, index, getEndMultilineComment())) {
						fsm.processTerminal(XReaderTerminals.END_MULTILINE, (a)->{
//							System.err.println("P5:");
							for (XReaderActions item : a) {
								switch(item) {
									case CLEAN					:
										sb.setLength(0);
										break;
									case COLLECT				:
										sb.append(data, forRanges[0], forRanges[1]-forRanges[0]);
										break;
									case COLLECT_PREFIX			:
										sb.append(data, forRanges[0], forRanges[1]-forRanges[0]-1);
										break;
									case EXTRACT_AND_PARSE		:
										internalProcessLine0(displacement, lineNo, sb.toString().toCharArray(), 0, sb.length());
										break;
									case PARSE_AS_IS			:
										internalProcessLine0(displacement, lineNo, data, forRanges[0], forRanges[1]-forRanges[0]);
										break;
									case PARSE_TAIL				:
										forRanges[0] = forRanges[1] + getEndMultilineComment().length;
										break;
									case PARSE_TRUNCATED		:
										internalProcessLine0(displacement, lineNo, data, forRanges[0], forRanges[1]-forRanges[0]);
										putContent(NL);
										break;
									default:
										break;
									}
							}
						});
					}
				} catch (FlowException e) {
					if (e.getCause() instanceof SyntaxException) {
						throw (SyntaxException)e.getCause(); 
					}
					else {
						throw new SyntaxException(lineNo, index-from, e.getLocalizedMessage(), e); 
					}
				}
			}
		}
	}	
	
	private void processFSM(final FSM<XReaderTerminals,XReaderState,XReaderActions,ActionsCallback> fsm, final XReaderTerminals terminal, final XReaderState fromState, final XReaderState toState, final XReaderActions[] action, final ActionsCallback parameter) throws FlowException {
		try{parameter.process(action);
		} catch (Exception e) {
			throw new FlowException(e);
		}
	}
	
	private void internalProcessLine0(final long displacement, final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
		final int	start = from, to = from + length;
		
		if (data[from] == '#') {
			from = CharUtils.skipBlank(data,CharUtils.parseName(data, from + 1, forNames),true);
			
			switch (getCommandType(data,forNames[0],forNames[1]+1)) {
				case CMD_DEFINE		:
					if (Character.isJavaIdentifierStart(data[from])) {
						from = CharUtils.skipBlank(data,CharUtils.parseName(data, from, forNames),true);
						from = CharUtils.skipBlank(data,toEndOfLine(data, from, forStrings),true);
						
						putDefinition(data, forNames[0], forNames[1]+1, Arrays.copyOfRange(data, forStrings[0], forStrings[1]+1));
					}
					else {
						throw new SyntaxException(lineNo, from - start, "Missing name in the #define directive");
					}
					break;
				case CMD_ERROR		:
					from = CharUtils.skipBlank(data,toEndOfLine(data, from, forStrings), true);
					
					throw new SyntaxException(lineNo, 0, new String(data, forStrings[0], forStrings[1]-forStrings[0]));
				case CMD_STDOUT		:
					from = CharUtils.skipBlank(data,toEndOfLine(data, from, forStrings),true);
					
					putContent(data, forStrings[0], forStrings[1]-forStrings[0]);
					putContent(NL);
					break;
				case CMD_IFDEF		:
					if (Character.isJavaIdentifierStart(data[from])) {
						from = CharUtils.skipBlank(data,CharUtils.parseName(data, from, forNames),true);
						
						currentDepth++;
						nestedMask ^= (1L << currentDepth);
						if (!definitionExists(data, forNames[0], forNames[1]+1)) {
							skipMask ^= (1L << currentDepth);
						}
					}
					else {
						throw new SyntaxException(lineNo, from - start, "Missing name in the #ifdef directive");
					}
					break;
				case CMD_IFNDEF		:
					if (Character.isJavaIdentifierStart(data[from])) {
						from = CharUtils.skipBlank(data,CharUtils.parseName(data, from, forNames),true);
	
						currentDepth++;
						nestedMask ^= (1L << currentDepth);
						if (definitionExists(data, forNames[0], forNames[1]+1)) {
							skipMask ^= (1L << currentDepth);
						}
					}
					else {
						throw new SyntaxException(lineNo, from - start, "Missing name in the #ifndef directive");
					}
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
						if (insideMultiline) {
							insideMultiline = false;
							putContent(getEndMultilineComment());
							putContent(NL);
						}
					}
					break;
				case CMD_INCLUDE	:
					final StringBuilder	sb = new StringBuilder();
					final URI	uri;
					
					switch (data[from]) {
						case '\"' :
							from = CharUtils.parseString(data,from + 1,'\"',sb);
							uri = URI.create("root:/"+this.getClass().getCanonicalName()+"/bt.clipper/includes/"+sb);
							break;
						case '<'  :
							from = CharUtils.parseString(data,from + 1,'>',sb);
							uri = URI.create("root:/"+this.getClass().getCanonicalName()+"/bt.clipper/includes/"+sb);
							break;
						default :
							from = toEndOfLine(data, from, forStrings);
							sb.append(data, forStrings[0], forStrings[1]);
							uri = URI.create(new String(data, forStrings[0], forStrings[1]-forStrings[0]+1));
							break;
					}
					pushReader(uri, getIncludeCallback().getIncludeStream(uri));
					break;
				case CMD_UNDEF		:
					if (Character.isJavaIdentifierStart(data[from])) {
						from = CharUtils.skipBlank(data,CharUtils.parseName(data, from, forNames),true);
						
						removeDefinition(data, forNames[0], forNames[1]+1);
					}
					else {
						throw new SyntaxException(lineNo, from - start, "Missing name in the #undef directive");
					}
					break;
				case CMD_COMMAND	:
					processCommand(CommandType.CMD_COMMAND,data,from);
					break;
				case CMD_XCOMMAND	:
					processCommand(CommandType.CMD_XCOMMAND,data,from);
					break;
				case CMD_TRANSLATE	:
					processCommand(CommandType.CMD_TRANSLATE,data,from);
					break;
				case CMD_XTRANSLATE	:
					processCommand(CommandType.CMD_XTRANSLATE,data,from);
					break;
				case CMD_UNKNOWN	:
					putContent(data, start, length);
					break;
				default:
					throw new UnsupportedOperationException("Command type ["+getCommandType(data,forNames[0],forNames[1])+"] is not supprted yet");
			}			
		}
		else if (skipMask != ENABLED_OUTPUT_MASK) {
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
	protected AbstractPreprocessingReader newDelegate(final URI nestedReaderURI, final Reader nestedReader, final Map<String, Object> varsAndOptions, final IncludeCallback includeCallback) throws IOException, SyntaxException {
		return new PreprocessingXReader(nestedReaderURI, nestedReader, varsAndOptions, includeCallback);
	}
	
	@Override
	protected void substitute(final char[] data, final int from, final int length) throws SyntaxException {	// This method is used to reduce stringbuilder operations
		boolean	sameFirst = true;
		long	id;
		int		end;
		
		for (int index = from, to = from + length; index < to; index++) {
			if (Character.isJavaIdentifierStart(data[index])) {
				end = index;
				while (end < to && Character.isJavaIdentifierPart(data[end])) {
					end++;
				}
				if (sameFirst) {
					if ((id = commands.seekName(data,index,end)) >= 0) {
						for(PatternAndSubstitutor	pas : commands.getCargo(id)) {
							pas.process(data, from, (c,f,l)->putContent(c, f, l));
						}
						return;
					}
					else {
						sameFirst = false;
					}
				}
				if (translations.seekName(data,index,end) >= 0) {
					final StringBuilder	sb = new StringBuilder();
					
					putContent(data,from,index-from);
					substitute(sb.append(data,index,to-index),0);
					putContent(sb.toString().toCharArray());
					return;
				}
				else {
					index = end;
				}
			}
		}
		super.substitute(data,from,length);
	}

	
	
	protected CommandType getCommandType(final char[] data, final int start, final int end) {
		final long	id = isIgnoreCase() ? COMMANDS.seekNameI(data,start,end) : COMMANDS.seekName(data,start,end); 
		
		if (id >= 0) {
			return COMMANDS.getCargo(id);
		}
		else {
			return CommandType.CMD_UNKNOWN;
		}
	}

	private void processCommand(final CommandType command, final char[] content, final int from) throws SyntaxException {
		PatternAndSubstitutor	pas; 
		
		switch (command) {
			case CMD_COMMAND		:
				pas = CommandParser.build(content, from, true);
				placePattern(commands, pas.getKeyword(), pas);
				if (pas.getKeyword().length > 4) {
					placePattern(commands, trunc2four(pas.getKeyword()), pas);
				}
				break;
			case CMD_TRANSLATE		:
				pas = CommandParser.build(content, from, true);
				placePattern(translations, pas.getKeyword(), pas);
				if (pas.getKeyword().length > 4) {
					placePattern(translations, trunc2four(pas.getKeyword()), pas);
				}
				break;
			case CMD_XCOMMAND		:
				pas = CommandParser.build(content, from, false);
				placePattern(commands, pas.getKeyword(), pas);
				break;
			case CMD_XTRANSLATE		:
				pas = CommandParser.build(content, from, false);
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
		definitions.placeName(data, from, to, value);
	}

	private boolean definitionExists(final char[] data, final int from, final int to) {
		return (isIgnoreCase() ? definitions.seekNameI(data, from, to) : definitions.seekName(data, from, to)) >= 0; 
	}

	private int toEndOfLine(final char[] data, int from, final int[] ranges) {
		ranges[0] = CharUtils.skipBlank(data, from, true);
		
		while (data[from] != '\r' && data[from] != '\n') {
			if (data[from] == '/' && data[from + 1] == '/') {
				break;
			}
			else {
				from++;
			}
		}
		while (from > ranges[0] && data[from] <= ' ') {
			from--;
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
		final long	id = isIgnoreCase() ? definitions.seekNameI(data, from, to) : definitions.seekName(data, from, to); 
		
		if (id >= 0) {
			definitions.removeName(id);
		}
	}
}
