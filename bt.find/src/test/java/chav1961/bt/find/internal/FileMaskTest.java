package chav1961.bt.find.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import chav1961.bt.find.internal.FileMask.ExprPriority;
import chav1961.bt.find.internal.FileMask.LexType;
import chav1961.bt.find.internal.FileMask.Lexema;
import chav1961.bt.find.internal.FileMask.Operand;
import chav1961.bt.find.internal.FileMask.OperandType;
import chav1961.bt.find.internal.FileMask.SyntaxNodeType;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

public class FileMaskTest {
	private static final File	TEST_DIR = new File(System.getProperty("java.io.tmpdir"),"filemasktest");

	@BeforeClass
	public static void prepare() throws IOException {
		TEST_DIR.mkdirs();
		try(final InputStream		is = FileMaskTest.class.getResourceAsStream("resource.zip");
			final ZipInputStream	zis = new ZipInputStream(is)) {
		
			ZipEntry	ze;
			while ((ze = zis.getNextEntry()) != null) {
				final String	name = ze.getName();
				final File		outFile = new File(TEST_DIR, name);
				
				if (name.endsWith("/")) {
					outFile.mkdirs();
				}
				else {
					outFile.getParentFile().mkdirs();
					try(final OutputStream	os = new FileOutputStream(outFile)) {
						Utils.copyStream(zis, os);
					}
				}
			}
		}
	}

	@AfterClass
	public static void unprepare() {
		Utils.deleteDir(TEST_DIR);
	}
	
	
	@Test
	public void lexParserTest() throws SyntaxException {
		final SyntaxTreeInterface<String>	names = FileMask.prepareSyntaxTree();
		
		Lexema[] result = FileMask.parse(CharUtils.terminateAndConvert2CharArray("/(){|}[]+-", '\n'), names);
		
		Assert.assertEquals(11, result.length);
		Assert.assertTrue(allMatches(result, LexType.SEPARATOR, LexType.OPEN, LexType.CLOSE, LexType.START_ALTER, LexType.ALTER, LexType.END_ALTER, LexType.START_EXPR, LexType.END_EXPR, LexType.ADD, LexType.SUBTRACT, LexType.EOF));

		result = FileMask.parse(CharUtils.terminateAndConvert2CharArray(">>=<<==<>", '\n'), names);
		Assert.assertTrue(allMatches(result, LexType.COMPARE, LexType.COMPARE, LexType.COMPARE, LexType.COMPARE, LexType.COMPARE, LexType.COMPARE, LexType.EOF));

		result = FileMask.parse(CharUtils.terminateAndConvert2CharArray("/*/**/*a?/a*.*/*.?a/a/", '\n'), names);
		Assert.assertTrue(allMatches(result, LexType.SEPARATOR, LexType.ANY_NAME, LexType.SEPARATOR, LexType.ANY_SUBTREE, LexType.SEPARATOR, LexType.WILDCARD_NAME, LexType.SEPARATOR, LexType.WILDCARD_NAME, LexType.SEPARATOR, LexType.WILDCARD_NAME, LexType.SEPARATOR, LexType.ORDINAL_NAME, LexType.SEPARATOR, LexType.EOF));
		Assert.assertEquals("*a?",names.getName(result[5].id));
		Assert.assertEquals("a*.*",names.getName(result[7].id));
		Assert.assertEquals("*.?a",names.getName(result[9].id));
		Assert.assertEquals("a",names.getName(result[11].id));

		result = FileMask.parse(CharUtils.terminateAndConvert2CharArray("./.", '\n'), names);
		Assert.assertTrue(allMatches(result, LexType.DOT_SEPARATOR, LexType.ORDINAL_NAME, LexType.EOF));
		
		result = FileMask.parse(CharUtils.terminateAndConvert2CharArray("../..", '\n'), names);
		Assert.assertTrue(allMatches(result, LexType.DOT_SEPARATOR, LexType.ORDINAL_NAME, LexType.EOF));
		
		result = FileMask.parse(CharUtils.terminateAndConvert2CharArray("[||&&!length 10 10k 10M 10G]", '\n'), names);
		Assert.assertTrue(allMatches(result, LexType.START_EXPR, LexType.OR, LexType.AND, LexType.NOT, LexType.PREDEFINED, LexType.CONST, LexType.CONST, LexType.CONST, LexType.CONST, LexType.END_EXPR, LexType.EOF));
		
		try{FileMask.parse(CharUtils.terminateAndConvert2CharArray("&", '\n'), names);
			Assert.fail("Mandatory exception was not detected (unknown lexema)");
		} catch (SyntaxException exc) {
		}
		try{FileMask.parse(CharUtils.terminateAndConvert2CharArray("[mzinana]", '\n'), names);
			Assert.fail("Mandatory exception was not detected (unknown lexema)");
		} catch (SyntaxException exc) {
		}
		try{FileMask.parse(CharUtils.terminateAndConvert2CharArray("[*]", '\n'), names);
			Assert.fail("Mandatory exception was not detected (unknown lexema)");
		} catch (SyntaxException exc) {
		}
		try{FileMask.parse(CharUtils.terminateAndConvert2CharArray("[/]", '\n'), names);
			Assert.fail("Mandatory exception was not detected (unknown lexema)");
		} catch (SyntaxException exc) {
		}
	}
	
