package chav1961.bt.openclmatrix.large;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import chav1961.bt.openclmatrix.internal.GPUExecutor;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUScheduler;
import chav1961.bt.openclmatrix.internal.InternalUtils;
import chav1961.purelib.matrix.AbstractMatrix;
import chav1961.purelib.matrix.interfaces.Matrix;

abstract class LargeMatrix extends AbstractMatrix {
	static final int			PARALLEL_FACTOR = 16;
	static final long			S_1GIGABYTE = 1024 * 1024* 1024; 
	static final long			S_16MEGABYTE = 16 * 1024* 1024; 

	private static final long	INIT_BUFFER_SIZE = PARALLEL_FACTOR * 1024 * 1024; 
	private static final String	RAF_READ_WRITE_ACCESS_MODE = "rwd";
	private static final String	RAF_READ_ONLY_ACCESS_MODE = "r";

	private final GPUExecutor	executor;
	private final long			totalSize;
	private final File			largeKeeper;
	private final byte[]		byteBuffer = new byte[16];
	private GPUScheduler		sched = null;
	
	LargeMatrix(final GPUExecutor executor, final File contentDir, final Type type, final int rows, final int cols) {
		super(type, rows, cols);
		if (executor == null) {
			throw new NullPointerException("GPU executor can't be null");
		}
		else if (contentDir == null) {
			throw new NullPointerException("Content directory can't be null");
		}
		else if (!contentDir.exists() || !contentDir.isDirectory() || !contentDir.canWrite()) {
			throw new IllegalArgumentException("Content directory ["+contentDir.getAbsolutePath()+"] not existst, not a directory or can't be written for you");
		}
		else {
			File	tempFile = null;
			
			this.executor = executor;
			this.totalSize = 1L * getType().getItemSize() * getType().getNumberOfItems() * rows * cols;

			try{
				tempFile = File.createTempFile(InternalUtils.OPENCL_PREFIX+getType().getProgramSuffix(), ".matrix", contentDir);
				
				if (tempFile.getFreeSpace() < totalSize) {
					throw new IllegalArgumentException("No space available in the directory ["+tempFile.getAbsolutePath()+"], required="+(totalSize/S_1GIGABYTE)+"GByte, available="+(tempFile.getFreeSpace()/S_1GIGABYTE)+"GByte");
				}
				else {
					final int		bufferSize = (int) INIT_BUFFER_SIZE;
					final byte[]	buffer = new byte[bufferSize];
					
					try(final RandomAccessFile raf = new RandomAccessFile(tempFile, RAF_READ_WRITE_ACCESS_MODE)) {
						for(long address = 0; address < totalSize; address += bufferSize) {
							raf.write(buffer, 0, address + bufferSize > totalSize ? (int)(totalSize - address) : bufferSize);
						}
					}
					this.largeKeeper = tempFile;
				}
			} catch (IOException e) {
				if (tempFile != null) {
					tempFile.delete();
				}
				throw new IllegalArgumentException(e.getLocalizedMessage(), e);
			}
		}
	}

	LargeMatrix(final GPUExecutor executor, final File contentDir, final Type type, final int rows, final int cols, final File fill, final boolean copyFileContent) {
		super(type, rows, cols);
		if (executor == null) {
			throw new NullPointerException("GPU executor can't be null");
		}
		else if (contentDir == null) {
			throw new NullPointerException("Content directory can't be null");
		}
		else if (!contentDir.exists() || !contentDir.isDirectory() || !contentDir.canWrite()) {
			throw new IllegalArgumentException("Content directory ["+contentDir.getAbsolutePath()+"] not existst, not a directory or can't be written for you");
		}
		else if (fill == null) {
			throw new NullPointerException("Fill file can't be null");
		}
		else if (!fill.exists() || !fill.isFile() || !fill.canRead()) {
			throw new IllegalArgumentException("File to fill ["+fill.getAbsolutePath()+"] not existst, not a file or can't be read for you");
		}
		else {
			File	tempFile = null;
			
			this.executor = executor;
			this.totalSize = 1L * getType().getItemSize() * getType().getNumberOfItems() * rows * cols;

			try{
				tempFile = File.createTempFile(InternalUtils.OPENCL_PREFIX+getType().getProgramSuffix(), ".matrix", contentDir);
				
				if (tempFile.getFreeSpace() < totalSize) {
					throw new IllegalArgumentException("No space available in the directory ["+tempFile.getAbsolutePath()+"], required="+(totalSize/S_1GIGABYTE)+"GByte, available="+(tempFile.getFreeSpace()/S_1GIGABYTE)+"GByte");
				}
				else if (copyFileContent) {
					final int		bufferSize = (int) INIT_BUFFER_SIZE;
					final byte[]	buffer = new byte[bufferSize];
					
					try(final RandomAccessFile rafFrom = new RandomAccessFile(fill, RAF_READ_ONLY_ACCESS_MODE);
						final RandomAccessFile raf = new RandomAccessFile(tempFile, RAF_READ_WRITE_ACCESS_MODE)) {
						int	len;
						
						while ((len = rafFrom.read(buffer)) > 0) {
							raf.write(buffer, 0, len);
						}
					}
				}
				else {
					tempFile.delete();
					fill.renameTo(tempFile);
				}
				this.largeKeeper = tempFile;
			} catch (IOException e) {
				if (tempFile != null) {
					tempFile.delete();
				}
				throw new IllegalArgumentException(e.getLocalizedMessage(), e);
			}
		}
	}
	
