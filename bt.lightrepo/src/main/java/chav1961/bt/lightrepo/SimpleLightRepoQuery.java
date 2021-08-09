package chav1961.bt.lightrepo;

import chav1961.bt.lightrepo.interfaces.LightRepoInterface.CommitDescriptor;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.RepoItemDescriptor;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.TransalatedQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import chav1961.bt.lightrepo.AbstractLightRepo.LexType;
import chav1961.bt.lightrepo.AbstractLightRepo.NodeType;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface;
import chav1961.bt.lightrepo.interfaces.LightRepoQueryInterface;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

public class SimpleLightRepoQuery implements LightRepoQueryInterface {
	private static final Hashtable<String, Object>		NULL_ANTTRIBUTES = new Hashtable<>();
	private static final SyntaxTreeInterface<LexType>	PREDEFINED_NAMES = new AndOrTree<>();
	static final char									END_OF_QUERY = '\uFFFF';
	
	static {
		PREDEFINED_NAMES.placeName("or", LexType.OR);
		PREDEFINED_NAMES.placeName("and", LexType.AND);
		PREDEFINED_NAMES.placeName("not", LexType.NOT);
		PREDEFINED_NAMES.placeName("in", LexType.IN);
		
		PREDEFINED_NAMES.placeName(LightRepoInterface.EXISTS_FUNC, LexType.EXISTS);
		PREDEFINED_NAMES.placeName(LightRepoInterface.CONTAINS_FUNC, LexType.CONTAINS);
		PREDEFINED_NAMES.placeName(LightRepoInterface.APPEARS_FUNC, LexType.APPEARS);
		PREDEFINED_NAMES.placeName(LightRepoInterface.DISAPPEARS_FUNC, LexType.DISAPPEARS);
		PREDEFINED_NAMES.placeName(LightRepoInterface.NOW_FUNC, LexType.NOW);
		PREDEFINED_NAMES.placeName(LightRepoInterface.CREATED_FUNC, LexType.CREATED);
		PREDEFINED_NAMES.placeName(LightRepoInterface.CHANGED_FUNC, LexType.CHANGED);
		PREDEFINED_NAMES.placeName(LightRepoInterface.RENAMED_FUNC, LexType.RENAMED);
		PREDEFINED_NAMES.placeName(LightRepoInterface.REMOVED_FUNC, LexType.REMOVED);
		
		PREDEFINED_NAMES.placeName(LightRepoInterface.PREV_MOD, LexType.PREV);
		PREDEFINED_NAMES.placeName(LightRepoInterface.NEXT_MOD, LexType.NEXT);
		PREDEFINED_NAMES.placeName(LightRepoInterface.UPPERCASE_MOD, LexType.UPPERCASE);
		PREDEFINED_NAMES.placeName(LightRepoInterface.LOWERCASE_MOD, LexType.LOWERCASE);
		
		PREDEFINED_NAMES.placeName(LightRepoInterface.FILE_VAR, LexType.FILE);
		PREDEFINED_NAMES.placeName(LightRepoInterface.COMMIT_VAR, LexType.COMMIT);
		
		PREDEFINED_NAMES.placeName(LightRepoInterface.AUTHOR_F, LexType.AUTHOR);
		PREDEFINED_NAMES.placeName(LightRepoInterface.COMMENT_F, LexType.COMMENT);
		PREDEFINED_NAMES.placeName(LightRepoInterface.COMMIT_ID_F, LexType.ID);
		PREDEFINED_NAMES.placeName(LightRepoInterface.PATH_F, LexType.PATH);
		PREDEFINED_NAMES.placeName(LightRepoInterface.CHANGE_F, LexType.CHANGE);
		PREDEFINED_NAMES.placeName(LightRepoInterface.TIMESTAMP_F, LexType.TIMESTAMP);
		PREDEFINED_NAMES.placeName(LightRepoInterface.PARSEABLE_F, LexType.PARSEABLE);
		PREDEFINED_NAMES.placeName(LightRepoInterface.VERSION_F, LexType.VERSION);
		PREDEFINED_NAMES.placeName(LightRepoInterface.CONTENT_F, LexType.CONTENT);
	}

	private static enum LexGroup {
		OR_GROUP,
		AND_GROUP,
		NOT_GROUP,
		COMPARE_GROUP,
		ADD_GROUP,
		TERM_GROUP,
		SUBTERM_GROUP,
		UNKNOWN;
	}
	
	private static enum TermType {
		FUNCTION,
		PREDEFINED_NAME,
		FILE_SUFFIX,
		COMMIT_SUFFIX,
		ANY_SUFFIX,
		UNKNOWN;
	}
	
