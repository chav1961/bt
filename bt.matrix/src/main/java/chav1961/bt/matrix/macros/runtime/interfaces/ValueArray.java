package chav1961.bt.matrix.macros.runtime.interfaces;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.cdb.CompilerUtils;

public interface ValueArray extends Value {
	int length();
	<T> T getValue(int index, Class<T> awaited) throws ContentException;
	<T> void setValue(int index, Class<T> awaited, T value) throws ContentException;
    <T> void addValue(int index, Class<T> awaited, T value) throws ContentException;
    <T> T removeValue(int index, Class<T> awaited) throws ContentException;
    
    public static class Factory {
    	public static ValueArray newReadOnlyInstance(final boolean... values) {
    		if (values == null) {
    			throw new NullPointerException("Values can't be null array");
    		}
    		else {
    			return new ValueArrayReadOnlyImpl(values);
    		}
    	}

    	public static ValueArray newReadOnlyInstance(final long... values) {
    		if (values == null) {
    			throw new NullPointerException("Values can't be null array");
    		}
    		else {
    			return new ValueArrayReadOnlyImpl(values);
    		}
    	}

    	public static ValueArray newReadOnlyInstance(final double... values) {
    		if (values == null) {
    			throw new NullPointerException("Values can't be null array");
    		}
    		else {
    			return new ValueArrayReadOnlyImpl(values);
    		}
    	}

    	public static ValueArray newReadOnlyInstance(final char[]... values) {
    		if (values == null || Utils.checkArrayContent4Nulls(values) >= 0) {
    			throw new NullPointerException("Values are null array or contains nulls inside");
    		}
    		else {
    			return new ValueArrayReadOnlyImpl(values);
    		}
    	}
    }
    
    static class ValueArrayReadOnlyImpl implements ValueArray {
    	private final ValueType	type;
    	private final Object	array;
    	
    	ValueArrayReadOnlyImpl(final boolean... values) {
    		this.type = ValueType.BOOLEAN_ARRAY;
    		this.array = values.clone();
    	}

    	ValueArrayReadOnlyImpl(final long... values) {
    		this.type = ValueType.INT_ARRAY;
    		this.array = values.clone();
    	}

    	ValueArrayReadOnlyImpl(final double... values) {
    		this.type = ValueType.REAL_ARRAY;
    		this.array = values.clone();
    	}

    	ValueArrayReadOnlyImpl(final char[]... values) {
    		final char[][]	temp = values.clone();
    		
    		this.type = ValueType.STRING_ARRAY;
    		for(int index = 0; index < values.length; index++) {
    			temp[index] = temp[index].clone(); 
    		}
    		this.array = temp;
    	}
    	
    	ValueArrayReadOnlyImpl(final CharSequence... values) {
    		final char[][]	temp = new char[values.length][];
    		
    		this.type = ValueType.STRING_ARRAY;
    		for(int index = 0; index < values.length; index++) {
    			temp[index] = CharUtils.toCharArray(values[index]); 
    		}
    		this.array = temp;
    	}

		@Override
		public Object clone() throws CloneNotSupportedException {
			switch (getType()) {
				case BOOLEAN : case INT : case REAL : case STRING : 
					throw new IllegalArgumentException("Value can't be cloned");
				case BOOLEAN_ARRAY : case INT_ARRAY : case REAL_ARRAY : case STRING_ARRAY :
					return this;
				default :
					throw new UnsupportedOperationException("Var value type ["+getType()+"] is not supported yet");			
			}
		}
		
		@Override
		public ValueType getType() {
			return type;
		}

		@Override
		public <T> T getValue(final Class<T> awaited) throws ContentException {
			throw new UnsupportedOperationException("This method is not valid for arrays");
		}

		@Override
		public <T> void setValue(final Class<T> awaited, final T value) throws ContentException {
			throw new IllegalStateException("This implementation is read-only");
		}

		@Override
		public int length() {
			return Array.getLength(array);
		}

		@Override
		public <T> T getValue(final int index, final Class<T> awaited) {
			if (awaited.isPrimitive()) {
				return (T) getValue(index, CompilerUtils.toWrappedClass(awaited));
			}
			else {
				return awaited.cast(Array.get(array, index));
			}
		}

		@Override
		public <T> void setValue(final int index, final Class<T> awaited, final T value) {
			throw new IllegalStateException("This implementation is read-only");
		}

		@Override
		public <T> void addValue(final int index, final Class<T> awaited, final T value) {
			throw new IllegalStateException("This implementation is read-only");
		}

		@Override
		public <T> T removeValue(final int index, final Class<T> awaited) {
			throw new IllegalStateException("This implementation is read-only");
		}
    	
