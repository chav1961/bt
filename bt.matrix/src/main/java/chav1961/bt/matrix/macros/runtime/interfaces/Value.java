package chav1961.bt.matrix.macros.runtime.interfaces;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.sql.SQLUtils;

public interface Value {
	public static enum ValueType {
		INT,
		REAL,
		STRING,
		BOOLEAN,
		INT_ARRAY,
		REAL_ARRAY,
		STRING_ARRAY,
		BOOLEAN_ARRAY;
	}
	
	ValueType getType();
	<T> T getValue(Class<T> awaited) throws ContentException;
	<T> void setValue(Class<T> awaited, T value) throws ContentException;
	
	public static class Factory {
		public static Value newReadOnlyInstance(final boolean value) {
			return new ValueImpl(value);
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
	}
}
