package chav1961.bt.comm.spi;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Hashtable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;
import javax.sound.sampled.TargetDataLine;

import com.fazecast.jSerialComm.SerialPort;

import chav1961.bt.comm.io.CommPort;
import chav1961.bt.comm.utils.CommUtils;
import chav1961.bt.comm.utils.CommUtils.Parity;
import chav1961.bt.comm.utils.CommUtils.StopBits;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;

//
//	URL format:
//		comm://comNNN:?baud={110|300|600|1200|2400|4800|9600|19200|38400|57600|115200}&parity={odd|even|none|mark|space}&stop={1|1.5|2}&data={5|7|8}&flow={none|cts|rts_cts|dsr|dtr_dsr|XonXoff}
//		comm://enumerate
//

class CommURLConnection extends URLConnection implements Closeable {
	private final URI	toConnect;
	private boolean		connected = false;
	private byte[]		enumerator = null;
	private CommPort	port = null;
	
	CommURLConnection(final URL url) throws IOException {
		super(url);
		if (url == null) {
			throw new NullPointerException("URL to open connection can't be null");
		}
		else {
			try {
				this.toConnect = url.toURI();
			} catch (URISyntaxException e) {
				throw new IOException(e.getLocalizedMessage(), e);
			}
		}
	}


	@Override
	public void connect() throws IOException {
		if ("enumerate".equalsIgnoreCase(toConnect.getHost())) {
			final StringBuilder	result = new StringBuilder();
			
			for(SerialPort item : SerialPort.getCommPorts()) {
				result.append(Handler.PROTOCOL).append("://").append(item.getSystemPortName());
				result.append('?').append(CommUtils.BAUD_RATE).append('=').append(item.getBaudRate());
				result.append('&').append(CommUtils.DATA_BITS).append('=').append(item.getNumDataBits());
				result.append('&').append(CommUtils.STOP_BITS).append('=');
				switch (item.getNumStopBits()) {
					case SerialPort.ONE_STOP_BIT 	:
						result.append(CommUtils.StopBits.one);
						break;
					case SerialPort.ONE_POINT_FIVE_STOP_BITS :
						result.append(CommUtils.StopBits.oneAndHalf);
						break;
					case SerialPort.TWO_STOP_BITS	:
						result.append(CommUtils.StopBits.two);
						break;
					default :
						throw new UnsupportedOperationException("Unsupported number of stop bits ["+item.getNumStopBits()+"] detected");
				}
				result.append('&').append(CommUtils.PARITY).append('=').append(CommUtils.Parity.of(item.getParity()));
				result.append('&').append(CommUtils.FLOW_CONTROL).append('=').append(CommUtils.FlowControl.of(item.getFlowControlSettings()));
				result.append(System.lineSeparator());
			}
			this.enumerator = result.toString().getBytes(PureLibSettings.DEFAULT_CONTENT_ENCODING);
			this.connected = true;
		}
		else if (connected) {
			throw new IllegalStateException("Attempt to call connect twice");
		}
		else {
			this.port = new CommPort(toConnect);
			this.connected = true;
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		if (!connected) {
			throw new IllegalStateException("Attempt to call this method before connection. Call connect firstly");
		}
		else if (this.port != null) {
			return port.getOutputStream(); 
		}
		else {
			throw new IOException("Com URI ["+toConnect+"] doesn't support output");
		}
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		if (!connected) {
			throw new IllegalStateException("Attempt to call this method before connection. Call connect firstly");
		}
		else if (this.port != null) {
			return port.getInputStream(); 
		}
		else {
			return new ByteArrayInputStream(enumerator);
		}
	}
	
	@Override
	public void close() throws IOException {
		if (!connected) {
			throw new IllegalStateException("Communication port is not connected or was closed earlier.");
		}
		else {
			try {
				if (port != null) {
					port.close();
				}
			} finally {
				enumerator = null;
				port = null;
				connected = false;
			}
		}
	}
}
