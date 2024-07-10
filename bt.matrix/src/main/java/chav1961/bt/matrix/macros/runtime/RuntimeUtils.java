package chav1961.bt.matrix.macros.runtime;

import java.util.function.Function;

import chav1961.bt.matrix.macros.runtime.interfaces.Value;

public class RuntimeUtils {
	private static final Function<Value,Value>[][]	CONVERTOR;
	
	static {
		CONVERTOR = new Function[Value.ValueType.values().length][];
		CONVERTOR[Value.ValueType.BOOLEAN.ordinal()] = toBoolean();
		CONVERTOR[Value.ValueType.INT.ordinal()] = toInt();
		CONVERTOR[Value.ValueType.REAL.ordinal()] = toReal();
		CONVERTOR[Value.ValueType.STRING.ordinal()] = toChar();
		CONVERTOR[Value.ValueType.BOOLEAN_ARRAY.ordinal()] = toBooleanArray();
		CONVERTOR[Value.ValueType.INT_ARRAY.ordinal()] = toIntArray();
		CONVERTOR[Value.ValueType.REAL_ARRAY.ordinal()] = toRealArray();
		CONVERTOR[Value.ValueType.STRING_ARRAY.ordinal()] = toCharArray();
	}
	
	public static Value convert(final Value source, final Value.ValueType targetType) {
		if (source == null) {
			throw new NullPointerException("Source item can't be null");
		}
		else if (targetType == null) {
			throw new NullPointerException("Target type can't be null");
		}
		else if (source.getType() == targetType) {
			return source;
		}
		else {
			return CONVERTOR[targetType.ordinal()][source.getType().ordinal()].apply(source);
		}
	}

	private static Function<Value, Value>[] toBoolean() {
		final Function<Value, Value>[]	result = new Function[Value.ValueType.values().length];
		
		result[Value.ValueType.BOOLEAN.ordinal()] = null;
		result[Value.ValueType.INT.ordinal()] = null;
		result[Value.ValueType.REAL.ordinal()] = null;
		result[Value.ValueType.STRING.ordinal()] = null;
		result[Value.ValueType.BOOLEAN_ARRAY.ordinal()] = null;
		result[Value.ValueType.INT_ARRAY.ordinal()] = null;
		result[Value.ValueType.REAL_ARRAY.ordinal()] = null;
		result[Value.ValueType.STRING_ARRAY.ordinal()] = null;
		return result;
	}

	private static Function<Value, Value>[] toInt() {
		final Function<Value, Value>[]	result = new Function[Value.ValueType.values().length];
		
		result[Value.ValueType.BOOLEAN.ordinal()] = null;
		result[Value.ValueType.INT.ordinal()] = null;
		result[Value.ValueType.REAL.ordinal()] = null;
		result[Value.ValueType.STRING.ordinal()] = null;
		result[Value.ValueType.BOOLEAN_ARRAY.ordinal()] = null;
		result[Value.ValueType.INT_ARRAY.ordinal()] = null;
		result[Value.ValueType.REAL_ARRAY.ordinal()] = null;
		result[Value.ValueType.STRING_ARRAY.ordinal()] = null;
		return result;
	}

	private static Function<Value, Value>[] toReal() {
		final Function<Value, Value>[]	result = new Function[Value.ValueType.values().length];
		
		result[Value.ValueType.BOOLEAN.ordinal()] = null;
		result[Value.ValueType.INT.ordinal()] = null;
		result[Value.ValueType.REAL.ordinal()] = null;
		result[Value.ValueType.STRING.ordinal()] = null;
		result[Value.ValueType.BOOLEAN_ARRAY.ordinal()] = null;
		result[Value.ValueType.INT_ARRAY.ordinal()] = null;
		result[Value.ValueType.REAL_ARRAY.ordinal()] = null;
		result[Value.ValueType.STRING_ARRAY.ordinal()] = null;
		return result;
	}

	private static Function<Value, Value>[] toChar() {
		final Function<Value, Value>[]	result = new Function[Value.ValueType.values().length];
		
		result[Value.ValueType.BOOLEAN.ordinal()] = null;
		result[Value.ValueType.INT.ordinal()] = null;
		result[Value.ValueType.REAL.ordinal()] = null;
		result[Value.ValueType.STRING.ordinal()] = null;
		result[Value.ValueType.BOOLEAN_ARRAY.ordinal()] = null;
		result[Value.ValueType.INT_ARRAY.ordinal()] = null;
		result[Value.ValueType.REAL_ARRAY.ordinal()] = null;
		result[Value.ValueType.STRING_ARRAY.ordinal()] = null;
		return result;
	}

	private static Function<Value, Value>[] toBooleanArray() {
		final Function<Value, Value>[]	result = new Function[Value.ValueType.values().length];
		
		result[Value.ValueType.BOOLEAN.ordinal()] = null;
		result[Value.ValueType.INT.ordinal()] = null;
		result[Value.ValueType.REAL.ordinal()] = null;
		result[Value.ValueType.STRING.ordinal()] = null;
		result[Value.ValueType.BOOLEAN_ARRAY.ordinal()] = null;
		result[Value.ValueType.INT_ARRAY.ordinal()] = null;
		result[Value.ValueType.REAL_ARRAY.ordinal()] = null;
		result[Value.ValueType.STRING_ARRAY.ordinal()] = null;
		return result;
	}

	private static Function<Value, Value>[] toIntArray() {
		final Function<Value, Value>[]	result = new Function[Value.ValueType.values().length];
		
		result[Value.ValueType.BOOLEAN.ordinal()] = null;
		result[Value.ValueType.INT.ordinal()] = null;
		result[Value.ValueType.REAL.ordinal()] = null;
		result[Value.ValueType.STRING.ordinal()] = null;
		result[Value.ValueType.BOOLEAN_ARRAY.ordinal()] = null;
		result[Value.ValueType.INT_ARRAY.ordinal()] = null;
		result[Value.ValueType.REAL_ARRAY.ordinal()] = null;
		result[Value.ValueType.STRING_ARRAY.ordinal()] = null;
		return result;
	}
	
	private static Function<Value, Value>[] toRealArray() {
		final Function<Value, Value>[]	result = new Function[Value.ValueType.values().length];
		
		result[Value.ValueType.BOOLEAN.ordinal()] = null;
		result[Value.ValueType.INT.ordinal()] = null;
		result[Value.ValueType.REAL.ordinal()] = null;
		result[Value.ValueType.STRING.ordinal()] = null;
		result[Value.ValueType.BOOLEAN_ARRAY.ordinal()] = null;
		result[Value.ValueType.INT_ARRAY.ordinal()] = null;
		result[Value.ValueType.REAL_ARRAY.ordinal()] = null;
		result[Value.ValueType.STRING_ARRAY.ordinal()] = null;
		return result;
	}

	private static Function<Value, Value>[] toCharArray() {
		final Function<Value, Value>[]	result = new Function[Value.ValueType.values().length];
		
		result[Value.ValueType.BOOLEAN.ordinal()] = null;
		result[Value.ValueType.INT.ordinal()] = null;
		result[Value.ValueType.REAL.ordinal()] = null;
		result[Value.ValueType.STRING.ordinal()] = null;
		result[Value.ValueType.BOOLEAN_ARRAY.ordinal()] = null;
		result[Value.ValueType.INT_ARRAY.ordinal()] = null;
		result[Value.ValueType.REAL_ARRAY.ordinal()] = null;
		result[Value.ValueType.STRING_ARRAY.ordinal()] = null;
		return result;
	}
}

