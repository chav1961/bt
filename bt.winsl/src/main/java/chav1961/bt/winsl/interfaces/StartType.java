package chav1961.bt.winsl.interfaces;

import chav1961.bt.winsl.utils.JavaServiceDescriptor;

public enum StartType {
	autoStart(JavaServiceDescriptor.SERVICE_AUTO_START),
	bootStart(JavaServiceDescriptor.SERVICE_BOOT_START),
	demandStart(JavaServiceDescriptor.SERVICE_DEMAND_START),
	systemStart(JavaServiceDescriptor.SERVICE_SYSTEM_START),
	disabled(JavaServiceDescriptor.SERVICE_DISABLED);
	
	private final int	mode;
	
	StartType(final int mode) {
		this.mode = mode;
	}
	
	public int getStartType() {
		return mode;
	}
}