	static enum LexType {
		OR(LexGroup.OR_GROUP),
		AND(LexGroup.AND_GROUP),
		NOT(LexGroup.NOT_GROUP),
		LIKE(LexGroup.COMPARE_GROUP),
		EQ(LexGroup.COMPARE_GROUP),
		NE(LexGroup.COMPARE_GROUP),
		GT(LexGroup.COMPARE_GROUP),
		GE(LexGroup.COMPARE_GROUP),
		LT(LexGroup.COMPARE_GROUP),
		LE(LexGroup.COMPARE_GROUP),
		IN(LexGroup.COMPARE_GROUP),
		RANGE(LexGroup.UNKNOWN),
		ADD(LexGroup.ADD_GROUP),
		SUB(LexGroup.ADD_GROUP),
		NUMBER(LexGroup.TERM_GROUP),
		STRING(LexGroup.TERM_GROUP),
		
		EXISTS(LexGroup.TERM_GROUP,1,1),
		CONTAINS(LexGroup.TERM_GROUP,2,2),
		APPEARS(LexGroup.TERM_GROUP,2,2),
		DISAPPEARS(LexGroup.TERM_GROUP,2,2),
		NOW(LexGroup.TERM_GROUP,0,0),
		CREATED(LexGroup.TERM_GROUP,1,1),
		CHANGED(LexGroup.TERM_GROUP,1,1),
		RENAMED(LexGroup.TERM_GROUP,1,2),
		REMOVED(LexGroup.TERM_GROUP,1,1),
		
		PREV(LexGroup.SUBTERM_GROUP,0,1),
		NEXT(LexGroup.SUBTERM_GROUP,0,1),
		FILE(LexGroup.TERM_GROUP,TermType.PREDEFINED_NAME),
		COMMIT(LexGroup.SUBTERM_GROUP,TermType.PREDEFINED_NAME),
		AUTHOR(LexGroup.SUBTERM_GROUP,TermType.COMMIT_SUFFIX),
		COMMENT(LexGroup.SUBTERM_GROUP,TermType.COMMIT_SUFFIX),
		ID(LexGroup.SUBTERM_GROUP,TermType.COMMIT_SUFFIX),
		PATH(LexGroup.SUBTERM_GROUP,TermType.FILE_SUFFIX),
		TIMESTAMP(LexGroup.SUBTERM_GROUP,TermType.FILE_SUFFIX),
		CHANGE(LexGroup.SUBTERM_GROUP,TermType.FILE_SUFFIX),
		PARSEABLE(LexGroup.SUBTERM_GROUP,TermType.FILE_SUFFIX),
		VERSION(LexGroup.SUBTERM_GROUP,TermType.ANY_SUFFIX),
		CONTENT(LexGroup.SUBTERM_GROUP,TermType.ANY_SUFFIX),
		UPPERCASE(LexGroup.SUBTERM_GROUP,TermType.ANY_SUFFIX),
		LOWERCASE(LexGroup.SUBTERM_GROUP,TermType.ANY_SUFFIX),
		OPENB(LexGroup.UNKNOWN),
		CLOSEB(LexGroup.UNKNOWN),
		DOT(LexGroup.UNKNOWN),
		COMMA(LexGroup.UNKNOWN),
		
		EOF(LexGroup.UNKNOWN);
		
		private final LexGroup	group;
		private final TermType	termType;
		private final int		mandatoryArgC, argC;
		
		private LexType(final LexGroup group) {
			this.group = group;
			this.termType = TermType.UNKNOWN;
			this.mandatoryArgC = -1;
			this.argC = -1;
		}

		private LexType(final LexGroup group, final int mandatoryArgC, final int argC) {
			this.group = group;
			this.termType = TermType.FUNCTION;
			this.mandatoryArgC = mandatoryArgC;
			this.argC = argC;
		}

		private LexType(final LexGroup group, final TermType termType) {
			this.group = group;
			this.termType = termType;
			this.mandatoryArgC = -1;
			this.argC = -1;
		}
		
		public LexGroup getGroup() {
			return group;
		}
		
		public TermType getTermType() {
			return termType;
		}
		
		public int getMandatoryArgCount() {
			return mandatoryArgC;
		}
		
		public int getArgCount() {
			return argC;
		}
	}
	
	static enum NodeType {
		OR, AND, NOT,
		LIST, RANGE,
		COMPARISON,
		ADD,
		NUM_CONST, STR_CONST, VAR, FUNCION
	}
	
	static enum Priority {
		OR, AND, NOT, COMP, ADD, TERM
	}
	
	private final SyntaxNode<NodeType, SyntaxNode> 	root;
	private final boolean	hasCommits, hasRepoItems;
	private final boolean	hasExplicitCommits, hasExplicitRepoItems;
	private final String	commitsPattern, repoItemsPattern;
	
