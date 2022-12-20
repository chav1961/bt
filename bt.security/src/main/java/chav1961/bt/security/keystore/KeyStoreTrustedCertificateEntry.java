package chav1961.bt.security.keystore;

import java.security.cert.Certificate;

import chav1961.bt.security.interfaces.KeyStoreEntryType;

public class KeyStoreTrustedCertificateEntry extends KeyStoreEntry{
    private final Certificate certificate;

    public KeyStoreTrustedCertificateEntry(String alias, Certificate certificate){
        super(alias, KeyStoreEntryType.CERTIFICATE);
        this.certificate = certificate;
    }

    public Certificate getCertificate(){
        return certificate;
    }
}
