package chav1961.bt.lightrepo;

import chav1961.bt.lightrepo.AbstractLightRepo.LexType;
import chav1961.bt.lightrepo.AbstractLightRepo.NodeType;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.CommitDescriptor;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.RepoItemDescriptor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

import chav1961.bt.lightrepo.interfaces.LightRepoQueryInterface;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.cdb.SyntaxNodeUtils;
import chav1961.purelib.cdb.SyntaxNodeUtils.WalkCallback;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public class AbstractLightRepoQuery implements LightRepoQueryInterface {
	private final SyntaxNode<NodeType, SyntaxNode> 	root;
	private final boolean	hasCommits;
	private final boolean	hasRepoItems;
	
	public AbstractLightRepoQuery(final SyntaxNode<NodeType, SyntaxNode> root) {
		if (root == null) {
			throw new NullPointerException("Root node can't be null");
		}
		else {
			final boolean[]	wasVariables = {false, false}, wasExpressions = {false, false};
			
			SyntaxNodeUtils.walkDown(root, new WalkCallback<>() {
				@Override
				public ContinueMode process(final NodeEnterMode mode, final SyntaxNode node) {
					if (mode == NodeEnterMode.ENTER && node.getType() == NodeType.VAR) {
						if (node.cargo == LexType.COMMIT) {
							wasVariables[0] = true;
							return ContinueMode.SKIP_CHILDREN;
						}
						else if (node.cargo == LexType.FILE) {
							wasVariables[1] = true;
							return ContinueMode.SKIP_CHILDREN;
						}
						else {
							return ContinueMode.CONTINUE;
						}
					}
					else {
						return ContinueMode.CONTINUE;
					}
				}
			});
			
			this.root = root;
			this.hasCommits = wasVariables[0];
			this.hasRepoItems = wasVariables[1];
		}
	}
	
	@Override
	public boolean hasCommits() {
		return hasCommits;
	}

	@Override
	public boolean hasExplicitCommits() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasRepoItems() {
		return hasRepoItems;
	}

	@Override
	public boolean hasExplicitRepoItems() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getCommitsAwaited() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRepoItemsAwaited() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean testCommit(final CommitDescriptor desc) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean testRepoItem(final RepoItemDescriptor desc) {
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
	
	private void walkDNF(final SyntaxNode<NodeType, SyntaxNode> root, final WalkCallback callback) {
		
	}
}