	public SimpleLightRepoQuery(final String query, final Hashtable<String, Object> attributes, final SyntaxNode<NodeType, SyntaxNode> root) {
		if (root == null) {
			throw new NullPointerException("Root node can't be null");
		}
		else {
			final List<List<SyntaxNode<NodeType, SyntaxNode>>>	dnf = new ArrayList<>();
			boolean	wasPath = false, wasCommit = false; 
			int		pathAmount = 0, commitAmount = 0; 
			
			walkDNF(root, false, dnf);
			
			for (List<SyntaxNode<NodeType, SyntaxNode>> item : dnf) {
				boolean	wasPathHere = false, wasCommitHere = false;
				
				for (SyntaxNode<NodeType, SyntaxNode> op : item) {
					if (canApplyPatternFor(op)) {
						if (isPureFieldUsed(op.children[0], LexType.COMMIT, LexType.ID)) {
							wasCommit = true;
							wasCommitHere = true;
						}
						else if (isPureFieldUsed(op.children[0], LexType.FILE, LexType.PATH)) {
							wasPath = true;
							wasPathHere = true;
						}
					}
				}
				if (wasPathHere) {
					pathAmount++;
				}
				if (wasCommitHere) {
					commitAmount++;
				}
			}
			
			this.root = root;
			this.hasCommits = wasCommit;
			this.hasRepoItems = wasPath;
			this.hasExplicitCommits = commitAmount == dnf.size();
			this.hasExplicitRepoItems = pathAmount == dnf.size();
			this.commitsPattern = hasExplicitCommits ? collectExplicitPattern(dnf, LexType.COMMIT, LexType.ID) : ".*";
			this.repoItemsPattern = hasExplicitRepoItems ? collectExplicitPattern(dnf, LexType.FILE, LexType.PATH) : ".*";
		}
	}
	
	@Override
	public boolean hasCommits() {
		return hasCommits;
	}

	@Override
	public boolean hasExplicitCommits() {
		return hasExplicitCommits;
	}

	@Override
	public boolean hasRepoItems() {
		return hasRepoItems;
	}

	@Override
	public boolean hasExplicitRepoItems() {
		return hasExplicitRepoItems;
	}

	@Override
	public String getCommitsAwaited() {
		return commitsPattern;
	}

	@Override
	public String getRepoItemsAwaited() {
		return repoItemsPattern;
	}

	@Override
	public boolean testCommit(final CommitDescriptor desc) {
		if (desc== null) {
			throw new NullPointerException("Commit descriptor can't be null");
		}
		else {
			return ((Boolean)testCommit(desc,root));
		}
	}

	@Override
	public boolean testRepoItem(final RepoItemDescriptor desc) {
		if (desc== null) {
			throw new NullPointerException("Repo item descriptor can't be null");
		}
		else {
			return ((Boolean)testRepoItem(desc,root));
		}
	}
	
	public static LightRepoQueryInterface translateQuery(final String  query, final Hashtable<String, Object> attributes) throws SyntaxException {
		final SyntaxNode<NodeType, SyntaxNode>	root = new SyntaxNode<>(0, 0, NodeType.OR, 0, null);
		final char[]					content = CharUtils.terminateAndConvert2CharArray(query, END_OF_QUERY);
		final Lexema[]					lexList = parseQuery(content, 0);
		final int						pos = translateQuery(Priority.OR, lexList, 0, attributes, root);
		final LightRepoQueryInterface	tq = new SimpleLightRepoQuery(query, attributes, root);
		
		return tq;
	}
	
