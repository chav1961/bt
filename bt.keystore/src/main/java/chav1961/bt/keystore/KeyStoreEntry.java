package chav1961.bt.keystore;

public class KeyStoreEntry {
    private final String alias;

    protected KeyStoreEntry(String alias){
        this.alias = alias;
    }

    public String getAlias(){
        return alias;
    }
}
