package chav1961.bt.clipper.inner.streams;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.clipper.inner.streams.CommandParser.Lexema;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class CommandParserTest {

	@Test
	public void literalParseTest() throws SyntaxException {
		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.Char,2), new Lexema(Lexema.LexType.EOF)}, parse("1"));
		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.Char,2), new Lexema(Lexema.LexType.Char,3), new Lexema(Lexema.LexType.EOF)}, parse("11 1"));
		Assert.assertArrayEquals(new Lexema[] {new Lexema(Lexema.LexType.Char,2), new Lexema(Lexema.LexType.Char,3), new Lexema(Lexema.LexType.Char,2), new Lexema(Lexema.LexType.Char,3), new Lexema(Lexema.LexType.EOF)}, parse("11 1 11 1"));

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
	}
	
	private Lexema[] parse(final String content) throws SyntaxException {
		return CommandParser.parse(CharUtils.terminateAndConvert2CharArray(content, '\n'), 0, new AndOrTree<Long>(1,1), false);
	}
}
