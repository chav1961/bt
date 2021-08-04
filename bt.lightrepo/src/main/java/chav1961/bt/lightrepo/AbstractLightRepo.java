package chav1961.bt.lightrepo;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import chav1961.bt.lightrepo.interfaces.LightRepoInterface;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.ChangesDescriptor.ChangeType;
import chav1961.bt.lightrepo.interfaces.LightRepoQueryInterface;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.CharUtils.Prescription;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class AbstractLightRepo implements LightRepoInterface, Closeable {
	public static final String							CURRENT_PATH = "current";
	public static final String							DELTAS_PATH = "deltas";
	public static final String							COMMITS_PATH = "commits";
	
	private static final Hashtable<String, Object>		NULL_ANTTRIBUTES = new Hashtable<>();
	private static final SyntaxTreeInterface<LexType>	PREDEFINED_NAMES = new AndOrTree<>();
	private static final char							END_OF_QUERY = '\uFFFF';
	
	static {
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
		COMMA(LexGroup.UNKNOWN);
		
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
	
	private static enum Priority {
		OR, AND, NOT, COMP, ADD, TERM
	}
	
	private final FileSystemInterface			nested;
	private final InputStreamGetter				inGetter;
	private final OutputStreamGetter			outGetter;
	private final Map<UUID,TransalatedQuery>	queries = new ConcurrentHashMap<>(); 
	
	public AbstractLightRepo(final FileSystemInterface nested, final InputStreamGetter inGetter, final OutputStreamGetter outGetter) {
		if (nested == null) {
			throw new NullPointerException("Nested file system can't be null");
		}
		else if (inGetter == null) {
			throw new NullPointerException("Input stream getter can't be null");
		}
		else if (outGetter == null) {
			throw new NullPointerException("Output stream getter can't be null");
		}
		else {
			this.nested = nested;
			this.inGetter = inGetter;
			this.outGetter = outGetter;
		}
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Iterable<RepoItemDescriptor> queryForPath(final String query) throws SyntaxException, IOException {
		if (query == null || query.isEmpty()) {
			throw new IllegalArgumentException("Query string can't be null or empty");
		}
		else {
			final UUID	queryId = translateQuery(query, NULL_ANTTRIBUTES);
			
			try {
				return queryForPath(queryId);
			} finally {
				removeQuery(queryId);
			}
		}
	}

	@Override
	public Iterable<CommitDescriptor> queryForCommit(final String query) throws SyntaxException, IOException {
		if (query == null || query.isEmpty()) {
			throw new IllegalArgumentException("Query string can't be null or empty");
		}
		else {
			final UUID	queryId = translateQuery(query, NULL_ANTTRIBUTES);
			
			try {
				return queryForCommit(queryId);
			} finally {
				removeQuery(queryId);
			}
		}
	}

	@Override
	public UUID translateQuery(final String query, final Hashtable<String, Object> attributes) throws SyntaxException {
		if (query == null || query.isEmpty()) {
			throw new IllegalArgumentException("Query string can't be null or empty");
		}
		else if (attributes == null) {
			throw new NullPointerException("Attributes can't be null");
		}
		else {
			for (Entry<UUID, TransalatedQuery> item : queries.entrySet()) {
				if (item.getValue().getQueryString().equals(query)) {
					return item.getValue().getId();
				}
			}
			final SyntaxNode<NodeType, SyntaxNode>	root = new SyntaxNode<>(0, 0, NodeType.OR, 0, null);
			final char[]			content = CharUtils.terminateAndConvert2CharArray(query, END_OF_QUERY);
			final Lexema[]			lexList = parseQuery(content, 0);
			final int				pos = translateQuery(Priority.OR, lexList, 0, attributes, root);
			final TransalatedQuery	tq = new TransalatedQueryImpl(query, attributes, root);
			
			queries.put(tq.getId(), tq);
			return tq.getId();
		}
	}

	@Override
	public Iterable<TransalatedQuery> getQueriesTranslated() throws IOException {
		return queries.values();
	}

	@Override
	public Iterable<RepoItemDescriptor> queryForPath(final UUID query) throws SyntaxException, IOException {
		if (query == null) {
			throw new NullPointerException("Query ID can't be null");
		}
		else {
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Iterable<CommitDescriptor> queryForCommit(final UUID query) throws SyntaxException, IOException {
		if (query == null) {
			throw new NullPointerException("Query ID can't be null");
		}
		else {
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Iterable<ChangesDescriptor> calculateChanges(final String path, final long firstVersion, final long secondVersion) throws IOException {
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Path can't be null or empty");
		}
		else {
			return calculateChanges(getContent(path,firstVersion),getContent(path,secondVersion));
		}
	}

	@Override
	public FileSystemInterface getFileSystem() throws IOException {
		return nested;
	}

	@Override
	public TransactionDescriptor beginTransaction(final String author, final String comment) throws IOException {
		if (author == null || author.isEmpty()) {
			throw new IllegalArgumentException("Author can't be null or empty");
		}
		else if (comment == null || comment.isEmpty()) {
			throw new IllegalArgumentException("Comment can't be null or empty");
		}
		else {
			// TODO Auto-generated method stub
			
			return null;
		}
	}
	
	protected void removeQuery(final UUID queryId) {
		queries.remove(queryId);
	}

	protected Reader getContent(final String path, final long version) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	protected Iterable<ChangesDescriptor> calculateChanges(final Reader left, final Reader right) throws IOException {
		final GrowableCharArray<?>	gLeft = new GrowableCharArray<>(false), gRight = new GrowableCharArray<>(false);
		
		gLeft.append(left);
		gRight.append(right);
		
		final Prescription			pre = CharUtils.calcLevenstain(gLeft.extract(), gRight.extract());
		final ChangesDescriptor[]	desc = new ChangesDescriptor[pre.route.length];
		
		for (int index = 0, maxIndex = desc.length; index < maxIndex; index++) {
			switch (pre.route[index][0]) {
				case Prescription.LEV_INSERT 	:
					desc[index] = new ChangesDescriptorImpl(ChangeType.INSERTED, pre.route[index][1], "", pre.route[index][1], "");
					break;
				case Prescription.LEV_DELETE 	:
					desc[index] = new ChangesDescriptorImpl(ChangeType.REMOVED, pre.route[index][1], "", pre.route[index][1], "");
					break;
				case Prescription.LEV_REPLACE	:
					desc[index] = new ChangesDescriptorImpl(ChangeType.CHANGED, pre.route[index][1], "", pre.route[index][1], "");
					break;
			}
		}
		return Arrays.asList(desc);
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
						pos = translateQuery(Priority.AND, content, pos + 1, attributes, node);
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
						pos = translateQuery(Priority.NOT, content, pos + 1, attributes, node);
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
					node.cargo = nested;
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
							node.cargo = content[pos].type;
							node.children = inList.toArray(new SyntaxNode[inList.size()]);
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
						pos = translateQuery(Priority.TERM, content, pos + 1, attributes, node);
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

						do {final SyntaxNode<NodeType, SyntaxNode> 		parm = (SyntaxNode<NodeType, SyntaxNode>) node.clone();
							
							pos = translateQuery(Priority.OR, content, pos + 1, attributes, parm);
							parmList.add(parm);
						} while (content[pos].type == LexType.COMMA);
						break;
					case COMMIT :
						return translateCommitQuery(content, pos + 1, node);
					case FILE	:
						return translateFileQuery(content, pos + 1, node);
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
				break;
			default:
				break;
		}
		return 0;
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
					break;
				case ID :
					item.type = NodeType.VAR;
					item.cargo = content[pos].type;
					path.add(item);
					break loop;
				case UPPERCASE : case LOWERCASE :
					item.type = NodeType.VAR;
					item.cargo = content[pos].type;
					path.add(item);
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
					break;
				case CHANGE : case TIMESTAMP : case PARSEABLE : case VERSION :
					item.type = NodeType.VAR;
					item.cargo = content[pos].type;
					path.add(item);
					break loop;
				case UPPERCASE : case LOWERCASE :
					item.type = NodeType.VAR;
					item.cargo = content[pos].type;
					path.add(item);
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
					}
			}
		} while (content[pos].type == LexType.DOT);
		
		node.type = NodeType.VAR;
		node.cargo = LexType.COMMIT;
		node.children = path.toArray(new SyntaxNode[path.size()]);
		return pos;
	}

	private static class Lexema {
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

		@Override
		public String toString() {
			return "Lexema [position=" + position + ", type=" + type + ", value=" + value + ", string=" + Arrays.toString(string) + "]";
		}
	}
	
	private static class TransalatedQueryImpl implements TransalatedQuery {
		private final UUID								uniqueId = UUID.randomUUID();
		private final String							query;
		private final Hashtable<String, Object>			attributes;
		private final SyntaxNode<NodeType, SyntaxNode>	root;
		
		private TransalatedQueryImpl(final String query, final Hashtable<String, Object> attributes, final SyntaxNode<NodeType, SyntaxNode> root) {
			this.query = query;
			this.attributes = attributes;
			this.root = root;
		}

		@Override
		public UUID getId() {
			return uniqueId;
		}

		@Override
		public String getQueryString() {
			return query;
		}

		@Override
		public Hashtable<String, Object> getAttributes() {
			return attributes;
		}
		
		@Override
		public LightRepoQueryInterface getQuery() {
			// TODO Auto-generated method stub
			return null;
		}
		
		
		public SyntaxNode<NodeType, SyntaxNode> getRoot() {
			// TODO Auto-generated method stub
			return root;
		}

		@Override
		public String toString() {
			return "TransalatedQueryImpl [uniqueId=" + uniqueId + ", query=" + query + ", attributes=" + attributes + "]";
		}
	}
	
	private static class ChangesDescriptorImpl implements ChangesDescriptor {
		private final ChangeType	changeType;
		private final long			firstLine, secondLine;
		private final String		first, second;
		
		private ChangesDescriptorImpl(final ChangeType changeType, final long firstLine, final String first, final long secondLine, final String second) {
			this.changeType = changeType;
			this.firstLine = firstLine;
			this.first = first;
			this.secondLine = secondLine;
			this.second = second;
		}

		@Override
		public long getFirstLine() {
			return firstLine;
		}

		@Override
		public String getFirstLineContent() {
			return first;
		}

		@Override
		public long getSecondLine() {
			return secondLine;
		}

		@Override
		public String getSecondLineContent() {
			return second;
		}

		@Override
		public ChangeType getChangeType() {
			return changeType;
		}

		@Override
		public String toString() {
			return "ChangesDescriptorImpl [changeType=" + changeType + ", firstLine=" + firstLine + ", secondLine=" + secondLine + ", first=" + first + ", second=" + second + "]";
		}
	}
}
