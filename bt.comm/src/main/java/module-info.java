module chav1961.bt.comm {
	requires transitive chav1961.purelib;
	requires java.base;
	requires com.fazecast.jSerialComm;

	uses java.net.spi.URLStreamHandlerProvider;
	provides java.net.spi.URLStreamHandlerProvider with chav1961.bt.comm.spi.CommHandlerProvider;
}
