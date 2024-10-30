package chav1961.bt.openclmatrix.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.function.Consumer;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;

import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUEvent;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryBuffer;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.Piece;
import chav1961.purelib.matrix.interfaces.Matrix.Type;

class GPUBufferImpl implements GPUBuffer {
	private static final int			ADDRESS_STEP = 1024 * 1024;
	
	final cl_mem 						buffer;
	private final GPUSchedulerImpl		owner;
	private final Consumer<GPUBuffer>	onCloseCallback;
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
	public int getSize() {
		return size;
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
					switch (type) {
						case COMPLEX_DOUBLE	:
						case REAL_DOUBLE	:
							downloadDouble(in);
							break;
						case COMPLEX_FLOAT	:
						case REAL_FLOAT	:
							downloadFloat(in);
							break;
						case REAL_INT	:
							downloadInt(in);
							break;
						case REAL_LONG	:
							downloadLong(in);
							break;
						default:
							throw new UnsupportedOperationException("Matrux type ["+type+"] is not supported yet");
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
	public GPUEvent download(final TemporaryBuffer in, final Matrix.Type type) throws IOException {
		if (in == null) {
			throw new NullPointerException("Buffer can't be null");
		}
		else if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else {
			final GPUEvent	event = owner.createEvent();

			final Thread	t = new Thread(()->{
				try {
					switch (type) {
						case COMPLEX_DOUBLE	:
						case REAL_DOUBLE	:
							downloadDouble(in);
							break;
						case COMPLEX_FLOAT	:
						case REAL_FLOAT	:
							downloadFloat(in);
							break;
						case REAL_INT	:
							downloadInt(in);
							break;
						case REAL_LONG	:
							downloadLong(in);
							break;
						default:
							throw new UnsupportedOperationException("Matrix type ["+type+"] is not supported yet");
					
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
					switch (type) {
						case COMPLEX_DOUBLE	:
						case REAL_DOUBLE	:
							uploadDouble(out);
							break;
						case COMPLEX_FLOAT	:
						case REAL_FLOAT		:
							uploadFloat(out);
							break;
						case REAL_INT	:
							uploadInt(out);
							break;
						case REAL_LONG	:
							uploadLong(out);
							break;
						default:
							throw new UnsupportedOperationException("Matrix type ["+type+"] is not supported yet");
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
	public GPUEvent upload(final TemporaryBuffer out, final Matrix.Type type) throws IOException {
		if (out == null) {
			throw new NullPointerException("Buffer can't be null");
		}
		else if (type == null) {
			throw new NullPointerException("Content type can't be null");
		}
		else {
			final GPUEvent	event = owner.createEvent();
	
			final Thread	t = new Thread(()->{
				try {
					switch (type) {
						case COMPLEX_DOUBLE	:
						case REAL_DOUBLE	:
							uploadDouble(out);
							break;
						case COMPLEX_FLOAT	:
						case REAL_FLOAT		:
							uploadFloat(out);
							break;
						case REAL_INT	:
							uploadInt(out);
							break;
						case REAL_LONG	:
							uploadLong(out);
							break;
						default:
							throw new UnsupportedOperationException("Matrix type ["+type+"] is not supported yet");
					}
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

	private int read(final double[] buf, final int bufSize, final DataInput in) throws IOException {
		int index = 0;
		
		try {
			for(index = 0; index < bufSize; index++) {
				buf[index] = in.readDouble();
			}
		} catch (EOFException exc) {
		}
		return index;
	}	

	private int read(final float[] buf, final int bufSize, final DataInput in) throws IOException {
		int index = 0;
		
		try {
			for(index = 0; index < bufSize; index++) {
				buf[index] = in.readFloat();
			}
		} catch (EOFException exc) {
		}
		return index;
	}	

	private int read(final int[] buf, int bufSize, DataInput in) throws IOException {
		int index = 0;
		
		try {
			for(index = 0; index < bufSize; index++) {
				buf[index] = in.readInt();
			}
		} catch (EOFException exc) {
		}
		return index;
	}	

	private int read(final long[] buf, int bufSize, DataInput in) throws IOException {
		int index = 0;
		
		try {
			for(index = 0; index < bufSize; index++) {
				buf[index] = in.readLong();
			}
		} catch (EOFException exc) {
		}
		return index;
	}	

	private void downloadDouble(final DataInput in) throws IOException {
		final double[]	buf = new double[ADDRESS_STEP];
		
		for (int piece = 0; piece < size; piece += ADDRESS_STEP) {
			final int	bufSize = read(buf, buf.length, in);
			
			CL.clEnqueueWriteBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, bufSize * Sizeof.cl_double, Pointer.to(buf), 0, null, null);
		}
	}

	private void downloadFloat(final DataInput in) throws IOException {
		final float[]	buf = new float[ADDRESS_STEP];
		
		for (int piece = 0; piece < size; piece += ADDRESS_STEP) {
			final int	bufSize = read(buf, buf.length, in);
			
			CL.clEnqueueWriteBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, bufSize * Sizeof.cl_float, Pointer.to(buf), 0, null, null);
		}
	}
	
	private void downloadInt(final DataInput in) throws IOException {
		final int[]	buf = new int[ADDRESS_STEP];
		
		for (int piece = 0; piece < size; piece += ADDRESS_STEP) {
			final int	bufSize = read(buf, buf.length, in);
			
			CL.clEnqueueWriteBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, bufSize * Sizeof.cl_int, Pointer.to(buf), 0, null, null);
		}
	}
	
	private void downloadLong(DataInput in) throws IOException {
		final long[]	buf = new long[ADDRESS_STEP];
		
		for (int piece = 0; piece < size; piece += ADDRESS_STEP) {
			final int	bufSize = read(buf, buf.length, in);
			
			CL.clEnqueueWriteBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, bufSize * Sizeof.cl_long, Pointer.to(buf), 0, null, null);
		}
	}
	

	private void downloadDouble(final TemporaryBuffer in) throws IOException {
		final double[]	buf = new double[ADDRESS_STEP];
		final int		currentSize = size / Sizeof.cl_double;
		
		for (int piece = 0; piece < currentSize; piece += ADDRESS_STEP) {
			final int	bufSize = Math.min(buf.length * Sizeof.cl_double, in.getSize() - piece * Sizeof.cl_double);
			final int 	len = in.read(buf, 0, bufSize / Sizeof.cl_double);

			CL.clEnqueueWriteBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, len * Sizeof.cl_double, Pointer.to(buf), 0, null, null);
		}
	}

	private void downloadFloat(final TemporaryBuffer in) throws IOException {
		final float[]	buf = new float[ADDRESS_STEP];
		final int		currentSize = size / Sizeof.cl_float;
		
		for (int piece = 0; piece < currentSize; piece += ADDRESS_STEP) {
			final int	bufSize = Math.min(buf.length * Sizeof.cl_float, in.getSize() - piece * Sizeof.cl_float);
			final int 	len = in.read(buf, 0, bufSize / Sizeof.cl_float);

			CL.clEnqueueWriteBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, len * Sizeof.cl_float, Pointer.to(buf), 0, null, null);
		}
	}
	
	private void downloadInt(final TemporaryBuffer in) throws IOException {
		final int[]		buf = new int[ADDRESS_STEP];
		final int		currentSize = size / Sizeof.cl_int;
		
		for (int piece = 0; piece < currentSize; piece += ADDRESS_STEP) {
			final int	bufSize = Math.min(buf.length * Sizeof.cl_int, in.getSize() - piece * Sizeof.cl_int);
			final int 	len = in.read(buf, 0, bufSize / Sizeof.cl_int);

			CL.clEnqueueWriteBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, len * Sizeof.cl_int, Pointer.to(buf), 0, null, null);
		}
	}
	
	private void downloadLong(final TemporaryBuffer in) throws IOException {
		final long[]	buf = new long[ADDRESS_STEP];
		final int		currentSize = size / Sizeof.cl_int;
		
		for (int piece = 0; piece < currentSize; piece += ADDRESS_STEP) {
			final int	bufSize = Math.min(buf.length * Sizeof.cl_long, in.getSize() - piece * Sizeof.cl_long);
			final int 	len = in.read(buf, 0, bufSize / Sizeof.cl_long);

			CL.clEnqueueWriteBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, len * Sizeof.cl_long, Pointer.to(buf), 0, null, null);
		}
	}
	
	private void uploadDouble(final DataOutput out) throws IOException {
		final double[]	buf = new double[ADDRESS_STEP];
		final int		currentSize = size / Sizeof.cl_double;
		
		for (int piece = 0; piece < currentSize; piece += ADDRESS_STEP) {
			final int	bufSize = Math.min(ADDRESS_STEP, currentSize-piece);
			
			CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, bufSize * Sizeof.cl_double, Pointer.to(buf), 0, null, null);
			for(int index = 0; index < bufSize; index++) {
				out.writeDouble(buf[index]);
			}
		}
	}

	private void uploadFloat(final DataOutput out) throws IOException {
		final float[]	buf = new float[ADDRESS_STEP];
		final int		currentSize = size / Sizeof.cl_float;
		
		for (int piece = 0; piece < currentSize; piece += ADDRESS_STEP) {
			final int	bufSize = Math.min(ADDRESS_STEP, currentSize-piece);
			
			CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, bufSize * Sizeof.cl_float, Pointer.to(buf), 0, null, null);
			for(int index = 0; index < bufSize; index++) {
				out.writeFloat(buf[index]);
			}
		}
	}
	
	private void uploadInt(final DataOutput out) throws IOException {
		final int[]		buf = new int[ADDRESS_STEP];
		final int		currentSize = size / Sizeof.cl_int;
		
		for (int piece = 0; piece < currentSize; piece += ADDRESS_STEP) {
			final int	bufSize = Math.min(ADDRESS_STEP, currentSize-piece);
			
			CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, bufSize * Sizeof.cl_int, Pointer.to(buf), 0, null, null);
			for(int index = 0; index < bufSize; index++) {
				out.writeInt(buf[index]);
			}
		}
	}
	
	private void uploadLong(final DataOutput out) throws IOException {
		final long[]	buf = new long[ADDRESS_STEP];
		final int		currentSize = size / Sizeof.cl_long;
		
		for (int piece = 0; piece < currentSize; piece += ADDRESS_STEP) {
			final int	bufSize = Math.min(ADDRESS_STEP, currentSize-piece);
			
			CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, bufSize * Sizeof.cl_long, Pointer.to(buf), 0, null, null);
			for(int index = 0; index < bufSize; index++) {
				out.writeLong(buf[index]);
			}
		}
	}

	private void uploadDouble(final TemporaryBuffer out) throws IOException {
		final double[]	buf = new double[ADDRESS_STEP];
		final int		currentSize = size / Sizeof.cl_double;
		
		for (int piece = 0; piece < currentSize; piece += ADDRESS_STEP) {
			final int	bufSize = Math.min(ADDRESS_STEP, currentSize-piece);
			
			CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, bufSize * Sizeof.cl_double, Pointer.to(buf), 0, null, null);
			out.write(buf, 0, bufSize);
		}
	}

	private void uploadFloat(final TemporaryBuffer out) throws IOException {
		final float[]	buf = new float[ADDRESS_STEP];
		final int		currentSize = size / Sizeof.cl_float;
		
		for (int piece = 0; piece < currentSize; piece += ADDRESS_STEP) {
			final int	bufSize = Math.min(ADDRESS_STEP, currentSize-piece);
			
			CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, bufSize * Sizeof.cl_float, Pointer.to(buf), 0, null, null);
			out.write(buf, 0, bufSize);
		}
	}

	private void uploadInt(final TemporaryBuffer out) throws IOException {
		final int[]		buf = new int[ADDRESS_STEP];
		final int		currentSize = size / Sizeof.cl_int;
		
		for (int piece = 0; piece < currentSize; piece += ADDRESS_STEP) {
			final int	bufSize = Math.min(ADDRESS_STEP, currentSize-piece);
			
			CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, bufSize * Sizeof.cl_int, Pointer.to(buf), 0, null, null);
			out.write(buf, 0, bufSize);
		}
	}

	private void uploadLong(final TemporaryBuffer out) throws IOException {
		final long[]	buf = new long[ADDRESS_STEP];
		final int		currentSize = size / Sizeof.cl_long;
		
		for (int piece = 0; piece < currentSize; piece += ADDRESS_STEP) {
			final int	bufSize = Math.min(ADDRESS_STEP, currentSize-piece);
			
			CL.clEnqueueReadBuffer(owner.owner.queue, buffer, CL.CL_TRUE, piece, bufSize * Sizeof.cl_long, Pointer.to(buf), 0, null, null);
			out.write(buf, 0, bufSize);
		}
	}
}
