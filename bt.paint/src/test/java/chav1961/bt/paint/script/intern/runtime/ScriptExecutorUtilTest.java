package chav1961.bt.paint.script.intern.runtime;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.paint.control.Predefines;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.ColorWrapper;
import chav1961.bt.paint.script.interfaces.FontWrapper;
import chav1961.bt.paint.script.interfaces.ImageWrapper;
import chav1961.bt.paint.script.interfaces.PointWrapper;
import chav1961.bt.paint.script.interfaces.RectWrapper;
import chav1961.bt.paint.script.interfaces.SizeWrapper;
import chav1961.bt.paint.script.interfaces.StrokeWrapper;
import chav1961.bt.paint.script.interfaces.TransformWrapper;
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

		// Array
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : array of int := array(20); begin i[0] := 10 end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(10, ((Long)(((PaintScriptListInterface[])stack.getVar(names.seekName("i")))[0].get(0))).longValue());

		// Map
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : map of int := map(\"x\" : 20); begin i[\"x\"] := 10 end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(10, ((Long)(((PaintScriptMapInterface[])stack.getVar(names.seekName("i")))[0].get("x".toCharArray()))).longValue());

		// Color
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : color := color(10,20,30), j : int; begin j := i.getRed() end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(10, (long)((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : color; begin i := color(10,20,30) end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(new Color(10,20,30), (Color)((ColorWrapper[])stack.getVar(names.seekName("i")))[0].getColor());

		// Font
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : font := font(\"Monospace\",12), j : int; begin j := i.getSize() end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(12, (long)((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : font; begin i := font(\"Monospace\",12) end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(new Font("Monospace",0,12), (Font)((FontWrapper[])stack.getVar(names.seekName("i")))[0].getFont());

		// Stroke
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : stroke := stroke(\"SOLID\",12,\"ROUND\",\"ROUND\"), j : int; begin j := i.getLineWidth() end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(12, (long)((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : stroke; begin i := stroke(\"SOLID\",12,\"ROUND\",\"ROUND\") end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(12, ((BasicStroke)((StrokeWrapper[])stack.getVar(names.seekName("i")))[0].getStroke()).getLineWidth(), 0.0001);

		// Transform
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : transform := transform(), j : bool; begin j := i.isIdentity() end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertTrue((boolean)((boolean[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : transform; begin i := transform(\"rotate(90)\") end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(8, ((AffineTransform)((TransformWrapper[])stack.getVar(names.seekName("i")))[0].getTransform()).getType());
		
		// Image
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : image := image(), j : int; begin j := i.getWidth() end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(1, (long)((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : image; begin i := image(10,10,\"INT_RGB\") end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(1, ((BufferedImage)((ImageWrapper[])stack.getVar(names.seekName("i")))[0].getImage()).getType());
		
		// Point
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

		// Size
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : size := size(10,20), j : int; begin j := i.getWidth() end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(10, (long)((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : size := size(10,20), j : int; begin j := i.width end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(10, (long)((long[])stack.getVar(names.seekName("j")))[0]);
		
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : size; begin i := size(10,20) end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(new Dimension(10,20), (Dimension)((SizeWrapper[])stack.getVar(names.seekName("i")))[0].getSize());
		
		// Rectangle
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : rect := rect(10,20,30,40), j : int; begin j := i.getX() end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(10, (long)((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : rect := rect(10,20,30,40), j : int; begin j := i.width end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(30, (long)((long[])stack.getVar(names.seekName("j")))[0]);
		
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : rect; begin i := rect(10,20,30,40) end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(new Rectangle(10,20,30,40), (Rectangle)((RectWrapper[])stack.getVar(names.seekName("i")))[0].getRect());
	}

	@Test
	public void methodCallTest() throws PaintScriptException, InterruptedException, SyntaxException {
		final SyntaxTreeInterface	names = new AndOrTree();
		final Predefines			predef = new Predefines(new String[0]);
		final ExecuteScriptCallback	callback = (l,n)->{}; 
		LocalStack					stack = new LocalStack(names, predef, callback);
		Object						result;

		// Method call
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : image := image(10,10,\"INT_RGB\"), j : int; begin j := i.getRGB(1,1) end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(0xFF000000, ((long[])stack.getVar(names.seekName("j")))[0]);

		// Method chain call
		names.clear();
		result = ScriptExecutorUtil.calc(ScriptParserUtil.parseScript(new StringReader("var i : image := image(10,10,\"INT_RGB\"), j : int; begin j := i.getSubimage(0,0,10,10).getRGB(1,1) end"), names), names, 
						stack = new LocalStack(names, predef, callback), predef, 0, callback);
		Assert.assertEquals(0xFF000000, ((long[])stack.getVar(names.seekName("j")))[0]);
	}

	@Test
	public void operatorTest() throws PaintScriptException, InterruptedException, SyntaxException {
		final SyntaxTreeInterface	names = new AndOrTree();
		final Predefines			predef = new Predefines(new String[0]);
		final ExecuteScriptCallback	callback = (l,n)->{}; 
		LocalStack					stack;
		Object						result;

		// If test
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int := 1, j : int; begin if i = 1 then j := 1 else j := -1 end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(1, ((long[])stack.getVar(names.seekName("j")))[0]);
		
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int := 0, j : int; begin if i = 1 then j := 1 else j := -1 end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(-1, ((long[])stack.getVar(names.seekName("j")))[0]);

		// while test
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := 10; begin while j >= 0 do j := j - 1 end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(-1, ((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := -10; begin while j >= 0 do j := j - 1 end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(-10, ((long[])stack.getVar(names.seekName("j")))[0]);
		
		// until test
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := 10; begin do j := j - 1 while j >= 0 end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(-1, ((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := -1; begin do j := j - 1 while j >= 0 end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(-2, ((long[])stack.getVar(names.seekName("j")))[0]);
		
		// For test
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := 0; begin for i := 1 to 10 step 2 do j := j + 1; end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(5, ((long[])stack.getVar(names.seekName("j")))[0]);
		
		// For1 test
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := 0; begin for i := 1 to 10 do j := j + 1; end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(10, ((long[])stack.getVar(names.seekName("j")))[0]);

		// Forall test
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := 0; begin for i in 1,2,3 do j := j + i end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(6, ((long[])stack.getVar(names.seekName("j")))[0]);
	
		// case test
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int := 1, j : int; begin case i of 1: j := 1 of 2: j := 2 else j := 3 end end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(1, ((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int := 2, j : int; begin case i of 1: j := 1 of 2: j := 2 else j := 3 end end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(2, ((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int := 3, j : int; begin case i of 1: j := 1 of 2: j := 2 else j := 3 end end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(3, ((long[])stack.getVar(names.seekName("j")))[0]);

		// Break test
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int := 10, j : int := 0; begin while i >= 0 do {j := j + 1; break} end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(1, ((long[])stack.getVar(names.seekName("j")))[0]);
		
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int := 10, j : int := 0; begin do {j := j + 1; break} while i >= 0 end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(1, ((long[])stack.getVar(names.seekName("j")))[0]);
		
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := 0; begin for i := 1 to 10 do {j := j + 1; break} end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(1, ((long[])stack.getVar(names.seekName("j")))[0]);
	
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := 0; begin for i := 1 to 10 step 1 do {j := j + 1; break} end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(1, ((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := 0; begin for i in 1,2,3,4,5 do {j := j + 1; break} end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(1, ((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := 0; begin for i in 1,2,3,4,5 do {j := j + 1; if i = 1 then break} end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(1, ((long[])stack.getVar(names.seekName("j")))[0]);
	
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := 0; begin for i in 1,2,3,4,5 do {j := j + 1; if i <> 1 then j := j + 1 else break} end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(1, ((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, i1 : int, j : int := 0; begin for i := 0 to 10 do for i1 := 0 to 10 do {j := j + 1; break 2} end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(1, ((long[])stack.getVar(names.seekName("j")))[0]);
		
		// Continue test
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int := 10, j : int := 0; begin while i >= 0 do {i := i - 1; j := j + 1; continue} end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(11, ((long[])stack.getVar(names.seekName("j")))[0]);
		
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int := 10, j : int := 0; begin do {i := i - 1; j := j + 1; continue} while i >= 0 end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(11, ((long[])stack.getVar(names.seekName("j")))[0]);
		
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := 0; begin for i := 1 to 10 do {j := j + 1; continue} end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(10, ((long[])stack.getVar(names.seekName("j")))[0]);
	
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := 0; begin for i := 1 to 10 step 1 do {j := j + 1; continue} end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(10, ((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := 0; begin for i in 1,2,3,4,5 do {j := j + 1; continue} end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(5, ((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := 0; begin for i in 1,2,3,4,5 do {j := j + 1; if i = 1 then continue} end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(5, ((long[])stack.getVar(names.seekName("j")))[0]);
	
		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, j : int := 0; begin for i in 1,2,3,4,5 do {j := j + 1; if i <> 1 then j := j + 1 else continue} end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(9, ((long[])stack.getVar(names.seekName("j")))[0]);

		names.clear();
		result = ScriptExecutorUtil.execute(ScriptParserUtil.parseScript(new StringReader("var i : int, i1 : int, j : int := 0; begin for i := 0 to 10 do for i1 := 0 to 10 do {j := j + 1; continue 2} end"), names), 
					names, stack = new LocalStack(names, predef, callback), predef, 0, 0, callback);
		Assert.assertEquals(11, ((long[])stack.getVar(names.seekName("j")))[0]);
	}
}
