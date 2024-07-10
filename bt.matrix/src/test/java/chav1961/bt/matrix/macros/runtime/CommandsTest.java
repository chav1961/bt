package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.Command.ControlType;
import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;
import chav1961.bt.matrix.macros.runtime.interfaces.Value;
import chav1961.purelib.basic.exceptions.CalculationException;

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

}
