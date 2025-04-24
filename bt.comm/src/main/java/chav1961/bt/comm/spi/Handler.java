package chav1961.bt.comm.spi;


import java.io.IOException;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * <p>This class is  handler to support "comm" schema URL. Format of comm URL is:</p>
 * <code><b>comm://</b>&lt;device&gt;?&lt;parameters&gt;</code>
 * <ul>
 * <li>device - communication port (for example <b>COM1:</b>)</li>
 * <li>parameters - communication parameters</li>
 * </ul>
 * <p>Parameters can be:</p>
 * <ul>
 * <li>baud=&lt;number&gt; - baud rate. Available values are (110, 300, 600, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200)</li>
 * <li>parity=&lt;value&gt; - parity check (see {@linkplain CommUtils.Parity}).
 * <li>stop=&lt;value&gt; - number of stop bits. Available values are 1, 1.5 and 2 or {@linkplain CommUtils.StopBits}</li>
 * <li>data=&lt;number&gt; - data bits length. Available values are 5, 7 and 8</li>
 * <li>flow=&lt;value&gt; - flow control.type (see {@linkplain CommUtils.FlowControl}). </li>
 * </ul>
 * @see URLStreamHandler   
 * @see CommHandlerProvider   
 * @see CommUtils.Parity
 * @see CommUtils.StopBits
 * @see CommUtils.FlowControl
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @thread.safe
 */
class Handler extends URLStreamHandler {
	public static final String	PROTOCOL = "comm";
	
	@Override
	protected URLConnection openConnection(final URL url) throws IOException {
		if (PROTOCOL.equals(url.getProtocol())) {
			return new CommURLConnection(url);
		}
		else {
			throw new IOException("Illegal URL ["+url+"]: protocol ["+url.getProtocol()+"] is not supported"); 
		}
	}
}
