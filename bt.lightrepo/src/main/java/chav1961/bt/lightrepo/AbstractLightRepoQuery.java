package chav1961.bt.lightrepo;

import chav1961.bt.lightrepo.AbstractLightRepo.LexType;
import chav1961.bt.lightrepo.AbstractLightRepo.NodeType;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.CommitDescriptor;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.RepoItemDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import chav1961.bt.lightrepo.interfaces.LightRepoQueryInterface;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.cdb.SyntaxNodeUtils;
import chav1961.purelib.cdb.SyntaxNodeUtils.WalkCallback;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public class AbstractLightRepoQuery implements LightRepoQueryInterface {
	private final SyntaxNode<NodeType, SyntaxNode> 	root;
	private final boolean	hasCommits, hasRepoItems;
	private final boolean	hasExplicitCommits, hasExplicitRepoItems;
	private final String	commitsPattern, repoItemsPattern;
	
	public AbstractLightRepoQuery(final SyntaxNode<NodeType, SyntaxNode> root) {
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
}
