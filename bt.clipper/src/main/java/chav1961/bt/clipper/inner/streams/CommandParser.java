package chav1961.bt.clipper.inner.streams;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

class CommandParser {
	
	
	static PatternAndSubstitutor build(final char[] content, int from, final boolean fourLetter) throws SyntaxException {
		final SyntaxTreeInterface<Long>		names = new AndOrTree<>();
		
		final Lexema[]	lex = parse(content, from, names, fourLetter);
		return null;
	}
	
	private static Lexema[] parse(final char[] content, int from, final SyntaxTreeInterface<Long> names, final boolean fourLetter) throws SyntaxException {
		final List<Lexema>	result = new ArrayList<>();
		final int[]			forNames = new int[2];
		int					idCount = 0;
		boolean				afterErgo = false;
		
loop:	for(;;) {
			switch (content[from]) {
				case '\r' : case '\n' :
					result.add(new Lexema(Lexema.LexType.EOF));
					break loop;
				case '[' :
					result.add(new Lexema(Lexema.LexType.OpenB));
					break;
				case ']' :
					result.add(new Lexema(Lexema.LexType.CloseB));
					break;
				case ';' :
					result.add(new Lexema(Lexema.LexType.Continuation));
					break;
				case '\\' :
					result.add(new Lexema(Lexema.LexType.Char, content[++from]));
					break;
				case '>' :
					result.add(new Lexema(Lexema.LexType.Continuation));
					break;
				case '=' :
					if (content[from + 1] == '>') {
						result.add(new Lexema(Lexema.LexType.Ergo));
						afterErgo = true;
						from++;
					}
					else {
						result.add(new Lexema(Lexema.LexType.Char, content[from]));
					}
					break;
				case '#' :
					if (content[from + 1] == '<') {
						if (!afterErgo) {
							throw new SyntaxException(0, from, "Use dumb result marker in the left of '=>'");
						}
						else if (Character.isLetter(content[from + 2])) {
							from = extractName(content, from+2, names, forNames, idCount);
							if (content[from] != '>') {
								throw new SyntaxException(0, from, "Missing '>'");
							}
							else {
								idCount += forNames[0];
								result.add(new Lexema(Lexema.LexType.DumbResultMarker, forNames[1]));
							}
						}
						else {
							
						}
					}
					else {
						result.add(new Lexema(Lexema.LexType.Char, content[from]));
					}
					break;
				case '<' :
					switch (content[from + 1]) {
						case '(' 	:
							if (Character.isLetter(content[from + 2])) {
								from = extractName(content, from + 2, names, forNames, idCount);
								if (content[from] == ')' && content[from + 1] == '>') {
									idCount += forNames[0];
									result.add(new Lexema(afterErgo ? Lexema.LexType.SmartResultMarker : Lexema.LexType.ExtendedMarker, forNames[1]));
									from++;
								}
								else {
									throw new SyntaxException(0, from, "Missing ')>'");
								}
							}
							else {
								throw new SyntaxException(0, from, "Missing name");
							}
						case '*' 	:
							if (afterErgo) {
								throw new SyntaxException(0, from, "Use wild marker in the right of '=>'");
							}
							else if (Character.isLetter(content[from + 2])) {
								from = extractName(content, from + 2, names, forNames, idCount);
								if (content[from] == '*' && content[from + 1] == '>') {
									idCount += forNames[0];
									result.add(new Lexema(Lexema.LexType.WildMarker, forNames[1]));
									from++;
								}
								else {
									throw new SyntaxException(0, from, "Missing '*>'");
								}
							}
							else {
								throw new SyntaxException(0, from, "Missing name");
							}
							break;
						case '\"'	:
							if (!afterErgo) {
								throw new SyntaxException(0, from, "Use string result marker in the left of '=>'");
							}
							else if (Character.isLetter(content[from + 2])) {
								from = extractName(content, from + 2, names, forNames, idCount);
								if (content[from] == '\"' && content[from + 1] == '>') {
									idCount += forNames[0];
									result.add(new Lexema(Lexema.LexType.NormalResultMarker, forNames[1]));
									from++;
								}
								else {
									throw new SyntaxException(0, from, "Missing '\">'");
								}
							}
							else {
								throw new SyntaxException(0, from, "Missing name");
							}
							break;
						case '{'	:
							if (!afterErgo) {
								throw new SyntaxException(0, from, "Use block result marker in the left of '=>'");
							}
							else if (Character.isLetter(content[from + 2])) {
								from = extractName(content, from + 2, names, forNames, idCount);
								if (content[from] == '}' && content[from + 1] == '>') {
									idCount += forNames[0];
									result.add(new Lexema(Lexema.LexType.BlockResultMarker, forNames[1]));
									from++;
								}
								else {
									throw new SyntaxException(0, from, "Missing '.>'");
								}
							}
							else {
								throw new SyntaxException(0, from, "Missing name");
							}
							break;
						case '.'	:
							if (!afterErgo) {
								throw new SyntaxException(0, from, "Use logify result marker in the left of '=>'");
							}
							else if (Character.isLetter(content[from + 2])) {
								from = extractName(content, from + 2, names, forNames, idCount);
								if (content[from] == '.' && content[from + 1] == '>') {
									idCount += forNames[0];
									result.add(new Lexema(Lexema.LexType.BoolResultMarker, forNames[1]));
									from++;
								}
								else {
									throw new SyntaxException(0, from, "Missing '.>'");
								}
							}
							else {
								throw new SyntaxException(0, from, "Missing name");
							}
							break;
						default :
							if (Character.isLetter(content[from + 1])) {
								from = extractName(content, from + 1, names, forNames, idCount);
								if (content[from] == ',' && content[from + 1] == '.' && content[from + 2] == '.' && content[from + 3] == '.' && content[from + 4] == '>') {
									if (!afterErgo) {
										idCount += forNames[0];
										result.add(new Lexema(Lexema.LexType.ListMarker, forNames[1]));
										from += 4;
									}
									else {
										throw new SyntaxException(0, from, "Use list marker in the right of '=>'");
									}
								}
								else if (content[from] == '>') {
									idCount += forNames[0];
									result.add(new Lexema(afterErgo ? Lexema.LexType.RegularResultMarker : Lexema.LexType.RegularMarker, forNames[1]));
								}
								else if (content[from] == ':') {
//									idCount += forNames[0];
//									result.add(new Lexema(afterErgo ? Lexema.LexType.RegularResultMarker : Lexema.LexType.RegularMarker, forNames[1]));
								}
								else {
									throw new SyntaxException(0, from, "Missing '>' or ',...>");
								}
							}
							else {
								throw new SyntaxException(0, from, "Missing name");
							}
					}
					break;
				default :
					if (Character.isLetter(content[from])) {
						from = CharUtils.parseName(content, from, forNames);
					}
					else if (content[from] > ' ') {
						result.add(new Lexema(Lexema.LexType.Char, content[from]));
					}
					else {
						throw new SyntaxException(0, from, "Illegal char ["+(0+content[from])+"]");
					}
			}
			from++;
		}
		return null;
	}
	
