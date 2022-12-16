package chav1961.bt.security.encription;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import chav1961.bt.security.keystore.interfaces.PasswordFormatException;
import chav1961.purelib.basic.HexUtils;
import chav1961.purelib.basic.Utils;


public class AuthUtils {
    private final static SecureRandom	SECURE_RANDOM = new SecureRandom();
    private static final int 			PWD_HASH_MAX_LENGTH = 16;
    private static final byte 			KEY_LEN = 8;
    private static final byte 			KEY_LEN_3 = 3 * KEY_LEN;
    
    public static enum PwdWeakness {
        NONE,
        TOO_SHORT,
        NO_NUMERIC_CHARS,
        NO_ALPHABETIC_CHARS,
        ALL_ALPHABETIC_CHARS_IN_SAME_CASE,
        NO_SPECIAL_CHARS,
        SAME_AS_USER_NAME,
        STARTS_WITH_FORBIDDEN_CHARS,
        ENDS_WITH_FORBIDDEN_CHARS,
        CONTAIN_FORBIDDEN_CHARS,
        FORBIDDEN
    }
    
    public static class PwdWeaknessCheckResult {
        public static final PwdWeaknessCheckResult VALID = new PwdWeaknessCheckResult(PwdWeakness.NONE);
        
        private final PwdWeakness weakness;
        private final String details;
        
        private PwdWeaknessCheckResult(final PwdWeakness weakness){
            this.weakness = weakness;
            details = null;
        }
        
        private PwdWeaknessCheckResult(final PwdWeakness weakness, final String details){
            this.weakness = weakness;
            this.details = details;
        }

        public PwdWeakness getWeakness() {
            return weakness;
        }

        public String getDetails() {
            return details;
        }                
    }

    public static final Collection<PwdWeaknessCheckResult> checkPwdWeakness(final String password, final PasswordRequirements requirements) {
    	if (Utils.checkEmptyOrNullString(password)) {
    		throw new IllegalArgumentException("Password to check can't be null or empty string");
    	}
    	else if (requirements == null) {
    		throw new NullPointerException("Check requirements can't be null");
    	}
    	else {
            final List<PwdWeaknessCheckResult>	result = new ArrayList<>();
            
            checkPwdWeakness(password, requirements, result);
            return result;
    	}
    }                

    static final boolean checkPwdWeakness(final String password, final PasswordRequirements requirements, final Collection<PwdWeaknessCheckResult> result) {
        final String 	userName = requirements.getUserName();
        boolean			success = true;
        
        if (!Utils.checkEmptyOrNullString(userName) && userName.equalsIgnoreCase(password)){
        	if (result != null) {
                result.add(new PwdWeaknessCheckResult(PwdWeakness.SAME_AS_USER_NAME, userName ));
        	}
            success = false;
        }        

        if (requirements.getBlackList() != null) {
            for (String blackPwd : requirements.getBlackList()) {
                if (password.equals(blackPwd)){
                	if (result != null) {
                		result.add(new PwdWeaknessCheckResult(PwdWeakness.FORBIDDEN));
                	}
                    success = false;
                }
                else{
                	final String regex = ("\\Q" + blackPwd + "\\E").replace("*", "\\E.*\\Q");
                    
                    if (password.matches(regex)) {
                        final String 		withoutAsterisks = blackPwd.replace("*", "");
                        final PwdWeakness 	weakness;
                        
                        if (blackPwd.startsWith(withoutAsterisks)){
                            weakness = PwdWeakness.STARTS_WITH_FORBIDDEN_CHARS;
                        }
                        else if (blackPwd.endsWith(withoutAsterisks)){
                            weakness = PwdWeakness.ENDS_WITH_FORBIDDEN_CHARS;                    
                        }
                        else{
                            weakness = PwdWeakness.CONTAIN_FORBIDDEN_CHARS;                    
                        }
                    	if (result != null) {
                    		result.add(new PwdWeaknessCheckResult(weakness, blackPwd.replace("*", " ").trim()));
                    	}
                        success = false;
                        break;
                    }
                }
            }
        }
        final long minLength = requirements.getMinLength();
        
        if (minLength > 0 && password.length() < minLength) {
        	if (result != null) {
        		result.add(new PwdWeaknessCheckResult(PwdWeakness.TOO_SHORT, String.valueOf(minLength)));
        	}
            success = false;
        }
        
        if (requirements.mustContainLetter() || requirements.mustContainDigit() || requirements.mustContainSpecialChar()) {
            boolean 	hasAChars = false, hasNChars = false, hasSChars = false, hasACharsInLowerCase = false, hasACharsInUpperCase = false;
            
            for (char c : password.toCharArray()) {
                if (Character.isLetter(c)) {
                    hasAChars = true;
                    hasACharsInLowerCase |= Character.isLowerCase(c);
                    hasACharsInUpperCase |= Character.isUpperCase(c);
                }
                else if (Character.isDigit(c)) {
                    hasNChars = true;
                } 
                else{
                    hasSChars = true;
                }
            }
            
            //PCI-DSS: 8.5.11 Use passwords containing both numeric and alphabetic characters.
            if (requirements.mustContainLetter()) {
                if (!hasAChars){
                	if (result != null) {
                		result.add(new PwdWeaknessCheckResult(PwdWeakness.NO_ALPHABETIC_CHARS));
                	}
                    success = false;
                }
                if (requirements.mustContainLetterInMixedCase() && (!hasACharsInUpperCase || !hasACharsInLowerCase)){
                	if (result != null) {
                		result.add(new PwdWeaknessCheckResult(PwdWeakness.ALL_ALPHABETIC_CHARS_IN_SAME_CASE));
                	}
                    success = false;
                }
            }
            if (requirements.mustContainDigit() && !hasNChars) {
            	if (result != null) {
            		result.add(new PwdWeaknessCheckResult(PwdWeakness.NO_NUMERIC_CHARS));
            	}
                success = false;
            }
            if (requirements.mustContainSpecialChar() && !hasSChars){
            	if (result != null) {
            		result.add(new PwdWeaknessCheckResult(PwdWeakness.NO_SPECIAL_CHARS));
            	}
                success = false;
            }
        }
        return success;
    }
    
