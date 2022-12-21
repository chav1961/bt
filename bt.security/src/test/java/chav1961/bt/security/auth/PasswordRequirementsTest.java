package chav1961.bt.security.auth;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class PasswordRequirementsTest {
	@Test
	public void basicTest() {
		final PasswordRequirements	pr = new PasswordRequirements(10, true, true, true, true, "user", Arrays.asList("assa"), "pha");
		final PasswordRequirements	pr2 = pr.copyForUser("user");

		Assert.assertEquals(10, pr.getMinLength());
		Assert.assertTrue(pr.mustContainDigit());
		Assert.assertTrue(pr.mustContainLetter());
		Assert.assertTrue(pr.mustContainLetterInMixedCase());
		Assert.assertTrue(pr.mustContainSpecialChar());
		Assert.assertEquals("user", pr.getUserName());
		Assert.assertEquals(Arrays.asList("assa"), pr.getBlackList());
		Assert.assertEquals("pha", pr.getCurrentPwdHashAlgorithm());
		
		Assert.assertTrue(pr.equals(pr2));
		Assert.assertEquals(pr2.hashCode(), pr.hashCode());
		Assert.assertEquals(pr2.toString(), pr.toString());
	}
}
