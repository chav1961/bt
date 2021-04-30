package chav1961.bt.mnemoed.entities;

import java.io.IOException;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public final class PrimitiveConstantValueSource extends PrimitiveValueSource {
	private int		type;
	private long	primitiveValue;

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
	public void upload(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Stax printer can't be null");
		}
		else {
			printer.startObject().name("valType").value(type).name("value").value(primitiveValue).endObject();
		}
	}

	@Override
	public void download(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser == null) {
			throw new NullPointerException("Stax printer can't be null");
		}
		else {
			if (parser.current() == JsonStaxParserLexType.START_OBJECT) {
				JsonStaxParserLexType	lexType;
				
				do {lexType = parser.next();
					if (lexType == JsonStaxParserLexType.NAME) {
						switch (parser.name()) {
							case "valType" :
								if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER && parser.next() == JsonStaxParserLexType.INTEGER_VALUE) {
									type = (int)parser.intValue();
									lexType = parser.next();
								}
								else {
									throw new SyntaxException(parser.row(), parser.col(), "Structure corruption (integer awaited)");
								}
								break;
							case "value" :
								if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER && parser.next() == JsonStaxParserLexType.INTEGER_VALUE) {
									primitiveValue = parser.intValue();
									lexType = parser.next();
								}
								else {
									throw new SyntaxException(parser.row(), parser.col(), "Structure corruption (integer awaited)");
								}
								break;
							default :
								throw new SyntaxException(parser.row(), parser.col(), "Unsupported name. Only 'valType' and 'value' are valid here");
						}
					}
					else {
						throw new SyntaxException(parser.row(), parser.col(), "field name is missing");
					}
				} while (lexType == JsonStaxParserLexType.LIST_SPLITTER);
				if (lexType == JsonStaxParserLexType.END_OBJECT) {
					parser.next();
				}
				else {
					throw new SyntaxException(parser.row(), parser.col(), "'}' is missing");
				}
			}
			else {
				throw new SyntaxException(parser.row(), parser.col(), "'{' is missing");
			}
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (primitiveValue ^ (primitiveValue >>> 32));
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PrimitiveConstantValueSource other = (PrimitiveConstantValueSource) obj;
		if (primitiveValue != other.primitiveValue) return false;
		if (type != other.type) return false;
		return true;
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
