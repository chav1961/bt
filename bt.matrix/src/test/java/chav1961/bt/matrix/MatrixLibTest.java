package chav1961.bt.matrix;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.matrix.MatrixLib.Command;
import chav1961.bt.matrix.MatrixLib.LexType;
import chav1961.bt.matrix.MatrixLib.Lexema;
import chav1961.bt.matrix.MatrixLib.OperType;
import chav1961.bt.matrix.MatrixLib.Operation;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.SyntaxNode;

public class MatrixLibTest {
	@Test
	public void basicTest() {
		try(final MatrixLib	lib = MatrixLib.getInstance()) {
			Assert.assertNotNull(lib);
		}
	}
	
	@Test
	public void getMatrixTest() {
		try(final MatrixLib	lib = MatrixLib.getInstance()) {
			
			// test zero matrix
			
			final Matrix	zero = lib.getZeroMatrix(10, 10);
			
			Assert.assertEquals(10, zero.numberOfRows());
			Assert.assertEquals(10, zero.numberOfColumns());
			
			final float[]	content = zero.extractFloats();
			
			Assert.assertEquals(100, content.length);
			
			for(float item : content) {
				Assert.assertEquals(0f,  item, 0.0001f);
			}
			
			zero.close();
			
			try {
				lib.getZeroMatrix(0, 10);
				Assert.fail("Mandatory exception was not detected (zero 1-st argument)");
			} catch (IllegalArgumentException exc) {				
			}
			try {
				lib.getZeroMatrix(10, 0);
				Assert.fail("Mandatory exception was not detected (zero 2-nd argument)");
			} catch (IllegalArgumentException exc) {				
			}
			
			// test identity matrix
			
			final Matrix	identity = lib.getIdentityMatrix(10, 10);
			
			Assert.assertEquals(10, identity.numberOfRows());
			Assert.assertEquals(10, identity.numberOfColumns());
			
			final float[]	content2 = identity.extractFloats();
			
			Assert.assertEquals(100, content2.length);
			
			for(int i = 0; i < identity.numberOfRows(); i++) {
				for(int j = 0; j < identity.numberOfColumns(); j++) {
					final float	val = content2[i*identity.numberOfColumns() + j];
					
					Assert.assertEquals(i == j ? 1f : 0f,  val, 0.0001f);
				}
			}
			
			identity.close();

			try {
				lib.getIdentityMatrix(0, 10);
				Assert.fail("Mandatory exception was not detected (zero 1-st argument)");
			} catch (IllegalArgumentException exc) {				
			}
			try {
				lib.getIdentityMatrix(10, 0);
				Assert.fail("Mandatory exception was not detected (zero 2-nd argument)");
			} catch (IllegalArgumentException exc) {				
			}
			
			// test any matrix			
			
			final Matrix	any = lib.getIdentityMatrix(10, 10);
			
			Assert.assertEquals(10, any.numberOfRows());
			Assert.assertEquals(10, any.numberOfColumns());
			
			final float[]	content3 = any.extractFloats();
			
			Assert.assertEquals(100, content3.length);
			
			any.assign(new float[] {10, 20, 30});
			
			final float[]	content4 = any.extractFloats();
			
			Assert.assertEquals(10, content4[0], 0.0001f);
			Assert.assertEquals(20, content4[1], 0.0001f);
			Assert.assertEquals(30, content4[2], 0.0001f);
			Assert.assertEquals(0, content4[3], 0.0001f);
			
			any.close();
			
			try {
				lib.getMatrix(0, 10);
				Assert.fail("Mandatory exception was not detected (zero 1-st argument)");
			} catch (IllegalArgumentException exc) {				
			}
			try {
				lib.getMatrix(10, 0);
				Assert.fail("Mandatory exception was not detected (zero 2-nd argument)");
			} catch (IllegalArgumentException exc) {				
			}
			
		}
	}