		@Override
		public int compareTo(final Value o) {
			if (o == null || o.getType() != getType() || length() != ((ValueArray)o).length()) {
				throw new IllegalArgumentException("Value to compare is null or has incompatible type");
			}
			else {
				final ValueArray	va = (ValueArray)o;
				
				try {
					for(int index = 0, maxIndex = length(); index < maxIndex; index++) {
						int	delta;
						
						switch (getType().getComponentType()) {
								case BOOLEAN	:
									final long		numberContent = Array.getLong(array, index);
									final boolean	bLeft = numberContent != 0 ? true : false; 
									final boolean	bRight = va.getValue(index, boolean.class).booleanValue();
									
									delta = (bRight ? 1 : 0) - (bLeft ? 1 : 0);
									break;
								case INT		:
									final long		lRight = va.getValue(index, long.class).longValue();
									final long		lDelta = lRight - Array.getLong(array, index);
									
									delta = lDelta < 0 ? -1 : (lDelta > 0 ? 1 : 0);
									break;
								case REAL		:
									final double	lRightD = va.getValue(index, double.class).doubleValue();
									final double	lDeltaD = lRightD - Array.getDouble(array, index);
									
									delta = lDeltaD < 0 ? -1 : (lDeltaD > 0 ? 1 : 0);
									break;
								case STRING		:
									final char[]	lRightC = va.getValue(index, char[].class);
									final char[]	charContent = (char[])Array.get(array, index);
									
									if (charContent == null || lRightC == null) {
										throw new IllegalArgumentException("Value to compare is null or has incompatible type");
									}
									else {
										delta = CharUtils.compareTo(lRightC, charContent);
									}
									break;
							case BOOLEAN_ARRAY : case INT_ARRAY : case REAL_ARRAY : case STRING_ARRAY :
								throw new UnsupportedOperationException("Var value type ["+getType()+"] is not available");			
							default:
								throw new UnsupportedOperationException("Var value type ["+getType()+"] is not supported yet");			
						}
						if (delta != 0) {
							return delta;
						}
					}
					return 0;
				} catch (ContentException exc) {
					throw new IllegalArgumentException(exc);
				}
			}
		}
    }
    
    static class ValueArrayImpl implements ValueArray {
    	private final ValueType		type;
    	private final List<Value>	array = new ArrayList<>();

    	ValueArrayImpl(final boolean... values) {
    		this.type = ValueType.BOOLEAN_ARRAY;
    		for (boolean value : values) {
    			array.add(Value.Factory.newReadOnlyInstance(value));
    		}
    	}

    	ValueArrayImpl(final long... values) {
    		this.type = ValueType.INT_ARRAY;
    		for (long value : values) {
    			array.add(Value.Factory.newReadOnlyInstance(value));
    		}
    	}

    	ValueArrayImpl(final double... values) {
    		this.type = ValueType.REAL_ARRAY;
    		for (double value : values) {
    			array.add(Value.Factory.newReadOnlyInstance(value));
    		}
    	}

    	ValueArrayImpl(final char[]... values) {
    		this.type = ValueType.REAL_ARRAY;
    		for (char[] value : values) {
    			array.add(Value.Factory.newReadOnlyInstance(value.clone()));
    		}
    	}

    	private ValueArrayImpl(final ValueType type, final List<Value> values) {
    		this.type = type;
    		for (Value value : values) {
    			try{
					array.add((Value)value.clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
    		}
    	}
    	
    	
    	@Override
		public ValueType getType() {
			return type;
		}
    	
		@Override
		public <T> T getValue(Class<T> awaited) throws ContentException {
			throw new UnsupportedOperationException("This method is not valid for arrays");
		}
		@Override
		
		public <T> void setValue(Class<T> awaited, T value) throws ContentException {
			throw new UnsupportedOperationException("This method is not valid for arrays");
		}
		
		@Override
		public Object clone() throws CloneNotSupportedException {
			switch (getType()) {
				case BOOLEAN : case INT : case REAL : case STRING : 
					throw new IllegalArgumentException("Value can't be cloned");
				case BOOLEAN_ARRAY : case INT_ARRAY : case REAL_ARRAY : case STRING_ARRAY :
					return new ValueArrayImpl(type, array);
				default :
					throw new UnsupportedOperationException("Var value type ["+getType()+"] is not supported yet");			
			}
		}
		
		@Override
		public int compareTo(final Value o) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public int length() {
			return array.size();
		}
		
		@Override
		public <T> T getValue(int index, Class<T> awaited) throws ContentException {
			return array.get(index).getValue(awaited);
		}
		
		@Override
		public <T> void setValue(int index, Class<T> awaited, T value) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public <T> void addValue(int index, Class<T> awaited, T value) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public <T> T removeValue(int index, Class<T> awaited) {
			// TODO Auto-generated method stub
			return null;
		}
    }    
}
