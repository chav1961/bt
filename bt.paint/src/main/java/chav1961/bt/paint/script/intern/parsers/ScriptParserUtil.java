package chav1961.bt.paint.script.intern.parsers;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chav1961.bt.paint.script.interfaces.CanvasWrapper;
import chav1961.bt.paint.script.interfaces.ClipboardWrapper;
import chav1961.bt.paint.script.interfaces.ColorWrapper;
import chav1961.bt.paint.script.interfaces.FontWrapper;
import chav1961.bt.paint.script.interfaces.ImageWrapper;
import chav1961.bt.paint.script.interfaces.PointWrapper;
import chav1961.bt.paint.script.interfaces.RectWrapper;
import chav1961.bt.paint.script.interfaces.SizeWrapper;
import chav1961.bt.paint.script.interfaces.StrokeWrapper;
import chav1961.bt.paint.script.interfaces.SystemWrapper;
import chav1961.bt.paint.script.interfaces.TransformWrapper;
import chav1961.bt.paint.script.intern.interfaces.LexTypes;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

//<prog>::=<anon_block>[{<function>|<procedure>]}]...
//<anon_block>::=[<declarations>][<body>]
//<declarations>::='var'<declaration_list>
//<declaration_list>::=<declaration>[,<declaration>]...
//<body>::='begin'<statements>'end'
//<declaration>::=<@name>':'<type>[':='<initial>]
//<type>::={<simple_type>|<complex_type>}
//<simple_type>::={'int'|'real'|'str'|'bool'|'color'|'point'|'rect'|'font'|'transform'|'stroke'|'image'}
//<complex_type>::={'array''of'<simple_type>|'map''of'<simple_type>}
//<statements>::=<statement>[';'<statement>]...>
//<statement>::={<assignment>|<if>|<while>|<do>|<for>|<case>|<continue>|<break>|<call>|<return>|<sequence>}
//<sequence>::='{'<statements>'}'
//<assignment>::=<left_part>':='<right_part>
//<if>::='if'<cond>'then'<statement>['else'<statement>]
//<while>::='while'<cond>'do'<statement>
//<do>::='do'<statement>'while'<cond>
//<for>::='for'<var>{':='<right_part>'to'<right_part>['step'<right_part>]|':'<right_part>}'do'<statement>
//<case>::='case'<right_part>['of'<range_list>':'<statements>]...['default'':'<statements>]'end'
//<continue>::='continue'[<@int>]
//<break>::='break'[<@int>]
//<call>::='call'<name>'('<list>')'
//<return>::='return'[<right_part>]
//<range_list>::=<range>[','<range_list>
//<range>::=<right_part>'..'<right_part>
//<list>::=<right_part>[','<list>
//<right_part>::=<andNode>['||'<andNode>]...
//<and_node>::=<notNode>['&&'<notNode>]...
//<not_node>::='!'{<comparison>|<boolean>}
//<comparison>::=<concat>{{'>'|'>='|'<'|'<='|'='|'<>'}<concat>|'in'<range_list>}
//<concat>::=<addNode>[#<addNode>]...
//<addNode>::=<mulNode>[{'+'|'-'|'|'}<mulNode>]...
//<mulNode>::=<negNode>[{'*'|'/'|'%'|'&'|'^'}<negNode>]...
//<negNode>::={'-'|'+'|'~'}<term>
//<term>::={<var>|<const>|<func>|<predefined>}['::'<simple_type>]
//<var>::=<@name>['['<right_part>']'['.'<@name>]
//<const>::={<@int>|<@real>|<@string>|'true'|'false'|'`'<sequence>'`'}
//<func>::=<complex_name>'('[<list>]')'
//<complexName>::=<var>['.'<complexName>]...
//<left_part>::=<var>
//<predefined>::={'system'|'clipboard'|'canvas'|'args'}
//<function>::='func'<@name>'('[<declaration_list>]')'':'<{<simple_type>:<complex_type>}>';'[<declarations>]<body>
//<procedure>::='proc'<@name>'('[<declaration_list>]')'';'[<declarations>]<body>


public class ScriptParserUtil {
	private static final SyntaxTreeInterface<Keywords>		KEYWORDS = new AndOrTree<>();
	private static final Map<Keywords, EntityDescriptor>	PREDEFINED = new HashMap<>();

	private static enum EntityType {
		VAR,
		FUNC,
		PROC
	}
	
	static enum DataTypes {
		UNKNOWN(Object.class),
		INT(int[].class),
		REAL(double[].class),
		STR(char[].class),
		BOOL(boolean[].class),
		COLOR(ColorWrapper.class),
		POINT(PointWrapper.class),
		RECT(RectWrapper.class),
		SIZE(SizeWrapper.class),
		FONT(FontWrapper.class),
		STROKE(StrokeWrapper.class),
		TRANSFORM(TransformWrapper.class),
		IMAGE(ImageWrapper.class);
		
		private final Class<?>	associated;
		
		private DataTypes(final Class<?> associated) {
			this.associated = associated;
		}
		
		public Class<?> getClassAssociated() {
			return associated;
		}
	}

	static enum CollectionType {
		ORDINAL,
		STRUCTURE,
		ARRAY,
		MAP,
		FUNC,
		PROC;
	}

	static enum AccessType {
		GET_FIELD,
		GET_ARRAY_INDEX,
		GET_VAR_INDEX
	}
	
	private static enum OperatorLevelTypes {
		NONE,
		BINARY,
		STRONG_BINARY,
		PREFIX,
		SUFFIX
	}
	
	private static enum OperatorPriorities {
		TERM(OperatorLevelTypes.NONE),
		UNARY(OperatorLevelTypes.PREFIX),
		TYPE(OperatorLevelTypes.SUFFIX),
		BIT_AND(OperatorLevelTypes.BINARY),
		BIT_OR(OperatorLevelTypes.BINARY),
		MULTIPLICATION(OperatorLevelTypes.BINARY),
		ADDITION(OperatorLevelTypes.BINARY),
		COMPARISON(OperatorLevelTypes.STRONG_BINARY),
		BOOL_NOT(OperatorLevelTypes.PREFIX),
		BOOL_AND(OperatorLevelTypes.BINARY),
		BOOL_OR(OperatorLevelTypes.BINARY),
		ASSIGNMENT(OperatorLevelTypes.STRONG_BINARY),
		UNKNOWN(OperatorLevelTypes.NONE);
		
		private final OperatorLevelTypes	levelType;
		
		private OperatorPriorities(final OperatorLevelTypes levelType) {
			this.levelType = levelType;
		}
		
		public OperatorLevelTypes getLevelType() {
			return levelType;
		}
		
		public OperatorPriorities prev() {
			if (ordinal() == 0) {
				throw new IllegalStateException("Prev for the same first entity");
			}
			else {
				return values()[ordinal()-1];
			}
		}

		public OperatorPriorities next() {
			if (ordinal() == values().length - 1) {
				throw new IllegalStateException("Next for the same last entity");
			}
			else {
				return values()[ordinal()+1];
			}
		}
	}
	
