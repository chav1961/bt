package chav1961.bt.nlp.grammar;

import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

class InternalUtils {
	static final char	EOF = '\uFFFF';
	
	/*
	rule = left '->' rightList ('{' spec '}'){0,1}
	left = NT
	rightList = right ('|' right)*
	right = (NT | T | '"regex"')'<'props'>' ('*'){0,1}
	props = propAnd (';' propAnd)*
	propAnd = '~'* prop
	prop = name '(' parameter (',' parameter)* ')' 
	paramer = value | prop
	 */
	
	static Lexema[] parseLex(final char[] content, final SyntaxTreeInterface<?> names) throws SyntaxException {
		final List<Lexema>	result = new ArrayList<>();
		final StringBuilder	sb = new StringBuilder();
		final int[]			bounds = new int[2];
		
		int from = 0, line = 0, lineStart = 0, before;
		
loop:	for(;;) {
			from = CharUtils.skipBlank(content, from, true);
			
			switch (content[from]) {
				case '\n'	:
					lineStart = from + 1;
					line++;
					from++;
					break;
				case EOF 	:
					result.add(new Lexema(line, from - lineStart, LexType.EOF));
					break loop;
				case '|' 	:
					result.add(new Lexema(line, from - lineStart, LexType.Or));
					from++;
					break;
				case '&' 	:
					result.add(new Lexema(line, from - lineStart, LexType.And));
					from++;
					break;
				case '~' 	:
					result.add(new Lexema(line, from - lineStart, LexType.Not));
					from++;
					break;
				case '*' 	:
					result.add(new Lexema(line, from - lineStart, LexType.Asterisk));
					from++;
					break;
				case ',' 	:
					result.add(new Lexema(line, from - lineStart, LexType.Colon));
					from++;
					break;
				case ';' 	:
					result.add(new Lexema(line, from - lineStart, LexType.Semicolon));
					from++;
					break;
				case '.' 	:
					result.add(new Lexema(line, from - lineStart, LexType.Dot));
					from++;
					break;
				case '(' 	:
					result.add(new Lexema(line, from - lineStart, LexType.Open));
					from++;
					break;
				case ')' 	:
					result.add(new Lexema(line, from - lineStart, LexType.Close));
					from++;
					break;
				case '[' 	:
					result.add(new Lexema(line, from - lineStart, LexType.OpenB));
					from++;
					break;
				case ']' 	:
					result.add(new Lexema(line, from - lineStart, LexType.CloseB));
					from++;
					break;
				case '<' 	:
					result.add(new Lexema(line, from - lineStart, LexType.OpenA));
					from++;
					break;
				case '>' 	:
					result.add(new Lexema(line, from - lineStart, LexType.CloseA));
					from++;
					break;
				case '{' 	:
					result.add(new Lexema(line, from - lineStart, LexType.OpenF));
					from++;
					break;
				case '}' 	:
					result.add(new Lexema(line, from - lineStart, LexType.CloseF));
					from++;
					break;
				case '-' 	:
					if (content[from + 1] == '>') {
						result.add(new Lexema(line, from - lineStart, LexType.Ergo));
						from += 2;
					}
					else {
						throw new SyntaxException(line, from - lineStart, "Unsupported symbol ["+content[from]+"]");
					}
					break;
				case '\''	:
					before = from;
					from = CharUtils.parseStringExtended(content, from + 1, '\'', sb);
					
					if (content[from] == '\'') {
						result.add(new Lexema(line, before - lineStart, LexType.Constant, names.placeName(sb, null)));
						sb.setLength(0);
						from++;
					}
					else {
						throw new SyntaxException(line, from - lineStart, "Unpaired quotas");
					}
					break;
				default :
					if (Character.isJavaIdentifierStart(content[from])) {
						final boolean lowerCase = Character.isLowerCase(content[from]);
							
						from = CharUtils.parseNameExtended(content, from, bounds, '-');
						result.add(new Lexema(line, from - lineStart, lowerCase ? LexType.Pred : LexType.Var
										, names.placeName(content, bounds[0], bounds[1]-bounds[0], null)
									)
						);
					}
					else {
						throw new SyntaxException(line, from - lineStart, "Unsupported symbol ["+content[from]+"]");
					}
			}
		}
		return result.toArray(new Lexema[result.size()]);
	}
	
	static enum LexType {
		Pred,
		Var,
		Constant,
		Ergo,
		Or,
		And,
		Not,
		Open,
		Close,
		OpenB,
		CloseB,
		OpenA,
		CloseA,
		OpenF,
		CloseF,
		Dot,
		Colon,
		Semicolon,
		Asterisk,
		EOF
	}

	static class Lexema {
		final int		row;
		final int 		col;
		final LexType	type;
		final long		textId;

		Lexema(final int row, final int col, final LexType type) {
			this(row, col, type, -1);
		}
		
		Lexema(final int row, final int col, final LexType type, final long textId) {
			this.row = row;
			this.col = col;
			this.type = type;
			this.textId = textId;
		}

		@Override
		public String toString() {
			return "Lexema [row=" + row + ", col=" + col + ", type=" + type + ", textId=" + textId + "]";
		}
	}

}
