package chav1961.bt.preproc.runtime;

import chav1961.bt.preproc.runtime.interfaces.MacrosRuntime;
import chav1961.bt.preproc.runtime.interfaces.Value;
import chav1961.bt.preproc.runtime.interfaces.ValueArray;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;

public class AssignIndex extends AbstractNonResumedCommand {
	private final int	varName;
	
	public AssignIndex(final int varName) {
		this.varName = varName;
	}

	@Override
	public long execute(MacrosRuntime rt) throws CalculationException {
		try {
			final Value	val = rt.getProgramStack().popStackValue();
			final int 	index = rt.getProgramStack().popStackValue().getValue(long.class).intValue();

			switch (rt.getProgramStack().getVarType(varName).getComponentType()) {
				case BOOLEAN	:
					((ValueArray)rt.getProgramStack().getVarValue(varName)).setValue(index, boolean.class, val.getValue(boolean.class));
					break;
				case INT		:
					((ValueArray)rt.getProgramStack().getVarValue(varName)).setValue(index, long.class, val.getValue(long.class));
					break;
				case REAL		:
					((ValueArray)rt.getProgramStack().getVarValue(varName)).setValue(index, double.class, val.getValue(double.class));
					break;
				case STRING		:
					((ValueArray)rt.getProgramStack().getVarValue(varName)).setValue(index, char[].class, val.getValue(char[].class));
					break;
				default :
					throw new UnsupportedOperationException("Value component type ["+rt.getProgramStack().getVarType(varName).getComponentType()+"] is not suported yet");
			}
			return 1;
		} catch (ContentException e) {
			throw new CalculationException(e); 
		}
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}
}
