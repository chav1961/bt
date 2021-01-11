package chav1961.bt.nlp;

import chav1961.bt.nlp.interfaces.SentenceMember;
import chav1961.bt.nlp.interfaces.SentenceMemberDescriptor;
import chav1961.purelib.cdb.SyntaxNode;

public class MemberNode extends SyntaxNode<SentenceMember, MemberNode>{
	public MemberNode(final int row, final int col, final SentenceMember type, final long value, final SentenceMemberDescriptor cargo, final MemberNode... children) throws NullPointerException {
		super(row, col, type, value, cargo, children);
	}

	public MemberNode(final SyntaxNode<SentenceMember, MemberNode> another) throws NullPointerException {
		super(another);
	}

	public SentenceMemberDescriptor getDescriptor() {
		return (SentenceMemberDescriptor) cargo;
	}
}
