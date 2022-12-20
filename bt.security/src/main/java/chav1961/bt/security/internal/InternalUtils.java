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

// https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html
public class InternalUtils {
	public static final String	PROP_PROPERTY_LOCATION_URI = "bt.security.props.location.uri";
	
	public static final String	PROP_SECURERANDOM_ALGORITHM = "bt.security.securerandom.algorithm"; 
	public static final String	PROP_PASSWORD_HASH_ALGORITHM = "bt.security.passwordhash.algorithm"; 
	public static final String	PROP_PASSWORD_KEY_ALGORITHM = "bt.security.passwordkey.algorithm"; 
	public static final String	PROP_CIPHER_TYPE_ALGORITHM = "bt.security.ciphertype.algorithm"; 
	public static final String	PROP_AES_ENCRYPTION_ALGORITHM = "bt.security.aes.encryption.algorithm";
	
	public static final String	PROP_DEFAULT_ITERATIONS = "bt.security.default.iterations"; 
	public static final String	PROP_DEFAULT_KEY_LENGTH = "bt.security.default.keylength"; 
	public static final String	PROP_DEFAULT_PKCS11_TRIES = "bt.security.pkcs11.default.tries"; 
	public static final String	PROP_DEFAULT_PKCS11_PING_KEY = "bt.security.pkcs11.default.pingkey"; 
		
	public static final SubstitutableProperties	PROPS;
    public static final Random 					RANDOM;
    
    private static final String	PROP_DEFAULT_LOCATION_URI = "root://"+InternalUtils.class.getCanonicalName()+"/bt.security.default.properties";
	
	static {
		final SubstitutableProperties	parent = new SubstitutableProperties();

		try(final InputStream	is = URI.create(PROP_DEFAULT_LOCATION_URI).toURL().openStream()) {
			
			parent.load(is);
		} catch (IOException exc) {
			throw new PreparationException(exc.getLocalizedMessage(), exc);
		}

		PROPS = new SubstitutableProperties(parent);
		
		try(final InputStream	is = PureLibSettings.instance().getProperty(PROP_PROPERTY_LOCATION_URI, URI.class, PROP_DEFAULT_LOCATION_URI).toURL().openStream()) {
			
			PROPS.load(is);
			RANDOM = SecureRandom.getInstance(PROPS.getProperty(PROP_SECURERANDOM_ALGORITHM));
		} catch (IOException | NoSuchAlgorithmException e) {
			throw new PreparationException(e.getLocalizedMessage(), e);
		}
		PureLibSettings.PURELIB_LOCALIZER.add(Localizer.Factory.newInstance(URI.create("xml:root://"+InternalUtils.class.getCanonicalName()+"/chav1961/bt.security/i18n/i18n.xml")));
	}
}
