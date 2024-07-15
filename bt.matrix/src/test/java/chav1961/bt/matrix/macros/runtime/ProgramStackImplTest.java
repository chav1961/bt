package chav1961.bt.matrix.macros.runtime;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.matrix.macros.runtime.interfaces.ProgramStack;
import chav1961.bt.matrix.macros.runtime.interfaces.Value;
import chav1961.bt.matrix.macros.runtime.interfaces.Value.ValueType;
import chav1961.purelib.basic.exceptions.ContentException;

public class ProgramStackImplTest {
	@Test
	public void basicVariableTest() throws ContentException {
		final ProgramStack	ps = new ProgramStackImpl();
		
		ps.declare(1, ValueType.BOOLEAN);
		Assert.assertEquals(ValueType.BOOLEAN, ps.getVarType(1));
		Assert.assertNull(ps.getVarValue(1));

		ps.setVarValue(1, Value.Factory.newReadOnlyInstance(true));
		Assert.assertEquals(Value.Factory.newReadOnlyInstance(true), ps.getVarValue(1));
		
		try{ps.declare(1, ValueType.STRING);
			Assert.fail("Mandatory exception was not detected (duplicate name)");
		} catch (IllegalArgumentException exc) {
		}

		try{ps.setVarValue(1, Value.Factory.newReadOnlyInstance(0L));
			Assert.fail("Mandatory exception was not detected (incompatible types to assign)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{ps.getVarType(100);
			Assert.fail("Mandatory exception was not detected (name is missing)");
		} catch (IllegalArgumentException exc) {
		}
		try{ps.getVarValue(100);
			Assert.fail("Mandatory exception was not detected (name is missing)");
		} catch (IllegalArgumentException exc) {
		}
		
		ps.declare(2, ValueType.STRING);
		ps.setVarValue(2, Value.Factory.newReadOnlyInstance("test"));
		
		ps.pushBlock();
		Assert.assertEquals(ValueType.BOOLEAN, ps.getVarType(1));
		Assert.assertEquals(Value.Factory.newReadOnlyInstance(true), ps.getVarValue(1));
		Assert.assertEquals(ValueType.STRING, ps.getVarType(2));
		Assert.assertArrayEquals("test".toCharArray(), ps.getVarValue(2).getValue(char[].class));
		
		ps.declare(1, ValueType.STRING);
		Assert.assertEquals(ValueType.STRING, ps.getVarType(1));
		Assert.assertNull(ps.getVarValue(1));
		
		ps.popBlock();
		Assert.assertEquals(ValueType.BOOLEAN, ps.getVarType(1));
		Assert.assertEquals(Value.Factory.newReadOnlyInstance(true), ps.getVarValue(1));

		try{ps.popBlock();
			Assert.fail("Mandatory exception was not detected (stack exhausted)");
		} catch (IllegalStateException exc) {
		}
	}

	@Test
	public void basicStackTest() throws ContentException {
		final ProgramStack	ps = new ProgramStackImpl();
		
		try{ps.getStackValue();
			Assert.fail("Mandatory exception was not detected (empty stack)");
		} catch (IllegalStateException exc) {
		}
		
		ps.pushStackValue(Value.Factory.newReadOnlyInstance(true));
		Assert.assertEquals(Value.Factory.newReadOnlyInstance(true), ps.getStackValue());
		Assert.assertEquals(Value.Factory.newReadOnlyInstance(true), ps.popStackValue());
		
		try{ps.popStackValue();
			Assert.fail("Mandatory exception was not detected (empty stack)");
		} catch (IllegalStateException exc) {
		}
	}
}