	public static enum OperatorTypes {
		INC(OperatorPriorities.UNKNOWN, OperatorPriorities.UNARY, OperatorPriorities.TYPE),
		DEC(OperatorPriorities.UNKNOWN, OperatorPriorities.UNARY, OperatorPriorities.TYPE),
		BIT_INV(OperatorPriorities.UNKNOWN, OperatorPriorities.UNARY, OperatorPriorities.UNKNOWN),
		BIT_AND(OperatorPriorities.BIT_AND, OperatorPriorities.UNARY, OperatorPriorities.UNKNOWN),
		BIT_OR(OperatorPriorities.BIT_OR, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN),
		BIT_XOR(OperatorPriorities.BIT_OR, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN),
		MUL(OperatorPriorities.MULTIPLICATION, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN),
		DIV(OperatorPriorities.MULTIPLICATION, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN),
		MOD(OperatorPriorities.MULTIPLICATION, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN),
		ADD(OperatorPriorities.ADDITION, OperatorPriorities.UNARY, OperatorPriorities.UNKNOWN),
		SUB(OperatorPriorities.ADDITION, OperatorPriorities.UNARY, OperatorPriorities.UNKNOWN),
		GT(OperatorPriorities.COMPARISON, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN),
		GE(OperatorPriorities.COMPARISON, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN),
		LT(OperatorPriorities.COMPARISON, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN),
		LE(OperatorPriorities.COMPARISON, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN),
		EQ(OperatorPriorities.COMPARISON, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN),
		NE(OperatorPriorities.COMPARISON, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN),
		IN(OperatorPriorities.COMPARISON, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN, true),
		BOOL_NOT(OperatorPriorities.UNKNOWN, OperatorPriorities.BOOL_NOT, OperatorPriorities.UNKNOWN),
		BOOL_AND(OperatorPriorities.BOOL_AND, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN),
		BOOL_OR(OperatorPriorities.BOOL_OR, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN),
		ASSIGNMENT(OperatorPriorities.ASSIGNMENT, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN),
		UNKNOWN(OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN, OperatorPriorities.UNKNOWN);
		
		private final OperatorPriorities	infix;
		private final OperatorPriorities	prefix;
		private final OperatorPriorities	suffix;
		private final boolean				listSupported;

		
		private OperatorTypes(final OperatorPriorities infix, final OperatorPriorities prefix, final OperatorPriorities suffix) {
			this(infix, prefix, suffix, false);
		}
		
		private OperatorTypes(final OperatorPriorities infix, final OperatorPriorities prefix, final OperatorPriorities suffix, final boolean listSupported) {
			this.infix = infix;
			this.prefix = prefix;
			this.suffix = suffix;
			this.listSupported = listSupported;
		}
		
		public OperatorPriorities getInfixPriority() {
			return infix;
		}

		public OperatorPriorities getPrefixPriority() {
			return prefix;
		}

		public OperatorPriorities getSuffixPriority() {
			return suffix;
		}
		
		public boolean isListSupported() {
			return listSupported;
		}
	}
	
	private static enum Keywords {
		VAR(LexTypes.STATEMENT),
		BEGIN(LexTypes.STATEMENT),
		END(LexTypes.STATEMENT),
		INT(LexTypes.TYPE, DataTypes.INT),
		REAL(LexTypes.TYPE, DataTypes.REAL),
		STR(LexTypes.TYPE, DataTypes.STR),
		BOOL(LexTypes.TYPE, DataTypes.BOOL),
		COLOR(LexTypes.TYPE, DataTypes.COLOR),
		POINT(LexTypes.TYPE, DataTypes.POINT),
		RECT(LexTypes.TYPE, DataTypes.RECT),
		FONT(LexTypes.TYPE, DataTypes.FONT),
		STROKE(LexTypes.TYPE, DataTypes.STROKE),
		TRANSFORM(LexTypes.TYPE, DataTypes.TRANSFORM),
		IMAGE(LexTypes.TYPE, DataTypes.IMAGE),
		ARRAY(LexTypes.TYPE),
		MAP(LexTypes.TYPE),
		OF(LexTypes.OPTION),
		IF(LexTypes.STATEMENT),
		THEN(LexTypes.OPTION),
		ELSE(LexTypes.OPTION),
		WHILE(LexTypes.STATEMENT),
		DO(LexTypes.STATEMENT),
		FOR(LexTypes.STATEMENT),
		TO(LexTypes.OPTION),
		STEP(LexTypes.OPTION),
		CASE(LexTypes.STATEMENT),
		CONTINUE(LexTypes.STATEMENT),
		BREAK(LexTypes.STATEMENT),
		RETURN(LexTypes.STATEMENT),
		IN(LexTypes.OPERATOR, OperatorTypes.IN),
		TRUE(LexTypes.CONSTANT, DataTypes.BOOL),
		FALSE(LexTypes.CONSTANT, DataTypes.BOOL),
		SYSTEM(LexTypes.PREDEFINED_VAR, SystemWrapper.class),
		CLIPBOARD(LexTypes.PREDEFINED_VAR, ClipboardWrapper.class),
		CANVAS(LexTypes.PREDEFINED_VAR, CanvasWrapper.class),
		ARGS(LexTypes.PREDEFINED_VAR, char[].class),
		FUNC(LexTypes.PART),
		PROC(LexTypes.PART),
		FORWARD(LexTypes.OPTION),
		;
		
		private final LexTypes		type;
		private final DataTypes		dataType;
		private final OperatorTypes	opType;
		private final Class<?>		association;
		
		Keywords(final LexTypes type) {
			this.type = type;
			this.dataType = DataTypes.UNKNOWN;
			this.opType = OperatorTypes.UNKNOWN;
			this.association = null;
		}

		Keywords(final LexTypes type, final DataTypes dataType) {
			this.type = type;
			this.dataType = dataType;
			this.opType = OperatorTypes.UNKNOWN;
			this.association = null;
		}

		Keywords(final LexTypes type, final OperatorTypes opType) {
			this.type = type;
			this.dataType = DataTypes.UNKNOWN;
			this.opType = opType;
			this.association = null;
		}

		Keywords(final LexTypes type, final Class<?> association) {
			this.type = type;
			this.dataType = DataTypes.UNKNOWN;
			this.opType = null;
			this.association = association;
		}
		
		public LexTypes getLexType() {
			return type;
		}
		
		public DataTypes getDataType() {
			return dataType;
		}

		public OperatorTypes getOperatorType() {
			return opType;
		}
	}

	public static enum SyntaxNodeType {
		ROOT,
		SEQUENCE,
		IF,
		WHILE, 
		UNTIL,
		FORALL,
		FOR,
		FOR1,
		CASE,
		CASEDEF,
		BREAK,
		CONTINUE,
		RETURN,
		RETURN1,
		RANGE,
		LIST,
		BINARY,
		STRONG_BINARY,
		PREFIX,
		SUFFIX,
		CONSTANT,
		SUBSTITUTION,
		ACCESS,
		CALL
	}
	
