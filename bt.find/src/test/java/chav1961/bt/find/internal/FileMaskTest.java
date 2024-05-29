package chav1961.bt.find.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import chav1961.bt.find.internal.FileMask.ExprPriority;
import chav1961.bt.find.internal.FileMask.LexType;
import chav1961.bt.find.internal.FileMask.Lexema;
import chav1961.bt.find.internal.FileMask.Operand;
import chav1961.bt.find.internal.FileMask.SyntaxNodeType;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

public class FileMaskTest {

	@BeforeClass
	public static void prepare() {
		
	}

	@AfterClass
	public static void unprepare() {
		
	}
	
	
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
	
	@Test
	public void buildExpressionTest() throws SyntaxException {
		SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	node;
		
		node = buildExpression(""); 
	}	

	@Test
	public void calculateExpressionTest() throws SyntaxException {
		Operand 	result = calculateExpression("");
	}	
	
	@Test
	public void buildTemplateTest() throws SyntaxException {
		SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	node;

		node = buildTemplate("");
	}

	@Test
	public void walkTemplateTest() throws SyntaxException {
		final File	root = new File("");
		List<File>	found;
		
		found = walkTemplate(root, "");
	}
	
	
	private static boolean allMatches(final Lexema[] source, final LexType... awaited) {
		for(int index = 0, maxIndex = Math.min(source.length, awaited.length); index < maxIndex; index++) {
			if (source[index].type != awaited[index]) {
				return false;
			}
		}
		return true;
	}

	private static SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> buildExpression(final String expr) throws SyntaxException {
		final SyntaxTreeInterface<String>	names = FileMask.prepareSyntaxTree();
		final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	node = new SyntaxNode<>(0, 0, SyntaxNodeType.ROOT, 0, null);
		
		FileMask.buildExpression(FileMask.parse(CharUtils.terminateAndConvert2CharArray("", '\n'), names), 0, ExprPriority.OR, null);
		return node;
	}

	private static SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> buildTemplate(final String expr) throws SyntaxException {
		final SyntaxTreeInterface<String>	names = FileMask.prepareSyntaxTree();
		final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	node = new SyntaxNode<>(0, 0, SyntaxNodeType.ROOT, 0, null);
		
		FileMask.buildCurrentName(FileMask.parse(CharUtils.terminateAndConvert2CharArray("", '\n'), names), 0, node);
		return node;
	}
	
	private static Operand calculateExpression(final String expr) throws SyntaxException {
		final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	node = buildExpression(expr);
		final File	file = new File("test.txt");
	
		return FileMask.calculateExpr(file, node);
	}
	
	private static List<File> walkTemplate(final File file, final String expr) throws SyntaxException {
		final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	node = buildTemplate(expr);
		final List<File>	result = new ArrayList<>();
	
		FileMask.walk(file, node, (f)->result.add(f));
		return result;
	}
}