	@Test
	public void parseTest() throws SyntaxException {
		final Lexema[]	parsed = MatrixLib.parse("+-* ** ***^22.5%1.T InV DeT Sp ()\0".toCharArray());
		final LexType[]	awaited = {LexType.PLUS, LexType.MINUS, LexType.MUL, LexType.MUL_H, LexType.MUL_K, LexType.POWER, LexType.REAL_VALUE, LexType.NAME, LexType.DOT, LexType.PREDEFINED, LexType.PREDEFINED, LexType.PREDEFINED, LexType.PREDEFINED, LexType.OPEN, LexType.CLOSE, LexType.EOF};
		
		Assert.assertEquals(16, parsed.length);
		for(int index = 0; index < parsed.length; index++) {
			Assert.assertEquals(awaited[index], parsed[index].type);
		}
		Assert.assertEquals(22.5, Double.longBitsToDouble(parsed[6].value), 0.0001);
		Assert.assertEquals(1, parsed[7].value);
		Assert.assertEquals(0, parsed[9].value);
		Assert.assertEquals(1, parsed[10].value);
		Assert.assertEquals(2, parsed[11].value);
		Assert.assertEquals(3, parsed[12].value);
		
		try {MatrixLib.parse("?\0".toCharArray());
			Assert.fail("Mandatory exception was not detected (unknown lexema)");
		} catch (SyntaxException exc) {
		}
		try {MatrixLib.parse("unknown\0".toCharArray());
			Assert.fail("Mandatory exception was not detected (unknown lexema)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void buildTreeTest() throws SyntaxException {
		SyntaxNode<Operation, SyntaxNode<?,?>>	node;
		
		// term processing
		
		node = buildTree("%1",1);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.getType());
		Assert.assertEquals(1, node.value);

		node = buildTree("(%1)",3);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.getType());
		Assert.assertEquals(1, node.value);
		
		node = buildTree("22.5",1);
		Assert.assertEquals(Operation.LOAD_REAL, node.getType());
		Assert.assertEquals(22.5, Double.longBitsToDouble(node.value), 0.0001);

		node = buildTree("%1.t",3);
		Assert.assertEquals(Operation.TRANSPOSE, node.getType());
		Assert.assertNotNull(node.children);
		Assert.assertEquals(1, node.children.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[0].getType());

		node = buildTree("%1.det",3);
		Assert.assertEquals(Operation.DET, node.getType());
		Assert.assertNotNull(node.children);
		Assert.assertEquals(1, node.children.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[0].getType());

		node = buildTree("%1.inv",3);
		Assert.assertEquals(Operation.INVERT, node.getType());
		Assert.assertNotNull(node.children);
		Assert.assertEquals(1, node.children.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[0].getType());

		node = buildTree("%1.sp",3);
		Assert.assertEquals(Operation.SPOOR, node.getType());
		Assert.assertNotNull(node.children);
		Assert.assertEquals(1, node.children.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[0].getType());

		// unary processing
		
		node = buildTree("-%1",2);
		Assert.assertEquals(Operation.NEGATE, node.getType());
		Assert.assertNotNull(node.children);
		Assert.assertEquals(1, node.children.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[0].getType());

		node = buildTree("-22.5",2);
		Assert.assertEquals(Operation.MINUS, node.getType());
		Assert.assertNotNull(node.children);
		Assert.assertEquals(1, node.children.length);
		Assert.assertEquals(Operation.LOAD_REAL, node.children[0].getType());

		node = buildTree("%1^2",3);
		Assert.assertEquals(Operation.POWER, node.getType());
		Assert.assertEquals(2, Double.longBitsToDouble(node.value), 0.0001);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(1, node.children.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[0].getType());

		node = buildTree("22.5^2",3);
		Assert.assertEquals(Operation.POWER, node.getType());
		Assert.assertEquals(2, Double.longBitsToDouble(node.value), 0.0001);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(1, node.children.length);
		Assert.assertEquals(Operation.LOAD_REAL, node.children[0].getType());

		// mul processing
		
		node = buildTree("%1*%2",3);
		Assert.assertEquals(Operation.MUL, node.getType());
		Assert.assertNotNull(node.cargo);
		Assert.assertEquals(1, ((LexType[])node.cargo).length);
		Assert.assertEquals(LexType.MUL, ((LexType[])node.cargo)[0]);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[0].getType());
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[1].getType());

		node = buildTree("%1**%2",3);
		Assert.assertEquals(Operation.MUL, node.getType());
		Assert.assertNotNull(node.cargo);
		Assert.assertEquals(1, ((LexType[])node.cargo).length);
		Assert.assertEquals(LexType.MUL_H, ((LexType[])node.cargo)[0]);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[0].getType());
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[1].getType());

		node = buildTree("%1***%2",3);
		Assert.assertEquals(Operation.MUL, node.getType());
		Assert.assertNotNull(node.cargo);
		Assert.assertEquals(1, ((LexType[])node.cargo).length);
		Assert.assertEquals(LexType.MUL_K, ((LexType[])node.cargo)[0]);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[0].getType());
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[1].getType());

		node = buildTree("%1*2.5",3);
		Assert.assertEquals(Operation.MUL, node.getType());
		Assert.assertNotNull(node.cargo);
		Assert.assertEquals(1, ((LexType[])node.cargo).length);
		Assert.assertEquals(LexType.MUL, ((LexType[])node.cargo)[0]);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[0].getType());
		Assert.assertEquals(Operation.LOAD_REAL, node.children[1].getType());

		node = buildTree("2.5*%1",3);
		Assert.assertEquals(Operation.MUL, node.getType());
		Assert.assertNotNull(node.cargo);
		Assert.assertEquals(1, ((LexType[])node.cargo).length);
		Assert.assertEquals(LexType.MUL, ((LexType[])node.cargo)[0]);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(Operation.LOAD_REAL, node.children[0].getType());
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[1].getType());

		// add processing
		
		node = buildTree("%1+%2",3);
		Assert.assertEquals(Operation.ADD, node.getType());
		Assert.assertNotNull(node.cargo);
		Assert.assertEquals(1, ((LexType[])node.cargo).length);
		Assert.assertEquals(LexType.PLUS, ((LexType[])node.cargo)[0]);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[0].getType());
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[1].getType());

		node = buildTree("%1-%2",3);
		Assert.assertEquals(Operation.ADD, node.getType());
		Assert.assertNotNull(node.cargo);
		Assert.assertEquals(1, ((LexType[])node.cargo).length);
		Assert.assertEquals(LexType.MINUS, ((LexType[])node.cargo)[0]);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[0].getType());
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[1].getType());

		node = buildTree("%1+2.5",3);
		Assert.assertEquals(Operation.ADD, node.getType());
		Assert.assertNotNull(node.cargo);
		Assert.assertEquals(1, ((LexType[])node.cargo).length);
		Assert.assertEquals(LexType.PLUS, ((LexType[])node.cargo)[0]);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[0].getType());
		Assert.assertEquals(Operation.LOAD_REAL, node.children[1].getType());

		node = buildTree("%1-2.5",3);
		Assert.assertEquals(Operation.ADD, node.getType());
		Assert.assertNotNull(node.cargo);
		Assert.assertEquals(1, ((LexType[])node.cargo).length);
		Assert.assertEquals(LexType.MINUS, ((LexType[])node.cargo)[0]);
		Assert.assertNotNull(node.children);
		Assert.assertEquals(2, node.children.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, node.children[0].getType());
		Assert.assertEquals(Operation.LOAD_REAL, node.children[1].getType());

		// error built
		
		try{buildTree("%1++",3);
			Assert.fail("Mandatory exception was not detceted (missing operand)");
		} catch (SyntaxException exc) {
		}
		try{buildTree("(%1",3);
			Assert.fail("Mandatory exception was not detceted (unclosed ')')");
		} catch (SyntaxException exc) {
		}
		try{buildTree("%1.+",3);
			Assert.fail("Mandatory exception was not detceted (missing predefined name)");
		} catch (SyntaxException exc) {
		}
		try{buildTree("%1^",3);
			Assert.fail("Mandatory exception was not detceted (missing number at the power right)");
		} catch (SyntaxException exc) {
		}
	}	

	@Test
	public void buildCommandsTest() throws SyntaxException {
		Command[]	cmds;
		
		// Term processing
		
		cmds = buildCommands("%1");
		Assert.assertEquals(1, cmds.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[0].op);
		Assert.assertEquals(1, cmds[0].operand);

		cmds = buildCommands("%1.t");
		Assert.assertEquals(2, cmds.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[0].op);
		Assert.assertEquals(Operation.TRANSPOSE, cmds[1].op);

		cmds = buildCommands("%1.det");
		Assert.assertEquals(2, cmds.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[0].op);
		Assert.assertEquals(Operation.DET, cmds[1].op);

		cmds = buildCommands("%1.inv");
		Assert.assertEquals(2, cmds.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[0].op);
		Assert.assertEquals(Operation.INVERT, cmds[1].op);

		cmds = buildCommands("%1.sp");
		Assert.assertEquals(2, cmds.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[0].op);
		Assert.assertEquals(Operation.SPOOR, cmds[1].op);

		// Unary processing

		cmds = buildCommands("-%1");
		Assert.assertEquals(2, cmds.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[0].op);
		Assert.assertEquals(Operation.NEGATE, cmds[1].op);

		cmds = buildCommands("-2.5");
		Assert.assertEquals(2, cmds.length);
		Assert.assertEquals(Operation.LOAD_REAL, cmds[0].op);
		Assert.assertEquals(Operation.MINUS, cmds[1].op);
		
		cmds = buildCommands("%1^2.5");
		Assert.assertEquals(2, cmds.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[0].op);
		Assert.assertEquals(Operation.POWER, cmds[1].op);
		Assert.assertEquals(2.5, Double.longBitsToDouble(cmds[1].operand), 0.0001);

		cmds = buildCommands("3^2.5");
		Assert.assertEquals(2, cmds.length);
		Assert.assertEquals(Operation.LOAD_REAL, cmds[0].op);
		Assert.assertEquals(3, Double.longBitsToDouble(cmds[0].operand), 0.0001);
		Assert.assertEquals(Operation.POWER_VAL, cmds[1].op);
		Assert.assertEquals(2.5, Double.longBitsToDouble(cmds[1].operand), 0.0001);

		// Mul processing

		cmds = buildCommands("%1 * %2");
		Assert.assertEquals(3, cmds.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[0].op);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[1].op);
		Assert.assertEquals(Operation.MUL, cmds[2].op);
		
		cmds = buildCommands("2.5 * %2");
		Assert.assertEquals(3, cmds.length);
		Assert.assertEquals(Operation.LOAD_REAL, cmds[0].op);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[1].op);
		Assert.assertEquals(Operation.MUL_VAL_MATRIX, cmds[2].op);

		cmds = buildCommands("%2 * 2.5");
		Assert.assertEquals(3, cmds.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[0].op);
		Assert.assertEquals(Operation.LOAD_REAL, cmds[1].op);
		Assert.assertEquals(Operation.MUL_MATRIX_VAL, cmds[2].op);

		cmds = buildCommands("%1 ** %2");
		Assert.assertEquals(3, cmds.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[0].op);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[1].op);
		Assert.assertEquals(Operation.MUL_H, cmds[2].op);

		cmds = buildCommands("%1 *** %2");
		Assert.assertEquals(3, cmds.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[0].op);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[1].op);
		Assert.assertEquals(Operation.MUL_K, cmds[2].op);

		// Add processing

		cmds = buildCommands("%1 + %2");
		Assert.assertEquals(3, cmds.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[0].op);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[1].op);
		Assert.assertEquals(Operation.ADD, cmds[2].op);

		cmds = buildCommands("%1 + 2.5");
		Assert.assertEquals(3, cmds.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[0].op);
		Assert.assertEquals(Operation.LOAD_REAL, cmds[1].op);
		Assert.assertEquals(Operation.ADD_MATRIX_VAL, cmds[2].op);

		cmds = buildCommands("2.5 + %2");
		Assert.assertEquals(3, cmds.length);
		Assert.assertEquals(Operation.LOAD_REAL, cmds[0].op);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[1].op);
		Assert.assertEquals(Operation.ADD_VAL_MATRIX, cmds[2].op);

		cmds = buildCommands("1 + 2");
		Assert.assertEquals(3, cmds.length);
		Assert.assertEquals(Operation.LOAD_REAL, cmds[0].op);
		Assert.assertEquals(Operation.LOAD_REAL, cmds[1].op);
		Assert.assertEquals(Operation.ADD_VAL, cmds[2].op);

		cmds = buildCommands("%1 - %2");
		Assert.assertEquals(3, cmds.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[0].op);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[1].op);
		Assert.assertEquals(Operation.SUB, cmds[2].op);

		cmds = buildCommands("%1 - 2.5");
		Assert.assertEquals(3, cmds.length);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[0].op);
		Assert.assertEquals(Operation.LOAD_REAL, cmds[1].op);
		Assert.assertEquals(Operation.SUB_MATRIX_VAL, cmds[2].op);

		cmds = buildCommands("2.5 - %2");
		Assert.assertEquals(3, cmds.length);
		Assert.assertEquals(Operation.LOAD_REAL, cmds[0].op);
		Assert.assertEquals(Operation.LOAD_MATRIX, cmds[1].op);
		Assert.assertEquals(Operation.SUB_VAL_MATRIX, cmds[2].op);

		cmds = buildCommands("1 - 2");
		Assert.assertEquals(3, cmds.length);
		Assert.assertEquals(Operation.LOAD_REAL, cmds[0].op);
		Assert.assertEquals(Operation.LOAD_REAL, cmds[1].op);
		Assert.assertEquals(Operation.SUB_VAL, cmds[2].op);
	}	
	
	@Test
	public void complexTest() throws SyntaxException {
		final Calculator	calc = MatrixLib.compile("%1+%2");
		
		
	}
	
	private SyntaxNode<Operation, SyntaxNode<?,?>> buildTree(final String expr, final int tail) throws SyntaxException {
		final Lexema[]	parsed = MatrixLib.parse(CharUtils.terminateAndConvert2CharArray(expr, '\0'));
		final SyntaxNode<Operation, SyntaxNode<?,?>>	root = new SyntaxNode<>(0, 0, Operation.UNKNOWN, 0, null);
		
		Assert.assertEquals(tail, MatrixLib.buildTree(OperType.ADD, parsed, 0, root));
		return root;
	}

	private Command[] buildCommands(final String expr) throws SyntaxException {
		final Lexema[]		parsed = MatrixLib.parse(CharUtils.terminateAndConvert2CharArray(expr, '\0'));
		final SyntaxNode<Operation, SyntaxNode<?,?>>	root = new SyntaxNode<>(0, 0, Operation.UNKNOWN, 0, null);
		final int			stopped = MatrixLib.buildTree(OperType.ADD, parsed, 0, root);
		final List<Command>	result = new ArrayList<>();
		
		MatrixLib.buildCommands(root, result);
		Assert.assertEquals(LexType.EOF, parsed[stopped].type);
		return result.toArray(new Command[result.size()]);
	}
}
