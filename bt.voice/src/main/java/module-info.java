module chav1961.bt.voice {
	requires transitive chav1961.purelib;
	requires java.base;
	requires java.desktop;
	requires java.datatransfer;
	requires transitive vosk;
	requires com.sun.jna;
	requires freetts;
	
	exports chav1961.bt.voice.play; 
	exports chav1961.bt.voice.recognize; 
}
