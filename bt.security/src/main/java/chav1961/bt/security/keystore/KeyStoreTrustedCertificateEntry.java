package chav1961.bt.security.keystore;

import java.security.cert.Certificate;

public class KeyStoreTrustedCertificateEntry extends KeyStoreEntry{
    private final Certificate certificate;

    public KeyStoreTrustedCertificateEntry(String alias, Certificate certificate){
        super(alias);
        this.certificate = certificate;
    }

    public Certificate getCertificate(){
        return certificate;
    }
}
