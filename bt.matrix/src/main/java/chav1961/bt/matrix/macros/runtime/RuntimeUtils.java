package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.Value;

public class RuntimeUtils {
	public static Value convert(Value source, Value.ValueType targetType) {
		if (source.getType() == targetType) {
			return source;
		}
		else {
			return null;
		}
	}
}
