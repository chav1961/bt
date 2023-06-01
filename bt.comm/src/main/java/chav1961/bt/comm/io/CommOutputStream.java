package chav1961.bt.comm.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import com.fazecast.jSerialComm.SerialPort;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.URIUtils;

public class CommOutputStream extends OutputStream {
	private final SerialPort	nested;
	
	public CommOutputStream(final URI comm) throws IOException {
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
	public void write(int b) throws IOException {
		nested.getOutputStream().write(b);
	}
}
