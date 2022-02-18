package chav1961.bt.clipper.inner.streams;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.clipper.inner.streams.CommandParser.Level;
import chav1961.bt.clipper.inner.streams.CommandParser.Lexema;
import chav1961.bt.clipper.inner.streams.CommandParser.NodeType;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.streams.char2byte.asm.ExpressionNodeType;

public class CommandParserTest {

	@Test
	public void literalParseTest() throws SyntaxException {
		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.Char,"1".toCharArray()), new Lexema(Lexema.LexType.EOF)}, parse("1"));
		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.Char,"11".toCharArray()), new Lexema(Lexema.LexType.Char,"1".toCharArray()), new Lexema(Lexema.LexType.EOF)}, parse("11 1"));
		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.Char,"11".toCharArray()), new Lexema(Lexema.LexType.Char,"1".toCharArray()), new Lexema(Lexema.LexType.Char,"11".toCharArray()), new Lexema(Lexema.LexType.Char,"1".toCharArray()), new Lexema(Lexema.LexType.EOF)}, parse("11 1 11 1"));

		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.Keyword,2), new Lexema(Lexema.LexType.Keyword,2), new Lexema(Lexema.LexType.EOF)}, parse("test TeSt"));

		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.OpenB), new Lexema(Lexema.LexType.CloseB), new Lexema(Lexema.LexType.Ergo), new Lexema(Lexema.LexType.Continuation), new Lexema(Lexema.LexType.EOF)}, parse("[ ] => ;"));

		try{parse(";");
			Assert.fail("Mandatory exception was not detected (Continuation at the left of ergo)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void leftMarkersParseTest() throws SyntaxException {
		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.RegularMarker,0), new Lexema(Lexema.LexType.RegularMarker,1), new Lexema(Lexema.LexType.RegularMarker,0), new Lexema(Lexema.LexType.EOF)}, parse("<x><y><X>"));
		
		try{parse("<x");
			Assert.fail("Mandatory exception was not detected (unclosed marker)");
		} catch (SyntaxException exc) {
		}
		try{parse("<1>");
			Assert.fail("Mandatory exception was not detected (Missing name)");
		} catch (SyntaxException exc) {
		}

		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.ListMarker,0), new Lexema(Lexema.LexType.ListMarker,1), new Lexema(Lexema.LexType.ListMarker,0), new Lexema(Lexema.LexType.EOF)}, parse("<x,...><y,...><X,...>"));
		
		try{parse("<x,...");
			Assert.fail("Mandatory exception was not detected (unclosed marker)");
		} catch (SyntaxException exc) {
		}
		try{parse("<1,...>");
			Assert.fail("Mandatory exception was not detected (Missing name)");
		} catch (SyntaxException exc) {
		}
		try{parse("=> <x,...>");
			Assert.fail("Mandatory exception was not detected (marker at the right)");
		} catch (SyntaxException exc) {
		}

		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.ExtendedMarker,0), new Lexema(Lexema.LexType.ExtendedMarker,1), new Lexema(Lexema.LexType.ExtendedMarker,0), new Lexema(Lexema.LexType.EOF)}, parse("<(x)><(y)><(X)>"));
		
		try{parse("<(x)");
			Assert.fail("Mandatory exception was not detected (unclosed marker)");
		} catch (SyntaxException exc) {
		}
		try{parse("<(1)>");
			Assert.fail("Mandatory exception was not detected (Missing name)");
		} catch (SyntaxException exc) {
		}

		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.WildMarker,0), new Lexema(Lexema.LexType.WildMarker,1), new Lexema(Lexema.LexType.WildMarker,0), new Lexema(Lexema.LexType.EOF)}, parse("<*x*><*y*><*X*>"));
		
		try{parse("<*x*");
			Assert.fail("Mandatory exception was not detected (unclosed marker)");
		} catch (SyntaxException exc) {
		}
		try{parse("<*1*>");
			Assert.fail("Mandatory exception was not detected (Missing name)");
		} catch (SyntaxException exc) {
		}
		try{parse("=> <*x*>");
			Assert.fail("Mandatory exception was not detected (marker at the right)");
		} catch (SyntaxException exc) {
		}

		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.RestrictedMarker, 0, new long[]{1,2,0}), new Lexema(Lexema.LexType.RestrictedMarker, 1, new long[]{1,2,0}), new Lexema(Lexema.LexType.RestrictedMarker, 0, new long[]{1,2,0}), new Lexema(Lexema.LexType.EOF)}, parse("<x:a,b,&><y:a,b,&><X:a,b,&>"));
		
		try{parse("<x:a,b,&");
			Assert.fail("Mandatory exception was not detected (unclosed marker)");
		} catch (SyntaxException exc) {
		}
		try{parse("<1:a,b,&>");
			Assert.fail("Mandatory exception was not detected (Missing name)");
		} catch (SyntaxException exc) {
		}
		try{parse("=> <x:a,b,&>");
			Assert.fail("Mandatory exception was not detected (marker at the right)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void rightMarkersParseTest() throws SyntaxException {
		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.Ergo), new Lexema(Lexema.LexType.RegularResultMarker,0), new Lexema(Lexema.LexType.RegularResultMarker,1), new Lexema(Lexema.LexType.RegularResultMarker,0), new Lexema(Lexema.LexType.EOF)}, parse("=><x><y><X>"));

		try{parse("=><x");
			Assert.fail("Mandatory exception was not detected (unclosed marker)");
		} catch (SyntaxException exc) {
		}
		try{parse("=><1>");
			Assert.fail("Mandatory exception was not detected (Missing name)");
		} catch (SyntaxException exc) {
		}
		
		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.Ergo), new Lexema(Lexema.LexType.DumbResultMarker,0), new Lexema(Lexema.LexType.DumbResultMarker,1), new Lexema(Lexema.LexType.DumbResultMarker,0), new Lexema(Lexema.LexType.EOF)}, parse("=>#<x>#<y>#<X>"));

		try{parse("=>#<x");
			Assert.fail("Mandatory exception was not detected (unclosed marker)");
		} catch (SyntaxException exc) {
		}
		try{parse("=>#<1>");
			Assert.fail("Mandatory exception was not detected (Missing name)");
		} catch (SyntaxException exc) {
		}

		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.Ergo), new Lexema(Lexema.LexType.NormalResultMarker,0), new Lexema(Lexema.LexType.NormalResultMarker,1), new Lexema(Lexema.LexType.NormalResultMarker,0), new Lexema(Lexema.LexType.EOF)}, parse("=><\"x\"><\"y\"><\"X\">"));

		try{parse("=><\"x\"");
			Assert.fail("Mandatory exception was not detected (unclosed marker)");
		} catch (SyntaxException exc) {
		}
		try{parse("=><\"1\">");
			Assert.fail("Mandatory exception was not detected (Missing name)");
		} catch (SyntaxException exc) {
		}

		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.Ergo), new Lexema(Lexema.LexType.SmartResultMarker,0), new Lexema(Lexema.LexType.SmartResultMarker,1), new Lexema(Lexema.LexType.SmartResultMarker,0), new Lexema(Lexema.LexType.EOF)}, parse("=><(x)><(y)><(X)>"));

		try{parse("=><(x)");
			Assert.fail("Mandatory exception was not detected (unclosed marker)");
		} catch (SyntaxException exc) {
		}
		try{parse("=><(1)>");
			Assert.fail("Mandatory exception was not detected (Missing name)");
		} catch (SyntaxException exc) {
		}

		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.Ergo), new Lexema(Lexema.LexType.BlockResultMarker,0), new Lexema(Lexema.LexType.BlockResultMarker,1), new Lexema(Lexema.LexType.BlockResultMarker,0), new Lexema(Lexema.LexType.EOF)}, parse("=><{x}><{y}><{X}>"));

		try{parse("=><{x}");
			Assert.fail("Mandatory exception was not detected (unclosed marker)");
		} catch (SyntaxException exc) {
		}
		try{parse("=><{1}>");
			Assert.fail("Mandatory exception was not detected (Missing name)");
		} catch (SyntaxException exc) {
		}

		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.Ergo), new Lexema(Lexema.LexType.BoolResultMarker,0), new Lexema(Lexema.LexType.BoolResultMarker,1), new Lexema(Lexema.LexType.BoolResultMarker,0), new Lexema(Lexema.LexType.EOF)}, parse("=><.x.><.y.><.X.>"));

		try{parse("=><.x.");
			Assert.fail("Mandatory exception was not detected (unclosed marker)");
		} catch (SyntaxException exc) {
		}
		try{parse("=><.1.>");
			Assert.fail("Mandatory exception was not detected (Missing name)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void literalBuildTest() throws SyntaxException {
		SyntaxNode<NodeType, SyntaxNode> root;
		
		root = new SyntaxNode<>(0, 0, NodeType.Root, 0, null);
		CommandParser.buildTree(parse("x => y"), 0, Level.Top, root);
//		System.err.println(CommandParser.printTree(root));

		root = new SyntaxNode<>(0, 0, NodeType.Root, 0, null);
		CommandParser.buildTree(parse("x [ y ] => z"), 0, Level.Top, root);
//		System.err.println(CommandParser.printTree(root));

		root = new SyntaxNode<>(0, 0, NodeType.Root, 0, null);
		CommandParser.buildTree(parse("x [ y ] [z] => t "), 0, Level.Top, root);
//		System.err.println(CommandParser.printTree(root));

		try{CommandParser.buildTree(parse("x [ y ] [z] "), 0, Level.Top, new SyntaxNode<>(0, 0, NodeType.Root, 0, null));
			Assert.fail("Mandatory exception was not detected (Missing =>)");
		} catch (SyntaxException exc) {
		}
	}	

	@Test
	public void lexIdentificationTest() throws SyntaxException {
		final List<int[]>	ranges = new ArrayList<>();
		
		Assert.assertTrue(identity(" x ", " x ", ranges));
		Assert.assertTrue(identity(" x ", " X ", ranges));
		Assert.assertFalse(identity(" x ", " y ", ranges));

		Assert.assertTrue(identity(" : , ", ":,", ranges));
		Assert.assertTrue(identity(":,", " : , ", ranges));

		Assert.assertTrue(identity("<x>", "2 + 3", ranges));
	}	
	
	
	private Lexema[] parse(final String content) throws SyntaxException {
		return parse(content, new AndOrTree<Long>(1,1));
	}

	private Lexema[] parse(final String content, final SyntaxTreeInterface<Long> tree) throws SyntaxException {
		return CommandParser.parse(CharUtils.terminateAndConvert2CharArray(content, '\n'), 0, tree, false);
	}
	
	private boolean identity(final String template, final String test, final List<int[]> ranges) throws SyntaxException {
		final SyntaxTreeInterface<Long>		tree = new AndOrTree<>(1, 1);
		final int[]							forNames = new int[2];
		Lexema[]							lex = parse(template, tree);
		
		ranges.clear();
		return CommandParser.identify(CharUtils.terminateAndConvert2CharArray(test, '\n'), 0, tree, lex[0], forNames, ranges);
	}

}
