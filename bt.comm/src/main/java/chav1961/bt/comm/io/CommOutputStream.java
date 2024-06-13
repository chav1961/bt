package chav1961.bt.comm.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import com.fazecast.jSerialComm.SerialPort;

import chav1961.bt.comm.utils.CommUtils;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.URIUtils;

public class CommOutputStream extends OutputStream {
	private final SerialPort	nested;
	private final OutputStream	nestedStream;
	
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
			else if (!nested.openPort()) {				
				throw new IOException("Comm port ["+name+"] error #"+nested.getLastErrorCode()); 
			}
			else {
				this.nestedStream = nested.getOutputStream();
			}
		}
	}

	@Override
	public void write(int b) throws IOException {
		nestedStream.write(b);
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		nestedStream.close();
		nested.closePort();
	}
}
