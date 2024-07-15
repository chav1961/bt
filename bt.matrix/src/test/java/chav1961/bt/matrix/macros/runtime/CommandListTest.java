package chav1961.bt.matrix.macros.runtime;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.matrix.macros.runtime.CommandList.CommandType;
import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;
import chav1961.bt.matrix.macros.runtime.interfaces.Value;

public class CommandListTest {

	@Test
	public void basicTest() {
		final CommandList	cl = new CommandList();
		final MacrosRuntime	rt = new SingleMacrosRuntime();
		
		cl.addCommand(CommandType.CONST_CHAR, Value.Factory.newReadOnlyInstance("test".toCharArray()));
		cl.addCommand(CommandType.PRINT);
		
	}
}
