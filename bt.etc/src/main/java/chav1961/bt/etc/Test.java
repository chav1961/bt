package chav1961.bt.etc;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;  
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;  
import java.util.Base64;  
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec; 

import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.spec.SecretKeySpec;

// https://www.baeldung.com/java-aes-encryption-decryption

public class Test {
	public static String	val = new StringBuilder().append("val1 "+"val2").toString();

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		testMD5();
		testSHA256();
		testSHA512();
		testPBE();
		
		testEncryption();
		testDESEncodeDecode();
		testAESEncodeDecode();
	}

	private static void testMD5() {
		String	password = "myPassword";
		String encryptedpassword = null;
		
		try {
			/* MessageDigest instance for MD5. */
			MessageDigest m = MessageDigest.getInstance("MD5");
			
			/* Add plain-text password bytes to digest using MD5 update() method. */
			m.update(password.getBytes());
			
			/* Convert the hash value into bytes */
			byte[] bytes = m.digest();
		
			/* The bytes array has bytes in decimal form. Converting it into hexadecimal format. */
			StringBuilder s = new StringBuilder();
			for(int i=0; i < bytes.length; i++) {
				s.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}

			/* Complete hashed password in hexadecimal format */
			encryptedpassword = s.toString();
		} catch (NoSuchAlgorithmException exc) {
			exc.printStackTrace();
		}
		
		/* Display the unencrypted and encrypted passwords. */
		System.err.println("Plain-text password: " + password);
		System.err.println("Encrypted password using MD5: " + encryptedpassword);
	}
	
	private static void testSHA256() {
		try {  
            String string1 = "myPassword";  
            System.err.println("\n" + string1 + " : " + toHexString(getSHA256(string1)));  
  
            String string2 = "hashtrial";  
            System.err.println("\n" + string2 + " : " + toHexString(getSHA256(string2)));  
        } catch (NoSuchAlgorithmException e) {  
            System.err.println("Exception thrown for incorrect algorithm: " + e);  
        }  	
	}

	public static byte[] getSHA256(String input) throws NoSuchAlgorithmException {  
        /* MessageDigest instance for hashing using SHA256 */  
        MessageDigest md = MessageDigest.getInstance("SHA-256");  
  
        /* digest() method called to calculate message digest of an input and return array of byte */  
        return md.digest(input.getBytes(StandardCharsets.UTF_8));  
    }  

	private static void testSHA512() {  
        try {  
            String string1 = "myPassword";  
            System.err.println("\n" + string1 + " : " + toHexString(getSHA512(string1)));  
  
            String string2 = "hashtrial";  
            System.err.println("\n" + string2 + " : " + toHexString(getSHA512(string2)));  
        } catch (NoSuchAlgorithmException e) {  
            System.err.println("Exception thrown for incorrect algorithm: " + e);  
        }  
    }  	
	
	private static byte[] getSHA512(String input) throws NoSuchAlgorithmException {  
        /* MessageDigest instance for hashing using SHA512*/  
        MessageDigest md = MessageDigest.getInstance("SHA-512");  
  
        /* digest() method called to calculate message digest of an input and return array of byte */  
        return md.digest(input.getBytes(StandardCharsets.UTF_8));  
    }  

	private static void testPBE() {
		 String password = "myNewPass123";  
		          
        /* generates the Salt value. It can be stored in a database. */  
        String saltvalue = PassBasedEnc.getSaltvalue(30);  
          
        /* generates an encrypted password. It can be stored in a database.*/  
        String encryptedpassword = PassBasedEnc.generateSecurePassword(password, saltvalue);  
          
        /* Print out plain text password, encrypted password and salt value. */  
        System.err.println("Plain text password = " + password);  
        System.err.println("Secure password = " + encryptedpassword);  
        System.err.println("Salt value = " + saltvalue);  
          
        /* verify the original password and encrypted password */  
        boolean status = PassBasedEnc.verifyUserPassword(password,encryptedpassword,saltvalue);
        
        if (status == true) {
            System.err.println("Password Matched!!");  
        }
        else {
            System.err.println("Password Mismatched");  
        }
	}
	

	private static void testEncryption() throws UnsupportedEncodingException, GeneralSecurityException {
		String	password = "MyPassword";
		
		byte[] salt = new String("622836429").getBytes();
		int iterationCount = 10000;
		int keyLength = 128;
		 
		TestSecretKey object = new TestSecretKey();
		SecretKeySpec key = object.generateSecretKey(password.toCharArray(), salt, iterationCount, keyLength);
		  
		String originalPassword = password;
		System.err.println("Original password: " + originalPassword);
		String encryptedPassword = object.encrypt(originalPassword, key);
		 
		System.err.println("Encrypted password: " + encryptedPassword);
	}

	
	private static void testDESEncodeDecode() throws Exception {
       TrippleDes td= new TrippleDes();

        String target="imparator";
        String encrypted=td.encrypt(target);
        String decrypted=td.decrypt(encrypted);

        System.err.println("--- DES ---: ");
        System.err.println("String To Encrypt: "+ target);
        System.err.println("Encrypted String:" + encrypted);
        System.err.println("Decrypted String:" + decrypted);

	}

	private static void testAESEncodeDecode() throws Exception {
       TrippleAES td = new TrippleAES();
       SecretKey key = td.generateKey(128);
       IvParameterSpec ivParameterSpec = td.generateIv();
       String algorithm = "AES/CBC/PKCS5Padding";
       String input = "testString";
       
       String cipherText = td.encrypt(algorithm, input, key, ivParameterSpec);
       String plainText = td.decrypt(algorithm, cipherText, key, ivParameterSpec);
       
       System.err.println("--- AES ---: ");
       System.err.println("String To Encrypt: "+ input);
       System.err.println("Encrypted String:" + cipherText);
       System.err.println("Decrypted String:" + plainText);
	}
	
	private static String toHexString(byte[] hash) {  
        /* Convert byte array of hash into digest */  
        BigInteger number = new BigInteger(1, hash);  
  
        /* Convert the digest into hex value */  
        StringBuilder hexString = new StringBuilder(number.toString(16));  
  
        /* Pad with leading zeros */  
        while (hexString.length() < 32) {  
            hexString.insert(0, '0');  
        }  
  
        return hexString.toString();  
    }  
}


