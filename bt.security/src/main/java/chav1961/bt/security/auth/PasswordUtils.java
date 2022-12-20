package chav1961.bt.security.auth;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import chav1961.bt.security.interfaces.PasswordWeakness;
import chav1961.bt.security.interfaces.SecurityProcessingException;
import chav1961.bt.security.internal.InternalUtils;
import chav1961.purelib.basic.Utils;


public class PasswordUtils {
    private static final byte[] 	CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".getBytes();  
    
    public static final Collection<PasswordWeaknessCheckResult> checkPasswordWeakness(final String password, final PasswordRequirements requirements) {
    	if (Utils.checkEmptyOrNullString(password)) {
    		throw new IllegalArgumentException("Password to check can't be null or empty string");
    	}
    	else if (requirements == null) {
    		throw new NullPointerException("Check requirements can't be null");
    	}
    	else {
            final List<PasswordWeaknessCheckResult>	result = new ArrayList<>();
            
            checkPasswordWeakness(password, requirements, result);
            return result;
    	}
    }                
    
    public static byte[] generateSaltValue(final int length) {
    	if (length <= 0) {
    		throw new IllegalArgumentException("Length ["+length+"] must be positive"); 
    	}
    	else {
            final byte[]	result = new byte[length];  
            
            for (int index = 0; index < result.length; index++) {  
                result[index] = CHARACTERS[InternalUtils.RANDOM.nextInt(CHARACTERS.length)];  
            }  
            return result;  
    	}
    }     

    public static byte[] createPasswordHash(final char[] password, final byte[] salt) throws SecurityProcessingException {
    	if (password == null || password.length == 0) {
    		throw new IllegalArgumentException("Password to create hash for can't be null or empty array"); 
    	}
    	else if (salt == null) {
    		throw new NullPointerException("Salt to create hash for can't be null"); 
    	}
    	else {
        	return createPasswordHash(password, salt, InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_ITERATIONS, int.class), InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class));
    	}
    }
    
    public static byte[] createPasswordHash(final char[] password, final byte[] salt, final int iterations, final int keyLength) throws SecurityProcessingException {
    	if (password == null || password.length == 0) {
    		throw new IllegalArgumentException("Password to create hash for can't be null or empty array"); 
    	}
    	else if (salt == null) {
    		throw new NullPointerException("Salt to create hash for can't be null"); 
    	}
    	else if (iterations <= 0) {
    		throw new IllegalArgumentException("Number of iterations ["+iterations+"] must be greater than 0"); 
    	}
    	else if (keyLength <= 0 || keyLength % 8 != 0) {
    		throw new IllegalArgumentException("Key length ["+keyLength+"] must be greater than 0 and be a multiple of 8"); 
    	}
    	else {
            final PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);  

            try{final SecretKeyFactory 	skf = SecretKeyFactory.getInstance(InternalUtils.PROPS.getProperty(InternalUtils.PROP_PASSWORD_HASH_ALGORITHM));
            
                return skf.generateSecret(spec).getEncoded();  
            } catch (NoSuchAlgorithmException | InvalidKeySpecException exc) {  
                throw new SecurityProcessingException(exc.getLocalizedMessage(), exc);  
            } finally {  
                spec.clearPassword();  
            }  
    	}
    }  

    static final boolean checkPasswordWeakness(final String password, final PasswordRequirements requirements, final Collection<PasswordWeaknessCheckResult> result) {
        final String 	userName = requirements.getUserName();
        boolean			success = true;
        
        if (!Utils.checkEmptyOrNullString(userName) && userName.equalsIgnoreCase(password)){
        	if (result != null) {
                result.add(new PasswordWeaknessCheckResult(PasswordWeakness.SAME_AS_USER_NAME, userName ));
        	}
            success = false;
        }        

        if (requirements.getBlackList() != null) {
            for (String blackPwd : requirements.getBlackList()) {
                if (password.equals(blackPwd)){
                	if (result != null) {
                		result.add(new PasswordWeaknessCheckResult(PasswordWeakness.IN_BLACKLIST));
                	}
                    success = false;
                }
                else{
                	final String regex = ("\\Q" + blackPwd + "\\E").replace("*", "\\E.*\\Q");
                    
                    if (password.matches(regex)) {
                        final String 		withoutAsterisks = blackPwd.replace("*", "");
                        final PasswordWeakness 	weakness;
                        
                        if (blackPwd.startsWith(withoutAsterisks)){
                            weakness = PasswordWeakness.STARTS_WITH_FORBIDDEN_CHARS;
                        }
                        else if (blackPwd.endsWith(withoutAsterisks)){
                            weakness = PasswordWeakness.ENDS_WITH_FORBIDDEN_CHARS;                    
                        }
                        else{
                            weakness = PasswordWeakness.CONTAIN_FORBIDDEN_CHARS;                    
                        }
                    	if (result != null) {
                    		result.add(new PasswordWeaknessCheckResult(weakness, blackPwd.replace("*", " ").trim()));
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
        		result.add(new PasswordWeaknessCheckResult(PasswordWeakness.TOO_SHORT, String.valueOf(minLength)));
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
                		result.add(new PasswordWeaknessCheckResult(PasswordWeakness.ALPHABETIC_CHARS_MISSING));
                	}
                    success = false;
                }
                if (requirements.mustContainLetterInMixedCase() && (!hasACharsInUpperCase || !hasACharsInLowerCase)){
                	if (result != null) {
                		result.add(new PasswordWeaknessCheckResult(PasswordWeakness.ALL_ALPHABETIC_CHARS_IN_SAME_CASE));
                	}
                    success = false;
                }
            }
            if (requirements.mustContainDigit() && !hasNChars) {
            	if (result != null) {
            		result.add(new PasswordWeaknessCheckResult(PasswordWeakness.NUMERIC_CHARS_MISSING));
            	}
                success = false;
            }
            if (requirements.mustContainSpecialChar() && !hasSChars){
            	if (result != null) {
            		result.add(new PasswordWeaknessCheckResult(PasswordWeakness.SPECIAL_CHARS_MISSING));
            	}
                success = false;
            }
        }
        return success;
    }
    
    public static class PasswordWeaknessCheckResult {
        public static final PasswordWeaknessCheckResult VALID = new PasswordWeaknessCheckResult(PasswordWeakness.ALL_OK);
        
        private final PasswordWeakness	problem;
        private final String 			details;
        
        private PasswordWeaknessCheckResult(final PasswordWeakness problem) {
            this.problem = problem;
            details = "";
        }
        
        private PasswordWeaknessCheckResult(final PasswordWeakness problem, final String details) {
            this.problem = problem;
            this.details = details;
        }

        public PasswordWeakness getProblem() {
            return problem;
        }

        public String getDetails() {
            return details;
        }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((details == null) ? 0 : details.hashCode());
			result = prime * result + ((problem == null) ? 0 : problem.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			PasswordWeaknessCheckResult other = (PasswordWeaknessCheckResult) obj;
			if (details == null) {
				if (other.details != null) return false;
			} else if (!details.equals(other.details)) return false;
			if (problem != other.problem) return false;
			return true;
		}

		@Override
		public String toString() {
			return "PasswordWeaknessCheckResult [problem=" + problem + ", details=" + details + "]";
		}
    }
}