	@Test
	public void buildExpressionTest() throws SyntaxException {
		SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	node;
		
		node = buildExpression("[10]"); 
		Assert.assertEquals(SyntaxNodeType.CONSTANT, node.type);
		Assert.assertEquals(10, node.value);

		node = buildExpression("[(10)]"); 
		Assert.assertEquals(SyntaxNodeType.CONSTANT, node.type);
		Assert.assertEquals(10, node.value);
		
		try{
			buildExpression("[(10]");
			Assert.fail("Mandatory exception was not detected (missing ')'");
		} catch (SyntaxException exc) {
			Assert.assertEquals(4, exc.getCol());
		}
		
		node = buildExpression("[10M]"); 
		Assert.assertEquals(SyntaxNodeType.CONSTANT, node.type);
		Assert.assertEquals(10*1024*1024, node.value);
		
		node = buildExpression("[length]"); 
		Assert.assertEquals(SyntaxNodeType.PREDEFINED, node.type);
		
		node = buildExpression("[-10]"); 
		Assert.assertEquals(SyntaxNodeType.MINUS, node.type);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(1, node.children.length);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, node.children[0].type);

		node = buildExpression("[10+20]"); 
		Assert.assertEquals(SyntaxNodeType.ADD, node.type);
		Assert.assertEquals(0, node.value);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, node.children[0].type);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, node.children[1].type);

		node = buildExpression("[10-20]"); 
		Assert.assertEquals(SyntaxNodeType.ADD, node.type);
		Assert.assertEquals(2, node.value);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, node.children[0].type);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, node.children[1].type);

		node = buildExpression("[10>20]"); 
		Assert.assertEquals(SyntaxNodeType.COMPARE, node.type);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, node.children[0].type);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, node.children[1].type);

		node = buildExpression("[!10]"); 
		Assert.assertEquals(SyntaxNodeType.NOT, node.type);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(1, node.children.length);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, node.children[0].type);

		node = buildExpression("[10&&20]"); 
		Assert.assertEquals(SyntaxNodeType.AND, node.type);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, node.children[0].type);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, node.children[1].type);

		node = buildExpression("[10||20]"); 
		Assert.assertEquals(SyntaxNodeType.OR, node.type);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, node.children[0].type);
		Assert.assertEquals(SyntaxNodeType.CONSTANT, node.children[1].type);
	}	

	@Test
	public void calculateExpressionTest() throws SyntaxException {
		Operand 	result;
		
		result = calculateExpression("[10]");
		Assert.assertEquals(OperandType.NUMERIC, result.getType());
		Assert.assertEquals(10, ((Long)result.getValue()).longValue());

		result = calculateExpression("[-10]");
		Assert.assertEquals(OperandType.NUMERIC, result.getType());
		Assert.assertEquals(-10, ((Long)result.getValue()).longValue());

		result = calculateExpression("[10 + 20]");
		Assert.assertEquals(OperandType.NUMERIC, result.getType());
		Assert.assertEquals(30, ((Long)result.getValue()).longValue());

		result = calculateExpression("[10 - 20]");
		Assert.assertEquals(OperandType.NUMERIC, result.getType());
		Assert.assertEquals(-10, ((Long)result.getValue()).longValue());

		result = calculateExpression("[10 > 20]");
		Assert.assertEquals(OperandType.BOOLEAN, result.getType());
		Assert.assertFalse(((Boolean)result.getValue()).booleanValue());
		result = calculateExpression("[10 >= 20]");
		Assert.assertEquals(OperandType.BOOLEAN, result.getType());
		Assert.assertFalse(((Boolean)result.getValue()).booleanValue());
		result = calculateExpression("[10 < 20]");
		Assert.assertEquals(OperandType.BOOLEAN, result.getType());
		Assert.assertTrue(((Boolean)result.getValue()).booleanValue());
		result = calculateExpression("[10 <= 20]");
		Assert.assertEquals(OperandType.BOOLEAN, result.getType());
		Assert.assertTrue(((Boolean)result.getValue()).booleanValue());
		result = calculateExpression("[10 = 20]");
		Assert.assertEquals(OperandType.BOOLEAN, result.getType());
		Assert.assertFalse(((Boolean)result.getValue()).booleanValue());
		result = calculateExpression("[10 <> 20]");
		Assert.assertEquals(OperandType.BOOLEAN, result.getType());
		Assert.assertTrue(((Boolean)result.getValue()).booleanValue());
		
		result = calculateExpression("[!10 <> 20]");
		Assert.assertEquals(OperandType.BOOLEAN, result.getType());
		Assert.assertFalse(((Boolean)result.getValue()).booleanValue());
		
		result = calculateExpression("[10 <> 20 && 10 <> 30]");
		Assert.assertEquals(OperandType.BOOLEAN, result.getType());
		Assert.assertTrue(((Boolean)result.getValue()).booleanValue());
		result = calculateExpression("[10 <> 20 && 10 <> 10]");
		Assert.assertEquals(OperandType.BOOLEAN, result.getType());
		Assert.assertFalse(((Boolean)result.getValue()).booleanValue());

		result = calculateExpression("[10 <> 20 || 10 <> 30]");
		Assert.assertEquals(OperandType.BOOLEAN, result.getType());
		Assert.assertTrue(((Boolean)result.getValue()).booleanValue());
		result = calculateExpression("[10 <> 10 || 10 <> 10]");
		Assert.assertEquals(OperandType.BOOLEAN, result.getType());
		Assert.assertFalse(((Boolean)result.getValue()).booleanValue());
	}	
	
	@Test
	public void buildCurrentNameTest() throws SyntaxException {
		SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	node;

		node = buildCurrentName("/*");
		Assert.assertEquals(SyntaxNodeType.ANY_NAME, node.type);
		Assert.assertNull(node.cargo);
		Assert.assertNull(node.children);
		
		node = buildCurrentName("/**");
		Assert.assertEquals(SyntaxNodeType.ANY_SUBTREE, node.type);
		Assert.assertNull(node.cargo);
		Assert.assertNull(node.children);
		
		node = buildCurrentName("/*a*");
		Assert.assertEquals(SyntaxNodeType.WILDCARD_NAME, node.type);
		Assert.assertNull(node.cargo);
		Assert.assertNull(node.children);

		node = buildCurrentName("/a");
		Assert.assertEquals(SyntaxNodeType.ORDINAL_NAME, node.type);
		Assert.assertNull(node.cargo);
		Assert.assertNull(node.children);

		node = buildCurrentName("/a[10 = 10]");
		Assert.assertEquals(SyntaxNodeType.ORDINAL_NAME, node.type);
		Assert.assertNotNull(node.cargo);
		Assert.assertNull(node.children);

		node = buildCurrentName("/{a|b/c|d[10 = 20]}");
		Assert.assertEquals(SyntaxNodeType.LIST, node.type);
		Assert.assertNull(node.cargo);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(4, node.children.length);
		Assert.assertNull(node.children[0]);
		Assert.assertEquals(SyntaxNodeType.ORDINAL_NAME, node.children[1].type);
		Assert.assertEquals(SyntaxNodeType.ORDINAL_NAME, node.children[2].type);
		Assert.assertEquals(SyntaxNodeType.ORDINAL_NAME, node.children[3].type);
	
		node = buildCurrentName("/{a|b/c|d}[10 = 20]");
		Assert.assertEquals(SyntaxNodeType.LIST, node.type);
		Assert.assertNotNull(node.cargo);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(4, node.children.length);
		Assert.assertNull(node.children[0]);
		Assert.assertEquals(SyntaxNodeType.ORDINAL_NAME, node.children[1].type);
		Assert.assertEquals(SyntaxNodeType.ORDINAL_NAME, node.children[2].type);
		Assert.assertEquals(SyntaxNodeType.ORDINAL_NAME, node.children[3].type);

		node = buildCurrentName("/*/*");
		Assert.assertEquals(SyntaxNodeType.ANY_NAME, node.type);
		Assert.assertNull(node.cargo);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(1, node.children.length);
		Assert.assertEquals(SyntaxNodeType.ANY_NAME, node.children[0].type);

		node = buildCurrentName("/{a|b/c|d}/*/**");
		Assert.assertEquals(SyntaxNodeType.LIST, node.type);
		Assert.assertNull(node.cargo);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(4, node.children.length);
		Assert.assertEquals(SyntaxNodeType.ANY_NAME, node.children[0].type);
		Assert.assertEquals(SyntaxNodeType.ORDINAL_NAME, node.children[1].type);
		Assert.assertEquals(SyntaxNodeType.ORDINAL_NAME, node.children[2].type);
		Assert.assertEquals(SyntaxNodeType.ORDINAL_NAME, node.children[3].type);
	}

	@Test
	public void walkTemplateTest() throws SyntaxException {
		List<File>	found;
		
		found = walkTemplate(TEST_DIR, "./s.txt");
		Assert.assertEquals(1, found.size());
		Assert.assertEquals("s.txt", found.get(0).getName());

		found = walkTemplate(TEST_DIR, "./{s.txt|t.txt}");
		Assert.assertEquals(1, found.size());
		Assert.assertEquals("s.txt", found.get(0).getName());
		
		found = walkTemplate(TEST_DIR, "./*/t.txt");
		Assert.assertEquals(1, found.size());
		Assert.assertEquals("t.txt", found.get(0).getName());

		found = walkTemplate(TEST_DIR, "./subdir1/t.txt");
		Assert.assertEquals(1, found.size());
		Assert.assertEquals("t.txt", found.get(0).getName());

		found = walkTemplate(TEST_DIR, "./*1/t.txt");
		Assert.assertEquals(1, found.size());
		Assert.assertEquals("t.txt", found.get(0).getName());

		found = walkTemplate(TEST_DIR, "./**/t.txt");
		Assert.assertEquals(1, found.size());
		Assert.assertEquals("t.txt", found.get(0).getName());

		found = walkTemplate(TEST_DIR, "./**/{s.txt|t.txt}");
		Assert.assertEquals(2, found.size());
		Assert.assertEquals("s.txt", found.get(0).getName());
		Assert.assertEquals("t.txt", found.get(1).getName());

		found = walkTemplate(TEST_DIR, "./**/{s.txt|*}/t.txt");
		Assert.assertEquals(2, found.size());
		Assert.assertEquals("s.txt", found.get(0).getName());
		Assert.assertEquals("t.txt", found.get(1).getName());
		
		found = walkTemplate(TEST_DIR, "./**/t.txt[length<1000]");
		Assert.assertEquals(1, found.size());
		Assert.assertEquals("t.txt", found.get(0).getName());

		found = walkTemplate(TEST_DIR, "./**/t.txt[length>1000]");
		Assert.assertEquals(0, found.size());
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
		final Lexema[]						lex = FileMask.parse(CharUtils.terminateAndConvert2CharArray(expr, '\n'), names); 
		
		FileMask.buildExpression(lex, 1, ExprPriority.OR, node);
		return node;
	}

	private static SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>> buildCurrentName(final String expr) throws SyntaxException {
		final SyntaxTreeInterface<String>	names = FileMask.prepareSyntaxTree();
		final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	node = new SyntaxNode<>(0, 0, SyntaxNodeType.ROOT, 0, null);
		final Lexema[]						lex = FileMask.parse(CharUtils.terminateAndConvert2CharArray(expr, '\n'), names); 
		
		FileMask.buildCurrentName(lex, 1, node);
		return node;
	}
	
	private static Operand calculateExpression(final String expr) throws SyntaxException {
		final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	node = buildExpression(expr);
		final File	file = new File("test.txt");
	
		return FileMask.calculateExpr((s)->calc(file, s), node);
	}
	
	private static List<File> walkTemplate(final File file, final String expr) throws SyntaxException {
		final SyntaxTreeInterface<String>	names = FileMask.prepareSyntaxTree();
		final SyntaxNode<SyntaxNodeType, SyntaxNode<?, ?>>	node = new SyntaxNode<>(0, 0, SyntaxNodeType.ROOT, 0, null);
		final Lexema[]						lex = FileMask.parse(CharUtils.terminateAndConvert2CharArray(expr, '\n'), names); 
		final List<File>					result = new ArrayList<>();
	
		FileMask.buildCurrentName(lex, 1, node);
		FileMask.walk(file, node, names, (f)->result.add(f));
		return result;
	}
	
	private static Operand calc(final File file, final String predefined) {
		switch (predefined) {
			case "length" :
				return new Operand(file.length());
			case "lastUpdate" :
				return new Operand(file.lastModified());
			case "canRead" :
				return new Operand(file.canRead());
			case "canWrite" :
				return new Operand(file.canWrite());
			case "canExecute" :
				return new Operand(file.canExecute());
			default :
				return new Operand(false);
		}
	}
	
}
