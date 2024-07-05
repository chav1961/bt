package chav1961.bt.matrix.macros.runtime.interfaces;

public interface ProgramStack {
	void push();
	void declare(final int name, final Value.ValueType type, final Value value);
	Value getVarValue(final int name);
	void setVarValue(final int name, final Value value);
	void pop();
	void pushStackValue(final Value value);
	Value getStackValue();
	Value popStackValue();
}
