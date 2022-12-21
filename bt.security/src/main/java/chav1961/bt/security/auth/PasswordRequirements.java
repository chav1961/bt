package chav1961.bt.security.auth;

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
    private final String	currentPwdHashAlgorithm;
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

    public PasswordRequirements(final long minLen, final boolean mustContainAChar, final boolean mustContainACharInMixedCase, final boolean mustContainNChar, final boolean mustContainSChar, final String userName, final Collection<String> blackList, final String currentPwdHashAlgorithm) { 
    	this.minLength = minLen;
    	this.mustContainLetter = mustContainAChar;
    	this.mustContainLetterInMixedCase = mustContainACharInMixedCase;
    	this.mustContainDigit = mustContainNChar;
    	this.mustContainSpecial = mustContainSChar;
        this.userName = userName;
        this.blackList = Collections.unmodifiableList(blackList != null ? new ArrayList<>(blackList) : Collections.<String>emptyList());
        this.currentPwdHashAlgorithm = currentPwdHashAlgorithm;
    }

    private PasswordRequirements(final PasswordRequirements copy, final String userName) {
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

    public final String getCurrentPwdHashAlgorithm(){
        return currentPwdHashAlgorithm;
    }

    public final boolean isPasswordGood(final String password, final Collection<PasswordUtils.PasswordWeaknessCheckResult> problems) {
    	if (Utils.checkEmptyOrNullString(password)) {
    		throw new IllegalArgumentException("Password to check can't be null or empty");
    	}
    	else {
            return PasswordUtils.checkPasswordWeakness(password, this, problems);
    	}
    }

    public static PasswordRequirements getDefault() {
        return DEFAULT;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blackList == null) ? 0 : blackList.hashCode());
		result = prime * result + ((currentPwdHashAlgorithm == null) ? 0 : currentPwdHashAlgorithm.hashCode());
		result = prime * result + (int) (minLength ^ (minLength >>> 32));
		result = prime * result + (mustContainDigit ? 1231 : 1237);
		result = prime * result + (mustContainLetter ? 1231 : 1237);
		result = prime * result + (mustContainLetterInMixedCase ? 1231 : 1237);
		result = prime * result + (mustContainSpecial ? 1231 : 1237);
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PasswordRequirements other = (PasswordRequirements) obj;
		if (blackList == null) {
			if (other.blackList != null) return false;
		} else if (!blackList.equals(other.blackList)) return false;
		if (currentPwdHashAlgorithm == null) {
			if (other.currentPwdHashAlgorithm != null) return false;
		} else if (!currentPwdHashAlgorithm.equals(other.currentPwdHashAlgorithm)) return false;
		if (minLength != other.minLength) return false;
		if (mustContainDigit != other.mustContainDigit) return false;
		if (mustContainLetter != other.mustContainLetter) return false;
		if (mustContainLetterInMixedCase != other.mustContainLetterInMixedCase) return false;
		if (mustContainSpecial != other.mustContainSpecial) return false;
		if (userName == null) {
			if (other.userName != null) return false;
		} else if (!userName.equals(other.userName)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "PasswordRequirements [minLength=" + minLength + ", mustContainLetter=" + mustContainLetter
				+ ", mustContainLetterInMixedCase=" + mustContainLetterInMixedCase + ", mustContainDigit="
				+ mustContainDigit + ", mustContainSpecial=" + mustContainSpecial + ", userName=" + userName
				+ ", currentPwdHashAlgorithm=" + currentPwdHashAlgorithm + ", blackList=" + blackList + "]";
	}
}
