module chav1961.bt.paint {
	requires transitive chav1961.purelib;
	requires java.desktop;
	requires java.scripting;
	requires java.xml;
	requires java.sql;
	requires java.rmi;
	requires java.management;
	requires jdk.httpserver;
	requires java.compiler;
	requires jdk.javadoc;
	requires java.base;
	
	exports chav1961.bt.paint;
	exports chav1961.bt.paint.control;
	opens chav1961.bt.paint.dialogs to chav1961.purelib;
}
