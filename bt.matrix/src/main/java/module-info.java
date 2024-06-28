module chav1961.bt.matrix {
	requires transitive chav1961.purelib;
	requires java.base;
	requires transitive jocl;
	requires org.yaml.snakeyaml;
	
	exports chav1961.bt.matrix;
	exports chav1961.bt.matrix.utils to org.yaml.snakeyaml;
}
