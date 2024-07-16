package chav1961.bt.preproc.runtime;

import chav1961.bt.preproc.runtime.interfaces.Command.ControlType;
import chav1961.bt.preproc.runtime.interfaces.MacrosRuntime;
import chav1961.bt.preproc.runtime.interfaces.Value;
import chav1961.bt.preproc.runtime.interfaces.Value.ValueType;
import chav1961.bt.preproc.runtime.interfaces.ValueArray;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;

import org.junit.Assert;
import org.junit.Test;

public class CommandsTest {
	@Test
	public void singletonsTest() throws CalculationException {
		final MacrosRuntime	rt = new SingleMacrosRuntime();
		final IncLockCount	ilc = IncLockCount.SINGLETON;
		
		Assert.assertEquals(1, ilc.execute(rt));
		Assert.assertEquals(ControlType.SEQUENCE, ilc.getControlType());
		Assert.assertEquals(1, rt.getLockCount());

		final DecLockCount	dlc = DecLockCount.SINGLETON;
		
		Assert.assertEquals(1, dlc.execute(rt));
		Assert.assertEquals(ControlType.SEQUENCE, dlc.getControlType());
		Assert.assertEquals(0, rt.getLockCount());
		
		final PushContext	puc = PushContext.SINGLETON;
		
		Assert.assertEquals(1, rt.getProgramStack().getBlockDepth());
		Assert.assertEquals(1, puc.execute(rt));
		Assert.assertEquals(ControlType.SEQUENCE, puc.getControlType());
		Assert.assertEquals(2, rt.getProgramStack().getBlockDepth());

		final PopContext	poc = PopContext.SINGLETON;
		
		Assert.assertEquals(2, rt.getProgramStack().getBlockDepth());
		Assert.assertEquals(1, poc.execute(rt));
		Assert.assertEquals(ControlType.SEQUENCE, poc.getControlType());
		Assert.assertEquals(1, rt.getProgramStack().getBlockDepth());
		
		final PushStack		pus = PushStack.SINGLETON;

		Assert.assertEquals(0, rt.getProgramStack().getStackDepth());
		Assert.assertEquals(1, pus.execute(rt));
		Assert.assertEquals(ControlType.SEQUENCE, pus.getControlType());
		Assert.assertEquals(1, rt.getProgramStack().getStackDepth());

		final DuplicateStack	dup = DuplicateStack.SINGLETON;

		Assert.assertEquals(1, rt.getProgramStack().getStackDepth());
		Assert.assertEquals(1, dup.execute(rt));
		Assert.assertEquals(ControlType.SEQUENCE, dup.getControlType());
		Assert.assertEquals(2, rt.getProgramStack().getStackDepth());
		
		final PopStack		pos = PopStack.SINGLETON;

		Assert.assertEquals(2, rt.getProgramStack().getStackDepth());
		Assert.assertEquals(1, pos.execute(rt));
		Assert.assertEquals(ControlType.SEQUENCE, pos.getControlType());
		Assert.assertEquals(1, rt.getProgramStack().getStackDepth());
	}

	@Test
	public void printContentTest() throws CalculationException {
		final MacrosRuntime	rt = new SingleMacrosRuntime();
		final PrintContent	pc1 = new PrintContent(false, false);
		
		rt.getProgramStack().pushStackValue(Value.Factory.newReadOnlyInstance("test"));		
		Assert.assertEquals(1, pc1.execute(rt));
		Assert.assertEquals(ControlType.SEQUENCE, pc1.getControlType());
		Assert.assertEquals("test"+System.lineSeparator(), rt.getBuffer().toString());

		final PrintContent	pc2 = new PrintContent(true, false);
		
		rt.getProgramStack().pushStackValue(Value.Factory.newReadOnlyInstance("test"));		
		Assert.assertEquals(1, pc2.execute(rt));
		
		final PrintContent	pc3 = new PrintContent(true, true);
		
		rt.getProgramStack().pushStackValue(Value.Factory.newReadOnlyInstance("test"));		
		try{pc3.execute(rt);
			Assert.fail("Mandatory exception was not detected (exception must be)");
		} catch (CalculationException exc) {
			Assert.assertEquals("test", exc.getLocalizedMessage());
		}		
	}

	@Test
	public void declareVarTest() throws CalculationException {
		final MacrosRuntime		rt = new SingleMacrosRuntime();
		final DeclareVariable	dv = new DeclareVariable(1, ValueType.INT); 
		
		Assert.assertFalse(rt.getProgramStack().hasVar(1));
		Assert.assertEquals(1, dv.execute(rt));
		Assert.assertEquals(ControlType.SEQUENCE, dv.getControlType());
		Assert.assertTrue(rt.getProgramStack().hasVar(1));
		Assert.assertEquals(ValueType.INT, rt.getProgramStack().getVarType(1));
	}	

	@Test
	public void putContentTest() throws CalculationException {
		final MacrosRuntime		rt = new SingleMacrosRuntime();
		final PutOrdinalCommand	poc = new PutOrdinalCommand("test".toCharArray());
		
		Assert.assertEquals(1, poc.execute(rt));
		Assert.assertEquals(ControlType.SEQUENCE, poc.getControlType());
		Assert.assertEquals("test", rt.getBuffer().toString());
		Assert.assertArrayEquals("test".toCharArray(), rt.extractLine());
		
		final PutSubstitution	ps = new PutSubstitution((t)->"test".toCharArray());
		
		Assert.assertEquals(1, ps.execute(rt));
		Assert.assertEquals(ControlType.SEQUENCE, ps.getControlType());
		Assert.assertEquals("test", rt.getBuffer().toString());
		Assert.assertArrayEquals("test".toCharArray(), rt.extractLine());
	}

	@Test
	public void assignmentTest() throws CalculationException, ContentException {
		final MacrosRuntime		rt = new SingleMacrosRuntime();
		final DeclareVariable	dv1 = new DeclareVariable(1, ValueType.INT); 
		final AssignVar			av1 = new AssignVar(1);
		final Value				val = Value.Factory.newReadOnlyInstance(100);
		
		dv1.execute(rt);
		rt.getProgramStack().pushStackValue(val);
		Assert.assertNull(rt.getProgramStack().getVarValue(1));
		Assert.assertEquals(1, av1.execute(rt));
		Assert.assertEquals(ControlType.SEQUENCE, av1.getControlType());
		Assert.assertEquals(val, rt.getProgramStack().getVarValue(1));
		
		final DeclareVariable	dv2 = new DeclareVariable(2, ValueType.INT_ARRAY); 
		final AssignVar			av2 = new AssignVar(2);
		final ValueArray		va = ValueArray.Factory.newInstance(1,2,3);
		final Value				index = Value.Factory.newReadOnlyInstance(0);
		final AssignIndex		ai = new AssignIndex(2);

		dv2.execute(rt);
		rt.getProgramStack().pushStackValue(va);
		av2.execute(rt);
		rt.getProgramStack().pushStackValue(index);
		rt.getProgramStack().pushStackValue(val);
		Assert.assertEquals(1, ai.execute(rt));
		Assert.assertEquals(ControlType.SEQUENCE, ai.getControlType());
		Assert.assertEquals(val.getValue(long.class), ((ValueArray)rt.getProgramStack().getVarValue(2)).getValue(0, long.class));
	}
}
