package chav1961.bt.matrix.macros.runtime.interfaces;

import java.util.Arrays;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.sql.SQLUtils;

public interface Value extends Cloneable, Comparable<Value> {
	
	public static enum ValueType {
		INT(true, false, long.class, null),
		REAL(true, false, double.class, null),
		STRING(false, false, char[].class, null),
		BOOLEAN(false, false, boolean.class, null),
		INT_ARRAY(true, true, int[].class, INT),
		REAL_ARRAY(true, true, double[].class, REAL),
		STRING_ARRAY(false, true, char[][].class, STRING),
		BOOLEAN_ARRAY(false, true, boolean[].class, BOOLEAN);
		
		private final boolean	isNumber;
		private final boolean	isArray;
		private final ValueType	componentType;
		private final Class<?>	clazz;
		
		private ValueType(final boolean isNumber, final boolean isArray, final Class<?> clazz, final ValueType componentType) {
			this.isNumber = isNumber;
			this.isArray = isArray;
			this.clazz = clazz;
			this.componentType = componentType;
		}
		
		public boolean isNumber() {
			return isNumber;
		}
		
		public boolean isArray() {
			return isArray;
		}

		public ValueType getComponentType() {
			return componentType;
		}
		
		public Class<?> getType() {
			return clazz;
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
			this.type = ValueType.REAL;
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
				try {
					switch (getType()) {
						case BOOLEAN	:
							final boolean	bLeft = numberContent != 0 ? true : false; 
							final boolean	bRight = o.getValue(boolean.class).booleanValue();
							
							return (bRight ? 1 : 0) - (bLeft ? 1 : 0); 
						case INT		:
							final long		lRight = o.getValue(long.class).longValue();
							final long		lDelta = lRight - numberContent;
							
							return lDelta < 0 ? -1 : (lDelta > 0 ? 1 : 0);
						case REAL		:
							final double	lRightD = o.getValue(double.class).doubleValue();
							final double	lDeltaD = lRightD - Double.longBitsToDouble(numberContent);
							
							return lDeltaD < 0 ? -1 : (lDeltaD > 0 ? 1 : 0);
						case STRING		:
							final char[]	lRightC = o.getValue(char[].class);
							
							if (charContent == null || lRightC == null) {
								throw new IllegalArgumentException("Value to compare is null or has incompatible type");
							}
							else {
								return CharUtils.compareTo(lRightC, charContent);
							}
						case INT_ARRAY : case BOOLEAN_ARRAY : case REAL_ARRAY : case STRING_ARRAY :
							throw new IllegalArgumentException("Value type ["+getType()+"] is not available in this context");
						default :
							throw new UnsupportedOperationException("Value type ["+getType()+"] is not supported yet");
					}
				} catch (ContentException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}
	}
}
