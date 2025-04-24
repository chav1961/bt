package chav1961.bt.comm.nio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

import com.fazecast.jSerialComm.SerialPort;

import chav1961.bt.comm.io.CommPort;
import chav1961.bt.comm.utils.CommUtils;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.URIUtils;

// https://www.baeldung.com/java-nio-selector
public class ComPortChannel extends AbstractSelectableChannel implements ReadableByteChannel, WritableByteChannel {
	private final SerialPort	nested;
	
	protected ComPortChannel(final URI comm) throws FileNotFoundException, IOException {
		super(new ComPortSelectorProvider(CommUtils.prepareCommPort(comm)));
	}

	@Override
	public int validOps() {
		return SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE;
	}

	@Override
	public int write(final ByteBuffer src) throws IOException {
		if (src == null) {
			throw new NullPointerException("Source buffer can't be null");
		}
		else if (src.hasArray()) {
			final byte[]	content = src.array();
			final int		len = nested.writeBytes(content, src.position(), src.arrayOffset());
			
			if (len == -2) {
				throw new IllegalArgumentException("I/O error writing content");
			}
			else if (len == -1) {
				throw new IOException("I/O error writing content");
			}
			else {
				return len;
			}
		}
		else {
			final byte[]	content = new byte[src.position()];
			
			src.get(content);
			final int		len = nested.writeBytes(content, content.length);
			
			if (len == -2) {
				throw new IllegalArgumentException("I/O error writing content");
			}
			else if (len == -1) {
				throw new IOException("I/O error writing content");
			}
			else {
				return len;
			}
		}
	}

	@Override
	public int read(final ByteBuffer target) throws IOException {
		if (target == null) {
			throw new NullPointerException("Target buffer can't be null");
		}
		else if (target.hasArray()) {
			final byte[]	content = target.array();
			final int		len = nested.readBytes(content, content.length);
			
			if (len == -2) {
				throw new IllegalArgumentException("I/O error writing content");
			}
			else if (len == -1) {
				throw new IOException("I/O error writing content");
			}
			else {
				target.put(0, content, len, 0);
				return len;
			}
		}
		else {
			final byte[]	content = new byte[target.position()];
			
			target.get(content);
			final int		len = nested.writeBytes(content, content.length);
			
			if (len == -2) {
				throw new IllegalArgumentException("I/O error writing content");
			}
			else if (len == -1) {
				throw new IOException("I/O error writing content");
			}
			else {
				return len;
			}
		}
	}

	@Override
	protected void implCloseSelectableChannel() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void implConfigureBlocking(final boolean block) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public static ComPortChannel open(final URI comm) throws IOException {
		if (comm == null || !comm.isAbsolute()) {
			throw new IllegalArgumentException("Comm port can't be null and must be absolute");
		}
		else {
			return new ComPortChannel(comm);
		}
	}
}
