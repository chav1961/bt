module chav1961.bt.ocr {
	requires transitive chav1961.purelib;
	requires java.base;
	requires java.desktop;
	requires java.datatransfer;
	requires tess4j;
	requires simplemagic;
	
	exports chav1961.bt.ocr;
	opens chav1961.bt.ocr to chav1961.purelib;
}
