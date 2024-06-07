package chav1961.bt.find.internal;



import java.nio.CharBuffer;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.find.internal.ContentPattern.Lexema;
import chav1961.bt.find.internal.ContentPattern.OperandPrty;
import chav1961.bt.find.internal.ContentPattern.OperandType;
import chav1961.bt.find.internal.ContentPattern.LexType;
import chav1961.bt.find.internal.ContentPattern.SyntaxNodeType;
import chav1961.bt.find.internal.ContentPattern.Operand;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.ReusableInstances;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

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

	@Test
	public void exprParserTest() throws SyntaxException {
		SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	root;
		
		// Terms
		
		root = buildExpression("10");
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.getType());
		Assert.assertEquals(10, root.value);
		Assert.assertNull(root.children);

		root = buildExpression("count");
		Assert.assertEquals(SyntaxNodeType.VAR, root.getType());
		Assert.assertEquals(0, root.value);
		Assert.assertNull(root.children);

		root = buildExpression("group1");
		Assert.assertEquals(SyntaxNodeType.VAR, root.getType());
		Assert.assertEquals(1, root.value);
		Assert.assertNull(root.children);
		
		root = buildExpression("'assa'");
		Assert.assertEquals(SyntaxNodeType.STRING, root.getType());
		Assert.assertEquals("assa", root.cargo);
		Assert.assertNull(root.children);
		
		root = buildExpression("('assa')");
		Assert.assertEquals(SyntaxNodeType.STRING, root.getType());
		Assert.assertEquals("assa", root.cargo);
		Assert.assertNull(root.children);
		
		try {buildExpression("('assa'");
			Assert.fail("Mandatory exception was not detected (missing ')')");
		} catch (SyntaxException exc) {
		}
		try {buildExpression("unknown");
			Assert.fail("Mandatory exception was not detected (unknown name)");
		} catch (SyntaxException exc) {
		}
		try {buildExpression(")");
			Assert.fail("Mandatory exception was not detected (unwaited lex)");
		} catch (SyntaxException exc) {
		}
		
		// Unary
		
		root = buildExpression("-10");
		Assert.assertEquals(SyntaxNodeType.MINUS, root.getType());
		Assert.assertNotNull(root.children);
		Assert.assertEquals(1, root.children.length);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[0].getType());
		Assert.assertEquals(10, root.children[0].value);
		
		// Add and mul
		
		root = buildExpression("10+20-30");
		Assert.assertEquals(SyntaxNodeType.ADD, root.getType());
		Assert.assertTrue(root.cargo instanceof char[]);
		Assert.assertArrayEquals(new char[] {'+','+','-'}, (char[])root.cargo);
		Assert.assertNotNull(root.children);
		Assert.assertEquals(3, root.children.length);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[0].getType());
		Assert.assertEquals(10, root.children[0].value);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[1].getType());
		Assert.assertEquals(20, root.children[1].value);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[2].getType());
		Assert.assertEquals(30, root.children[2].value);

		root = buildExpression("10*20/30%40");
		Assert.assertEquals(SyntaxNodeType.MUL, root.getType());
		Assert.assertTrue(root.cargo instanceof char[]);
		Assert.assertArrayEquals(new char[] {'*','*','/','%'}, (char[])root.cargo);
		Assert.assertNotNull(root.children);
		Assert.assertEquals(4, root.children.length);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[0].getType());
		Assert.assertEquals(10, root.children[0].value);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[1].getType());
		Assert.assertEquals(20, root.children[1].value);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[2].getType());
		Assert.assertEquals(30, root.children[2].value);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[3].getType());
		Assert.assertEquals(40, root.children[3].value);

		// Comparison
		
		root = buildExpression("10>20");
		Assert.assertEquals(SyntaxNodeType.COMPARE, root.getType());
		Assert.assertTrue(root.cargo instanceof LexType);
		Assert.assertEquals(LexType.GT, (LexType)root.cargo);
		Assert.assertNotNull(root.children);
		Assert.assertEquals(2, root.children.length);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[0].getType());
		Assert.assertEquals(10, root.children[0].value);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[1].getType());
		Assert.assertEquals(20, root.children[1].value);

		// Not
		
		root = buildExpression("!10");
		Assert.assertEquals(SyntaxNodeType.NOT, root.getType());
		Assert.assertNotNull(root.children);
		Assert.assertEquals(1, root.children.length);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[0].getType());
		Assert.assertEquals(10, root.children[0].value);

		// And and or
		
		root = buildExpression("10&&20&&30");
		Assert.assertEquals(SyntaxNodeType.AND, root.getType());
		Assert.assertNull(root.cargo);
		Assert.assertNotNull(root.children);
		Assert.assertEquals(3, root.children.length);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[0].getType());
		Assert.assertEquals(10, root.children[0].value);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[1].getType());
		Assert.assertEquals(20, root.children[1].value);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[2].getType());
		Assert.assertEquals(30, root.children[2].value);

		root = buildExpression("10||20||30");
		Assert.assertEquals(SyntaxNodeType.OR, root.getType());
		Assert.assertNull(root.cargo);
		Assert.assertNotNull(root.children);
		Assert.assertEquals(3, root.children.length);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[0].getType());
		Assert.assertEquals(10, root.children[0].value);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[1].getType());
		Assert.assertEquals(20, root.children[1].value);
		Assert.assertEquals(SyntaxNodeType.NUMBER, root.children[2].getType());
		Assert.assertEquals(30, root.children[2].value);
	}	
	
	@Test
	public void treeBuilderTest() throws SyntaxException {
		SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	root;
		
		// Basic test
		
		root = buildSyntaxTree("/.*/");
		Assert.assertEquals(SyntaxNodeType.NODE, root.getType());
		Assert.assertTrue(root.cargo instanceof Pattern);
		Assert.assertEquals(1, (int)(root.value >> 32));
		Assert.assertEquals(1, (int)(root.value & 0xFFFFFFFF));
		Assert.assertNull(root.children);

		// Repeaters test
		
		root = buildSyntaxTree("/.*/*");
		Assert.assertEquals(SyntaxNodeType.NODE, root.getType());
		Assert.assertTrue(root.cargo instanceof Pattern);
		Assert.assertEquals(0, (int)(root.value >> 32));
		Assert.assertEquals(Integer.MAX_VALUE, (int)(root.value & 0xFFFFFFFF));
		Assert.assertNull(root.children);

		root = buildSyntaxTree("/.*/+");
		Assert.assertEquals(SyntaxNodeType.NODE, root.getType());
		Assert.assertTrue(root.cargo instanceof Pattern);
		Assert.assertEquals(1, (int)(root.value >> 32));
		Assert.assertEquals(Integer.MAX_VALUE, (int)(root.value & 0xFFFFFFFF));
		Assert.assertNull(root.children);

		root = buildSyntaxTree("/.*/{2,3}");
		Assert.assertEquals(SyntaxNodeType.NODE, root.getType());
		Assert.assertTrue(root.cargo instanceof Pattern);
		Assert.assertEquals(2, (int)(root.value >> 32));
		Assert.assertEquals(3, (int)(root.value & 0xFFFFFFFF));
		Assert.assertNull(root.children);

		root = buildSyntaxTree("/.*/{2}");
		Assert.assertEquals(SyntaxNodeType.NODE, root.getType());
		Assert.assertTrue(root.cargo instanceof Pattern);
		Assert.assertEquals(2, (int)(root.value >> 32));
		Assert.assertEquals(Integer.MAX_VALUE, (int)(root.value & 0xFFFFFFFF));
		Assert.assertNull(root.children);

		root = buildSyntaxTree("/.*/{,2}");
		Assert.assertEquals(SyntaxNodeType.NODE, root.getType());
		Assert.assertTrue(root.cargo instanceof Pattern);
		Assert.assertEquals(0, (int)(root.value >> 32));
		Assert.assertEquals(2, (int)(root.value & 0xFFFFFFFF));
		Assert.assertNull(root.children);
		
		try{buildSyntaxTree("/.*/{2,3");
			Assert.fail("Mandatory exception was not detected (missing '}')");
		} catch (SyntaxException exc) {			
		}
		try{buildSyntaxTree("/.*/{2,}");
			Assert.fail("Mandatory exception was not detected (missing numer)");
		} catch (SyntaxException exc) {			
		}
		try{buildSyntaxTree("/.*/{}");
			Assert.fail("Mandatory exception was not detected (missing numer)");
		} catch (SyntaxException exc) {			
		}

		// Expressions test
		root = buildSyntaxTree("/.*/(10=10)");
		Assert.assertEquals(SyntaxNodeType.NODE, root.getType());
		Assert.assertTrue(root.cargo instanceof Pattern);
		Assert.assertEquals(1, (int)(root.value >> 32));
		Assert.assertEquals(1, (int)(root.value & 0xFFFFFFFF));
		Assert.assertNotNull(root.children);
		Assert.assertEquals(2, root.children.length);
		Assert.assertNull(root.children[0]);
		Assert.assertNotNull(root.children[1]);
		
		// Sequences test
		
		root = buildSyntaxTree("/.*/ -> /.*/");
		Assert.assertEquals(SyntaxNodeType.NODE, root.getType());
		Assert.assertTrue(root.cargo instanceof Pattern);
		Assert.assertEquals(1, (int)(root.value >> 32));
		Assert.assertEquals(1, (int)(root.value & 0xFFFFFFFF));
		Assert.assertNotNull(root.children);
		Assert.assertEquals(2, root.children.length);
		Assert.assertNotNull(root.children[0]);
		Assert.assertNull(root.children[1]);
		root = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) root.children[0];
		Assert.assertEquals(SyntaxNodeType.NODE, root.getType());
		Assert.assertTrue(root.cargo instanceof Pattern);
		Assert.assertEquals(1, (int)(root.value >> 32));
		Assert.assertEquals(1, (int)(root.value & 0xFFFFFFFF));
		Assert.assertNull(root.children);

		root = buildSyntaxTree("/.*/{2,3}(10) -> /.*/");
		Assert.assertEquals(SyntaxNodeType.NODE, root.getType());
		Assert.assertTrue(root.cargo instanceof Pattern);
		Assert.assertEquals(2, (int)(root.value >> 32));
		Assert.assertEquals(3, (int)(root.value & 0xFFFFFFFF));
		Assert.assertNotNull(root.children);
		Assert.assertEquals(2, root.children.length);
		Assert.assertNotNull(root.children[0]);
		Assert.assertNotNull(root.children[1]);
		root = (SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>) root.children[0];
		Assert.assertEquals(SyntaxNodeType.NODE, root.getType());
		Assert.assertTrue(root.cargo instanceof Pattern);
		Assert.assertEquals(1, (int)(root.value >> 32));
		Assert.assertEquals(1, (int)(root.value & 0xFFFFFFFF));
		Assert.assertNull(root.children);
		
	}

	@Test
	public void calcExprTest() throws SyntaxException {
		Operand	op;

		// Terms
		
		op = calculateExpr("10", (id)->"10");
		Assert.assertEquals(OperandType.NUMBER, op.type);
		Assert.assertEquals(10L, op.value);

		op = calculateExpr("'10'", (id)->"10");
		Assert.assertEquals(OperandType.STRING, op.type);
		Assert.assertEquals("10", op.value);
		
		op = calculateExpr("count", (id)->"10");
		Assert.assertEquals(OperandType.STRING, op.type);
		Assert.assertEquals("10", op.value);
		
		// Unary
		
		op = calculateExpr("-10", (id)->"10");
		Assert.assertEquals(OperandType.NUMBER, op.type);
		Assert.assertEquals(-10L, op.value);

		// Multiplication and addition
		
		op = calculateExpr("10*10/3%10", (id)->"10");
		Assert.assertEquals(OperandType.NUMBER, op.type);
		Assert.assertEquals(3L, op.value);

		op = calculateExpr("10+20-40", (id)->"10");
		Assert.assertEquals(OperandType.NUMBER, op.type);
		Assert.assertEquals(-10L, op.value);

		op = calculateExpr("10+20-count", (id)->"10");
		Assert.assertEquals(OperandType.NUMBER, op.type);
		Assert.assertEquals(20L, op.value);
		
		// Comparison
		
		op = calculateExpr("10=20", (id)->"10");
		Assert.assertEquals(OperandType.BOOLEAN, op.type);
		Assert.assertEquals(Boolean.FALSE, op.value);
	
		op = calculateExpr("10<>20", (id)->"10");
		Assert.assertEquals(OperandType.BOOLEAN, op.type);
		Assert.assertEquals(Boolean.TRUE, op.value);

		op = calculateExpr("10>10", (id)->"10");
		Assert.assertEquals(OperandType.BOOLEAN, op.type);
		Assert.assertEquals(Boolean.FALSE, op.value);

		op = calculateExpr("10>=10", (id)->"10");
		Assert.assertEquals(OperandType.BOOLEAN, op.type);
		Assert.assertEquals(Boolean.TRUE, op.value);
	
		op = calculateExpr("10<10", (id)->"10");
		Assert.assertEquals(OperandType.BOOLEAN, op.type);
		Assert.assertEquals(Boolean.FALSE, op.value);

		op = calculateExpr("10<=10", (id)->"10");
		Assert.assertEquals(OperandType.BOOLEAN, op.type);
		Assert.assertEquals(Boolean.TRUE, op.value);
		
		// Not
		
		op = calculateExpr("!10=20", (id)->"10");
		Assert.assertEquals(OperandType.BOOLEAN, op.type);
		Assert.assertEquals(Boolean.TRUE, op.value);

		// And and or
		
		op = calculateExpr("10=10&&20=20", (id)->"10");
		Assert.assertEquals(OperandType.BOOLEAN, op.type);
		Assert.assertEquals(Boolean.TRUE, op.value);

		op = calculateExpr("10=10&&20<>20", (id)->"10");
		Assert.assertEquals(OperandType.BOOLEAN, op.type);
		Assert.assertEquals(Boolean.FALSE, op.value);

		op = calculateExpr("10<>10||20<>20", (id)->"10");
		Assert.assertEquals(OperandType.BOOLEAN, op.type);
		Assert.assertEquals(Boolean.FALSE, op.value);

		op = calculateExpr("10<>10||20=20", (id)->"10");
		Assert.assertEquals(OperandType.BOOLEAN, op.type);
		Assert.assertEquals(Boolean.TRUE, op.value);
	}	

	@Test
	public void matchTest() throws SyntaxException {
		
	}	
	
	private static boolean allMatches(final Lexema[] source, final LexType... awaited) {
		for(int index = 0, maxIndex = Math.min(source.length, awaited.length); index < maxIndex; index++) {
			if (source[index].type != awaited[index]) {
				return false;
			}
		}
		return true;
	}
	
	private SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> buildSyntaxTree(final String content) throws SyntaxException {
		final SyntaxTreeInterface<String>	names  = ContentPattern.prepareSyntaxTree();
		final Lexema[]						parsed = ContentPattern.parse(CharUtils.terminateAndConvert2CharArray(content, '\n'), names);
		final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	root = new SyntaxNode<>(0, 0, SyntaxNodeType.UNKNOWN, 0, parsed);
		
		final int 	theEnd = ContentPattern.buildSyntaxTree(parsed, 0, root, names);
		
		if (parsed[theEnd].type != LexType.EOF) {
			throw new SyntaxException(0, theEnd, "Dust in the tail");
		}
		else {
			return root;
		}
	}

	private SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> buildExpression(final String content) throws SyntaxException {
		final SyntaxTreeInterface<String>	names  = ContentPattern.prepareSyntaxTree();
		final Lexema[]						parsed = ContentPattern.parse(CharUtils.terminateAndConvert2CharArray(content, '\n'), names);
		final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	root = new SyntaxNode<>(0, 0, SyntaxNodeType.UNKNOWN, 0, parsed);
		
		final int 	theEnd = ContentPattern.parseExpr(OperandPrty.OR, parsed, 0, root, names);
		
		if (parsed[theEnd].type != LexType.EOF) {
			throw new SyntaxException(0, theEnd, "Dust in the tail");
		}
		else {
			return root;
		}
	}
	
	private Operand calculateExpr(final String content, final Function<Long, String> getter) throws SyntaxException {
		final SyntaxTreeInterface<String>	names  = ContentPattern.prepareSyntaxTree();
		final Lexema[]						parsed = ContentPattern.parse(CharUtils.terminateAndConvert2CharArray(content, '\n'), names);
		final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	root = new SyntaxNode<>(0, 0, SyntaxNodeType.UNKNOWN, 0, parsed);
		
		final int 	theEnd = ContentPattern.parseExpr(OperandPrty.OR, parsed, 0, root, names);
		
		if (parsed[theEnd].type != LexType.EOF) {
			throw new SyntaxException(0, theEnd, "Dust in the tail");
		}
		else {
			final ReusableInstances<Operand>	ops = new ReusableInstances<>(()->new Operand());
			
			return ContentPattern.testExpr(root, getter, ops);
		}
	}
}
