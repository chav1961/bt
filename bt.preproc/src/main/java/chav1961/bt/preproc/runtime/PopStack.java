package chav1961.bt.preproc.runtime;

import chav1961.bt.preproc.runtime.interfaces.MacrosRuntime;
import chav1961.purelib.basic.exceptions.CalculationException;

public class PopStack extends AbstractNonResumedCommand {
	public static final PopStack	SINGLETON = new PopStack(); 
	
	private PopStack() {
	}

	@Override
	public long execute(final MacrosRuntime rt) throws CalculationException {
		rt.getProgramStack().popStackValue();
		return 1;
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
