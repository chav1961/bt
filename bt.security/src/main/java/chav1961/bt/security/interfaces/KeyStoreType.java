package chav1961.bt.security.interfaces;

public enum KeyStoreType {
    FILE("jceks"),
    PKCS11("pkcs11");
    
    private final String fileExt;

    private KeyStoreType(final String fileExt) {
        this.fileExt = fileExt;
    }

    public String getFileExt() {
        return fileExt;
    }
}