	static {
		KEYWORDS.placeName("var", Keywords.VAR);
		KEYWORDS.placeName("begin", Keywords.BEGIN);
		KEYWORDS.placeName("end", Keywords.END);
		KEYWORDS.placeName("int", Keywords.INT);
		KEYWORDS.placeName("real", Keywords.REAL);
		KEYWORDS.placeName("str", Keywords.STR);
		KEYWORDS.placeName("bool", Keywords.BOOL);
		KEYWORDS.placeName("color", Keywords.COLOR);
		KEYWORDS.placeName("point", Keywords.POINT);
		KEYWORDS.placeName("rect", Keywords.RECT);
		KEYWORDS.placeName("font", Keywords.FONT);
		KEYWORDS.placeName("stroke", Keywords.STROKE);
		KEYWORDS.placeName("transform", Keywords.TRANSFORM);
		KEYWORDS.placeName("image", Keywords.IMAGE);
		KEYWORDS.placeName("array", Keywords.ARRAY);
		KEYWORDS.placeName("map", Keywords.MAP);
		KEYWORDS.placeName("of", Keywords.OF);
		KEYWORDS.placeName("if", Keywords.IF);
		KEYWORDS.placeName("then", Keywords.THEN);
		KEYWORDS.placeName("else", Keywords.ELSE);
		KEYWORDS.placeName("while", Keywords.WHILE);
		KEYWORDS.placeName("do", Keywords.DO);
		KEYWORDS.placeName("for", Keywords.FOR);
		KEYWORDS.placeName("to", Keywords.TO);
		KEYWORDS.placeName("step", Keywords.STEP);
		KEYWORDS.placeName("case", Keywords.CASE);
		KEYWORDS.placeName("continue", Keywords.CONTINUE);
		KEYWORDS.placeName("break", Keywords.BREAK);
		KEYWORDS.placeName("return", Keywords.RETURN);
		KEYWORDS.placeName("in", Keywords.IN);
		KEYWORDS.placeName("true", Keywords.TRUE);
		KEYWORDS.placeName("false", Keywords.FALSE);
		KEYWORDS.placeName("system", Keywords.SYSTEM);
		KEYWORDS.placeName("clipboard", Keywords.CLIPBOARD);
		KEYWORDS.placeName("canvas", Keywords.CANVAS);
		KEYWORDS.placeName("args", Keywords.ARGS);
		KEYWORDS.placeName("func", Keywords.FUNC);
		KEYWORDS.placeName("proc", Keywords.PROC);
		KEYWORDS.placeName("forward", Keywords.FORWARD);
	}
	
	public static <T> List<Lexema> parseLex(final Reader content, final SyntaxTreeInterface<T> names, final boolean ignoreErrors) throws SyntaxException {
		final List<Lexema>	result = new ArrayList<>();
		
		try(final LineByLineProcessor	lblp = new LineByLineProcessor((displacement, lineNo, data, from, length)->parseLine((int)displacement, lineNo, data, from, length, names, result, ignoreErrors))){
			lblp.write(content);
		} catch (IOException e) {
			throw new SyntaxException(0, 0, e.getLocalizedMessage(), e);
		}
		result.add(new Lexema(0, 0, 0, LexTypes.EOF));
		return result;
	}

