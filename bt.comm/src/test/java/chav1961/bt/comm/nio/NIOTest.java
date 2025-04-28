package chav1961.bt.comm.nio;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class NIOTest {

	@Test
	public void test() throws IOException {
		try(final Selector			sel  = Selector.open();
			final ComPortChannel	cpc = ComPortChannel.open(URI.create("COM1:?baudRate=9600"))) {
			
			cpc.configureBlocking(false);
			cpc.register(sel, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE, cpc);

			for (;;) {
				sel.select();
				final Set<SelectionKey>	events = sel.selectedKeys();
				final Iterator<SelectionKey>	it = events.iterator();
				
				while (it.hasNext()) {
					final SelectionKey	key = it.next();
					
					if (key.isConnectable()) {
						
					}
					else if (key.isReadable()) {
						
					}
					else if (key.isWritable()) {
						
					}
					it.remove();
				}
			}
		}
	}
}
