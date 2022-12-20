package chav1961.bt.security.keystore;

import java.security.Key;
import java.security.cert.Certificate;

import chav1961.bt.security.interfaces.KeyStoreEntryType;

public class KeyStoreRsaKeyEntry extends KeyStoreEntry {
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
}
