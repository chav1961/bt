package chav1961.bt.openclmatrix.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_event;
import org.jocl.cl_mem;

import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUEvent;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryBuffer;
import chav1961.bt.openclmatrix.spi.OpenCLDescriptor.OpenCLContext;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.Piece;
import chav1961.purelib.matrix.interfaces.Matrix.Type;

class GPUBufferImpl implements GPUBuffer {
	private static final int			ADDRESS_STEP = 1024 * 1024;
	
	private final GPUSchedulerImpl		owner;
	private final Consumer<GPUBuffer>	onCloseCallback;
	private final cl_mem 				buffer;
	private final int					size;
	
	GPUBufferImpl(final GPUSchedulerImpl owner, final int size, final Consumer<GPUBuffer> onCloseCallback) {
		this.owner = owner;
		this.onCloseCallback = onCloseCallback;
		this.buffer = CL.clCreateBuffer(owner.owner.context, CL.CL_MEM_READ_ONLY, size * Sizeof.cl_char, null, null);
		this.size = size;
	}

	@Override
	public void close() throws RuntimeException {
		onCloseCallback.accept(this);
		CL.clReleaseMemObject(buffer);
	}

	@Override
	public GPUEvent download(final DataInput in, final Type type) throws IOException {
		if (in == null) {
			throw new NullPointerException("Data input can't be null");
		}
		else if (type == null) {
			throw new NullPointerException("Content type can't be null");
		}
		else {
			final GPUEvent	event = owner.createEvent();

			final Thread	t = new Thread(()->{
				try {
					final byte[]	buf = new byte[ADDRESS_STEP];
					
					for (int piece = 0; piece < size; piece += ADDRESS_STEP) {
						final int	bufSize = read(buf, buf.length, in, type);
						
						CL.clEnqueueWriteBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, bufSize * Sizeof.cl_char, Pointer.to(buf), 0, null, null);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					event.post();
				}
			});
			t.setDaemon(true);
			t.start();
			return event;
		}
	}

	@Override
	public GPUEvent download(final Piece piece, final Matrix matrix) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (matrix == null) {
			throw new NullPointerException("Matrix can't be null");
		}
		else {
			final GPUEvent	event = owner.createEvent();
			
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE	:
				case REAL_DOUBLE	:
					final double[]	dContent = new double[piece.getWidth() * matrix.getType().getNumberOfItems()];

					final Thread	tDouble = new Thread(()->{
													for(int index = piece.getTop(), maxIndex = piece.getTop()+piece.getHeight(); index < maxIndex; index++) {
														matrix.extractDoubles(Piece.of(index, piece.getLeft(), 1, piece.getWidth()));
														CL.clEnqueueWriteBuffer(owner.owner.queue, buffer, CL.CL_TRUE, index * piece.getWidth(), dContent.length * Sizeof.cl_double, Pointer.to(dContent), 0, null, null);
													}
													event.post();
												});
					tDouble.setDaemon(true);
					tDouble.start();
					break;
				case COMPLEX_FLOAT	:
				case REAL_FLOAT		:
					final float[]	fContent = new float[piece.getWidth() * matrix.getType().getNumberOfItems()];

					final Thread	tFloat = new Thread(()->{
													for(int index = piece.getTop(), maxIndex = piece.getTop()+piece.getHeight(); index < maxIndex; index++) {
														matrix.assign(Piece.of(index, piece.getLeft(), 1, piece.getWidth()));
														CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, index * piece.getWidth(), fContent.length * Sizeof.cl_float, Pointer.to(fContent), 0, null, null);
													}
													event.post();
												});
					tFloat.setDaemon(true);
					tFloat.start();
					break;
				case REAL_INT		:
					final int[]		iContent = new int[piece.getWidth() * matrix.getType().getNumberOfItems()];

