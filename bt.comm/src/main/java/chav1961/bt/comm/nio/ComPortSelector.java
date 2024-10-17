package chav1961.bt.comm.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import chav1961.purelib.concurrent.LightWeightRWLockerWrapper;
import chav1961.purelib.concurrent.LightWeightRWLockerWrapper.Locker;

public class ComPortSelector extends AbstractSelector {
	
	private final LightWeightRWLockerWrapper	rw = new LightWeightRWLockerWrapper(); 
	protected final Map<AbstractSelectableChannel, KeyDescription[]>	descriptions = new HashMap<>();
	protected final List<ComPortSelectionKey>	keys = new ArrayList<>();
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
		else if (ops <= 0) {
			throw new IllegalArgumentException("Ops ["+ops+"] doesn't contain any operations"); 
		}
		else {
			try(final Locker lock = rw.lock(false)) {
				int		mask = 0;
				
				for (int index = 0; index < 32; index++) {
					if ((ops & (1 << index)) != 0) {
						final KeyDescription	kd = new KeyDescription(ch, 1 << index, att);
						
						if (!descriptions.containsKey(ch)) {
							descriptions.put(ch, new KeyDescription[32]);
						}
						descriptions.get(ch)[index] = kd;
						mask |= (1 << index);
					}
				}
				return new ComPortSelectionKey(ch, mask, att);
			}
		}
	}

	@Override
	public Set<SelectionKey> keys() {
		try(final Locker lock = rw.lock()) {
			
			return Set.of(keys.toArray(new ComPortSelectionKey[keys.size()]));
		}
	}

	@Override
	public Set<SelectionKey> selectedKeys() {
		// TODO Auto-generated method stub
		try(final Locker lock = rw.lock()) {
			return null;
		}
	}

	@Override
	public int selectNow() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int select(final long timeout) throws IOException {
		// TODO Auto-generated method stub
		try(final Locker lock = rw.lock()) {
			
			sema.tryAcquire(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	private static class KeyDescription {
		private final AbstractSelectableChannel	ch;
		private final int		op;
		private final Object	attachment;
		
		public KeyDescription(final AbstractSelectableChannel ch, final int op, final Object attachment) {
			this.ch = ch;
			this.op = op;
			this.attachment = attachment;
		}
	}
}
