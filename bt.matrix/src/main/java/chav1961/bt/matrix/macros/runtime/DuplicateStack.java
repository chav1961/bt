package chav1961.bt.matrix.macros.runtime;

import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;
import chav1961.bt.matrix.macros.runtime.interfaces.Value;
import chav1961.purelib.basic.exceptions.CalculationException;

public class DuplicateStack extends AbstractNonResumedCommand {
	public static final DuplicateStack	SINGLETON = new DuplicateStack();
	
	private DuplicateStack() {
		
	}

	@Override
	public long execute(final MacrosRuntime rt) throws CalculationException {
		try {
			final Value 	source = (Value) rt.getProgramStack().getStackValue();
			
			rt.getProgramStack().pushStackValue(source != null ? (Value)source.clone() : null);
			return 1;
		} catch (CloneNotSupportedException e) {
			throw new CalculationException(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
