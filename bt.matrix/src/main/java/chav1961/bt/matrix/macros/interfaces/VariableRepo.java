package chav1961.bt.matrix.macros.interfaces;

public interface VariableRepo {
	<T> void addVariable(int nameId, Class<T> type);		
	boolean hasVariable(int nameId);
	<T> Class<T> getVariableType(int nameId);
	<T> T getVariable(int nameId);
	<T> void setVariable(int nameId, T value);
	int[] getNameIds();
}