	@Override public abstract Object clone() throws CloneNotSupportedException;
	
	@Override
	public void close() throws RuntimeException {
		this.largeKeeper.delete();
	}
	
	@Override
	public String toHumanReadableString() {
		return "Matrix content is too long to use this method. Use toHumanReadableString(PrintStream) instead";
	}

	@Override
	public void toHumanReadableString(final PrintStream ps) {
		if (ps == null) {
			throw new NullPointerException("Print stream can't be null");
		}
		else {
			try(final RandomAccessFile raf = new RandomAccessFile(largeKeeper, "rw")) {
				
				ps.println(">>> Matrix: type="+getType()+", size="+numberOfRows()+"x"+numberOfColumns());
				scanContent(raf, Piece.of(0, 0, numberOfRows(), numberOfColumns()), new ProcessRAFContent() {
					int oldRow = -1;
					
					@Override
					public boolean process(int row, int col, RandomAccessFile raf) throws IOException {
						final double	real = raf.readDouble(), image = raf.readDouble();
						
						if (oldRow != row) {
							oldRow = row;
							ps.println("");
						}
						if (real == 0 && image == 0) {
							ps.print("0, ");
						}
						else if (image == 0) {
							ps.print(real);
							ps.print(", ");
						}
						else if (real == 0) {
							ps.print(image);
							ps.print("i, ");
						}
						else if (image < 0) {
							ps.print(real);
							ps.print(image);
							ps.print("i, ");
						}
						else {
							ps.print(real);
							ps.print('+');
							ps.print(image);
							ps.print("i, ");
						}
						return true;
					}
				});
				ps.println();
				ps.println(">>> end matrix");
			} catch (IOException exc) {
				ps.println(exc.getLocalizedMessage());
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + numberOfColumns();
		result = prime * result + numberOfRows();
		result = prime * result + (int) (totalSize ^ (totalSize >>> 32));
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof LargeMatrix)) return false;
		LargeMatrix other = (LargeMatrix) obj;
		if (numberOfColumns() != other.numberOfColumns()) return false;
		if (numberOfRows() != other.numberOfRows()) return false;
		if (totalSize != other.totalSize) return false;
		return deepEquals(other);
	}
	
	public GPUExecutor getExecutor() {
		return executor;
	}

	public GPUScheduler getScheduler() {
		return sched;
	}
	
	File getFileKeeper() {
		return largeKeeper;
	}

	@Override
	protected void beginTransaction() {
		if (areAllAsyncCompleted()) {
			super.beginTransaction();
			this.sched = getExecutor().startTransaction();
		}
	}
	
	@Override
	protected void completeTransaction() {
		if (!areAllAsyncCompleted()) {
			super.completeTransaction();
			this.sched.close();
			this.sched = null;
		}
	}

	protected void replaceFileKeeper(final File toReplace) {
		
	}
	
	@FunctionalInterface
	static interface ProcessRAFContent {
		boolean process(int row, int col, RandomAccessFile raf) throws IOException;
	}

	@FunctionalInterface
	static interface ProcessFCContent {
		boolean process(int row, int col, ByteBuffer content) throws IOException;
	}
	
	@FunctionalInterface
	static interface ProcessFCContentPair {
		boolean process(int row, int col, ByteBuffer left, ByteBuffer right) throws IOException;
	}
	
