package chav1961.bt.matrix.macros;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import chav1961.bt.matrix.macros.NestedReader.Line;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class InternalParser {
	private static final SyntaxTreeInterface<Lexema>	NAMES = new AndOrTree<>(1, 1);
	private static final char[]							TRUE = "true".toCharArray();
	private static final char[]							FALSE = "false".toCharArray();
	private static final char[]							IN = "in".toCharArray();
	
	static {
		NAMES.placeName((CharSequence)"param", new Lexema(0, LexType.KWD_PARAM));
		NAMES.placeName((CharSequence)"var", new Lexema(0, LexType.KWD_VAR));
		NAMES.placeName((CharSequence)"set", new Lexema(0, LexType.KWD_SET));
		NAMES.placeName((CharSequence)"if", new Lexema(0, LexType.KWD_IF));
		NAMES.placeName((CharSequence)"elsif", new Lexema(0, LexType.KWD_ELSIF));
		NAMES.placeName((CharSequence)"else", new Lexema(0, LexType.KWD_ELSIF));
		NAMES.placeName((CharSequence)"endif", new Lexema(0, LexType.KWD_ENDIF));
		NAMES.placeName((CharSequence)"while", new Lexema(0, LexType.KWD_WHILE));
		NAMES.placeName((CharSequence)"endwhile", new Lexema(0, LexType.KWD_ENDWHILE));
		NAMES.placeName((CharSequence)"for", new Lexema(0, LexType.KWD_FOR));
		NAMES.placeName((CharSequence)"endfor", new Lexema(0, LexType.KWD_ENDFOR));
		NAMES.placeName((CharSequence)"case", new Lexema(0, LexType.KWD_CASE));
		NAMES.placeName((CharSequence)"of", new Lexema(0, LexType.KWD_OF));
		NAMES.placeName((CharSequence)"default", new Lexema(0, LexType.KWD_DEFAULT));
		NAMES.placeName((CharSequence)"endcase", new Lexema(0, LexType.KWD_ENDCASE));
		NAMES.placeName((CharSequence)"break", new Lexema(0, LexType.KWD_BREAK));
		NAMES.placeName((CharSequence)"continue", new Lexema(0, LexType.KWD_CONTINUE));
		NAMES.placeName((CharSequence)"error", new Lexema(0, LexType.KWD_ERROR));
		NAMES.placeName((CharSequence)"warning", new Lexema(0, LexType.KWD_WARNING));
		NAMES.placeName((CharSequence)"print", new Lexema(0, LexType.KWD_PRINT));
		NAMES.placeName((CharSequence)"include", new Lexema(0, LexType.KWD_INCLUDE));
		NAMES.placeName((CharSequence)"call", new Lexema(0, LexType.KWD_CALL));
	}
	
	private final StringBuilder	sb = new StringBuilder();
	final char[]				keywordPrefix;
	final char[]				substPrefix;
	final char[]				substSuffix;
	final char					startKeyword;
	final char					startSubst;
	final boolean				suffixIsMandatory;
	final int[]					forInt = new int[2];
	final long[]				forLong = new long[2]; 	

	public InternalParser(final char[] keywordPrefix, final char[] substPrefix, final char[] substSuffix, final boolean suffixIsMandatory) {
		this.keywordPrefix = keywordPrefix;
		this.startKeyword = keywordPrefix[0];
		this.substPrefix = substPrefix;
		this.startSubst = substPrefix[0];
		this.substSuffix = substSuffix;
		this.suffixIsMandatory = suffixIsMandatory;
	}
	
	List<Lexema> parseMacroLine(final Line line) throws SyntaxException {
		final List<Lexema>	result = new ArrayList<>();
		final char[]		source = line.content;
		int					from = line.from;
		
loop:	for(;;) {
			while (source[from] <= ' ' && source[from] != '\n') {
				from++;
			}
			switch (source[from]) {
				case '\n' :
					result.add(new Lexema(from, LexType.EOF));
					break loop;
				case '(' :
					result.add(new Lexema(from++, LexType.OPEN));
					break;
				case '[' :
					result.add(new Lexema(from++, LexType.OPENB));
					break;
				case '{' :
					result.add(new Lexema(from++, LexType.OPENF));
					break;
				case ')' :
					result.add(new Lexema(from++, LexType.CLOSE));
					break;
				case ']' :
					result.add(new Lexema(from++, LexType.CLOSEB));
					break;
				case '}' :
					result.add(new Lexema(from++, LexType.CLOSEF));
					break;
				case ':' :
					result.add(new Lexema(from++, LexType.COLON));
					break;
				case ',' :
					result.add(new Lexema(from++, LexType.LIST));
					break;
				case '+' :
					result.add(new Lexema(from++, LexType.ADD));
					break;
				case '-' :
					result.add(new Lexema(from++, LexType.SUB));
					break;
				case '*' :
					result.add(new Lexema(from++, LexType.MUL));
					break;
				case '/' :
					result.add(new Lexema(from++, LexType.DIV));
					break;
				case '%' :
					result.add(new Lexema(from++, LexType.MOD));
					break;
				case '.' :
					if (source[from + 1] == '.') {
						result.add(new Lexema(from, LexType.RANGE));
						from += 2;
					}
					else {
						result.add(new Lexema(from++, LexType.DOT));
					}
					break;
				case '=' :
					if (source[from + 1] == '=') {
						result.add(new Lexema(from, LexType.EQ));
						from += 2;
					}
					else {
						result.add(new Lexema(from++, LexType.ASSIGN));
					}
					break;
				case '<' :
					if (source[from + 1] == '=') {
						result.add(new Lexema(from, LexType.LE));
						from += 2;
					}
					else {
						result.add(new Lexema(from++, LexType.LT));
					}
					break;
				case '>' :
					if (source[from + 1] == '=') {
						result.add(new Lexema(from, LexType.GE));
						from += 2;
					}
					else {
						result.add(new Lexema(from++, LexType.GT));
					}
					break;
				case '!' :
					if (source[from + 1] == '=') {
						result.add(new Lexema(from, LexType.NE));
						from += 2;
					}
					else {
						result.add(new Lexema(from++, LexType.NOT));
					}
					break;
				case '&' :
					if (source[from + 1] == '&') {
						result.add(new Lexema(from, LexType.AND));
						from += 2;
					}
					else {
						throw new SyntaxException(0, from, "Unknown lexema");
					}
					break;
				case '|' :
					if (source[from + 1] == '|') {
						result.add(new Lexema(from, LexType.OR));
						from += 2;
					}
					else {
						throw new SyntaxException(0, from, "Unknown lexema");
					}
					break;
				case '\"' :
					final int	startConstC = from;
					
					from = CharUtils.parseString(source, from, '\"', sb);
					if (source[from] == '\"') {
						result.add(new Lexema(startConstC, LexType.CONSTANT_C, sb.toString().toCharArray()));
						from++;
					}
					else {
						throw new SyntaxException(0, from, "Unclosed quota");
					}
					break;
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					final int	startConstN = from;
					
					from = CharUtils.parseNumber(source, from, forLong, CharUtils.PREF_LONG | CharUtils.PREF_DOUBLE, false);
					if (forLong[1] == CharUtils.PREF_LONG) {
						result.add(new Lexema(startConstN, LexType.CONSTANT_I, forLong[0]));
					}
					else {
						result.add(new Lexema(startConstN, LexType.CONSTANT_R, forLong[0]));
					}
					break;
				default :
					if (Character.isJavaIdentifierStart(source[from])) {
						final int	startName = from;
						
						from = CharUtils.parseName(source, from, forInt);

						if (CharUtils.compare(source, startName, TRUE) && forInt[1] - forInt[0] == TRUE.length) {
							result.add(new Lexema(startName, LexType.CONSTANT_B, 1));
						}
						else if (CharUtils.compare(source, startName, FALSE) && forInt[1] - forInt[0] == FALSE.length) {
							result.add(new Lexema(startName, LexType.CONSTANT_B, 0));
						}
						else if (CharUtils.compare(source, startName, IN) && forInt[1] - forInt[0] == IN.length) {
							result.add(new Lexema(startName, LexType.KWD_IN, 0));
						}
						else {
							result.add(new Lexema(startName, LexType.NAME, Arrays.copyOfRange(source, forInt[0], forInt[1])));
						}
					}
					else if (source[from] == startKeyword && CharUtils.compare(source, from, keywordPrefix)) {
						final int	startKwd = from + keywordPrefix.length;
						
						from = CharUtils.parseName(source, startKwd, forInt);
						final long	id = NAMES.seekName(source, forInt[0], forInt[1]);
						
						if (id > 0) {
							final Lexema	temp = NAMES.getCargo(id);
							
							result.add(new Lexema(startKwd, temp.type));
						}
						else {
							throw new SyntaxException(0, from, "Unsupported keyword");
						}
					}
					else {
						throw new SyntaxException(0, from, "Unknown lexema ");
					}
			}
		}
		return result;
	}

	List<Function<char[], char[]>> parseSubstLine(final Line line, final Function<char[], char[]> callback) throws SyntaxException {
		final List<Function<char[], char[]>>	result = new ArrayList<>();
		final char			start = startSubst;
		final char[]		source = line.content;
		int					from = line.from, begin = from, end;
		
		for(;;) {
			if (source[from] == '\n') {
				end = from;
				break;
			}
			else if (source[from] == start && CharUtils.compare(source, from, substPrefix)) {
				end = from - 1;
				from = CharUtils.parseName(source, from + substPrefix.length, forInt);
				
				if (suffixIsMandatory && CharUtils.compare(source, from, substSuffix)) {
					from += substSuffix.length;
				}
				final char[]	piece = Arrays.copyOfRange(source, begin, end);
				
				result.add((f)->piece);
				result.add((f)->callback.apply(piece));
			}
			else {
				from++;
			}
		}
		final char[]	piece = Arrays.copyOfRange(source, begin, end);
		
		result.add((f)->piece);
		return result;
	}
	
	static enum Priority {
		TERM,
		UNARY,
		MUL,
		ADD,
		COMPARE,
		NOT,
		AND,
		OR,
		ASSIGN,
		UNKNOWN
	}
	
	static enum LexType {
		EOF,
		CONSTANT_C(Priority.TERM),
		CONSTANT_I(Priority.TERM),
		CONSTANT_R(Priority.TERM),
		CONSTANT_B(Priority.TERM),
		NAME,
		TYPE,
		ARRAY,
		OPEN,
		OPENB,
		OPENF,
		CLOSE,
		CLOSEB,
		CLOSEF,
		DOT,
		COLON,
		LIST,
		RANGE,
		ASSIGN(Priority.ASSIGN),
		ADD(Priority.ADD),
		SUB(Priority.ADD),
		MUL(Priority.MUL),
		DIV(Priority.MUL),
		MOD(Priority.MUL),
		EQ(Priority.COMPARE),
		LT(Priority.COMPARE),
		LE(Priority.COMPARE),
		GT(Priority.COMPARE),
		GE(Priority.COMPARE),
		NE(Priority.COMPARE),
		NOT(Priority.NOT),
		AND(Priority.AND),
		OR(Priority.OR),
		KWD_IN,
		KWD_PARAM,
		KWD_VAR,
		KWD_SET,
		KWD_IF,
		KWD_ELSIF,
		KWD_ELSE,
		KWD_ENDIF,
		KWD_WHILE,
		KWD_ENDWHILE,
		KWD_FOR,
		KWD_ENDFOR,
		KWD_CASE,
		KWD_OF,
		KWD_DEFAULT,
		KWD_ENDCASE,
		KWD_BREAK,
		KWD_CONTINUE,
		KWD_ERROR,
		KWD_WARNING,
		KWD_PRINT,
		KWD_INCLUDE,
		KWD_CALL;
		
		private final Priority	prty;
		
		private LexType() {
			this.prty = Priority.UNKNOWN;
		}

		private LexType(final Priority prty) {
			this.prty = prty;
		}
		
		public Priority getPriority() {
			return prty;
		}
	}
	
	
	static class Lexema {
		final int		pos;
		final LexType	type;
		final long		value;
		final Object	content;
		
		Lexema(final int pos, final LexType type) {
			this(pos, type, 0, null);
		}

		Lexema(final int pos, final LexType type, final long value) {
			this(pos, type, value, null);
		}

		Lexema(final int pos, final LexType type, final Object content) {
			this(pos, type, 0, content);
		}
		
		Lexema(final int pos, final LexType type, final long value, final Object content) {
			this.pos = pos;
			this.type = type;
			this.value = value;
			this.content = content;
		}

		@Override
		public String toString() {
			return "Lexema [pos=" + pos + ", type=" + type + ", value=" + value + ", content=" + content + "]";
		}
	}
}
