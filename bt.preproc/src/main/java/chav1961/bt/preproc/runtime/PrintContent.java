package chav1961.bt.preproc.runtime;

import chav1961.bt.preproc.runtime.interfaces.MacrosRuntime;
import chav1961.bt.preproc.runtime.interfaces.Value;
import chav1961.bt.preproc.runtime.interfaces.Value.ValueType;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;

public class PrintContent extends AbstractNonResumedCommand {
	private final boolean	diagnosticStream;
	private final boolean	throwError;
	
	public PrintContent(final boolean diagnosticStream, final boolean throwError) {
		this.diagnosticStream = diagnosticStream;
		this.throwError = throwError;
	}

	@Override
	public long execute(final MacrosRuntime rt) throws CalculationException {
		try {
			final Value		val = rt.getProgramStack().popStackValue();
			final char[]	toPrint = RuntimeUtils.convert(val, ValueType.STRING).getValue(char[].class);
			
			if (diagnosticStream) {
				rt.getPrintStream().println(toPrint);
				if (throwError) {
					throw new CalculationException(new String(toPrint));
				}
			}
			else {
				rt.getBuffer().append(toPrint).append(System.lineSeparator());
			}			
			return 1;
		} catch (ContentException e) {
			throw new CalculationException(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public ControlType getControlType() {
		return ControlType.SEQUENCE;
	}

}
