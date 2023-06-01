package chav1961.bt.comm.nio;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

class ComPortSelectorProvider extends SelectorProvider {
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
		// TODO Auto-generated method stub
		return null;
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
