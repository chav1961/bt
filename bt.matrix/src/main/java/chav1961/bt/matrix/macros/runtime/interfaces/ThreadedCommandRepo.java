package chav1961.bt.matrix.macros.runtime.interfaces;


import chav1961.bt.matrix.macros.runtime.CommandList.CommandType;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;

public interface ThreadedCommandRepo {
	public static interface CommandRepoExecutor {
		void execute(MacrosRuntime rt) throws CalculationException, ContentException;
	}
	
	void reset();	
	ThreadedCommandRepo addCommand(CommandType type);
	ThreadedCommandRepo addCommand(CommandType type, Object... parameters);
	void registerForwardLabel(final int label);	
	void registerBackwardLabel(final int label);
	CommandRepoExecutor build();
}
