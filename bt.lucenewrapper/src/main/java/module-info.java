module chav1961.bt.lucenewrapper {
	requires transitive java.sql;
	requires transitive chav1961.purelib;
	requires transitive lucene.core;
	requires transitive lucene.queryparser;

	exports chav1961.bt.lucenewrapper;
	exports chav1961.bt.lucenewrapper.interfaces;
}