	static Lexema[] parseQuery(final char[] content, int from) throws SyntaxException {
		final List<Lexema>			result = new  ArrayList<>();
		final long[]				forValues = new long[1];
		final GrowableCharArray<?>	forString = new GrowableCharArray<>(false);
		
loop:	while (content[from] != END_OF_QUERY) {
			from = CharUtils.skipBlank(content, from, false);
			switch (content[from]) {
				case '~' : 
					result.add(new Lexema(from++, LexType.LIKE)); 
					break;
				case ',' : 
					result.add(new Lexema(from++, LexType.COMMA)); 
					break;
				case '(' : 
					result.add(new Lexema(from++, LexType.OPENB)); 
					break;
				case ')' : 
					result.add(new Lexema(from++, LexType.CLOSEB)); 
					break;
				case '+' : 
					result.add(new Lexema(from++, LexType.ADD)); 
					break;
				case '-' : 
					result.add(new Lexema(from++, LexType.SUB)); 
					break;
				case '>' : 
					if (content[from+1] == '=') {
						result.add(new Lexema(from, LexType.GE));
						from += 2;
					}
					else {
						result.add(new Lexema(from++, LexType.GT));
					}
					break;
				case '<' :
					if (content[from+1] == '=') {
						result.add(new Lexema(from, LexType.LE));
						from += 2;
					}
					else {
						result.add(new Lexema(from++, LexType.LT));
					}
					break;
				case '.' :
					if (content[from+1] == '.') {
						result.add(new Lexema(from, LexType.RANGE));
						from += 2;
					}
					else {
						result.add(new Lexema(from++, LexType.DOT));
					}
					break;
				case '=' :
					if (content[from+1] == '=') {
						result.add(new Lexema(from, LexType.EQ));
						from += 2;
					}
					else {
						throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Unknonw lexema");
					}
					break;
				case '!' :
					if (content[from+1] == '=') {
						result.add(new Lexema(from, LexType.NE));
						from += 2;
					}
					else {
						throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Unknonw lexema");
					}
					break;
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					from = CharUtils.parseLong(content, from, forValues, true);
					result.add(new Lexema(from, LexType.NUMBER, forValues[0]));
					break;
				case '\"' :
					from = CharUtils.parseString(content, from + 1, '\"', forString);
					result.add(new Lexema(from, LexType.STRING, forString.extract()));
					forString.length(0);
					break;
				default :
					if (content[from] == END_OF_QUERY) {
						break loop;
					}
					else if (Character.isJavaIdentifierStart(content[from])) {
						final int	start = from;
						
						while (Character.isJavaIdentifierPart(content[from])) {
							from++;
						}
						final long		nameId = PREDEFINED_NAMES.seekName(content, start, from);
						
						if (nameId  < 0) {
							throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Unknonw predefined name");
						}
						else {
							result.add(new Lexema(from, PREDEFINED_NAMES.getCargo(nameId)));
						}
					}
					else {
						throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Unknonw lexema");
					}
			}
		}
		result.add(new Lexema(from, LexType.EOF));
		return result.toArray(new Lexema[result.size()]);
	}

