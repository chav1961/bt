package chav1961.bt.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import chav1961.bt.security.encription.EncriptionUtils;
import chav1961.bt.security.interfaces.KeyStoreControllerException;
import chav1961.bt.security.interfaces.SecurityProcessingException;
import chav1961.bt.security.internal.InternalUtils;
import chav1961.bt.security.keystore.KeyStoreController;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;

public class SecurityUtils {
	private static final byte[]	DUMMY_IV = new byte[0];
	
	private static enum InstanceState {
		BEFORE_ACTION,
		ACTION,
		AFTER_ACTION;
	}

	private static enum ActionType {
		MESSAGE_DIGEST,
		SIGNATURE_SIGN,
		SIGNATURE_VERIFY;
	}
	
	
	private InstanceState		state = InstanceState.BEFORE_ACTION;
	private ActionType			actionType = null;
	private byte[]				sourceByte = null;
	private Provider			sourceProvider = null;
	private InputStream			sourceInputStream = null;
	private OutputStream		sourceOutputStream = null;
	private KeyStoreController	sourceKeyStoreController = null;
	private MessageDigest		messageDigest = null;
	private Signature			signature = null;
	private boolean				result; 
	
	private SecurityUtils() {
		
	}
	
	public SecurityUtils and(final byte[] source) {
		if (source == null) {
			throw new NullPointerException("Source byte array can't be null");
		}
		else if (state != InstanceState.BEFORE_ACTION) {
			throw new IllegalStateException("Can't call this method after action methods");
		}
		else if (sourceByte != null) {
			throw new IllegalStateException("Can't call this method twice");
		}
		else {
			sourceByte = source;
			return this;
		}
	}

	public SecurityUtils and(final InputStream source) {
		if (source == null) {
			throw new NullPointerException("Source input stream can't be null");
		}
		else if (state != InstanceState.BEFORE_ACTION) {
			throw new IllegalStateException("Can't call this method after action methods");
		}
		else if (sourceInputStream != null) {
			throw new IllegalStateException("Can't call this method twice");
		}
		else {
			sourceInputStream = source;
			return this;
		}
	}

	public SecurityUtils and(final OutputStream source) {
		if (source == null) {
			throw new NullPointerException("Source output stream can't be null");
		}
		else if (state != InstanceState.BEFORE_ACTION) {
			throw new IllegalStateException("Can't call this method after action methods");
		}
		else if (sourceOutputStream != null) {
			throw new IllegalStateException("Can't call this method twice");
		}
		else {
			sourceOutputStream = source;
			return this;
		}
	}
	
	public SecurityUtils and(final KeyStoreController ks) {
		if (ks == null) {
			throw new NullPointerException("Source key store controller can't be null");
		}
		else if (state != InstanceState.BEFORE_ACTION) {
			throw new IllegalStateException("Can't call this method after action methods");
		}
		else if (sourceKeyStoreController != null) {
			throw new IllegalStateException("Can't call this method twice");
		}
		else {
			sourceKeyStoreController = ks;
			return this;
		}
	}

	public SecurityUtils and(final Provider provider) {
		if (provider == null) {
			throw new NullPointerException("Source provider can't be null");
		}
		else if (state != InstanceState.BEFORE_ACTION) {
			throw new IllegalStateException("Can't call this method after action methods");
		}
		else if (sourceProvider != null) {
			throw new IllegalStateException("Can't call this method twice");
		}
		else {
			sourceProvider = provider;
			return this;
		}
	}

	// ----------------------------
	
	public SecurityUtils encrypt(final String keyAlias, final char[] password) {
		if (Utils.checkEmptyOrNullString(keyAlias)) {
			throw new IllegalArgumentException("Key alias can't be null or empty");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password can't be null or empty array");
		}
		else if (sourceKeyStoreController == null) {
			throw new IllegalStateException("Key store controller is not defined. Call add(KeyStoreController) method first");
		}
		else {
			return encrypt(keyAlias, password, new IvParameterSpec(DUMMY_IV));
		}
	}

