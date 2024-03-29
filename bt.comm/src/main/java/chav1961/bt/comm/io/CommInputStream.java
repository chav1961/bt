package chav1961.bt.comm.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.fazecast.jSerialComm.SerialPort;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.URIUtils;

public class CommInputStream extends InputStream {
	private final SerialPort nested;
	
	public CommInputStream(final URI comm) throws IOException {
		if (comm == null || !comm.isAbsolute()) {
			throw new IllegalArgumentException("Comm URI can't be null and must be absolute");
		}
		else {
			final SubstitutableProperties	props = CommUtils.parseCommQueryParameters(URIUtils.parseQuery(comm));	
			final String	name = comm.getScheme();
			
			this.nested = CommUtils.prepareCommPort(name, props);
			if (this.nested == null) {
				throw new FileNotFoundException("Unknown comm port name ["+name+"]"); 
			}
		}
	}

	@Override
	public int read() throws IOException {
		return nested.getInputStream().read();
	}
}
