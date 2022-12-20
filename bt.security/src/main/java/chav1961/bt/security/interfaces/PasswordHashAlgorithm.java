package chav1961.bt.security.interfaces;

public enum PasswordHashAlgorithm {
    SHA1("SHA-1"), 
    SHA256("SHA-256");
    
    private final String asString;
    
    private PasswordHashAlgorithm(final String algo){
        asString = algo;
    }
    
    public String getTitle() {
        return asString;
    }
}