package chav1961.bt.find.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

/*
 * File mask syntax:
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
			
	
	private final SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>>	root;
	
	
	private FileMask(final SyntaxNode<SyntaxNodeType, SyntaxNode<?,?>> root) {
		this.root = root;
		
	}
	
	public void walk(final File root) {
		
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
				clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
				clone.col = content[from].from;
				clone.type = SyntaxNodeType.ANY_NAME;
				clone.value = content[from].id;
				children = new SyntaxNode[] {null, clone};
				break;
			case ANY_SUBTREE	:
				clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
				clone.col = content[from].from;
				clone.type = SyntaxNodeType.ANY_SUBTREE;
				clone.value = content[from].id;
				children = new SyntaxNode[] {null, clone};
				break;
			case ORDINAL_NAME	:
				clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
				clone.col = content[from].from;
				clone.type = SyntaxNodeType.ORDINAL_NAME;
				clone.value = content[from].id;
				children = new SyntaxNode[] {null, clone};
				break;
			case WILDCARD_NAME	:
				clone = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) node.clone();
				clone.col = content[from].from;
				clone.type = SyntaxNodeType.WILDCARD_NAME;
				clone.value = content[from].id;
				children = new SyntaxNode[] {null, clone};
				break;
			case START_ALTER	:
				final List<SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>>	list = new ArrayList<>();
				
				list.add(null);
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
			
			from = buildCurrentName(content, from + 1, node);
			children[0] = child;
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
					
					from = buildExpression(content, from + 1, ExprPriority.ADD, node);
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
							operations |= (1L << count);
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
					result.add(new Lexema(LexType.EOF, index++, index));
					break loop;
				case '/' :
					result.add(new Lexema(LexType.SEPARATOR, index++, index));
					separatorDetected = true;
					break;
				case '(' :
					result.add(new Lexema(LexType.OPEN, index++, index));
					continue loop;
				case ')' :
					result.add(new Lexema(LexType.CLOSE, index++, index));
					continue loop;
				case '{' :
					result.add(new Lexema(LexType.START_ALTER, index++, index));
					continue loop;
				case '}' :
					result.add(new Lexema(LexType.END_ALTER, index++, index));
					continue loop;
				case '[' :
					result.add(new Lexema(LexType.START_EXPR, index++, index));
					expressionDetected = true;
					continue loop;
				case ']' :
					result.add(new Lexema(LexType.END_EXPR, index++, index));
					expressionDetected = false;
					continue loop;
				case '=' :
					result.add(new Lexema(LexType.COMPARE, index++, index));
					continue loop;
				case '+' :
					result.add(new Lexema(LexType.ADD, index++, index));
					continue loop;
				case '-' :
					result.add(new Lexema(LexType.SUBTRACT, index++, index));
					continue loop;
				case '&' :
					if (content[index + 1] == '&') {
						result.add(new Lexema(LexType.AND, index += 2, index));
						continue loop;
					}
					else {
						throw new SyntaxException(0, index, "Unknown lexema");
					}
				case '|' :
					if (content[index + 1] == '|') {
						result.add(new Lexema(LexType.OR, index += 2, index));
					}
					else {
						result.add(new Lexema(LexType.ALTER, index++, index));
					}
					continue loop;
				case '!' :
					if (content[index + 1] == '=') {
						result.add(new Lexema(LexType.COMPARE, index += 2, index));
					}
					else {
						result.add(new Lexema(LexType.NOT, index++, index));
					}
					continue loop;
				case '>' :
					if (content[index + 1] == '=') {
						result.add(new Lexema(LexType.COMPARE, index += 2, index));
					}
					else {
						result.add(new Lexema(LexType.COMPARE, index++, index));
					}
					continue loop;
				case '<' :
					if (content[index + 1] == '=') {
						result.add(new Lexema(LexType.COMPARE, index += 2, index));
					}
					else if (content[index + 1] == '>') {
						result.add(new Lexema(LexType.COMPARE, index += 2, index));
					}
					else {
						result.add(new Lexema(LexType.COMPARE, index++, index));
					}
					continue loop;
				case '.' :
					if (sameFirst) {
						if (content[index + 1] == '.' && content[index + 2] == '/') {
							result.add(new Lexema(LexType.DOT_SEPARATOR, index += 3, index));
						}
						else if (content[index + 1] == '/') {
							result.add(new Lexema(LexType.DOT_SEPARATOR, index += 2, index));
						}
						sameFirst = false;
						continue loop;
					}
					break;
				case '*' :
					if (separatorDetected) {
						if (content[index + 1] == '*') {
							result.add(new Lexema(LexType.ANY_SUBTREE, index += 2, index));
						}
						else if (content[index + 1] == '/') {
							result.add(new Lexema(LexType.ANY_NAME, index += 2, index));
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
					result.add(new Lexema(wildCard ? LexType.WILDCARD_NAME : LexType.ORDINAL_NAME, from, index));
				}
			}
		}
		
		return result.toArray(new Lexema[result.size()]);
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
}
