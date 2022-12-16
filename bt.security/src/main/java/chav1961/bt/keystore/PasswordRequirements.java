package chav1961.bt.keystore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import chav1961.purelib.basic.Utils;

public class PasswordRequirements {
    //PCI-DSS: 8.5.10 Require a minimum password length of at least seven characters.
    private final static PasswordRequirements 	DEFAULT = new PasswordRequirements(7, true, true);
    
    private final long 		minLength;
    private final boolean 	mustContainLetter;
    private final boolean 	mustContainLetterInMixedCase;
    private final boolean 	mustContainDigit;
    private final boolean 	mustContainSpecial;
    private final String 	userName;
    private final PasswordHash.Algorithm currentPwdHashAlgorithm;
    private final Collection<String> blackList;

    public PasswordRequirements(final long minLen, final boolean mustContainAChar, final boolean mustContainNChar) {
        this(minLen, mustContainAChar, false, mustContainNChar, false, null, null, null);
    }
    
    public PasswordRequirements(final long minLen, final boolean mustContainAChar, final boolean mustContainNChar, final Collection<String> blackList) {
        this(minLen, mustContainAChar, false, mustContainNChar, false, null, blackList, null);
    }
    
    public PasswordRequirements(final long minLen, final boolean mustContainAChar, final boolean mustContainNChar, final String userName, final Collection<String> blackList) {
        this(minLen, mustContainAChar, false, mustContainNChar, false, userName, blackList, null);
    }    

    public PasswordRequirements(final long minLen, final boolean mustContainAChar, final boolean mustContainACharInMixedCase, final boolean mustContainNChar, final boolean mustContainSChar, final String userName, final Collection<String> blackList) {
        this(minLen, mustContainAChar, mustContainACharInMixedCase, mustContainNChar, mustContainSChar, userName, blackList, null);
    }

    public PasswordRequirements(final long minLen, final boolean mustContainAChar, final boolean mustContainACharInMixedCase, final boolean mustContainNChar, final boolean mustContainSChar, final String userName, final Collection<String> blackList, final PasswordHash.Algorithm currentPwdHashAlgorithm) { 
    	this.minLength = minLen;
    	this.mustContainLetter = mustContainAChar;
    	this.mustContainLetterInMixedCase = mustContainACharInMixedCase;
    	this.mustContainDigit = mustContainNChar;
    	this.mustContainSpecial = mustContainSChar;
        this.userName = userName;
        this.blackList = Collections.unmodifiableList(blackList != null ? new ArrayList<>(blackList) : Collections.<String>emptyList());
        this.currentPwdHashAlgorithm = currentPwdHashAlgorithm;
    }

    private PasswordRequirements(final PasswordRequirements copy, final String userName){
    	this.minLength = copy.minLength;
    	this.mustContainLetter = copy.mustContainLetter;
    	this.mustContainLetterInMixedCase = copy.mustContainLetterInMixedCase;
    	this.mustContainDigit = copy.mustContainDigit;
    	this.mustContainSpecial = copy.mustContainSpecial;
    	this.currentPwdHashAlgorithm = copy.currentPwdHashAlgorithm;
        this.userName = userName;
        this.blackList = Collections.unmodifiableList(copy.blackList != null ? new ArrayList<>(copy.blackList) : Collections.<String>emptyList());
    }

    public final long getMinLength() {
        return minLength;
    }

    public final boolean mustContainDigit() {
        return mustContainDigit;
    }

    public final boolean mustContainLetter() {
        return mustContainLetter;
    }

    public final boolean mustContainLetterInMixedCase() {
        return mustContainLetter && mustContainLetterInMixedCase;
    }

    public final boolean mustContainSpecialChar() {
        return mustContainSpecial;
    }    

    public final String getUserName (){
        return userName;
    }

    public Collection<String> getBlackList() {
        return blackList;
    }

    public PasswordRequirements copyForUser(final String userName) {
    	if (Utils.checkEmptyOrNullString(userName)) {
    		throw new IllegalArgumentException("User name can't be null or empty");
    	}
    	else {
    		return new PasswordRequirements(this, userName);
    	}
    }

    public final PasswordHash.Algorithm getCurrentPwdHashAlgorithm(){
        return currentPwdHashAlgorithm;
    }

    public final boolean isPasswordGood(final String password, final Collection<AuthUtils.PwdWeaknessCheckResult> problems) {
    	if (Utils.checkEmptyOrNullString(password)) {
    		throw new IllegalArgumentException("Password to check can't be null or empty");
    	}
    	else {
            return AuthUtils.checkPwdWeakness(password, this, problems);
    	}
    }
    
    public static PasswordRequirements getDefault() {
        return DEFAULT;
    }
}
