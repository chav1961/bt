package chav1961.bt.security.encription;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import chav1961.bt.security.AlgorithmUtils;
import chav1961.bt.security.AlgorithmUtils.AlgorithmDescriptor;
import chav1961.bt.security.interfaces.SecurityProcessingException;
import chav1961.bt.security.internal.InternalUtils;
import chav1961.purelib.basic.Utils;

public class EncriptionUtils {
	public static SecretKey createSecretKey(final int keySize) throws SecurityProcessingException {
		if (keySize <= 0 || keySize % 8 != 0) {
			throw new IllegalArgumentException("Key size ["+keySize+"] must be positive and must be a multiple of 8");
		}
		else {
			return createSecretKey(InternalUtils.PROPS.getProperty(InternalUtils.PROP_CIPHER_TYPE_ALGORITHM), keySize);
		}
	}

	public static SecretKey createSecretKey(final String algorithm, final int keySize) throws SecurityProcessingException {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty");
		}
		else if (keySize <= 0 || keySize % 8 != 0) {
			throw new IllegalArgumentException("Key size ["+keySize+"] must be positive and must be a multiple of 8");
		}
		else {
			try{final KeyGenerator	keyGenerator = KeyGenerator.getInstance(algorithm);
			
		        keyGenerator.init(keySize);
		        return keyGenerator.generateKey();
			} catch (InvalidParameterException e) {
				throw new IllegalArgumentException(e.getLocalizedMessage(), e); 
			} catch (NoSuchAlgorithmException e) {
				throw new SecurityProcessingException(e);
			}
		}
	}
	
    public static SecretKey createSecretKeyFromPassword(final char[] password, final byte[] salt) throws SecurityProcessingException {
    	if (password == null || password.length == 0) {
    		throw new IllegalArgumentException("Password can't be null or empty array"); 
    	}
    	else if (salt == null) {
    		throw new NullPointerException("Salt can't be null"); 
    	}
    	else {
    		return createSecretKeyFromPasswordInternal(InternalUtils.PROPS.getProperty(InternalUtils.PROP_PASSWORD_KEY_ALGORITHM),
    				InternalUtils.PROPS.getProperty(InternalUtils.PROP_CIPHER_TYPE_ALGORITHM),
    				password, 
    				salt, 
    				InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_ITERATIONS, int.class), 
    				InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class));
    	}
    }
    
    public static SecretKey createSecretKeyFromPassword(final String passwordKeyAlgorithm, final String cipherAlgorithm, final char[] password, final byte[] salt, final int iterations, final int keySize) throws SecurityProcessingException {
    	if (Utils.checkEmptyOrNullString(passwordKeyAlgorithm)) {
    		throw new IllegalArgumentException("Password key algorithm name can't be null or empty"); 
    	}
    	else if (Utils.checkEmptyOrNullString(cipherAlgorithm)) {
    		throw new IllegalArgumentException("Cipher algorithm name can't be null or empty"); 
    	}
    	else if (password == null || password.length == 0) {
    		throw new IllegalArgumentException("Password can't be null or empty array"); 
    	}
    	else if (salt == null) {
    		throw new NullPointerException("Salt can't be null"); 
    	}
    	else if (iterations <= 0) {
    		throw new IllegalArgumentException("Number of iterations ["+iterations+"] must be greater than 0"); 
    	}
    	else if (keySize <= 0 || keySize % 8 != 0) {
			throw new IllegalArgumentException("Key size ["+keySize+"] must be positive and must be a multiple of 8");
    	}
    	else {
    		return createSecretKeyFromPasswordInternal(passwordKeyAlgorithm, cipherAlgorithm, password, salt, iterations, keySize);
    	}
    }
    
    public static IvParameterSpec generateIinitialVector() {
        final byte[] 	iv = new byte[16];
        
        InternalUtils.RANDOM.nextBytes(iv);
        return new IvParameterSpec(iv);
    }    

    public static byte[] encrypt(final byte[] input, final SecretKey key, final IvParameterSpec iv) throws SecurityProcessingException {
    	return encrypt(InternalUtils.PROPS.getProperty(InternalUtils.PROP_AES_ENCRYPTION_ALGORITHM), input, key, iv);
    }
    
    public static byte[] encrypt(final String algorithm, final byte[] input, final SecretKey key, final IvParameterSpec iv) throws SecurityProcessingException {
    	if (Utils.checkEmptyOrNullString(algorithm)) {
    		throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
    	}
    	else if (input == null || input.length == 0) {
    		throw new IllegalArgumentException("Array to encrypt can't be null or empty"); 
    	}
    	else if (key == null) {
    		throw new NullPointerException("Secret key can't be null"); 
    	}
    	else if (iv == null) {
    		throw new NullPointerException("Initial vector can't be null"); 
    	}
    	else {
			try{final Cipher 	cipher = Cipher.getInstance(algorithm);
			
	    	    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
	    	    return cipher.doFinal(input);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException exc) {
				throw new SecurityProcessingException(exc.getLocalizedMessage(), exc); 
			}
    	}
	}

    public static SealedObject encrypt(final Serializable input, final SecretKey key, final IvParameterSpec iv) throws SecurityProcessingException  {
    	return encrypt(InternalUtils.PROPS.getProperty(InternalUtils.PROP_AES_ENCRYPTION_ALGORITHM), input, key, iv);
    }
    
    public static SealedObject encrypt(final String algorithm, final Serializable input, final SecretKey key, final IvParameterSpec iv) throws SecurityProcessingException  {
    	if (Utils.checkEmptyOrNullString(algorithm)) {
    		throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
    	}
    	else if (input == null) {
    		throw new NullPointerException("Object to encrypt can't be null"); 
    	}
    	else if (key == null) {
    		throw new NullPointerException("Secret key can't be null"); 
    	}
    	else if (iv == null) {
    		throw new NullPointerException("Initial vector can't be null"); 
    	}
    	else {
			try{final Cipher 	cipher = Cipher.getInstance(algorithm);
    	    
	    	    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
	    	    return new SealedObject(input, cipher);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | IOException exc) {
				throw new SecurityProcessingException(exc.getLocalizedMessage(), exc); 
			}
    	}
	}

    public static byte[] decrypt(final byte[] input, final SecretKey key, final IvParameterSpec iv) throws SecurityProcessingException {
    	return decrypt(InternalUtils.PROPS.getProperty(InternalUtils.PROP_AES_ENCRYPTION_ALGORITHM), input, key, iv);
    }
    
    public static byte[] decrypt(final String algorithm, final byte[] input, final SecretKey key, final IvParameterSpec iv) throws SecurityProcessingException {
    	if (Utils.checkEmptyOrNullString(algorithm)) {
    		throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
    	}
    	else if (input == null || input.length == 0) {
    		throw new IllegalArgumentException("Array to decrypt can't be null or empty"); 
    	}
    	else if (key == null) {
    		throw new NullPointerException("Secret key can't be null"); 
    	}
    	else if (iv == null) {
    		throw new NullPointerException("Initial vector can't be null"); 
    	}
    	else {
			try {final Cipher 	cipher = Cipher.getInstance(algorithm);
			
			    cipher.init(Cipher.DECRYPT_MODE, key, iv);
			    return cipher.doFinal(input);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException exc) {
				throw new SecurityProcessingException(exc.getLocalizedMessage(), exc); 
			}
    	}
	}

    public static <T extends Serializable> T decrypt(final SealedObject input, final SecretKey key, final IvParameterSpec iv) throws SecurityProcessingException  {
    	return decrypt(InternalUtils.PROPS.getProperty(InternalUtils.PROP_AES_ENCRYPTION_ALGORITHM), input, key, iv);
    }
    
    public static <T extends Serializable> T decrypt(final String algorithm, final SealedObject input, final SecretKey key, final IvParameterSpec iv) throws SecurityProcessingException  {
    	if (Utils.checkEmptyOrNullString(algorithm)) {
    		throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
    	}
    	else if (input == null) {
    		throw new NullPointerException("Object to decrypt can't be null"); 
    	}
    	else if (key == null) {
    		throw new NullPointerException("Secret key can't be null"); 
    	}
    	else if (iv == null) {
    		throw new NullPointerException("Initial vector can't be null"); 
    	}
    	else {
		    try{final Cipher 	cipher = Cipher.getInstance(algorithm);
		    
		    	cipher.init(Cipher.DECRYPT_MODE, key, iv);
				return (T)input.getObject(cipher);
			} catch (ClassNotFoundException | IllegalBlockSizeException | BadPaddingException | IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException exc) {
				throw new SecurityProcessingException(exc.getLocalizedMessage(), exc); 
			}
    	}
	}

    public static CipherOutputStream getCypherOutputStream(final OutputStream os, final SecretKey key, final IvParameterSpec iv) throws IOException {
    	return getCypherOutputStream(os, InternalUtils.PROPS.getProperty(InternalUtils.PROP_AES_ENCRYPTION_ALGORITHM), key, iv);
    }    
    
    public static CipherOutputStream getCypherOutputStream(final OutputStream os, final String algorithm, final SecretKey key, final IvParameterSpec iv) throws IOException {
    	if (os == null) {
    		throw new NullPointerException("Output stream can't be null"); 
    	}
    	else if (Utils.checkEmptyOrNullString(algorithm)) {
    		throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
    	}
    	else if (key == null) {
    		throw new NullPointerException("Secret key can't be null"); 
    	}
    	else if (iv == null) {
    		throw new NullPointerException("Initial vector can't be null"); 
    	}
    	else {
			try{final Cipher 	cipher = Cipher.getInstance(algorithm);
			
			    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
	    		return new CipherOutputStream(os, cipher);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException exc) {
				throw new IOException(exc.getLocalizedMessage(), exc); 
			}
    	}
    }

    public static CipherInputStream getCypherInputStream(final InputStream os, final SecretKey key, final IvParameterSpec iv) throws IOException {
    	return getCypherInputStream(os, InternalUtils.PROPS.getProperty(InternalUtils.PROP_AES_ENCRYPTION_ALGORITHM), key, iv);
    }    
    
    public static CipherInputStream getCypherInputStream(final InputStream os, final String algorithm, final SecretKey key, final IvParameterSpec iv) throws IOException {
    	if (os == null) {
    		throw new NullPointerException("Output stream can't be null"); 
    	}
    	else if (Utils.checkEmptyOrNullString(algorithm)) {
    		throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
    	}
    	else if (key == null) {
    		throw new NullPointerException("Secret key can't be null"); 
    	}
    	else if (iv == null) {
    		throw new NullPointerException("Initial vector can't be null"); 
    	}
    	else {
			try{final Cipher 	cipher = Cipher.getInstance(algorithm);
			
			    cipher.init(Cipher.DECRYPT_MODE, key, iv);
	    		return new CipherInputStream(os, cipher);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException exc) {
				throw new IOException(exc.getLocalizedMessage(), exc); 
			}
    	}
    }

    private static SecretKey createSecretKeyFromPasswordInternal(final String secretKeyAlgorithm, final String cipherAlgorithm, final char[] password, final byte[] salt, final int iterations, final int keyLength) throws SecurityProcessingException {
    	for (AlgorithmDescriptor item : AlgorithmUtils.getAlgorithms()) {
    		if (item.getName().equals(cipherAlgorithm)) {
    	    	try{final SecretKeyFactory	factory = SecretKeyFactory.getInstance(secretKeyAlgorithm);
	    		    final KeySpec 			spec = new PBEKeySpec(password, salt, iterations, keyLength);
	    		    
	    		    return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), cipherAlgorithm);
	    		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
	    			throw new SecurityProcessingException(e);
	    		}
    		}
    	}
    	throw new IllegalArgumentException("Cipher algorithm ["+cipherAlgorithm+"] is not supported by any providers"); 
    }    
}
