package chav1961.bt.matrix.macros.runtime;

import java.util.function.Function;

import chav1961.bt.matrix.macros.runtime.interfaces.Value;
import chav1961.purelib.basic.exceptions.ContentException;

public class RuntimeUtils {
	@FunctionalInterface
	private static interface Conversion {
		Value convert(Value src) throws ContentException;
	}
	
	private static final Conversion[][]	CONVERTOR;
	
	static {
		CONVERTOR = new Conversion[Value.ValueType.values().length][];
		CONVERTOR[Value.ValueType.BOOLEAN.ordinal()] = toBoolean();
		CONVERTOR[Value.ValueType.INT.ordinal()] = toInt();
		CONVERTOR[Value.ValueType.REAL.ordinal()] = toReal();
		CONVERTOR[Value.ValueType.STRING.ordinal()] = toChar();
		CONVERTOR[Value.ValueType.BOOLEAN_ARRAY.ordinal()] = toBooleanArray();
		CONVERTOR[Value.ValueType.INT_ARRAY.ordinal()] = toIntArray();
		CONVERTOR[Value.ValueType.REAL_ARRAY.ordinal()] = toRealArray();
		CONVERTOR[Value.ValueType.STRING_ARRAY.ordinal()] = toCharArray();
	}
	
	public static Value convert(final Value source, final Value.ValueType targetType) throws ContentException {
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
			return CONVERTOR[targetType.ordinal()][source.getType().ordinal()].convert(source);
		}
	}

	private static Conversion[] toBoolean() {
		final Conversion[]	result = new Conversion[Value.ValueType.values().length];
		
		result[Value.ValueType.BOOLEAN.ordinal()] = (src)->src;
		result[Value.ValueType.INT.ordinal()] = null;
		result[Value.ValueType.REAL.ordinal()] = null;
		result[Value.ValueType.STRING.ordinal()] = null;
		result[Value.ValueType.BOOLEAN_ARRAY.ordinal()] = null;
		result[Value.ValueType.INT_ARRAY.ordinal()] = null;
		result[Value.ValueType.REAL_ARRAY.ordinal()] = null;
		result[Value.ValueType.STRING_ARRAY.ordinal()] = null;
		return result;
	}

	private static Conversion[] toInt() {
		final Conversion[]	result = new Conversion[Value.ValueType.values().length];
		
		result[Value.ValueType.BOOLEAN.ordinal()] = null;
		result[Value.ValueType.INT.ordinal()] = (src)->src;
		result[Value.ValueType.REAL.ordinal()] = null;
		result[Value.ValueType.STRING.ordinal()] = null;
		result[Value.ValueType.BOOLEAN_ARRAY.ordinal()] = null;
		result[Value.ValueType.INT_ARRAY.ordinal()] = null;
		result[Value.ValueType.REAL_ARRAY.ordinal()] = null;
		result[Value.ValueType.STRING_ARRAY.ordinal()] = null;
		return result;
	}

	private static Conversion[] toReal() {
		final Conversion[]	result = new Conversion[Value.ValueType.values().length];
		
		result[Value.ValueType.BOOLEAN.ordinal()] = null;
		result[Value.ValueType.INT.ordinal()] = null;
		result[Value.ValueType.REAL.ordinal()] = (src)->src;
		result[Value.ValueType.STRING.ordinal()] = null;
		result[Value.ValueType.BOOLEAN_ARRAY.ordinal()] = null;
		result[Value.ValueType.INT_ARRAY.ordinal()] = null;
		result[Value.ValueType.REAL_ARRAY.ordinal()] = null;
		result[Value.ValueType.STRING_ARRAY.ordinal()] = null;
		return result;
	}

	private static Conversion[] toChar() {
		final Conversion[]	result = new Conversion[Value.ValueType.values().length];
		
		result[Value.ValueType.BOOLEAN.ordinal()] = null;
		result[Value.ValueType.INT.ordinal()] = (src)->Value.Factory.newReadOnlyInstance(String.valueOf(src.getValue(long.class).longValue()));
		result[Value.ValueType.REAL.ordinal()] = (src)->Value.Factory.newReadOnlyInstance(String.valueOf(src.getValue(double.class).doubleValue()));
		result[Value.ValueType.STRING.ordinal()] = (src)->src;
		result[Value.ValueType.BOOLEAN_ARRAY.ordinal()] = null;
		result[Value.ValueType.INT_ARRAY.ordinal()] = null;
		result[Value.ValueType.REAL_ARRAY.ordinal()] = null;
		result[Value.ValueType.STRING_ARRAY.ordinal()] = null;
		return result;
	}

	private static Conversion[] toBooleanArray() {
		final Conversion[]	result = new Conversion[Value.ValueType.values().length];
		
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

	private static Conversion[] toIntArray() {
		final Conversion[]	result = new Conversion[Value.ValueType.values().length];
		
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
	
	private static Conversion[] toRealArray() {
		final Conversion[]	result = new Conversion[Value.ValueType.values().length];
		
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

	private static Conversion[] toCharArray() {
		final Conversion[]	result = new Conversion[Value.ValueType.values().length];
		
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

