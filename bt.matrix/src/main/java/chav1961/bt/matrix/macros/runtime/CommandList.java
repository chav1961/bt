package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.Command;
import chav1961.bt.matrix.macros.runtime.interfaces.CommandRepo;
import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;
import chav1961.purelib.basic.exceptions.CalculationException;

public class CommandList {

	public static class CommandCursor {
		private final CommandRepo	repo = null;
		private long	commandAddress = 0;
		
		public boolean executeCommand(final MacrosRuntime rt) throws CalculationException {
			Command		c;
			long		delta;
			do {
				c = repo.getCommand(commandAddress);
				delta = c.execute(rt);
				
				switch (c.getControlType()) {
					case BACKWARD_BRUNCH : case BACKWARD_CONDITIONAL : case SEQUENCE :
						commandAddress += delta; 
						break;
					case FORWARD_BRUNCH : case FORWARD_CONDITIONAL :
						break;
					default :
						throw new UnsupportedOperationException("Control type ["+c.getControlType()+"] is not supported yet");
				}
				
			} while (!c.resumeRequired(rt));
			return true;
		}
	}
}
