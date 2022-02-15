package chav1961.bt.clipper.inner.streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.BitCharSet;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

class CommandParser {
	static final char[][]		NULL_TERMINALS = new char[0][0];
	static final long			AMP_ID = 0L;
	
	private static BitCharSet	STOP_CHARS = new BitCharSet("\r\n\t\f [];\\=><#");
	private static final char[]	AMP_TEXT = "&".toCharArray();

	
	static PatternAndSubstitutor build(final char[] content, int from, final boolean fourLetter) throws SyntaxException {
		final SyntaxTreeInterface<Long>			names = new AndOrTree<>(1,1);
		final SyntaxNode<NodeType, SyntaxNode> 	root = new SyntaxNode<>(0, 0, NodeType.Root, 0, null);
		final Lexema[]							lex = parse(content, from, names, fourLetter);
		
		from = buildTree(lex, 0, Level.Top, root);
		return new PatternAndSubstitutorImpl(names, root, 0);
	}

	static boolean identify(final char[] content, int from, final SyntaxTreeInterface<Long> names, final SyntaxNode<NodeType, SyntaxNode> root, final int[] temp, final int[][] markerRanges) throws SyntaxException {
		int		current = 0;
		long	id;
		
		for (;;) {
			from = CharUtils.skipBlank(content, from, true);
			
			switch((NodeType)root.children[current].type) {
				case Mandatory	:
					switch (((Lexema)root.children[current].cargo).type) {
						case Char :
							break;
						case Keyword :
							from = CharUtils.parseName(content, from, temp); 
							
							if (names.seekName(content, temp[0], temp[1]) < 0) {
								return false;
							}
							break;
						case ExtendedMarker		:
							break;
						case ListMarker			:
							break;
						case RegularMarker		:
							break;
						case RestrictedMarker	:
							markerRanges[1] = new int[] {from, 0};
							from = extractExpression(content, from, NULL_TERMINALS);
							markerRanges[1][1] = from;
							break;
						case WildMarker			:
							temp[0] = from; 
							while(content[from] < ' ' && content[from] != '\n') {
								from++;
							}
							temp[1] = from - 1;
							return true;
						default :
							throw new UnsupportedOperationException("Lexema type ["+((Lexema)root.children[current].cargo).type+"] is not supported uet"); 
					}
					break;
				case Optional	:
					break;
				default:
					break;
			}
			from++;
		}
	}

	private static boolean identify(final char[] content, int from, final SyntaxTreeInterface<Long> names, final Lexema lex, final int[] temp, final int[][] markerRanges) {
//		switch (lex.type) {
//			case Char :
//				break;
//			case Keyword :
//				from = CharUtils.parseName(content, from, temp); 
//				
//				return names.seekName(content, temp[0], temp[1])>=0;
//			case ExtendedMarker		:
//				break;
//			case ListMarker			:
//				break;
//			case RegularMarker		:
//				break;
//			case RestrictedMarker	:
//				break;
//			case WildMarker			:
//				temp[0] = from; 
//				while(content[from] < ' ' && content[from] != '\n') {
//					from++;
//				}
//				temp[1] = from - 1;
//				return true;
//			default :
//				throw new UnsupportedOperationException("Lexema type ["+lex.type+"] is not supported uet"); 
//		}
		return false;
	}
	
	static void upload(final char[] content, final SyntaxNode<NodeType, SyntaxNode> root, final int[] temp, final int[][] markerRanges) {
	}
	
