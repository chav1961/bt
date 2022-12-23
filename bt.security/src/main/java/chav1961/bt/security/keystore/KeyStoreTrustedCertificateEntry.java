package chav1961.bt.security.keystore;

import java.net.URI;
import java.security.cert.Certificate;

import chav1961.bt.security.interfaces.KeyStoreControllerException;
import chav1961.bt.security.interfaces.KeyStoreEntryType;
import chav1961.bt.security.internal.InternalUtils;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class KeyStoreTrustedCertificateEntry extends KeyStoreEntry {
	private static final String			CERTIFICATE_SCHEME = "certificate";
    private static final String			KEY_CERTIFICATE_LABEL = "";
    private static final String			KEY_CERTIFICATE_TOOLTIP = "";
    private static final String			KEY_CERTIFICATE_HELP = "";
    
    private final Certificate certificate;

    public KeyStoreTrustedCertificateEntry(String alias, Certificate certificate) {
        super(alias, KeyStoreEntryType.CERTIFICATE);
        this.certificate = certificate;
    }

    public Certificate getCertificate(){
        return certificate;
    }

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return new MutableContentNodeMetadata(getAlias(), 
				getClass(), 
				CERTIFICATE_SCHEME, 
				InternalUtils.LOCALIZER.getLocalizerId(), 
				KEY_CERTIFICATE_LABEL, 
				KEY_CERTIFICATE_TOOLTIP, 
				KEY_CERTIFICATE_HELP, 
				new FieldFormat(getClass()), 
				URI.create(APP_SCHEME+':'+CERTIFICATE_SCHEME+":/"+getAlias()), 
				URI.create("root://"+getClass().getCanonicalName()+"/chav1961/bt.security/images/certificate.png"));
	}
}
