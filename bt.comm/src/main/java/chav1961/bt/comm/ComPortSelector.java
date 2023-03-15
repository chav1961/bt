package chav1961.bt.comm;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class ComPortSelector extends AbstractSelector {
	
	protected final Semaphore	sema = new Semaphore(0);

	protected ComPortSelector() {
		super(ComPortChannel.cpsp);
	}

	@Override
	protected void implCloseSelector() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected SelectionKey register(final AbstractSelectableChannel ch, final int ops, final Object att) {
		if (!(ch instanceof ComPortChannel)) {
			throw new IllegalArgumentException("Only ComPortChannel is supported by this selector"); 
		}
		else {
			return new ComPortSelectionKey(ch, ops, att);
		}
	}

	@Override
	public Set<SelectionKey> keys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SelectionKey> selectedKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int selectNow() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int select(final long timeout) throws IOException {
		// TODO Auto-generated method stub
		sema.tryAcquire(timeout, null)
		return 0;
	}

	@Override
	public int select() throws IOException {
		return select(Long.MAX_VALUE);
	}

	@Override
	public Selector wakeup() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public static Selector open() {
		return new ComPortSelector();
	}
}
