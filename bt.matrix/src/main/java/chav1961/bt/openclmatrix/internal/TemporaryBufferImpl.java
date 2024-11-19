package chav1961.bt.openclmatrix.internal;


import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.util.function.Consumer;

import org.jocl.Sizeof;

import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryBuffer;
import chav1961.purelib.matrix.AbstractMatrix;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.Piece;

class TemporaryBufferImpl implements TemporaryBuffer {
	private static final int	GROUP_SIZE = 16;
	
	private final MappedByteBuffer	buffer;
	private final Consumer<TemporaryBufferImpl>	onCloseCallback;
	private final long			address;
	private final int			size;
	private final byte[]		smallBuffer = new byte[8];
	private boolean 			closed = false;
	private int					location = 0;
	
	TemporaryBufferImpl(final MappedByteBuffer buffer, final long address, final int size, final Consumer<TemporaryBufferImpl> onCloseCallback) {
		this.buffer = buffer;
		this.onCloseCallback = onCloseCallback;
		this.address = address;
		this.size = size;
	}
	
	@Override
	public void close() throws IOException {
		flush();
		onCloseCallback.accept(this);
		closed = true;
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
	public int read(final byte[] content, final int to, final int len) throws IOException {
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
			final int	currentLen = Math.min(len, buffer.limit()-buffer.position());
			
			buffer.get(content, to, currentLen);
			seek(buffer.position());
			return currentLen;
		}
	}

