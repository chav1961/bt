package chav1961.bt.comm.nio;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

import com.fazecast.jSerialComm.SerialPort;

class ComPortSelectorProvider extends SelectorProvider {
	private final SerialPort	port;
	
	ComPortSelectorProvider(final SerialPort port) {
		this.port = port;
	}
	
	@Override
	public DatagramChannel openDatagramChannel() throws IOException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	@Override
	public DatagramChannel openDatagramChannel(final ProtocolFamily family) throws IOException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	@Override
	public Pipe openPipe() throws IOException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	@Override
	public AbstractSelector openSelector() throws IOException {
		return new ComPortSelector(this.port);
	}

	@Override
	public ServerSocketChannel openServerSocketChannel() throws IOException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	@Override
	public SocketChannel openSocketChannel() throws IOException {
		throw new UnsupportedOperationException("This method is not supported");
	}
}
