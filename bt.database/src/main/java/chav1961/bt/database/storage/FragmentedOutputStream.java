package chav1961.bt.database.storage;

import java.io.IOException;
import java.io.OutputStream;

import chav1961.bt.database.storage.FragmentedInputStream.PieceDescriptor;

public abstract class FragmentedOutputStream extends OutputStream {
	private PieceDescriptor	head = null, tail = null;
	private int				currentDispl = 0, currentSize = 0;
	
	public FragmentedOutputStream() {
		
	}

	public FragmentedOutputStream(final byte[] content) {
		if (content == null) {
			throw new NullPointerException("Content can't be null"); 
		}
		else {
			appendInternal(content, 0, content.length);
		}
	}

	public FragmentedOutputStream(final byte[] content, final int from, final int len) {
		if (content == null) {
			throw new NullPointerException("Content can't be null"); 
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length - 1)); 
		}
		else if (len < 0 || from + len >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] + length ["+len+"] out of range 0.."+(content.length - 1)); 
		}
		else {
			appendInternal(content, 0, content.length);
		}
	}

	protected abstract boolean morePieces() throws IOException;
	
	public FragmentedOutputStream append(final byte[] content) {
		if (content == null) {
			throw new NullPointerException("Content can't be null"); 
		}
		else {
			appendInternal(content, 0, content.length);
			return this;
		}
	}
	
	public FragmentedOutputStream append(final byte[] content, final int from, final int len) {
		if (content == null) {
			throw new NullPointerException("Content can't be null"); 
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length - 1)); 
		}
		else if (len < 0 || from + len >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] + length ["+len+"] out of range 0.."+(content.length - 1)); 
		}
		else {
			appendInternal(content, 0, content.length);
			return this;
		}
	}
	
	@Override
	public void write(final int b) throws IOException {
		if (currentDispl > currentSize + 1 && !getNext()) {
			throw new IOException("Content storage exhausted");
		}
		else {
			head.content[currentDispl++] = (byte)b;
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("Buffer can't be null"); 
		}
		else if (off < 0 || off >= b.length) {
			throw new IllegalArgumentException("Offset position ["+off+"] out of range 0.."+(b.length - 1)); 
		}
		else if (len < 0 || off + len >= b.length) {
			throw new IllegalArgumentException("Offset position ["+off+"] + length ["+len+"] out of range 0.."+(b.length - 1)); 
		}
		else {
			final PieceDescriptor	current = head;
			final int				currentLen = Math.min(currentSize - currentDispl, len);
			
			System.arraycopy(b, off, current.content, currentDispl, len);
			currentDispl += len;
			
			if (currentLen < len) {
				if (!getNext()) {
					throw new IOException("Content storage exhausted");
				}
				else {
					write(b, off + currentLen, len - currentLen);
				}
			}
		}
	}
	
	public int getLastPieceFill() {
		return currentSize - currentDispl;
	}

	private void appendInternal(final byte[] content, final int from, final int len) {
		if (head == null) {
			final PieceDescriptor	desc = new PieceDescriptor(null, content, from, len);
			
			head = tail = desc;
		}
		else {
			final PieceDescriptor	last = tail;
			final PieceDescriptor	desc = new PieceDescriptor(last.next, content, from, len);
			
			tail = desc;
		}
	}

	private boolean getNext() throws IOException {
		final PieceDescriptor	current = head, next = current.next;
		
		if (next != null) {
			current.next = null;
			currentDispl = next.from;
			currentSize = next.from + next.length;			
			head = next;
			return true;
		}
		else if (morePieces()) {
			return getNext();
		}
		else {
			return false;
		}
	}

	
}
