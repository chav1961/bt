package chav1961.bt.preproc.runtime.interfaces;

import chav1961.bt.preproc.runtime.interfaces.Value.ValueType;

public interface ProgramStack {
	void pushBlock();
	int getBlockDepth();
	void declare(final int name, final Value.ValueType type);
	boolean hasVar(final int name);
	ValueType getVarType(final int name);
	Value getVarValue(final int name);
	void setVarValue(final int name, final Value value);
	void popBlock();
	void pushStackValue(final Value value);
	int getStackDepth();
	Value getStackValue();
	void setStackValue(final Value value);
	Value popStackValue();
}
