module chav1961.bt.security {
	requires transitive chav1961.purelib;
	requires java.base;
	requires java.desktop;
	requires java.datatransfer;
	
	exports chav1961.bt.security;
	exports chav1961.bt.security.auth;
	exports chav1961.bt.security.encription;
	exports chav1961.bt.security.interfaces;
	exports chav1961.bt.security.keystore;
	exports chav1961.bt.security.swing;
}
