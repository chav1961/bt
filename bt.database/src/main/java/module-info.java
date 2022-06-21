module chav1961.bt.database {
	exports chav1961.bt.database;
	exports chav1961.bt.database.interfaces;
	
	requires transitive java.sql;
	requires transitive chav1961.purelib;
	requires java.base;
	requires java.xml;

	uses chav1961.bt.database.storage.providers.AbstractIOProvider;
	provides chav1961.bt.database.storage.providers.AbstractIOProvider with chav1961.bt.database.storage.providers.RandomAccessFileProvider;	
}
