package chav1961.bt.matrix.macros.runtime.interfaces;

import java.util.Arrays;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.sql.SQLUtils;

public interface Value extends Cloneable, Comparable<Value> {
	
	public static enum ValueType {
		INT(true, false),
		REAL(true, false),
		STRING(false, false),
		BOOLEAN(false, false),
		INT_ARRAY(true, true),
		REAL_ARRAY(true, true),
		STRING_ARRAY(false, true),
		BOOLEAN_ARRAY(false, true);
		
		private final boolean	isNumber;
		private final boolean	isArray;
		
		private ValueType(final boolean isNumber, final boolean isArray) {
			this.isNumber = isNumber;
			this.isArray = isArray;
		}
		
		public boolean isNumber() {
			return isNumber;
		}
		
		public boolean isArray() {
			return isArray;
		}

	}
	
	ValueType getType();
	<T> T getValue(Class<T> awaited) throws ContentException;
	<T> void setValue(Class<T> awaited, T value) throws ContentException;
	Object clone() throws CloneNotSupportedException;
	@Override
	int compareTo(Value o);
	
	public static class Factory {
		public static final Value	TRUE = new ValueImpl(true);  
		public static final Value	FALSE = new ValueImpl(false);
		
		public static Value newReadOnlyInstance(final boolean value) {
			return value ? TRUE : FALSE;
		}

		public static Value newReadOnlyInstance(final long value) {
			return new ValueImpl(value);
		}

		public static Value newReadOnlyInstance(final double value) {
			return new ValueImpl(value);
		}

		public static Value newReadOnlyInstance(final char[] value) {
			if (value == null) {
				throw new NullPointerException("Value can't be null");
			}
			else {
				return new ValueImpl(value);
			}
		}

		public static Value newReadOnlyInstance(final CharSequence value) {
			if (value == null) {
				throw new NullPointerException("Value can't be null");
			}
			else {
				return new ValueImpl(CharUtils.toCharArray(value));
			}
		}
	}
	
	static class ValueImpl implements Value {
		private final ValueType	type;
		private final long		numberContent;
		private final char[]	charContent;

		private ValueImpl(final boolean value) {
			this.type = ValueType.BOOLEAN;
			this.numberContent = value ? 1 : 0;
			this.charContent = null;
		}

		private ValueImpl(final long value) {
			this.type = ValueType.INT;
			this.numberContent = value;
			this.charContent = null;
		}

		private ValueImpl(final double value) {
			this.type = ValueType.INT;
			this.numberContent = Double.doubleToLongBits(value);
			this.charContent = null;
		}

		private ValueImpl(final char[] value) {
			this.type = ValueType.STRING;
			this.numberContent = 0;
			this.charContent = value.clone();
		}
		
		@Override
		public ValueType getType() {
			return type;
		}

		@Override
		public <T> T getValue(final Class<T> awaited) throws ContentException {
			if (awaited.isAnnotation()) {
				return (T) getValue(CompilerUtils.toWrappedClass(awaited));
			}
			else {
				switch (getType()) {
					case BOOLEAN	:
						return SQLUtils.convert(awaited, numberContent == 1 ? Boolean.TRUE : Boolean.FALSE);
					case INT		:
						return SQLUtils.convert(awaited, Long.valueOf(numberContent));
					case REAL		:
						return SQLUtils.convert(awaited, Double.valueOf(Double.longBitsToDouble(numberContent)));
					case STRING		:
						return SQLUtils.convert(awaited, charContent);
					case BOOLEAN_ARRAY : case INT_ARRAY : case REAL_ARRAY : case STRING_ARRAY :
						throw new IllegalArgumentException("Var value type [] can't support conversion to ["+awaited.getCanonicalName()+"]");
					default :
						throw new UnsupportedOperationException("Var value type ["+getType()+"] is not supported yet");			
				}
			}
		}

		@Override
		public <T> void setValue(final Class<T> awaited, final T value) throws ContentException {
			throw new IllegalStateException("This implementation is read-only");
		}

		@Override
		public Object clone() throws CloneNotSupportedException {
			switch (getType()) {
				case BOOLEAN : case INT : case REAL : case STRING : 
					return super.clone();
				case BOOLEAN_ARRAY : case INT_ARRAY : case REAL_ARRAY : case STRING_ARRAY :
					throw new IllegalArgumentException("Value can't be cloned");
				default :
					throw new UnsupportedOperationException("Var value type ["+getType()+"] is not supported yet");			
			}
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(charContent);
			result = prime * result + (int) (numberContent ^ (numberContent >>> 32));
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ValueImpl other = (ValueImpl) obj;
			if (!Arrays.equals(charContent, other.charContent)) return false;
			if (numberContent != other.numberContent) return false;
			if (type != other.type) return false;
			return true;
		}

		@Override
		public String toString() {
			return "ValueImpl [type=" + type + ", numberContent=" + numberContent + ", charContent=" + Arrays.toString(charContent) + "]";
		}

		@Override
		public int compareTo(final Value o) {
			if (o == null || o.getType() != getType()) {
				throw new IllegalArgumentException("Value to compare is null or has incompatible type");
			}
			else {
				return -1;
			}
		}
	}
}
