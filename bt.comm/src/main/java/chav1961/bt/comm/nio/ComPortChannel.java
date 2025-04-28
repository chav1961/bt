package chav1961.bt.comm.nio;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import chav1961.bt.comm.utils.CommUtils;

// https://www.baeldung.com/java-nio-selector
public class ComPortChannel extends SelectableChannel implements ReadableByteChannel, WritableByteChannel {
	private final SerialPort		port;
	private final SelectorProvider	provider;
	private final Object			internalLock = new Object();
	private final CopyOnWriteArrayList<Record>		selectors = new CopyOnWriteArrayList<>();
	private volatile boolean		blocking = true;
	private volatile boolean		closed = false;
	
	protected ComPortChannel(final SerialPort port, final SelectorProvider provider) {
		this.port = port;
		this.provider = provider;
		this.port.addDataListener(new SerialPortDataListener() {
			@Override
			public void serialEvent(final SerialPortEvent event) {
				int	ops = 0;
				
				if ((event.getEventType() & SerialPort.LISTENING_EVENT_DATA_RECEIVED) != 0) {
					ops |= SelectionKey.OP_READ;
				}
				if ((event.getEventType() & SerialPort.LISTENING_EVENT_DATA_WRITTEN) != 0) {
					ops |= SelectionKey.OP_WRITE;
				}
				for (Record item : selectors) {
					if ((item.ops & ops) != 0) {
						item.sel.wakeup();
					}
				}
			}
			
			@Override
			public int getListeningEvents() {
				return SerialPort.LISTENING_EVENT_DATA_AVAILABLE | SerialPort.LISTENING_EVENT_DATA_RECEIVED | SerialPort.LISTENING_EVENT_DATA_WRITTEN;				
			}
		});
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
			final int		len = getPort().writeBytes(content, src.position(), src.arrayOffset());
			
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
			final int		len = getPort().writeBytes(content, content.length);
			
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
			final int		len = getPort().readBytes(content, content.length);
			
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
			final int		len = getPort().writeBytes(content, content.length);
			
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

	public static ComPortChannel open(final URI port) throws IOException {
		return new ComPortChannel(CommUtils.prepareCommPort(port), SelectorProvider.provider());
	}
	
	@Override
	public SelectorProvider provider() {
		return provider;
	}

	@Override
	public boolean isRegistered() {
		synchronized (blockingLock()) {
			return !selectors.isEmpty(); 
		}
	}

	@Override
	public SelectionKey keyFor(final Selector sel) {
		if (sel == null) {
			throw new NullPointerException("Selector can't be null");
		}
		else {
			synchronized (blockingLock()) {
				for(Record item : selectors) {
					if (sel.equals(item.sel)) {
						return item.key;
					}
				}
			}
			return null;
		}
	}

	@Override
	public SelectionKey register(final Selector sel, final int ops, final Object att) throws ClosedChannelException {
		if (sel == null) {
			throw new NullPointerException("Selector to register can't be null");
		}
		else if ((ops & ~validOps()) != 0) {
			throw new IllegalArgumentException("No any supported operations detected in the ops parameter. One of (SelectionKey.OP_CONNECT, SelectionKey.OP_READ, SelectionKey.OP_WRITE) keys must be presented");
		}
		else if (isChannelClosed()) {
			throw new ClosedChannelException();
		}
		else if (!sel.isOpen()) {
			throw new ClosedSelectorException();
		}
		else if (isBlocking()) {
			throw new IllegalBlockingModeException();
		}
		else {
			synchronized (blockingLock()) {
				final SK	sk = new SK(this, sel, ops); 
				
				selectors.add(new Record(sel, ops, att, sk));
				return sk; 
			}
		}
	}

	@Override
	public SelectableChannel configureBlocking(final boolean block) throws IOException {
		synchronized (blockingLock()) {
			if (block) {
				if (isRegistered()) {
					throw new  IllegalBlockingModeException();
				}
				else {
					blocking = true;
				}
			}
			else {
				blocking = false;
			}
		}
		return this;
	}

	@Override
	public boolean isBlocking() {
		return blocking;
	}

	@Override
	public Object blockingLock() {
		return internalLock;
	}

	@Override
	protected void implCloseChannel() throws IOException {
		closed = getPort().closePort();
	}

	protected boolean isChannelClosed() {
		return closed;
	}
	
	protected SerialPort getPort() {
		return port;
	}
	
	private class SK extends SelectionKey {
		private final SelectableChannel	channel;
		private final Selector			selector;
		private volatile int			ops;
		private volatile int			readyOps;
		private volatile boolean		cancelled = false;

		private SK(final SelectableChannel channel, final Selector selector, final int ops) {
			this.channel = channel;
			this.selector = selector;
			this.ops = ops;
		}

		@Override
		public SelectableChannel channel() {
			return channel;
		}

		@Override
		public Selector selector() {
			return selector;
		}

		@Override
		public boolean isValid() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void cancel() {
			this.cancelled = true;
		}

		@Override
		public int interestOps() {
			return ops;
		}

		@Override
		public SelectionKey interestOps(final int ops) {
			this.ops = ops;
			return this;
		}

		@Override
		public int readyOps() {
			if (isCancelled()) {
				throw new CancelledKeyException();
			}
			else {
				return readyOps;
			}
		}
		
		private boolean isCancelled() {
			return cancelled;
		}
	}

	private static class Record {
		final Selector	sel;
		final int		ops;
		final SK		key;	
		final Object	att;
		
		private Record(final Selector sel, final int ops, final Object att, final SK key) {
			this.sel = sel;
			this.ops = ops;
			this.att = att;
			this.key = key;
		}

		@Override
		public String toString() {
			return "Record [sel=" + sel + ", ops=" + ops + ", att=" + att + "]";
		}
	}
}
