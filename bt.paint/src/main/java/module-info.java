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
	requires java.datatransfer;
	
	exports chav1961.bt.paint;
	exports chav1961.bt.paint.control;
	exports chav1961.bt.paint.script.interfaces;
	exports chav1961.bt.paint.script.intern to chav1961.purelib;
	opens chav1961.bt.paint.dialogs to chav1961.purelib;
}
