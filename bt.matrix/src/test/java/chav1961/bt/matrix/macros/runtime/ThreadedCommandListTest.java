package chav1961.bt.matrix.macros.runtime;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.matrix.macros.runtime.CommandList.CommandType;
import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;
import chav1961.bt.matrix.macros.runtime.interfaces.Value;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;

public class ThreadedCommandListTest {
	@Test
	public void basicTest() throws CalculationException, ContentException {
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
	public void commandsTest() throws CalculationException, ContentException {
		final ThreadedCommandList	tcl = new ThreadedCommandList();
		final MacrosRuntime			mrt = new SingleMacrosRuntime();
		
		
	}
}
