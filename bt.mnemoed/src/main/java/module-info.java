module chav1961.bt.mnemoed {
	requires transitive chav1961.purelib;
	requires chav1961.bt.mnemort;
	requires java.desktop;
	requires java.scripting;
	requires java.xml;
	requires java.sql;
	requires java.rmi;
	requires java.management;
	requires jdk.httpserver;
	requires java.compiler;
	requires jdk.javadoc;
	
	opens chav1961.bt.mnemoed to chav1961.purelib;
}
