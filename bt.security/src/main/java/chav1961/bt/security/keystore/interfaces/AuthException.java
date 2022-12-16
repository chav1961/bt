package chav1961.bt.security.keystore.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;

public class AuthException extends ContentException {
	private static final long serialVersionUID = 7147107352157115851L;

	/**
	 * <p>Constructor of the class</p>
	 */
	public AuthException() {
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 */
	public AuthException(final String message) {
		super(message);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param cause exception cause
	 */
	public AuthException(final Throwable cause) {
		super(cause);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 * @param cause exception cause
	 */
	public AuthException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
