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
    private static final int		DEFAULT_MIN_PASSWORD_LENGTH = 10;
    
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

    public static char[] generateRandomPassword(final PasswordRequirements requirements, final int maxIterations) {
    	if (requirements == null) {
    		throw new NullPointerException("Password requirements can't be null"); 
    	}
    	else if (maxIterations < 1) {
    		throw new IllegalArgumentException("Number of iterations must be greater than zero"); 
    	}
    	else {
    		final int		length = (int) (requirements.getMinLength() <= 0 ? DEFAULT_MIN_PASSWORD_LENGTH : requirements.getMinLength());
    		final char[]	result = new char[length];
    		final int 		piece = (requirements.mustContainDigit() ? 1 : 0) 
    								+ (requirements.mustContainLetter() ? (requirements.mustContainLetterInMixedCase() ? 2 : 1) : 0)
    								+ (requirements.mustContainSpecialChar() ? 1 : 0);
    		
    		if (piece == 0) {
    			throw new IllegalArgumentException("Password requirements are too weak to generate password (no any serious requirements). Use untimate secret password '123' if you wish :-)"); 
    		}
    		else {
    			final int	blockSize = length / piece;
    			
    			if (blockSize == 0) {
        			throw new IllegalArgumentException("Password requirements are too weak to generate password (too short password length for your requirements). Use untimate secret password '123' if you wish :-)"); 
    			}
    			else {
	    			for(int iterations = 0; iterations < maxIterations; iterations++) {
		    			int	to = 0;
		    			
		    			if (requirements.mustContainDigit()) {
			    			for (int index = 0; index < piece; index++, to++) {
			            		result[to] = (char)InternalUtils.RANDOM.nextInt('0', '9');
			    			}
		    			}
		    			if (requirements.mustContainLetter()) {
		    				if (requirements.mustContainLetterInMixedCase()) {
		    	    			for (int index = 0; index < blockSize; index++, to++) {
		    	            		result[to] = (char)InternalUtils.RANDOM.nextInt('a', 'z');
		    	    			}
		    	    			for (int index = 0; index < blockSize; index++, to++) {
		    	            		result[to] = (char)InternalUtils.RANDOM.nextInt('A', 'Z');
		    	    			}
		    				}
		    				else {
		    	    			for (int index = 0; index < blockSize; index++, to++) {
		    	            		result[to] = (char)InternalUtils.RANDOM.nextInt('a', 'Z');
		    	    			}
		    				}
		    			}
		    			if (requirements.mustContainSpecialChar()) {
			    			for (int index = 0; index < blockSize; index++, to++) {
			            		result[to] = (char)InternalUtils.RANDOM.nextInt('!', '/');
			    			}
		    			}
		    			
		        		for(; to < length; to++) {	// fill password to the end
		        			result[to] = (char)InternalUtils.RANDOM.nextInt('a', 'z');
		        		}
		    			
		                // Shuffle array (see Collections.shuffle(...) 
		                for (int index = length; index > 1; index--) {
		                    swap(result, index - 1, InternalUtils.RANDOM.nextInt(index));
		                }
		                if (checkPasswordWeakness(new String(result), requirements, null)) {
		                	return result;
		                }
	    			}
	    			throw new IllegalArgumentException("Password generated is too weak after ["+maxIterations+"] iterations. Use untimate secret password '123' if you wish :-)"); 
    			}
    		}
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
        	return createPasswordHash(password, salt
        			, InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_ITERATIONS, int.class)
        			, InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class)
        			, InternalUtils.PROPS.getProperty(InternalUtils.PROP_PASSWORD_HASH_ALGORITHM));
    	}
    }
    
    public static byte[] createPasswordHash(final char[] password, final byte[] salt, final int iterations, final int keyLength, final String hashAlgorithm) throws SecurityProcessingException {
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
    	else if (Utils.checkEmptyOrNullString(hashAlgorithm)) {
    		throw new IllegalArgumentException("Password hash algorithm can't be null or empty"); 
    	}
    	else {
            final PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);  

            try{final SecretKeyFactory 	skf = SecretKeyFactory.getInstance(hashAlgorithm);
            
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
            
            if (requirements.mustContainLetter()) {
                if (!hasAChars){
                	if (result != null) {
                		result.add(new PasswordWeaknessCheckResult(PasswordWeakness.ALPHABETIC_CHARS_MISSING));
                	}
                    success = false;
                }
                if (hasAChars && requirements.mustContainLetterInMixedCase() && (!hasACharsInUpperCase || !hasACharsInLowerCase)){
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
        
        if (requirements.getBlackList() != null) {
            for (String blackPwd : requirements.getBlackList()) {
                if (password.equals(blackPwd)){
                	if (result != null) {
                		result.add(new PasswordWeaknessCheckResult(PasswordWeakness.IN_BLACKLIST));
                	}
                    success = false;
                }
                else {
                	final String regex = ".*"+("\\Q" + blackPwd + "\\E").replace("*", "\\E.*\\Q")+".*";
                    
                    if (password.matches(regex)) {
                    	if (result != null) {
                    		result.add(new PasswordWeaknessCheckResult(PasswordWeakness.CONTAIN_FORBIDDEN_SEQUENCES, blackPwd));
                    	}
                        success = false;
                        break;
                    }
                }
            }
        }

        if (success) {
        	if (result != null) {
        		result.add(new PasswordWeaknessCheckResult(PasswordWeakness.ALL_OK));
        	}
        }
        return success;
    }

    private static void swap(final char[] arr, final int i, final int j) {
        char tmp = arr[i];
        
        arr[i] = arr[j];
        arr[j] = tmp;
    }
    
    public static class PasswordWeaknessCheckResult {
        public static final PasswordWeaknessCheckResult VALID = new PasswordWeaknessCheckResult(PasswordWeakness.ALL_OK);
        
        private final PasswordWeakness	problem;
        private final String 			details;
        
        PasswordWeaknessCheckResult(final PasswordWeakness problem) {
            this.problem = problem;
            details = "";
        }
        
        PasswordWeaknessCheckResult(final PasswordWeakness problem, final String details) {
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
