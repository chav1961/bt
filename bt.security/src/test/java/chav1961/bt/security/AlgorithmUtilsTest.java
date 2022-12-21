package chav1961.bt.security;

import java.security.Provider;
import java.security.Security;
import java.security.Provider.Service;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.security.AlgorithmUtils.AlgorithmDescriptor;
import chav1961.bt.security.interfaces.AlgorithmType;

public class AlgorithmUtilsTest {
	@Test
	public void providersTest() {
		int	count = 0;
		
		for (Provider item : AlgorithmUtils.getProviders()) {
			count++;
		}
		Assert.assertTrue(count > 0);
		
		int count2 = 0;
		
		for (Provider item : AlgorithmUtils.getProviders("Sun.*")) {
			count2++;
		}
		Assert.assertTrue(count2 > 0 && count2 < count);
		
		try{AlgorithmUtils.getProviders((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{AlgorithmUtils.getProviders("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{AlgorithmUtils.getProviders(".{1");
			Assert.fail("Mandatory exception was not detected (wrong 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{AlgorithmUtils.getProviders((Pattern)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void providerAlgorithmsTest() {
		final Provider				p = Security.getProvider("XMLDSig");
		
		// -----
		
		final Set<AlgorithmType>	types = new HashSet<>();
		int	count = 0;
		
		for(AlgorithmDescriptor item : AlgorithmUtils.getAlgorithms(p)) {
			Assert.assertEquals(p, item.getProvider());
			types.add(item.getType());
			count++;
		}
		Assert.assertTrue(count > 0);
		Assert.assertEquals(Set.of(AlgorithmType.TRANSFORM_SERVICE, AlgorithmType.KEY_INFO_FACTORY, AlgorithmType.XML_SIGNATURE_FACTORY), types);

		try {AlgorithmUtils.getAlgorithms((Provider)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		// -----
		
		final Set<AlgorithmType>	types2 = new HashSet<>();
		int	count2 = 0;
		
		for(AlgorithmDescriptor item : AlgorithmUtils.getAlgorithms(p, AlgorithmType.XML_SIGNATURE_FACTORY)) {
			Assert.assertEquals(p, item.getProvider());
			types2.add(item.getType());
			count2++;
		}
		Assert.assertTrue(count2 > 0);
		Assert.assertEquals(Set.of(AlgorithmType.XML_SIGNATURE_FACTORY), types2);

		try {AlgorithmUtils.getAlgorithms((Provider)null, AlgorithmType.XML_SIGNATURE_FACTORY);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {AlgorithmUtils.getAlgorithms(p, (AlgorithmType)null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}

		int	count21 = 0;
		
		for(AlgorithmDescriptor item : AlgorithmUtils.getAlgorithms(p, ".*base64")) {
			Assert.assertEquals(p, item.getProvider());
			count21++; 
		}
		Assert.assertTrue(count21 > 0);

		try {AlgorithmUtils.getAlgorithms(null, "1");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {AlgorithmUtils.getAlgorithms(null, Pattern.compile("1"));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {AlgorithmUtils.getAlgorithms(p, (String)null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {AlgorithmUtils.getAlgorithms(p, "");
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {AlgorithmUtils.getAlgorithms(p, (Pattern)null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}

		// -----
		
		final Set<AlgorithmType>	types3 = new HashSet<>();
		int	count3 = 0;
		
		for(AlgorithmDescriptor item : AlgorithmUtils.getAlgorithms(p, AlgorithmType.TRANSFORM_SERVICE, ".*base64")) {
			Assert.assertEquals(p, item.getProvider());
			types3.add(item.getType());
			count3++;
		}
		Assert.assertTrue(count3 > 0);
		Assert.assertEquals(Set.of(AlgorithmType.TRANSFORM_SERVICE), types3);

		try {AlgorithmUtils.getAlgorithms(null, AlgorithmType.ALGORITHM_PARAMETER_GENERATOR, "1");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {AlgorithmUtils.getAlgorithms(null, AlgorithmType.ALGORITHM_PARAMETER_GENERATOR, Pattern.compile("1"));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {AlgorithmUtils.getAlgorithms(p, null, "1");
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) { 
		}
		try {AlgorithmUtils.getAlgorithms(p, null, Pattern.compile("1"));
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try {AlgorithmUtils.getAlgorithms(p, AlgorithmType.ALGORITHM_PARAMETER_GENERATOR, (String)null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {AlgorithmUtils.getAlgorithms(p, AlgorithmType.ALGORITHM_PARAMETER_GENERATOR, "");
			Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {AlgorithmUtils.getAlgorithms(p, AlgorithmType.ALGORITHM_PARAMETER_GENERATOR, ".{1");
			Assert.fail("Mandatory exception was not detected (wrong 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {AlgorithmUtils.getAlgorithms(p, AlgorithmType.ALGORITHM_PARAMETER_GENERATOR, (Pattern)null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}
 
	@Test
	public void algorithmsTest() {
		int	count = 0;
		
		for(AlgorithmDescriptor item : AlgorithmUtils.getAlgorithms()) {
			count++;
		}
		Assert.assertTrue(count > 0);
		
		int	count2 = 0;
		
		for(AlgorithmDescriptor item : AlgorithmUtils.getAlgorithms(".*base64")) {
			count2++;
		}
		Assert.assertTrue(count2 > 0 && count2 < count);

		try {AlgorithmUtils.getAlgorithms((Pattern)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {AlgorithmUtils.getAlgorithms((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {AlgorithmUtils.getAlgorithms("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {AlgorithmUtils.getAlgorithms(".{1");
			Assert.fail("Mandatory exception was not detected (wrong 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void existenceTest() {
		final Provider				p = Security.getProvider("XMLDSig");
		
		Assert.assertTrue(AlgorithmUtils.exists(AlgorithmType.KEY_STORE, "JCEKS"));
		Assert.assertFalse(AlgorithmUtils.exists(AlgorithmType.KEY_STORE, "UNKNOWN"));
		
		boolean	hasStore = false;
		
		for (Provider item : AlgorithmUtils.getProviders()) {
			hasStore |= AlgorithmUtils.exists(item, AlgorithmType.KEY_STORE, "JCEKS");
		}
		Assert.assertTrue(hasStore);
		
		try {AlgorithmUtils.exists(null, "JCEKS");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {AlgorithmUtils.exists(AlgorithmType.ALGORITHM_PARAMETER_GENERATOR, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {AlgorithmUtils.exists(AlgorithmType.ALGORITHM_PARAMETER_GENERATOR, "");
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}

		try {AlgorithmUtils.exists(null, AlgorithmType.ALGORITHM_PARAMETER_GENERATOR, "JCEKS");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {AlgorithmUtils.exists(p, null, "JCEKS");
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try {AlgorithmUtils.exists(p, AlgorithmType.ALGORITHM_PARAMETER_GENERATOR, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {AlgorithmUtils.exists(p, AlgorithmType.ALGORITHM_PARAMETER_GENERATOR, "");
			Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
