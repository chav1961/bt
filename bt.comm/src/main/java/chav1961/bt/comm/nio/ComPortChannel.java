package chav1961.bt.comm.nio;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

import chav1961.purelib.basic.Utils;

// https://www.baeldung.com/java-nio-selector
public class ComPortChannel extends AbstractSelectableChannel implements ReadableByteChannel, WritableByteChannel {
	static ComPortSelectorProvider	cpsp = new ComPortSelectorProvider();

	protected ComPortChannel(final URI comm) {
		super(cpsp);
		if (comm == null || !comm.isAbsolute()) {
			throw new IllegalArgumentException("Comm port can't be null and must be absolute");
		}
		else {
		}
	}

	@Override
	public int validOps() {
		return SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE;
	}

	@Override
	public int write(final ByteBuffer src) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int read(final ByteBuffer dst) throws IOException {
		// TODO Auto-generated method stub
		return 0;
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
		return new ComPortChannel(comm);
	}
}
