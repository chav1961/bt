package chav1961.bt.security.keystore;

import java.net.URI;
import java.security.Key;

import chav1961.bt.security.interfaces.KeyStoreEntryType;
import chav1961.bt.security.internal.InternalUtils;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class KeyStoreDesKeyEntry extends KeyStoreEntry {
	private static final String			DES_KEY_SCHEME = "deskey";
    private static final String			KEY_DES_KEY_LABEL = "";
    private static final String			KEY_DES_KEY_TOOLTIP = "";
    private static final String			KEY_DES_KEY_HELP = "";

    private final Key key;

    public KeyStoreDesKeyEntry(String alias){
        super(alias, KeyStoreEntryType.DES_KEY);
        this.key = null;
    }

    public KeyStoreDesKeyEntry(String alias, Key key){
        super(alias, KeyStoreEntryType.DES_KEY);
        this.key = key;
    }
    
    public Key getKey(){
        return key;
    }

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return new MutableContentNodeMetadata(getAlias(), 
				getClass(), 
				DES_KEY_SCHEME, 
				InternalUtils.LOCALIZER.getLocalizerId(), 
				KEY_DES_KEY_LABEL, 
				KEY_DES_KEY_TOOLTIP, 
				KEY_DES_KEY_HELP, 
				new FieldFormat(getClass()), 
				URI.create(APP_SCHEME+':'+DES_KEY_SCHEME+":/"+getAlias()), 
				URI.create("root://"+getClass().getCanonicalName()+"/chav1961/bt.security/images/DESkey.png"));
	}
}


//byte[] data = "test".getBytes("UTF8");
//KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
//kpg.initialize(512);
//KeyPair keyPair = kpg.genKeyPair();
//
//byte[] pk = keyPair.getPublic().getEncoded();
//X509EncodedKeySpec spec = new X509EncodedKeySpec(pk);
//KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//PublicKey pubKey = keyFactory.generatePublic(spec);
//Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//cipher.init(Cipher.ENCRYPT_MODE, pubKey);
//byte[] encrypted = cipher.doFinal(data);
//
//byte[] priv = keyPair.getPrivate().getEncoded();
//PKCS8EncodedKeySpec spec2 = new PKCS8EncodedKeySpec(priv);
//PrivateKey privKey = keyFactory.generatePrivate(spec2);
//cipher.init(Cipher.DECRYPT_MODE, privKey);
//byte[] plain = cipher.doFinal(encrypted);


//KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
//keyGen.initialize(1024, new SecureRandom());
//KeyPair pair = keyGen.generateKeyPair();
//
//PrivateKey priv = pair.getPrivate();
//dsa.initSign(priv);
//
//PublicKey pub = pair.getPublic();
//
//byte[] encoded = pub.getEncoded();
//
//PublicKey fromEncoded = KeyFactory.getInstance("DSA", "SUN").generatePublic(new X509EncodedKeySpec(encoded));
//dsa.initVerify(fromEncoded);


//KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
//kpg.initialize(2048);
//KeyPair keyPair = kpg.generateKeyPair();
//PublicKey pub = keyPair.getPublic();
//PrivateKey prv = keyPair.getPrivate();
//
//byte[] pubBytes = pub.getEncoded();
//byte[] prvBytes = prv.getEncoded();
//
////now save pubBytes or prvBytes
//
////to recover the key
//KeyFactory kf = KeyFactory.getInstance("RSA");
//PrivateKey prv_recovered = kf.generatePrivate(new PKCS8EncodedKeySpec(prvBytes));
//PublicKey pub_recovered = kf.generatePublic(new X509EncodedKeySpec(pubBytes));

//KeyGenerator kgenerator = KeyGenerator.getInstance("AES");
//SecureRandom random = new SecureRandom();
//kgenerator.init(128, random);
//Key aeskey = kgenerator.generateKey();
//
//KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
//kpg.initialize(1024, random);
//KeyPair kpa = kpg.genKeyPair();
//PublicKey pubKey = kpa.getPublic();
//PrivateKey privKey = kpa.getPrivate();    
//
////Encrypt the generated Symmetric AES Key using RSA cipher  
//Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");            
//rsaCipher.init(Cipher.WRAP_MODE, pubKey);
//byte[] encryptedSymmKey = rsaCipher.wrap(aeskey);            
////RSA Decryption of Encrypted Symmetric AES key
//rsaCipher.init(Cipher.UNWRAP_MODE, privKey);
//Key decryptedKey = rsaCipher.unwrap(encryptedSymmKey, "AES", Cipher.SECRET_KEY);
