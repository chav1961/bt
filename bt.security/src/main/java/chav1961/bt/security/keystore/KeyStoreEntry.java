package chav1961.bt.security.keystore;

import chav1961.bt.security.interfaces.KeyStoreEntryType;

public abstract class KeyStoreEntry extends KeyStoreItem {
    private final String 			alias;
    private final KeyStoreEntryType	type;

    protected KeyStoreEntry(final String alias, final KeyStoreEntryType type) {
        this.alias = alias;
        this.type = type;
    }

    public String getAlias() {
        return alias;
    }
    
    public KeyStoreEntryType getEntryType() {
    	return type;
    }
}