    public static final byte[] calcPwdToken(final byte[] challenge, final byte[] pwdHash) throws PasswordFormatException {
        if (challenge == null) {
            throw new NullPointerException("Challenge can't be null");
        }
        else if (challenge.length != 8) {
            throw new IllegalArgumentException("Invalid challenge length: " + challenge.length);
        }
        else {
            return encryptAesByPwdHash(challenge, pwdHash);
        }
    }

    public static boolean checkPwdToken(final byte[] pwdToken, final byte[] pwdHash, final byte[] expectedChallenge) throws PasswordFormatException{
        if (pwdToken == null || pwdToken.length==0) {
            return false;
        }
        try {
            return Arrays.equals(decryptAesByPwdHash(pwdToken, pwdHash), expectedChallenge);
        }catch (RuntimeException ex){
            return false;
        }
    }

    public static byte[] encryptNewPwdHash(final byte[] newPwdHash, final byte[] oldPwdHash) throws PasswordFormatException {
        if (newPwdHash == null) {
            throw new IllegalArgumentException("newPwdHash is null");
        }
        return encryptAesByPwdHash(newPwdHash,oldPwdHash);
    }

    public static byte[] decryptNewPwdHash(final byte[] newPwdHashCryptogram, final byte[] oldPwdHash) throws PasswordFormatException {
        if (newPwdHashCryptogram == null) {
            throw new IllegalArgumentException("newPwdHashCryptogram is null");
        }
        return decryptAesByPwdHash(newPwdHashCryptogram,oldPwdHash);
    }

    private static byte[] encryptAesByPwdHash(final byte[] data, final byte[] pwdHash) throws PasswordFormatException{
        if (pwdHash.length==16){
            return encrypt_aes(data,pwdHash);
        }
        if (pwdHash.length!=32){
            throw new IllegalArgumentException("Invalid pwdHash length: " + pwdHash.length);
        }
        final byte[] key = new byte[16];
        System.arraycopy(pwdHash,0,key,0,16);
        final byte[] encryptedData = encrypt_aes(data,key);
        System.arraycopy(pwdHash,16,key,0,16);
        return encrypt_aes(encryptedData,key);
    }

    private static byte[] decryptAesByPwdHash(final byte[] data, final byte[] pwdHash) throws PasswordFormatException{
        if (pwdHash.length==16){
            return decrypt_aes(data,pwdHash);
        }
        if (pwdHash.length!=32){
            throw new IllegalArgumentException("Invalid pwdHash length: " + pwdHash.length);
        }
        final byte[] key = new byte[16];
        System.arraycopy(pwdHash,16,key,0,16);
        final byte[] encryptedData = decrypt_aes(data,key);
        System.arraycopy(pwdHash,0,key,0,16);
        return decrypt_aes(encryptedData,key);
    }

