module chav1961.bt.security {
	requires transitive chav1961.purelib;
	requires java.base;
	
	exports chav1961.bt.security.auth;
	exports chav1961.bt.security.encription;
	exports chav1961.bt.security.interfaces;
	exports chav1961.bt.security.keystore;
}
