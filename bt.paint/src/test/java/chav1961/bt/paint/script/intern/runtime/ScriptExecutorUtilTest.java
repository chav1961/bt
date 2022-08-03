package chav1961.bt.paint.script.intern.runtime;

import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.paint.control.Predefines;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.intern.interfaces.ExecuteScriptCallback;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.SyntaxNodeType;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

public class ScriptExecutorUtilTest {
	
	@Test
	public void expressionTest() throws PaintScriptException, InterruptedException, SyntaxException {
		final SyntaxTreeInterface	names = new AndOrTree();
		final Predefines			predef = new Predefines(new String[0]);
		final ExecuteScriptCallback	callback = (l,n)->{}; 
		LocalStack					stack = new LocalStack(names, predef, callback);
		Object						result;
		
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : int; begin i := 10 end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(10, ((Long)(((long[])stack.getVar(names.seekName("i")))[0])).longValue());

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : int; begin i := -10 end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(-10, ((Long)(((long[])stack.getVar(names.seekName("i")))[0])).longValue());

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : int; begin i := ~1 end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(-2, ((Long)(((long[])stack.getVar(names.seekName("i")))[0])).longValue());

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : bool; begin i := !false end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(true, ((Boolean)(((boolean[])stack.getVar(names.seekName("i")))[0])).booleanValue());

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : int; begin i := 2*9/3%3 end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(2, ((Long)(((long[])stack.getVar(names.seekName("i")))[0])).longValue());
	}
}