	@Override
	public int read(final int[] content, final int to, final int len) throws IOException {
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
			final int	currentLen = Math.min(len, (buffer.limit()-buffer.position()) / Sizeof.cl_int);
			
			for(int index = 0; index < currentLen; index++) {
				buffer.get(smallBuffer, 0, Sizeof.cl_int);
				content[index] = InternalUtils.toInt(smallBuffer, 0);
			}
			seek(buffer.position());
			return currentLen;
		}
	}

	@Override
	public int read(final long[] content, final int to, final int len) throws IOException {
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
			final int	currentLen = Math.min(len, (buffer.limit()-buffer.position()) / Sizeof.cl_long);
			
			for(int index = 0; index < currentLen; index++) {
				buffer.get(smallBuffer, 0, Sizeof.cl_long);
				content[index] = InternalUtils.toLong(smallBuffer, 0);
			}
			seek(buffer.position());
			return currentLen;
		}
	}

	@Override
	public int read(final float[] content, final int to, final int len) throws IOException {
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
			final int	currentLen = Math.min(len, (buffer.limit()-buffer.position()) / Sizeof.cl_float);
			
			for(int index = 0; index < currentLen; index++) {
				buffer.get(smallBuffer, 0, Sizeof.cl_float);
				content[index] = Float.intBitsToFloat(InternalUtils.toInt(smallBuffer, 0));
			}
			seek(buffer.position());
			return currentLen;
		}
	}

	@Override
	public int read(final double[] content, final int to, final int len) throws IOException {
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
			final int	currentLen = Math.min(len, (buffer.limit()-buffer.position()) / Sizeof.cl_double);
			
			for(int index = 0; index < currentLen; index++) {
				buffer.get(smallBuffer, 0, Sizeof.cl_double);
				content[index] = Double.longBitsToDouble(InternalUtils.toLong(smallBuffer, 0));
			}
			seek(buffer.position());
			return currentLen;
		}
	}
	
	@Override
	public int write(final byte[] content, final int from, final int len) throws IOException {
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
			final int	currentLen = Math.min(len, buffer.capacity()-buffer.position());
			
			buffer.put(content, from, currentLen);
			seek(Math.min(buffer.position(), getSize()-1));
			return currentLen;
		}
	}

	@Override
	public int write(final int[] content, final int from, final int len) throws IOException {
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
			final int	currentLen = Math.min(len, (buffer.capacity()-buffer.position()) / Sizeof.cl_int);

			for(int index = 0; index < currentLen; index++) {
				InternalUtils.fromInt(smallBuffer, 0, content[index]);
				buffer.put(smallBuffer, 0, Sizeof.cl_int);
			}
			seek(Math.min(buffer.position(), getSize()-1));
			return currentLen;
		}
	}

	@Override
	public int write(final long[] content, final int from, final int len) throws IOException {
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
			final int	currentLen = Math.min(len, (buffer.capacity()-buffer.position()) / Sizeof.cl_long);

			for(int index = 0; index < currentLen; index++) {
				InternalUtils.fromLong(smallBuffer, 0, content[index]);
				buffer.put(smallBuffer, 0, Sizeof.cl_long);
			}
			seek(Math.min(buffer.position(), getSize()-1));
			return currentLen;
		}
	}

	@Override
	public int write(final float[] content, final int from, final int len) throws IOException {
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
			final int	currentLen = Math.min(len, (buffer.capacity()-buffer.position()) / Sizeof.cl_float);

			for(int index = 0; index < currentLen; index++) {
				InternalUtils.fromInt(smallBuffer, 0, Float.floatToIntBits(content[index]));
				buffer.put(smallBuffer, 0, Sizeof.cl_float);
			}
			seek(Math.min(buffer.position(), getSize()-1));
			return currentLen;
		}
	}

	@Override
	public int write(final double[] content, final int from, final int len) throws IOException {
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
			final int	currentLen = Math.min(len, (buffer.capacity()-buffer.position()) / Sizeof.cl_double);

			for(int index = 0; index < currentLen; index++) {
				InternalUtils.fromLong(smallBuffer, 0, Double.doubleToLongBits(content[index]));
				buffer.put(smallBuffer, 0, Sizeof.cl_double);
			}
			seek(Math.min(buffer.position(), getSize()-1));
			return currentLen;
		}
	}

	@Override
	public int write(final TemporaryBuffer content, final int from, final int len) throws IOException {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (from < 0 || from + len > content.getSize()) {
			throw new IllegalArgumentException("From position ["+from+"] less than 0 or from+len position ["+(from+len)+"] out of range 0.."+(content.getSize() - 1));
		}
		else if (closed) {
			throw new IllegalStateException("Attempt to write content on closed map");
		}
		else {
			buffer.position(position());
			final int	currentLen = Math.min(len, (buffer.capacity()-buffer.position()));
			
			buffer.put(position(), buffer, from, currentLen);
			seek(Math.min(buffer.position(), getSize()-1));
			return currentLen;
		}
	}

	@Override
	public int write(final Piece piece, final Matrix content) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (AbstractMatrix.isOverlaps(content, piece)) {
			throw AbstractMatrix.overlapsError(content, piece);
		}
		else {
			final int		lineSize = piece.getWidth() * content.getType().getNumberOfItems();
			final int		bufferSize = GROUP_SIZE * lineSize;
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE	:
				case REAL_DOUBLE	:
					final double[]	dBuffer = new double[bufferSize];
					
					for(int y = piece.getTop(), maxY = piece.getTop() + piece.getHeight(); y < maxY; y+= GROUP_SIZE) {
						final int	size = Math.min(GROUP_SIZE, maxY - y);
						
						content.extractDoubles(Piece.of(y, piece.getLeft(), size, piece.getWidth()), dBuffer);
						write(dBuffer, 0, lineSize * size);
					}
					break;
				case COMPLEX_FLOAT	:
				case REAL_FLOAT		:
					final float[]	fBuffer = new float[bufferSize];
					
					for(int y = piece.getTop(), maxY = piece.getTop() + piece.getHeight(); y < maxY; y+= GROUP_SIZE) {
						final int	size = Math.min(GROUP_SIZE, maxY - y);
						
						content.extractFloats(Piece.of(y, piece.getLeft(), size, piece.getWidth()), fBuffer);
						write(fBuffer, 0, lineSize * size);
					}
					break;
				case REAL_INT		:
					final int[]		iBuffer = new int[bufferSize];
					
					for(int y = piece.getTop(), maxY = piece.getTop() + piece.getHeight(); y < maxY; y+= GROUP_SIZE) {
						final int	size = Math.min(GROUP_SIZE, maxY - y);
						
						content.extractInts(Piece.of(y, piece.getLeft(), size, piece.getWidth()), iBuffer);
						write(iBuffer, 0, lineSize * size);
					}
					break;
				case REAL_LONG		:
					final long[]	lBuffer = new long[bufferSize];
					
					for(int y = piece.getTop(), maxY = piece.getTop() + piece.getHeight(); y < maxY; y+= GROUP_SIZE) {
						final int	size = Math.min(GROUP_SIZE, maxY - y);
						
						content.extractLongs(Piece.of(y, piece.getLeft(), size, piece.getWidth()), lBuffer);
						write(lBuffer, 0, lineSize * size);
					}
					break;
				case BIT			:
				default:
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
			return 0;
		}
	}
	
	
	@Override
	public void flush() throws IOException {
		buffer.force();
	}
	
	@Override
	public String toString() {
		return "TemporaryBufferImpl [address=" + address + ", size=" + size + ", location=" + location + "]";
	}
}