    public static byte[] generateAesKey(final byte[] keyMaterial, final byte[] salt, final int keyLengthInBits) throws PasswordFormatException{
        if (keyLengthInBits!=128 && keyLengthInBits!=192 && keyLengthInBits!=256){
            throw new IllegalArgumentException("Invalid AES key length: " + keyLengthInBits);
        }
        final KeySpec spec = new PBEKeySpec(HexUtils.encode(keyMaterial), salt, 65536, keyLengthInBits);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        }catch (NoSuchAlgorithmException | InvalidKeySpecException exception){
            throw new PasswordFormatException("Unable to generate encryption key", exception);
        }
    }

    public static final byte[] encrypt_aes(final byte[] data, final byte[] keyData) throws PasswordFormatException {
        if (keyData.length<32) {
            return encrypt_decrypt_aes(data, keyData, Cipher.ENCRYPT_MODE);
        }
        if (keyData.length>32){
            throw new IllegalArgumentException("Invalid encryption key length: " + keyData.length);
        }
        final byte[] key128 = new byte[16];
        byte encryptedData[] = null;
        try {
            System.arraycopy(keyData, 0, key128, 0, 16);
            encryptedData = encrypt_decrypt_aes(data, key128, Cipher.ENCRYPT_MODE);
            System.arraycopy(keyData, 16, key128, 0, 16);
            return encrypt_decrypt_aes(encryptedData, key128, Cipher.ENCRYPT_MODE);
        }finally {
            Arrays.fill(key128,(byte)0);
            if (encryptedData!=null) {
                Arrays.fill(encryptedData, (byte) 0);
            }
        }
    }

    public static final byte[] decrypt_aes(final byte[] data, final byte[] keyData) throws PasswordFormatException {
        if (keyData.length<32){
            return encrypt_decrypt_aes(data,keyData,Cipher.DECRYPT_MODE);
        }
        if (keyData.length>32){
            throw new IllegalArgumentException("Invalid encryption key length: " + keyData.length);
        }
        final byte[] key128 = new byte[16];
        byte decryptedData[] = null;
        try {
            System.arraycopy(keyData, 16, key128, 0, 16);
            decryptedData = encrypt_decrypt_aes(data, key128, Cipher.DECRYPT_MODE);
            System.arraycopy(keyData, 0, key128, 0, 16);
            return encrypt_decrypt_aes(decryptedData, key128, Cipher.DECRYPT_MODE);
        }finally {
            Arrays.fill(key128,(byte)0);
            if (decryptedData!=null) {
                Arrays.fill(decryptedData, (byte) 0);
            }
        }
    }

    private static final byte[] encrypt_decrypt_aes(final byte[] data, final byte[] key, final int cipherMode) throws PasswordFormatException {
        if (key.length!= 16 && key.length!=24 && key.length!=32){
            throw new IllegalArgumentException("Invalid encryption key length: " + key.length);
        }

        final int ivSize = 16;
        final byte[] iv = new byte[ivSize];
        if (cipherMode==Cipher.ENCRYPT_MODE) {
            SECURE_RANDOM.nextBytes(iv);
        }else{
            System.arraycopy(data, 0, iv, 0, ivSize);
        }
        final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        final byte[] processData;
        if (cipherMode==Cipher.ENCRYPT_MODE) {
            processData = data;
        }else{
            final int encryptedDataLength = data.length-ivSize;
            processData = new byte[encryptedDataLength];
            System.arraycopy(data, ivSize, processData, 0, encryptedDataLength);
        }


        final byte[] result;
        try {
            final SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(cipherMode, secretKey, ivParameterSpec);
            result = cipher.doFinal(processData);
        } catch (InvalidAlgorithmParameterException e) {
            throw new PasswordFormatException(e.getMessage(), e);
        } catch (IllegalBlockSizeException e) {
            throw new PasswordFormatException(e.getMessage(), e);
        } catch (BadPaddingException e) {
            throw new PasswordFormatException(e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            throw new PasswordFormatException(e.getMessage(), e);
        } catch (InvalidKeyException e) {
            throw new PasswordFormatException(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new PasswordFormatException(e.getMessage(), e);
        }

        if (cipherMode==Cipher.DECRYPT_MODE){
            return result;
        }

        byte[] encryptedAndIV = new byte[ivSize + result.length];
        System.arraycopy(iv, 0, encryptedAndIV, 0, ivSize);
        System.arraycopy(result, 0, encryptedAndIV, ivSize, result.length);

        return encryptedAndIV;
    }

}

