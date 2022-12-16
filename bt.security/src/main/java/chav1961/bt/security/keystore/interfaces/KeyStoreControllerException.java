package chav1961.bt.security.keystore.interfaces;

import chav1961.purelib.basic.exceptions.EnvironmentException;

public class KeyStoreControllerException extends EnvironmentException {
	private static final long serialVersionUID = 5345405103220321188L;

	/**
	 * <p>Constructor of the class</p>
	 */
	public KeyStoreControllerException() {
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 */
	public KeyStoreControllerException(final String message) {
		super(message);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param cause exception cause
	 */
	public KeyStoreControllerException(final Throwable cause) {
		super(cause);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 * @param cause exception cause
	 */
	public KeyStoreControllerException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
}