	void serialize(final int[] content, final int length, final ByteBuffer target) {
		final byte[]	buf = byteBuffer;
		
		for(int index = 0; index < length; index++) {
			InternalUtils.fromInt(buf, 4*index, content[index]);
		}
		target.put(buf, 0, 4 * length);
	}

	void deserialize(final ByteBuffer source, final int[] content, final int length) {
		final byte[]	buf = byteBuffer;
		
		try {
		source.get(buf, 0, 4 * length);
		for(int index = 0; index < length; index++) {
			content[index] = InternalUtils.toInt(buf, 4 * index);
		}
		} catch (BufferUnderflowException exc) {
			int x = 10;
		}
	}

	void serialize(final long[] content, final int length, final ByteBuffer target) {
		final byte[]	buf = byteBuffer;
		
		for(int index = 0; index < length; index++) {
			InternalUtils.fromLong(buf, 8*index, content[index]);
		}
		target.put(buf, 0, 8 * length);
	}

	void deserialize(final ByteBuffer source, final long[] content, final int length) {
		final byte[]	buf = byteBuffer;
		
		source.get(buf, 0, 8 * length);
		for(int index = 0; index < length; index++) {
			content[index] = InternalUtils.toLong(buf, 8 * index);
		}
	}

	void extractAny(final Piece piece, final ProcessFCContent callback) {
		try(final FileChannel	in = FileChannel.open(getFileKeeper().toPath(), StandardOpenOption.READ)) {
			scanContentReadOnly(in, piece, callback, PARALLEL_FACTOR);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("IO error processing request: "+e.getLocalizedMessage(), e);
		}
	}

	void assignAny(final Piece piece, final ProcessFCContent callback) {
		try(final FileChannel	in = FileChannel.open(getFileKeeper().toPath(), StandardOpenOption.WRITE)) {
			scanContentWriteOnly(in, piece, callback, PARALLEL_FACTOR);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("IO error processing request: "+e.getLocalizedMessage(), e);
		}
	}

	void applyAny(final Piece piece, final ProcessFCContent callback) {
		try(final FileChannel	in = FileChannel.open(getFileKeeper().toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)) {
			scanContentReadWrite(in, piece, callback, PARALLEL_FACTOR);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("IO error processing request: "+e.getLocalizedMessage(), e);
		}
	}
	
	boolean scanContent(final RandomAccessFile raf, final Piece piece, final ProcessRAFContent callback) throws IOException {
		final long 	cols = numberOfColumns();
		final int	size = getType().getNumberOfItems() * getType().getItemSize(); 
		
		for(int y = piece.getTop(), maxY = piece.getTop() + piece.getHeight(); y < maxY; y++) {
			final long	address = 1L * y * cols * size;
			
			raf.seek(address);			
			for(int x = piece.getLeft(), maxX = piece.getLeft() + piece.getWidth(); x < maxX; x++) {
				if (!callback.process(y, x, raf)) {
					return false;
				}
			}
		}
		return true;
	}
	
	boolean scanContentReadOnly(final FileChannel fc, final Piece piece, final ProcessFCContent callback, final int parallels) throws IOException {
		if (getType() != Type.COMPLEX_FLOAT) {
			int x = 10;
		}
		
		final int	size = getType().getNumberOfItems() * getType().getItemSize();
		final int	bufferSwitches = (int) (S_16MEGABYTE / (numberOfColumns() * size));
		final int	bufferSize = bufferSwitches * numberOfColumns() * size;
		long		displ = 0;
		ByteBuffer	bb = null;

		System.err.println("Type: "+getType()+", noi="+getType().getNumberOfItems()+", is="+getType().getItemSize());
		System.err.println("Switches: "+bufferSwitches+", size="+bufferSize+", fc.size="+fc.size()+", item.size="+size);
		
		for(int y = piece.getTop(), index = 0, maxY = piece.getTop() + piece.getHeight(); y < maxY; y++, index++) {
			if (index % bufferSwitches == 0) {
				bb = fc.map(MapMode.READ_ONLY, displ, Math.min(bufferSize, fc.size() - displ));
				System.err.println("Allocate: "+displ+", size="+Math.min(bufferSize, fc.size() - displ));
				displ += bufferSize;
			}
			bb.position(((index % bufferSwitches) * numberOfColumns() + piece.getLeft()) * size);
			System.err.println("Pos="+(((index % bufferSwitches) * numberOfColumns() + piece.getLeft()) * size)+", size="+bb.capacity()+", y="+y+", maxY="+maxY);
			for(int x = piece.getLeft(), maxX = piece.getLeft() + piece.getWidth(); x < maxX; x++) {
				if (!callback.process(y, x, bb)) {
					return false;
				}
			}
		}
//		final ByteBuffer	bb = ByteBuffer.allocateDirect((int)(cols * size));
//		
//		for(int y = piece.getTop(), maxY = piece.getTop() + piece.getHeight(); y < maxY; y++) {
//			bb.clear();
//			fc.read(bb, 1L * (y * numberOfColumns() + piece.getLeft()) * size);
//			bb.rewind();
//			for(int x = piece.getLeft(), maxX = piece.getLeft() + piece.getWidth(); x < maxX; x++) {
//				if (!callback.process(y, x, bb)) {
//					return false;
//				}
//			}
//		}
		return true;
	}
	
