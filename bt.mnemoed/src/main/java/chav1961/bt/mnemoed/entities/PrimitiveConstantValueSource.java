package chav1961.bt.mnemoed.entities;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class PrimitiveConstantValueSource extends PrimitiveValueSource {
	private final int		type;
	private final long		primitiveValue;

	public PrimitiveConstantValueSource(final byte primitiveValue) {
		this(CompilerUtils.CLASSTYPE_BYTE,primitiveValue);
	}

	public PrimitiveConstantValueSource(final short primitiveValue) {
		this(CompilerUtils.CLASSTYPE_SHORT,primitiveValue);
	}
	
	public PrimitiveConstantValueSource(final int primitiveValue) {
		this(CompilerUtils.CLASSTYPE_INT,primitiveValue);
	}

	public PrimitiveConstantValueSource(final long primitiveValue) {
		this(CompilerUtils.CLASSTYPE_LONG,primitiveValue);
	}

	public PrimitiveConstantValueSource(final float primitiveValue) {
		this(CompilerUtils.CLASSTYPE_FLOAT,Double.doubleToLongBits(primitiveValue));
	}

	public PrimitiveConstantValueSource(final double primitiveValue) {
		this(CompilerUtils.CLASSTYPE_DOUBLE,Double.doubleToLongBits(primitiveValue));
	}

	public PrimitiveConstantValueSource(final char primitiveValue) {
		this(CompilerUtils.CLASSTYPE_CHAR,0xFFFF & primitiveValue);
	}

	public PrimitiveConstantValueSource(final boolean primitiveValue) {
		this(CompilerUtils.CLASSTYPE_BOOLEAN,primitiveValue ? 1 : 0);
	}
	
	protected PrimitiveConstantValueSource(final int type, final long primitiveValue) {
		super(ValueSourceType.PRIMITIVE_CONST);
		this.type = type;
		this.primitiveValue = primitiveValue;
	}

	public int getType() {
		return type;
	}
	
	public byte getByteValue() {
		return (byte)primitiveValue;
	}

	public short getShortValue() {
		return (short)primitiveValue;
	}

	public int getIntValue() {
		return (int)primitiveValue;
	}

	public long getLongValue() {
		return primitiveValue;
	}

	public float getFloatValue() {
		return (float)Double.longBitsToDouble(primitiveValue);
	}

	public double getDoubleValue() {
		return Double.longBitsToDouble(primitiveValue);
	}

	public char getCharValue() {
		return (char)primitiveValue;
	}

	public boolean getBooleanValue() {
		return primitiveValue != 0;
	}

	@Override
	public void upload(final JsonStaxPrinter printer) throws PrintingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void download(final JsonStaxParser parser) throws SyntaxException {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public String toString() {
		return "PrimitiveConstantValueSource [type=" + type + ", primitiveValue=" + asString() + ", getSourceType()=" + getSourceType() + "]";
	}
	
	private String asString() {
		switch (getType()) {
			case CompilerUtils.CLASSTYPE_BYTE : case CompilerUtils.CLASSTYPE_SHORT : case CompilerUtils.CLASSTYPE_INT : case CompilerUtils.CLASSTYPE_LONG :
				return String.valueOf(primitiveValue);
			case CompilerUtils.CLASSTYPE_FLOAT : case CompilerUtils.CLASSTYPE_DOUBLE :
				return String.valueOf(Double.longBitsToDouble(primitiveValue));
			case CompilerUtils.CLASSTYPE_CHAR :
				return ""+(char)primitiveValue;
			case CompilerUtils.CLASSTYPE_BOOLEAN :
				return Boolean.toString(getBooleanValue());
			default :
				throw new UnsupportedOperationException("Value type ["+getType()+"] is not supported yet");
		}
	}
}
