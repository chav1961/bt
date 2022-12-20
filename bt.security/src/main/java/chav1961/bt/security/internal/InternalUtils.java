package chav1961.bt.security.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.i18n.interfaces.Localizer;

// https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html#securerandom-number-generation-algorithms

public class InternalUtils {
	public static final String					PROP_SECURERANDOM_ALGORITHM = "bt.security.securerandom.algorithm"; 
	public static final String					PROP_PASSWORD_HASH_ALGORITHM = "bt.security.passwordhash.algorithm"; 
	public static final String					PROP_PASSWORD_KEY_ALGORITHM = "bt.security.passwordkey.algorithm"; 
	public static final String					PROP_CIPHER_TYPE_ALGORITHM = "bt.security.ciphertype.algorithm"; 
	public static final String					PROP_AES_ENCRYPTION_ALGORITHM = "bt.security.aes.encryption.algorithm"; 
	public static final String					PROP_DEFAULT_ITERATIONS = "bt.security.default.iterations"; 
	public static final String					PROP_DEFAULT_KEY_LENGTH = "bt.security.default.keylength"; 
	
	public static final SubstitutableProperties	PROPS = new SubstitutableProperties();
    public static final Random 					RANDOM;  
	
	static {
		try(final InputStream	is = URI.create("root://"+InternalUtils.class.getCanonicalName()+"/bt.security.default.properties").toURL().openStream()) {
			
			PROPS.load(is);
			RANDOM = SecureRandom.getInstance(PROPS.getProperty(PROP_SECURERANDOM_ALGORITHM));
		} catch (IOException | NoSuchAlgorithmException e) {
			throw new PreparationException(e.getLocalizedMessage(), e);
		}
		PureLibSettings.PURELIB_LOCALIZER.add(Localizer.Factory.newInstance(URI.create("xml:root://"+InternalUtils.class.getCanonicalName()+"/chav1961/bt.security/i18n/i18n.xml")));
	}
}
