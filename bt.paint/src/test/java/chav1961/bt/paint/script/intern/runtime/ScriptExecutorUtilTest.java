package chav1961.bt.paint.script.intern.runtime;

import java.awt.Color;
import java.awt.Point;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.paint.control.Predefines;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.ColorWrapper;
import chav1961.bt.paint.script.interfaces.PointWrapper;
import chav1961.bt.paint.script.intern.interfaces.ExecuteScriptCallback;
import chav1961.bt.paint.script.intern.interfaces.PaintScriptListInterface;
import chav1961.bt.paint.script.intern.interfaces.PaintScriptMapInterface;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.SyntaxNodeType;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

public class ScriptExecutorUtilTest {
	
	@Test
	public void calculateExpressionTest() throws PaintScriptException, InterruptedException, SyntaxException {
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
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : int; begin i := 7&3|4^2 end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(5, ((Long)(((long[])stack.getVar(names.seekName("i")))[0])).longValue());
		
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : int; begin i := 2*9/3%3+1 end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(1, ((Long)(((long[])stack.getVar(names.seekName("i")))[0])).longValue());

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : real; begin i := 2*9/3%3+1 end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(1, ((Double)(((double[])stack.getVar(names.seekName("i")))[0])).doubleValue(), 0.0001);

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : real; begin i := 2.0*9/3%3+1 end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(1, ((Double)(((double[])stack.getVar(names.seekName("i")))[0])).doubleValue(), 0.0001);

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : int; begin i := 2.0*9/3%3+1 end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(1, ((Long)(((long[])stack.getVar(names.seekName("i")))[0])).longValue());
		
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : str; begin i := \"as\"-\"sa\"+\"?\" end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertArrayEquals("assa?".toCharArray(), (((char[][])stack.getVar(names.seekName("i")))[0]));

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : bool; begin i := false && true end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(false, ((Boolean)(((boolean[])stack.getVar(names.seekName("i")))[0])).booleanValue());

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : bool; begin i := true && false end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(false, ((Boolean)(((boolean[])stack.getVar(names.seekName("i")))[0])).booleanValue());
		
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : bool; begin i := true && true end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(true, ((Boolean)(((boolean[])stack.getVar(names.seekName("i")))[0])).booleanValue());
		
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : bool; begin i := true || false end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(true, ((Boolean)(((boolean[])stack.getVar(names.seekName("i")))[0])).booleanValue());

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : bool; begin i := false || true end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(true, ((Boolean)(((boolean[])stack.getVar(names.seekName("i")))[0])).booleanValue());
		
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : bool; begin i := false || false end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(false, ((Boolean)(((boolean[])stack.getVar(names.seekName("i")))[0])).booleanValue());

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : str; begin i := 2.0*9/3%3+1 end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertArrayEquals("1.0".toCharArray(), (((char[][])stack.getVar(names.seekName("i")))[0]));

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : bool; begin i := true && \"true\" end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(true, ((Boolean)(((boolean[])stack.getVar(names.seekName("i")))[0])).booleanValue());
	}


	@Test
	public void accessTest() throws PaintScriptException, InterruptedException, SyntaxException {
		final SyntaxTreeInterface	names = new AndOrTree();
		final Predefines			predef = new Predefines(new String[0]);
		final ExecuteScriptCallback	callback = (l,n)->{}; 
		LocalStack					stack = new LocalStack(names, predef, callback);
		Object						result;
		
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : array of int := array(20); begin i[0] := 10 end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(10, ((Long)(((PaintScriptListInterface[])stack.getVar(names.seekName("i")))[0].get(0))).longValue());

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : map of int := map(\"x\" : 20); begin i[\"x\"] := 10 end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(10, ((Long)(((PaintScriptMapInterface[])stack.getVar(names.seekName("i")))[0].get("x".toCharArray()))).longValue());

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : color := color(10,20,30), j : int; begin j := i.getRed() end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(10, (long)((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : color; begin i := color(10,20,30) end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(new Color(10,20,30), (Color)((ColorWrapper[])stack.getVar(names.seekName("i")))[0].getColor());

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : point := point(10,20), j : int; begin j := i.getX() end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(10, (long)((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : point := point(10,20), j : int; begin j := i.x end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(10, (long)((long[])stack.getVar(names.seekName("j")))[0]);
		
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : point; begin i := point(10,20) end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(new Point(10,20), (Point)((PointWrapper[])stack.getVar(names.seekName("i")))[0].getPoint());
	}
}
