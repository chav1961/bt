package chav1961.bt.security;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import chav1961.bt.security.internal.InternalUtils;
import chav1961.purelib.basic.Utils;

public class Generators {
	
	// ---
	
	public static SecureRandom generateSecureRandom() {
		return generateSecureRandom(InternalUtils.PROPS.getProperty(InternalUtils.PROP_SECURERANDOM_ALGORITHM));
	}

	public static SecureRandom generateSecureRandom(final Provider provider) {
		if (provider == null) {
			throw new NullPointerException("Security proviced can't be null"); 
		}
		else {
			return generateSecureRandom(InternalUtils.PROPS.getProperty(InternalUtils.PROP_SECURERANDOM_ALGORITHM), provider);
		}
	}

	public static SecureRandom generateSecureRandom(final String algorithm) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else {
			return generateSecureRandom(algorithm, System.nanoTime());
		}
	}

	public static SecureRandom generateSecureRandom(final String algorithm, final Provider provider) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (provider == null) {
			throw new NullPointerException("Security proviced can't be null"); 
		}
		else {
			return generateSecureRandom(algorithm, provider, System.nanoTime());
		}
	}

	public static SecureRandom generateSecureRandom(final String algorithm, final long seed) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else {
			try{final SecureRandom 	rand = SecureRandom.getInstance(algorithm);
			
				rand.setSeed(seed);
				return rand;
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Security random algorithm ["+algorithm+"] is not known"); 
			}
		}
	}
	
	public static SecureRandom generateSecureRandom(final String algorithm, final Provider provider, final long seed) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (provider == null) {
			throw new NullPointerException("Security proviced can't be null"); 
		}
		else {
			try{final SecureRandom 	rand = SecureRandom.getInstance(algorithm, provider);
			
				rand.setSeed(seed);
				return rand;
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Security random algorithm ["+algorithm+"] is not known"); 
			}
		}
	}
	
	// ---

	public static SecretKey generateSymmetricKey() {
		return generateSymmetricKey(InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class));
	}
	
	public static SecretKey generateSymmetricKey(final int length) {
		if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else {
			return generateSymmetricKey(InternalUtils.PROPS.getProperty(InternalUtils.PROP_SYMMETRIC_KEY_ALGORITHM), length);
		}
	}

	public static SecretKey generateSymmetricKey(final String algorithm) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else {
			return generateSymmetricKey(algorithm, InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class));
		}
	}
	
	public static SecretKey generateSymmetricKey(final String algorithm, final int length) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else {
			try{final KeyGenerator	key = KeyGenerator.getInstance(algorithm);
				
				key.init(length);
				return key.generateKey();
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Key algorithm name ["+algorithm+"] is not known"); 
			}
		}
	}

	public static SecretKey generateSymmetricKey(final Provider provider) {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else {
			return generateSymmetricKey(provider, InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class));
		}
	}
	
	public static SecretKey generateSymmetricKey(final Provider provider, final int length) {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else {
			return generateSymmetricKey(InternalUtils.PROPS.getProperty(InternalUtils.PROP_SYMMETRIC_KEY_ALGORITHM), provider, length);
		}
	}

	public static SecretKey generateSymmetricKey(final String algorithm, final Provider provider) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else {
			return generateSymmetricKey(algorithm, provider, InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class));
		}
	}
	
	public static SecretKey generateSymmetricKey(final String algorithm, final Provider provider, final int length) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else {
			try{final KeyGenerator	key = KeyGenerator.getInstance(algorithm, provider);
			
				key.init(length);
				return key.generateKey();
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Key algorithm name ["+algorithm+"] is not known"); 
			}
		}
	}

	public static SecretKey generateSymmetricKey(final SecureRandom random) {
		if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			return generateSymmetricKey(InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class), random);
		}
	}
	
	public static SecretKey generateSymmetricKey(final int length, final SecureRandom random) {
		if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			return generateSymmetricKey(InternalUtils.PROPS.getProperty(InternalUtils.PROP_SYMMETRIC_KEY_ALGORITHM), length, random);
		}
	}

	public static SecretKey generateSymmetricKey(final String algorithm, final SecureRandom random) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			return generateSymmetricKey(algorithm, InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class), random);
		}
	}
	
	public static SecretKey generateSymmetricKey(final String algorithm, final int length, final SecureRandom random) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			try{final KeyGenerator	key = KeyGenerator.getInstance(algorithm);
			
				key.init(length, random);
				return key.generateKey();
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Key algorithm name ["+algorithm+"] is not known"); 
			}
		}
	}
	
	public static SecretKey generateSymmetricKey(final Provider provider, final SecureRandom random) {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			return generateSymmetricKey(provider, InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class), random);
		}
	}
	
	public static SecretKey generateSymmetricKey(final Provider provider, final int length, final SecureRandom random) {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			return generateSymmetricKey(InternalUtils.PROPS.getProperty(InternalUtils.PROP_SYMMETRIC_KEY_ALGORITHM), provider, length, random);
		}
	}

	public static SecretKey generateSymmetricKey(final String algorithm, final Provider provider, final SecureRandom random) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			return generateSymmetricKey(algorithm, provider, InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class), random);
		}
	}
	
	public static SecretKey generateSymmetricKey(final String algorithm, final Provider provider, final int length, final SecureRandom random) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			try{final KeyGenerator	key = KeyGenerator.getInstance(algorithm, provider);
			
				key.init(length, random);
				return key.generateKey();
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Key algorithm name ["+algorithm+"] is not known"); 
			}
		}
	}
	
	// ---

	public static KeyPair generateKeyPair() {
		return generateKeyPair(InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class));
	}
	
	public static KeyPair generateKeyPair(final int length) {
		if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else {
			return generateKeyPair(InternalUtils.PROPS.getProperty(InternalUtils.PROP_SYMMETRIC_KEY_ALGORITHM), length);
		}
	}

	public static KeyPair generateKeyPair(final String algorithm) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else {
			return generateKeyPair(algorithm, InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class));
		}
	}
	
	public static KeyPair generateKeyPair(final String algorithm, final int length) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else {
			try{final KeyPairGenerator	key = KeyPairGenerator.getInstance(algorithm);
				
				key.initialize(length);
				return key.generateKeyPair();
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Key algorithm name ["+algorithm+"] is not known"); 
			}
		}
	}

	public static KeyPair generateKeyPair(final Provider provider) {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else {
			return generateKeyPair(provider, InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class));
		}
	}
	
	public static KeyPair generateKeyPair(final Provider provider, final int length) {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else {
			return generateKeyPair(InternalUtils.PROPS.getProperty(InternalUtils.PROP_SYMMETRIC_KEY_ALGORITHM), provider, length);
		}
	}

	public static KeyPair generateKeyPair(final String algorithm, final Provider provider) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else {
			return generateKeyPair(algorithm, provider, InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class));
		}
	}
	
	public static KeyPair generateKeyPair(final String algorithm, final Provider provider, final int length) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else {
			try{final KeyPairGenerator	key = KeyPairGenerator.getInstance(algorithm, provider);
			
				key.initialize(length);
				return key.generateKeyPair();
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Key algorithm name ["+algorithm+"] is not known"); 
			}
		}
	}

	public static KeyPair generateKeyPair(final SecureRandom random) {
		if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			return generateKeyPair(InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class), random);
		}
	}
	
	public static KeyPair generateKeyPair(final int length, final SecureRandom random) {
		if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			return generateKeyPair(InternalUtils.PROPS.getProperty(InternalUtils.PROP_SYMMETRIC_KEY_ALGORITHM), length, random);
		}
	}

	public static KeyPair generateKeyPair(final String algorithm, final SecureRandom random) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			return generateKeyPair(algorithm, InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class), random);
		}
	}
	
	public static KeyPair generateKeyPair(final String algorithm, final int length, final SecureRandom random) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			try{final KeyPairGenerator	key = KeyPairGenerator.getInstance(algorithm);
			
				key.initialize(length, random);
				return key.generateKeyPair();
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Key algorithm name ["+algorithm+"] is not known"); 
			}
		}
	}
	
	public static KeyPair generateKeyPair(final Provider provider, final SecureRandom random) {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			return generateKeyPair(provider, InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class), random);
		}
	}
	
	public static KeyPair generateKeyPair(final Provider provider, final int length, final SecureRandom random) {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			return generateKeyPair(InternalUtils.PROPS.getProperty(InternalUtils.PROP_SYMMETRIC_KEY_ALGORITHM), provider, length, random);
		}
	}

	public static KeyPair generateKeyPair(final String algorithm, final Provider provider, final SecureRandom random) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			return generateKeyPair(algorithm, provider, InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_KEY_LENGTH, int.class), random);
		}
	}
	
	public static KeyPair generateKeyPair(final String algorithm, final Provider provider, final int length, final SecureRandom random) {
		if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (length != 128 && length != 192 && length != 256) {
			throw new IllegalArgumentException("Key length [] can be 128, 192 or 256 bits only");
		}
		else if (random == null) {
			throw new NullPointerException("Secure random can't be null");
		}
		else {
			try{final KeyPairGenerator	key = KeyPairGenerator.getInstance(algorithm, provider);
			
				key.initialize(length, random);
				return key.generateKeyPair();
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Key algorithm name ["+algorithm+"] is not known"); 
			}
		}
	}

	// ---
	
	public static X509Certificate generateCertificate(String dn, KeyPair pair, int days, String algorithm)
            throws GeneralSecurityException, IOException {
        PrivateKey privkey = pair.getPrivate();
//        X509CertInfo info = new X509CertInfo();
//        Date from = new Date();
//        Date to = new Date(from.getTime() + days * 86400000l);
//        CertificateValidity interval = new CertificateValidity(from, to);
//        BigInteger sn = new BigInteger(64, new SecureRandom());
//        X500Name owner = new X500Name(dn);
//
//        info.set(X509CertInfo.VALIDITY, interval);
//        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
//        info.set(X509CertInfo.SUBJECT, owner);
//        info.set(X509CertInfo.ISSUER, owner);
//        info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
//        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
//        AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
//        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
//
//        // Sign the cert to identify the algorithm that's used.
//        X509CertImpl cert = new X509CertImpl(info);
//        cert.sign(privkey, algorithm);
//
//        // Update the algorith, and resign.
//        algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
//        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
//        cert = new X509CertImpl(info);
//        cert.sign(privkey, algorithm);
//        return cert;
        
//        Calendar expiry = Calendar.getInstance();
//        expiry.add(Calendar.DAY_OF_YEAR, 100);
//
//        X509Name x509Name = new X509Name("CN=" + dn);
//
//        V3TBSCertificateGenerator certGen = new V3TBSCertificateGenerator();
//        certGen.setSerialNumber(new DERInteger(BigInteger.valueOf(System.currentTimeMillis())));
//        certGen.setIssuer(PrincipalUtil.getSubjectX509Principal(caCert));
//        certGen.setSubject(x509Name);
//        DERObjectIdentifier sigOID = X509Util.getAlgorithmOID(“SHA1WithRSAEncryption”);
//        AlgorithmIdentifier sigAlgId = new AlgorithmIdentifier(sigOID, new DERNull());
//        certGen.setSignature(sigAlgId);
//        certGen.setSubjectPublicKeyInfo(new SubjectPublicKeyInfo((ASN1Sequence)new ASN1InputStream(
//                new ByteArrayInputStream(pubKey.getEncoded())).readObject()));
//        certGen.setStartDate(new Time(new Date(System.currentTimeMillis())));
//        certGen.setEndDate(new Time(expiry.getTime()));
//        TBSCertificateStructure tbsCert = certGen.generateTBSCertificate();        

// https://www.mayrhofer.eu.org/post/create-x509-certs-in-java/
        
        return null;
    }	
	

}
