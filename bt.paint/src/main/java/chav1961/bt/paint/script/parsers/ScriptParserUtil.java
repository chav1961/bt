package chav1961.bt.paint.script.parsers;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

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
		VAR,
		PREDEFINED_VAR,
		CONSTANT,
		OPERATOR
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

	private static enum OperatorPriorities {
		COMPARISON,
		UNKNOWN;
	}
	
	private static enum OperatorTypes {
		IN(OperatorPriorities.COMPARISON),
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
		CALL(LexTypes.STATEMENT),
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
		KEYWORDS.placeName("call", Keywords.CALL);
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
	}
	
}
