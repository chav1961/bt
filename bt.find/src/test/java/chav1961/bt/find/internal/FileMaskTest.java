package chav1961.bt.find.internal;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.find.internal.FileMask.LexType;
import chav1961.bt.find.internal.FileMask.Lexema;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class FileMaskTest {

	@Test
	public void lexParserTest() throws SyntaxException {
		final SyntaxTreeInterface<String>	names = FileMask.prepareSyntaxTree();
		
		Lexema[] result = FileMask.parse(CharUtils.terminateAndConvert2CharArray("/(){|}[]+-", '\n'), names);
		
		Assert.assertEquals(11, result.length);
		Assert.assertTrue(allMatches(result, LexType.SEPARATOR, LexType.OPEN, LexType.CLOSE, LexType.START_ALTER, LexType.ALTER, LexType.END_ALTER, LexType.START_EXPR, LexType.END_EXPR, LexType.ADD, LexType.SUBTRACT, LexType.EOF));

		result = FileMask.parse(CharUtils.terminateAndConvert2CharArray(">>=<<==<>", '\n'), names);

		result = FileMask.parse(CharUtils.terminateAndConvert2CharArray("/*/**/*a?/a*.*/*.?a/a/", '\n'), names);

		result = FileMask.parse(CharUtils.terminateAndConvert2CharArray("./.", '\n'), names);
		
		result = FileMask.parse(CharUtils.terminateAndConvert2CharArray("../..", '\n'), names);
		
		result = FileMask.parse(CharUtils.terminateAndConvert2CharArray("[||&&!length 10 10k 10M 10G]", '\n'), names);
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
