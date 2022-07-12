package chav1961.bt.paint.script;

import java.io.Closeable;
import java.util.Map;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;

public interface ScriptOwner<Node extends Enum<?>> extends ExecutionControl, Closeable, Appendable, CharSequence {
	void clear();
	SyntaxNode<Node, SyntaxNode<Node,?>> compile() throws SyntaxException;
	void execute(SyntaxNode<Node, SyntaxNode<Node,?>> root, Map<String,Object> vars) throws ContentException;
}
