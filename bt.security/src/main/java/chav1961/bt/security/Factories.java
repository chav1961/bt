package chav1961.bt.security;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import chav1961.bt.security.interfaces.SecurityProcessingException;
import chav1961.bt.security.internal.InternalUtils;
import chav1961.purelib.basic.Utils;

public class Factories {
	// ---
	
	public static PublicKey toPublicKey(final byte[] content) throws SecurityProcessingException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			return toPublicKey(InternalUtils.PROPS.getProperty(InternalUtils.PROP_KEYPAIR_ALGORITHM), content);
		}
	}

	public static PublicKey toPublicKey(final String algorithm, final byte[] content) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			final X509EncodedKeySpec 	pkSpec= new X509EncodedKeySpec(content);
			
			try{final KeyFactory 		kf = KeyFactory.getInstance(algorithm);
			
				return kf.generatePublic(pkSpec);		
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Algorithm name ["+algorithm+"] is not known"); 
			} catch (InvalidKeySpecException e) {
				throw new SecurityProcessingException(e.getLocalizedMessage(), e);
			}
		}
	}

	public static PublicKey toPublicKey(final Provider provider, final byte[] content) throws SecurityProcessingException {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			return toPublicKey(InternalUtils.PROPS.getProperty(InternalUtils.PROP_KEYPAIR_ALGORITHM), provider, content);
		}
	}

	public static PublicKey toPublicKey(final String algorithm, final Provider provider, final byte[] content) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			final X509EncodedKeySpec 	pkSpec= new X509EncodedKeySpec(content);
			
			try{final KeyFactory 		kf = KeyFactory.getInstance(algorithm, provider);
			
				return kf.generatePublic(pkSpec);		
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Algorithm name ["+algorithm+"] is not known"); 
			} catch (InvalidKeySpecException e) {
				throw new SecurityProcessingException(e.getLocalizedMessage(), e);
			}
		}
	}

	// ---
	
	public static PrivateKey toPrivateKey(final byte[] content) throws SecurityProcessingException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			return toPrivateKey(InternalUtils.PROPS.getProperty(InternalUtils.PROP_KEYPAIR_ALGORITHM), content);
		}
	}

	public static PrivateKey toPrivateKey(final String algorithm, final byte[] content) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			final X509EncodedKeySpec 	pkSpec= new X509EncodedKeySpec(content);
			
			try{final KeyFactory 		kf = KeyFactory.getInstance(algorithm);
			
				return kf.generatePrivate(pkSpec);		
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Algorithm name ["+algorithm+"] is not known"); 
			} catch (InvalidKeySpecException e) {
				throw new SecurityProcessingException(e.getLocalizedMessage(), e);
			}
		}
	}

	public static PrivateKey toPrivateKey(final Provider provider, final byte[] content) throws SecurityProcessingException {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			return toPrivateKey(InternalUtils.PROPS.getProperty(InternalUtils.PROP_KEYPAIR_ALGORITHM), provider, content);
		}
	}

	public static PrivateKey toPrivateKey(final String algorithm, final Provider provider, final byte[] content) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			final X509EncodedKeySpec 	pkSpec= new X509EncodedKeySpec(content);
			
			try{final KeyFactory 		kf = KeyFactory.getInstance(algorithm, provider);
			
				return kf.generatePrivate(pkSpec);		
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Algorithm name ["+algorithm+"] is not known"); 
			} catch (InvalidKeySpecException e) {
				throw new SecurityProcessingException(e.getLocalizedMessage(), e);
			}
		}
	}

	// ---
	
	public static Certificate toCertificate(final byte[] content) throws SecurityProcessingException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			return toCertificate(InternalUtils.PROPS.getProperty(InternalUtils.PROP_CERTIFICATE_ALGORITHM), content);
		}
	}

	public static Certificate toCertificate(final String algorithm, final byte[] content) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			try{final CertificateFactory 	cf = CertificateFactory.getInstance(algorithm);
			
				return cf.generateCertificate(new ByteArrayInputStream(content));
			} catch (CertificateException e) {
				throw new SecurityProcessingException(e.getLocalizedMessage(), e);
			}
		}
	}

	public static Certificate toCertificate(final Provider provider, final byte[] content) throws SecurityProcessingException {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			return toCertificate(InternalUtils.PROPS.getProperty(InternalUtils.PROP_CERTIFICATE_ALGORITHM), provider, content);
		}
	}

	public static Certificate toCertificate(final String algorithm, final Provider provider, final byte[] content) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			try{final CertificateFactory 	cf = CertificateFactory.getInstance(algorithm, provider);
			
				return cf.generateCertificate(new ByteArrayInputStream(content));
			} catch (CertificateException e) {
				throw new SecurityProcessingException(e.getLocalizedMessage(), e);
			}
		}
	}

	// ---
	
	public static CertPath toCertPath(final byte[] content) throws SecurityProcessingException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			return toCertPath(InternalUtils.PROPS.getProperty(InternalUtils.PROP_CERTIFICATE_ALGORITHM), content);
		}
	}

	public static CertPath toCertPath(final String algorithm, final byte[] content) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			try{final CertificateFactory 	cf = CertificateFactory.getInstance(algorithm);
			
				return cf.generateCertPath(new ByteArrayInputStream(content));
			} catch (CertificateException e) {
				throw new SecurityProcessingException(e.getLocalizedMessage(), e);
			}
		}
	}

	public static CertPath toCertPath(final Provider provider, final byte[] content) throws SecurityProcessingException {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			return toCertPath(InternalUtils.PROPS.getProperty(InternalUtils.PROP_CERTIFICATE_ALGORITHM), provider, content);
		}
	}

	public static CertPath toCertPath(final String algorithm, final Provider provider, final byte[] content) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Key content can't be null or empty array"); 
		}
		else {
			try{final CertificateFactory 	cf = CertificateFactory.getInstance(algorithm, provider);
			
				return cf.generateCertPath(new ByteArrayInputStream(content));
			} catch (CertificateException e) {
				throw new SecurityProcessingException(e.getLocalizedMessage(), e);
			}
		}
	}
}
