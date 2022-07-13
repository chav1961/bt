package chav1961.bt.paint.script.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;

public class ScriptException extends ContentException {
	private static final long serialVersionUID = 5638644351663305739L;

	public ScriptException() {
		super();
	}

	public ScriptException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptException(String message) {
		super(message);
	}

	public ScriptException(Throwable cause) {
		super(cause);
	}
}