	static Lexema[] parse(final char[] content, int from, final SyntaxTreeInterface<Long> names, final boolean fourLetter) throws SyntaxException {
		final List<Lexema>	result = new ArrayList<>();
		final int[]			forNames = new int[2];
		int					idCount = 0;  
		boolean				afterErgo = false;
		
		names.placeName(AMP_TEXT, 0, AMP_TEXT.length, AMP_ID);
		
loop:	for(;;) {
			from = CharUtils.skipBlank(content, from, true);
			
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
					if (!afterErgo) {
						throw new SyntaxException(0, from, "Use ';' in the left of '=>'");
					}
					else {
						result.add(new Lexema(Lexema.LexType.Continuation));
					}
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
							break;
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
									final List<Long>	list = new ArrayList<>();
									final int			nameId = forNames[1];
									
									idCount += forNames[0];
									from = extractList(content, from, names, forNames, idCount, list);
									idCount += forNames[0];
									result.add(new Lexema(Lexema.LexType.ListMarker, nameId, Utils.unwrapArray(list.toArray(new Long[list.size()]))));
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
						from = CharUtils.parseName(content, from, forNames) - 1;
						
						long	nameId = names.seekNameI(content, forNames[0], forNames[1]);
						
						if (nameId < 0) {
							nameId = names.placeName(content, forNames[0], forNames[1], 0L);
						}
						
						result.add(new Lexema(Lexema.LexType.Keyword, nameId));
					}
					else if (content[from] > ' ') {
						forNames[0] = from;
						while (!STOP_CHARS.contains(content[from])) {
							from++;
						}
						forNames[1] = from--;
						result.add(new Lexema(Lexema.LexType.Char, names.placeName(content, forNames[0], forNames[1], 0L)));
					}
					else {
						throw new SyntaxException(0, from, "Illegal char ["+(0+content[from])+"]");
					}
			}
			from++;
		}
		return result.toArray(new Lexema[result.size()]);
	}
	
	private static int extractName(final char[] content, int from, final SyntaxTreeInterface<Long> names, final int[] temp, final int newId) {
		from = CharUtils.parseName(content, from, temp);
		
		final long	id = names.seekNameI(content, temp[0], temp[1] + 1);
		
		if (id >= 0) {
			temp[0] = 0;
			temp[1] = names.getCargo(id).intValue();
		}
		else {
			names.placeName(content, temp[0], temp[1] + 1, Long.valueOf(newId));
			temp[0] = 1;
			temp[1] = newId;
		}
		return from;
	}

	private static int extractList(final char[] content, int from, final SyntaxTreeInterface<Long> names, final int[] temp, int newId, final List<Long> list) {
		do {	// colon or div awaited here!
			from = CharUtils.skipBlank(content, from + 1, true);
			if (Character.isLetter(content[from])) {
				from = extractName(content, from, names, temp, newId);
				newId += temp[0];
				list.add(Long.valueOf(temp[1]));
			}
			else if (content[from] == '&') {
				list.add(AMP_ID);
				from++;
			}
			from = CharUtils.skipBlank(content, from, true);
		} while (content[from] == ',');
		
		temp[0] = newId;
		return from;
	}
	
	private static int extractExpression(final char[] content, int from, final char[]... terminals) throws SyntaxException {
		boolean	inQuotes = false;
		
		for(;;) {
			switch(content[from]) {
				case '\n' : case ']' : case ')' : case '}' :
					return from;
				case '\"' : case '\'' :
					inQuotes = !inQuotes;
					break;
				case '['	:
					if (!inQuotes && content[from = extractExpression(content, from + 1, NULL_TERMINALS)] != ']') {
						throw new SyntaxException(0, from, "']' is missing");
					}
					break;
				case '(' 	:
					if (!inQuotes && content[from = extractExpression(content, from + 1, NULL_TERMINALS)] != ')') {
						throw new SyntaxException(0, from, "')' is missing");
					}
					break;
				case '{' 	: 
					if (!inQuotes && content[from = extractExpression(content, from + 1, NULL_TERMINALS)] != '}') {
						throw new SyntaxException(0, from, "'}' is missing");
					}
					break;
				default :
					if (!inQuotes) {
						for (char[] item : terminals) {
							if (content[from] == item[0] && CharUtils.compare(content, from, item)) {
								return from;
							}
						}
					}
			}
			from++;
		}
	}
	
	private static int buildTree(final Lexema[] content, int from, final Level level, final SyntaxNode<NodeType, SyntaxNode> root) throws SyntaxException {
		switch (level) {
			case Top	:
				final SyntaxNode<NodeType, SyntaxNode> 	left = (SyntaxNode<NodeType, SyntaxNode>) root.clone(), right = (SyntaxNode<NodeType, SyntaxNode>) root.clone();
				
				from = buildTree(content, from, Level.Left, left);
				if (content[from].type == Lexema.LexType.Ergo) {
					from = buildTree(content, from + 1, Level.Right, right);
					root.type = NodeType.Root;
					root.children = new SyntaxNode[]{left, right};
					if (content[from].type == Lexema.LexType.EOF) {
						return from + 1;
					}
					else {
						throw new SyntaxException(0, from, "Dust in the tail");
					}
				}
				else {
					throw new SyntaxException(0, from, "Ergo sign is missing");
				}
			case Left	:
				final List<SyntaxNode<NodeType, SyntaxNode>>	leftContent = new ArrayList<>();
				SyntaxNode<NodeType, SyntaxNode>				leftItem;
				
leftLoop:		for(;;) {
					switch (content[from].type) {
						case Char : case ExtendedMarker : case Keyword : case ListMarker : case RegularMarker : case RestrictedMarker : case WildMarker :
							leftItem = (SyntaxNode<NodeType, SyntaxNode>) root.clone();
							leftItem.type = NodeType.Mandatory;
							leftItem.cargo = content[from];
							leftContent.add(leftItem);
							break;
						case Continuation		:
							break;
						case OpenB				:
							final List<SyntaxNode<NodeType, SyntaxNode>>	leftOptional = new ArrayList<>();
							
							do {
								leftItem = (SyntaxNode<NodeType, SyntaxNode>) root.clone();
								from = buildTree(content, from + 1, Level.Left, leftItem);
								if (content[from].type != Lexema.LexType.CloseB) {
									throw new SyntaxException(0, from, "']' is missing"); 
								}
								else {
									leftItem.type = NodeType.Optional;
									leftOptional.add(leftItem);
								}
							} while (content[from].type == Lexema.LexType.OpenB);
							leftItem = (SyntaxNode<NodeType, SyntaxNode>) root.clone();
							leftItem.type = NodeType.Optional;
							leftItem.children = leftOptional.toArray(new SyntaxNode[leftOptional.size()]);
							leftContent.add(leftItem);
							break;
						default :
							break leftLoop;
					}
					from++;
				}
				break;
			case Right	:
				final List<SyntaxNode<NodeType, SyntaxNode>>	rightContent = new ArrayList<>();
				SyntaxNode<NodeType, SyntaxNode>				rightItem;
				
rightLoop:		for(;;) {
					switch (content[from].type) {
						case Char : case Keyword : case RegularResultMarker : case DumbResultMarker : case NormalResultMarker : case SmartResultMarker : case BlockResultMarker : case BoolResultMarker : case Continuation :
							rightItem = (SyntaxNode<NodeType, SyntaxNode>) root.clone();
							rightItem.type = NodeType.Mandatory;
							rightItem.cargo = content[from];
							rightContent.add(rightItem);
							break;
						case OpenB				:
							final List<SyntaxNode<NodeType, SyntaxNode>>	rightOptional = new ArrayList<>();
							
							do {
								rightItem = (SyntaxNode<NodeType, SyntaxNode>) root.clone();
								from = buildTree(content, from + 1, Level.Right, rightItem);
								if (content[from].type != Lexema.LexType.CloseB) {
									throw new SyntaxException(0, from, "']' is missing"); 
								}
								else {
									rightItem.type = NodeType.Optional;
									rightOptional.add(rightItem);
								}
							} while (content[from].type == Lexema.LexType.OpenB);
							rightItem = (SyntaxNode<NodeType, SyntaxNode>) root.clone();
							rightItem.type = NodeType.Optional;
							rightItem.children = rightOptional.toArray(new SyntaxNode[rightOptional.size()]);
							rightContent.add(rightItem);
							break;
						default :
							break rightLoop;
					}
					from++;
				}
				break;
			default 	:
				throw new UnsupportedOperationException("Level type ["+level+"] is not supported yet"); 
		}
		return from;
	}
	
	private static enum NodeType {
		Mandatory, Optional, Root
	}

	private static enum Level {
		Top, Left, Right
	}
	
	static class Lexema {
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(associations);
			result = prime * result + (int) (entityId ^ (entityId >>> 32));
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Lexema other = (Lexema) obj;
			if (!Arrays.equals(associations, other.associations)) return false;
			if (entityId != other.entityId) return false;
			if (type != other.type) return false;
			return true;
		}

		@Override
		public String toString() {
			return "Lexema [type=" + type + ", entityId=" + entityId + ", associations=" + Arrays.toString(associations) + "]";
		}
	}
	
	private static class PatternAndSubstitutorImpl implements PatternAndSubstitutor {
		private final SyntaxTreeInterface<Long> 		names;
		private final SyntaxNode<NodeType, SyntaxNode>	root;
		private final int								markerCount;
		
		private PatternAndSubstitutorImpl(final SyntaxTreeInterface<Long> names, final SyntaxNode<NodeType, SyntaxNode> root, final int markerCount) {
			this.names = names;
			this.root = root;
			this.markerCount = markerCount;
		}

		@Override
		public char[] getKeyword() {
			return (char[])root.children[0].cargo;
		}

		@Override
		public int process(final char[] data, final int from, final OutputWriter writer) throws SyntaxException {
			final int[]		ranges = new int[2];
			final int[][]	markers = new int[markerCount][2]; 
			
			if (identify(data, from, names, root.children[0], ranges, markers)) {
				final int	result = ranges[0]; 
				
				upload(data, root.children[1], ranges, markers);
				return result;
			}
			else {
				return from;
			}
		}
		
	}
}