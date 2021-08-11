package chav1961.bt.lightrepo.interfaces;

import chav1961.bt.lightrepo.interfaces.LightRepoInterface.CommitDescriptor;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.RepoItemDescriptor;

public interface LightRepoQueryInterface {
	boolean isCommitUsed();
	boolean isRepoItemUsed();
	boolean hasCommits();
	boolean hasExplicitCommits();
	boolean hasRepoItems();
	boolean hasExplicitRepoItems();
	String getCommitsAwaited();
	String getRepoItemsAwaited();
	boolean testCommit(CommitDescriptor desc);
	boolean testRepoItem(RepoItemDescriptor desc);
}
