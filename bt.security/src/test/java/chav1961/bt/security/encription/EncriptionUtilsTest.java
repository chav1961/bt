package chav1961.bt.security.encription;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.security.auth.PasswordUtils;
import chav1961.bt.security.interfaces.SecurityProcessingException;

public class EncriptionUtilsTest {
	@Test
	public void createSecretKeyTest() throws SecurityProcessingException {
		final SecretKey	sk = EncriptionUtils.createSecretKey(128);
		
		Assert.assertEquals("AES", sk.getAlgorithm());
		
		try{EncriptionUtils.createSecretKey(0);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.createSecretKey(9);
			Assert.fail("Mandatory exception was not detected (1-st argument not multiple 8)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.createSecretKey(16);
			Assert.fail("Mandatory exception was not detected (1-st argument illegal for the given algorithm)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{EncriptionUtils.createSecretKey(null, 128);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.createSecretKey("", 128);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{EncriptionUtils.createSecretKey("AES", 0);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.createSecretKey("AES", 9);
			Assert.fail("Mandatory exception was not detected (2-nd argument not multiple 8)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.createSecretKey("AES", 16);
			Assert.fail("Mandatory exception was not detected (2-nd argument illegal for given algorithm)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	@Test
	public void createSecretKeyFromPasswordTest() throws SecurityProcessingException {
		final char[]	password = "mzinana".toCharArray();
		final byte[]	salt = PasswordUtils.generateSaltValue(16);
		final SecretKey	skp = EncriptionUtils.createSecretKeyFromPassword(password, salt);
		
		Assert.assertEquals("AES", skp.getAlgorithm());
		
		try{EncriptionUtils.createSecretKeyFromPassword(null, salt);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.createSecretKeyFromPassword(new char[0], salt);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{EncriptionUtils.createSecretKeyFromPassword(password, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		try{EncriptionUtils.createSecretKeyFromPassword(null, "AES", password, salt, 65536, 256);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.createSecretKeyFromPassword("", "AES", password, salt, 65536, 256);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.createSecretKeyFromPassword("UNKNOWN", "AES", password, salt, 65536, 256);
			Assert.fail("Mandatory exception was not detected (wrong 1-nd argument)");
		} catch (SecurityProcessingException exc) {
		}
		
		try{EncriptionUtils.createSecretKeyFromPassword("PBKDF2WithHmacSHA256", null, password, salt, 65536, 256);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.createSecretKeyFromPassword("PBKDF2WithHmacSHA256", "", password, salt, 65536, 256);
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.createSecretKeyFromPassword("PBKDF2WithHmacSHA256", "UNKNOWN", password, salt, 65536, 256);
			Assert.fail("Mandatory exception was not detected (wrong 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
	
		try{EncriptionUtils.createSecretKeyFromPassword("PBKDF2WithHmacSHA256", "AES", null, salt, 65536, 256);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.createSecretKeyFromPassword("PBKDF2WithHmacSHA256", "AES", new char[0], salt, 65536, 256);
			Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{EncriptionUtils.createSecretKeyFromPassword("PBKDF2WithHmacSHA256", "AES", password, null, 65536, 256);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}

		try{EncriptionUtils.createSecretKeyFromPassword("PBKDF2WithHmacSHA256", "AES", password, salt, 0, 256);
			Assert.fail("Mandatory exception was not detected (5-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{EncriptionUtils.createSecretKeyFromPassword("PBKDF2WithHmacSHA256", "AES", password, salt, 65536, 0);
			Assert.fail("Mandatory exception was not detected (6-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.createSecretKeyFromPassword("PBKDF2WithHmacSHA256", "AES", password, salt, 65536, 9);
			Assert.fail("Mandatory exception was not detected (6-th argument not multiple 8)");
		} catch (IllegalArgumentException exc) {
		}
	}	

	@Test
	public void byteArrayEncriptionTest() throws SecurityProcessingException {
		final byte[]			source = "test string".getBytes();
		final SecretKey			key = EncriptionUtils.createSecretKey(128);
		final IvParameterSpec	iv = EncriptionUtils.generateIinitialVector();
		
		Assert.assertArrayEquals(source, EncriptionUtils.decrypt(EncriptionUtils.encrypt(source, key, iv), key, iv));
		
		try{EncriptionUtils.encrypt(null, key, iv);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.encrypt(new byte[0], key, iv);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{EncriptionUtils.encrypt(source, null, iv);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}

		try{EncriptionUtils.encrypt(source, key, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}

		// ---

		try{EncriptionUtils.decrypt((byte[])null, key, iv);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.decrypt(new byte[0], key, iv);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	
		try{EncriptionUtils.decrypt(source, null, iv);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	
		try{EncriptionUtils.decrypt(source, key, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	
		// ---
		
		try{EncriptionUtils.encrypt(null, source, key, iv);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.encrypt("", source, key, iv);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{EncriptionUtils.encrypt("AES/CBC/PKCS5Padding", null, key, iv);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{EncriptionUtils.encrypt("AES/CBC/PKCS5Padding", source, null, iv);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}

		try{EncriptionUtils.encrypt("AES/CBC/PKCS5Padding", source, key, null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}

		// ---
		
		try{EncriptionUtils.decrypt(null, source, key, iv);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.decrypt("", source, key, iv);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{EncriptionUtils.decrypt("AES/CBC/PKCS5Padding", (byte[])null, key, iv);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{EncriptionUtils.decrypt("AES/CBC/PKCS5Padding", new byte[0], key, iv);
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{EncriptionUtils.decrypt("AES/CBC/PKCS5Padding", source, null, iv);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}

		try{EncriptionUtils.encrypt("AES/CBC/PKCS5Padding", source, key, null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void streamEncriptionTest() throws SecurityProcessingException, IOException {
		final SecretKey			key = EncriptionUtils.createSecretKey(128);
		final IvParameterSpec	iv = EncriptionUtils.generateIinitialVector();
		
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final CipherOutputStream	cos = EncriptionUtils.getCypherOutputStream(baos, key, iv)) {
			
			cos.write("test string\n".getBytes());
			cos.close();	// !!!!!!!!!
			
			try{EncriptionUtils.getCypherOutputStream(null, key, iv);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			try{EncriptionUtils.getCypherOutputStream(baos, null, iv);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			try{EncriptionUtils.getCypherOutputStream(baos, key, null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
			
			try(final ByteArrayInputStream	bais = new ByteArrayInputStream(baos.toByteArray());
				final CipherInputStream		cis = EncriptionUtils.getCypherInputStream(bais, key, iv);
				final Reader				rdr = new InputStreamReader(cis);
				final BufferedReader		brdr = new BufferedReader(rdr)) {
				
				Assert.assertEquals("test string", brdr.readLine());
				
				try{EncriptionUtils.getCypherInputStream(null, key, iv);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}

				try{EncriptionUtils.getCypherInputStream(bais, null, iv);
					Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
				} catch (NullPointerException exc) {
				}
				
				try{EncriptionUtils.getCypherInputStream(bais, key, null);
					Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
				} catch (NullPointerException exc) {
				}
			}
		}
	}

	@Test
	public void objectEncriptionTest() throws SecurityProcessingException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
		final SecretKey			key = EncriptionUtils.createSecretKey(128);
		final IvParameterSpec	iv = EncriptionUtils.generateIinitialVector();
		final Cipher			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    
	    cipher.init(Cipher.ENCRYPT_MODE, key, iv);				
		
	    final SealedObject		so = new SealedObject("test string", cipher);
	    
		Assert.assertEquals("test string", EncriptionUtils.decrypt(EncriptionUtils.encrypt("test string", key, iv), key, iv));
		
		try{EncriptionUtils.encrypt((Serializable)null, key, iv);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{EncriptionUtils.encrypt("test string", null, iv);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		try{EncriptionUtils.encrypt("test string", key, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}

		// ---
		
		try{EncriptionUtils.decrypt((SealedObject)null, key, iv);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{EncriptionUtils.decrypt(so, null, iv);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}

		try{EncriptionUtils.decrypt(so, key, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}
}
