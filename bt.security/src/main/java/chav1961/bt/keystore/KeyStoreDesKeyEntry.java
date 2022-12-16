package chav1961.bt.keystore;

import java.security.Key;

public class KeyStoreDesKeyEntry extends KeyStoreEntry {
    private final Key key;

    public KeyStoreDesKeyEntry(String alias){
        super(alias);
        this.key = null;
    }

    public KeyStoreDesKeyEntry(String alias, Key key){
        super(alias);
        this.key = key;
    }
    
    public Key getKey(){
        return key;
    }
}