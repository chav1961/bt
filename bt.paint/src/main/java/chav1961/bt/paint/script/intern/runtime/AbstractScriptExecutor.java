package chav1961.bt.paint.script.intern.runtime;

import java.lang.Thread.State;

import chav1961.bt.paint.control.Predefines;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.SyntaxNodeType;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;

public class AbstractScriptExecutor<T> implements ExecutionControl {
	public static enum Actions {
		CONTINUE,
	}

	private final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	root;
	private final SyntaxTreeInterface<T>	names;
	private final Predefines				predef;
	private final Thread					t = new Thread(()->execute());
	private volatile boolean				paused = false;
	
	protected AbstractScriptExecutor(final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> root, final SyntaxTreeInterface<T> names, final Predefines predef) {
		this.root = root;
		this.names = names;
		this.predef = predef;
	}
	
	protected Actions completed(final int row, final int col, final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> node) {
		return Actions.CONTINUE;
	}

	@Override
	public void start() throws RuntimeException {
		if (t.getState() == State.NEW) {
			t.setDaemon(true);
			t.setName("script-executor");
			t.start();
		}
		else {
			throw new IllegalStateException("Thread to start already terminated. Create new instance of the class ans use in instead"); 
		}
	}

	@Override
	public void suspend() throws RuntimeException {
		// TODO Auto-generated method stub
		if (!paused) {
			paused = true;
		}
		else {
			throw new IllegalStateException("Thread to suspend already suspended"); 
		}
	}

	@Override
	public void resume() throws Exception {
		// TODO Auto-generated method stub
		if (paused) {
			paused = false;
		}
		else {
			throw new IllegalStateException("Thread to resume already resumed"); 
		}
	}

	@Override
	public void stop() throws RuntimeException {
		if (t.getState() != State.TERMINATED) {
			t.interrupt();
		}
		else {
			throw new IllegalStateException("Thread to stop already terminated. Create new instance of the class ans use in instead"); 
		}
	}

	@Override
	public boolean isStarted() {
		return t.getState() != State.NEW && t.getState() != State.TERMINATED;
	}

	@Override
	public boolean isSuspended() {
		return paused;
	}
	
	private void execute() {
		try{execute(root,names,predef);
		} catch (PaintScriptException e) {
			e.printStackTrace();
		}
	}

	private void execute(final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> node, final SyntaxTreeInterface<T> names, final Predefines predef) throws PaintScriptException {
		// TODO Auto-generated method stub
	}
}
