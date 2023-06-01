package chav1961.bt.comm.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.fazecast.jSerialComm.SerialPort;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.URIUtils;

public class CommInputStream extends InputStream {
	private final InputStream	nested;
	
	public CommInputStream(final URI comm) throws IOException {
		if (comm == null || !comm.isAbsolute()) {
			throw new IllegalArgumentException("Comm URI can't be null and must be absolute");
		}
		else {
			final SubstitutableProperties	props = CommUtils.parseCommQueryParameters(URIUtils.parseQuery(comm));			
			InputStream	temp = null;
			
			for (SerialPort comPort : SerialPort.getCommPorts()) {
				if (comPort.getDescriptivePortName().equals(comm.getScheme())) {
					comPort.setComPortParameters(props.getProperty(CommUtils.BAUD_RATE, int.class), 
							props.getProperty(CommUtils.DATA_BITS, int.class), 
							props.getProperty(CommUtils.STOP_BITS, CommUtils.StopBits.class).getStopBitsMode(), 
							props.getProperty(CommUtils.PARITY, CommUtils.Parity.class).getParityMode()
							);
					temp = comPort.getInputStream();
					break;
				}
			}
			if (temp == null) {
				throw new FileNotFoundException("Unknown comm port name ["+comm.getScheme()+"]"); 
			}
			else {
				this.nested = temp;
			}
		}
	}

	@Override
	public int read() throws IOException {
		return nested.read();
	}
}