					final Thread	tInt = new Thread(()->{
													for(int index = piece.getTop(), maxIndex = piece.getTop()+piece.getHeight(); index < maxIndex; index++) {
														CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, index * piece.getWidth(), iContent.length * Sizeof.cl_int, Pointer.to(iContent), 0, null, null);
														matrix.assign(Piece.of(index, piece.getLeft(), 1, piece.getWidth()));
													}
													event.post();
												});
					tInt.setDaemon(true);
					tInt.start();
					break;
				case REAL_LONG		:
					final long[]	lContent = new long[piece.getWidth() * matrix.getType().getNumberOfItems()];

					final Thread	tLong = new Thread(()->{
													for(int index = piece.getTop(), maxIndex = piece.getTop()+piece.getHeight(); index < maxIndex; index++) {
														CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, index * piece.getWidth(), lContent.length * Sizeof.cl_long, Pointer.to(lContent), 0, null, null);
														matrix.assign(Piece.of(index, piece.getLeft(), 1, piece.getWidth()));
													}
													event.post();
												});
					tLong.setDaemon(true);
					tLong.start();
					break;
				default :
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
			return event;
		}
	}

	@Override
	public GPUEvent download(final TemporaryBuffer in) throws IOException {
		if (in == null) {
			throw new NullPointerException("Buffer can't be null");
		}
		else {
			final GPUEvent	event = owner.createEvent();

			final Thread	t = new Thread(()->{
				try {
					final byte[]	buf = new byte[ADDRESS_STEP];
					
					for (int piece = 0; piece < size; piece += ADDRESS_STEP) {
						final int	bufSize = Math.min(ADDRESS_STEP, in.getSize()-piece);
						
						in.read(buf, 0, bufSize);
						CL.clEnqueueWriteBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, bufSize * Sizeof.cl_char, Pointer.to(buf), 0, null, null);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					event.post();
				}
			});
			t.setDaemon(true);
			t.start();
			return event;
		}
	}

	@Override
	public GPUEvent upload(final DataOutput out, final Type type) throws IOException {
		if (out == null) {
			throw new NullPointerException("Data output can't be null");
		}
		else if (type == null) {
			throw new NullPointerException("Content type can't be null");
		}
		else {
			final GPUEvent	event = owner.createEvent();

			final Thread	t = new Thread(()->{
				try {
					final byte[]	buf = new byte[ADDRESS_STEP];
					
					for (int piece = 0; piece < size; piece += ADDRESS_STEP) {
						final int	bufSize = Math.min(ADDRESS_STEP, size-piece);
						
						CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, buf.length * Sizeof.cl_char, Pointer.to(buf), 0, null, null);
						write(buf, 0, bufSize, out, type);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					event.post();
				}
			});
			t.setDaemon(true);
			t.start();
			return event;
		}
	}

	@Override
	public GPUEvent upload(final Piece piece, final Matrix matrix) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (matrix == null) {
			throw new NullPointerException("Matrix can't be null");
		}
		else {
			final GPUEvent	event = owner.createEvent();
			
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE	:
				case REAL_DOUBLE	:
					final double[]	dContent = new double[piece.getWidth() * matrix.getType().getNumberOfItems()];

					final Thread	tDouble = new Thread(()->{
													for(int index = piece.getTop(), maxIndex = piece.getTop()+piece.getHeight(); index < maxIndex; index++) {
														CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, index * piece.getWidth(), dContent.length * Sizeof.cl_double, Pointer.to(dContent), 0, null, null);
														matrix.assign(Piece.of(index, piece.getLeft(), 1, piece.getWidth()));
													}
													event.post();
												});
					tDouble.setDaemon(true);
					tDouble.start();
					break;
				case COMPLEX_FLOAT	:
				case REAL_FLOAT		:
					final float[]	fContent = new float[piece.getWidth() * matrix.getType().getNumberOfItems()];

					final Thread	tFloat = new Thread(()->{
													for(int index = piece.getTop(), maxIndex = piece.getTop()+piece.getHeight(); index < maxIndex; index++) {
														CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, index * piece.getWidth(), fContent.length * Sizeof.cl_float, Pointer.to(fContent), 0, null, null);
														matrix.assign(Piece.of(index, piece.getLeft(), 1, piece.getWidth()));
													}
													event.post();
												});
					tFloat.setDaemon(true);
					tFloat.start();
					break;
				case REAL_INT		:
					final int[]		iContent = new int[piece.getWidth() * matrix.getType().getNumberOfItems()];

					final Thread	tInt = new Thread(()->{
													for(int index = piece.getTop(), maxIndex = piece.getTop()+piece.getHeight(); index < maxIndex; index++) {
														CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, index * piece.getWidth(), iContent.length * Sizeof.cl_int, Pointer.to(iContent), 0, null, null);
														matrix.assign(Piece.of(index, piece.getLeft(), 1, piece.getWidth()));
													}
													event.post();
												});
					tInt.setDaemon(true);
					tInt.start();
					break;
				case REAL_LONG		:
					final long[]	lContent = new long[piece.getWidth() * matrix.getType().getNumberOfItems()];

					final Thread	tLong = new Thread(()->{
													for(int index = piece.getTop(), maxIndex = piece.getTop()+piece.getHeight(); index < maxIndex; index++) {
														CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, index * piece.getWidth(), lContent.length * Sizeof.cl_long, Pointer.to(lContent), 0, null, null);
														matrix.assign(Piece.of(index, piece.getLeft(), 1, piece.getWidth()));
													}
													event.post();
												});
					tLong.setDaemon(true);
					tLong.start();
					break;
				default :
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
			return event;
		}
	}

	@Override
	public GPUEvent upload(final TemporaryBuffer out) throws IOException {
		if (buffer == null) {
			throw new NullPointerException("Buffer can't be null");
		}
		else {
			final GPUEvent	event = owner.createEvent();
	
			final Thread	t = new Thread(()->{
				try {
					final byte[]	buf = new byte[ADDRESS_STEP];
					
					for (int piece = 0; piece < size; piece += ADDRESS_STEP) {
						final int	bufSize = Math.min(ADDRESS_STEP, size-piece);
						
						CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, buf.length * Sizeof.cl_char, Pointer.to(buf), 0, null, null);
						out.write(buf, 0, bufSize);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					event.post();
				}
			});
			t.setDaemon(true);
			t.start();
			return event;
		}
	}

	private void write(final byte[] buf, final int bufStart, final int bufSize, final DataOutput out, final Type type) throws IOException {
		switch (type) {
			case COMPLEX_DOUBLE	:
			case REAL_DOUBLE	:
				for(int index = bufStart; index < bufSize; index += type.getItemSize()) {
					out.writeDouble(Double.longBitsToDouble(toLong(buf, index)));
				}
				break;
			case COMPLEX_FLOAT	:
			case REAL_FLOAT		:
				for(int index = bufStart; index < bufSize; index += type.getItemSize()) {
					out.writeFloat(Float.intBitsToFloat(toInt(buf, index)));
				}
				break;
			case REAL_INT		:
				for(int index = bufStart; index < bufSize; index += type.getItemSize()) {
					out.writeInt((toInt(buf, index)));
				}
				break;
			case REAL_LONG		:
				for(int index = bufStart; index < bufSize; index += type.getItemSize()) {
					out.writeLong(toLong(buf, index));
				}
				break;
			default:
				throw new UnsupportedOperationException("Matrix type ["+type+"] is not supported yet");
		}
	}
	
	private int toInt(final byte[] buf, final int pos) {
        return ((buf[pos] << 24) + (buf[pos+1] << 16) + (buf[pos+2] << 8) + (buf[pos+3] << 0));
	}

	private long toLong(final byte[] buf, final int pos) {
        return (((long)buf[pos] << 56) +
                ((long)(buf[pos+1] & 255) << 48) +
                ((long)(buf[pos+2] & 255) << 40) +
                ((long)(buf[pos+3] & 255) << 32) +
                ((long)(buf[pos+4] & 255) << 24) +
                ((buf[pos+5] & 255) << 16) +
                ((buf[pos+6] & 255) <<  8) +
                ((buf[pos+7] & 255) <<  0));
	}

	private int read(final byte[] buf, int bufSize, DataInput in, Type type) throws IOException {
		int index = 0;
		
		try {
			switch (type) {
				case COMPLEX_DOUBLE	:
				case REAL_DOUBLE	:
					for(index = 0; index < bufSize; index += type.getItemSize()) {
						fromLong(buf, index, Double.doubleToLongBits(in.readDouble()));
					}
					break;
				case COMPLEX_FLOAT	:
				case REAL_FLOAT		:
					for(index = 0; index < bufSize; index += type.getItemSize()) {
						fromInt(buf, index, Float.floatToIntBits(in.readFloat()));
					}
					break;
				case REAL_INT		:
					for(index = 0; index < bufSize; index += type.getItemSize()) {
						fromInt(buf, index, in.readInt());
					}
					break;
				case REAL_LONG		:
					for(index = 0; index < bufSize; index += type.getItemSize()) {
						fromLong(buf, index, in.readLong());
					}
					break;
				default:
					throw new UnsupportedOperationException("Matrix type ["+type+"] is not supported yet");
			}
		} catch (EOFException exc) {
		}
		return index;
	}
	
	private void fromLong(final byte[] buffer, final int from, final long value) {
        buffer[from] = (byte)(value >>> 56);
        buffer[from+1] = (byte)(value >>> 48);
        buffer[from+2] = (byte)(value >>> 40);
        buffer[from+3] = (byte)(value >>> 32);
        buffer[from+4] = (byte)(value >>> 24);
        buffer[from+5] = (byte)(value >>> 16);
        buffer[from+6] = (byte)(value >>>  8);
        buffer[from+7] = (byte)(value >>>  0);
	}

	private void fromInt(final byte[] buffer, final int from, final int value) {
        buffer[from] = (byte)(value >>> 24);
        buffer[from+1] = (byte)(value >>> 16);
        buffer[from+2] = (byte)(value >>>  8);
        buffer[from+3] = (byte)(value >>>  0);
	}

}
