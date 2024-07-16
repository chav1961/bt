package chav1961.bt.preproc.runtime.interfaces;


import chav1961.bt.preproc.runtime.CommandList.CommandType;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;

public interface ThreadedCommandRepo {
	public static interface CommandRepoExecutor {
		void execute(MacrosRuntime rt) throws CalculationException, ContentException;
	}
	
	void reset();	
	ThreadedCommandRepo addCommand(CommandType type);
	ThreadedCommandRepo addCommand(CommandType type, Object... parameters);
	CommandRepoExecutor build();
}
