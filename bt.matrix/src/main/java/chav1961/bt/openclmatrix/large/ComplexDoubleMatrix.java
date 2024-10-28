package chav1961.bt.openclmatrix.large;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import chav1961.bt.openclmatrix.internal.InternalUtils;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.streams.DataInputAdapter;

public class ComplexDoubleMatrix implements Matrix {
	private static final int	PARALLEL_FACTOR = 16;
	private static final long	INIT_BUFFER_SIZE = PARALLEL_FACTOR * 1024 * 1024; 
	private static final long	GIGABYTE = 1024 * 1024* 1024; 
	private static final String	RAF_READ_WRITE_ACCESS_MODE = "rwd";
	private static final String	RAF_READ_ONLY_ACCESS_MODE = "r";
	
	private final int		rows;
	private final int		cols;
	private final long		totalSize;
	private final File		largeKeeper;
	private boolean			transactionMode = false;

	public ComplexDoubleMatrix(final int rows, final int cols) {
		this(InternalUtils.TEMP_DIR_LOCATION, rows, cols);
	}	
	
	public ComplexDoubleMatrix(final File contentDir, final int rows, final int cols) {
		if (contentDir == null) {
			throw new NullPointerException("Content directory can't be null");
		}
		else if (!contentDir.exists() || !contentDir.isDirectory() || !contentDir.canWrite()) {
			throw new IllegalArgumentException("Content directory ["+contentDir.getAbsolutePath()+"] not existst, not a directory or can't be written for you");
		}
		else if (rows <= 0) {
			throw new IllegalArgumentException("NUmber of rows ["+rows+"] must be greater than 0");
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("NUmber of columns ["+cols+"] must be greater than 0");
		}
		else {
			File	tempFile = null; 
			
			this.rows = rows;
			this.cols = cols;
			this.totalSize = 1L * getType().getItemSize() * getType().getNumberOfItems() * rows * cols;
			
			try{
				tempFile = File.createTempFile(InternalUtils.OPENCL_PREFIX+getType().getProgramSuffix(), ".matrix", contentDir);
				
				if (tempFile.getFreeSpace() < totalSize) {
					throw new IllegalArgumentException("No space available in the directory ["+tempFile.getAbsolutePath()+"], required="+(totalSize/GIGABYTE)+"GByte, available="+(tempFile.getFreeSpace()/GIGABYTE)+"GByte");
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

	private ComplexDoubleMatrix(final File contentDir, final int rows, final int cols, final File fill) {
		File	tempFile = null; 
		
		this.rows = rows;
		this.cols = cols;
		this.totalSize = 1L * getType().getItemSize() * getType().getNumberOfItems() * rows * cols;
		
		try{
			tempFile = File.createTempFile(InternalUtils.OPENCL_PREFIX+getType().getProgramSuffix(), ".matrix", contentDir);
			
			if (tempFile.getFreeSpace() < totalSize) {
				throw new IllegalArgumentException("No space available in the directory ["+tempFile.getAbsolutePath()+"], required="+(totalSize/GIGABYTE)+"GByte, available="+(tempFile.getFreeSpace()/GIGABYTE)+"GByte");
			}
			else {
				final int		bufferSize = (int) INIT_BUFFER_SIZE;
				final byte[]	buffer = new byte[bufferSize];
				
				try(final RandomAccessFile	src = new RandomAccessFile(fill, RAF_READ_ONLY_ACCESS_MODE);
					final RandomAccessFile  raf = new RandomAccessFile(tempFile, RAF_READ_WRITE_ACCESS_MODE)) {
					int	len;
					
					while ((len = src.read(buffer)) > 0) {
						raf.write(buffer, 0, len);
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
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		ensureTransactionCompleted();
		return new ComplexDoubleMatrix(largeKeeper.getParentFile(), rows, cols, largeKeeper);
	}
	
	@Override
	public void close() throws RuntimeException {
		largeKeeper.delete();
	}

	@Override
	public Type getType() {
		return Type.COMPLEX_DOUBLE;
	}

	@Override
	public int numberOfRows() {
		return rows;
	}

	@Override
	public int numberOfColumns() {
		return cols;
	}

	@Override
	public boolean deepEquals(final Matrix another) {
		if (another == null) {
			return false;
		}
		else if (another == this) {
			return true;
		}
		else if (another.getType() != getType() || another.numberOfRows() != numberOfRows() || another.numberOfColumns() != numberOfColumns()) {
			return false;
		}
		else if (another.getClass() == this.getClass()) {
			ensureTransactionCompleted();
			try(final FileChannel	left = FileChannel.open(largeKeeper.toPath(), StandardOpenOption.READ);
				final FileChannel	right = FileChannel.open(((ComplexDoubleMatrix)another).largeKeeper.toPath(), StandardOpenOption.READ)) {

				if (left.size() != right.size()) {
					return false;
				}
				else {
					return scanContentReadOnly(left, right, Piece.of(0, 0, numberOfRows(), numberOfColumns()), (y, x, realLeft, imageLeft, realRight, imageRight)->realLeft == realRight && imageLeft == imageRight);
				}
			} catch (IOException e) {
				return false;
			}
		}
		else {
			ensureTransactionCompleted();
			
//			try(final FileChannel	in = FileChannel.open(largeKeeper.toPath(), StandardOpenOption.READ);
//				final FileChannel	out = FileChannel.open(((ComplexDoubleMatrix)another).largeKeeper.toPath(), StandardOpenOption.READ)) {
//
//				return scanContent(in, out, (inBuffer, outBuffer)->Objects.equals(inBuffer, outBuffer), true);
//			} catch (IOException e) {
//				return false;
//			}
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public int[] extractInts(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else {
			ensureTransactionCompleted();
			final long	size = 1L * numberOfRows() * numberOfColumns(), maxSize = Integer.MAX_VALUE;
			final int[]	result = new int[(int) Math.min(size, maxSize)];
			
			extractAny(piece, new ProcessContent() {
					int index = 0;
				
					public boolean process(final int row, final int col, final double real, final double image) throws IOException {
						if (index >= result.length - 1) {
							return false;
						}
						else {
							result[index++] = (int)real;
							result[index++] = (int)image;
							return true;
						}
					};
				} 
			);
			return result;
		}
	}
	
	@Override
	public void extractInts(final Piece piece, final DataOutput dataOutput) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else if (dataOutput == null) {
			throw new NullPointerException("Data output can't be null");
		}
		else {
			ensureTransactionCompleted();
			extractAny(piece, (y, x, real, image) -> {
				dataOutput.writeInt((int)real);
				dataOutput.writeInt((int)image);
				return true;
			});
		}
	}

	@Override
	public long[] extractLongs(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else {
			ensureTransactionCompleted();
			final long		size = 1L * numberOfRows() * numberOfColumns(), maxSize = Integer.MAX_VALUE;
			final long[]	result = new long[(int) Math.min(size, maxSize)];
			
			extractAny(piece, new ProcessContent() {
					int index = 0;
				
					public boolean process(final int row, final int col, final double real, final double image) throws IOException {
						if (index >= result.length - 1) {
							return false;
						}
						else {
							result[index++] = (long)real;
							result[index++] = (long)image;
							return true;
						}
					};
				} 
			);
			return result;
		}
	}

	@Override
	public void extractLongs(final Piece piece, final DataOutput dataOutput) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else if (dataOutput == null) {
			throw new NullPointerException("Data output can't be null");
		}
		else {
			ensureTransactionCompleted();
			extractAny(piece, (y, x, real, image) -> {
				dataOutput.writeLong((long)real);
				dataOutput.writeLong((long)image);
				return true;
			});
		}
	}
	
	@Override
	public float[] extractFloats(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else {
			ensureTransactionCompleted();
			final long		size = 1L * numberOfRows() * numberOfColumns(), maxSize = Integer.MAX_VALUE;
			final float[]	result = new float[(int) Math.min(size, maxSize)];
			
			extractAny(piece, new ProcessContent() {
					int index = 0;
				
					public boolean process(final int row, final int col, final double real, final double image) throws IOException {
						if (index >= result.length - 1) {
							return false;
						}
						else {
							result[index++] = (float)real;
							result[index++] = (float)image;
							return true;
						}
					};
				} 
			);
			return result;
		}
	}

	@Override
	public void extractFloats(final Piece piece, final DataOutput dataOutput) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else if (dataOutput == null) {
			throw new NullPointerException("Data output can't be null");
		}
		else {
			ensureTransactionCompleted();
			extractAny(piece, (y, x, real, image) -> {
				dataOutput.writeFloat((float)real);
				dataOutput.writeFloat((float)image);
				return true;
			});
		}
	}
	
	@Override
	public double[] extractDoubles(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else {
			ensureTransactionCompleted();
			final long		size = 1L * numberOfRows() * numberOfColumns(), maxSize = Integer.MAX_VALUE;
			final double[]	result = new double[(int) Math.min(size, maxSize)];
			
			extractAny(piece, new ProcessContent() {
					int index = 0;
				
					public boolean process(final int row, final int col, final double real, final double image) throws IOException {
						if (index >= result.length - 1) {
							return false;
						}
						else {
							result[index++] = real;
							result[index++] = image;
							return true;
						}
					};
				} 
			);
			return result;
		}
	}

	@Override
	public void extractDoubles(final Piece piece, final DataOutput dataOutput) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else if (dataOutput == null) {
			throw new NullPointerException("Data output can't be null");
		}
		else {
			ensureTransactionCompleted();
			extractAny(piece, (y, x, real, image) -> {
				dataOutput.writeDouble(real);
				dataOutput.writeDouble(image);
				return true;
			});
		}
	}
	
	@Override
	public Matrix assign(final Piece piece, final int... content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			ensureTransactionCompleted();
			try {
				return assign(piece, new DataInputAdapter() {
					int index = 0;
					
					@Override
					public int readInt() throws IOException {
						if (index >= content.length) {
							throw new EOFException();
						}
						else {
							return content[index++]; 
						}
					}
				}, Type.REAL_INT);
			} catch (IOException e) {
				throw new IllegalArgumentException("I/O error processing matrix: "+e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public Matrix assign(final Piece piece, final long... content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			ensureTransactionCompleted();
			try {
				return assign(piece, new DataInputAdapter() {
					int index = 0;
					
					@Override
					public long readLong() throws IOException {
						if (index >= content.length) {
							throw new EOFException();
						}
						else {
							return content[index++];
						}
					}
				}, Type.REAL_LONG);
			} catch (IOException e) {
				throw new IllegalArgumentException("I/O error processing matrix: "+e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public Matrix assign(final Piece piece, final float... content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			ensureTransactionCompleted();
			try {
				return assign(piece, new DataInputAdapter() {
					int index = 0;
					
					@Override
					public float readFloat() throws IOException {
						if (index >= content.length) {
							throw new EOFException();
						}
						else {
							return content[index++];
						}
					}
				}, Type.COMPLEX_FLOAT);
			} catch (IOException e) {
				throw new IllegalArgumentException("I/O error processing matrix: "+e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public Matrix assign(final Piece piece, final double... content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			ensureTransactionCompleted();
			try {
				return assign(piece, new DataInputAdapter() {
					int index = 0;
					
					@Override
					public double readDouble() throws IOException {
						if (index >= content.length) {
							throw new EOFException();
						}
						else {
							return content[index++];
						}
					}
				}, Type.COMPLEX_DOUBLE);
			} catch (IOException e) {
				throw new IllegalArgumentException("I/O error processing matrix: "+e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public Matrix assign(Piece piece, Matrix content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix assign(final Piece piece, final DataInput content, final Type type) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (type == null) {
			throw new NullPointerException("Type can't be null");
		}
		else {
			final ProcessArrayContent	pac;
			
			switch (type) {
				case BIT			:
					pac = (y, x, value)->{value[0] = content.readBoolean() ? 1 : 0; value[1] = content.readBoolean() ? 1 : 0; return true;};
					break;
				case COMPLEX_DOUBLE	:
				case REAL_DOUBLE	:
					pac = (y, x, value)->{value[0] = content.readDouble(); value[1] = content.readDouble(); return true;};
					break;
				case COMPLEX_FLOAT	:
				case REAL_FLOAT		:
					pac = (y, x, value)->{value[0] = content.readFloat(); value[1] = content.readFloat(); return true;};
					break;
				case REAL_INT		:
					pac = (y, x, value)->{value[0] = content.readInt(); value[1] = content.readInt(); return true;};
					break;
				case REAL_LONG		:
					pac = (y, x, value)->{value[0] = content.readLong(); value[1] = content.readLong(); return true;};
					break;
				default:
					throw new UnsupportedOperationException("Matrix type ["+type+"] is not supported yet"); 
			}
			
			try(final FileChannel	out = FileChannel.open(largeKeeper.toPath(), StandardOpenOption.WRITE)) {
				scanContentWriteOnly(out, piece, pac, PARALLEL_FACTOR);
			}
			return this;
		}
	}
	
	@Override
	public Matrix fill(final Piece piece, final int value) {
		return fill(piece, (double)value, 0);
	}

	@Override
	public Matrix fill(final Piece piece, final long value) {
		return fill(piece, (double)value, 0);
	}

	@Override
	public Matrix fill(final Piece piece, final float value) {
		return fill(piece, (double)value, 0);
	}

	@Override
	public Matrix fill(final Piece piece, final float real, final float image) {
		return fill(piece, (double)real, (double)image);
	}

	@Override
	public Matrix fill(final Piece piece, final double value) {
		return fill(piece, value, 0);
	}

	@Override
	public Matrix fill(final Piece piece, final double real, final double image) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else {
			ensureTransactionCompleted();
			
			try(final FileChannel	in = FileChannel.open(largeKeeper.toPath(), StandardOpenOption.WRITE)) {
				scanContentWriteOnly(in, piece, (y, x, values) -> {
					values[0] = real;
					values[1] = image;
					return true;
				}, PARALLEL_FACTOR);
			} catch (IOException e) {
				e.printStackTrace();
				throw new IllegalArgumentException("IO error processing request: "+e.getLocalizedMessage(), e);
			}
			return this;
		}
	}

	@Override
	public Matrix cast(Type type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix add(int... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix add(long... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix add(float... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix add(double... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix add(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else if (content.numberOfRows() != numberOfRows() || content.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Different matrix size: current is "+content.numberOfRows()+"x"+content.numberOfColumns()+", awaited is "+numberOfRows()+"x"+numberOfColumns());
		}
		else {
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix addValue(final int value) {
		return addValue((double)value, 0);
	}

	@Override
	public Matrix addValue(final long value) {
		return addValue((double)value, 0);
	}

	@Override
	public Matrix addValue(final float value) {
		return addValue((double)value, 0);
	}

	@Override
	public Matrix addValue(final float real, final float image) {
		return addValue((double)real, (double)image);
	}

	@Override
	public Matrix addValue(final double value) {
		return addValue(value, 0);
	}

	@Override
	public Matrix addValue(double real, double image) {
		// TODO Auto-generated method stub
		beginTransaction();
		return null;
	}

	@Override
	public Matrix subtract(int... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix subtract(long... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix subtract(float... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix subtract(double... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix subtract(Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix subtractValue(final int value) {
		return subtractValue((double)value, 0);
	}

	@Override
	public Matrix subtractValue(final long value) {
		return subtractValue((double)value, 0);
	}

	@Override
	public Matrix subtractValue(final float value) {
		return subtractValue((double)value, 0);
	}

	@Override
	public Matrix subtractValue(final float real, final float image) {
		return subtractValue((double)real, (double)image);
	}

	@Override
	public Matrix subtractValue(final double value) {
		return subtractValue(value, 0);
	}

	@Override
	public Matrix subtractValue(double real, double image) {
		// TODO Auto-generated method stub
		beginTransaction();
		return null;
	}

	@Override
	public Matrix subtractFrom(int... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix subtractFrom(long... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix subtractFrom(float... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix subtractFrom(double... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix subtractFrom(Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix subtractFromValue(final int value) {
		return subtractFromValue((double)value, 0);
	}

	@Override
	public Matrix subtractFromValue(final long value) {
		return subtractFromValue((double)value, 0);
	}

	@Override
	public Matrix subtractFromValue(final float value) {
		return subtractFromValue((double)value, 0);
	}

	@Override
	public Matrix subtractFromValue(final float real, final float image) {
		return subtractFromValue((double)real, (double)image);
	}

	@Override
	public Matrix subtractFromValue(final double value) {
		return subtractFromValue(value, 0);
	}

	@Override
	public Matrix subtractFromValue(double real, double image) {
		// TODO Auto-generated method stub
		beginTransaction();
		return null;
	}

	@Override
	public Matrix mul(Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulFrom(Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulValue(final int value) {
		return mulValue((double)value, 0);
	}

	@Override
	public Matrix mulValue(final long value) {
		return mulValue((double)value, 0);
	}

	@Override
	public Matrix mulValue(final float value) {
		return mulValue((double)value, 0);
	}

	@Override
	public Matrix mulValue(final float real, final float image) {
		return mulValue((double)real, (double)image);
	}

	@Override
	public Matrix mulValue(final double value) {
		return mulValue(value, 0);
	}

	@Override
	public Matrix mulValue(double real, double image) {
		// TODO Auto-generated method stub
		beginTransaction();
		return null;
	}

	@Override
	public Matrix divValue(final int value) {
		return divValue((double)value, 0);
	}

	@Override
	public Matrix divValue(final long value) {
		return divValue((double)value, 0);
	}

	@Override
	public Matrix divValue(final float value) {
		return divValue((double)value, 0);
	}

	@Override
	public Matrix divValue(final float real, final float image) {
		return divValue((double)real, (double)image);
	}

	@Override
	public Matrix divValue(final double value) {
		return divValue(value, 0);
	}

	@Override
	public Matrix divValue(double real, double image) {
		// TODO Auto-generated method stub
		beginTransaction();
		return null;
	}

	@Override
	public Matrix divFromValue(final int value) {
		return divFromValue((double)value, 0);
	}

	@Override
	public Matrix divFromValue(final long value) {
		return divFromValue((double)value, 0);
	}

	@Override
	public Matrix divFromValue(final float value) {
		return divFromValue((double)value, 0);
	}

	@Override
	public Matrix divFromValue(final float real, final float image) {
		return divFromValue((double)real, (double)image);
	}

	@Override
	public Matrix divFromValue(final double value) {
		return divFromValue(value, 0);
	}

	@Override
	public Matrix divFromValue(double real, double image) {
		// TODO Auto-generated method stub
		beginTransaction();
		return null;
	}

	@Override
	public Matrix mulHadamard(int... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulHadamard(long... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulHadamard(float... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulHadamard(double... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulHadamard(Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulInvHadamard(int... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulInvHadamard(long... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulInvHadamard(float... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulInvHadamard(double... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulInvHadamard(Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulInvFromHadamard(int... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulInvFromHadamard(long... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulInvFromHadamard(float... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulInvFromHadamard(double... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix mulInvFromHadamard(Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix tensorMul(Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix tensorMulFrom(Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			beginTransaction();
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Matrix invert() {
		// TODO Auto-generated method stub
		beginTransaction();
		return null;
	}

	@Override
	public Matrix transpose() {
		// TODO Auto-generated method stub
		beginTransaction();
		return null;
	}

	@Override
	public Matrix aggregate(AggregateDirection dir, AggregateType aggType) {
		// TODO Auto-generated method stub
		beginTransaction();
		return null;
	}

	@Override
	public Number det() {
		// TODO Auto-generated method stub
		beginTransaction();
		return null;
	}

	@Override
	public Number track() {
		// TODO Auto-generated method stub
		beginTransaction();
		return null;
	}

	@Override
	public Number[] det2() {
		// TODO Auto-generated method stub
		beginTransaction();
		return null;
	}

	@Override
	public Number[] track2() {
		// TODO Auto-generated method stub
		beginTransaction();
		return null;
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyBit callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyInt callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyLong callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyFloat callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyDouble callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyFloat2 callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyDouble2 callback) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else if (callback == null) {
			throw new NullPointerException("callback can't be null");
		}
		else {
			ensureTransactionCompleted();
			
			try(final FileChannel	out = FileChannel.open(largeKeeper.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)) {
				
				scanContentReadWrite(out, piece, (y, x, values)->{callback.apply(y, x, values); return true;}, PARALLEL_FACTOR);
			} catch (IOException e) {
				throw new IllegalArgumentException(e.getLocalizedMessage(), e);
			}
			return this;
		}
	}

	@Override
	public String toString() {
		return "CompleDoubleMatrix["+numberOfRows()+"x"+numberOfColumns()+" based on "+largeKeeper.getAbsolutePath()+", total size="+(totalSize/GIGABYTE)+"Gb, transaction mode is "+transactionMode+"]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cols;
		result = prime * result + rows;
		result = prime * result + (int) (totalSize ^ (totalSize >>> 32));
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ComplexDoubleMatrix other = (ComplexDoubleMatrix) obj;
		if (cols != other.cols) return false;
		if (rows != other.rows) return false;
		if (totalSize != other.totalSize) return false;
		return deepEquals(other);
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
				scanContent(raf, Piece.of(0, 0, numberOfRows(), numberOfColumns()), new ProcessContent() {
					int oldRow = -1;
					
					@Override
					public boolean process(int row, int col, double real, double image) throws IOException {
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
	public Matrix done() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean areAllAsyncCompleted() {
		return !transactionMode;
	}
	
	private void ensureTransactionCompleted() {
		if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Attempl to call this method until transaction completed");
		}
	}

	private void extractAny(final Piece piece, final ProcessContent callback) {
		try(final FileChannel	in = FileChannel.open(largeKeeper.toPath(), StandardOpenOption.READ)) {
			scanContentReadOnly(in, piece, callback, PARALLEL_FACTOR);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("IO error processing request: "+e.getLocalizedMessage(), e);
		}
	}
	
	
	private void beginTransaction() {
		transactionMode = true;
	}
	
	private boolean scanContent(final RandomAccessFile raf, final Piece piece, final ProcessContent callback) throws IOException {
		final long 	cols = numberOfColumns();
		final int	size = getType().getNumberOfItems() * getType().getItemSize(); 
		
		for(int y = piece.getTop(), maxY = piece.getTop() + piece.getHeight(); y < maxY; y++) {
			final long	address = 1L * y * cols * size;
			
			raf.seek(address);			
			for(int x = piece.getLeft(), maxX = piece.getLeft() + piece.getWidth(); x < maxX; x++) {
				if (!callback.process(y, x, raf.readDouble(), raf.readDouble())) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean scanContentReadOnly(final FileChannel fc, final Piece piece, final ProcessContent callback, final int parallels) throws IOException {
		final long 			cols = piece.getWidth();
		final int			size = getType().getNumberOfItems() * getType().getItemSize();
		final ByteBuffer	bb = ByteBuffer.allocateDirect((int)(cols * size));
		final byte[]		data = new byte[size];
		final double[]		values = new double[2];
		
		for(int y = piece.getTop(), maxY = piece.getTop() + piece.getHeight(); y < maxY; y++) {
			bb.clear();
			fc.read(bb, 1L * (y * numberOfColumns() + piece.getLeft()) * size);
			bb.rewind();
			for(int x = piece.getLeft(), maxX = piece.getLeft() + piece.getWidth(); x < maxX; x++) {
				bb.get(data);
				deserialize(data, values);
				
				if (!callback.process(y, x, values[0], values[1])) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean scanContentWriteOnly(final FileChannel fc, final Piece piece, final ProcessArrayContent callback, final int parallels) throws IOException {
		final long 			cols = piece.getWidth();
		final int			size = getType().getNumberOfItems() * getType().getItemSize();
		final ByteBuffer	bb = ByteBuffer.allocateDirect((int)(cols * size));
		final byte[]		data = new byte[size];
		final double[]		values = new double[2];
		
		for(int y = piece.getTop(), maxY = piece.getTop() + piece.getHeight(); y < maxY; y++) {
			bb.clear();
			
			fc.position(1L * (y * numberOfColumns() + piece.getLeft()) * size);
			for(int x = piece.getLeft(), maxX = piece.getLeft() + piece.getWidth(); x < maxX; x++) {
				try{
					if (!callback.process(y, x, values)) {
						bb.flip();
						while(bb.hasRemaining()) {
							fc.write(bb);
						}			
						return false;
					}
					else {
						bb.put(serialize(data, values));
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

	private boolean scanContentReadWrite(final FileChannel fc, final Piece piece, final ProcessArrayContent callback, final int parallels) throws IOException {
		final long 			cols = piece.getWidth();
		final int			size = getType().getNumberOfItems() * getType().getItemSize();
		final ByteBuffer	bb = ByteBuffer.allocateDirect((int)(cols * size));
		final byte[]		data = new byte[size];
		final double[]		values = new double[2];
		
		for(int y = piece.getTop(), maxY = piece.getTop() + piece.getHeight(); y < maxY; y++) {
			final long	address = 1L * (y * numberOfColumns() + piece.getLeft()) * size;
			
			bb.clear();
			fc.position(address);
			fc.read(bb);
			bb.rewind();
			for(int x = piece.getLeft(), maxX = piece.getLeft() + piece.getWidth(); x < maxX; x++) {
				final int	position = bb.position();
				
				bb.get(data);
				deserialize(data, values);
				if (!callback.process(y, x, values)) {
					bb.flip();
					fc.position(address);
					while(bb.hasRemaining()) {
						fc.write(bb);
					}			
					return false;
				}
				else {
					bb.put(position, serialize(data, values));
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
	
	private boolean scanContentReadOnly(final FileChannel left, final FileChannel right, final Piece piece, final ProcessContentPair callback) throws IOException {
		final long 			cols = piece.getWidth();
		final int			size = getType().getNumberOfItems() * getType().getItemSize();
		final ByteBuffer	bbLeft = ByteBuffer.allocateDirect((int)(cols * size));
		final ByteBuffer	bbRight = ByteBuffer.allocate((int)(cols * size));
		final byte[]		data = new byte[size];
		final double[]		leftValues = new double[2], rightValues = new double[2];
		
		for(int y = piece.getTop(), maxY = piece.getTop() + piece.getHeight(); y < maxY; y++) {
			bbLeft.clear();
			bbRight.clear();
			left.read(bbLeft, 1L * (y * numberOfColumns() + piece.getLeft()) * size);
			right.read(bbRight, 1L * (y * numberOfColumns() + piece.getLeft()) * size);
			bbLeft.rewind();
			bbRight.rewind();
			for(int x = piece.getLeft(), maxX = piece.getLeft() + piece.getWidth(); x < maxX; x++) {
				bbLeft.get(data);
				deserialize(data, leftValues);
				bbRight.get(data);
				deserialize(data, rightValues);
				
				if (!callback.process(y, x, leftValues[0], leftValues[1], rightValues[0], rightValues[1])) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isOverlaps(final Piece piece) {
		if (piece.getTop() < 0 || piece.getLeft() < 0 || piece.getTop() >= numberOfRows() || piece.getLeft() >= numberOfColumns()) {
			return true;
		}
		else if (piece.getTop() + piece.getHeight() < 0 || piece.getLeft() + piece.getWidth() < 0 || piece.getTop() + piece.getHeight() > numberOfRows() || piece.getLeft() + piece.getWidth() > numberOfColumns()) {
			return true;
		}
		else if (piece.getWidth() <= 0 || piece.getHeight() <= 0) {
			return true;
		}
		else {
			return false;
		}
	}

	private Piece totalPiece() {
		return Piece.of(0, 0, numberOfRows(), numberOfColumns());
	}
	
	static byte[] serialize(final byte[] target, final double[] source) {
		int	count = 0;
		
		for(int index = 0; index < source.length; index++) {
			final long	value = Double.doubleToLongBits(source[index]);
			
			for(int shift = 56; shift >= 0; shift -= 8) {
				target[count++] = (byte)(value >>> shift);
			}
		}
		return target;
	}

	static double[] deserialize(final byte[] source, final double[] target) {
		int	count = 0;
		
		for(int index = 0; index < target.length; index++) {
			long	value = (((long)source[count++] << 56) +
	                		((long)(source[count++] & 0xFF) << 48) +
	                		((long)(source[count++] & 0xFF) << 40) +
	                		((long)(source[count++] & 0xFF) << 32) +
	                		((long)(source[count++] & 0xFF) << 24) +
	                		((source[count++] & 0xFF) << 16) +
	                		((source[count++] & 0xFF) << 8) +
	                		((source[count++] & 0xFF) << 0));
			target[index] = Double.longBitsToDouble(value);
		}
		return target;
	}
	
	@FunctionalInterface
	private static interface ProcessContent {
		boolean process(int row, int col, double real, double image) throws IOException;
	}

	@FunctionalInterface
	private static interface ProcessContentPair {
		boolean process(int row, int col, double realLeft, double imageLeft, double realRight, double imageRight) throws IOException;
	}
	
	@FunctionalInterface
	private static interface ProcessArrayContent {
		boolean process(int row, int col, double[] content) throws IOException;
	}
}