	private static <T> void parseLine(final int displacement, final int lineNo, final char[] data, int from, final int length, final SyntaxTreeInterface<T> names, final List<Lexema> result, final boolean ignoreErrors) throws SyntaxException {
		final StringBuilder	sb = new StringBuilder();
		final int[]			forNames = new int[2];
		final long[]		forNumbers = new long[2];
		final int			start = from;
		
		while (from < data.length) {
			from = CharUtils.skipBlank(data, from, true);
			switch (data[from]) {
				case '\n' : case '\r' :
					return;
				case '(' :
					result.add(new Lexema(displacement, lineNo, from-start, LexTypes.OPEN));
					from++;
					break;
				case ')' :
					result.add(new Lexema(displacement, lineNo, from-start, LexTypes.CLOSE));
					from++;
					break;
				case '[' :
					result.add(new Lexema(displacement, lineNo, from-start, LexTypes.OPENB));
					from++;
					break;
				case ']' :
					result.add(new Lexema(displacement, lineNo, from-start, LexTypes.CLOSEB));
					from++;
					break;
				case '{' :
					result.add(new Lexema(displacement, lineNo, from-start, LexTypes.OPENF));
					from++;
					break;
				case '}' :
					result.add(new Lexema(displacement, lineNo, from-start, LexTypes.CLOSEF));
					from++;
					break;
				case ',' :
					result.add(new Lexema(displacement, lineNo, from-start, LexTypes.COMMA));
					from++;
					break;
				case ';' :
					result.add(new Lexema(displacement, lineNo, from-start, LexTypes.SEMICOLON));
					from++;
					break;
				case '.' :
					if (data[from + 1] == '.') {
						result.add(new Lexema(displacement, lineNo, from-start, LexTypes.RANGE));
						from += 2;
					}
					else {
						result.add(new Lexema(displacement, lineNo, from-start, LexTypes.DOT));
						from++;
					}
					break;
				case ':' :
					if (data[from + 1] == ':') {
						result.add(new Lexema(displacement, lineNo, from-start, LexTypes.CAST));
						from += 2;
					}
					else if (data[from + 1] == '=') {
						result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.ASSIGNMENT));
						from += 2;
					}
					else {
						result.add(new Lexema(displacement, lineNo, from-start, LexTypes.COLON));
						from++;
					}
					break;
				case '~' :
					result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.BIT_INV));
					from++;
					break;
				case '^' :
					result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.BIT_XOR));
					from++;
					break;
				case '!' :
					result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.BOOL_NOT));
					from++;
					break;
				case '*' :
					result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.MUL));
					from++;
					break;
				case '%' :
					result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.MOD));
					from++;
					break;
				case '=' :
					result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.EQ));
					from++;
					break;
				case '/' :
					if (data[from + 1] == '/') {
						return;
					}
					else {
						result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.DIV));
						from++;
					}
					break;
				case '+' :
					if (data[from + 1] == '+') {
						result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.INC));
						from += 2;
					}
					else {
						result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.ADD));
						from++;
					}
					break;
				case '-' :
					if (data[from + 1] == '-') {
						result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.DEC));
						from += 2;
					}
					else {
						result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.SUB));
						from++;
					}
					break;
				case '&' :
					if (data[from + 1] == '&') {
						result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.BOOL_AND));
						from += 2;
					}
					else {
						result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.BIT_AND));
						from++;
					}
					break;
				case '|' :
					if (data[from + 1] == '|') {
						result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.BOOL_OR));
						from += 2;
					}
					else {
						result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.BIT_OR));
						from++;
					}
					break;
				case '>' :
					if (data[from + 1] == '=') {
						result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.GE));
						from += 2;
					}
					else {
						result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.GT));
						from++;
					}
					break;
				case '<' :
					if (data[from + 1] == '=') {
						result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.LE));
						from += 2;
					}
					else if (data[from + 1] == '>') {
						result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.NE));
						from += 2;
					}
					else {
						result.add(new Lexema(displacement, lineNo, from-start, OperatorTypes.LT));
						from++;
					}
					break;
				case '\"' :
					try{from = CharUtils.parseStringExtended(data, from + 1, '\"', sb);
						result.add(new Lexema(displacement, lineNo, from-start, sb.toString().toCharArray()));
						sb.setLength(0);
					} catch (IllegalArgumentException exc) {
						if (ignoreErrors) {
							result.add(new Lexema(displacement, lineNo, from-start, LexTypes.ERROR));
							from++;
						}
						else {
							throw new SyntaxException(lineNo, from-start, "Unterminated string");
						}
					}
					break;
				case '`' :
					try{from = CharUtils.parseStringExtended(data, from + 1, '`', sb);
						result.add(new Lexema(displacement, lineNo, from-start, LexTypes.SUBSTITUTION, sb.toString().toCharArray()));
						sb.setLength(0);
					} catch (IllegalArgumentException exc) {
						if (ignoreErrors) {
							result.add(new Lexema(displacement, lineNo, from-start, LexTypes.ERROR));
							from++;
						}
						else {
							throw new SyntaxException(lineNo, from-start, "Unterminated string");
						}
					}
					break;
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					final int	beforeDigit = from;
					
					from = CharUtils.parseNumber(data, from, forNumbers, CharUtils.PREF_LONG, true);
					if (!(data[from] == '.' && data[from + 1] == '.')) {	// Can be range!
						from = CharUtils.parseNumber(data, beforeDigit, forNumbers, CharUtils.PREF_ANY, true);
					}
					else {
						forNumbers[1] = CharUtils.PREF_LONG;
					}
					
					switch ((int)forNumbers[1]) {
						case CharUtils.PREF_INT : case CharUtils.PREF_LONG :
							result.add(new Lexema(displacement, lineNo, from-start, forNumbers[0]));
							break;
						case CharUtils.PREF_FLOAT	: 
							result.add(new Lexema(displacement, lineNo, from-start, Float.intBitsToFloat((int)forNumbers[0])));
							break;
						case CharUtils.PREF_DOUBLE 	:
							result.add(new Lexema(displacement, lineNo, from-start, Double.longBitsToDouble(forNumbers[0])));
							break;
					}
					break;
				default :
					if (Character.isJavaIdentifierStart(data[from])) {
						from = CharUtils.parseName(data, from, forNames);
						final long	id = KEYWORDS.seekName(data, forNames[0], forNames[1] + 1);
						
						if (id >= 0) {
							final Keywords	kw = KEYWORDS.getCargo(id);
							
							switch (kw.type) {
								case CONSTANT	:
									result.add(new Lexema(displacement, lineNo, forNames[0]-start, kw == Keywords.TRUE));
									break;
								default :
									result.add(new Lexema(displacement, lineNo, forNames[0]-start, kw));
									break;
							}
						}
						else {
							final long	nameId = names.seekName(data, forNames[0], forNames[1] + 1);
							
							if (nameId < 0) {
								result.add(new Lexema(displacement, lineNo, forNames[0]-start, LexTypes.NAME, names.placeName(data, forNames[0], forNames[1] + 1, null)));
							}
							else {
								result.add(new Lexema(displacement, lineNo, forNames[0]-start, LexTypes.NAME, nameId));
							}
						}
					}
					else {
						if (ignoreErrors) {
							result.add(new Lexema(displacement, lineNo, from-start, LexTypes.ERROR));
							from++;
						}
						else {
							throw new SyntaxException(lineNo, from-start, "Unknown lexema");
						}
					}
					break;
			}
		}
	}

	public static <T> SyntaxNode<SyntaxNodeType,SyntaxNode<SyntaxNodeType,?>> parseScript(final Reader content, final SyntaxTreeInterface<T> names) {
		return null;
	} 

	static int buildSyntaxTree(final Lexema[] data, int from, final SyntaxTreeInterface<EntityDescriptor> names, final SyntaxNode<SyntaxNodeType,SyntaxNode<SyntaxNodeType,?>> root) throws SyntaxException {
loop:	for (;;) {
			switch (data[from].getType()) {
				case EOF 	:
					break loop;
				case PART 	:
					switch (data[from].getKeyword()) {
						case FUNC	:
							from = buildFunction(data, from, names, root);
							break; 
						case PROC	:
							from = buildProcedure(data, from, names, root);
							break; 
						default :
							throw new SyntaxException(data[from].getRow(), data[from].getCol(), "FUNC/PROC awaited");
					}					
					break;
				default :
					from = buildAnonBlock(data, from, names, root);
					break;
			}
		}
		return from;
	}

	private static int buildAnonBlock(final Lexema[] data, int from, final SyntaxTreeInterface<EntityDescriptor> names, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root) throws SyntaxException {
		// TODO Auto-generated method stub
		if (data[from].getType() == LexTypes.STATEMENT && data[from].getKeyword() == Keywords.VAR) {
			from = buildDeclarations(data, from + 1, names, root);
		}
		if (data[from].getType() == LexTypes.STATEMENT && data[from].getKeyword() == Keywords.BEGIN) {
			do {
				from = buildStatement(data, from + 1, names, root);
			} while (data[from].getType() == LexTypes.SEMICOLON);
			
			if (data[from].getType() == LexTypes.STATEMENT && data[from].getKeyword() == Keywords.END) {
				from++;
			}
			else {
				throw new SyntaxException(data[from].getRow(), data[from].getCol(), "END awaited");
			}
		}
		return from;
	}

	private static int buildProcedure(final Lexema[] data, int from, final SyntaxTreeInterface<EntityDescriptor> names, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root) throws SyntaxException {
		// TODO Auto-generated method stub
		if (data[from].getType() == LexTypes.STATEMENT && data[from].getKeyword() == Keywords.VAR) {
			from = buildDeclarations(data, from + 1, names, root);
		}
		if (data[from].getType() == LexTypes.STATEMENT && data[from].getKeyword() == Keywords.BEGIN) {
			do {
				from = buildStatement(data, from + 1, names, root);
			} while (data[from].getType() == LexTypes.SEMICOLON);
			
			if (data[from].getType() == LexTypes.STATEMENT && data[from].getKeyword() == Keywords.END) {
				from++;
			}
			else {
				throw new SyntaxException(data[from].getRow(), data[from].getCol(), "END awaited");
			}
		}
		return from;
	}

	private static int buildFunction(final Lexema[] data, int from, final SyntaxTreeInterface<EntityDescriptor> names, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root) throws SyntaxException {
		// TODO Auto-generated method stub
		if (data[from].getType() == LexTypes.STATEMENT && data[from].getKeyword() == Keywords.VAR) {
			from = buildDeclarations(data, from + 1, names, root);
		}
		if (data[from].getType() == LexTypes.STATEMENT && data[from].getKeyword() == Keywords.BEGIN) {
			do {
				from = buildStatement(data, from + 1, names, root);
			} while (data[from].getType() == LexTypes.SEMICOLON);
			
			if (data[from].getType() == LexTypes.STATEMENT && data[from].getKeyword() == Keywords.END) {
				from++;
			}
			else {
				throw new SyntaxException(data[from].getRow(), data[from].getCol(), "END awaited");
			}
		}
		return from;
	}

	static int buildDeclarations(final Lexema[] data, int from, final SyntaxTreeInterface<EntityDescriptor> names, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root) throws SyntaxException {
		while (data[from].getType() == LexTypes.NAME) {
			final long				nameId = data[from].getLongAssociated();
			final DataTypes			nameType;
			final CollectionType	nameCollection;
			final int				nameLex = from;
			SyntaxNode				initials = null;
			
			if (data[from+1].getType() == LexTypes.COLON) {
				if (data[from+2].getType() == LexTypes.TYPE) {
					switch (data[from+2].getKeyword()) {
						case ARRAY 	:
							nameCollection = CollectionType.ARRAY;
							if (data[from+3].getType() == LexTypes.OPTION && data[from+3].getKeyword() == Keywords.OF) {
								if (data[from+4].getType() == LexTypes.TYPE) {
									switch (data[from+4].getKeyword()) {
										case INT : case REAL : case STR : case BOOL : case COLOR : case POINT : case RECT : case FONT : case STROKE : case TRANSFORM : case IMAGE :
											nameType = data[from+4].getDataType();
											from += 5;
											break;
										default :
											throw new UnsupportedOperationException("Type ["+data[from+4].getKeyword()+"] is not supported yet");
									}
								}
								else {
									throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing type keyword");
								}
							}
							else {
								throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing 'of'");
							}
							break;
						case MAP	:
							nameCollection = CollectionType.MAP;
							if (data[from+3].getType() == LexTypes.OPTION && data[from+3].getKeyword() == Keywords.OF) {
								if (data[from+4].getType() == LexTypes.TYPE) {
									switch (data[from+4].getKeyword()) {
										case INT : case REAL : case STR : case BOOL : case COLOR : case POINT : case RECT : case FONT : case STROKE : case TRANSFORM : case IMAGE :
											nameType = data[from+4].getDataType();
											from += 5;
											break;
										default :
											throw new UnsupportedOperationException("Type ["+data[from+4].getKeyword()+"] is not supported yet");
									}
								}
								else {
									throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing type keyword");
								}
							}
							else {
								throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing 'of'");
							}
							break;
						case INT : case REAL : case STR : case BOOL :
							nameType = data[from+2].getDataType();
							nameCollection = CollectionType.ORDINAL;
							from += 3;
							break;
						case COLOR : case POINT : case RECT : case FONT : case STROKE : case TRANSFORM : case IMAGE :
							nameType = data[from+2].getDataType();
							nameCollection = CollectionType.STRUCTURE;
							from += 3;
							break;
						default :
							throw new UnsupportedOperationException("Type ["+data[from+2].getKeyword()+"] is not supported yet");
					}
				}
				else {
					throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing type keyword");
				}
			}
			else {
				throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing ':'");
			}
			if (data[from].getType() == LexTypes.OPERATOR && data[from].opType == OperatorTypes.ASSIGNMENT) {
				initials = (SyntaxNode) root.clone();
				
				from = buildExpression(data, from + 1, names, initials);
			}
			if (names.getCargo(nameId) == null) {
				names.setCargo(nameId, new EntityDescriptor(nameId, nameCollection, nameType, initials));
			}
			else {
				throw new SyntaxException(data[nameLex].getRow(), data[nameLex].getCol(), "Name already declared earlier");
			}
			if (data[from].getType() == LexTypes.COMMA) {
				from++;
			}
		}
		return from;
	}

	static int buildStatement(final Lexema[] data, int from, final SyntaxTreeInterface<EntityDescriptor> names, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root) throws SyntaxException {
		// TODO Auto-generated method stub
		root.row = data[from].getRow();
		root.col = data[from].getCol();
		
		switch (data[from].getType()) {
			case OPENF		:
				final List<SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>>	stmts = new ArrayList<>();
				
				do {final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	child = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
					
					from = buildStatement(data, from + 1, names, child);
					stmts.add(child);
				} while (data[from].getType() == LexTypes.SEMICOLON);
				
				if (data[from].getType() == LexTypes.CLOSEF) {
					root.type = SyntaxNodeType.SEQUENCE;
					root.children = stmts.toArray(new SyntaxNode[stmts.size()]);
					from++;
				}
				else {
					throw new SyntaxException(data[from].getRow(), data[from].getCol(), "Missing '}'");
				}
				break;
			case NAME		:
				final EntityDescriptor	desc = names.getCargo(data[from].getLongAssociated());
				
				if (desc == null) {
					throw new SyntaxException(data[from].getRow(), data[from].getCol(), "Name is not declared");
				}
				else {
					from = buildAccess(data, from, desc, names, root);
				}
				if (data[from].getType() == LexTypes.OPERATOR && data[from].getOperatorType() == OperatorTypes.ASSIGNMENT) {
					final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	leftPart = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
					final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	rightPart = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
					
					from = buildExpression(data, from+1, names, rightPart);
					root.type = SyntaxNodeType.STRONG_BINARY;
					root.cargo = OperatorTypes.ASSIGNMENT;
					root.children = new SyntaxNode[] {leftPart, rightPart};
				}
				else {
					final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	callPart = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
					
					root.type = SyntaxNodeType.CALL;
					root.cargo = callPart;
				}
				break;
			case PREDEFINED_VAR	:
				final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	callPart = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
				
				from = buildAccess(data, from, PREDEFINED.get(data[from].getKeyword()), names, callPart);
				root.type = SyntaxNodeType.CALL;
				root.cargo = callPart;
				break;
			case STATEMENT	:
				switch (data[from].getKeyword()) {
					case IF			:
						final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	ifCond = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
						final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	thenNode = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
						
						from = buildExpression(data, from+1, names, ifCond);
						if (data[from].getType() == LexTypes.OPTION && data[from].getKeyword() == Keywords.THEN) {
							from = buildStatement(data, from + 1, names, thenNode);
						}
						else {
							throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing 'then'");
						}
						
						root.type = SyntaxNodeType.IF;
						root.cargo = ifCond;
						
						if (data[from].getType() == LexTypes.OPTION && data[from].getKeyword() == Keywords.ELSE) {
							final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	elseNode = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
							
							from = buildStatement(data, from + 1, names, elseNode);
							root.children = new SyntaxNode[] {thenNode, elseNode};
						}
						else {
							root.children = new SyntaxNode[] {thenNode};
						}
						break;
					case WHILE		:
						final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	whileCond = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
						final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	whileNode = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
						
						from = buildExpression(data, from+1, names, whileCond);
						if (data[from].getType() == LexTypes.STATEMENT && data[from].getKeyword() == Keywords.DO) {
							from = buildStatement(data, from + 1, names, whileNode);
						}
						else {
							throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing 'do'");
						}
						root.type = SyntaxNodeType.WHILE;
						root.cargo = whileCond;
						root.children = new SyntaxNode[] {whileNode};
						break;
					case DO			:
						final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	untilCond = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
						final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	untilNode = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
						
						from = buildStatement(data, from+1, names, untilNode);
						if (data[from].getType() == LexTypes.STATEMENT && data[from].getKeyword() == Keywords.WHILE) {
							from = buildExpression(data, from + 1, names, untilCond);
						}
						else {
							throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing 'while'");
						}
						root.type = SyntaxNodeType.UNTIL;
						root.cargo = untilCond;
						root.children = new SyntaxNode[] {untilNode};
						break;
					case FOR		:
						if (data[from+1].getType() == LexTypes.NAME) {
							final long	varId = data[from+1].getLongAssociated();
							
							if (names.getCargo(varId) == null) {
								throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Undeclared variable");
							}
							else if (data[from+2].getType() == LexTypes.OPERATOR) {
								switch (data[from+2].getOperatorType()) {
									case IN 		:
										final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	inExpr = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
										final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	inNode = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
										
										from = buildListExpression(data, from + 3, names, inExpr);
										if (data[from].getType() == LexTypes.STATEMENT && data[from].getKeyword() == Keywords.DO) {
											from = buildStatement(data, from + 1, names, inNode);
											root.type = SyntaxNodeType.FORALL;
											root.value = varId;
											root.cargo = inExpr;
											root.children = new SyntaxNode[] {inNode};
										}
										else {
											throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing 'do'");
										}
										break;
									case ASSIGNMENT	:
										final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	forInitial = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
										final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	forTerminal = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
										final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	forNode = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
										
										from = buildExpression(data, from + 3, names, forInitial);
										if (data[from].getType() == LexTypes.OPTION && data[from].getKeyword() == Keywords.TO) {
											from = buildExpression(data, from + 1, names, forTerminal);
											
											if (data[from].getType() == LexTypes.OPTION && data[from].getKeyword() == Keywords.STEP) {
												final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	forStep = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();

												from = buildExpression(data, from + 1, names, forStep);
												root.type = SyntaxNodeType.FOR;
												root.value = varId;
												root.children = new SyntaxNode[] {forInitial, forTerminal, forStep, forNode};
											}											
											else {
												root.type = SyntaxNodeType.FOR1;
												root.value = varId;
												root.children = new SyntaxNode[] {forInitial, forTerminal, forNode};
											}
										}
										else {
											throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing 'while'");
										}
										break;
									default :
										throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Illegal operator. Only ':=' or 'in' is avilable");
								}
							}
						}
						else {
							throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing var name");
						}
						break;
					case CASE		:
						final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>			caseExpr = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
						final List<SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>> 	ofList = new ArrayList<SyntaxNode<SyntaxNodeType,SyntaxNode<SyntaxNodeType,?>>>();
						
						from = buildExpression(data, from + 1, names, caseExpr);
						while (data[from].getType() == LexTypes.OPTION && data[from].getKeyword() == Keywords.OF) {
							final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>		ofCond = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
							final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>		ofNode = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
							
							from = buildListExpression(data, from + 1, names, ofCond);
							if (data[from].getType() == LexTypes.COLON) {
								from = buildStatement(data, from+1, names, ofNode);
							}
							else {
								throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing ':'");
							}
							ofList.add(ofCond);
							ofList.add(ofNode);
						}
						if (data[from].getType() == LexTypes.OPTION && data[from].getKeyword() == Keywords.ELSE) {
							final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>		elseNode = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
							
							from = buildStatement(data, from + 1, names, elseNode);
							if (data[from].getType() == LexTypes.STATEMENT && data[from].getKeyword() == Keywords.END) {
								ofList.add(elseNode);
								root.type = SyntaxNodeType.CASEDEF;
								root.cargo = caseExpr;
								root.children = ofList.toArray(new SyntaxNode[ofList.size()]);
							}
							else {
								throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing 'end'");
							}
						}
						else if (data[from].getType() == LexTypes.STATEMENT && data[from].getKeyword() == Keywords.END) {
							root.type = SyntaxNodeType.CASE;
							root.cargo = caseExpr;
							root.children = ofList.toArray(new SyntaxNode[ofList.size()]);
						}
						else {
							throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing 'end'");
						}
						break;
					case CONTINUE	:
						if (data[from+1].getType() == LexTypes.CONSTANT && data[from+1].getDataType() == DataTypes.INT) {
							root.type = SyntaxNodeType.CONTINUE;
							root.value = data[from+1].getLongAssociated();
							from++;
						}
						else {
							root.type = SyntaxNodeType.CONTINUE;
							root.value = 1;
						}
						break;
					case BREAK		:
						if (data[from+1].getType() == LexTypes.CONSTANT && data[from+1].getDataType() == DataTypes.INT) {
							root.type = SyntaxNodeType.BREAK;
							root.value = data[from+1].getLongAssociated();
							from++;
						}
						else {
							root.type = SyntaxNodeType.BREAK;
							root.value = 1;
						}
						break;
					case RETURN		:
						if (data[from+1].getType() != LexTypes.SEMICOLON && data[from+1].getType() != LexTypes.EOF) {
							final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	returnExpr = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
							
							from = buildExpression(data, from + 1, names, returnExpr);
							root.type = SyntaxNodeType.RETURN1;
							root.cargo = returnExpr;
						}
						else {
							root.type = SyntaxNodeType.RETURN;
						}
						break;
					default :
						throw new UnsupportedOperationException("Statement ["+data[from].getKeyword()+"] is not supported yet"); 
				}
				break;
			default :
				throw new SyntaxException(data[from].getRow(), data[from].getCol(), "Missing statement");
		}
		return from;
	}

	static int buildListExpression(final Lexema[] data, int from, final SyntaxTreeInterface<EntityDescriptor> names, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root) throws SyntaxException {
		final List<SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>> list = new ArrayList<>();
		
		root.row = data[from].getRow();
		root.col = data[from].getCol();
		from--;
		
		do {final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	expr = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
			
			from = buildExpression(data, from + 1, names, expr);
			if (data[from].getType() == LexTypes.RANGE) {
				final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>		rangeExpr = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
				final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>		nextExpr = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
								
				from = buildExpression(data, from+1, names, nextExpr);
				rangeExpr.type = SyntaxNodeType.RANGE;
				rangeExpr.children = new SyntaxNode[] {expr, nextExpr}; 
				list.add(rangeExpr);
			}
			else {
				list.add(expr);
			}
		} while(data[from].getType() == LexTypes.COMMA);
		
		if (list.size() == 1) {
			final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	content = list.get(0);
			
			root.type = content.type;
			root.value = content.value;
			root.cargo = content.cargo;
			root.children = content.children;
		}
		else {
			root.type = SyntaxNodeType.LIST;
			root.children = list.toArray(new SyntaxNode[list.size()]);
		}
		return from;
	}

	static int buildExpression(final Lexema[] data, int from, final SyntaxTreeInterface<EntityDescriptor> names, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root) throws SyntaxException {
		return buildExpression(OperatorPriorities.BOOL_OR, data, from, names, root);
	}

	private static int buildExpression(final OperatorPriorities prty, final Lexema[] data, int from, final SyntaxTreeInterface<EntityDescriptor> names, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root) throws SyntaxException {
		root.row = data[from].getRow();
		root.col = data[from].getCol();
		
		switch (prty.getLevelType()) {
			case BINARY		:
				from = buildExpression(prty.prev(), data, from, names, root);
				if (data[from].getType() == LexTypes.OPERATOR && data[from].getOperatorType().getInfixPriority() == prty) {
					final List<SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>>	args = new ArrayList<>();
					final List<OperatorTypes>	ops = new ArrayList<>();
					
					args.add((SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone());
					do {final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>		expr = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
						
						ops.add(data[from].getOperatorType());
						from = buildExpression(prty.prev(), data, from + 1, names, expr);
						args.add(expr);
					} while (data[from].getType() == LexTypes.OPERATOR && data[from].getOperatorType().getInfixPriority() == prty);
					root.type = SyntaxNodeType.BINARY; 
					root.cargo = ops.toArray(new OperatorTypes[ops.size()]);
					root.children = args.toArray(new SyntaxNode[args.size()]);
				}
				break;
			case NONE		:
				from = buildTerminal(data, from, names, root);
				break;
			case PREFIX		:
				if (data[from].getType() == LexTypes.OPERATOR && data[from].getOperatorType().getPrefixPriority() == prty) {
					final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>		expr = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
					final OperatorTypes	op = data[from].getOperatorType();
					
					from = buildExpression(prty.prev(), data, from + 1, names, expr);
					root.type = SyntaxNodeType.PREFIX;
					root.cargo = op;
					root.children = new SyntaxNode[] {expr};
				}
				else {
					from = buildExpression(prty.prev(), data, from, names, root);
				}
				break;
			case STRONG_BINARY	:
				from = buildExpression(prty.prev(), data, from, names, root);
				if (data[from].getType() == LexTypes.OPERATOR && data[from].getOperatorType().getInfixPriority() == prty) {
					final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	leftExpr = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
					final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	rightExpr = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
					final OperatorTypes												opType = data[from].getOperatorType(); 

					if (data[from].getOperatorType().isListSupported()) {
						from = buildListExpression(data, from + 1, names, rightExpr);
					}
					else {
						from = buildExpression(prty.prev(), data, from + 1, names, rightExpr);
					}
					root.type = SyntaxNodeType.STRONG_BINARY;
					root.cargo = opType;
					root.children = new SyntaxNode[] {leftExpr, rightExpr};
				}
				break;
			case SUFFIX		:
				from = buildExpression(prty.prev(), data, from, names, root);
				if (data[from].getType() == LexTypes.OPERATOR && data[from].getOperatorType().getSuffixPriority() == prty) {
					final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>		expr = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
					
					root.type = SyntaxNodeType.SUFFIX;
					root.cargo = data[from].getOperatorType();
					root.children = new SyntaxNode[] {expr};
					from++;
				}
				break;
			default :
				throw new UnsupportedOperationException("Level type ["+prty.getLevelType()+"] is not supported yet");
		}
		return from;
	}

	private static int buildTerminal(final Lexema[] data, int from, final SyntaxTreeInterface<EntityDescriptor> names, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root) throws SyntaxException {
		root.row = data[from].getRow();
		root.col = data[from].getCol();
		
		switch (data[from].getType()) {
			case OPEN 		:
				from = buildExpression(data, from + 1, names, root);
				if (data[from].getType() == LexTypes.CLOSE) {
					from++;
				}
				else {
					throw new SyntaxException(data[from].getRow(), data[from].getCol(), "Missing ')'");
				}
				break;
			case NAME		:
				final EntityDescriptor	desc = names.getCargo(data[from].getLongAssociated());
				
				if (desc == null) {
					throw new SyntaxException(data[from].getRow(), data[from].getCol(), "Name is not declared");
				}
				else {
					from = buildAccess(data, from, desc, names, root);
				}
				break;
			case PREDEFINED_VAR	:
				from = buildAccess(data, from, PREDEFINED.get(data[from].getKeyword()), names, root);
				break;
			case CONSTANT	:
				root.type = SyntaxNodeType.CONSTANT;
				root.cargo = data[from];
				from++;
				break;
			case SUBSTITUTION:
				root.type = SyntaxNodeType.SUBSTITUTION;
				root.cargo = data[from];
				from++;
				break;
			default :
				throw new SyntaxException(data[from].getRow(), data[from].getCol(), "Missing terminal");
		}
		return from;
	}
		
	private static int buildAccess(final Lexema[] data, int from, final EntityDescriptor desc, final SyntaxTreeInterface<EntityDescriptor> names, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root) throws SyntaxException {
		return buildAccess(data, from, desc, desc.dataType.getClassAssociated(), names, root);
	}

	private static int buildAccess(final Lexema[] data, int from, final EntityDescriptor desc, Class<?> current, final SyntaxTreeInterface<EntityDescriptor> names, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root) throws SyntaxException {
		final List<SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>>	items = new ArrayList<>();
		boolean theSameFirstName = true;
		
		root.row = data[from].getRow();
		root.col = data[from].getCol();
		root.type = SyntaxNodeType.ACCESS;
		root.value = data[from].getLongAssociated(); 
		from--;
		
		do {from++;
			if (data[from].getType() == LexTypes.NAME) {
				if (data[from + 1].getType() == LexTypes.OPEN) {
					final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	methodAccess = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
					final List<SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>>	parameters = new ArrayList<>();
					
					methodAccess.row = data[from].getRow();
					methodAccess.col = data[from].getCol();
					methodAccess.type = SyntaxNodeType.CALL;
					methodAccess.value = data[from].getLongAssociated();
					
					do {final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	parameterValue = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
						from = buildExpression(data, from + 1, names, parameterValue);
						parameters.add(parameterValue);
					} while (data[from].getType() == LexTypes.COLON);
					
					if (data[from].getType() != LexTypes.CLOSE) {
						throw new SyntaxException(data[from].getRow(), data[from].getCol(), "Missing ')'"); 
					}
					else {
						final SyntaxNode[]	children = parameters.toArray(new SyntaxNode[parameters.size()]);
						final Class<?>[]	signature = buildSignature(children);
						Class<?>			classFound = null;
						boolean				found = false;
						
						for (Method m : current.getMethods()) {
							if (names.seekName(m.getName()) == methodAccess.value && Arrays.equals(m.getParameterTypes(), signature)) {
								methodAccess.cargo = m;
								classFound = m.getReturnType();
								found = true;
							}
						}
						if (data[from].getType() != LexTypes.CLOSEB) {
							throw new SyntaxException(data[from].getRow(), data[from].getCol(), "Missing ']'"); 
						}
						else {
							methodAccess.children = children;
							current = classFound;
							from++;
						}
					}
				}
				else {
					final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	fieldAccess = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
					Class<?>	classFound = null;
					boolean		found = false;
					
					for (Field f : current.getFields()) {
						if (names.seekName(f.getName()) == data[from].associatedLong) {
							fieldAccess.row = data[from].getRow();
							fieldAccess.col = data[from].getCol();
							fieldAccess.type = SyntaxNodeType.SUFFIX;
							fieldAccess.value = data[from].associatedLong;
							classFound = f.getType();
							found = true;
							break;
						}
					}
					if (!found && !theSameFirstName) {
						throw new SyntaxException(data[from].getRow(), data[from].getCol(), "Unknown field"); 
					}
					else if (data[from + 1].getType() == LexTypes.OPENB) {
						final List<SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>>	indices = new ArrayList<>();
						
						do {final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	indexValue = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
							from = buildExpression(data, from + 1, names, indexValue);
							indices.add(indexValue);
						} while (data[from].getType() == LexTypes.COLON);
						
						if (data[from].getType() != LexTypes.CLOSEB) {
							throw new SyntaxException(data[from].getRow(), data[from].getCol(), "Missing ']'"); 
						}
						else {
							fieldAccess.cargo = theSameFirstName ? AccessType.GET_VAR_INDEX : AccessType.GET_ARRAY_INDEX;
							fieldAccess.children = indices.toArray(new SyntaxNode[indices.size()]);
							
							for (int index = 0; index < indices.size(); index++) {
								if (classFound.isArray()) {
									classFound = classFound.getComponentType();
								}
								else {
									throw new SyntaxException(data[from].getRow(), data[from].getCol(), "Too many indices for array"); 
								}
							}
							current = classFound;
							from++;
						}
						items.add(fieldAccess);
					}
					else if (!theSameFirstName) {
						fieldAccess.cargo = AccessType.GET_FIELD;
						current = classFound;
						items.add(fieldAccess);
						from++;
					}
					else {
						from++;
					}
				}
			}
			theSameFirstName = false;
		} while (data[from].getType() == LexTypes.DOT);

		root.children = items.toArray(new SyntaxNode[items.size()]);
		return from;
	}	
	
	private static Class<?>[] buildSignature(final SyntaxNode[] children) {
		// TODO Auto-generated method stub
		return null;
	}

	public static class Lexema {
		private final int			displ;
		private final int			row;
		private final int			col;
		private final LexTypes		type;
		private final OperatorTypes	opType;
		private final DataTypes		dataType;
		private final Keywords		kw;
		private final long			associatedLong;
		private final Object		associatedObject;
		
		Lexema(final int displ, final int row, final int col, final LexTypes type) {
			this.displ = displ;
			this.row = row;
			this.col = col;
			this.type = type;
			this.opType = null;
			this.dataType = null;
			this.kw = null;
			this.associatedLong = 0;
			this.associatedObject = null;
		}

		Lexema(final int displ, final int row, final int col, final LexTypes type, final char[] associatedObject) {
			this.displ = displ;
			this.row = row;
			this.col = col;
			this.type = type;
			this.opType = null;
			this.dataType = null;
			this.kw = null;
			this.associatedLong = 0;
			this.associatedObject = associatedObject;
		}
		
		Lexema(final int displ, final int row, final int col, final OperatorTypes opType) {
			this.displ = displ;
			this.row = row;
			this.col = col;
			this.type = LexTypes.OPERATOR;
			this.opType = opType;
			this.dataType = null;
			this.kw = null;
			this.associatedLong = 0;
			this.associatedObject = null;
		}

		Lexema(final int displ, final int row, final int col, final char[] content) {
			this.displ = displ;
			this.row = row;
			this.col = col;
			this.type = LexTypes.CONSTANT;
			this.opType = null;
			this.dataType = DataTypes.STR;
			this.kw = null;
			this.associatedLong = 0;
			this.associatedObject = content;
		}

		Lexema(final int displ, final int row, final int col, final long content) {
			this.displ = displ;
			this.row = row;
			this.col = col;
			this.type = LexTypes.CONSTANT;
			this.opType = null;
			this.dataType = DataTypes.INT;
			this.kw = null;
			this.associatedLong = content;
			this.associatedObject = null;
		}
		
		Lexema(final int displ, final int row, final int col, final double content) {
			this.displ = displ;
			this.row = row;
			this.col = col;
			this.type = LexTypes.CONSTANT;
			this.opType = null;
			this.dataType = DataTypes.REAL;
			this.kw = null;
			this.associatedLong = Double.doubleToLongBits(content);
			this.associatedObject = null;
		}

		Lexema(final int displ, final int row, final int col, final boolean content) {
			this.displ = displ;
			this.row = row;
			this.col = col;
			this.type = LexTypes.CONSTANT;
			this.opType = null;
			this.dataType = DataTypes.BOOL;
			this.kw = null;
			this.associatedLong = content ? 1 : 0;
			this.associatedObject = null;
		}

		Lexema(final int displ, final int row, final int col, final Keywords kw) {
			this.displ = displ;
			this.row = row;
			this.col = col;
			this.type = kw.getLexType();
			this.opType = kw.opType;
			this.dataType = kw.dataType;
			this.kw = kw;
			this.associatedLong = 0;
			this.associatedObject = null;
		}

		Lexema(final int displ, final int row, final int col, final LexTypes type, final long content) {
			this.displ = displ;
			this.row = row;
			this.col = col;
			this.type = type;
			this.opType = null;
			this.dataType = null;
			this.kw = null;
			this.associatedLong = content;
			this.associatedObject = null;
		}
		
		public int getDispl() {
			return displ;
		}
		
		public int getRow() {
			return row;
		}
		
		public int getCol() {
			return col;
		}
		
		public LexTypes getType() {
			return type;
		}
		
		public OperatorTypes getOperatorType() {
			return opType;
		}
		
		public DataTypes getDataType() {
			return dataType;
		}
		
		public Keywords getKeyword() {
			return kw;
		}
		
		public long getLongAssociated() {
			return associatedLong;
		}
		
		public <T> T getObjectAssociated(final Class<T> awaited) {
			return awaited.cast(associatedObject);
		}

		@Override
		public String toString() {
			return "Lexema [row=" + row + ", col=" + col + ", type=" + type + ", opType=" + opType + ", dataType="
					+ dataType + ", kw=" + kw + ", associatedLong=" + associatedLong + ", associatedObject="
					+ associatedObject + "]";
		}
	}
	
	static class EntityDescriptor {
		final EntityType			type;
		final long					id;
		final CollectionType		collType;
		final DataTypes				dataType;
		final EntityDescriptor[]	parameters;
		final EntityDescriptor		returns;
		final SyntaxNode			initials;

		public EntityDescriptor(long id, CollectionType collType, DataTypes dataType) {
			this(EntityType.VAR, id, collType, dataType, null, null, null);
		}

		public EntityDescriptor(long id, CollectionType collType, DataTypes dataType, SyntaxNode initials) {
			this(EntityType.VAR, id, collType, dataType, null, null, initials);
		}
		
		private EntityDescriptor(EntityType type, long id, CollectionType collType, DataTypes dataType, EntityDescriptor[] parameters, EntityDescriptor returns, final SyntaxNode initials) {
			this.type = type;
			this.id = id;
			this.collType = collType;
			this.dataType = dataType;
			this.parameters = parameters;
			this.returns = returns;
			this.initials = initials;					
		}

		@Override
		public String toString() {
			return "EntityDescriptor [type=" + type + ", id=" + id + ", collType=" + collType + ", dataType=" + dataType + ", parameters=" + Arrays.toString(parameters) + ", returns=" + returns + "]";
		}
	}
}
