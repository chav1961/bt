package chav1961.bt.lucenewrapper.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;

public class SearchRepositoryException extends ContentException {
	private static final long serialVersionUID = 2519928505914164083L;

	/**
	 * <p>Constructor of the class</p>
	 */
	public SearchRepositoryException() {
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 */
	public SearchRepositoryException(final String message) {
		super(message);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param cause exception cause
	 */
	public SearchRepositoryException(final Throwable cause) {
		super(cause);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 * @param cause exception cause
	 */
	public SearchRepositoryException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
}
