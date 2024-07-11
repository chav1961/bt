package chav1961.bt.matrix.macros.runtime;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.matrix.macros.runtime.CommandList.CommandType;
import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;
import chav1961.bt.matrix.macros.runtime.interfaces.Value;
import chav1961.bt.matrix.macros.runtime.interfaces.Value.ValueType;
import chav1961.bt.matrix.macros.runtime.interfaces.ValueArray;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;

public class ThreadedCommandListTest {
	@Test
	public void constTest() throws CalculationException, ContentException {
		final ThreadedCommandList	tcl = new ThreadedCommandList();
		final MacrosRuntime			mrt = new SingleMacrosRuntime();
		
		tcl.addCommand(CommandType.CONST_BOOLEAN, true);
		tcl.build().execute(mrt);
		Assert.assertEquals(Value.Factory.TRUE, mrt.getProgramStack().popStackValue());

		tcl.addCommand(CommandType.CONST_INT, 100);
		tcl.build().execute(mrt);
		Assert.assertEquals(Value.Factory.newReadOnlyInstance(100), mrt.getProgramStack().popStackValue());

		tcl.addCommand(CommandType.CONST_REAL, 100.0);
		tcl.build().execute(mrt);
		Assert.assertEquals(Value.Factory.newReadOnlyInstance(100.0), mrt.getProgramStack().popStackValue());

		tcl.addCommand(CommandType.CONST_CHAR, "vassya");
		tcl.build().execute(mrt);
		Assert.assertEquals(Value.Factory.newReadOnlyInstance("vassya"), mrt.getProgramStack().popStackValue());
	}

	@Test
	public void varAccessTest() throws CalculationException, ContentException {
		final ThreadedCommandList	tcl = new ThreadedCommandList();
		final MacrosRuntime			mrt = new SingleMacrosRuntime();
	
		mrt.getProgramStack().declare(1, ValueType.INT);
		mrt.getProgramStack().setVarValue(1, Value.Factory.newReadOnlyInstance(100));
		
		tcl.addCommand(CommandType.LOAD_VAR, 1).build().execute(mrt);
		Assert.assertEquals(100, mrt.getProgramStack().popStackValue().getValue(long.class).longValue());

		mrt.getProgramStack().declare(2, ValueType.INT_ARRAY);
		mrt.getProgramStack().setVarValue(2, ValueArray.Factory.newReadOnlyInstance(new long[] {100}));
	
		tcl.addCommand(CommandType.CONST_INT, 0).addCommand(CommandType.LOAD_INDEX, 2).build().execute(mrt);
		Assert.assertEquals(100, mrt.getProgramStack().popStackValue().getValue(long.class).longValue());
	}	
	
	@Test
	public void unaryTest() throws CalculationException, ContentException {
		final ThreadedCommandList	tcl = new ThreadedCommandList();
		final MacrosRuntime			mrt = new SingleMacrosRuntime();
		
		tcl.addCommand(CommandType.CONST_INT, 100).addCommand(CommandType.NEGATE).build().execute(mrt);
		Assert.assertEquals(-100, mrt.getProgramStack().popStackValue().getValue(long.class).longValue());
		
		tcl.addCommand(CommandType.CONST_REAL, 100).addCommand(CommandType.NEGATE).build().execute(mrt);
		Assert.assertEquals(-100, mrt.getProgramStack().popStackValue().getValue(double.class).doubleValue(), 0.001);

		try {
			tcl.addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.NEGATE).build().execute(mrt);
			Assert.fail("Mandatory exception was not detected (non-number to negate)");
		} catch (ContentException | CalculationException exc) {
		}

