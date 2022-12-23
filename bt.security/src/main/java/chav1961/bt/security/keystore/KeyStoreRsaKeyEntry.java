package chav1961.bt.security.keystore;

import java.net.URI;
import java.security.Key;
import java.security.cert.Certificate;

import chav1961.bt.security.interfaces.KeyStoreEntryType;
import chav1961.bt.security.internal.InternalUtils;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class KeyStoreRsaKeyEntry extends KeyStoreEntry {
	private static final String			RSA_KEY_SCHEME = "rsakey";
    private static final String			KEY_RSA_KEY_LABEL = "";
    private static final String			KEY_RSA_KEY_TOOLTIP = "";
    private static final String			KEY_RSA_KEY_HELP = "";
    
    private final Certificate[] certificateChain;
    private final Key key;

    public KeyStoreRsaKeyEntry(String alias, Certificate[] certificateChain){
        super(alias, KeyStoreEntryType.RSA_KEY);
        this.certificateChain = certificateChain;
        this.key = null;
    }

    public KeyStoreRsaKeyEntry(String alias, Certificate[] certificateChain, Key key){
        super(alias, KeyStoreEntryType.RSA_KEY);
        this.certificateChain = certificateChain;
        this.key = key;
    }

    public Certificate[] getCertificateChain(){
        return certificateChain;
    }

    public Key getKey(){
        return key;
    }

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return new MutableContentNodeMetadata(getAlias(), 
				getClass(), 
				RSA_KEY_SCHEME, 
				InternalUtils.LOCALIZER.getLocalizerId(), 
				KEY_RSA_KEY_LABEL, 
				KEY_RSA_KEY_TOOLTIP, 
				KEY_RSA_KEY_HELP, 
				new FieldFormat(getClass()), 
				URI.create(APP_SCHEME+':'+RSA_KEY_SCHEME+":/"+getAlias()), 
				URI.create("root://"+getClass().getCanonicalName()+"/chav1961/bt.security/images/RSAkey.png"));
	}
}
