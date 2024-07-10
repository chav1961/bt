package chav1961.bt.matrix.macros.runtime.interfaces;

import chav1961.bt.matrix.macros.runtime.CommandList.CommandType;

public interface ThreadedCommandRepo {
	void addCommand(CommandType type);
	void addCommand(CommandType type, Object... parameters);
	void registerForwardLabel(final int label);	
	void registerBackwardLabel(final int label);	
}