	boolean scanContentReadOnly2(final FileChannel left, final FileChannel right, final Piece piece, final ProcessFCContentPair callback) throws IOException {
		final long 			cols = piece.getWidth();
		final int			size = getType().getNumberOfItems() * getType().getItemSize();
		final ByteBuffer	bbLeft = ByteBuffer.allocateDirect((int)(cols * size));
		final ByteBuffer	bbRight = ByteBuffer.allocate((int)(cols * size));
		
		for(int y = piece.getTop(), maxY = piece.getTop() + piece.getHeight(); y < maxY; y++) {
			bbLeft.clear();
			bbRight.clear();
			left.read(bbLeft, 1L * (y * numberOfColumns() + piece.getLeft()) * size);
			right.read(bbRight, 1L * (y * numberOfColumns() + piece.getLeft()) * size);
			bbLeft.rewind();
			bbRight.rewind();
			for(int x = piece.getLeft(), maxX = piece.getLeft() + piece.getWidth(); x < maxX; x++) {
				if (!callback.process(y, x, bbLeft, bbRight)) {
					return false;
				}
			}
		}
		return true;
	}

	boolean scanContentWriteOnly(final FileChannel fc, final Piece piece, final ProcessFCContent callback, final int parallels) throws IOException {
		final long 			cols = piece.getWidth();
		final int			size = getType().getNumberOfItems() * getType().getItemSize();
		final ByteBuffer	bb = ByteBuffer.allocateDirect((int)(cols * size));
		
		for(int y = piece.getTop(), maxY = piece.getTop() + piece.getHeight(); y < maxY; y++) {
			bb.clear();
			
			fc.position(1L * (y * numberOfColumns() + piece.getLeft()) * size);
			for(int x = piece.getLeft(), maxX = piece.getLeft() + piece.getWidth(); x < maxX; x++) {
				try{
					if (!callback.process(y, x, bb)) {
						bb.flip();
						while(bb.hasRemaining()) {
							fc.write(bb);
						}			
						return false;
					}
				} catch (EOFException exc) {
					bb.flip();
					while(bb.hasRemaining()) {
						fc.write(bb);
					}			
					return false;
				}
			}
			bb.flip();
			while(bb.hasRemaining()) {
				fc.write(bb);
			}
		}
		return true;
	}
	
	boolean scanContentReadWrite(final FileChannel fc, final Piece piece, final ProcessFCContent callback, final int parallels) throws IOException {
		final long 			cols = piece.getWidth();
		final int			size = getType().getNumberOfItems() * getType().getItemSize();
		final ByteBuffer	bb = ByteBuffer.allocateDirect((int)(cols * size));
		
		for(int y = piece.getTop(), maxY = piece.getTop() + piece.getHeight(); y < maxY; y++) {
			final long	address = 1L * (y * numberOfColumns() + piece.getLeft()) * size;
			
			bb.clear();
			fc.position(address);
			fc.read(bb);
			bb.rewind();
			for(int x = piece.getLeft(), maxX = piece.getLeft() + piece.getWidth(); x < maxX; x++) {
				if (!callback.process(y, x, bb)) {
					bb.flip();
					fc.position(address);
					while(bb.hasRemaining()) {
						fc.write(bb);
					}			
					return false;
				}
			}
			bb.flip();
			fc.position(address);
			while(bb.hasRemaining()) {
				fc.write(bb);
			}
		}
		return true;
	}
}
