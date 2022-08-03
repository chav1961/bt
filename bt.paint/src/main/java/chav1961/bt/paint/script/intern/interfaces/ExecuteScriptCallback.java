package chav1961.bt.paint.script.intern.interfaces;

import chav1961.purelib.cdb.SyntaxNode;

@FunctionalInterface
public interface ExecuteScriptCallback {
	void process(int depth, SyntaxNode node) throws InterruptedException;
}