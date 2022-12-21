package chav1961.bt.security.interfaces;

import java.util.regex.Pattern;

public enum AlgorithmType {
	ALGORITHM_PARAMETER_GENERATOR("AlgorithmParameterGenerator"),
	ALGORITHM_PARAMETERS("AlgorithmParameters"),
	CERT_PATH_BUILDER("CertPathBuilder"),
	CERT_PATH_VALIDATOR("CertPathValidator"),
	CERT_STORE("CertStore"),
	CERTIFICATE_FACTORY("CertificateFactory"),
	CIPHER("Cipher"),
	CONFIGURATION("Configuration"),
	GSS_API_MECHANISM("GssApiMechanism"),
	KEY_AGREEMENT("KeyAgreement"),
	KEY_FACTORY("KeyFactory"),
	KEY_GENERATOR("KeyGenerator"),
	KEY_INFO_FACTORY("KeyInfoFactory"),
	KEY_MANAGER_FACTORY("KeyManagerFactory"),
	KEY_PAIR_GENERATOR("KeyPairGenerator"),
	KEY_STORE("KeyStore"),
	MAC("Mac"),
	MESSAGE_DIGEST("MessageDigest"),
	POLICY("Policy"),
	SASL_CLIENT_FACTORY("SaslClientFactory"),
	SASL_SERVER_FACTORY("SaslServerFactory"),
	SECURE_RANDOM("SecureRandom"),
	SECRET_KEY_FACTORY("SecretKeyFactory"),
	SIGNATURE("Signature"),
	SSL_CONTEXT("SSLContext"),
	TERMINAL_FACTORY("TerminalFactory"),
	TRANSFORM_SERVICE("TransformService"),
	TRUST_MANAGER_FACTORY("TrustManagerFactory"),
	XML_SIGNATURE_FACTORY("XMLSignatureFactory"),
	;
	
	private final Pattern	serviceName;
	
	private AlgorithmType(final String serviceNamePattern) {
		this.serviceName = Pattern.compile(serviceNamePattern);
	}
	
	public Pattern getServiceNamePattern() {
		return serviceName;
	}
}
