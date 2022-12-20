package chav1961.bt.security.interfaces;

import java.util.regex.Pattern;

public enum AlgorithmType {
	RANDOM_GENERATOR("S"),
	KEY_STORE("KeyStore");
	
	private final Pattern	serviceName;
	
	private AlgorithmType(final String serviceNamePattern) {
		this.serviceName = Pattern.compile(serviceNamePattern);
	}
	
	public Pattern getServiceNamePattern() {
		return serviceName;
	}
}
