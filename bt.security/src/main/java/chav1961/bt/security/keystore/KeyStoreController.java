package chav1961.bt.security.keystore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AuthProvider;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.crypto.Cipher;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;

import chav1961.bt.security.auth.PasswordRequirements;
import chav1961.bt.security.auth.PasswordUtils;
import chav1961.bt.security.interfaces.KeyStoreControllerException;
import chav1961.bt.security.interfaces.KeyStoreType;
import chav1961.bt.security.internal.InternalUtils;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;

public class KeyStoreController implements AutoCloseable {
    private static final Set<String> 	ALGORITHMS_SUPPORTED = Set.of("DES", "DESede", "AES");  
    private static final String 		PKCS11_PROVIDER = "PKCS11"; 
    private static final String 		SUN_PKCS11_PROVIDER = "SunPKCS11"; 
    private static final String 		KEYSTORE_FILE_TYPE = "JCEKS"; //"JKS"

    private final KeyStoreType 	type;
    private final KeyStore 		keystore;

    private Provider 			cachedPkcs11Provider = null;
    private String 				keystoreName = "";
    private String 				pkcs11ProviderName = "";
    
    /**
     * Default constructor, creates an empty file-based keystore
     *
     * @throws KeystoreControllerException
     */
    public KeyStoreController() throws KeyStoreControllerException {
        try{
            this.keystore = KeyStore.getInstance(KEYSTORE_FILE_TYPE);
        	this.type = KeyStoreType.FILE;
        	this.keystore.load(null, null);
        	this.keystoreName = "";
        } catch (GeneralSecurityException | IOException e) {
            throw new KeyStoreControllerException(e.getMessage(), e);
        }
    }

    /**
     * Constructor for working with a file-based keystore
     *
     * @param keystoreFile
     * @param keyStorePassword
     * @throws KeystoreControllerException
     */
    public KeyStoreController(final String keystoreFile, final char[] password) throws KeyStoreControllerException {
    	if (Utils.checkEmptyOrNullString(keystoreFile)) {
    		throw new IllegalArgumentException("Key store file path can't be null or empty"); 
    	}
    	else if (password == null || password.length == 0) {
    		throw new IllegalArgumentException("Key store password can't be null or empty array"); 
    	}
    	else {
	        try {
	            this.keystore = KeyStore.getInstance(KEYSTORE_FILE_TYPE);
	            this.type = KeyStoreType.FILE;
	            
		        try (final FileInputStream fileInputStream = new FileInputStream(keystoreFile)){
		        	this.keystore.load(fileInputStream, password);
		        	this.keystoreName = keystoreFile;
		        } catch (GeneralSecurityException | IOException e) {
		            throw new KeyStoreControllerException(e.getMessage(), e);
		        }
	        } catch (KeyStoreException e) {
	            throw new KeyStoreControllerException(e.getMessage(), e);
	        }
    	}
    }

    public KeyStoreController(final SubstitutableProperties configuration, final CallbackHandler callbackHandler) throws KeyStoreControllerException {
    	this(configuration, SUN_PKCS11_PROVIDER, callbackHandler);
    }    
    
