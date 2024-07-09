package chav1961.bt.matrix.macros.runtime.interfaces;

import chav1961.bt.matrix.macros.runtime.interfaces.Value.ValueType;

public interface ProgramStack {
	void pushBlock();
	int getBlockDepth();
	void declare(final int name, final Value.ValueType type);
	ValueType getVarType(final int name);
	Value getVarValue(final int name);
	void setVarValue(final int name, final Value value);
	void popBlock();
	void pushStackValue(final Value value);
	Value getStackValue();
	Value popStackValue();
}
