package chav1961.bt.openclmatrix.internal;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.util.function.Consumer;

import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryBuffer;

class TemporaryBufferImpl implements TemporaryBuffer {
	private final MappedByteBuffer	buffer;
	private final Consumer<TemporaryBufferImpl>	onCloseCallback;
	private final long		address;
	private final int		size;
	private boolean 		closed = false;
	private int				location = 0;
	
	TemporaryBufferImpl(final MappedByteBuffer buffer, final long address, final int size, final Consumer<TemporaryBufferImpl> onCloseCallback) {
		this.buffer = buffer;
		this.onCloseCallback = onCloseCallback;
		this.address = address;
		this.size = size;
	}
	
	@Override
	public void close() throws IOException {
		onCloseCallback.accept(this);
		closed = true;
		try {
		    final Method 	cleanerMethod = buffer.getClass().getMethod("cleaner");
		    
		    cleanerMethod.setAccessible(true);
		    final Object 	cleaner = cleanerMethod.invoke(buffer);
		    final Method 	cleanMethod = cleaner.getClass().getMethod("clean");
		    
		    cleanMethod.setAccessible(true);
		    cleanMethod.invoke(cleaner);
		} catch(Exception ex) {
		}		
	}

	@Override
	public long getAddress() {
		return address;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public int position() {
		return location;
	}

	@Override
	public int seek(final int newPos) {
		if (newPos < 0 || newPos >= getSize()) {
			throw new IllegalArgumentException("Position ["+newPos+"] out of range 0.."+(size-1));
		}
		else {
			final int	oldPos = this.location;
			
			this.location = newPos;
			return oldPos;
		}
	}
	
	@Override
	public void read(final byte[] content, final int to, final int len) throws IOException {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (to < 0 || to + len > content.length) {
			throw new IllegalArgumentException("To position ["+to+"] less than 0 or to+len position ["+(to+len)+"] out of range 0.."+(content.length - 1));
		}
		else if (closed) {
			throw new IllegalStateException("Attempt to read content on closed map");
		}
		else {
			buffer.position(position());
			buffer.get(content, to, len);
		}
	}

	@Override
	public void write(byte[] content, int from, int len) throws IOException {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (from < 0 || from + len > content.length) {
			throw new IllegalArgumentException("From position ["+from+"] less than 0 or from+len position ["+(from+len)+"] out of range 0.."+(content.length - 1));
		}
		else if (closed) {
			throw new IllegalStateException("Attempt to write content on closed map");
		}
		else {
			buffer.position(position());
			buffer.put(content, from, len);
		}
	}

	@Override
	public String toString() {
		return "TemporaryBufferImpl [address=" + address + ", size=" + size + ", location=" + location + "]";
	}
}
