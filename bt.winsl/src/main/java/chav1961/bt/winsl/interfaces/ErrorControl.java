package chav1961.bt.winsl.interfaces;

import chav1961.bt.winsl.utils.JavaServiceDescriptor;

public enum ErrorControl {
	ignore(JavaServiceDescriptor.SERVICE_ERROR_IGNORE),
	normal(JavaServiceDescriptor.SERVICE_ERROR_NORMAL),
	severe(JavaServiceDescriptor.SERVICE_ERROR_SEVERE),
	critical(JavaServiceDescriptor.SERVICE_ERROR_CRITICAL);
	
	private final int	mode;
	
	ErrorControl(final int mode) {
		this.mode = mode;
	}
	
	public int getErrorControl() {
		return mode;
	}
}