	public SecurityUtils encrypt(final String keyAlias, final char[] password, final IvParameterSpec spec) {
		if (Utils.checkEmptyOrNullString(keyAlias)) {
			throw new IllegalArgumentException("Key alias can't be null or empty");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password can't be null or empty array");
		}
		else if (spec == null) {
			throw new NullPointerException("Initial vector spec can't be null");
		}
		else if (sourceKeyStoreController == null) {
			throw new IllegalStateException("Key store controller is not defined. Call add(KeyStoreController) method first");
		}
		else {
			return encrypt(InternalUtils.PROPS.getProperty(InternalUtils.PROP_AES_ENCRYPTION_ALGORITHM), keyAlias, password, spec);
		}
	}

	public SecurityUtils encrypt(final String algorithm, final String keyAlias, final char[] password, final IvParameterSpec spec) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm can't be null or empty");
		}
		else if (Utils.checkEmptyOrNullString(keyAlias)) {
			throw new IllegalArgumentException("Key alias can't be null or empty");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password can't be null or empty array");
		}
		else if (spec == null) {
			throw new NullPointerException("Initial vector spec can't be null");
		}
		else if (sourceKeyStoreController == null) {
			throw new IllegalStateException("Key store controller is not defined. Call add(KeyStoreController) method first");
		}
		else if (state != InstanceState.BEFORE_ACTION) {
			throw new IllegalStateException("Can't call this method after action methods");
		}
		else {
			state = InstanceState.ACTION;
			// TODO:
			return this;
		}
	}
	
	public SecurityUtils encrypt(final SecretKey key) {
		if (key == null) {
			throw new NullPointerException("Secret key can't be null");
		}
		else {
			return encrypt(key, new IvParameterSpec(DUMMY_IV));
		}
	}
	
	public SecurityUtils encrypt(final SecretKey key, final IvParameterSpec spec) {
		if (key == null) {
			throw new NullPointerException("Secret key can't be null");
		}
		else if (spec == null) {
			throw new NullPointerException("Intial vector spec can't be null");
		}
		else {
			return encrypt(InternalUtils.PROPS.getProperty(InternalUtils.PROP_AES_ENCRYPTION_ALGORITHM), key, new IvParameterSpec(DUMMY_IV));
		}
	}

	public SecurityUtils encrypt(final String algorithm, final SecretKey key, final IvParameterSpec spec) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty");
		}
		else if (key == null) {
			throw new NullPointerException("Secret key can't be null");
		}
		else if (spec == null) {
			throw new NullPointerException("Intial vector spec can't be null");
		}
		else if (sourceByte == null && sourceInputStream == null) {
			throw new IllegalStateException("Neither byte array nor input stream are not defined. Call add(byte[]) or add(InputStream) method first");
		}
		else if (state != InstanceState.BEFORE_ACTION) {
			throw new IllegalStateException("Can't call this method after action methods");
		}
		else {
			state = InstanceState.ACTION;
			// TODO:
			return this;
		}
	}

	// ----------------------------
	
	public SecurityUtils decrypt(final String keyAlias, final char[] password) {
		if (Utils.checkEmptyOrNullString(keyAlias)) {
			throw new IllegalArgumentException("Key alias can't be null or empty");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password can't be null or empty array");
		}
		else {
			return decrypt(keyAlias, password, new IvParameterSpec(DUMMY_IV));
		}
	}

	public SecurityUtils decrypt(final String keyAlias, final char[] password, final IvParameterSpec spec) {
		if (Utils.checkEmptyOrNullString(keyAlias)) {
			throw new IllegalArgumentException("Key alias can't be null or empty");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password can't be null or empty array");
		}
		else if (spec == null) {
			throw new NullPointerException("Initial vector spec can't be null");
		}
		else {
			return decrypt(InternalUtils.PROPS.getProperty(InternalUtils.PROP_AES_ENCRYPTION_ALGORITHM), keyAlias, password, spec);
		}
	}

	public SecurityUtils decrypt(final String algorithm, final String keyAlias, final char[] password, final IvParameterSpec spec) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm can't be null or empty");
		}
		else if (Utils.checkEmptyOrNullString(keyAlias)) {
			throw new IllegalArgumentException("Key alias can't be null or empty");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password can't be null or empty array");
		}
		else if (spec == null) {
			throw new NullPointerException("Initial vector spec can't be null");
		}
		else if (sourceKeyStoreController == null) {
			throw new IllegalStateException("Key store controller is not defined. Call add(KeyStoreController) method first");
		}
		else if (state != InstanceState.BEFORE_ACTION) {
			throw new IllegalStateException("Can't call this method after action methods");
		}
		else {
			state = InstanceState.ACTION;
			// TODO:
			return this;
		}
	}
	
	public SecurityUtils decrypt(final SecretKey key) {
		if (key == null) {
			throw new NullPointerException("Secret key can't be null");
		}
		else {
			return decrypt(key, new IvParameterSpec(DUMMY_IV));
		}
	}
	
	public SecurityUtils decrypt(final SecretKey key, final IvParameterSpec spec) {
		if (key == null) {
			throw new NullPointerException("Secret key can't be null");
		}
		else if (spec == null) {
			throw new NullPointerException("Intial vector spec can't be null");
		}
		else {
			return decrypt(InternalUtils.PROPS.getProperty(InternalUtils.PROP_AES_ENCRYPTION_ALGORITHM), key, new IvParameterSpec(DUMMY_IV));
		}
	}
	
	public SecurityUtils decrypt(final String algorithm, final SecretKey key, final IvParameterSpec spec) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty");
		}
		else if (key == null) {
			throw new NullPointerException("Secret key can't be null");
		}
		else if (spec == null) {
			throw new NullPointerException("Intial vector spec can't be null");
		}
		else if (sourceByte == null && sourceInputStream == null) {
			throw new IllegalStateException("Neither byte array nor input stream are not defined. Call add(byte[]) or add(InputStream) method first");
		}
		else if (state != InstanceState.BEFORE_ACTION) {
			throw new IllegalStateException("Can't call this method after action methods");
		}
		else {
			state = InstanceState.ACTION;
			// TODO:
			return this;
		}
	}

	// ----------------------------
	
	public SecurityUtils messageDigest() throws SecurityProcessingException {
		return messageDigest(InternalUtils.PROPS.getProperty(InternalUtils.PROP_MESSAGE_DIGEST_ALGORITHM));
	}

	public SecurityUtils messageDigest(final String algorithm) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty");
		}
		else if (sourceByte == null && sourceInputStream == null) {
			throw new IllegalStateException("Neither byte array nor input stream are not defined. Call add(byte[]) or add(InputStream) method first");
		}
		else if (state != InstanceState.BEFORE_ACTION) {
			throw new IllegalStateException("Can't call this method after action methods");
		}
		else {
			try{state = InstanceState.ACTION;
				actionType = ActionType.MESSAGE_DIGEST;
				messageDigest = sourceProvider != null ? MessageDigest.getInstance(algorithm, sourceProvider) : MessageDigest.getInstance(algorithm);
				if (sourceByte != null) {
					messageDigest.update(sourceByte);
				}
				else {
					messageDigest.update(new GrowableByteArray(false).append(sourceInputStream).extract());
				}
			} catch (NoSuchAlgorithmException | IOException exc) {
				throw new SecurityProcessingException(exc.getLocalizedMessage(), exc);
			} 
			return this;
		}
	}

	// ----------------------------
	
	public SecurityUtils sign(final String alias, final char[] password) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(alias)) {
			throw new IllegalArgumentException("Alias can't be null or empty");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password can't be null or empty array");
		}
		else {
			return sign(alias, password, InternalUtils.RANDOM);
		}
	}

	public SecurityUtils sign(final String alias, final char[] password, final SecureRandom random) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(alias)) {
			throw new IllegalArgumentException("Alias can't be null or empty");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password can't be null or empty array");
		}
		else if (random == null) {
			throw new NullPointerException("Random generator can't be null");
		}
		else {
			return sign(InternalUtils.PROPS.getProperty(InternalUtils.PROP_SIGNATURE_ALGORITHM), alias, password, InternalUtils.RANDOM);
		}
	}

	public SecurityUtils sign(final String algorithm, final String alias, final char[] password) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty");
		}
		else if (Utils.checkEmptyOrNullString(alias)) {
			throw new IllegalArgumentException("Alias can't be null or empty");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password can't be null or empty array");
		}
		else {
			return sign(algorithm, alias, password, InternalUtils.RANDOM);
		}
	}

	public SecurityUtils sign(final String algorithm, final String alias, final char[] password, final SecureRandom random) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty");
		}
		else if (Utils.checkEmptyOrNullString(alias)) {
			throw new IllegalArgumentException("Alias can't be null or empty");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password can't be null or empty array");
		}
		else if (random == null) {
			throw new NullPointerException("Random generator can't be null");
		}
		else if (sourceKeyStoreController == null) {
			throw new IllegalStateException("Key store controller is not defined. Call and(KeyStoreController) method first");
		}
		else {
			try{return sign(algorithm, (PrivateKey)sourceKeyStoreController.getRsaKeyEntry(alias, password).getKey(), random);
			} catch (KeyStoreControllerException e) {
				throw new SecurityProcessingException(e.getLocalizedMessage(), e);
			}
		}
	}
	
	public SecurityUtils sign(final PrivateKey key) throws SecurityProcessingException {
		if (key == null) {
			throw new NullPointerException("Private key can't be null");
		}
		else {
			return sign(key, InternalUtils.RANDOM);
		}
	}

	public SecurityUtils sign(final PrivateKey key, final SecureRandom random) throws SecurityProcessingException {
		if (key == null) {
			throw new NullPointerException("Private key can't be null");
		}
		else if (random == null) {
			throw new NullPointerException("Random generator can't be null");
		}
		else {
			return sign(InternalUtils.PROPS.getProperty(InternalUtils.PROP_SIGNATURE_ALGORITHM), key, random);
		}
	}

	public SecurityUtils sign(final String algorithm, final PrivateKey key) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty");
		}
		else if (key == null) {
			throw new NullPointerException("Private key can't be null");
		}
		else {
			return sign(algorithm, key, InternalUtils.RANDOM);
		}
	}

	public SecurityUtils sign(final String algorithm, final PrivateKey key, final SecureRandom random) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty");
		}
		else if (key == null) {
			throw new NullPointerException("Private key can't be null");
		}
		else if (random == null) {
			throw new NullPointerException("Random generator can't be null");
		}
		else if (sourceByte == null && sourceInputStream == null) {
			throw new IllegalStateException("Neither byte array nor input stream are not defined. Call and(byte[]) or and(InputStream) method first");
		}
		else {
			try{state = InstanceState.ACTION;
				actionType = ActionType.SIGNATURE_SIGN;
				signature = sourceProvider != null ? Signature.getInstance(algorithm, sourceProvider) : Signature.getInstance(algorithm);
				signature.initSign(key, random);
				if (sourceByte != null) {
					signature.update(sourceByte);
				}
				else {
					signature.update(new GrowableByteArray(false).append(sourceInputStream).extract());
				}
				return this;
			} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException exc) {
				throw new SecurityProcessingException(exc.getLocalizedMessage(), exc); 
			}
		}
	}

	public SecurityUtils verify(final String alias, final char[] password, final byte[] signature) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(alias)) {
			throw new IllegalArgumentException("Alias can't be null or empty");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password can't be null or empty array");
		}
		else if (signature == null || signature.length == 0) {
			throw new IllegalArgumentException("Signature can't be null or empty array");
		}
		else {
			return verify(InternalUtils.PROPS.getProperty(InternalUtils.PROP_SIGNATURE_ALGORITHM), alias, password, signature);
		}
	}

	public SecurityUtils verify(final String algorithm, final String alias, final char[] password, final byte[] signature) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algoritm name can't be null or empty");
		}
		else if (Utils.checkEmptyOrNullString(alias)) {
			throw new IllegalArgumentException("Alias can't be null or empty");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password can't be null or empty array");
		}
		else if (signature == null || signature.length == 0) {
			throw new IllegalArgumentException("Signature can't be null or empty array");
		}
		else if (sourceKeyStoreController == null) {
			throw new IllegalStateException("Key store controller is not defined. Call and(KeyStoreController) method first");
		}
		else {
			try {
				return verify(algorithm, sourceKeyStoreController.getRsaKeyEntry(alias, password).getCertificateChain()[0], signature);
			} catch (KeyStoreControllerException exc) {
				throw new SecurityProcessingException(exc.getLocalizedMessage(), exc); 
			}
		}
	}
	
	public SecurityUtils verify(final PublicKey key, final byte[] signature) throws SecurityProcessingException {
		if (key == null) {
			throw new NullPointerException("Public key can't be null");
		}
		else if (signature == null || signature.length == 0) {
			throw new IllegalArgumentException("Signature can't be null or empty array");
		}
		else {
			return verify(InternalUtils.PROPS.getProperty(InternalUtils.PROP_SIGNATURE_ALGORITHM), key, signature);
		}
	}

	public SecurityUtils verify(final String algorithm, final PublicKey key, final byte[] sign) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algoritm name can't be null or empty");
		}
		else if (key == null) {
			throw new NullPointerException("Public key can't be null");
		}
		else if (sign == null || sign.length == 0) {
			throw new IllegalArgumentException("Signature can't be null or empty array");
		}
		else if (sourceByte == null && sourceInputStream == null) {
			throw new IllegalStateException("Neither byte array nor input stream are not defined. Call and(byte[]) or and(InputStream) method first");
		}
		else {
			try{state = InstanceState.ACTION;
				actionType = ActionType.SIGNATURE_VERIFY;
				signature = sourceProvider != null ? Signature.getInstance(algorithm, sourceProvider) : Signature.getInstance(algorithm);
				signature.initVerify(key);
				if (sourceByte != null) {
					signature.update(sourceByte);
				}
				else {
					signature.update(new GrowableByteArray(false).append(sourceInputStream).extract());
				}
				result = signature.verify(sign);
				return this;
			} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException exc) {
				throw new SecurityProcessingException(exc.getLocalizedMessage(), exc); 
			}
		}
	}

	public SecurityUtils verify(final Certificate cert, final byte[] sign) throws SecurityProcessingException {
		if (cert == null) {
			throw new NullPointerException("Certificate can't be null");
		}
		else if (sign == null || sign.length == 0) {
			throw new IllegalArgumentException("Signature can't be null or empty array");
		}
		else {
			return verify(InternalUtils.PROPS.getProperty(InternalUtils.PROP_SIGNATURE_ALGORITHM), cert, sign);
		}
	}

	public SecurityUtils verify(final String algorithm, final Certificate cert, final byte[] sign) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algoritm name can't be null or empty");
		}
		else if (cert == null) {
			throw new NullPointerException("Certificate can't be null");
		}
		else if (sign == null || sign.length == 0) {
			throw new IllegalArgumentException("Signature can't be null or empty array");
		}
		else {
			try{state = InstanceState.ACTION;
				actionType = ActionType.SIGNATURE_VERIFY;
				signature = sourceProvider != null ? Signature.getInstance(algorithm, sourceProvider) : Signature.getInstance(algorithm);
				signature.initVerify(cert);
				if (sourceByte != null) {
					signature.update(sourceByte);
				}
				else {
					signature.update(new GrowableByteArray(false).append(sourceInputStream).extract());
				}
				result = signature.verify(sign);
				return this;
			} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException exc) {
				throw new SecurityProcessingException(exc.getLocalizedMessage(), exc); 
			}
		}
	}
	
	// ----------------------------
	
	public byte[] toByteArray() {
		if (state != InstanceState.ACTION) {
			throw new IllegalStateException("Can't call this method before action methods");
		}
		else {
			state = InstanceState.AFTER_ACTION;
			switch (actionType) {
				case SIGNATURE_VERIFY	:
					return null;
				case MESSAGE_DIGEST	: case SIGNATURE_SIGN :
					throw new IllegalStateException("This method ia not appplicable for ["+actionType+"] action type");
				default:
					throw new UnsupportedOperationException("Action type ["+actionType+"] is not supported yet");
			}
		}
	}
	
	public InputStream toInputStream() {
		if (state != InstanceState.ACTION) {
			throw new IllegalStateException("Can't call this method before action methods");
		}
		else {
			state = InstanceState.AFTER_ACTION;
			switch (actionType) {
				case SIGNATURE_VERIFY	:
					return null;
				case MESSAGE_DIGEST	: case SIGNATURE_SIGN :
					throw new IllegalStateException("This method ia not appplicable for ["+actionType+"] action type");
				default:
					throw new UnsupportedOperationException("Action type ["+actionType+"] is not supported yet");
			}
		}
	}

	public OutputStream toOutputStream() {
		if (state != InstanceState.ACTION) {
			throw new IllegalStateException("Can't call this method before action methods");
		}
		else {
			state = InstanceState.AFTER_ACTION;
			switch (actionType) {
				case SIGNATURE_VERIFY	:
					return null;
				case MESSAGE_DIGEST	: case SIGNATURE_SIGN :
					throw new IllegalStateException("This method ia not appplicable for ["+actionType+"] action type");
				default:
					throw new UnsupportedOperationException("Action type ["+actionType+"] is not supported yet");
			}
		}
	}

	public boolean isSuccessful() {
		if (state != InstanceState.ACTION) {
			throw new IllegalStateException("Can't call this method before action methods");
		}
		else {
			state = InstanceState.AFTER_ACTION;
			switch (actionType) {
				case SIGNATURE_VERIFY	:
					return result;
				case MESSAGE_DIGEST	: case SIGNATURE_SIGN :
					throw new IllegalStateException("This method ia not appplicable for ["+actionType+"] action type");
				default:
					throw new UnsupportedOperationException("Action type ["+actionType+"] is not supported yet");
			}
		}
	}
	
	public static SecurityUtils with(final byte[] source) {
		if (source == null) {
			throw new NullPointerException("Source byte array can't be null");
		}
		else {
			return new SecurityUtils().and(source);
		}
	}

	public static SecurityUtils with(final InputStream source) {
		if (source == null) {
			throw new NullPointerException("Source input stream can't be null");
		}
		else {
			return new SecurityUtils().and(source);
		}
	}

	public static SecurityUtils with(final OutputStream source) {
		if (source == null) {
			throw new NullPointerException("Source output stream can't be null");
		}
		else {
			return new SecurityUtils().and(source);
		}
	}
	
	public static SecurityUtils with(final KeyStoreController ks) {
		if (ks == null) {
			throw new NullPointerException("Key store controller can't be null");
		}
		else {
			return new SecurityUtils().and(ks);
		}
	}

	public static SecurityUtils with(final Provider provider) {
		if (provider == null) {
			throw new NullPointerException("Security provider can't be null");
		}
		else {
			return new SecurityUtils().and(provider);
		}
	}
}