	private static int extractName(final char[] content, int from, final SyntaxTreeInterface<Long> names, final int[] temp, final int newId) {
		from = CharUtils.parseName(content, from, temp);
		
		final long	id = names.seekName(content, temp[0], temp[1]);
		
		if (id >= 0) {
			temp[0] = 0;
			temp[1] = names.getCargo(id).intValue();
		}
		else {
			names.placeName(content, temp[0], temp[1], Long.valueOf(newId));
			temp[0] = 1;
			temp[1] = newId;
		}
		return from;
	}
	
	private static class Lexema {
		public static enum LexType {
			EOF, Keyword, Char, OpenB, CloseB, Ergo, Continuation,  
			RegularMarker, ListMarker, RestrictedMarker, WildMarker, ExtendedMarker,
			RegularResultMarker, DumbResultMarker, NormalResultMarker, SmartResultMarker, BlockResultMarker, BoolResultMarker 
		}
		
		private final LexType	type;
		private final long		entityId;
		private final long[]	associations;
		
		public Lexema(LexType type) {
			this(type, -1, null);
		}

		public Lexema(LexType type, long entityId) {
			this(type, entityId, null);
		}
		
		public Lexema(LexType type, long entityId, long[] associations) {
			this.type = type;
			this.entityId = entityId;
			this.associations = associations;
		}

		public LexType getType() {
			return type;
		}
		
		public long getEntityId() {
			return entityId;
		}
		
		public long[] getAssociations() {
			return associations;
		}
		
		
	}
}
