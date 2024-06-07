package chav1961.bt.find.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import chav1961.bt.find.internal.FileMask.Operand;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

/*
 *	File mask syntax:
 * 
 *  <mask> ::= {'/'|'./'}<component>['/'<component>...]
 *  <component> ::= {<alter>|'{'<alter>['|'<alter>]...'}'}
 *  <alter> ::= {'*'|'**'|<wildcardName>}['['<expression>']']
 *  <wildcardName> ::= {<letterOrDigit>|'*'|'?'|'.'}<wildcardName>
 *  <expression> ::= <and>[||<and>...]
 *  <and> ::= <not>[&&<not>...]
 *  <not> ::= [!]<compare>
 *  <compare> ::= <add> [{'>'|'>='|'<'|'<='|'='|'<>'} <add>]
 *  <add> ::= <unary>[{'+'|'-'}<unary>...]
 *  <unary> ::= ['-']<term>
 *  <term> ::= {<predefined>|<constant>|'('<expression>')'}
 *  <predefined> ::= {'name'|'length'|'canRead'|'canWrite'|'canExecute'|'lastModified'}
 *  
 */

public class FileMask {
	private static final int		SYMBOL_PLUS = 1;
	private static final int		SYMBOL_MINUS = 2;
	private static final int		SYMBOL_GT = 3;
	private static final int		SYMBOL_GE = 4;
	private static final int		SYMBOL_LT = 5;
	private static final int		SYMBOL_LE = 6;
	private static final int		SYMBOL_EQ = 7;
	private static final int		SYMBOL_NE = 8;
	private static final int		PREDEFINED_LENGTH = 9;
	private static final int		PREDEFINED_LAST_UPDATE = 10;
	private static final int		PREDEFINED_CAN_READ = 11;
	private static final int		PREDEFINED_CAN_WRITE = 12;
	private static final int		PREDEFINED_CAN_EXECUTE = 13;
	private static final Symbols[]	SYMBOLS = { new Symbols("+", SYMBOL_PLUS), 	new Symbols("-", SYMBOL_MINUS),
												new Symbols(">", SYMBOL_GT), 	new Symbols(">=", SYMBOL_GE),
												new Symbols("<", SYMBOL_LT), 	new Symbols("<=", SYMBOL_LE),
												new Symbols("=", SYMBOL_EQ), 	new Symbols("<>", SYMBOL_NE),
												new Symbols("length", PREDEFINED_LENGTH),
												new Symbols("updated", PREDEFINED_LAST_UPDATE),
												new Symbols("canRead", PREDEFINED_CAN_READ),
												new Symbols("canWrite", PREDEFINED_CAN_WRITE),
												new Symbols("canExecute", PREDEFINED_CAN_EXECUTE)
											};
	private static final Operand	TRUE = new Operand(true);
	private static final Operand	FALSE = new Operand(false);

	@FunctionalInterface
	static interface WalkCallback {
		boolean process(File file, SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> node, SyntaxTreeInterface<String> names, WalkCallback callback, Consumer<File> fileCallback) throws SyntaxException;
	}
	
	
	private final SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>>	root;
	
	
	private FileMask(final SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>> root) {
		this.root = root;
		
	}
	
	public void walk(final File root, final Consumer<File> callback) {
		
	}
	
	public boolean matches(final File file) {
		return true;
	}
	
	public static FileMask compile(final String source) throws SyntaxException {
		if (Utils.checkEmptyOrNullString(source)) {
			throw new IllegalArgumentException("File mask string can't be null or empty");
		}
		else {
			return compile(CharUtils.terminateAndConvert2CharArray(source, '\n'));
		}
	}
	
	static SyntaxTreeInterface<String> prepareSyntaxTree() {
		final SyntaxTreeInterface<String>	names = new AndOrTree<>(16, 16);
		
		for(Symbols item : SYMBOLS) {
			names.placeName((CharSequence)item.symbol, item.id, null);
		}
		return names;
	}
	
