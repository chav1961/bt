module chav1961.bt.winsl {
	requires chav1961.purelib;
	requires java.desktop;
	requires java.scripting;
	requires java.xml;
	requires java.sql;
	requires java.rmi;
	requires java.management;
	requires transitive jdk.httpserver;
	requires java.compiler;
	requires jdk.javadoc;
	
	exports chav1961.bt.winsl.echoserver; 
}
