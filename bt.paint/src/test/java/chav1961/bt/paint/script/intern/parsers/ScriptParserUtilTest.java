package chav1961.bt.paint.script.intern.parsers;

import java.io.StringReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.paint.script.intern.interfaces.LexTypes;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.CollectionType;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.DataTypes;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.EntityDescriptor;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.Lexema;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.OperatorTypes;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.SyntaxNodeType;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.OrdinalSyntaxTree;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

public class ScriptParserUtilTest {
	@Test
	public void parseLexTest() throws SyntaxException {
		final SyntaxTreeInterface<Object>	names = new OrdinalSyntaxTree<>();
		List<Lexema>	result;

		// single chars
		result = ScriptParserUtil.parseLex(new StringReader("()[]{},.;*%=!"), names, true);
		Assert.assertEquals(14, result.size());
		Assert.assertEquals(LexTypes.OPEN, result.get(0).getType());
		Assert.assertEquals(LexTypes.CLOSE, result.get(1).getType());
		Assert.assertEquals(LexTypes.OPENB, result.get(2).getType());
		Assert.assertEquals(LexTypes.CLOSEB, result.get(3).getType());
		Assert.assertEquals(LexTypes.OPENF, result.get(4).getType());
		Assert.assertEquals(LexTypes.CLOSEF, result.get(5).getType());
		Assert.assertEquals(LexTypes.COMMA, result.get(6).getType());
		Assert.assertEquals(LexTypes.DOT, result.get(7).getType());
		Assert.assertEquals(LexTypes.SEMICOLON, result.get(8).getType());
		Assert.assertEquals(OperatorTypes.MUL, result.get(9).getOperatorType());
		Assert.assertEquals(OperatorTypes.MOD, result.get(10).getOperatorType());
		Assert.assertEquals(OperatorTypes.EQ, result.get(11).getOperatorType());
		Assert.assertEquals(OperatorTypes.BOOL_NOT, result.get(12).getOperatorType());
		Assert.assertEquals(LexTypes.EOF, result.get(13).getType());

		// char sequences
		result = ScriptParserUtil.parseLex(new StringReader(": :: + ++ - -- & && | || > >= < <= <>"), names, true);
		Assert.assertEquals(16, result.size());
		Assert.assertEquals(LexTypes.COLON, result.get(0).getType());
		Assert.assertEquals(LexTypes.CAST, result.get(1).getType());
		Assert.assertEquals(OperatorTypes.ADD, result.get(2).getOperatorType());
		Assert.assertEquals(OperatorTypes.INC, result.get(3).getOperatorType());
		Assert.assertEquals(OperatorTypes.SUB, result.get(4).getOperatorType());
		Assert.assertEquals(OperatorTypes.DEC, result.get(5).getOperatorType());
		Assert.assertEquals(OperatorTypes.BIT_AND, result.get(6).getOperatorType());
		Assert.assertEquals(OperatorTypes.BOOL_AND, result.get(7).getOperatorType());
		Assert.assertEquals(OperatorTypes.BIT_OR, result.get(8).getOperatorType());
		Assert.assertEquals(OperatorTypes.BOOL_OR, result.get(9).getOperatorType());
		Assert.assertEquals(OperatorTypes.GT, result.get(10).getOperatorType());
		Assert.assertEquals(OperatorTypes.GE, result.get(11).getOperatorType());
		Assert.assertEquals(OperatorTypes.LT, result.get(12).getOperatorType());
		Assert.assertEquals(OperatorTypes.LE, result.get(13).getOperatorType());
		Assert.assertEquals(OperatorTypes.NE, result.get(14).getOperatorType());

		// Complex lexemas
		result = ScriptParserUtil.parseLex(new StringReader("123 \"456\" `789` true false for mzinana"), names, true);
		Assert.assertEquals(8, result.size());
		Assert.assertEquals(LexTypes.CONSTANT, result.get(0).getType());
		Assert.assertEquals(LexTypes.CONSTANT, result.get(1).getType());
		Assert.assertEquals(LexTypes.SUBSTITUTION, result.get(2).getType());
		Assert.assertEquals(LexTypes.CONSTANT, result.get(3).getType());
		Assert.assertEquals(LexTypes.CONSTANT, result.get(4).getType());
		Assert.assertEquals(LexTypes.STATEMENT, result.get(5).getType());
		Assert.assertEquals(LexTypes.NAME, result.get(6).getType());

		// Erroneous sequences
		result = ScriptParserUtil.parseLex(new StringReader("\"123"), names, true);
		Assert.assertEquals(3, result.size());
		Assert.assertEquals(LexTypes.ERROR, result.get(0).getType());

		result = ScriptParserUtil.parseLex(new StringReader("`123"), names, true);
		Assert.assertEquals(3, result.size());
		Assert.assertEquals(LexTypes.ERROR, result.get(0).getType());

		result = ScriptParserUtil.parseLex(new StringReader("?"), names, true);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(LexTypes.ERROR, result.get(0).getType());

		// Single-line comment
		result = ScriptParserUtil.parseLex(new StringReader("//"), names, true);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(LexTypes.EOF, result.get(0).getType());
		
		// Exceptions
		try {ScriptParserUtil.parseLex(null, names, true);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {ScriptParserUtil.parseLex(new StringReader("123 \"456\" `789` true false for mzinana"), null, true);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		try {ScriptParserUtil.parseLex(new StringReader("\"123"), names, false);
			Assert.fail("Mandatory exception was not detected (unterminated string)");
		} catch (SyntaxException exc) {
		}
		try {ScriptParserUtil.parseLex(new StringReader("`123"), names, false);
			Assert.fail("Mandatory exception was not detected (unterminated substitution)");
		} catch (SyntaxException exc) {
		}
		try {ScriptParserUtil.parseLex(new StringReader("?"), names, false);
			Assert.fail("Mandatory exception was not detected (unknown lexema)");
		} catch (SyntaxException exc) {
		}
	}
	
	@Test
	public void simpleTermTest() throws SyntaxException {
		final SyntaxTreeInterface<EntityDescriptor>	names = new AndOrTree<>();
		SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	root = new SyntaxNode<>(0, 0, SyntaxNodeType.ROOT, 0, null);
		
		ScriptParserUtil.buildExpression(buildLex("1", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.getType());
		Assert.assertEquals(1, ((Lexema)root.cargo).getLongAssociated());

		ScriptParserUtil.buildExpression(buildLex("\"abc\"", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.getType());
		Assert.assertArrayEquals("abc".toCharArray(), ((Lexema)root.cargo).getObjectAssociated(char[].class));

		ScriptParserUtil.buildExpression(buildLex("`abc`", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.SUBSTITUTION, root.getType());
		Assert.assertArrayEquals("abc".toCharArray(), ((Lexema)root.cargo).getObjectAssociated(char[].class));

		ScriptParserUtil.buildExpression(buildLex("true", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.getType());
		Assert.assertEquals(1, ((Lexema)root.cargo).getLongAssociated());
		
		ScriptParserUtil.buildExpression(buildLex("(1)", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.getType());
		Assert.assertEquals(1, ((Lexema)root.cargo).getLongAssociated());
		
		try{ScriptParserUtil.buildExpression(buildLex("(1", names), 0, names, root);
			Assert.fail("Mandatory exception was not detected (missing ')')");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void expressionTest() throws SyntaxException {
		final SyntaxTreeInterface<EntityDescriptor>	names = new AndOrTree<>();
		SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	root = new SyntaxNode<>(0, 0, SyntaxNodeType.ROOT, 0, null);

		ScriptParserUtil.buildExpression(buildLex("-1", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.PREFIX, root.getType());
		Assert.assertEquals(OperatorTypes.SUB, root.cargo);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[0].getType());

		ScriptParserUtil.buildExpression(buildLex("+1", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.PREFIX, root.getType());
		Assert.assertEquals(OperatorTypes.ADD, root.cargo);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[0].getType());

		ScriptParserUtil.buildExpression(buildLex("~1", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.PREFIX, root.getType());
		Assert.assertEquals(OperatorTypes.BIT_INV, root.cargo);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[0].getType());

		ScriptParserUtil.buildExpression(buildLex("++1", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.PREFIX, root.getType());
		Assert.assertEquals(OperatorTypes.INC, root.cargo);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[0].getType());

		ScriptParserUtil.buildExpression(buildLex("1++", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.SUFFIX, root.getType());
		Assert.assertEquals(OperatorTypes.INC, root.cargo);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[0].getType());

		ScriptParserUtil.buildExpression(buildLex("2&3&4", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.BINARY, root.getType());
		Assert.assertArrayEquals(new OperatorTypes[] {OperatorTypes.BIT_AND, OperatorTypes.BIT_AND}, (OperatorTypes[])root.cargo);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[0].getType());
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[1].getType());
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[2].getType());

		ScriptParserUtil.buildExpression(buildLex("2|3^4", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.BINARY, root.getType());
		Assert.assertArrayEquals(new OperatorTypes[] {OperatorTypes.BIT_OR, OperatorTypes.BIT_XOR}, (OperatorTypes[])root.cargo);
		
		ScriptParserUtil.buildExpression(buildLex("2*3/4%5", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.BINARY, root.getType());
		Assert.assertArrayEquals(new OperatorTypes[] {OperatorTypes.MUL, OperatorTypes.DIV, OperatorTypes.MOD}, (OperatorTypes[])root.cargo);

		ScriptParserUtil.buildExpression(buildLex("2+3-4", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.BINARY, root.getType());
		Assert.assertArrayEquals(new OperatorTypes[] {OperatorTypes.ADD, OperatorTypes.SUB}, (OperatorTypes[])root.cargo);

		ScriptParserUtil.buildExpression(buildLex("2 > 0", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.STRONG_BINARY, root.getType());
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[0].getType());
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[1].getType());
		
		ScriptParserUtil.buildExpression(buildLex("!1", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.PREFIX, root.getType());
		Assert.assertEquals(OperatorTypes.BOOL_NOT, root.cargo);
		
		ScriptParserUtil.buildExpression(buildLex("2&&3&&4", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.BINARY, root.getType());
		Assert.assertArrayEquals(new OperatorTypes[] {OperatorTypes.BOOL_AND, OperatorTypes.BOOL_AND}, (OperatorTypes[])root.cargo);

		ScriptParserUtil.buildExpression(buildLex("2||3||4", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.BINARY, root.getType());
		Assert.assertArrayEquals(new OperatorTypes[] {OperatorTypes.BOOL_OR, OperatorTypes.BOOL_OR}, (OperatorTypes[])root.cargo);
	}	

	@Test
	public void expressionListTest() throws SyntaxException {
		final SyntaxTreeInterface<EntityDescriptor>	names = new AndOrTree<>();
		SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	root = new SyntaxNode<>(0, 0, SyntaxNodeType.ROOT, 0, null);

		ScriptParserUtil.buildListExpression(buildLex("1", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.getType());
	
		ScriptParserUtil.buildListExpression(buildLex("1..2", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.RANGE, root.getType());
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[0].getType());
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[1].getType());

		ScriptParserUtil.buildListExpression(buildLex("1,2,3", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.LIST, root.getType());
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[0].getType());
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[1].getType());
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[2].getType());
		
		ScriptParserUtil.buildListExpression(buildLex("1,2..3,4", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.LIST, root.getType());
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[0].getType());
		Assert.assertEquals(SyntaxNodeType.RANGE, root.children[1].getType());
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[2].getType());

		ScriptParserUtil.buildExpression(buildLex("2 in 3..4", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.STRONG_BINARY, root.getType());
		Assert.assertEquals(SyntaxNodeType.CONSTANT, root.children[0].getType());
		Assert.assertEquals(SyntaxNodeType.RANGE, root.children[1].getType());
	}

	@Test
	public void accessTest() throws SyntaxException {
	}	

	@Test
	public void declarationsTest() throws SyntaxException {
		SyntaxTreeInterface<EntityDescriptor>	names = new AndOrTree<>();
		SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	root = new SyntaxNode<>(0, 0, SyntaxNodeType.ROOT, 0, null);

		ScriptParserUtil.buildDeclarations(buildLex("i : int", names), 0, names, root);
		Assert.assertTrue(names.seekName("i") >= 0);
		Assert.assertEquals(DataTypes.INT, names.getCargo(names.seekName("i")).dataType);

		ScriptParserUtil.buildDeclarations(buildLex("j : real := 10", names), 0, names, root);
		Assert.assertTrue(names.seekName("j") >= 0);
		Assert.assertEquals(DataTypes.REAL, names.getCargo(names.seekName("j")).dataType);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, names.getCargo(names.seekName("j")).initials.getType());

		ScriptParserUtil.buildDeclarations(buildLex("k : str := \"123\", m : int", names), 0, names, root);
		Assert.assertTrue(names.seekName("k") >= 0);
		Assert.assertEquals(DataTypes.STR, names.getCargo(names.seekName("k")).dataType);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, names.getCargo(names.seekName("k")).initials.getType());
		Assert.assertTrue(names.seekName("m") >= 0);
		
		try{ScriptParserUtil.buildDeclarations(buildLex("a int", names), 0, names, root);
			Assert.fail("Mandatory exception was not detected (missing ':')");
		} catch (SyntaxException exc) {
		}
		try{ScriptParserUtil.buildDeclarations(buildLex("a : unknown", names), 0, names, root);
			Assert.fail("Mandatory exception was not detected (unknown type)");
		} catch (SyntaxException exc) {
		}
		try{ScriptParserUtil.buildDeclarations(buildLex("i : int, i: int", names), 0, names, root);
			Assert.fail("Mandatory exception was not detected (var was already declared)");
		} catch (SyntaxException exc) {
		}
	}	

	@Test
	public void statementsTest() throws SyntaxException {
		final SyntaxTreeInterface<EntityDescriptor>	names = new AndOrTree<>();
		final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	root = new SyntaxNode<>(0, 0, SyntaxNodeType.ROOT, 0, null);

		ScriptParserUtil.buildDeclarations(buildLex("k : int", names), 0, names, root);
		
		ScriptParserUtil.buildStatement(buildLex("k := 10", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.STRONG_BINARY, root.getType());
		
		ScriptParserUtil.buildStatement(buildLex("", names), 0, names, root);
		Assert.assertEquals(SyntaxNodeType.STRONG_BINARY, root.getType());
	}	
	
	private Lexema[] buildLex(final String string, final SyntaxTreeInterface<?> names) throws SyntaxException {
		final List<Lexema>	result = ScriptParserUtil.parseLex(new StringReader(string), names, true);
		
		return result.toArray(new Lexema[result.size()]);
	}	
}