    /**
     * Constructor for working with a PKCS#11 keystore
     *
     * @param configurationFile
     * @param callbackHandler
     * @throws KeystoreControllerException
     */
    public KeyStoreController(final SubstitutableProperties configuration, final String providerName, final CallbackHandler callbackHandler) throws KeyStoreControllerException {
    	if (configuration == null) {
    		throw new NullPointerException("Configuration props can't be null");
    	}
    	else if (Utils.checkEmptyOrNullString(providerName)) {
    		throw new IllegalArgumentException("Configuration provider name can't be null or empty");
    	}
    	else if (callbackHandler == null) {
    		throw new NullPointerException("Callback handler can't be null");
    	}
    	else {
	        final Provider 	provider = getOrCreatePKCS11Provider(configuration, providerName);
	
	        try {
	        	final KeyStore.Builder 	builder = KeyStore.Builder.newInstance(PKCS11_PROVIDER, provider, new KeyStore.CallbackHandlerProtection(callbackHandler));
	            KeyStoreException 		firstKeyStoreException = null;
	            KeyStore 				tempKeyStore = null;
	            
	            for(int tryNumber = 0, maxTries = InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_PKCS11_TRIES, int.class); tryNumber < maxTries; tryNumber++) {
	                try {
	                	tempKeyStore = builder.getKeyStore();
	                    break;
	                } catch (KeyStoreException ex) {
	                    Throwable cause = ex;
	                    
	                    while (cause != null) {
	                    	if (cause instanceof FailedLoginException) {
		                        throw new KeyStoreControllerException("Wrong password", cause);
	                    	}
	                    	else {
	                    		cause = cause.getCause();
	                    	}
	                    }
                        if (firstKeyStoreException == null) {
                            firstKeyStoreException = ex;
                        }
	                }
	            }
	            if (tempKeyStore == null) {
		        	Security.removeProvider(provider.getName());
	                throw new KeyStoreControllerException("Can't instantiate a keystore, because the device driver hasn't been properly loaded", firstKeyStoreException);
	            }
	            else {
		            tempKeyStore.load(null, null);
		            
			        this.keystoreName = tempKeyStore.getProvider().getName();
			        this.type = KeyStoreType.PKCS11;
			        this.keystore = tempKeyStore;
			        this.pkcs11ProviderName = provider.getName();
	            }
	        } catch (GeneralSecurityException | IOException e) {
	        	Security.removeProvider(provider.getName());
	            throw new KeyStoreControllerException(e.getMessage(), e);
	        }
    	}
    }

    public String getKeyStoreName() {
        return keystoreName;
    }

    public KeyStore getKeystore() {
        return keystore;
    }

    public KeyStoreType getKeyStoreType() {
        return type;
    }
    
    public String generateUniqueAliasName() throws KeyStoreControllerException {
    	final Set<String>			blackList = new HashSet<>();
    	final PasswordRequirements	rq = new PasswordRequirements(8, true, true, true, false, "", blackList, null);
    	
loop:  	for(;;) {
        	final String	alias = new String(PasswordUtils.generateRandomPassword(rq, 10));
        	
        	for (String item : getAliases()) {
        		if (alias.equals(item)) {
        			blackList.add(alias);
       				continue loop; 
        		}
			}
        	return alias;
    	}
    }

    protected String getPingKeyAlias() {
    	return InternalUtils.PROPS.getProperty(InternalUtils.PROP_DEFAULT_PKCS11_PING_KEY);
    }

    private Provider getOrCreatePKCS11Provider(final SubstitutableProperties configuration, final String providerName) throws KeyStoreControllerException {
        try{
        	synchronized (Provider.class) {
                if (cachedPkcs11Provider != null) {
                    return cachedPkcs11Provider;
                }
                final Provider provider = tryCreatePKCS11Provider(configuration, providerName);
                final Provider prevProvider = Security.getProvider(provider.getName());
                
                if (prevProvider != null) {
                    cachedPkcs11Provider = prevProvider;
                    return prevProvider;
                } else {
                    Security.addProvider(provider);
                    cachedPkcs11Provider = provider;
                    return provider;
                }
            }
        } catch (Throwable t) {
            throw new KeyStoreControllerException("Unable to initialize PKCS#11 keystore: " + t.getMessage(), t);
        }
    }

    private static Provider tryCreatePKCS11Provider(final SubstitutableProperties config, final String providerName) throws KeyStoreControllerException {
        try{final Provider p = Security.getProvider(providerName);
            
        	if (p == null) {
                throw new KeyStoreControllerException("PKCS provider ["+providerName+"] is not installed in your system");
            }
            else {
            	final StringBuilder	conf = new StringBuilder("--");	// See sun.security.pkcs11.Config sources for details
                final Method 		m = p.getClass().getMethod("configure", String.class);
                
                for (Entry<Object, Object> item : config.entrySet()) {
                	conf.append(item.getKey()).append('=').append(config.getProperty(item.getKey().toString())).append("\\\\n");
                }
                return (Provider) m.invoke(p, conf.toString());
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new KeyStoreControllerException("Unable to create Sun PKCS11 provider: " + ex.getMessage(), ex);
        }
    }

    /**
     * Saves a PKCS#11 keystore
     */
    public void savePKCS11KeyStore(final char[] password) throws KeyStoreControllerException {
        if (type != KeyStoreType.PKCS11) {
            throw new KeyStoreControllerException("savePkcs11KeyStore() should only be called for a PKCS#11 keystore");
        }
        else {
            try {
                keystore.store(null, password);
            } catch (GeneralSecurityException | IOException e) {
                throw new KeyStoreControllerException(e.getMessage(), e);
            }
        }
    }

    public void saveKeyStore(final String keystoreFile, final char[] password) throws KeyStoreControllerException {
    	if (Utils.checkEmptyOrNullString(keystoreFile)) {
    		throw new IllegalArgumentException("Key store file can't be null or empty");
    	}
    	else if (password == null || password.length == 0) {
    		throw new IllegalArgumentException("Password can't be null or empty array");
    	}
    	else if (type != KeyStoreType.FILE) {
            throw new IllegalStateException("saveKeyStoreToFile() should only be called for a file-based keystore");
        }
    	else {
	        try (final FileOutputStream fileOutputStream = new FileOutputStream(new File(keystoreFile))) {
	        	
	            keystore.store(fileOutputStream, password);
		        if (Utils.checkEmptyOrNullString(keystoreName)) {
		            keystoreName = keystoreFile;
		        }
	        } catch (GeneralSecurityException | IOException e) {
	            throw new KeyStoreControllerException(e.getMessage(), e);
	        }
    	}
    }

    /**
     * Returns an array of all aliases in keystore
     */
    public String[] getAliases() throws KeyStoreControllerException {
        return getAliases(true, true, true);
    }
    
    /**
     * Returns a list of keystore's entries
     */
    public Iterable<KeyStoreEntry> getKeyStoreEntries() throws KeyStoreControllerException {
        final ArrayList<KeyStoreEntry> list = new ArrayList<>();
        
        try{final Enumeration<String> aliases = keystore.aliases();
        
	        while (aliases.hasMoreElements()) {
	            final String alias = aliases.nextElement();
	            
	            try{if (keystore.isKeyEntry(alias)) {
	                    if (keystore.getCertificateChain(alias) != null) {
	                        list.add(new KeyStoreRsaKeyEntry(alias, keystore.getCertificateChain(alias)));
	                    } else {
	                        list.add(new KeyStoreDesKeyEntry(alias));
	                    }
	                } else if (keystore.isCertificateEntry(alias)) {
	                    list.add(new KeyStoreTrustedCertificateEntry(alias, keystore.getCertificate(alias)));
	                }
	            } catch (KeyStoreException e) {
	                throw new KeyStoreControllerException(e.getMessage(), e);
	            }
	        }
	        return list;
        } catch (KeyStoreException e) {
            throw new KeyStoreControllerException(e.getMessage(), e);
        }
    }

    /**
     * Returns an array of RSA key aliases in keystore
     */
    public String[] getRsaKeyAliases() throws KeyStoreControllerException {
        return getAliases(true, false, false);
    }

    /**
     * Returns a RSA key entry for the specified alias
     */
    public KeyStoreRsaKeyEntry getRsaKeyEntry(final String alias, final char[] keyPassword) throws KeyStoreControllerException {
    	if (Utils.checkEmptyOrNullString(alias)) {
    		throw new IllegalArgumentException("Alias name can't be null or empty"); 
    	}
    	else if (keyPassword == null || keyPassword.length == 0) {
    		throw new IllegalArgumentException("Password can't be null op empty array"); 
    	}
		else {
	        try {
	            final PrivateKey	privateKey = (PrivateKey) getKey(alias, keyPassword);
	            final Certificate[]	chain = keystore.getCertificateChain(alias);
	            
	            if (privateKey == null) {
	                throw new KeyStoreControllerException("Key is missing for alias [" + alias + "]");
	            }
	            if (chain == null || chain.length == 0) {
	                throw new KeyStoreControllerException("Certificate chain is empty for alias [" + alias + "]");
	            }
	            else {
	                return new KeyStoreRsaKeyEntry(alias, chain, privateKey);
	            }
	        } catch (GeneralSecurityException e) {
	            throw new KeyStoreControllerException(e.getMessage(), e);
	        }
		}    	
    }

    /**
     * Stores an RSA key and it's certificate chain in keystore
     */
    public void storeRsaKeyEntry(final KeyStoreRsaKeyEntry entry, final String alias, final char[] keyPassword) throws KeyStoreControllerException {
        try {
            keystore.setKeyEntry(alias, entry.getKey(), keyPassword, entry.getCertificateChain());
        } catch (KeyStoreException e) {
            throw new KeyStoreControllerException(e.getMessage(), e);
        }
    }

    /**
     * Returns an array of DES key aliases in keystore
     */
    public String[] getDesKeyAliases() throws KeyStoreControllerException {
        return getAliases(false, false, true);
    }

    /**
     * Returns a DES key entry for the specified alias
     */
    public KeyStoreDesKeyEntry getDesKeyEntry(final String alias, final char[] keyPassword) throws KeyStoreControllerException {
    	if (Utils.checkEmptyOrNullString(alias)) {
    		throw new IllegalArgumentException("Alias name can't be null or empty"); 
    	}
    	else if (keyPassword == null || keyPassword.length == 0) {
    		throw new IllegalArgumentException("Password can't be null op empty array"); 
    	}
		else {
	        final Key 	key = getKey(alias, keyPassword);
	        
	        if (key == null) {
	            throw new KeyStoreControllerException("Key is missing for alias [" + alias + "]");
	        }
	        else {
	        	return new KeyStoreDesKeyEntry(alias, key);
	        }
		}
    }

    /**
     * Stores a DES key in keystore
     */
    public void storeDesKeyEntry(final KeyStoreDesKeyEntry entry, final String alias, final char[] keyPassword) throws KeyStoreControllerException {
        try {
            keystore.setKeyEntry(alias, entry.getKey(), keyPassword, null);
        } catch (KeyStoreException e) {
            throw new KeyStoreControllerException(e.getMessage(), e);
        }
    }

    /**
     * Returns an array of trusted certificate aliases in keystore
     */
    public String[] getTrustedCertificateAliases() throws KeyStoreControllerException {
        return getAliases(false, true, false);
    }

    private String[] getAliases(final boolean includeRsaKeys, final boolean includeTrustedCertificates, final boolean includeDesKeys) throws KeyStoreControllerException {
        final ArrayList<String> aliasList = new ArrayList<>();
		try {final  Enumeration<String> 	aliasEnumeration = keystore.aliases();
		
	        while (aliasEnumeration.hasMoreElements()) {
	            final String alias = aliasEnumeration.nextElement();
	            
	            try {
	                if (keystore.isKeyEntry(alias)) {
	                    if (keystore.getCertificate(alias) != null && includeRsaKeys) {
            				aliasList.add(alias);
	                    } else if (includeDesKeys) {
            				aliasList.add(alias);
	                    }
	                }
	                else if (keystore.isCertificateEntry(alias) && includeTrustedCertificates) {
          				aliasList.add(alias);
	                }
	            } catch (KeyStoreException e) {
	                throw new KeyStoreControllerException(e.getMessage(), e);
	            }
	        }
	        
	        return aliasList.toArray(new String[aliasList.size()]);
		} catch (KeyStoreException e) {
            throw new KeyStoreControllerException(e.getMessage(), e);
		}
    }

    /**
     * Get a key from keystore or from cache
     */
    public Key getKey(final String alias, final char[] keyPassword) throws KeyStoreControllerException {
        try {
        	return keystore.getKey(alias, keyPassword);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new KeyStoreControllerException(e.getMessage(), e);
        }
    }

    /**
     * Stores a certificate chain for the specified alias
     */
    public void storeCertificateChain(final String alias, final char[] keyPassword, final X509Certificate... chain) throws KeyStoreControllerException {
        if (chain == null || chain.length == 0) {
            throw new KeyStoreControllerException("Received cerificate chain is empty for alias '" + alias + "'");
        }

        final PrivateKey privateKey;
        final X509Certificate currentCertificate;
        try {
            privateKey = (PrivateKey) getKey(alias, keyPassword);
            currentCertificate = (X509Certificate) keystore.getCertificate(alias);
        } catch (GeneralSecurityException e) {
            throw new KeyStoreControllerException(e.getMessage(), e);
        }

        if (privateKey == null) {
            throw new KeyStoreControllerException("Key is not present for alias '" + alias + "'");
        }

        if (!chain[0].getPublicKey().equals(currentCertificate.getPublicKey())) {
            throw new KeyStoreControllerException("Certificate doesn't correspond to the keypair's public key");
        }

        storeRsaKeyEntry(new KeyStoreRsaKeyEntry(alias, chain, privateKey), alias, keyPassword);
    }

    /**
     * Stores a trusted certificate in the specified alias
     */
    public void storeTrustedCertificate(final String alias, final X509Certificate certificate) throws KeyStoreControllerException {
        try {
            keystore.setCertificateEntry(alias, certificate);
        } catch (KeyStoreException e) {
            throw new KeyStoreControllerException(e.getMessage(), e);
        }
    }

    /**
     * Removes an entry with the specified alias
     */
    public void deleteEntry(final String alias) throws KeyStoreControllerException {
        try {
            keystore.deleteEntry(alias);
        } catch (KeyStoreException e) {
            throw new KeyStoreControllerException(e.getMessage(), e);
        }
    }

    /**
     * Checks whether the specified alias exists and contains an RSA key entry
     */
    public boolean containsRsaKey(final String alias) throws KeyStoreControllerException {
        try {
            return keystore.containsAlias(alias) && keystore.isKeyEntry(alias) && keystore.getCertificateChain(alias) != null;
        } catch (KeyStoreException e) {
            throw new KeyStoreControllerException(e.getMessage(), e);
        }
    }

    /**
     * Returns a certificate with the specified alias
     */
    public X509Certificate getCertificate(final String alias) throws KeyStoreControllerException {
        try {
            return (X509Certificate) keystore.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw new KeyStoreControllerException(e.getMessage(), e);
        }
    }

    /**
     * Initializes a TrustManagerFactory with the underlying keystore
     *
     * @param factory TrustManagerFactory to be initialized
     * @throws KeystoreControllerException
     */
    public void initTrustManagerFactory(final TrustManagerFactory factory) throws KeyStoreControllerException {
        try {
            factory.init(keystore);
        } catch (KeyStoreException e) {
            throw new KeyStoreControllerException(e.getMessage(), e);
        }
    }

    /**
     * Initializes a KeyManagerFactory with the underlying keystore
     *
     * @param factory     KeyManagerFactory to be initialized
     * @param keyPassword password for accessing a (single) private key in the
     *                    keystore
     * @throws KeystoreControllerException
     */
    public void initKeyManagerFactory(final KeyManagerFactory factory, final char[] keyPassword) throws KeyStoreControllerException {
        try {
            factory.init(keystore, keyPassword);
        } catch (UnrecoverableKeyException e) {
            throw new KeyStoreControllerException(e.getMessage(), e);
        } catch (GeneralSecurityException e) {
            throw new KeyStoreControllerException(e.getMessage(), e);
        }
    }

    public boolean pingPKCS11Keystore(final char[] password) {
        final String alias = getPingKeyAlias().trim();

        if (alias.isEmpty()) {
            return false;
        }
        else {
	        try {
	            final Key key = getKey(alias, password);
	            
	            if (key == null) {
	                return false;
	            }
	            else {
		            final String alg = key.getAlgorithm();
		            
		            if (ALGORITHMS_SUPPORTED.contains(alg)) {
			            final Cipher cipher = Cipher.getInstance(alg + "/ECB/NoPadding");
			            
			            cipher.init(Cipher.ENCRYPT_MODE, key);
			            cipher.doFinal(new SecureRandom().generateSeed(16));
			            return true;
		            } else {
		                return false;
		            }
	            }
	        } catch (Throwable e) {
	        	return false;
	        }
        }
    }

    protected static boolean isIncorrectPasswordException(final Exception ex) {
        for (Throwable e = ex; e != null; e = e.getCause()) {
            if ((e instanceof UnrecoverableKeyException) || (e instanceof FailedLoginException)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final void close() throws KeyStoreControllerException {
    	if (!Utils.checkEmptyOrNullString(pkcs11ProviderName)) {
	        try{
	            final AuthProvider pkcs11Provider = (AuthProvider) Security.getProvider(pkcs11ProviderName);
	            
	            if (pkcs11Provider != null) {
	                pkcs11Provider.logout();
	            }
	        } catch (Exception ex) {
	            throw new KeyStoreControllerException("Error occured on connection close", ex);
	        } finally {
	            Security.removeProvider(pkcs11ProviderName);
	        }
    	}
    }

    public static class PasswordCallbackHandler implements CallbackHandler {
        private final char[] password;

        public PasswordCallbackHandler(final char[] password) {
        	if (password == null || password.length == 0) {
        		throw new IllegalArgumentException("Password can't be null or empty array");
        	}
        	else {
                this.password = password;
        	}
        }

        @Override
        public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        	for (Callback item : callbacks) {
                if (item instanceof javax.security.auth.callback.PasswordCallback) {
                    ((PasswordCallback)item).setPassword(password);
                } 
                else {
                    throw new UnsupportedCallbackException(item, "Unsupported callback type ["+item.getClass().getCanonicalName()+"]");
                }
            }
        }
    }
}
