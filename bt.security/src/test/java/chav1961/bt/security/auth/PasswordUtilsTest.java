package chav1961.bt.security.auth;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.security.auth.PasswordUtils.PasswordWeaknessCheckResult;
import chav1961.bt.security.interfaces.PasswordWeakness;
import chav1961.bt.security.interfaces.SecurityProcessingException;

public class PasswordUtilsTest {
	@Test
	public void weaknessTest() {
		final PasswordRequirements	pr = new PasswordRequirements(10, true, true, true, true, "UserName10@", Arrays.asList("12Ab#CdEf0"), "pha");
		
		final Set<PasswordWeaknessCheckResult>	result = new HashSet<>();
		
		result.clear();
		Assert.assertFalse(PasswordUtils.checkPasswordWeakness("UserName10@", pr, result));
		Assert.assertEquals(Set.of(new PasswordWeaknessCheckResult(PasswordWeakness.SAME_AS_USER_NAME, "UserName10@")), result);
		
		result.clear();
		Assert.assertFalse(PasswordUtils.checkPasswordWeakness("12qwQW#$_", pr, result));
		Assert.assertEquals(Set.of(new PasswordWeaknessCheckResult(PasswordWeakness.TOO_SHORT, "10")), result);

		result.clear();
		Assert.assertFalse(PasswordUtils.checkPasswordWeakness("123456#$__", pr, result));
		Assert.assertEquals(Set.of(new PasswordWeaknessCheckResult(PasswordWeakness.ALPHABETIC_CHARS_MISSING)), result);

		result.clear();
		Assert.assertFalse(PasswordUtils.checkPasswordWeakness("12abcd#$__", pr, result));
		Assert.assertEquals(Set.of(new PasswordWeaknessCheckResult(PasswordWeakness.ALL_ALPHABETIC_CHARS_IN_SAME_CASE)), result);

		result.clear();
		Assert.assertFalse(PasswordUtils.checkPasswordWeakness("xxaBcD#$__", pr, result));
		Assert.assertEquals(Set.of(new PasswordWeaknessCheckResult(PasswordWeakness.NUMERIC_CHARS_MISSING)), result);

		result.clear();
		Assert.assertFalse(PasswordUtils.checkPasswordWeakness("12aBcD3456", pr, result));
		Assert.assertEquals(Set.of(new PasswordWeaknessCheckResult(PasswordWeakness.SPECIAL_CHARS_MISSING)), result);

		result.clear();
		Assert.assertFalse(PasswordUtils.checkPasswordWeakness("12Ab#CdEf0", pr, result));
		Assert.assertEquals(Set.of(new PasswordWeaknessCheckResult(PasswordWeakness.IN_BLACKLIST)), result);

		result.clear();
		Assert.assertFalse(PasswordUtils.checkPasswordWeakness("12Ab#CdEf0___", pr, result));
		Assert.assertEquals(Set.of(new PasswordWeaknessCheckResult(PasswordWeakness.CONTAIN_FORBIDDEN_SEQUENCES, "12Ab#CdEf0")), result);
		
		result.clear();
		Assert.assertTrue(PasswordUtils.checkPasswordWeakness("12qwQW#$__", pr, result));
		Assert.assertEquals(Set.of(PasswordWeaknessCheckResult.VALID), result);

		
		try{PasswordUtils.checkPasswordWeakness(null, pr);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{PasswordUtils.checkPasswordWeakness("", pr);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{PasswordUtils.checkPasswordWeakness("abcde", null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@Test
	public void generateRandonPasswordTest() throws SecurityProcessingException {
		final PasswordRequirements	pr = new PasswordRequirements(10, true, true, true, true, "UserName10@", Arrays.asList("12Ab#CdEf0"), "pha");
		final char[]				password = PasswordUtils.generateRandomPassword(pr, 1);

		Assert.assertEquals(10, password.length);
		
		try{PasswordUtils.generateRandomPassword(null, 1);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{PasswordUtils.generateRandomPassword(new PasswordRequirements(10, false, false, false, false, "UserName10@", Arrays.asList("12Ab#CdEf0"), "pha"), 1);
			Assert.fail("Mandatory exception was not detected (no special requirements in the 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{PasswordUtils.generateRandomPassword(new PasswordRequirements(3, true, true, true, true, "UserName10@", Arrays.asList("12Ab#CdEf0"), "pha"), 1);
			Assert.fail("Mandatory exception was not detected (password length is too short in the 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{PasswordUtils.generateRandomPassword(pr, 0);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
	}	
	
	
	@Test
	public void hashTest() throws SecurityProcessingException {
		final byte[]	salt = PasswordUtils.generateSaltValue(16);
		
		Assert.assertEquals(16, salt.length);
		
		try{PasswordUtils.generateSaltValue(0);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		final char[]	password = "mzinana".toCharArray();
		final byte[]	hash = PasswordUtils.createPasswordHash(password, salt);
		
		Assert.assertEquals(32, hash.length);

		try{PasswordUtils.createPasswordHash(null, salt);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{PasswordUtils.createPasswordHash(new char[0], salt);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{PasswordUtils.createPasswordHash(password, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		try{PasswordUtils.createPasswordHash(null, salt, 655236, 256, "UNKNOWN");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{PasswordUtils.createPasswordHash(new char[0], salt, 655236, 256, "UNKNOWN");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{PasswordUtils.createPasswordHash(password, null, 655236, 256, "UNKNOWN");
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{PasswordUtils.createPasswordHash(password, salt, 0, 256, "UNKNOWN");
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{PasswordUtils.createPasswordHash(password, salt, 65536, 0, "UNKNOWN");
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{PasswordUtils.createPasswordHash(password, salt, 65536, 255, "UNKNOWN");
			Assert.fail("Mandatory exception was not detected (4-th argument not a multiple 8)");
		} catch (IllegalArgumentException exc) {
		}
		try{PasswordUtils.createPasswordHash(password, salt, 65536, 256, null);
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{PasswordUtils.createPasswordHash(password, salt, 65536, 256, "");
			Assert.fail("Mandatory exception was not detected (empty 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{PasswordUtils.createPasswordHash(password, salt, 65536, 256, "UNKNOWN");
			Assert.fail("Mandatory exception was not detected (wrong 5-th argument)");
		} catch (SecurityProcessingException exc) {
		}
	}	
}