	static FileMask compile(final char[] content) throws SyntaxException {
		final SyntaxTreeInterface<String>	names = prepareSyntaxTree();
		
		final Lexema[]		parsed = parse(content, names);
		if (parsed[0].type == LexType.SEPARATOR || parsed[0].type == LexType.DOT_SEPARATOR) {
			final SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>>	root = new SyntaxNode<>(0, 0, SyntaxNodeType.ROOT, 0, null);
			final int 			stop = buildSyntaxTree(parsed, 1, root);
		
			if (parsed[stop].type != LexType.EOF) {
				throw new SyntaxException(0, parsed[stop].from, "Unparsed tail in the expression");
			}
			else {
				return new FileMask(root);
			}
		}
		else {
			throw new SyntaxException(0, parsed[0].from, "Expression must be started from separator '/' or './' or '../");
		}
	}
	
	static int buildSyntaxTree(final Lexema[] content, int from, final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> node) throws SyntaxException {
		from = buildCurrentName(content, from, node);
		if (content[from].type == LexType.SEPARATOR) {
			return buildSyntaxTree(content, from + 1, node);
		}
		else {
			return from;
		}
	}

	static int buildCurrentName(final Lexema[] content, int from, final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> node) throws SyntaxException {
		SyntaxNode[]		children = null;
		SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	clone;
		
		switch (content[from].type) {
			case ANY_NAME		:
				node.col = content[from].from;
				node.type = SyntaxNodeType.ANY_NAME;
				node.value = 0;
				from++;
				break;
			case ANY_SUBTREE	:
				node.col = content[from].from;
				node.type = SyntaxNodeType.ANY_SUBTREE;
				node.value = 0;
				from++;
				break;
			case ORDINAL_NAME	:
				node.col = content[from].from;
				node.type = SyntaxNodeType.ORDINAL_NAME;
				node.value = content[from].id;
				from++;
				break;
			case WILDCARD_NAME	:
				node.col = content[from].from;
				node.type = SyntaxNodeType.WILDCARD_NAME;
				node.value = content[from].id;
				from++;
				break;
			case START_ALTER	:
				final List<SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>>	list = new ArrayList<>();
				
				list.add(null);
				node.col = from;
				node.value = 0;
				node.type = SyntaxNodeType.LIST;
				do {
					clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
					from = buildCurrentName(content, from + 1, clone);
					list.add(clone);
				} while (content[from].type == LexType.ALTER);
				
				if (content[from].type == LexType.END_ALTER) {
					children = list.toArray(new SyntaxNode[list.size()]);
					from++;
				}
				else {
					throw new SyntaxException(0, content[from].from, "Missing '}'");
				}
				break;
			default:
				throw new SyntaxException(0, content[from].from, "Inwaited lexema");
		}
		if (content[from].type == LexType.START_EXPR) {
			final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> expr = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
			
			from = buildExpression(content, from + 1, ExprPriority.OR, expr);
			if (content[from].type == LexType.END_EXPR) {
				from++;
			}
			else {
				throw new SyntaxException(0, content[from].from, "Missing ']'");
			}
			node.cargo = expr; 
		}
		else {
			node.cargo = null;
		}
		if (content[from].type == LexType.SEPARATOR) {
			final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> child = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
			
			from = buildCurrentName(content, from + 1, child);
			if (children == null) {
				children = new SyntaxNode[] {child};
			}
			else {
				children[0] = child;
			}
		}
		node.children = children;
		return from;
	}

