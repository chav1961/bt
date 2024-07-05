package chav1961.bt.matrix.macros.runtime.interfaces;

import java.lang.reflect.Array;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.ContentException;

public interface ValueArray extends Value {
	int length();
	<T> T getValue(int index, Class<T> awaited);
	<T> void setValue(int index, Class<T> awaited, T value);
    <T> void addValue(int index, Class<T> awaited, T value);
    <T> T removeValue(int index, Class<T> awaited);
    
    public static class Factory {
    	
    }
    
    static class ValueArrayImpl implements ValueArray {
    	private final ValueType	type;
    	private final Object	array;
    	
    	ValueArrayImpl(final boolean... values) {
    		this.type = ValueType.BOOLEAN_ARRAY;
    		this.array = values.clone();
    	}

    	ValueArrayImpl(final long... values) {
    		this.type = ValueType.INT_ARRAY;
    		this.array = values.clone();
    	}

    	ValueArrayImpl(final double... values) {
    		this.type = ValueType.REAL_ARRAY;
    		this.array = values.clone();
    	}

    	ValueArrayImpl(final char[]... values) {
    		final char[][]	temp = values.clone();
    		
    		this.type = ValueType.STRING_ARRAY;
    		for(int index = 0; index < values.length; index++) {
    			temp[index] = temp[index].clone(); 
    		}
    		this.array = temp;
    	}
    	
    	ValueArrayImpl(final CharSequence... values) {
    		final char[][]	temp = new char[values.length][];
    		
    		this.type = ValueType.STRING_ARRAY;
    		for(int index = 0; index < values.length; index++) {
    			temp[index] = CharUtils.toCharArray(values[index]); 
    		}
    		this.array = temp;
    	}
    	
		@Override
		public ValueType getType() {
			return type;
		}

		@Override
		public <T> T getValue(Class<T> awaited) throws ContentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T> void setValue(Class<T> awaited, T value) throws ContentException {
			throw new IllegalStateException("This implementation is read-only");
		}

		@Override
		public int length() {
			return Array.getLength(array);
		}

		@Override
		public <T> T getValue(int index, Class<T> awaited) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T> void setValue(int index, Class<T> awaited, T value) {
			throw new IllegalStateException("This implementation is read-only");
		}

		@Override
		public <T> void addValue(int index, Class<T> awaited, T value) {
			throw new IllegalStateException("This implementation is read-only");
		}

		@Override
		public <T> T removeValue(int index, Class<T> awaited) {
			throw new IllegalStateException("This implementation is read-only");
		}
    	
    }
}
