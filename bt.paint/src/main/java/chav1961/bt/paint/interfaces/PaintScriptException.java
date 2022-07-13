package chav1961.bt.paint.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;

public class PaintScriptException extends ContentException {
	private static final long serialVersionUID = 5638644351663305739L;

	public PaintScriptException() {
		super();
	}

	public PaintScriptException(String message, Throwable cause) {
		super(message, cause);
	}

	public PaintScriptException(String message) {
		super(message);
	}

	public PaintScriptException(Throwable cause) {
		super(cause);
	}
}