	static int translateQuery(final Priority prty, final Lexema[] content, final int from, final Hashtable<String, Object> attributes, final SyntaxNode<NodeType, SyntaxNode> node) throws SyntaxException {
		int	pos = from;
		
		switch (prty) {
			case OR		:
				pos = translateQuery(Priority.AND, content, pos, attributes, node);
				if (content[pos].type.getGroup() == LexGroup.OR_GROUP) {
					final List<SyntaxNode<NodeType, SyntaxNode>>	orList = new ArrayList<>();
					
					orList.add((SyntaxNode<NodeType, SyntaxNode>) node.clone());
					do {final SyntaxNode<NodeType, SyntaxNode> 		current = (SyntaxNode<NodeType, SyntaxNode>) node.clone();

						current.col = content[pos].position; 
						pos = translateQuery(Priority.AND, content, pos + 1, attributes, current);
						orList.add((SyntaxNode<NodeType, SyntaxNode>) current);
					} while (content[pos].type.getGroup() == LexGroup.OR_GROUP);
					node.type = NodeType.OR;
					node.children = orList.toArray(new SyntaxNode[orList.size()]);
				}
				return pos;
			case AND	:
				pos = translateQuery(Priority.NOT, content, pos, attributes, node);
				if (content[pos].type.getGroup() == LexGroup.AND_GROUP) {
					final List<SyntaxNode<NodeType, SyntaxNode>>	andList = new ArrayList<>();
					
					andList.add((SyntaxNode<NodeType, SyntaxNode>) node.clone());
					do {final SyntaxNode<NodeType, SyntaxNode> 		current = (SyntaxNode<NodeType, SyntaxNode>) node.clone();

						current.col = content[pos].position; 
						pos = translateQuery(Priority.NOT, content, pos + 1, attributes, current);
						andList.add((SyntaxNode<NodeType, SyntaxNode>) current);
					} while (content[pos].type.getGroup() == LexGroup.AND_GROUP);
					node.type = NodeType.AND;
					node.children = andList.toArray(new SyntaxNode[andList.size()]);
				}
				return pos;
			case NOT	:
				if (content[pos].type.getGroup() == LexGroup.NOT_GROUP) {
					final SyntaxNode<NodeType, SyntaxNode>	nested = (SyntaxNode<NodeType, SyntaxNode>) node.clone();
					
					node.type = NodeType.NOT;
					node.children = new SyntaxNode[]{nested};
					return translateQuery(Priority.COMP, content, pos + 1, attributes, nested);
				}
				else {
					return translateQuery(Priority.COMP, content, pos, attributes, node);
				}
			case COMP	:
				pos = translateQuery(Priority.ADD, content, pos, attributes, node);
				if (content[pos].type.getGroup() == LexGroup.COMPARE_GROUP) {
					final SyntaxNode<NodeType, SyntaxNode> 	left = (SyntaxNode<NodeType, SyntaxNode>) node.clone();
					
					switch (content[pos].type) {
						case LIKE : case EQ : case NE : case GT : case GE : case LT : case LE :
							final SyntaxNode<NodeType, SyntaxNode> 	right = (SyntaxNode<NodeType, SyntaxNode>) node.clone();
							
							node.type = NodeType.COMPARISON;
							node.cargo = content[pos].type; 
							pos = translateQuery(Priority.ADD, content, pos + 1, attributes, right);
							node.children = new SyntaxNode[] {left, right};
							break;
						case IN	:
							final List<SyntaxNode<NodeType, SyntaxNode>>	inList = new ArrayList<>();
							
							inList.add(left);
							do {final SyntaxNode<NodeType, SyntaxNode> 	item = (SyntaxNode<NodeType, SyntaxNode>) node.clone();
							
								pos = translateQuery(Priority.ADD, content, pos + 1, attributes, item);
								if (content[pos].type == LexType.RANGE) {
									final SyntaxNode<NodeType, SyntaxNode> 	leftItem = (SyntaxNode<NodeType, SyntaxNode>) item.clone();
									final SyntaxNode<NodeType, SyntaxNode> 	rightItem = (SyntaxNode<NodeType, SyntaxNode>) item.clone();
									
									pos = translateQuery(Priority.ADD, content, pos + 1, attributes, rightItem);
									item.type = NodeType.RANGE;
									item.children = new SyntaxNode[] {leftItem, rightItem};
								}
								inList.add(item);
							} while (content[pos].type == LexType.COMMA);
							node.type = NodeType.COMPARISON;
							node.cargo = LexType.IN;
							node.children = inList.toArray(new SyntaxNode[inList.size()]);
							break;
						default :
							throw new UnsupportedOperationException("Lexema type ["+content[pos].type+"] is not supported yet");
					}
				}
				return pos;
			case ADD	:
				pos = translateQuery(Priority.TERM, content, pos, attributes, node);
				if (content[pos].type.getGroup() == LexGroup.ADD_GROUP) {
					final List<SyntaxNode<NodeType, SyntaxNode>>	addList = new ArrayList<>();
					long		operation = 0;	// Positional bitmap: 0/1 - add/sub
					
					addList.add((SyntaxNode<NodeType, SyntaxNode>) node.clone());
					do {final SyntaxNode<NodeType, SyntaxNode> 			current = (SyntaxNode<NodeType, SyntaxNode>) node.clone();
						operation = operation << 1 | (content[pos].type == LexType.ADD ? 0 : 1);

						current.col = content[pos].position; 
						pos = translateQuery(Priority.TERM, content, pos + 1, attributes, current);
						addList.add((SyntaxNode<NodeType, SyntaxNode>) current);
					} while (content[pos].type.getGroup() == LexGroup.ADD_GROUP);
					node.type = NodeType.ADD;
					node.value = operation;
					node.children = addList.toArray(new SyntaxNode[addList.size()]);
				}
				return pos;
			case TERM	:
				switch (content[pos].type) {
					case APPEARS : case CONTAINS : case DISAPPEARS : case NOW : case EXISTS :
						final List<SyntaxNode<NodeType, SyntaxNode>>	parmList = new ArrayList<>();
						final LexType	func = content[pos].type; 

						if (content[pos+1].type == LexType.OPENB) {
							final int	minArg = content[pos].type.getMandatoryArgCount(), maxArg = content[pos].type.getArgCount();
							
							if (minArg == 0) {
								if (content[pos+2].type == LexType.CLOSEB) {
									node.type = NodeType.FUNCION;
									node.cargo = content[pos].type;
									node.children = new SyntaxNode[0];
									return pos + 3;
								}
							}
							final LexType	funcType = content[pos].type;
							
							pos++;
							do {final SyntaxNode<NodeType, SyntaxNode> 		parm = (SyntaxNode<NodeType, SyntaxNode>) node.clone();
								
								pos = translateQuery(Priority.OR, content, pos + 1, attributes, parm);
								parmList.add(parm);
							} while (content[pos].type == LexType.COMMA);
							
							if (content[pos].type == LexType.CLOSEB) {
								pos++;
							}
							else {
								throw new SyntaxException(0, content[pos].position, "missing ')'"); 
							}
							
							if (parmList.size() >= minArg && parmList.size() <= maxArg) {
								node.type = NodeType.FUNCION;
								node.cargo = funcType;
								node.children = parmList.toArray(new SyntaxNode[parmList.size()]);
								return pos;
							}
							else if (parmList.size() < minArg) {
								throw new SyntaxException(0, content[pos].position, "too few arguments for fuction"); 
							}
							else {
								throw new SyntaxException(0, content[pos].position, "too many arguments for fuction"); 
							}
						}
						else {
							throw new SyntaxException(0, content[pos].position, "missing '('"); 
						}
					case COMMIT :
						if (content[pos+1].type == LexType.DOT) {
							return translateCommitQuery(content, pos + 1, node);
						}
						else {
							node.type = NodeType.VAR;
							node.cargo = LexType.COMMIT;
							node.children = null;
							return pos + 1;
						}
					case FILE	:
						if (content[pos+1].type == LexType.DOT) {
							return translateFileQuery(content, pos + 1, node);
						}
						else {
							node.type = NodeType.VAR;
							node.cargo = LexType.FILE;
							node.children = null;
							return pos + 1;
						}
					case NUMBER	:
						node.type = NodeType.NUM_CONST;
						node.value = content[pos].value; 
						return pos + 1;
					case OPENB	:
						pos = translateQuery(Priority.OR, content, pos + 1, attributes, node);
						if (content[pos].type == LexType.CLOSEB) {
							return pos + 1;
						}
						else {
							throw new SyntaxException(0, content[pos].position, "missing ')'"); 
						}
					case STRING	:
						node.type = NodeType.STR_CONST;
						node.cargo = content[pos].string; 
						return pos + 1;
					default:
						throw new SyntaxException(0, content[pos].position, "missing operand"); 
				}
			default:
				throw new UnsupportedOperationException("Priority ["+prty+"] is not suported yet"); 
		}
	}

	
	Object testCommit(final CommitDescriptor desc, final SyntaxNode<NodeType, SyntaxNode> node) {
		// TODO Auto-generated method stub
		switch (node.getType()) {
			case OR			:
				for (SyntaxNode<NodeType, SyntaxNode> item : node.children) {
					if (((Boolean)testCommit(desc, item))) {
						return true;
					}
				}
				return false;
			case AND		:
				for (SyntaxNode<NodeType, SyntaxNode> item : node.children) {
					if (!((Boolean)testCommit(desc, item))) {
						return false;
					}
				}
				return true;
			case COMPARISON	:
				break;
			case NOT	:
				return !((Boolean)testCommit(desc, (SyntaxNode<NodeType, SyntaxNode>)node.cargo));
			case ADD		:
				final long	op = node.value; 
				long		result = 0;
				
				for (SyntaxNode<NodeType, SyntaxNode> item : node.children) {
					final Object add = testCommit(desc, item);
					
					if ((op & (1 << node.children.length)) != 0) {
						result -= ((Number)testCommit(desc, item)).longValue();
					}
					else {
						result += ((Number)testCommit(desc, item)).longValue();
					}
				}
				return result;
			case FUNCION	:
				break;
			case NUM_CONST	:
				return node.value;
			case STR_CONST	:
				return (String)node.cargo;
			case VAR		:
				break;
			default	:
				break;
		}
		return false;
	}
	
