package chav1961.bt.security.keystore;

import java.security.Key;

import chav1961.bt.security.interfaces.KeyStoreEntryType;

public class KeyStoreDesKeyEntry extends KeyStoreEntry {
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
}