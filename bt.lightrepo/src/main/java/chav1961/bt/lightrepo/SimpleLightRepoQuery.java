package chav1961.bt.lightrepo;

import chav1961.bt.lightrepo.interfaces.LightRepoInterface.CommitDescriptor;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.RepoItemDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import chav1961.bt.lightrepo.SimpleLightRepoQuery.ExpressionResult.ValueType;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface;
import chav1961.bt.lightrepo.interfaces.LightRepoQueryInterface;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.cdb.SyntaxNodeUtils;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.sql.SQLUtils;

public class SimpleLightRepoQuery implements LightRepoQueryInterface {
	public static final Hashtable<String, Object>		NULL_ATTRIBUTES = new Hashtable<>();
	
	static final char									END_OF_QUERY = '\uFFFF';
	private static final SyntaxTreeInterface<LexType>	PREDEFINED_NAMES = new AndOrTree<>();
	
	static {
		PREDEFINED_NAMES.placeName((CharSequence)"or", LexType.OR);
		PREDEFINED_NAMES.placeName((CharSequence)"and", LexType.AND);
		PREDEFINED_NAMES.placeName((CharSequence)"not", LexType.NOT);
		PREDEFINED_NAMES.placeName((CharSequence)"in", LexType.IN);
		
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.EXISTS_FUNC, LexType.EXISTS);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.CONTAINS_FUNC, LexType.CONTAINS);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.APPEARS_FUNC, LexType.APPEARS);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.DISAPPEARS_FUNC, LexType.DISAPPEARS);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.NOW_FUNC, LexType.NOW);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.CREATED_FUNC, LexType.CREATED);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.CHANGED_FUNC, LexType.CHANGED);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.RENAMED_FUNC, LexType.RENAMED);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.REMOVED_FUNC, LexType.REMOVED);
		
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.PREV_MOD, LexType.PREV);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.NEXT_MOD, LexType.NEXT);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.UPPERCASE_MOD, LexType.UPPERCASE);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.LOWERCASE_MOD, LexType.LOWERCASE);
		
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.FILE_VAR, LexType.FILE);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.COMMIT_VAR, LexType.COMMIT);
		
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.AUTHOR_F, LexType.AUTHOR);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.COMMENT_F, LexType.COMMENT);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.COMMIT_ID_F, LexType.ID);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.PATH_F, LexType.PATH);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.CHANGE_F, LexType.CHANGE);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.TIMESTAMP_F, LexType.TIMESTAMP);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.PARSEABLE_F, LexType.PARSEABLE);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.VERSION_F, LexType.VERSION);
		PREDEFINED_NAMES.placeName((CharSequence)LightRepoInterface.CONTENT_F, LexType.CONTENT);
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
		RANGE,
		COMPARISON,
		ADD,
		NUM_CONST, STR_CONST, VAR, FUNCION
	}
	
	static enum Priority {
		OR, AND, NOT, COMP, ADD, TERM
	}
	
	private final SyntaxNode<NodeType, SyntaxNode> 	root;
	private final boolean	commitsPresents, repoItemsPresents;
	private final boolean	hasCommits, hasRepoItems;
	private final boolean	hasExplicitCommits, hasExplicitRepoItems;
	private final String	commitsPattern, repoItemsPattern;
	
	private SimpleLightRepoQuery(final String query, final Hashtable<String, Object> attributes, final SyntaxNode<NodeType, SyntaxNode> root) {
		final List<SyntaxNode<NodeType, SyntaxNode>>	dnf = new ArrayList<>();
		boolean	pathUsed = false, commitUsed = false; 
		boolean	wasPath = false, wasCommit = false; 
		int		pathAmount = 0, commitAmount = 0; 
		
		SyntaxNodeUtils.buildDNF((SyntaxNode)root, NodeType.OR, NodeType.AND, NodeType.NOT, (mode, node) ->{
			if (mode == NodeEnterMode.ENTER) {
				dnf.add((SyntaxNode<NodeType, SyntaxNode>) node);
			}
			return ContinueMode.CONTINUE;
		});
		
		for (SyntaxNode<NodeType, SyntaxNode> item : dnf) {
			boolean	wasPathHere = false, wasCommitHere = false;
			
			for (SyntaxNode<NodeType, SyntaxNode> op : item.children) {
				if (isVarPresents(op, LexType.COMMIT)) {
					commitUsed = true;
				}
				if (isVarPresents(op, LexType.FILE)) {
					pathUsed = true;
				}
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
		this.commitsPresents = commitUsed;
		this.repoItemsPresents = pathUsed;
		this.hasCommits = wasCommit;
		this.hasRepoItems = wasPath;
		this.hasExplicitCommits = commitAmount == dnf.size();
		this.hasExplicitRepoItems = pathAmount == dnf.size();
		this.commitsPattern = hasExplicitCommits ? collectExplicitPattern(dnf, LexType.COMMIT, LexType.ID) : ".*";
		this.repoItemsPattern = hasExplicitRepoItems ? collectExplicitPattern(dnf, LexType.FILE, LexType.PATH) : ".*";
	}

	@Override
	public boolean isCommitUsed() {
		return commitsPresents;
	}

	@Override
	public boolean isRepoItemUsed() {
		return repoItemsPresents;
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
			try {final ExpressionResult	result = testCommit(desc,root);
			
				if (result.type == ValueType.BOOL) {
					return result.boolValue;
				}
				else {
					throw new ContentException("Expression result is not a boolean value");
				}
			} catch (ContentException | IOException e) {
				return false; 
			}
		}
	}

	@Override
	public boolean testRepoItem(final RepoItemDescriptor desc) {
		if (desc== null) {
			throw new NullPointerException("Repo item descriptor can't be null");
		}
		else {
			try {final ExpressionResult	result = testRepoItem(desc,root);
			
				if (result.type == ValueType.BOOL) {
					return result.boolValue;
				}
				else {
					throw new ContentException("Expression result is not a boolean value");
				}
			} catch (ContentException | IOException e) {
				return false; 
			}
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
							throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Unknonw predefined name ["+new String(content, start, from - start)+"]");
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
	
	static ExpressionResult testCommit(final CommitDescriptor desc, final SyntaxNode<NodeType, SyntaxNode> node) throws ContentException, NullPointerException, IOException {
		switch (node.getType()) {
			case OR			:
				for (SyntaxNode<NodeType, SyntaxNode> item : node.children) {
					final ExpressionResult	 result = testCommit(desc, item);
					
					if (result.type == ValueType.UNKNOWN) {
						return result;
					}
					else if (result.type == ValueType.BOOL && result.boolValue) {
						return new ExpressionResult(true);
					}
				}
				return new ExpressionResult(false);
			case AND		:
				for (SyntaxNode<NodeType, SyntaxNode> item : node.children) {
					final ExpressionResult	 result = testCommit(desc, item);
					
					if (result.type == ValueType.UNKNOWN) {
						return result;
					}
					else if (result.type == ValueType.BOOL) {
						if (!result.boolValue) {
							return new ExpressionResult(false);
						}
					}
					else {
						return new ExpressionResult(false);
					}
				}
				return new ExpressionResult(true);
			case NOT	:
				final ExpressionResult	 notResult = testCommit(desc, (SyntaxNode<NodeType, SyntaxNode>)node.cargo);
				
				if (notResult.type == ValueType.UNKNOWN) {
					return notResult;
				}
				else if (notResult.type == ValueType.BOOL) {
					return new ExpressionResult(!notResult.boolValue);
				}
				else {
					return new ExpressionResult(false);
				}
			case COMPARISON	:
				final ExpressionResult left = testCommit(desc, node.children[0]);
				
				if (left.type == ValueType.UNKNOWN) {
					return left;
				}
				else {
					switch ((LexType)node.cargo) {
						case LIKE	: return new ExpressionResult(ExpressionResult.convertTo(left, ValueType.STR).strValue.matches(ExpressionResult.convertTo(testCommit(desc, node.children[1]), ValueType.STR).strValue));
						case EQ		: return new ExpressionResult(left.equals(ExpressionResult.convertTo(testCommit(desc, node.children[1]), left.type)));
						case NE		: return new ExpressionResult(!left.equals(ExpressionResult.convertTo(testCommit(desc, node.children[1]), left.type)));
						case GT		: return new ExpressionResult(left.compareTo(ExpressionResult.convertTo(testCommit(desc, node.children[1]), left.type)) > 0);
						case GE		: return new ExpressionResult(left.compareTo(ExpressionResult.convertTo(testCommit(desc, node.children[1]), left.type)) >= 0);
						case LT		: return new ExpressionResult(left.compareTo(ExpressionResult.convertTo(testCommit(desc, node.children[1]), left.type)) < 0);
						case LE		: return new ExpressionResult(left.compareTo(ExpressionResult.convertTo(testCommit(desc, node.children[1]), left.type)) <= 0);
						case IN		:
							for (int index = 1; index < node.children.length; index++) {
								if (node.children[index].getType() == NodeType.RANGE) {
									final ExpressionResult min = ExpressionResult.convertTo(testCommit(desc, node.children[index].children[0]), left.type);
									final ExpressionResult max = ExpressionResult.convertTo(testCommit(desc, node.children[index].children[1]), left.type);
									
									if (left.compareTo(min) >= 0 && left.compareTo(max) <= 0) {
										return new ExpressionResult(true);
									}
								}
								else {
									final ExpressionResult right = ExpressionResult.convertTo(testCommit(desc, node.children[index]), left.type);
									
									if (left.equals(right)) {
										return new ExpressionResult(true);
									}
								}
							}
							return new ExpressionResult(false);
						default : throw new UnsupportedOperationException();
					}
				}
			case ADD		:
				final long	op = node.value; 
				long		result = 0;
				
				for (SyntaxNode<NodeType, SyntaxNode> item : node.children) {
					ExpressionResult add = testCommit(desc, item);
					
					if (add.type == ValueType.UNKNOWN) {
						return add;
					}
					else if (add.type != ValueType.INT) {
						add = ExpressionResult.convertTo(add, ValueType.INT);
					}
					if ((op & (1 << node.children.length)) != 0) {
						result -= add.intValue;
					}
					else {
						result += add.intValue;
					}
				}
				return new ExpressionResult(result);
			case FUNCION	:
				throw new UnsupportedOperationException();
			case NUM_CONST	:
				return new ExpressionResult(node.value);
			case STR_CONST	:
				return new ExpressionResult(new String((char[])node.cargo));
			case VAR		:
				return extractVarValue(desc, node);
			default	:
				throw new UnsupportedOperationException();
		}
	}
	
	static ExpressionResult testRepoItem(final RepoItemDescriptor desc, final SyntaxNode<NodeType, SyntaxNode> node) throws ContentException, NullPointerException, IOException {
		switch (node.getType()) {
			case OR			:
				for (SyntaxNode<NodeType, SyntaxNode> item : node.children) {
					final ExpressionResult	 result = testRepoItem(desc, item);

					if (result.type == ValueType.UNKNOWN) {
						return result;
					}
					else if (result.type == ValueType.BOOL && result.boolValue) {
						return new ExpressionResult(true);
					}
				}
				return new ExpressionResult(false);
			case AND		:
				for (SyntaxNode<NodeType, SyntaxNode> item : node.children) {
					final ExpressionResult	 result = testRepoItem(desc, item);
					
					if (result.type == ValueType.UNKNOWN) {
						return result;
					}
					else if (result.type == ValueType.BOOL) {
						if (!result.boolValue) {
							return new ExpressionResult(false);
						}
					}
					else {
						return new ExpressionResult(false);
					}
				}
				return new ExpressionResult(true);
			case NOT	:
				final ExpressionResult	 notResult = testRepoItem(desc, (SyntaxNode<NodeType, SyntaxNode>)node.cargo);
				
				if (notResult.type == ValueType.UNKNOWN) {
					return notResult;
				}
				else if (notResult.type == ValueType.BOOL) {
					return new ExpressionResult(!notResult.boolValue);
				}
				else {
					return new ExpressionResult(false);
				}
			case COMPARISON	:
				final ExpressionResult left = testRepoItem(desc, node.children[0]);
				
				if (left.type == ValueType.UNKNOWN) {
					return left;
				}
				else {
					switch ((LexType)node.cargo) {
						case LIKE	: return new ExpressionResult(ExpressionResult.convertTo(left, ValueType.STR).strValue.matches(ExpressionResult.convertTo(testRepoItem(desc, node.children[1]), ValueType.STR).strValue));
						case EQ		: return new ExpressionResult(left.equals(ExpressionResult.convertTo(testRepoItem(desc, node.children[1]), left.type)));
						case NE		: return new ExpressionResult(!left.equals(ExpressionResult.convertTo(testRepoItem(desc, node.children[1]), left.type)));
						case GT		: return new ExpressionResult(left.compareTo(ExpressionResult.convertTo(testRepoItem(desc, node.children[1]), left.type)) > 0);
						case GE		: return new ExpressionResult(left.compareTo(ExpressionResult.convertTo(testRepoItem(desc, node.children[1]), left.type)) >= 0);
						case LT		: return new ExpressionResult(left.compareTo(ExpressionResult.convertTo(testRepoItem(desc, node.children[1]), left.type)) < 0);
						case LE		: return new ExpressionResult(left.compareTo(ExpressionResult.convertTo(testRepoItem(desc, node.children[1]), left.type)) <= 0);
						case IN		:
							for (int index = 1; index < node.children.length; index++) {
								if (node.children[index].getType() == NodeType.RANGE) {
									final ExpressionResult min = ExpressionResult.convertTo(testRepoItem(desc, node.children[index].children[0]), left.type);
									final ExpressionResult max = ExpressionResult.convertTo(testRepoItem(desc, node.children[index].children[1]), left.type);
									
									if (left.compareTo(min) >= 0 && left.compareTo(max) <= 0) {
										return new ExpressionResult(true);
									}
								}
								else {
									final ExpressionResult right = ExpressionResult.convertTo(testRepoItem(desc, node.children[index]), left.type);
									
									if (left.equals(right)) {
										return new ExpressionResult(true);
									}
								}
							}
							return new ExpressionResult(false);
						default : throw new UnsupportedOperationException();
					}
				}
			case ADD		:
				final long	op = node.value; 
				long		result = 0;
				
				for (SyntaxNode<NodeType, SyntaxNode> item : node.children) {
					ExpressionResult add = testRepoItem(desc, item);
					
					if (add.type == ValueType.UNKNOWN) {
						return add;
					}
					else if (add.type != ValueType.INT) {
						add = ExpressionResult.convertTo(add, ValueType.INT);
					}
					if ((op & (1 << node.children.length)) != 0) {
						result -= add.intValue;
					}
					else {
						result += add.intValue;
					}
				}
				return new ExpressionResult(result);
			case FUNCION	:
				throw new UnsupportedOperationException();
			case NUM_CONST	:
				return new ExpressionResult(node.value);
			case STR_CONST	:
				return new ExpressionResult(new String((char[])node.cargo));
			case VAR		:
				return extractVarValue(desc, node);
			default	:
				throw new UnsupportedOperationException();
		}
	}

	static ExpressionResult extractVarValue(final CommitDescriptor desc, final SyntaxNode<NodeType, SyntaxNode> node) throws ContentException, IOException {
		if ((LexType)node.cargo == LexType.FILE) {
			throw new ContentException();
		}
		else {
			ExpressionResult	result = null;
			CommitDescriptor	temp = desc;
			
			for (int index = 0; index < node.children.length; index++) {
				switch ((LexType)node.children[index].cargo) {
					case PREV		:
						final ExpressionResult	prevStep = node.children[index].children != null ? testCommit(desc, node.children[index].children[0]) : new ExpressionResult(1);
						
						if (prevStep.type != ValueType.INT) {
							return new ExpressionResult();
						}
						else {
							for (int step = 0; temp != null && step < prevStep.intValue; step++) {
								temp = temp.getPrevious();
							}
							if (temp == null) {
								return new ExpressionResult();
							}
						}
						break;
					case NEXT		:
						final ExpressionResult	nextStep = node.children[index].children != null ? testCommit(desc, node.children[index].children[0]) : new ExpressionResult(1);
						
						if (nextStep.type != ValueType.INT) {
							return new ExpressionResult();
						}
						else {
							for (int step = 0; temp != null && step < nextStep.intValue; step++) {
								temp = temp.getNext();
							}
							if (temp == null) {
								return new ExpressionResult();
							}
						}
						break;
					case AUTHOR 	:
						result = new ExpressionResult(temp.getAuthor());
						break;
					case COMMENT	:
						result = new ExpressionResult(temp.getComment());
						break;
					case ID			:
						result = new ExpressionResult(temp.getCommitId().toString());
						break;
					case TIMESTAMP	:
						result = new ExpressionResult(temp.getTimestamp().getTime());
						break;
					case UPPERCASE	:
						if (result != null) {
							result = new ExpressionResult(ExpressionResult.convertTo(result, ValueType.STR).strValue.toUpperCase());
						}
						else {
							throw new ContentException(); 
						}
						break;
					case LOWERCASE	:
						if (result != null) {
							result = new ExpressionResult(ExpressionResult.convertTo(result, ValueType.STR).strValue.toLowerCase());
						}
						else {
							throw new ContentException(); 
						}
						break;
					default : throw new IllegalArgumentException();
				}
			}
			return result;
		}
	}
	
	static ExpressionResult extractVarValue(final RepoItemDescriptor desc, final SyntaxNode<NodeType, SyntaxNode> node) throws ContentException, IOException {
		if ((LexType)node.cargo == LexType.COMMIT) {
			return extractVarValue(desc.getCommit(), node);
		}
		else {
			ExpressionResult	result = null;
			RepoItemDescriptor	temp = desc;
			
			for (int index = 0; index < node.children.length; index++) {
				switch ((LexType)node.children[index].cargo) {
					case PREV		:
						final ExpressionResult	prevStep = node.children[index].children != null ? testRepoItem(desc, node.children[index].children[0]) : new ExpressionResult(1);
						
						if (prevStep.type != ValueType.INT) {
							return new ExpressionResult();
						}
						else {
							for (int step = 0; temp != null && step < prevStep.intValue; step++) {
								temp = temp.getPrevious();
							}
							if (temp == null) {
								return new ExpressionResult();
							}
						}
						break;
					case NEXT		:
						final ExpressionResult	nextStep = node.children[index].children != null ? testRepoItem(desc, node.children[index].children[0]) : new ExpressionResult(1);
						
						if (nextStep.type != ValueType.INT) {
							return new ExpressionResult();
						}
						else {
							for (int step = 0; temp != null && step < nextStep.intValue; step++) {
								temp = temp.getNext();
							}
							if (temp == null) {
								return new ExpressionResult();
							}
						}
						break;
					case AUTHOR 	:
						result = new ExpressionResult(temp.getAuthor());
						break;
					case COMMENT	:
						result = new ExpressionResult(temp.getComment());
						break;
					case ID			:
						result = new ExpressionResult(temp.getCommitId().toString());
						break;
					case PATH		:
						result = new ExpressionResult(temp.getPath());
						break;
					case TIMESTAMP	:
						result = new ExpressionResult(temp.getTimestamp().getTime());
						break;
					case CHANGE		:
						result = new ExpressionResult(temp.getChanges().toString());
						break;
					case PARSEABLE	:
						result = new ExpressionResult(false);
						break;
					case VERSION	:
						result = new ExpressionResult(temp.getVersion());
						break;
					case CONTENT	:
						result = new ExpressionResult("");
						break;
					case UPPERCASE	:
						if (result != null) {
							result = new ExpressionResult(ExpressionResult.convertTo(result, ValueType.STR).strValue.toUpperCase());
						}
						else {
							throw new ContentException(); 
						}
						break;
					case LOWERCASE	:
						if (result != null) {
							result = new ExpressionResult(ExpressionResult.convertTo(result, ValueType.STR).strValue.toLowerCase());
						}
						else {
							throw new ContentException(); 
						}
						break;
					default : throw new IllegalArgumentException();
				}
			}
			return result;
		}
	}

	static String collectExplicitPattern(final List<SyntaxNode<NodeType, SyntaxNode>> content, final LexType var, final LexType field) {
		if (content.size() == 1) {
			return collectExplicitPatternInternal(content.get(0), var, field);
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			String				prefix = "";
			
			for (SyntaxNode<NodeType, SyntaxNode> item : content) {
				sb.append(prefix).append(collectExplicitPatternInternal(item, var, field));
				prefix = "|";
			}
			return sb.toString();
		}
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
				case TIMESTAMP :
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
							
							pos = translateQuery(Priority.ADD, content, from + 1, NULL_ATTRIBUTES, parm);
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
					break;
				default :
					throw new SyntaxException(0, content[pos].position, "this predefined name doesn't support for current variable'"); 
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
							
							pos = translateQuery(Priority.ADD, content, from + 1, NULL_ATTRIBUTES, parm);
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
					break;
				default :
					throw new SyntaxException(0, content[pos].position, "this predefined name doesn't support for current variable'"); 
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
	
	private static String collectExplicitPatternInternal(final SyntaxNode<NodeType, SyntaxNode> list, final LexType var, final LexType field) {
		final StringBuilder	sb = new StringBuilder();
		String		prefix = "";

		for (SyntaxNode<NodeType, SyntaxNode> item : list.children) {
			if (canApplyPatternFor(item) && isPureFieldUsed(item.children[0], var, field)) {
				sb.append(prefix).append(extractPattern(item));
				prefix = "";
			}			
		}
		return sb.toString();
	}

	private static boolean canApplyPatternFor(final SyntaxNode<NodeType, SyntaxNode> node) {
		if (node.getType() == NodeType.COMPARISON) {
			switch (((LexType)node.cargo)) {
				case EQ	: case IN : case NE : case LIKE :
					for (int index = 1; index < node.children.length; index++) {
						if (!isConstantHere(node.children[index])) {
							return false;
						}
					}
					return true;
				default : 
					return false;
			}
		}
		else {
			return false;
		}
	}

	private boolean isVarPresents(final SyntaxNode<NodeType, SyntaxNode> node, final LexType var) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private static boolean isConstantHere(final SyntaxNode<NodeType, SyntaxNode> node) {
		return node.getType() == NodeType.NUM_CONST || node.getType() == NodeType.STR_CONST;
	}

	private static boolean isPureFieldUsed(final SyntaxNode<NodeType, SyntaxNode> node, final LexType var, final LexType field) {
		if (node.getType() == NodeType.VAR) {
			if (node.cargo == var && node.children != null && node.children.length >= 1) {
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
		switch (node.getType()) {
			case COMPARISON	:
				if (node.cargo == LexType.IN) {
					final StringBuilder	sb = new StringBuilder();
					String	prefix = "";
					
					for (int index = 1; index < node.children.length; index++) {
						sb.append(prefix).append(extractPattern(node.children[index]));
						prefix = "|";
					}
					return sb.toString();
				}
				else {
					return extractPattern(node.children[1]);
				}
			case NUM_CONST	:
				return String.valueOf(node.value);
			case STR_CONST	:
				return new String((char[])node.cargo);
			default : throw new IllegalArgumentException("Node type ["+node.getType()+"] is not available here");
		}
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
	
	static class ExpressionResult implements Comparable<ExpressionResult> {
		static enum ValueType {
			UNKNOWN(Object.class),
			INT(long.class), 
			STR(String.class),
			BOOL(boolean.class);
			
			private final Class<?>	valueClass;
			
			private ValueType(final Class<?> valueClass) {
				this.valueClass = valueClass;
			}
			
			public Class<?> getValueClass() {
				return valueClass;
			}
		}
		
		final ValueType	type;
		final long		intValue;
		final String	strValue;
		final boolean	boolValue;

		ExpressionResult(long intValue) {
			this(ValueType.INT, intValue, null, false);
		}		

		ExpressionResult(String strValue) {
			this(ValueType.STR, 0, strValue, false);
		}		
		
		ExpressionResult(boolean boolValue) {
			this(ValueType.BOOL, 0, null, boolValue);
		}		

		ExpressionResult() {
			this(ValueType.UNKNOWN, 0, null, false);
		}		
		
		private ExpressionResult(ValueType type, long intValue, String strValue, boolean boolValue) {
			this.type = type;
			this.intValue = intValue;
			this.strValue = strValue;
			this.boolValue = boolValue;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (boolValue ? 1231 : 1237);
			result = prime * result + (int) (intValue ^ (intValue >>> 32));
			result = prime * result + ((strValue == null) ? 0 : strValue.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ExpressionResult other = (ExpressionResult) obj;
			if (boolValue != other.boolValue) return false;
			if (intValue != other.intValue) return false;
			if (strValue == null) {
				if (other.strValue != null) return false;
			} else if (!strValue.equals(other.strValue)) return false;
			if (type != other.type) return false;
			return true;
		}

		@Override
		public int compareTo(final ExpressionResult o) {
			if (o != null &&  o.type == this.type) {
				switch (type) {
					case INT	:
						final long	diff = this.intValue - o.intValue;
						
						if (diff < 0) {
							return -1;
						}
						else if (diff > 0) {
							return 1;
						}
						else {
							return 0;
						}
					case STR	:
						return this.strValue.compareTo(o.strValue);
					case BOOL	:
						final int	other = o.boolValue ? 1 : 0, my = this.boolValue ? 1 : 0;
						
						return my - other;
					default : throw new UnsupportedOperationException("Value type ["+type+"]  is not supported yet"); 
				}
			}
			else {
				return 0;
			}
		}

		@Override
		public String toString() {
			return "ExpressionResult [type=" + type + ", intValue=" + intValue + ", strValue=" + strValue + ", boolValue=" + boolValue + "]";
		}

		public static ExpressionResult convertTo(final ExpressionResult item, final ValueType awaited) throws ContentException, NullPointerException {
			if (item == null) {
				throw new NullPointerException("Item to convert can't be null");
			}
			else if (awaited == null) {
				throw new NullPointerException("Awaited type can't be null");
			}
			else if (item.type == awaited) {
				return item;
			}
			else {
				final Object	result;
				
				switch (item.type) {
					case INT	: result = SQLUtils.convert(awaited.getValueClass(), item.intValue); break;
					case STR	: result = SQLUtils.convert(awaited.getValueClass(), item.strValue); break;
					case BOOL	: result = SQLUtils.convert(awaited.getValueClass(), item.boolValue); break;
					default : throw new UnsupportedOperationException("Value type ["+item.type+"]  is not supported yet"); 
				}
				for (ValueType valType : ValueType.values()) {
					if (CompilerUtils.toWrappedClass(valType.getValueClass()) == result.getClass()) {
						switch (valType) {
							case INT	: return new ExpressionResult((Long)result);
							case STR	: return new ExpressionResult((String)result);
							case BOOL	: return new ExpressionResult((Boolean)result);
							default : throw new UnsupportedOperationException("Value type ["+item.type+"]  is not supported yet"); 
						}
					}
				}
				throw new IllegalArgumentException("Result class ["+result.getClass().getCanonicalName()+"] is not compatible with value type ["+awaited+"]");
			}
		}
	}

}