	Object testRepoItem(final RepoItemDescriptor desc, final SyntaxNode<NodeType, SyntaxNode> node) {
		// TODO Auto-generated method stub
		return false;
	}

	private static int translateCommitQuery(final Lexema[] content, final int from, final SyntaxNode<NodeType, SyntaxNode> node) throws SyntaxException {
		final List<SyntaxNode<NodeType, SyntaxNode>>	path = new ArrayList<>();
		boolean prevEnded = false;
		int	pos = from;

loop:	do {final SyntaxNode<NodeType, SyntaxNode> 		item = (SyntaxNode<NodeType, SyntaxNode>) node.clone();
			
			switch (content[++pos].type) {
				case AUTHOR : case COMMENT :
					item.type = NodeType.VAR;
					item.cargo = content[pos].type;
					path.add(item);
					prevEnded = true;
					pos++;
					break;
				case ID :
					item.type = NodeType.VAR;
					item.cargo = content[pos].type;
					path.add(item);
					pos++;
					break loop;
				case UPPERCASE : case LOWERCASE :
					item.type = NodeType.VAR;
					item.cargo = content[pos].type;
					path.add(item);
					pos++;
					break loop;
				case PREV : case NEXT :
					if (prevEnded) {
						throw new SyntaxException(0, content[pos].position, "prev/next after property name"); 
					}
					else {
						item.type = NodeType.VAR;
						item.cargo = content[pos].type;
						if (content[pos+1].type == LexType.OPENB) {
							final SyntaxNode<NodeType, SyntaxNode> 		parm = (SyntaxNode<NodeType, SyntaxNode>) node.clone();
							
							pos = translateQuery(Priority.ADD, content, from + 1, NULL_ANTTRIBUTES, parm);
							if (content[pos].type == LexType.CLOSEB) {
								pos++;
							}
							else {
								throw new SyntaxException(0, content[pos].position, "missing ')'"); 
							}
							item.children = new SyntaxNode[] {parm};
						}
						else {
							item.children = null;
						}
						path.add(item);
						pos++;
					}
			}
		} while (content[pos].type == LexType.DOT);
		
		node.type = NodeType.VAR;
		node.cargo = LexType.COMMIT;
		node.children = path.toArray(new SyntaxNode[path.size()]);
		return pos;
	}

