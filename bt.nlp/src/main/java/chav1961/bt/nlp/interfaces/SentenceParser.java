package chav1961.bt.nlp.interfaces;

import chav1961.bt.nlp.MemberNode;
import chav1961.purelib.basic.exceptions.SyntaxException;

public interface SentenceParser {
	void parse(String content, MemberNode root) throws SyntaxException;

	int parse(char[] content, int from, MemberNode root) throws SyntaxException;
}
