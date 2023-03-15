package chav1961.bt.comm;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

class ComPortSelectionKey extends SelectionKey {
	private final SelectableChannel channel;
	private final int				ops;
	private final Object			attachment;
	
	ComPortSelectionKey(final SelectableChannel channel, final int ops, final Object attachment) {
		this.channel = channel;
		this.ops = ops;
		this.attachment = attachment;
	}
	
	@Override
	public SelectableChannel channel() {
		return channel;
	}

	@Override
	public Selector selector() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int interestOps() {
		return ops;
	}

	@Override
	public SelectionKey interestOps(int ops) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int readyOps() {
		// TODO Auto-generated method stub
		return 0;
	}
}