	private static int translateFileQuery(final Lexema[] content, final int from, final SyntaxNode<NodeType, SyntaxNode> node) throws SyntaxException {
		final List<SyntaxNode<NodeType, SyntaxNode>>	path = new ArrayList<>();
		boolean prevEnded = false;
		int	pos = from;

loop:	do {final SyntaxNode<NodeType, SyntaxNode> 		item = (SyntaxNode<NodeType, SyntaxNode>) node.clone();
			
			switch (content[++pos].type) {
				case PATH : case CONTENT :
					item.type = NodeType.VAR;
					item.cargo = content[pos].type;
					path.add(item);
					prevEnded = true;
					pos++;
					break;
				case CHANGE : case TIMESTAMP : case PARSEABLE : case VERSION :
					item.type = NodeType.VAR;
					item.cargo = content[pos].type;
					path.add(item);
					pos++;
					break loop;
				case UPPERCASE : case LOWERCASE :
					item.type = NodeType.VAR;
					item.cargo = content[pos].type;
					path.add(item);
					pos++;
					break loop;
				case PREV : case NEXT :
					if (prevEnded) {
						throw new SyntaxException(0, content[pos].position, "prev/next after property name"); 
					}
					else {
						item.type = NodeType.VAR;
						item.cargo = content[pos].type;
						if (content[pos+1].type == LexType.OPENB) {
							final SyntaxNode<NodeType, SyntaxNode> 		parm = (SyntaxNode<NodeType, SyntaxNode>) node.clone();
							
							pos = translateQuery(Priority.ADD, content, from + 1, NULL_ANTTRIBUTES, parm);
							if (content[pos].type == LexType.CLOSEB) {
								pos++;
							}
							else {
								throw new SyntaxException(0, content[pos].position, "missing ')'"); 
							}
							item.children = new SyntaxNode[] {parm};
						}
						else {
							item.children = null;
						}
						path.add(item);
						pos++;
					}
			}
		} while (content[pos].type == LexType.DOT);
		
		node.type = NodeType.VAR;
		node.cargo = LexType.FILE;
		node.children = path.toArray(new SyntaxNode[path.size()]);
		return pos;
	}
	
	private Iterable<RepoItemDescriptor> getItems(final RepoItemDescriptor[] desc) {
		if (hasRepoItems() && hasExplicitRepoItems()) {
			final Pattern	p = Pattern.compile(getRepoItemsAwaited());
			
			return new Iterable<RepoItemDescriptor>() {
				int	index = -1;
				
				@Override
				public Iterator<RepoItemDescriptor> iterator() {
					return new Iterator<RepoItemDescriptor>() {
						@Override
						public boolean hasNext() {
							while (++index < desc.length) {
								if (p.matcher(desc[index].getPath()).matches()) {
									index--;
									return true;
								}
							}
							return false;
						}

						@Override
						public RepoItemDescriptor next() {
							return desc[++index];
						}
					};
				}
			};
		}
		else {
			return Arrays.asList(desc);
		}
	}
	
	static void walkDNF(final SyntaxNode<NodeType, SyntaxNode> node, final boolean notNodeDetected, final List<List<SyntaxNode<NodeType, SyntaxNode>>> dnfCollection) {
		switch (node.getType()) {
			case OR		:
				if (notNodeDetected) {
					walkAndDNF(node, notNodeDetected, dnfCollection);
				}
				else {
					walkOrDNF(node, notNodeDetected, dnfCollection);
				}
				break;
			case AND	:
				if (notNodeDetected) {
					walkOrDNF(node, notNodeDetected, dnfCollection);
				}
				else {
					walkAndDNF(node, notNodeDetected, dnfCollection);
				}
				break;
			case NOT 	: 
				walkDNF(node, !notNodeDetected, dnfCollection);
				break;
			case COMPARISON : 
				for (List<SyntaxNode<NodeType, SyntaxNode>> item : dnfCollection) {
					if (notNodeDetected) {
						final SyntaxNode<NodeType, SyntaxNode>	temp =  (SyntaxNode<NodeType, SyntaxNode>) node.clone();
						
						switch ((LexType)node.cargo) {
							case EQ		:
								temp.cargo = LexType.NE;
								break;
							case GE		:
								temp.cargo = LexType.LT;
								break;
							case GT		:
								temp.cargo = LexType.LE;
								break;
							case LE		:
								temp.cargo = LexType.GT;
								break;
							case LT		:
								temp.cargo = LexType.GE;
								break;
							case NE		:
								temp.cargo = LexType.EQ;
								break;
							default:
								temp.type = NodeType.NOT;
								temp.cargo = node;
								break;
						}
						item.add(temp);
					}
					else {
						item.add(node);
					}
				}
				break;
			case FUNCION : 
				for (List<SyntaxNode<NodeType, SyntaxNode>> item : dnfCollection) {
					if (notNodeDetected) {
						final SyntaxNode<NodeType, SyntaxNode>	notItem =  (SyntaxNode<NodeType, SyntaxNode>) node.clone();
						
						notItem.type = NodeType.NOT;
						notItem.cargo = node;
						item.add(notItem);
					}
					else {
						item.add(node);
					}
				}
				break;
			case LIST : case NUM_CONST : case RANGE : case STR_CONST : case VAR : case ADD :
				throw new IllegalArgumentException();
			default :
				throw new UnsupportedOperationException("Node type ["+node.getType()+"] is not supported yet");
		}
	}

