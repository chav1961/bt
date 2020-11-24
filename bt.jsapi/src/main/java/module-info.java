module jsapi {
	exports javax.speech;
	exports javax.speech.recognition;
	exports javax.speech.spi;
	exports javax.speech.synthesis;
	requires java.desktop;
	requires java.scripting;
	requires java.xml;
	requires java.sql;
	requires java.rmi;
	requires java.management;
	requires jdk.httpserver;
	requires java.compiler;
	requires jdk.javadoc;
}
