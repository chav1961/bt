package chav1961.bt.paint.script.parsers;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	private static final SyntaxTreeInterface<Keywords>	KEYWORDS = new AndOrTree<>();

	private static enum LexTypes {
		PART,
		STATEMENT,
		TYPE,
		OPTION,
		NAME,
		PREDEFINED_VAR,
		CONSTANT,
		OPERATOR,
		OPEN,
		CLOSE,
		OPENB,
		CLOSEB,
		OPENF,
		CLOSEF,
		COMMA,
		DOT,
		RANGE,
		COLON,
		CAST,
		SEMICOLON,
		EOF
	}

	private static enum EntityType {
		VAR,
		FUNC,
		PROC
	}
	
	private static enum DataTypes {
		UNKNOWN,
		INT,
		REAL,
		STR,
		BOOL,
		COLOR,
		POINT,
		RECT,
		FONT,
		STROKE,
		TRANSFORM,
		IMAGE
	}

	private static enum CollectionType {
		NONE,
		ARRAY,
		MAP
	}
	
	private static enum OperatorPriorities {
		UNARY,
		BIT_AND,
		BIT_OR,
		MULTIPLICATION,
		ADDITION,
		COMPARISON,
		BOOL_NOT,
		BOOL_AND,
		BOOL_OR,
		ASSIGNMENT,
		UNKNOWN;
	}
	
	private static enum OperatorTypes {
		INC(OperatorPriorities.UNARY),
		DEC(OperatorPriorities.UNARY),
		BIT_INV(OperatorPriorities.UNARY),
		BIT_AND(OperatorPriorities.BIT_AND),
		BIT_OR(OperatorPriorities.BIT_OR),
		BIT_XOR(OperatorPriorities.BIT_OR),
		MUL(OperatorPriorities.MULTIPLICATION),
		DIV(OperatorPriorities.MULTIPLICATION),
		MOD(OperatorPriorities.MULTIPLICATION),
		ADD(OperatorPriorities.ADDITION),
		SUB(OperatorPriorities.ADDITION),
		GT(OperatorPriorities.COMPARISON),
		GE(OperatorPriorities.COMPARISON),
		LT(OperatorPriorities.COMPARISON),
		LE(OperatorPriorities.COMPARISON),
		EQ(OperatorPriorities.COMPARISON),
		NE(OperatorPriorities.COMPARISON),
		IN(OperatorPriorities.COMPARISON),
		BOOL_NOT(OperatorPriorities.BOOL_NOT),
		BOOL_AND(OperatorPriorities.BOOL_AND),
		BOOL_OR(OperatorPriorities.BOOL_OR),
		ASSIGNMENT(OperatorPriorities.ASSIGNMENT),
		UNKNOWN(OperatorPriorities.UNKNOWN);
		
		private final OperatorPriorities	prty;
		
		private OperatorTypes(final OperatorPriorities prty) {
			this.prty = prty;
		}
		
		public OperatorPriorities getPriority() {
			return prty;
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
		SYSTEM(LexTypes.PREDEFINED_VAR),
		CLIPBOARD(LexTypes.PREDEFINED_VAR),
		CANVAS(LexTypes.PREDEFINED_VAR),
		ARGS(LexTypes.PREDEFINED_VAR),
		FUNC(LexTypes.PART),
		PROC(LexTypes.PART),
		FORWARD(LexTypes.OPTION),
		;
		
		private final LexTypes		type;
		private final DataTypes		dataType;
		private final OperatorTypes	opType;
		
		Keywords(final LexTypes type) {
			this.type = type;
			this.dataType = DataTypes.UNKNOWN;
			this.opType = OperatorTypes.UNKNOWN;
		}

		Keywords(final LexTypes type, final DataTypes dataType) {
			this.type = type;
			this.dataType = dataType;
			this.opType = OperatorTypes.UNKNOWN;
		}

		Keywords(final LexTypes type, final OperatorTypes opType) {
			this.type = type;
			this.dataType = DataTypes.UNKNOWN;
			this.opType = opType;
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

	private static enum SyntaxNodeType {
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
		LIST
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
		KEYWORDS.placeName("step", Keywords.TO);
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
	
	public static <T> List<Lexema> parseLex(final Reader content, final SyntaxTreeInterface<T> names) throws SyntaxException {
		final List<Lexema>	result = new ArrayList<>();
		
		try(final LineByLineProcessor	lblp = new LineByLineProcessor((displacement, lineNo, data, from, length)->parseLine(lineNo, data, from, length, names, result))){
			lblp.write(content);
			lblp.flush();
			result.add(new Lexema(0, 0, LexTypes.EOF));
			return result;
		} catch (IOException e) {
			throw new SyntaxException(0, 0, e.getLocalizedMessage(), e);
		}
	}

	private static <T> void parseLine(final int lineNo, final char[] data, int from, final int length, final SyntaxTreeInterface<T> names, final List<Lexema> result) throws SyntaxException {
		final StringBuilder	sb = new StringBuilder();
		final int[]			forNames = new int[2];
		final long[]		forNumbers = new long[2];
		final int			start = from;
		
		from--;
		for (;;) {
			from = CharUtils.skipBlank(data, from + 1, false);
			switch (data[from]) {
				case '\n' : case '\r' :
					return;
				case '(' :
					result.add(new Lexema(lineNo, from-start, LexTypes.OPEN));
					break;
				case ')' :
					result.add(new Lexema(lineNo, from-start, LexTypes.CLOSE));
					break;
				case '[' :
					result.add(new Lexema(lineNo, from-start, LexTypes.OPENB));
					break;
				case ']' :
					result.add(new Lexema(lineNo, from-start, LexTypes.CLOSEB));
					break;
				case '{' :
					result.add(new Lexema(lineNo, from-start, LexTypes.OPENF));
					break;
				case '}' :
					result.add(new Lexema(lineNo, from-start, LexTypes.CLOSEF));
					break;
				case ',' :
					result.add(new Lexema(lineNo, from-start, LexTypes.COMMA));
					break;
				case ';' :
					result.add(new Lexema(lineNo, from-start, LexTypes.SEMICOLON));
					break;
				case '.' :
					if (data[from + 1] == '.') {
						result.add(new Lexema(lineNo, from-start, LexTypes.RANGE));
						from++;
					}
					else {
						result.add(new Lexema(lineNo, from-start, LexTypes.DOT));
					}
					break;
				case ':' :
					if (data[from + 1] == ':') {
						result.add(new Lexema(lineNo, from-start, LexTypes.CAST));
						from++;
					}
					else if (data[from + 1] == '=') {
						result.add(new Lexema(lineNo, from-start, OperatorTypes.ASSIGNMENT));
						from++;
					}
					else {
						result.add(new Lexema(lineNo, from-start, LexTypes.COLON));
					}
					break;
				case '~' :
					result.add(new Lexema(lineNo, from-start, OperatorTypes.BIT_INV));
					break;
				case '^' :
					result.add(new Lexema(lineNo, from-start, OperatorTypes.BIT_XOR));
					break;
				case '*' :
					result.add(new Lexema(lineNo, from-start, OperatorTypes.MUL));
					break;
				case '%' :
					result.add(new Lexema(lineNo, from-start, OperatorTypes.MOD));
					break;
				case '=' :
					result.add(new Lexema(lineNo, from-start, OperatorTypes.EQ));
					break;
				case '/' :
					if (data[from + 1] == '/') {
						return;
					}
					else {
						result.add(new Lexema(lineNo, from-start, OperatorTypes.DIV));
					}
					break;
				case '+' :
					if (data[from + 1] == '+') {
						result.add(new Lexema(lineNo, from-start, OperatorTypes.INC));
						from++;
					}
					else {
						result.add(new Lexema(lineNo, from-start, OperatorTypes.ADD));
					}
					break;
				case '-' :
					if (data[from + 1] == '|') {
						result.add(new Lexema(lineNo, from-start, OperatorTypes.DEC));
						from++;
					}
					else {
						result.add(new Lexema(lineNo, from-start, OperatorTypes.SUB));
					}
					break;
				case '&' :
					if (data[from + 1] == '&') {
						result.add(new Lexema(lineNo, from-start, OperatorTypes.BOOL_AND));
						from++;
					}
					else {
						result.add(new Lexema(lineNo, from-start, OperatorTypes.BIT_AND));
					}
					break;
				case '|' :
					if (data[from + 1] == '|') {
						result.add(new Lexema(lineNo, from-start, OperatorTypes.BOOL_OR));
						from++;
					}
					else {
						result.add(new Lexema(lineNo, from-start, OperatorTypes.BIT_OR));
					}
					break;
				case '>' :
					if (data[from + 1] == '=') {
						result.add(new Lexema(lineNo, from-start, OperatorTypes.GE));
						from++;
					}
					else {
						result.add(new Lexema(lineNo, from-start, OperatorTypes.GT));
					}
					break;
				case '<' :
					if (data[from + 1] == '=') {
						result.add(new Lexema(lineNo, from-start, OperatorTypes.LE));
						from++;
					}
					else if (data[from + 1] == '>') {
						result.add(new Lexema(lineNo, from-start, OperatorTypes.NE));
						from++;
					}
					else {
						result.add(new Lexema(lineNo, from-start, OperatorTypes.LT));
					}
					break;
				case '\"' :
					from = CharUtils.parseStringExtended(data, from, '\"', sb);
					if (data[from] != '\"') {
						throw new SyntaxException(lineNo, from-start, "Unterminated string");
					}
					else {
						result.add(new Lexema(lineNo, from-start, sb.toString().toCharArray()));
						sb.setLength(0);
					}
					break;
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					from = CharUtils.parseNumber(data, from, forNumbers, CharUtils.PREF_ANY, true);
					switch ((int)forNumbers[1]) {
						case CharUtils.PREF_INT : case CharUtils.PREF_LONG :
							result.add(new Lexema(lineNo, from-start, forNumbers[0]));
							break;
						case CharUtils.PREF_FLOAT	: 
							result.add(new Lexema(lineNo, from-start, Float.intBitsToFloat((int)forNumbers[0])));
							break;
						case CharUtils.PREF_DOUBLE 	:
							result.add(new Lexema(lineNo, from-start, Double.longBitsToDouble(forNumbers[0])));
							break;
					}
					break;
				default :
					if (Character.isJavaIdentifierStart(data[from])) {
						from = CharUtils.parseName(data, from, forNames);
						final long	id = KEYWORDS.seekName(data, forNames[0], forNames[1]);
						
						if (id >= 0) {
							final Keywords	kw = KEYWORDS.getCargo(id);
							
							switch (kw.type) {
								case CONSTANT	:
									result.add(new Lexema(lineNo, forNames[0]-start, kw == Keywords.TRUE));
									break;
								default :
									result.add(new Lexema(lineNo, forNames[0]-start, kw));
									break;
							}
						}
						else {
							result.add(new Lexema(lineNo, forNames[0]-start, LexTypes.NAME, names.placeName(data, forNames[0], forNames[1], null)));
						}
					}
					else {
						throw new SyntaxException(lineNo, from-start, "Unknown lexema");
					}
					break;
			}
		}
	}

	public static <T> SyntaxNode<SyntaxNodeType,SyntaxNode<SyntaxNodeType,?>> parseScript(final Reader content, final SyntaxTreeInterface<T> names) {
		return null;
	} 

	private static int buildSyntaxTree(final Lexema[] data, int from, final SyntaxTreeInterface<EntityDescriptor> names, final SyntaxNode<SyntaxNodeType,SyntaxNode<SyntaxNodeType,?>> root) throws SyntaxException {
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

	private static int buildDeclarations(final Lexema[] data, int from, final SyntaxTreeInterface<EntityDescriptor> names, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root) throws SyntaxException {
		while (data[from].getType() == LexTypes.NAME) {
			final long				nameId = data[from].getLongAssociated();
			final DataTypes			nameType;
			final CollectionType	nameCollection;
			
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
						case INT : case REAL : case STR : case BOOL : case COLOR : case POINT : case RECT : case FONT : case STROKE : case TRANSFORM : case IMAGE :
							nameType = data[from+2].getDataType();
							nameCollection = CollectionType.NONE;
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
			if (data[from].getType() == LexTypes.COMMA) {
				from++;
			}
			if (names.getCargo(nameId) == null) {
				names.setCargo(nameId, new EntityDescriptor(nameId, nameCollection, nameType));
			}
			else {
				throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Name already declared earlier");
			}
		}
		return from;
	}

	private static int buildStatement(final Lexema[] data, int from, final SyntaxTreeInterface<EntityDescriptor> names, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root) throws SyntaxException {
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
				break;
			case PREDEFINED_VAR	:
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
										
										from = buildExpression(data, from + 4, names, inExpr);
										if (data[from].getType() == LexTypes.STATEMENT && data[from].getKeyword() == Keywords.DO) {
											from = buildExpression(data, from + 1, names, inNode);
											root.type = SyntaxNodeType.FORALL;
											root.value = varId;
											root.cargo = inExpr;
											root.children = new SyntaxNode[] {inNode};
										}
										else {
											throw new SyntaxException(data[from+1].getRow(), data[from+1].getCol(), "Missing 'while'");
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
				break;
		}
		return from+1;
	}

	private static <T> int buildListExpression(final Lexema[] data, int from, final SyntaxTreeInterface<T> names, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root) {
		final List<SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>> list = new ArrayList<>();
		
		from--;
		root.row = data[from].getRow();
		root.col = data[from].getCol();
		do {final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	expr = (SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>) root.clone();
		
			from = buildExpression(data, from+1, names, expr);
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
		
		root.type = SyntaxNodeType.LIST;
		root.children = list.toArray(new SyntaxNode[list.size()]);
		return from;
	}

	private static <T> int buildExpression(final Lexema[] data, int from, final SyntaxTreeInterface<T> names, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root) {
		// TODO Auto-generated method stub
		return from;
	}
	
	public static class Lexema {
		private final int			row;
		private final int			col;
		private final LexTypes		type;
		private final OperatorTypes	opType;
		private final DataTypes		dataType;
		private final Keywords		kw;
		private final long			associatedLong;
		private final Object		associatedObject;
		
		Lexema(final int row, final int col, final LexTypes type) {
			this.row = row;
			this.col = col;
			this.type = type;
			this.opType = null;
			this.dataType = null;
			this.kw = null;
			this.associatedLong = 0;
			this.associatedObject = null;
		}

		Lexema(final int row, final int col, final OperatorTypes opType) {
			this.row = row;
			this.col = col;
			this.type = LexTypes.OPERATOR;
			this.opType = opType;
			this.dataType = null;
			this.kw = null;
			this.associatedLong = 0;
			this.associatedObject = null;
		}

		Lexema(final int row, final int col, final char[] content) {
			this.row = row;
			this.col = col;
			this.type = LexTypes.CONSTANT;
			this.opType = null;
			this.dataType = DataTypes.STR;
			this.kw = null;
			this.associatedLong = 0;
			this.associatedObject = null;
		}

		Lexema(final int row, final int col, final long content) {
			this.row = row;
			this.col = col;
			this.type = LexTypes.CONSTANT;
			this.opType = null;
			this.dataType = DataTypes.INT;
			this.kw = null;
			this.associatedLong = content;
			this.associatedObject = null;
		}
		
		Lexema(final int row, final int col, final double content) {
			this.row = row;
			this.col = col;
			this.type = LexTypes.CONSTANT;
			this.opType = null;
			this.dataType = DataTypes.REAL;
			this.kw = null;
			this.associatedLong = Double.doubleToLongBits(content);
			this.associatedObject = null;
		}

		Lexema(final int row, final int col, final boolean content) {
			this.row = row;
			this.col = col;
			this.type = LexTypes.CONSTANT;
			this.opType = null;
			this.dataType = DataTypes.BOOL;
			this.kw = null;
			this.associatedLong = content ? 1 : 0;
			this.associatedObject = null;
		}

		Lexema(final int row, final int col, final Keywords kw) {
			this.row = row;
			this.col = col;
			this.type = kw.getLexType();
			this.opType = null;
			this.dataType = null;
			this.kw = kw;
			this.associatedLong = 0;
			this.associatedObject = null;
		}

		Lexema(final int row, final int col, final LexTypes type, final long content) {
			this.row = row;
			this.col = col;
			this.type = type;
			this.opType = null;
			this.dataType = null;
			this.kw = null;
			this.associatedLong = content;
			this.associatedObject = null;
		}
		
		public int getRow() {
			return row;
		}
		
		public int getCol() {
			return row;
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
	}
	
	private static class EntityDescriptor {
		final EntityType			type;
		final long					id;
		final CollectionType		collType;
		final DataTypes				dataType;
		final EntityDescriptor[]	parameters;
		final EntityDescriptor		returns;

		public EntityDescriptor(long id, CollectionType collType, DataTypes dataType) {
			this(EntityType.VAR, id, collType, dataType, null, null);
		}
		
		private EntityDescriptor(EntityType type, long id, CollectionType collType, DataTypes dataType, EntityDescriptor[] parameters, EntityDescriptor returns) {
			this.type = type;
			this.id = id;
			this.collType = collType;
			this.dataType = dataType;
			this.parameters = parameters;
			this.returns = returns;
		}

		@Override
		public String toString() {
			return "EntityDescriptor [type=" + type + ", id=" + id + ", collType=" + collType + ", dataType=" + dataType + ", parameters=" + Arrays.toString(parameters) + ", returns=" + returns + "]";
		}
	}
}