	static String collectExplicitPattern(final List<List<SyntaxNode<NodeType, SyntaxNode>>> content, final LexType var, final LexType field) {
		if (content.size() == 1) {
			return collectExplicitPatternInternal(content.get(0), var, field);
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			String				prefix = "(";
			
			for (List<SyntaxNode<NodeType, SyntaxNode>> item : content) {
				sb.append(prefix).append(collectExplicitPatternInternal(item, var, field));
				prefix = ",";
			}
			return sb.append(")").toString();
		}
	}
	
	private static String collectExplicitPatternInternal(final List<SyntaxNode<NodeType, SyntaxNode>> list, final LexType var, final LexType field) {
		final StringBuilder	sb = new StringBuilder();
		String		prefix = "(";

		for (SyntaxNode<NodeType, SyntaxNode> item : list) {
			if (canApplyPatternFor(item) && isPureFieldUsed(item.children[0], var, field)) {
				sb.append(prefix).append(extractPattern(item));
				prefix = ",";
			}			
		}
		return sb.append(")").toString();
	}

	private static void walkAndDNF(final SyntaxNode<NodeType, SyntaxNode> node, final boolean notDetected, final List<List<SyntaxNode<NodeType, SyntaxNode>>> dnfCollection) {
		for (SyntaxNode item : node.children) {
			walkDNF(item, notDetected, dnfCollection);
		}
	}	

	private static void walkOrDNF(final SyntaxNode<NodeType, SyntaxNode> node, final boolean notDetected, final List<List<SyntaxNode<NodeType, SyntaxNode>>> dnfCollection) {
		final List<SyntaxNode>[]	content = dnfCollection.toArray(new List[dnfCollection.size()]);
		final List<List<SyntaxNode<NodeType, SyntaxNode>>>	result = new ArrayList<>();

		for(int index = 0; index < node.children.length; index++) {
			final List<List<SyntaxNode<NodeType, SyntaxNode>>> temp = new ArrayList(Arrays.asList(content));
			
			walkDNF(node.children[index], notDetected, temp);
			result.addAll(temp);
		}
		dnfCollection.clear();
		dnfCollection.addAll(result);
	}
	
	private static boolean canApplyPatternFor(final SyntaxNode<NodeType, SyntaxNode> node) {
		if (node.getType() == NodeType.COMPARISON) {
			switch (((LexType)node.cargo)) {
				case EQ	: case IN : case NE : return true;
				default : return false;
			}
		}
		else {
			return false;
		}
	}
	
	private static boolean isPureFieldUsed(final SyntaxNode<NodeType, SyntaxNode> node, final LexType var, final LexType field) {
		if (node.getType() == NodeType.VAR) {
			if (node.cargo == var && node.children != null && node.children.length > 1) {
				return node.children[node.children.length - 1].cargo == field;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	private static String extractPattern(final SyntaxNode<NodeType, SyntaxNode> node) {
		// TODO Auto-generated method stub
		return null;
	}

	static class Lexema {
		private final int		position;
		private final LexType	type;
		private final long		value;
		private final char[]	string;

		private Lexema(final int position, final LexType type) {
			this(position,type, 0, null);
		}

		private Lexema(final int position, final LexType type, final long value) {
			this(position, type, value, null);
		}

		private Lexema(final int position, final LexType type, final char[] string) {
			this(position, type, 0, string);
		}
		
		private Lexema(final int position, final LexType type, final long value, final char[] string) {
			this.position = position;
			this.type = type;
			this.value = value;
			this.string = string;
		}

		LexType getType() {
			return type;
		}
		
		@Override
		public String toString() {
			return "Lexema [position=" + position + ", type=" + type + ", value=" + value + ", string=" + Arrays.toString(string) + "]";
		}
	}
}
