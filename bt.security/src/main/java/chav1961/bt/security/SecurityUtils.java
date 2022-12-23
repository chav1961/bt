package chav1961.bt.security;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Provider;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import chav1961.bt.security.keystore.KeyStoreController;

public class SecurityUtils {
	private SecurityUtils() {
		
	}
	
	public SecurityUtils and(final byte[] source) {
		return this;
	}

	public SecurityUtils and(final InputStream source) {
		return this;
	}

	public SecurityUtils and(final OutputStream source) {
		return this;
	}
	
	public SecurityUtils and(final KeyStoreController ks) {
		return this;
	}

	public SecurityUtils and(final Provider provider) {
		return this;
	}
	
	public SecurityUtils loadCertificate() {
		return this;
	}

	public SecurityUtils unloadCertificate() {
		return this;
	}
	
	public SecurityUtils encrypt(final String keyAlias, final char[] password) {
		return this;
	}

	public SecurityUtils encrypt(final String keyAlias, final char[] password, final IvParameterSpec spec) {
		return this;
	}

	public SecurityUtils encrypt(final String algorithm, final String keyAlias, final char[] password, final IvParameterSpec spec) {
		return this;
	}
	
	public SecurityUtils encrypt(final SecretKey key) {
		return this;
	}
	
	public SecurityUtils encrypt(final SecretKey key, final IvParameterSpec spec) {
		return this;
	}

	public SecurityUtils encrypt(final String algorithm, final SecretKey key, final IvParameterSpec spec) {
		return this;
	}
	
	public SecurityUtils decrypt(final String keyAlias, final char[] password) {
		return this;
	}

	public SecurityUtils decrypt(final String keyAlias, final char[] password, final IvParameterSpec spec) {
		return this;
	}

	public SecurityUtils decrypt(final String algorithm, final String keyAlias, final char[] password, final IvParameterSpec spec) {
		return this;
	}
	
	public SecurityUtils decrypt(final SecretKey key) {
		return this;
	}
	
	public SecurityUtils decrypt(final SecretKey key, final IvParameterSpec spec) {
		return this;
	}
	
	public SecurityUtils decrypt(final String algorithm, final SecretKey key, final IvParameterSpec spec) {
		return this;
	}
	
	public byte[] toByteArray() {
		return null;
	}
	
	public InputStream toInputStream() {
		return null;
	}

	public OutputStream toOutputStream() {
		return null;
	}
	
	
	public static SecurityUtils with(final byte[] source) {
		return new SecurityUtils().and(source);
	}

	public static SecurityUtils with(final InputStream source) {
		return new SecurityUtils().and(source);
	}

	public static SecurityUtils with(final OutputStream source) {
		return new SecurityUtils().and(source);
	}
	
	public static SecurityUtils with(final KeyStoreController ks) {
		return new SecurityUtils().and(ks);
	}

	public static SecurityUtils with(final Provider provider) {
		return new SecurityUtils().and(provider);
	}
}
