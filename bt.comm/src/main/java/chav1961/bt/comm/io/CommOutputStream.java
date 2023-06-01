package chav1961.bt.comm.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import com.fazecast.jSerialComm.SerialPort;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.URIUtils;

public class CommOutputStream extends OutputStream {
	private final OutputStream	nested;
	
	public CommOutputStream(final URI comm) throws IOException {
		if (comm == null || !comm.isAbsolute()) {
			throw new IllegalArgumentException("Comm URI can't be null and must be absolute");
		}
		else {
			final SubstitutableProperties	props = CommUtils.parseCommQueryParameters(URIUtils.parseQuery(comm));			
			OutputStream	temp = null;
			
			for (SerialPort comPort : SerialPort.getCommPorts()) {
				if (comPort.getDescriptivePortName().equals(comm.getScheme())) {
					comPort.setComPortParameters(props.getProperty(CommUtils.BAUD_RATE, int.class), 
							props.getProperty(CommUtils.DATA_BITS, int.class), 
							props.getProperty(CommUtils.STOP_BITS, CommUtils.StopBits.class).getStopBitsMode(), 
							props.getProperty(CommUtils.PARITY, CommUtils.Parity.class).getParityMode()
							);
					temp = comPort.getOutputStream();
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
	public void write(int b) throws IOException {
		nested.write(b);
	}
}
