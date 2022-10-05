package chav1961.bt.clipper.inner.functions;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.clipper.inner.AbstractBuiltinClipperFunction;
import chav1961.bt.clipper.inner.ImmutableClipperValue;
import chav1961.bt.clipper.inner.interfaces.ClipperSyntaxEntity;
import chav1961.bt.clipper.inner.interfaces.ClipperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class RuntimeContainerTest {
	@Test
	public void testParameters() throws ContentException {
	}	

	@Test
	public void testAADD() throws ContentException {
		final SyntaxTreeInterface<ClipperSyntaxEntity>	sti = new AndOrTree<>();
		final RuntimeContainer							rc = new RuntimeContainer();
		
		rc.prepare(sti);
		final ClipperValue result = ((AbstractBuiltinClipperFunction)sti.getCargo(sti.seekName("AADD")).getValueAssociated()).invoke(
										 new ImmutableClipperValue(ClipperType.C_Array, new ClipperValue[] {
												 new ImmutableClipperValue(ClipperType.C_Number, 10)
												,new ImmutableClipperValue(ClipperType.C_Number, 20)
												,new ImmutableClipperValue(ClipperType.C_Number, 30)
												})
										 ,new ImmutableClipperValue(ClipperType.C_Number, 40)
										);
		Assert.assertEquals(ClipperType.C_Array, result.getType());
		Assert.assertArrayEquals(new ClipperValue[] {new ImmutableClipperValue(ClipperType.C_Number, 10), new ImmutableClipperValue(ClipperType.C_Number, 20), new ImmutableClipperValue(ClipperType.C_Number, 30), new ImmutableClipperValue(ClipperType.C_Number, 40)}, result.get(ClipperValue[].class));
	}
	
	@Test
	public void testABS() throws ContentException {
		final SyntaxTreeInterface<ClipperSyntaxEntity>	sti = new AndOrTree<>();
		final RuntimeContainer							rc = new RuntimeContainer();
		
		rc.prepare(sti);
		final ClipperValue resultLong = ((AbstractBuiltinClipperFunction)sti.getCargo(sti.seekName("ABS")).getValueAssociated()).invoke(
										 new ImmutableClipperValue(ClipperType.C_Number, 10L)
										);
		Assert.assertEquals(ClipperType.C_Number, resultLong.getType());
		Assert.assertEquals(-10L, resultLong.get(Number.class));

		final ClipperValue resultDouble = ((AbstractBuiltinClipperFunction)sti.getCargo(sti.seekName("ABS")).getValueAssociated()).invoke(
										 new ImmutableClipperValue(ClipperType.C_Number, 10.0)
										);
		Assert.assertEquals(ClipperType.C_Number, resultDouble.getType());
		Assert.assertEquals(-10.0, resultDouble.get(Number.class));
	}

	@Test
	public void testACLONE() throws ContentException {
		final SyntaxTreeInterface<ClipperSyntaxEntity>	sti = new AndOrTree<>();
		final RuntimeContainer							rc = new RuntimeContainer();
		
		rc.prepare(sti);
		final ClipperValue result = ((AbstractBuiltinClipperFunction)sti.getCargo(sti.seekName("ACLONE")).getValueAssociated()).invoke(
									 new ImmutableClipperValue(ClipperType.C_Array, new ClipperValue[] {
											 new ImmutableClipperValue(ClipperType.C_Number, 10)
											,new ImmutableClipperValue(ClipperType.C_Number, 20)
											,new ImmutableClipperValue(ClipperType.C_Number, 30)
											})
									);
		Assert.assertEquals(ClipperType.C_Array, result.getType());
		Assert.assertArrayEquals(new ClipperValue[] {new ImmutableClipperValue(ClipperType.C_Number, 10), new ImmutableClipperValue(ClipperType.C_Number, 20), new ImmutableClipperValue(ClipperType.C_Number, 30)}, result.get(ClipperValue[].class));
	}

	@Test
	public void testACOPY() throws ContentException {
		final SyntaxTreeInterface<ClipperSyntaxEntity>	sti = new AndOrTree<>();
		final RuntimeContainer							rc = new RuntimeContainer();
		
		rc.prepare(sti);
		final ClipperValue result = ((AbstractBuiltinClipperFunction)sti.getCargo(sti.seekName("ACOPY")).getValueAssociated()).invoke(
									 new ImmutableClipperValue(ClipperType.C_Array, new ClipperValue[] {
											 new ImmutableClipperValue(ClipperType.C_Number, 10)
											,new ImmutableClipperValue(ClipperType.C_Number, 20)
											,new ImmutableClipperValue(ClipperType.C_Number, 30)
											})
									);
		Assert.assertEquals(ClipperType.C_Array, result.getType());
		Assert.assertArrayEquals(new ClipperValue[] {new ImmutableClipperValue(ClipperType.C_Number, 10), new ImmutableClipperValue(ClipperType.C_Number, 20), new ImmutableClipperValue(ClipperType.C_Number, 30)}, result.get(ClipperValue[].class));
	}
	
	@Test
	public void testADEL() throws ContentException {
		final SyntaxTreeInterface<ClipperSyntaxEntity>	sti = new AndOrTree<>();
		final RuntimeContainer							rc = new RuntimeContainer();
		
		rc.prepare(sti);
		final ClipperValue result = ((AbstractBuiltinClipperFunction)sti.getCargo(sti.seekName("ADEL")).getValueAssociated()).invoke(
										 new ImmutableClipperValue(ClipperType.C_Array, new ClipperValue[] {
												 new ImmutableClipperValue(ClipperType.C_Number, 10)
												,new ImmutableClipperValue(ClipperType.C_Number, 20)
												,new ImmutableClipperValue(ClipperType.C_Number, 30)
												})
										 ,new ImmutableClipperValue(ClipperType.C_Number, 1)
										);
		Assert.assertEquals(ClipperType.C_Array, result.getType());
		Assert.assertArrayEquals(new ClipperValue[] {new ImmutableClipperValue(ClipperType.C_Number, 20), new ImmutableClipperValue(ClipperType.C_Number, 30), null}, result.get(ClipperValue[].class));
	}

	@Test
	public void testAFILL() throws ContentException {
		final SyntaxTreeInterface<ClipperSyntaxEntity>	sti = new AndOrTree<>();
		final RuntimeContainer							rc = new RuntimeContainer();
		
		rc.prepare(sti);
		final ClipperValue result = ((AbstractBuiltinClipperFunction)sti.getCargo(sti.seekName("AFILL")).getValueAssociated()).invoke(
										 new ImmutableClipperValue(ClipperType.C_Array, new ClipperValue[] {
												 new ImmutableClipperValue(ClipperType.C_Number, 10)
												,new ImmutableClipperValue(ClipperType.C_Number, 20)
												,new ImmutableClipperValue(ClipperType.C_Number, 30)
												})
										 ,new ImmutableClipperValue(ClipperType.C_Number, 40)
										);
		Assert.assertEquals(ClipperType.C_Array, result.getType());
		Assert.assertArrayEquals(new ClipperValue[] {new ImmutableClipperValue(ClipperType.C_Number, 40), new ImmutableClipperValue(ClipperType.C_Number, 40), new ImmutableClipperValue(ClipperType.C_Number, 40)}, result.get(ClipperValue[].class));
	}

	@Test
	public void testAINS() throws ContentException {
		final SyntaxTreeInterface<ClipperSyntaxEntity>	sti = new AndOrTree<>();
		final RuntimeContainer							rc = new RuntimeContainer();
		
		rc.prepare(sti);
		final ClipperValue result = ((AbstractBuiltinClipperFunction)sti.getCargo(sti.seekName("AINS")).getValueAssociated()).invoke(
										 new ImmutableClipperValue(ClipperType.C_Array, new ClipperValue[] {
												 new ImmutableClipperValue(ClipperType.C_Number, 10)
												,new ImmutableClipperValue(ClipperType.C_Number, 20)
												,new ImmutableClipperValue(ClipperType.C_Number, 30)
												})
										 ,new ImmutableClipperValue(ClipperType.C_Number, 1)
										);
		Assert.assertEquals(ClipperType.C_Array, result.getType());
		Assert.assertArrayEquals(new ClipperValue[] {null, new ImmutableClipperValue(ClipperType.C_Number, 10), new ImmutableClipperValue(ClipperType.C_Number, 20)}, result.get(ClipperValue[].class));
	}

	@Test
	public void testArray() throws ContentException {
		final SyntaxTreeInterface<ClipperSyntaxEntity>	sti = new AndOrTree<>();
		final RuntimeContainer							rc = new RuntimeContainer();
		
		rc.prepare(sti);
		final ClipperValue result = ((AbstractBuiltinClipperFunction)sti.getCargo(sti.seekName("ARRAY")).getValueAssociated()).invoke(
										 new ImmutableClipperValue(ClipperType.C_Number, 5)
										);
		Assert.assertEquals(ClipperType.C_Array, result.getType());
		Assert.assertEquals(5, result.get(ClipperValue[].class).length);
	}
	
	@Test
	public void testASIZE() throws ContentException {
		final SyntaxTreeInterface<ClipperSyntaxEntity>	sti = new AndOrTree<>();
		final RuntimeContainer							rc = new RuntimeContainer();
		
		rc.prepare(sti);
		final ClipperValue result = ((AbstractBuiltinClipperFunction)sti.getCargo(sti.seekName("ASIZE")).getValueAssociated()).invoke(
										 new ImmutableClipperValue(ClipperType.C_Array, new ClipperValue[] {
												 new ImmutableClipperValue(ClipperType.C_Number, 10)
												,new ImmutableClipperValue(ClipperType.C_Number, 20)
												,new ImmutableClipperValue(ClipperType.C_Number, 30)
												})
										 ,new ImmutableClipperValue(ClipperType.C_Number, 4)
										);
		Assert.assertEquals(ClipperType.C_Array, result.getType());
		Assert.assertArrayEquals(new ClipperValue[] {new ImmutableClipperValue(ClipperType.C_Number, 10), new ImmutableClipperValue(ClipperType.C_Number, 20), new ImmutableClipperValue(ClipperType.C_Number, 30), null}, result.get(ClipperValue[].class));
	}
}