		tcl.addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.NOT).build().execute(mrt);
		Assert.assertFalse(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_BOOLEAN, false).addCommand(CommandType.NOT).build().execute(mrt);
		Assert.assertTrue(mrt.getProgramStack().popStackValue().getValue(boolean.class));
		
		try {
			tcl.addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.NOT).build().execute(mrt);
			Assert.fail("Mandatory exception was not detected (non-boolean to NOT)");
		} catch (ContentException | CalculationException exc) {
		}
	}
	
	
	@Test
	public void multiplyTest() throws CalculationException, ContentException {
		final ThreadedCommandList	tcl = new ThreadedCommandList();
		final MacrosRuntime			mrt = new SingleMacrosRuntime();
		
		tcl.addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.CONST_INT, 20).addCommand(CommandType.MUL).build().execute(mrt);
		Assert.assertEquals(200, mrt.getProgramStack().popStackValue().getValue(long.class).longValue());
		
		tcl.addCommand(CommandType.CONST_INT, 20).addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.DIV).build().execute(mrt);
		Assert.assertEquals(2, mrt.getProgramStack().popStackValue().getValue(long.class).longValue());

		tcl.addCommand(CommandType.CONST_INT, 15).addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.MOD).build().execute(mrt);
		Assert.assertEquals(5, mrt.getProgramStack().popStackValue().getValue(long.class).longValue());

		tcl.addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.CONST_REAL, 20).addCommand(CommandType.MUL).build().execute(mrt);
		Assert.assertEquals(200, mrt.getProgramStack().popStackValue().getValue(double.class).doubleValue(), 0.001);
		
		tcl.addCommand(CommandType.CONST_REAL, 20).addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.DIV).build().execute(mrt);
		Assert.assertEquals(2, mrt.getProgramStack().popStackValue().getValue(double.class).doubleValue(), 0.001);

		tcl.addCommand(CommandType.CONST_REAL, 15).addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.MOD).build().execute(mrt);
		Assert.assertEquals(5, mrt.getProgramStack().popStackValue().getValue(double.class).doubleValue(), 0.001);

		try {
			tcl.addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.MUL).build().execute(mrt);
			Assert.fail("Mandatory exception was not detected (non-number to NUL)");
		} catch (ContentException | CalculationException exc) {
		}
		try {
			tcl.addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.DIV).build().execute(mrt);
			Assert.fail("Mandatory exception was not detected (non-number to NUL)");
		} catch (ContentException | CalculationException exc) {
		}
		try {
			tcl.addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.MOD).build().execute(mrt);
			Assert.fail("Mandatory exception was not detected (non-number to MOD)");
		} catch (ContentException | CalculationException exc) {
		}
	}
	
	@Test
	public void addTest() throws CalculationException, ContentException {
		final ThreadedCommandList	tcl = new ThreadedCommandList();
		final MacrosRuntime			mrt = new SingleMacrosRuntime();
		
		tcl.addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.CONST_INT, 20).addCommand(CommandType.ADD).build().execute(mrt);
		Assert.assertEquals(30, mrt.getProgramStack().popStackValue().getValue(long.class).longValue());

		tcl.addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.CONST_INT, 20).addCommand(CommandType.SUB).build().execute(mrt);
		Assert.assertEquals(-10, mrt.getProgramStack().popStackValue().getValue(long.class).longValue());

		tcl.addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.CONST_INT, 20).addCommand(CommandType.ADD).build().execute(mrt);
		Assert.assertEquals(30, mrt.getProgramStack().popStackValue().getValue(double.class).doubleValue(), 0.001);

		tcl.addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.CONST_INT, 20).addCommand(CommandType.SUB).build().execute(mrt);
		Assert.assertEquals(-10, mrt.getProgramStack().popStackValue().getValue(double.class).doubleValue(), 0.001);

		tcl.addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.CONST_CHAR, " thousands").addCommand(CommandType.ADD).build().execute(mrt);
		Assert.assertEquals("10 thousands", new String(mrt.getProgramStack().popStackValue().getValue(char[].class)));
		
		try {
			tcl.addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.ADD).build().execute(mrt);
			Assert.fail("Mandatory exception was not detected (non-number to ADD or CONCAT failed)");
		} catch (ContentException | CalculationException exc) {
		}
		try {
			tcl.addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.SUB).build().execute(mrt);
			Assert.fail("Mandatory exception was not detected (non-number to SUB)");
		} catch (ContentException | CalculationException exc) {
		}
	}	

	@Test
	public void compareTest() throws CalculationException, ContentException {
		final ThreadedCommandList	tcl = new ThreadedCommandList();
		final MacrosRuntime			mrt = new SingleMacrosRuntime();

		// int
		
		tcl.addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.EQ).build().execute(mrt);
		Assert.assertTrue(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.NE).build().execute(mrt);
		Assert.assertFalse(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.GE).build().execute(mrt);
		Assert.assertTrue(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.GT).build().execute(mrt);
		Assert.assertFalse(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.LE).build().execute(mrt);
		Assert.assertTrue(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.CONST_INT, 10).addCommand(CommandType.LT).build().execute(mrt);
		Assert.assertFalse(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		// real
		
		tcl.addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.EQ).build().execute(mrt);
		Assert.assertTrue(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.NE).build().execute(mrt);
		Assert.assertFalse(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.GE).build().execute(mrt);
		Assert.assertTrue(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.GT).build().execute(mrt);
		Assert.assertFalse(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.LE).build().execute(mrt);
		Assert.assertTrue(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.LT).build().execute(mrt);
		Assert.assertFalse(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		// str
	
		tcl.addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.EQ).build().execute(mrt);
		Assert.assertTrue(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.NE).build().execute(mrt);
		Assert.assertFalse(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.GE).build().execute(mrt);
		Assert.assertTrue(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.GT).build().execute(mrt);
		Assert.assertFalse(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.LE).build().execute(mrt);
		Assert.assertTrue(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.CONST_CHAR, "vassya").addCommand(CommandType.LT).build().execute(mrt);
		Assert.assertFalse(mrt.getProgramStack().popStackValue().getValue(boolean.class));
		
		// bool
		
		tcl.addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.EQ).build().execute(mrt);
		Assert.assertTrue(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.NE).build().execute(mrt);
		Assert.assertFalse(mrt.getProgramStack().popStackValue().getValue(boolean.class));
		
		tcl.addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.GE).build().execute(mrt);
		Assert.assertTrue(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.GT).build().execute(mrt);
		Assert.assertFalse(mrt.getProgramStack().popStackValue().getValue(boolean.class));
		
		tcl.addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.LE).build().execute(mrt);
		Assert.assertTrue(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.LT).build().execute(mrt);
		Assert.assertFalse(mrt.getProgramStack().popStackValue().getValue(boolean.class));
	}
	
	@Test
	public void andOrTest() throws CalculationException, ContentException {
		final ThreadedCommandList	tcl = new ThreadedCommandList();
		final MacrosRuntime			mrt = new SingleMacrosRuntime();

		tcl.addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.AND).build().execute(mrt);
		Assert.assertTrue(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.CONST_BOOLEAN, false).addCommand(CommandType.AND).build().execute(mrt);
		Assert.assertFalse(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_BOOLEAN, false).addCommand(CommandType.CONST_BOOLEAN, false).addCommand(CommandType.OR).build().execute(mrt);
		Assert.assertFalse(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		tcl.addCommand(CommandType.CONST_BOOLEAN, false).addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.OR).build().execute(mrt);
		Assert.assertTrue(mrt.getProgramStack().popStackValue().getValue(boolean.class));

		try {
			tcl.addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.AND).build().execute(mrt);
			Assert.fail("Mandatory exception was not detected (non-booelan to AND)");
		} catch (ContentException | CalculationException exc) {
		}

		try {
			tcl.addCommand(CommandType.CONST_REAL, 10).addCommand(CommandType.CONST_BOOLEAN, true).addCommand(CommandType.OR).build().execute(mrt);
			Assert.fail("Mandatory exception was not detected (non-booelan to OR)");
		} catch (ContentException | CalculationException exc) {
		}
	}	
}
