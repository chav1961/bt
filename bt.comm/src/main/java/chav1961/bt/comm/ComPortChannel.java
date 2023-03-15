package chav1961.bt.comm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

import chav1961.purelib.basic.Utils;

// https://www.baeldung.com/java-nio-selector
public class ComPortChannel extends AbstractSelectableChannel implements ReadableByteChannel, WritableByteChannel {
	static ComPortSelectorProvider	cpsp = new ComPortSelectorProvider();

	private final LoopBackComPort	lbcp;
	
	protected ComPortChannel(final String portName) {
		super(cpsp);
		if (Utils.checkEmptyOrNullString(portName)) {
			throw new IllegalArgumentException("Port name can't be null or empty");
		}
		else {
			switch (portName) {
				case "loopback" :
					this.lbcp = new LoopBackComPort();
					break;
				default :
					throw new UnsupportedOperationException("Port name ["+portName+"] is not supported"); 
			}
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

	public static ComPortChannel open(final String portName) throws IOException {
		return new ComPortChannel(portName);
	}
}
