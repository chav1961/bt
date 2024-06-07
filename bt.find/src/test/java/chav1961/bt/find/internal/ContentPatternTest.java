package chav1961.bt.find.internal;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.find.internal.ContentPattern.Lexema;
import chav1961.bt.find.internal.ContentPattern.LexType;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class ContentPatternTest {

	@Test
	public void parserTest() throws SyntaxException {
		final SyntaxTreeInterface<String>	names  = ContentPattern.prepareSyntaxTree();
		final Lexema[]						parsed = ContentPattern.parse("/.*/->{}()+-*/%,>>=<<==<>!&&count||group10'123'123\n".toCharArray(), names);

		Assert.assertTrue(allMatches(parsed, LexType.PATTERN, LexType.ERGO, LexType.OPENF, LexType.CLOSEF, LexType.OPEN, LexType.CLOSE, LexType.ADD, LexType.SUB, LexType.MUL, LexType.DIV, LexType.MOD, LexType.LIST, LexType.GT, LexType.GE, LexType.LT, LexType.LE, LexType.EQ, LexType.NE, LexType.NOT, LexType.AND, LexType.NAME, LexType.OR, LexType.NAME, LexType.STRING, LexType.NUMBER, LexType.EOF));
		
		try {
			ContentPattern.parse("/.*/ /.*/\n".toCharArray(), names);
			Assert.fail("Mandatory exception was not detected (pattern without ergo)");
		} catch (SyntaxException exc) {
		}
		try {
			ContentPattern.parse("'\n".toCharArray(), names);
			Assert.fail("Mandatory exception was not detected (unclosed quota)");
		} catch (SyntaxException exc) {
		}
		try {
			ContentPattern.parse("?\n".toCharArray(), names);
			Assert.fail("Mandatory exception was not detected (unknown char)");
		} catch (SyntaxException exc) {
		}
		try {
			ContentPattern.parse("&\n".toCharArray(), names);
			Assert.fail("Mandatory exception was not detected (unknown char)");
		} catch (SyntaxException exc) {
		}
		try {
			ContentPattern.parse("|\n".toCharArray(), names);
			Assert.fail("Mandatory exception was not detected (unknown char)");
		} catch (SyntaxException exc) {
		}
	}

	private static boolean allMatches(final Lexema[] source, final LexType... awaited) {
		for(int index = 0, maxIndex = Math.min(source.length, awaited.length); index < maxIndex; index++) {
			if (source[index].type != awaited[index]) {
				return false;
			}
		}
		return true;
	}
}
