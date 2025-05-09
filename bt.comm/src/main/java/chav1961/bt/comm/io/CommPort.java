package chav1961.bt.comm.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import com.fazecast.jSerialComm.SerialPort;

import chav1961.bt.comm.utils.CommUtils;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.interfaces.InputOutputPairInterface;

public class CommPort implements InputOutputPairInterface {
	private final SerialPort	nested;
	private final InputStream	nestedIn;
	private final OutputStream	nestedOut;

	public CommPort(final URI comm) throws IOException {
		if (comm == null || !comm.isAbsolute()) {
			throw new IllegalArgumentException("Comm URI can't be null and must be absolute");
		}
		else {
			final SubstitutableProperties	props = CommUtils.parseCommQueryParameters(URIUtils.parseQuery(comm));	
			final String					name = comm.getHost();
			
			if (!CommUtils.hasCommPort(name)) {
				throw new FileNotFoundException("Unknown comm port name ["+name+"]"); 
			}
			else {
				this.nested = CommUtils.prepareCommPort(name, props);
				if (!nested.openPort()) {				
					throw new IOException("Comm port ["+name+"] error #"+nested.getLastErrorCode()); 
				}
				else {
					nested.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING | SerialPort.TIMEOUT_READ_BLOCKING, 5000, 5000);
					this.nestedIn = nested.getInputStream();
					this.nestedOut = nested.getOutputStream();
				}
			}
		}
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		if (nested.isOpen()) {
			return nestedIn;
		}
		else {
			throw new IOException("Get input stream on closed port");
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		if (nested.isOpen()) {
			return nestedOut;
		}
		else {
			throw new IOException("Get output stream on closed port");
		}
	}

	@Override
	public void close() throws IOException {
		nestedIn.close();
		nestedOut.close();
		nested.closePort();
	}
}
