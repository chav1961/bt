/**
 * <p>This module contains Pure Library project content.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @see <a href="http://github.com/chav1961/purelib">Pure Library</a> project
 * @since 0.0.4
 */
module chav1961.bt.grants {
	requires transitive chav1961.purelib;
	requires transitive java.desktop;
	requires transitive java.scripting;
	requires java.xml;
	requires java.logging;
	requires jdk.jdi;
	requires jdk.unsupported;
	requires transitive java.sql;
	requires transitive java.rmi;
	requires java.management;
	requires transitive jdk.httpserver;

	exports chav1961.bt.grants; 
	exports chav1961.bt.grants.interfaces; 
}