	static int buildExpression(final Lexema[] content, int from, final ExprPriority prty, final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> node) throws SyntaxException {
		switch (prty) {
			case OR			:
				from = buildExpression(content, from, ExprPriority.AND, node);
				if (content[from].type == LexType.OR) {
					
					final List<SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>>	items = new ArrayList<>();
					
					items.add((SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone());
					do {
						final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
						
						from = buildExpression(content, from + 1, ExprPriority.AND, clone);
						items.add(clone);
					} while (content[from].type == LexType.OR);
					node.type = SyntaxNodeType.OR;
					node.children = items.toArray(new SyntaxNode[items.size()]);
				}
				break;
			case AND		:
				from = buildExpression(content, from, ExprPriority.NOT, node);
				if (content[from].type == LexType.AND) {
					final List<SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>>	items = new ArrayList<>();
					
					items.add((SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone());
					do {
						final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
						
						from = buildExpression(content, from + 1, ExprPriority.NOT, clone);
						items.add(clone);
					} while (content[from].type == LexType.AND);
					node.type = SyntaxNodeType.AND;
					node.children = items.toArray(new SyntaxNode[items.size()]);
				}
				break;
			case NOT		:
				if (content[from].type == LexType.NOT) {
					final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
					
					from = buildExpression(content, from + 1, ExprPriority.COMPARE, clone);
					node.type = SyntaxNodeType.NOT;
					node.children = new SyntaxNode[] {clone};
				}
				else {
					from = buildExpression(content, from, ExprPriority.COMPARE, node);
				}
				break;
			case COMPARE	:
				from = buildExpression(content, from, ExprPriority.ADD, node);
				if (content[from].type == LexType.COMPARE) {
					final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	left = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
					final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	right = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
					final long	compareId = content[from].id;
					
					from = buildExpression(content, from + 1, ExprPriority.ADD, right);
					node.type = SyntaxNodeType.COMPARE;
					node.children = new SyntaxNode[] {left, right};
					node.value = compareId;
				}
				break;
			case ADD		:
				from = buildExpression(content, from, ExprPriority.UNARY, node);
				if (content[from].type == LexType.ADD || content[from].type == LexType.SUBTRACT) {
					final List<SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>>	items = new ArrayList<>();
					long	operations = 0;
					int		count = 0;
					
					items.add((SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone());
					do {
						if (content[from].type == LexType.SUBTRACT) {
							operations |= (2L << count);
						}
						final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
						
						from = buildExpression(content, from + 1, ExprPriority.UNARY, clone);
						items.add(clone);
						count++;
					} while (content[from].type == LexType.ADD || content[from].type == LexType.ADD);
					node.type = SyntaxNodeType.ADD;
					node.value = operations;
					node.children = items.toArray(new SyntaxNode[items.size()]);
				}
				break;
			case UNARY		:
				if (content[from].type == LexType.SUBTRACT) {
					final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
					
					from = buildExpression(content, from + 1, ExprPriority.TERM, clone);
					node.type = SyntaxNodeType.MINUS;
					node.children = new SyntaxNode[] {clone};
				}
				else if (content[from].type == LexType.ADD) {
					from = buildExpression(content, from + 1, ExprPriority.TERM, node);
				}
				else {
					from = buildExpression(content, from, ExprPriority.TERM, node);
				}
				break;
			case TERM		:
				switch (content[from].type) {
					case CONST		:
						node.type = SyntaxNodeType.CONSTANT;
						node.value = content[from].id;
						from++;
						break;
					case OPEN		:
						from = buildExpression(content, from + 1, ExprPriority.OR, node);
						if (content[from].type == LexType.CLOSE) {
							from++;
						}
						else {
							throw new SyntaxException(0, content[from].from, "Missing ')'");
						}
						break;
					case PREDEFINED	:
						node.type = SyntaxNodeType.PREDEFINED;
						node.value = content[from].id;
						from++;
						break;
					default:
						throw new SyntaxException(0, content[from].from, "Term is missing");
				}
				break;
			default:
				throw new UnsupportedOperationException("Expression priority ["+prty+"] is not supported yet");
		}
		return from;
	}	
	
	static Lexema[] parse(final char[] content, final SyntaxTreeInterface<String> names) throws SyntaxException {
		final List<Lexema>	result = new ArrayList<>();
		boolean		sameFirst = true;
		boolean		separatorDetected = false;
		boolean		expressionDetected = false;
		int			index = 0;
		
loop:	for (;;) {
			while (content[index] <= ' ' && content[index] != '\n') {
				index++;
			}
			switch (content[index]) {
				case '\n' :
					result.add(new Lexema(LexType.EOF, index++, 0));
					break loop;
				case '/' :
					if (expressionDetected) {
						throw new SyntaxException(0, index, "Unsupported lexema in this context");
					}
					else {
						result.add(new Lexema(LexType.SEPARATOR, index++, 0));
						separatorDetected = true;
						continue loop;
					}
				case '(' :
					result.add(new Lexema(LexType.OPEN, index++, 0));
					continue loop;
				case ')' :
					result.add(new Lexema(LexType.CLOSE, index++, 0));
					continue loop;
				case '{' :
					result.add(new Lexema(LexType.START_ALTER, index++, 0));
					continue loop;
				case '}' :
					result.add(new Lexema(LexType.END_ALTER, index++, 0));
					continue loop;
				case '[' :
					result.add(new Lexema(LexType.START_EXPR, index++, 0));
					expressionDetected = true;
					continue loop;
				case ']' :
					result.add(new Lexema(LexType.END_EXPR, index++, 0));
					expressionDetected = false;
					continue loop;
				case '=' :
					result.add(new Lexema(LexType.COMPARE, index++, SYMBOL_EQ));
					continue loop;
				case '+' :
					result.add(new Lexema(LexType.ADD, index++, 0));
					continue loop;
				case '-' :
					result.add(new Lexema(LexType.SUBTRACT, index++, 0));
					continue loop;
				case '&' :
					if (content[index + 1] == '&') {
						result.add(new Lexema(LexType.AND, index += 2, 0));
						continue loop;
					}
					else {
						throw new SyntaxException(0, index, "Unknown lexema");
					}
				case '|' :
					if (content[index + 1] == '|') {
						result.add(new Lexema(LexType.OR, index += 2, 0));
					}
					else {
						result.add(new Lexema(LexType.ALTER, index++, 0));
					}
					continue loop;
				case '!' :
					if (content[index + 1] == '=') {
						result.add(new Lexema(LexType.COMPARE, index += 2, SYMBOL_NE));
					}
					else {
						result.add(new Lexema(LexType.NOT, index++, 0));
					}
					continue loop;
				case '>' :
					if (content[index + 1] == '=') {
						result.add(new Lexema(LexType.COMPARE, index += 2, SYMBOL_GE));
					}
					else {
						result.add(new Lexema(LexType.COMPARE, index++, SYMBOL_GT));
					}
					continue loop;
				case '<' :
					if (content[index + 1] == '=') {
						result.add(new Lexema(LexType.COMPARE, index += 2, SYMBOL_LE));
					}
					else if (content[index + 1] == '>') {
						result.add(new Lexema(LexType.COMPARE, index += 2, SYMBOL_NE));
					}
					else {
						result.add(new Lexema(LexType.COMPARE, index++, SYMBOL_LT));
					}
					continue loop;
				case '.' :
					if (sameFirst) {
						if (content[index + 1] == '.' && content[index + 2] == '/') {
							result.add(new Lexema(LexType.DOT_SEPARATOR, index += 3, 0));
						}
						else if (content[index + 1] == '/') {
							result.add(new Lexema(LexType.DOT_SEPARATOR, index += 2, 0));
						}
						separatorDetected = true;
						sameFirst = false;
						continue loop;
					}
					break;
				case '*' :
					if (separatorDetected) {
						while (content[index] <= ' ' && content[index] != '\n') {
							index++;
						}
						if (content[index + 1] == '*') {
							result.add(new Lexema(LexType.ANY_SUBTREE, index += 2, 0));
						}
						else if (!(Character.isJavaIdentifierPart(content[index + 1]) || content[index + 1] == '.' || content[index + 1] == '?')) {
							result.add(new Lexema(LexType.ANY_NAME, index++, 0));
						}
						separatorDetected = false;
						continue loop;
					}
					break;
			}
			int			from = index;
			
			if (expressionDetected) {
				if (Character.isJavaIdentifierStart(content[index])) {
					while (Character.isJavaIdentifierPart(content[index])) {
						index++;
					}
					final long	name = names.seekName(content, from, index); 
					
					if (name >= 0) {
						result.add(new Lexema(LexType.PREDEFINED, from, name));
					}
					else {
						throw new SyntaxException(0, index, "Unknown predefined name");
					}
				}
				else if (content[index] >= '0' && content[index] <= '9') {
					long	value = 0;
					
					while (content[index] >= '0' && content[index] <= '9') {
						value = 10 * value + content[index] - '0'; 
						index++;
					}
					if (content[index] == 'k' || content[index] == 'K') {
						value *= 1024;
						index++;
					}
					else if (content[index] == 'm' || content[index] == 'M') {
						value *= 1024 * 1024;
						index++;
					}
					else if (content[index] == 'g' || content[index] == 'G') {
						value *= 1024 * 1024 * 1024;
						index++;
					}
					result.add(new Lexema(LexType.CONST, from, value));
				}
				else {
					throw new SyntaxException(0, index, "Unsupported lexema in this context");
				}
			}
			else {
				boolean		wildCard = false;
				
				while (Character.isJavaIdentifierPart(content[index]) || content[index] == '*' || content[index] == '?' || content[index] == '.') {
					if (content[index] == '*' || content[index] == '?') {
						wildCard = true;
					}
					index++;
				}
				if (from != index) {
					final long	name = names.placeOrChangeName(content, from, index, new String(content, from, index-from));
					
					result.add(new Lexema(wildCard ? LexType.WILDCARD_NAME : LexType.ORDINAL_NAME, from, name));
				}
			}
		}
		
		return result.toArray(new Lexema[result.size()]);
	}

	static Operand calculateExpr(final Function<String,Operand> func, final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> node) throws SyntaxException {
		switch (node.type) {
			case OR			:
				for(int index = 0; index < node.children.length; index++) {
					final Operand	value = calculateExpr(func, (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.children[index]);
					
					if (value.getType() == OperandType.BOOLEAN) {
						if ((Boolean)value.getValue()) {
							return TRUE; 
						}
					}
					else {
						throw new SyntaxException(0, node.children[index].col, "Boolean operand awaiting");
					}
				}
				return FALSE;
			case AND		:
				for(int index = 0; index < node.children.length; index++) {
					final Operand	value = calculateExpr(func, (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.children[index]);
					
					if (value.getType() == OperandType.BOOLEAN) {
						if (!(Boolean)value.getValue()) {
							return FALSE; 
						}
					}
					else {
						throw new SyntaxException(0, node.children[index].col, "Boolean operand awaiting");
					}
				}
				return TRUE;
			case NOT		:
				final Operand	notValue = calculateExpr(func, (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.children[0]);
				
				if (notValue.getType() == OperandType.BOOLEAN) {
					return (Boolean)notValue.getValue() ? FALSE : TRUE;
				}
				else {
					throw new SyntaxException(0, node.col, "Boolean operand awaiting");
				}
			case COMPARE	:
				final Operand	left = calculateExpr(func, (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.children[0]);
				final Operand	right = calculateExpr(func, (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.children[1]);
				
				switch ((int)node.value) {
					case SYMBOL_GT	:
						return (Long)left.getValue() > (Long)right.getValue() ? TRUE : FALSE; 
					case SYMBOL_GE	:
						return (Long)left.getValue() >= (Long)right.getValue() ? TRUE : FALSE; 
					case SYMBOL_LT	:
						return (Long)left.getValue() < (Long)right.getValue() ? TRUE : FALSE; 
					case SYMBOL_LE	:
						return (Long)left.getValue() <= (Long)right.getValue() ? TRUE : FALSE; 
					case SYMBOL_EQ	:
						return (Long)left.getValue() == (Long)right.getValue() ? TRUE : FALSE; 
					case SYMBOL_NE	:
						return (Long)left.getValue() != (Long)right.getValue() ? TRUE : FALSE; 
					default :
						throw new SyntaxException(0, node.col, "Unsupported comparison type in the expression");
				}
			case ADD		:
				long		sum = 0;
				final long	mask = node.value;
				
				for(int index = 0; index < node.children.length; index++) {
					final Operand	value = calculateExpr(func, (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.children[index]);
					
					if (value.getType() == OperandType.NUMERIC) {
						if ((mask & (1L << index)) != 0) {
							sum -= (Long)value.getValue();
						}
						else {
							sum += (Long)value.getValue();
						}
					}
					else {
						throw new SyntaxException(0, node.children[index].col, "Boolean operand awaiting");
					}
				}
				return new Operand(sum);
			case MINUS		:
				final Operand	minusValue = calculateExpr(func, (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.children[0]);
				
				if (minusValue.getType() == OperandType.NUMERIC) {
					return new Operand(-(Long)minusValue.getValue());
				}
				else {
					throw new SyntaxException(0, node.col, "Numeric operand awaiting");
				}
			case CONSTANT	:
				return new Operand(node.value);
			case PREDEFINED	:
				switch ((int)node.value) {
					case PREDEFINED_LENGTH		:
						return func.apply("length");
					case PREDEFINED_LAST_UPDATE	:
						return func.apply("lastUpdate");
					case PREDEFINED_CAN_READ	:
						return func.apply("canRead");
					case PREDEFINED_CAN_WRITE	:
						return func.apply("canWrite");
					case PREDEFINED_CAN_EXECUTE	:
						return func.apply("canExecute");
					default :
						throw new UnsupportedOperationException("Predefined name id ["+node.value+"] is not supported yet");
				}
			default:
				throw new SyntaxException(0, node.col, "Unsupported node type in the expression");
		}
	}
	
	
	static enum LexType {
		SEPARATOR,
		DOT_SEPARATOR,
		ANY_NAME,
		ANY_SUBTREE,
		WILDCARD_NAME,
		ORDINAL_NAME,
		OPEN,
		CLOSE,
		START_EXPR,
		END_EXPR,
		START_ALTER,
		ALTER,
		END_ALTER,
		OR,
		AND,
		NOT,
		COMPARE,
		ADD,
		SUBTRACT,
		PREDEFINED,
		CONST,
		EOF
	}
	
	static enum SyntaxNodeType {
		ROOT,
		ORDINAL_NAME,
		WILDCARD_NAME,
		ANY_NAME,
		ANY_SUBTREE,
		LIST,
		OR,
		AND,
		NOT,
		COMPARE,
		ADD,
		MINUS,
		PREDEFINED,
		CONSTANT
	}

	static enum ExprPriority {
		OR,
		AND,
		NOT,
		COMPARE,
		ADD,
		UNARY,
		TERM
	}
	
	static enum OperandType {
		NUMERIC,
		STRING,
		BOOLEAN
	}
	
	static class Lexema {
		final LexType	type;
		final int 		from;
		final long		id;
		
		private Lexema(final LexType type, final int from, final long id) {
			this.type = type;
			this.from = from;
			this.id = id;
		}

		@Override
		public String toString() {
			return "Lexema [type=" + type + ", from=" + from + ", id=" + id + "]";
		}
	}
	
	static class Symbols {
		private final String	symbol;
		private final long		id;
		
		private Symbols(final String symbol, final long id) {
			this.symbol = symbol;
			this.id = id;
		}

		@Override
		public String toString() {
			return "Symbols [symbol=" + symbol + ", id=" + id + "]";
		}
	}
	
	static class Operand {
		private final OperandType	type;
		private final Object		value;
		
		private Operand(final OperandType type, final Object value) {
			this.type = type;
			this.value = value;
		}
		
		Operand(final boolean value) {
			this(OperandType.BOOLEAN, Boolean.valueOf(value));
		}

		Operand(final long value) {
			this(OperandType.NUMERIC, Long.valueOf(value));
		}
		
		Operand(final String value) {
			this(OperandType.STRING, value);
		}
		
		OperandType getType() {
			return type;
		}
		
		<V> V getValue() {
			return (V) value;
		}

		@Override
		public String toString() {
			return "Operand [type=" + type + ", value=" + value + "]";
		}
	}

	static boolean walk(final File root, final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> current, final SyntaxTreeInterface<String> names, final Consumer<File> fileCallback) throws SyntaxException {
		return walk(root, current, new ArrayList<>(), names, fileCallback);
	}
	
	private static boolean walk(final File root, final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> current, final List<SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>> continued, final SyntaxTreeInterface<String> names, final Consumer<File> fileCallback) throws SyntaxException {
		if (!root.exists()) {
			return false;
		}
		else if (current == null) {
			return walkContinued(root, current, continued, names, fileCallback);
		}
		else {
			switch (current.type) {
				case WILDCARD_NAME	:
					if (!matches(root.getName(), names.getCargo(current.value))) {
						return false;
					}
				case ANY_NAME		:
					if (root.isDirectory()) {
						if (current.children != null && current.children.length > 0) {
							final File[]	content = root.listFiles();
							
							if (content != null) {
								boolean	anyTrue = false;
								
								for(File item : content) {
									anyTrue |= walk(item, (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) current.children[0], continued, names, fileCallback);
								}
								return anyTrue;
							}
							else {
								return false;
							}
						}
						else {
							return walkContinued(root, current, continued, names, fileCallback);
						}
					}
					else {
						try2Accept(root, current, fileCallback);
						return true;
					}
				case ORDINAL_NAME	:
					final File	currentFile = new File(root, names.getCargo(current.value)); 
					
					if (currentFile.exists()) {
						if (currentFile.isFile()) {
							return try2Accept(currentFile, current, fileCallback);
						}
						else if (current.children != null && current.children.length > 0) {
							return walk(currentFile, (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) current.children[0], continued, names, fileCallback);
						}
						else {
							return walkContinued(root, current, continued, names, fileCallback);
						}
					}
					else {
						return false;
					}
				case ANY_SUBTREE	:
					if (root.isDirectory()) {
						if (current.children != null && current.children.length > 0) {
							boolean	anyTrue = false;
							
							anyTrue |= walk(root, (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) current.children[0], continued, names, fileCallback);
							final File[]	inner = root.listFiles();
							
							if (inner != null) {
								for(File item : inner) {
									anyTrue |= walk(item, current, continued, names, fileCallback);
								}
							}
							return anyTrue;
						}
						else {
							return false;
						}
					}
					else {
						try2Accept(root, current, fileCallback);
						return true;
					}
				case LIST			:
					boolean	anyTrue = false;
					
					if (current.children[0] != null) {
						continued.add(0, (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) current.children[0]);
					}
					for (int index = 1; index < current.children.length; index++) {
						anyTrue |= walk(root, (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) current.children[index], continued, names, fileCallback);
					}
					if (current.children[0] != null) {
						continued.remove(0);
					}
					return anyTrue;
				default :
					throw new SyntaxException(0, current.col, "Illegal tree node");
			}
		}
	}

	private static boolean walkContinued(final File root, final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> current, final List<SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>> continued, final SyntaxTreeInterface<String> names, final Consumer<File> fileCallback) throws SyntaxException {
		if (!continued.isEmpty()) {
			final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> temp = continued.remove(0);
			final boolean	result = walk(root, temp, continued, names, fileCallback);
			
			continued.add(0, temp);
			return result;
		}
		else {
			return false;
		}
	}	
	
	static boolean calc(final Function<String,Operand> func, final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> node) throws SyntaxException {
		final Operand	result = calculateExpr(func, node);
		
		if (result.getType() != OperandType.BOOLEAN) {
			throw new SyntaxException(0, node.col, "Awaited result is [BOOLEAN], but ["+result.getType()+"] was detected");
		}
		else {
			return ((Boolean)result.getValue()).booleanValue();
		}
	}
	
	private static boolean matches(final String name, final String template) {
		return true;
	}

	private static Operand calc(final File file, final String predefined) {
		switch (predefined) {
			case "length" :
				return new Operand(file.length());
			case "lastUpdate" :
				return new Operand(file.lastModified());
			case "canRead" :
				return new Operand(file.canRead());
			case "canWrite" :
				return new Operand(file.canWrite());
			case "canExecute" :
				return new Operand(file.canExecute());
			default :
				return new Operand(false);
		}
	}

	private static boolean try2Accept(final File file, final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> node, final Consumer<File> callback) throws SyntaxException {
		if (file.isFile() && (node.children == null || node.children.length == 0)) {
			if (node.cargo == null || calc((s)->calc(file, s), (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.cargo)) {
				callback.accept(file);
				return true;
			}
		}
		return false;
	}
}