class PassBasedEnc {  
    /* Declaration of variables */   
    private static final Random random = new SecureRandom();  
    private static final String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";  
    private static final int iterations = 10000;  
    private static final int keylength = 256;  
      
    /* Method to generate the salt value. */  
    public static String getSaltvalue(int length) {  
        StringBuilder finalval = new StringBuilder(length);  
  
        for (int i = 0; i < length; i++) {  
            finalval.append(characters.charAt(random.nextInt(characters.length())));  
        }  
  
        return new String(finalval);  
    }     
  
    /* Method to generate the hash value */  
    public static byte[] hash(char[] password, byte[] salt) {  
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keylength);  
        Arrays.fill(password, Character.MIN_VALUE);  
        
        try {  
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");  
            return skf.generateSecret(spec).getEncoded();  
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {  
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);  
        } finally {  
            spec.clearPassword();  
        }  
    }  
  
    /* Method to encrypt the password using the original password and salt value. */  
    public static String generateSecurePassword(String password, String salt) {  
        String finalval = null;  
  
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());  
        finalval = Base64.getEncoder().encodeToString(securePassword);  
        return finalval;  
    }  
      
    /* Method to verify if both password matches or not */  
    public static boolean verifyUserPassword(String providedPassword, String securedPassword, String salt) {  
        boolean finalval = false;  
          
        /* Generate New secure password with the same salt */  
        String newSecurePassword = generateSecurePassword(providedPassword, salt);  
        /* Check if two passwords are equal */  
        finalval = newSecurePassword.equalsIgnoreCase(securedPassword);  
        return finalval;  
    }  
}  


class TestSecretKey {
    // Method  
    public SecretKeySpec generateSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException { 
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        SecretKey tempKey = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(tempKey.getEncoded(), "AES");
    }
  
    private String base64Encoder(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
  
    public String encrypt(String dataToEncrypt, SecretKeySpec key) throws GeneralSecurityException, UnsupportedEncodingException {
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        
        pbeCipher.init(Cipher.ENCRYPT_MODE, key);
          
        AlgorithmParameters parameters = pbeCipher.getParameters();
          
        IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class);
          
        byte[] cryptoText = pbeCipher.doFinal(dataToEncrypt.getBytes("UTF-8"));
          
        byte[] iv = ivParameterSpec.getIV();
        
        return base64Encoder(iv) + ":" + base64Encoder(cryptoText);
    }
}


class TrippleDes {
    private static final String UNICODE_FORMAT = "UTF8";
    public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
    private KeySpec ks;
    private SecretKeyFactory skf;
    private Cipher cipher;
    byte[] arrayBytes;
    private String myEncryptionKey;
    private String myEncryptionScheme;
    SecretKey key;

    public TrippleDes() throws Exception {
        myEncryptionKey = "ThisIsSpartaThisIsSparta";
        myEncryptionScheme = DESEDE_ENCRYPTION_SCHEME;
        arrayBytes = myEncryptionKey.getBytes(UNICODE_FORMAT);
        ks = new DESedeKeySpec(arrayBytes);
        skf = SecretKeyFactory.getInstance(myEncryptionScheme);
        cipher = Cipher.getInstance(myEncryptionScheme);
        key = skf.generateSecret(ks);
    }


    public String encrypt(String unencryptedString) {
        String encryptedString = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] plainText = unencryptedString.getBytes(UNICODE_FORMAT);
            byte[] encryptedText = cipher.doFinal(plainText);
            encryptedString = new String(Base64.getEncoder().encode(encryptedText));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedString;
    }


    public String decrypt(String encryptedString) {
        String decryptedText=null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedText = Base64.getDecoder().decode(encryptedString);
            byte[] plainText = cipher.doFinal(encryptedText);
            decryptedText= new String(plainText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedText;
    }
}


class TrippleAES {
    public TrippleAES() throws Exception {
    }
	
    public SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }    
	
    public SecretKey getKeyFromPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
	    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
	    SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
	    return secret;
	}
    
    public IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }    
    
    public String encrypt(String algorithm, String input, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
	    Cipher cipher = Cipher.getInstance(algorithm);
	    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
	    byte[] cipherText = cipher.doFinal(input.getBytes());
	    return Base64.getEncoder().encodeToString(cipherText);
	}
    
    public String decrypt(String algorithm, String cipherText, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
	    Cipher cipher = Cipher.getInstance(algorithm);
	    cipher.init(Cipher.DECRYPT_MODE, key, iv);
	    byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
	    return new String(plainText);
	}
}