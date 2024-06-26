package chav1961.bt.comm.spi;

import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

/**
 * <p>This class is an SPI provider for the {@linkplain Handler} class. Don't use it directly</p>
 * @since 0.0.1
 */
public class CommHandlerProvider extends URLStreamHandlerProvider {
	@Override
	public URLStreamHandler createURLStreamHandler(final String protocol) {
		if (Handler.PROTOCOL.equalsIgnoreCase(protocol)) {
			return new Handler();
		}
		else {
			return null;
		}
	}
}
