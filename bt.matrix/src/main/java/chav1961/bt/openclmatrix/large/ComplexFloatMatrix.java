package chav1961.bt.openclmatrix.large;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import chav1961.bt.openclmatrix.internal.GPUExecutor;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUEvent;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUExecutable;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUScheduler;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryStore;
import chav1961.bt.openclmatrix.internal.InternalUtils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.MatrixCalc;

public class ComplexFloatMatrix extends LargeMatrix {
	private static final int	MAX_GPU_BUFFER_SIZE_IN_ITEMS = GPUBuffer.MAX_GPU_BUFFER_SIZE / Type.COMPLEX_FLOAT.getItemSize();
	private static final String	ADD_INT_ARRAY_NAME = "addIntArray"+Type.COMPLEX_FLOAT.getProgramSuffix();
	private static final String	ADD_INT_ARRAY_KERNEL =    "__kernel void "+ADD_INT_ARRAY_NAME+"(const int columns,\n"
														+ "                      __global float* source,\n"
														+ "                      const __global int* add) {\n"
														+ "	int row = get_global_id(0);\n"
														+ "	int start = 2 * columns * row;\n"
														+ "	for(int col = 0; col < 2 * columns; col++) {\n"
														+ "	  source[start + col] += add[start + col];\n"
														+ "	}\n"
														+ "}";
	private static final String	ADD_LONG_ARRAY_NAME = "addLongArray"+Type.COMPLEX_FLOAT.getProgramSuffix();
	private static final String	ADD_LONG_ARRAY_KERNEL =    "__kernel void "+ADD_LONG_ARRAY_NAME+"(const int columns,\n"
														+ "                      __global float* source,\n"
														+ "                      const __global long* add) {\n"
														+ "	int row = get_global_id(0);\n"
														+ "	int start = 2 * columns * row;\n"
														+ "	for(int col = 0; col < 2 * columns; col++) {\n"
														+ "	  source[start + col] += add[start + col];\n"
														+ "	}\n"
														+ "}";
	private static final String	ADD_FLOAT_ARRAY_NAME = "addFloatArray"+Type.COMPLEX_FLOAT.getProgramSuffix();
	private static final String	ADD_FLOAT_ARRAY_KERNEL =    "__kernel void "+ADD_FLOAT_ARRAY_NAME+"(const int columns,\n"
														+ "                      __global float2* source,\n"
														+ "                      const __global float2* add) {\n"
														+ "	int row = get_global_id(0);\n"
														+ "	int start = columns * row;\n"
														+ "	for(int col = 0; col < columns; col++) {\n"
														+ "	  source[start + col] += add[start + col];\n"
														+ "	}\n"
														+ "}";
	private static final String	ADD_DOUBLE_ARRAY_NAME = "addDoubleArray"+Type.COMPLEX_FLOAT.getProgramSuffix();
	private static final String	ADD_DOUBLE_ARRAY_KERNEL =    "__kernel void "+ADD_DOUBLE_ARRAY_NAME+"(const int columns,\n"
														+ "                      __global float* source,\n"
														+ "                      const __global double* add) {\n"
														+ "	int row = get_global_id(0);\n"
														+ "	int start = 2 * columns * row;\n"
														+ "	for(int col = 0; col < 2 * columns; col++) {\n"
														+ "	  source[start + col] += add[start + col];\n"
														+ "	}\n"
														+ "}";
	private static final String	ADD_VALUE_NAME = "addValue"+Type.COMPLEX_FLOAT.getProgramSuffix();
	private static final String	ADD_VALUE_KERNEL =    "__kernel void "+ADD_VALUE_NAME+"(const int columns,\n"
														+ "                      __global float2* source,\n"
														+ "                      const float real,\n"
														+ "                      const float image) {\n"
														+ "	int row = get_global_id(0);\n"
														+ "	int start = columns * row;\n"
														+ "	float2 val = (float2)(real, image);\n"
														+ "	for(int col = 0; col < columns; col++) {\n"
														+ "	  source[start + col] += val;\n"
														+ "	}\n"
														+ "}";
	private static final String	SUBTRACT_INT_ARRAY_NAME = "subtractIntArray"+Type.COMPLEX_FLOAT.getProgramSuffix();
	private static final String	SUBTRACT_INT_ARRAY_KERNEL =    "__kernel void "+SUBTRACT_INT_ARRAY_NAME+"(const int columns,\n"
														+ "                      __global float* source,\n"
														+ "                      const __global int* subtract) {\n"
														+ "	int row = get_global_id(0);\n"
														+ "	int start = 2 * columns * row;\n"
														+ "	for(int col = 0; col < 2 * columns; col++) {\n"
														+ "	  source[start + col] -= subtract[start + col];\n"
														+ "	}\n"
														+ "}";
	private static final String	SUBTRACT_LONG_ARRAY_NAME = "subtractLongArray"+Type.COMPLEX_FLOAT.getProgramSuffix();
	private static final String	SUBTRACT_LONG_ARRAY_KERNEL =    "__kernel void "+SUBTRACT_LONG_ARRAY_NAME+"(const int columns,\n"
														+ "                      __global float* source,\n"
														+ "                      const __global long* subtract) {\n"
														+ "	int row = get_global_id(0);\n"
														+ "	int start = 2 * columns * row;\n"
														+ "	for(int col = 0; col < 2 * columns; col++) {\n"
														+ "	  source[start + col] -= subtract[start + col];\n"
														+ "	}\n"
														+ "}";
	private static final String	SUBTRACT_FLOAT_ARRAY_NAME = "subtractFloatArray"+Type.COMPLEX_FLOAT.getProgramSuffix();
	private static final String	SUBTRACT_FLOAT_ARRAY_KERNEL =    "__kernel void "+SUBTRACT_FLOAT_ARRAY_NAME+"(const int columns,\n"
														+ "                      __global float2* source,\n"
														+ "                      const __global float2* subtract) {\n"
														+ "	int row = get_global_id(0);\n"
														+ "	int start = columns * row;\n"
														+ "	for(int col = 0; col < columns; col++) {\n"
														+ "	  source[start + col] -= subtract[start + col];\n"
														+ "	}\n"
														+ "}";
	private static final String	SUBTRACT_DOUBLE_ARRAY_NAME = "subtractDoubleArray"+Type.COMPLEX_FLOAT.getProgramSuffix();
	private static final String	SUBTRACT_DOUBLE_ARRAY_KERNEL =    "__kernel void "+SUBTRACT_DOUBLE_ARRAY_NAME+"(const int columns,\n"
														+ "                      __global float* source,\n"
														+ "                      const __global double* subtract) {\n"
														+ "	int row = get_global_id(0);\n"
														+ "	int start = 2 * columns * row;\n"
														+ "	for(int col = 0; col < 2 * columns; col++) {\n"
														+ "	  source[start + col] -= subtract[start + col];\n"
														+ "	}\n"
														+ "}";
	private static final String	SUBTRACT_VALUE_NAME = "subtractValue"+Type.COMPLEX_FLOAT.getProgramSuffix();
	private static final String	SUBTRACT_VALUE_KERNEL =    "__kernel void "+SUBTRACT_VALUE_NAME+"(const int columns,\n"
														+ "                      __global float2* source,\n"
														+ "                      const float real,\n"
														+ "                      const float image) {\n"
														+ "	int row = get_global_id(0);\n"
														+ "	int start = columns * row;\n"
														+ "	float2 val = (float2)(real, image);\n"
														+ "	for(int col = 0; col < columns; col++) {\n"
														+ "	  source[start + col] -= val;\n"
														+ "	}\n"
														+ "}";
	
	private final int[]		intBuffer = new int[2];
	private GPUScheduler	sched = null; 

	public ComplexFloatMatrix(final GPUExecutor executor, final int rows, final int cols) {
		this(executor, InternalUtils.TEMP_DIR_LOCATION, rows, cols);
	}	
	
	public ComplexFloatMatrix(final GPUExecutor executor, final File contentDir, final int rows, final int cols) {
		super(executor, contentDir, Type.COMPLEX_FLOAT, rows, cols);
	}

	private ComplexFloatMatrix(final GPUExecutor executor, final File contentDir, final int rows, final int cols, final File fill, final boolean copyFileContent) {
		super(executor, contentDir, Type.COMPLEX_FLOAT, rows, cols, fill, copyFileContent);
	}	
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		ensureTransactionCompleted();
		return new ComplexFloatMatrix(getExecutor(), getFileKeeper().getParentFile(), numberOfRows(), numberOfColumns(), getFileKeeper(), true);
	}
	
	@Override
	public boolean deepEquals(Matrix another) {
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
			final int[]	temp = new int[2];
			
			ensureTransactionCompleted();
			try(final FileChannel	left = FileChannel.open(getFileKeeper().toPath(), StandardOpenOption.READ);
				final FileChannel	right = FileChannel.open(((ComplexFloatMatrix)another).getFileKeeper().toPath(), StandardOpenOption.READ)) {

				if (left.size() != right.size()) {
					return false;
				}
				else {
					return scanContentReadOnly2(left, right, Piece.of(0, 0, numberOfRows(), numberOfColumns()), (y, x, leftBuffer, rightBuffer)->{
						deserialize(leftBuffer, intBuffer, 2);
						deserialize(rightBuffer, temp, 2);
						
						return intBuffer[0] == temp[0] && intBuffer[1] == temp[1];
					});
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
			final long		size = 1L * piece.getHeight() * piece.getWidth() * getType().getNumberOfItems(), maxSize = Integer.MAX_VALUE;
			final int[]		result = new int[(int) Math.min(size, maxSize)];
			
			extractAny(piece, new ProcessFCContent() {
				int index = 0;
			
				public boolean process(final int row, final int col, final ByteBuffer source) throws IOException {
					deserialize(source, intBuffer, 2);
					
					if (index >= result.length - 1) {
						return false;
					}
					else {
						result[index++] = (int)Float.intBitsToFloat(intBuffer[0]);
						result[index++] = (int)Float.intBitsToFloat(intBuffer[1]);
						return true;
					}
				};
			});
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
			extractAny(piece, new ProcessFCContent() {
				public boolean process(final int row, final int col, final ByteBuffer source) throws IOException {
					deserialize(source, intBuffer, 2);
					dataOutput.writeInt((int)Float.intBitsToFloat(intBuffer[0]));
					dataOutput.writeInt((int)Float.intBitsToFloat(intBuffer[1]));
					return true;
				};
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
			final long		size = 1L * piece.getHeight() * piece.getWidth() * getType().getNumberOfItems(), maxSize = Integer.MAX_VALUE;
			final long[]	result = new long[(int) Math.min(size, maxSize)];
			
			extractAny(piece, new ProcessFCContent() {
				int index = 0;
			
				public boolean process(final int row, final int col, final ByteBuffer source) throws IOException {
					deserialize(source, intBuffer, 2);
					
					if (index >= result.length - 1) {
						return false;
					}
					else {
						result[index++] = (long)Float.intBitsToFloat(intBuffer[0]);
						result[index++] = (long)Float.intBitsToFloat(intBuffer[1]);
						return true;
					}
				};
			});
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
			extractAny(piece, new ProcessFCContent() {
				public boolean process(final int row, final int col, final ByteBuffer source) throws IOException {
					deserialize(source, intBuffer, 2);
					dataOutput.writeLong((long)Float.intBitsToFloat(intBuffer[0]));
					dataOutput.writeLong((long)Float.intBitsToFloat(intBuffer[1]));
					return true;
				};
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
			final long		size = 1L * piece.getHeight() * piece.getWidth() * getType().getNumberOfItems(), maxSize = Integer.MAX_VALUE;
			final float[]	result = new float[(int) Math.min(size, maxSize)];

			return extractFloats(piece, result);
		}
	}

	@Override
	public float[] extractFloats(final Piece piece, final float[] target) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else if (target == null) {
			throw new NullPointerException("Target array can't be null");
		}
		else {
			ensureTransactionCompleted();
			
			Arrays.fill(intBuffer, 0);
			
			extractAny(piece, new ProcessFCContent() {
				int index = 0;
			
				public boolean process(final int row, final int col, final ByteBuffer source) throws IOException {
					deserialize(source, intBuffer, 2);
					
					if (index >= target.length - 1) {
						return false;
					}
					else {
						target[index++] = Float.intBitsToFloat(intBuffer[0]);
						target[index++] = Float.intBitsToFloat(intBuffer[1]);
						return true;
					}
				};
			});
			return target;
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
			extractAny(piece, new ProcessFCContent() {
				public boolean process(final int row, final int col, final ByteBuffer source) throws IOException {
					deserialize(source, intBuffer, 2);
					dataOutput.writeFloat(Float.intBitsToFloat(intBuffer[0]));
					dataOutput.writeFloat(Float.intBitsToFloat(intBuffer[1]));
					return true;
				};
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
			final long		size = 1L * piece.getHeight() * piece.getWidth() * getType().getNumberOfItems(), maxSize = Integer.MAX_VALUE;
			final double[]	result = new double[(int) Math.min(size, maxSize)];
			
			extractAny(piece, new ProcessFCContent() {
				int index = 0;
			
				public boolean process(final int row, final int col, final ByteBuffer source) throws IOException {
					deserialize(source, intBuffer, 2);
					
					if (index >= result.length - 1) {
						return false;
					}
					else {
						result[index++] = (double)Float.intBitsToFloat(intBuffer[0]);
						result[index++] = (double)Float.intBitsToFloat(intBuffer[1]);
						return true;
					}
				};
			});
			return result;
		}
	}

	@Override
	public void extractDoubles(Piece piece, DataOutput dataOutput) throws IOException {
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
			extractAny(piece, new ProcessFCContent() {
				public boolean process(final int row, final int col, final ByteBuffer source) throws IOException {
					deserialize(source, intBuffer, 2);
					dataOutput.writeDouble(Float.intBitsToFloat(intBuffer[0]));
					dataOutput.writeDouble(Float.intBitsToFloat(intBuffer[1]));
					return true;
				};
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
			assignAny(piece, new ProcessFCContent() {
				int index = 0;
				
				public boolean process(final int row, final int col, final ByteBuffer target) throws IOException {
					if (index < content.length - 1) {
						intBuffer[0] = Float.floatToIntBits(content[index++]);
						intBuffer[1] = Float.floatToIntBits(content[index++]);
						serialize(intBuffer, 2, target);
						return true;
					}
					else {
						return false;
					}
				};
			});
			return this;
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
			assignAny(piece, new ProcessFCContent() {
				int index = 0;
				
				public boolean process(final int row, final int col, final ByteBuffer target) throws IOException {
					if (index < content.length - 1) {
						intBuffer[0] = Float.floatToIntBits(content[index++]);
						intBuffer[1] = Float.floatToIntBits(content[index++]);
						serialize(intBuffer, 2, target);
						return true;
					}
					else {
						return false;
					}
				};
			});
			return this;
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
			assignAny(piece, new ProcessFCContent() {
				int index = 0;
				
				public boolean process(final int row, final int col, final ByteBuffer target) throws IOException {
					if (index < content.length - 1) {
						intBuffer[0] = Float.floatToIntBits(content[index++]);
						intBuffer[1] = Float.floatToIntBits(content[index++]);
						serialize(intBuffer, 2, target);
						return true;
					}
					else {
						return false;
					}
				};
			});
			return this;
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
			assignAny(piece, new ProcessFCContent() {
				int index = 0;
				
				public boolean process(final int row, final int col, final ByteBuffer target) throws IOException {
					if (index < content.length - 1) {
						intBuffer[0] = Float.floatToIntBits((float)content[index++]);
						intBuffer[1] = Float.floatToIntBits((float)content[index++]);
						serialize(intBuffer, 2, target);
						return true;
					}
					else {
						return false;
					}
				};
			});
			return this;
		}
	}

	@Override
	public Matrix assign(final Piece piece, final Matrix matrix) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else if (matrix == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			ensureTransactionCompleted();
			try(final PipedInputStream	pis = new PipedInputStream();
				final PipedOutputStream	pos = new PipedOutputStream(pis);
				final DataInputStream	dis = new DataInputStream(pis)) {
				
				final Thread	t = new Thread(()->{
									try (final DataOutputStream	dos = new DataOutputStream(pos)) {
										matrix.extractFloats(dos);
									} catch (IOException exc) {
									}
								});
				t.setDaemon(true);
				t.start();
				assign(piece, dis, matrix.getType());
			} catch (IOException e) {
			}
			return this;
		}
	}

	@FunctionalInterface
	private static interface AssignAcceptor {
		void process(DataInput in, int[] buf) throws IOException;
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
			throw new NullPointerException("Matrix type can't be null");
		}
		else {
			ensureTransactionCompleted();
			final AssignAcceptor	callback;
			
			switch (type) {
				case COMPLEX_DOUBLE	:
				case REAL_DOUBLE	:
					callback = (c,b)->{b[0] = Float.floatToIntBits((float)c.readDouble()); b[1] = Float.floatToIntBits((float)c.readDouble());};
					break;
				case COMPLEX_FLOAT	:
				case REAL_FLOAT		:
					callback = (c,b)->{b[0] = Float.floatToIntBits(c.readFloat()); b[1] = Float.floatToIntBits(c.readFloat());};
					break;
				case REAL_INT		:
					callback = (c,b)->{b[0] = Float.floatToIntBits(c.readInt()); b[1] = Float.floatToIntBits(c.readInt());};
					break;
				case REAL_LONG		:
					callback = (c,b)->{b[0] = Float.floatToIntBits(c.readLong()); b[1] = Float.floatToIntBits(c.readLong());};
					break;
				case BIT			:
					callback = (c,b)->{b[0] = c.readBoolean() ? 1 : 0; b[1] = 0;};
					break;
				default :
					throw new UnsupportedOperationException("Matrix type [] is not supported yet");
			}
			
			assignAny(piece, new ProcessFCContent() {
				public boolean process(final int row, final int col, final ByteBuffer target) throws IOException {
					callback.process(content, intBuffer);
					serialize(intBuffer, 2, target);
					return true;
				};
			});
			return this;
		}
	}

	@Override
	public Matrix fill(final Piece piece, final int value) {
		return fill(piece, (float)value, 0f);
	}

	@Override
	public Matrix fill(final Piece piece, final long value) {
		return fill(piece, (float)value, 0f);
	}

	@Override
	public Matrix fill(final Piece piece, final float value) {
		return fill(piece, value, 0f);
	}

	@Override
	public Matrix fill(final Piece piece, final float real, final float image) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else {
			ensureTransactionCompleted();
			intBuffer[0] = Float.floatToIntBits(real);
			intBuffer[1] = Float.floatToIntBits(image);
			
			assignAny(piece, new ProcessFCContent() {
				public boolean process(final int row, final int col, final ByteBuffer target) throws IOException {
					serialize(intBuffer, 2, target);
					return true;
				};
			});
			return this;
		}
	}

	@Override
	public Matrix fill(final Piece piece, final double value) {
		return fill(piece, (float)value, 0f);
	}

	@Override
	public Matrix fill(final Piece piece, final double real, final double image) {
		return fill(piece, (float)real, (float)image);
	}

	@Override
	public Matrix cast(final Type type) {
		if (type == null) {
			throw new NullPointerException("Cast type can't be null");
		}
		else {
			switch (type) {
				case COMPLEX_FLOAT	:
					try {
						return (Matrix) this.clone();
					} catch (CloneNotSupportedException e) {
						return this;
					}
				case COMPLEX_DOUBLE	:
				case REAL_DOUBLE	:
				case REAL_FLOAT		:
				case REAL_INT		:
				case REAL_LONG		:
				default:
					throw new UnsupportedOperationException("Matrix type ["+type+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix add(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			final ComplexFloatMatrix	result;
			
			if (content.length == 0) {
				try {
					result = (ComplexFloatMatrix) this.clone();
				} catch (CloneNotSupportedException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
			else {
				result = lineByLineAny(ADD_INT_ARRAY_NAME, ADD_INT_ARRAY_KERNEL, content.length, 
								new ControlledDataInput() {
										int 		index = 0;
										
										@Override
										public int readInt() throws IOException {
											if (index >= content.length) {
												throw new EOFException();
											}
											else if (index < content.length) {
												return content[index++];
											}
											else {
												index++;
												return 0;
											}
										}
					
										@Override
										public long getReadAmount() {
											return index;
										}
									}, Type.REAL_INT);
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix add(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			final ComplexFloatMatrix	result;
			
			if (content.length == 0) {
				try {
					result = (ComplexFloatMatrix) this.clone();
				} catch (CloneNotSupportedException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
			else {
				result = lineByLineAny(ADD_LONG_ARRAY_NAME, ADD_LONG_ARRAY_KERNEL, content.length, 
						new ControlledDataInput() {
								int 		index = 0;
								
								@Override
								public long readLong() throws IOException {
									if (index >= content.length) {
										throw new EOFException();
									}
									else if (index < content.length) {
										return content[index++];
									}
									else {
										index++;
										return 0;
									}
								}
			
								@Override
								public long getReadAmount() {
									return index;
								}
							}, Type.REAL_LONG);
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix add(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			final ComplexFloatMatrix	result;
			
			if (content.length == 0) {
				try {
					result = (ComplexFloatMatrix) this.clone();
				} catch (CloneNotSupportedException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
			else {
				result = lineByLineAny(ADD_FLOAT_ARRAY_NAME, ADD_FLOAT_ARRAY_KERNEL, content.length, 
						new ControlledDataInput() {
								int 		index = 0;
								
								@Override
								public float readFloat() throws IOException {
									if (index >= content.length) {
										throw new EOFException();
									}
									else if (index < content.length) {
										return content[index++];
									}
									else {
										index++;
										return 0;
									}
								}
			
								@Override
								public long getReadAmount() {
									return index;
								}
							}, Type.REAL_FLOAT);
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix add(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			final ComplexFloatMatrix	result;
			
			if (content.length == 0) {
				try {
					result = (ComplexFloatMatrix) this.clone();
				} catch (CloneNotSupportedException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
			else {
				result = lineByLineAny(ADD_DOUBLE_ARRAY_NAME, ADD_DOUBLE_ARRAY_KERNEL, content.length, 
						new ControlledDataInput() {
								int 		index = 0;
								
								@Override
								public double readDouble() throws IOException {
									if (index >= content.length) {
										throw new EOFException();
									}
									else if (index < content.length) {
										return content[index++];
									}
									else {
										index++;
										return 0;
									}
								}
			
								@Override
								public long getReadAmount() {
									return index;
								}
							}, Type.REAL_DOUBLE);
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix add(final Matrix matrix) {
		if (matrix == null) {
			throw new NullPointerException("Matrix can't be null");
		}
		else if (matrix.numberOfRows() != numberOfRows() || matrix.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Different matrix size: current="+numberOfRows()+'x'+numberOfColumns()+", another="+matrix.numberOfRows()+'x'+matrix.numberOfColumns()); 
		}
		else {
			final ComplexFloatMatrix	result;
			
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
					result = lineByLineAny(ADD_DOUBLE_ARRAY_NAME, ADD_DOUBLE_ARRAY_KERNEL, matrix);
					break;
				case COMPLEX_FLOAT 	:
					result = lineByLineAny(ADD_FLOAT_ARRAY_NAME, ADD_FLOAT_ARRAY_KERNEL, matrix);
					break;
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG : case BIT :
					throw new IllegalArgumentException("Attempt to add real and complex matrix. Use cast() before");
				default : 
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix addValue(final int value) {
		return addValue((float)value, 0f);
	}

	@Override
	public Matrix addValue(final long value) {
		return addValue((float)value, 0f);
	}

	@Override
	public Matrix addValue(float value) {
		return addValue((float)value, 0f);
	}

	@Override
	public Matrix addValue(final float real, final float image) {
		final ComplexFloatMatrix	result;
		
		if (real == 0 && image == 0) {
			try {
				result = (ComplexFloatMatrix) this.clone();
			} catch (CloneNotSupportedException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		else {
			result = lineByLineValue(ADD_VALUE_NAME, ADD_VALUE_KERNEL, real, image);
//			try {
//				final GPUExecutable	prog = getOrCreateProgram(ADD_VALUE_NAME, ADD_VALUE_KERNEL);
//				final int	leftRows = calcGPUBufferSize(getType(), numberOfRows(), numberOfColumns());
//				final int	leftBufferSize = leftRows * numberOfColumns() * getType().getNumberOfItems();
//				final int	occupiedBytes = getType().getNumberOfItems() * getType().getItemSize();
//				
//				try(final ComplexFloatMatrix	temp = new ComplexFloatMatrix(getExecutor(), getFileKeeper().getParentFile(), 1, 1)) {
//					final long[]				taskSize = new long[1]; 
//					File						tempStoreFile = null;
//					
//					temp.beginTransaction();
//					try(final GPUBuffer			left = temp.getScheduler().allocateGPUBuffer(leftBufferSize * getType().getItemSize());
//						final TemporaryStore	store = temp.getScheduler().allocateTemporaryStore(getFileKeeper().getParentFile(), 1L * occupiedBytes * numberOfRows() * numberOfColumns(), false)) {
//
//						tempStoreFile = store.getContentFile();
//						for (int leftIndex = 0, maxIndex = numberOfRows(); leftIndex < maxIndex; leftIndex += leftRows) {
//							final int		leftPiece = leftIndex + leftRows > numberOfRows() ? numberOfRows() - leftIndex : leftRows;
//							final Piece		currentPiece = Piece.of(leftIndex, 0, leftPiece, numberOfColumns());
//							final long		blockSize = occupiedBytes * currentPiece.getWidth() * currentPiece.getHeight();
//							
//							try(final TemporaryBuffer	out = store.getBuffer(leftIndex * blockSize / leftRows, (int)blockSize);
//								final GPUEvent 	downloadEventLeft = left.download(currentPiece, this);
//								final GPUEvent 	calcEvent = temp.getScheduler().createEvent()) {
//								
//								downloadEventLeft.awaitCurrent();
//								taskSize[0] = leftPiece;
//								prog.execute(calcEvent, taskSize, numberOfColumns(), left, real, image);
//								calcEvent.awaitCurrent();
//								left.upload(out, getType()).awaitCurrent().close();
//							}
//						}
//					}
//					result = new ComplexFloatMatrix(getExecutor(), getFileKeeper().getParentFile(), numberOfRows(), numberOfColumns(), tempStoreFile, false);
// 				}
//			} catch (ContentException | CalculationException exc) {
//				throw new IllegalStateException("Internal error: "+exc.getLocalizedMessage(), exc); 				
//			} catch (IOException | InterruptedException exc) {
//				throw new IllegalStateException("Internal error: "+exc.getLocalizedMessage(), exc); 				
//			}
		}
		result.beginTransaction();
		return result;
	}

	@Override
	public Matrix addValue(double value) {
		return addValue((float)value, 0f);
	}

	@Override
	public Matrix addValue(double real, double image) {
		return addValue((float)real, (float)image);
	}

	@Override
	public Matrix subtract(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			final ComplexFloatMatrix	result;
			
			if (content.length == 0) {
				try {
					result = (ComplexFloatMatrix) this.clone();
				} catch (CloneNotSupportedException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
			else {
				result = lineByLineAny(SUBTRACT_INT_ARRAY_NAME, SUBTRACT_INT_ARRAY_KERNEL, content.length, 
								new ControlledDataInput() {
										int 		index = 0;
										
										@Override
										public int readInt() throws IOException {
											if (index >= content.length) {
												throw new EOFException();
											}
											else if (index < content.length) {
												return content[index++];
											}
											else {
												index++;
												return 0;
											}
										}
					
										@Override
										public long getReadAmount() {
											return index;
										}
									}, Type.REAL_INT);
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix subtract(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			final ComplexFloatMatrix	result;
			
			if (content.length == 0) {
				try {
					result = (ComplexFloatMatrix) this.clone();
				} catch (CloneNotSupportedException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
			else {
				result = lineByLineAny(SUBTRACT_LONG_ARRAY_NAME, SUBTRACT_LONG_ARRAY_KERNEL, content.length, 
						new ControlledDataInput() {
								int 		index = 0;
								
								@Override
								public long readLong() throws IOException {
									if (index >= content.length) {
										throw new EOFException();
									}
									else if (index < content.length) {
										return content[index++];
									}
									else {
										index++;
										return 0;
									}
								}
			
								@Override
								public long getReadAmount() {
									return index;
								}
							}, Type.REAL_LONG);
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix subtract(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			final ComplexFloatMatrix	result;
			
			if (content.length == 0) {
				try {
					result = (ComplexFloatMatrix) this.clone();
				} catch (CloneNotSupportedException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
			else {
				result = lineByLineAny(SUBTRACT_FLOAT_ARRAY_NAME, SUBTRACT_FLOAT_ARRAY_KERNEL, content.length, 
						new ControlledDataInput() {
								int 		index = 0;
								
								@Override
								public float readFloat() throws IOException {
									if (index >= content.length) {
										throw new EOFException();
									}
									else if (index < content.length) {
										return content[index++];
									}
									else {
										index++;
										return 0;
									}
								}
			
								@Override
								public long getReadAmount() {
									return index;
								}
							}, Type.REAL_FLOAT);
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix subtract(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			final ComplexFloatMatrix	result;
			
			if (content.length == 0) {
				try {
					result = (ComplexFloatMatrix) this.clone();
				} catch (CloneNotSupportedException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
			else {
				result = lineByLineAny(SUBTRACT_DOUBLE_ARRAY_NAME, SUBTRACT_DOUBLE_ARRAY_KERNEL, content.length, 
						new ControlledDataInput() {
								int 		index = 0;
								
								@Override
								public double readDouble() throws IOException {
									if (index >= content.length) {
										throw new EOFException();
									}
									else if (index < content.length) {
										return content[index++];
									}
									else {
										index++;
										return 0;
									}
								}
			
								@Override
								public long getReadAmount() {
									return index;
								}
							}, Type.REAL_DOUBLE);
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix subtract(final Matrix matrix) {
		if (matrix == null) {
			throw new NullPointerException("Matrix can't be null");
		}
		else if (matrix.numberOfRows() != numberOfRows() || matrix.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Different matrix size: current="+numberOfRows()+'x'+numberOfColumns()+", another="+matrix.numberOfRows()+'x'+matrix.numberOfColumns()); 
		}
		else {
			final ComplexFloatMatrix	result;
			
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
					result = lineByLineAny(SUBTRACT_DOUBLE_ARRAY_NAME, SUBTRACT_DOUBLE_ARRAY_KERNEL, matrix);
					break;
				case COMPLEX_FLOAT 	:
					result = lineByLineAny(SUBTRACT_FLOAT_ARRAY_NAME, SUBTRACT_FLOAT_ARRAY_KERNEL, matrix);
					break;
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG : case BIT :
					throw new IllegalArgumentException("Attempt to subtract real and complex matrix. Use cast() before");
				default : 
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix subtractValue(final int value) {
		return subtractValue((float)value, 0f);
	}

	@Override
	public Matrix subtractValue(final long value) {
		return subtractValue((float)value, 0f);
	}

	@Override
	public Matrix subtractValue(final float value) {
		return subtractValue((float)value, 0f);
	}

	@Override
	public Matrix subtractValue(final float real, final float image) {
		final ComplexFloatMatrix	result;
		
		if (real == 0 && image == 0) {
			try {
				result = (ComplexFloatMatrix) this.clone();
			} catch (CloneNotSupportedException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		else {
			result = lineByLineValue(SUBTRACT_VALUE_NAME, SUBTRACT_VALUE_KERNEL, real, image);
		}
		result.beginTransaction();
		return result;
	}

	@Override
	public Matrix subtractValue(final double value) {
		return subtractValue((float)value, 0f);
	}

	@Override
	public Matrix subtractValue(final double real, final double image) {
		return subtractValue((float)real, (float)image);
	}

	@Override
	public Matrix subtractFrom(int... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractFrom(long... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractFrom(float... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractFrom(double... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractFrom(Matrix matrix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractFromValue(int value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractFromValue(long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractFromValue(float value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractFromValue(float real, float image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractFromValue(double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractFromValue(double real, double image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mul(Matrix matrix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulFrom(Matrix matrix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulValue(int value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulValue(long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulValue(float value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulValue(float real, float image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulValue(double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulValue(double real, double image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix divValue(int value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix divValue(long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix divValue(float value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix divValue(float real, float image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix divValue(double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix divValue(double real, double image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix divFromValue(int value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix divFromValue(long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix divFromValue(float value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix divFromValue(float real, float image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix divFromValue(double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix divFromValue(double real, double image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulHadamard(int... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulHadamard(long... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulHadamard(float... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulHadamard(double... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulHadamard(Matrix matrix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulInvHadamard(int... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulInvHadamard(long... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulInvHadamard(float... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulInvHadamard(double... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulInvHadamard(Matrix matrix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulInvFromHadamard(int... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulInvFromHadamard(long... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulInvFromHadamard(float... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulInvFromHadamard(double... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulInvFromHadamard(Matrix matrix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix tensorMul(Matrix matrix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix tensorMulFrom(Matrix matrix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix invert() {
		if (numberOfRows() != numberOfColumns()) {
			throw new IllegalStateException("Invert can be called for square matrix only");
		}
		else {
			final ComplexFloatMatrix	identity = new ComplexFloatMatrix(getExecutor(), numberOfRows(), numberOfColumns());
			
			identity.apply2((int y, int x, float[] values)->{
				values[0] = x == y ? 1f : 0f;
				values[1] = 0;
			});
			// TODO Auto-generated method stub
			return identity;
		}
	}

	@Override
	public Matrix transpose() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix aggregate(AggregateDirection dir, AggregateType aggType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number det() {
		throw new UnsupportedOperationException("This method can't be called for complex matrices, use det2() instead");
	}

	@Override
	public Number track() {
		throw new UnsupportedOperationException("This method can't be called for complex matrices, use track2() instead");
	}

	@Override
	public Number[] det2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number[] track2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix apply2(final Piece piece, final ApplyFloat2 callback) {
		// TODO Auto-generated method stub
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw new IllegalArgumentException("Piece ["+piece+"] overlaps matrix ranges ["+totalPiece()+"] or has non-positive size");
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null");
		}
		else {
			ensureTransactionCompleted();
			applyAny(piece, new ProcessFCContent() {
				final float[]	temp = new float[2];

				@Override
				public boolean process(int row, int col, ByteBuffer content) throws IOException {
					final int	oldPos = content.position();
					
					deserialize(content, intBuffer, getType().getNumberOfItems());
					temp[0] = Float.intBitsToFloat(intBuffer[0]);
					temp[1] = Float.intBitsToFloat(intBuffer[1]);
					callback.apply(row, col, temp);
					intBuffer[0] = Float.floatToIntBits(temp[0]);
					intBuffer[1] = Float.floatToIntBits(temp[1]);
					content.position(oldPos);
					serialize(intBuffer, getType().getNumberOfItems(), content);
					return true;
				}
			});
			return this;
		}
		
	}
	
	@Override
	protected void lastCall() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected MatrixCalc buildMatrixCalc(final Command... cmds) throws SyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	private int calcNumberOfRows(final int length, final int numberOfItems, final int numberOfColumns) {
		final int	delta = numberOfItems * numberOfColumns;
		int		count = 0;
		int		offset = 0;

		while (offset < length && offset < MAX_GPU_BUFFER_SIZE_IN_ITEMS) {
			offset += delta;
			count++;
		}
		return count;
	}
	
	private int calcTotalNumberOfRows(final int length, final int numberOfItems, final int numberOfColumns) {
		final int	delta = numberOfItems * numberOfColumns;
		int		count = 0;
		int		offset = 0;

		while (offset < length) {
			offset += delta;
			count++;
		}
		return count;
	}
	
	private int calcGPUBufferSize(final Type type, int numberOfRows, int numberOfColumns) {
		final int	delta = type.getNumberOfItems() * numberOfColumns;
		final long 	totalSize = 1L * delta * numberOfRows;
		int		count = 0;
		long	offset = 0;

		while (offset < totalSize && offset < MAX_GPU_BUFFER_SIZE_IN_ITEMS) {
			offset += delta;
			count++;
		}
		return count;
	}

	private ComplexFloatMatrix lineByLineAny(final String programName, final String programCode, final int contentLength, final ControlledDataInput di, final Type inputType) {
		try {
			final GPUExecutable	prog = getOrCreateProgram(programName, programCode);
			final int	leftRows = calcGPUBufferSize(getType(), numberOfRows(), numberOfColumns());
			final int	leftBufferSize = leftRows * numberOfColumns() * getType().getNumberOfItems();
			final int	rightRows = calcNumberOfRows(contentLength, getType().getNumberOfItems(), numberOfColumns());
			final int	rightBufferSize = rightRows * numberOfColumns() * getType().getNumberOfItems();
			final int	totalRightRows = calcTotalNumberOfRows(contentLength, getType().getNumberOfItems(), numberOfColumns());
			final int	occupiedBytes = getType().getNumberOfItems() * getType().getItemSize();
			
			try(final ComplexFloatMatrix	temp = new ComplexFloatMatrix(getExecutor(), getFileKeeper().getParentFile(), 1, 1)) {
				final long[]				taskSize = new long[1]; 
				File						tempStoreFile = null;
				
				temp.beginTransaction();
				try(final GPUBuffer			left = temp.getScheduler().allocateGPUBuffer(leftBufferSize * getType().getItemSize());
					final GPUBuffer			right = temp.getScheduler().allocateGPUBuffer(rightBufferSize * inputType.getItemSize());
					final TemporaryStore	store = temp.getScheduler().allocateTemporaryStore(getFileKeeper().getParentFile(), 1L * occupiedBytes * numberOfRows() * numberOfColumns(), false)) {
					final int 		maxContentLength = ((contentLength + numberOfColumns() - 1) / numberOfColumns()) * numberOfColumns();  

					tempStoreFile = store.getContentFile();
					for (int leftIndex = 0, rightIndex = 0, maxIndex = numberOfRows(); leftIndex < maxIndex; leftIndex += leftRows, rightIndex += rightRows) {
						final int		leftPiece = leftIndex + leftRows > numberOfRows() ? numberOfRows() - leftIndex : leftRows;
						final Piece		currentPiece = Piece.of(leftIndex, 0, leftPiece, numberOfColumns());
						final long		blockSize = occupiedBytes * currentPiece.getWidth() * currentPiece.getHeight();
						
						if (rightIndex < totalRightRows) {
							try(final TemporaryBuffer	out = store.getBuffer(leftIndex * blockSize / leftRows, (int)blockSize);
								final GPUEvent 	downloadEventRight = right.download(di, inputType);
								final GPUEvent 	downloadEventLeft = left.download(currentPiece, this);
								final GPUEvent 	calcEvent = temp.getScheduler().createEvent()) {
								
								downloadEventRight.awaitCurrent();
								downloadEventLeft.awaitCurrent();
								final int		rightPiece = di.getReadAmount() / numberOfColumns() > maxContentLength ? maxContentLength/numberOfColumns() - rightIndex : rightRows;  

								taskSize[0] = Math.min(leftPiece, rightPiece);
								prog.execute(calcEvent, taskSize, numberOfColumns(), left, right);
								calcEvent.awaitCurrent();
								left.upload(out, getType()).awaitCurrent().close();
							}
						}
						else {
							try(final TemporaryBuffer	out = store.getBuffer(leftIndex * blockSize / leftRows, (int)blockSize)) {
								out.write(currentPiece, this);
							}
						}
					}
				}
				return new ComplexFloatMatrix(getExecutor(), getFileKeeper().getParentFile(), numberOfRows(), numberOfColumns(), tempStoreFile, false);
			}
		} catch (ContentException | CalculationException exc) {
			throw new IllegalStateException("Internal error: "+exc.getLocalizedMessage(), exc); 				
		} catch (IOException | InterruptedException exc) {
			throw new IllegalStateException("Internal error: "+exc.getLocalizedMessage(), exc); 				
		}
	}	
	
	private ComplexFloatMatrix lineByLineAny(final String programName, final String programCode, final Matrix matrix) {
		try {
			final GPUExecutable	prog = getOrCreateProgram(programName, programCode);
			final int	leftRows = calcGPUBufferSize(getType(), numberOfRows(), numberOfColumns());
			final int	leftBufferSize = leftRows * numberOfColumns() * getType().getNumberOfItems();
			final int	occupiedBytes = getType().getNumberOfItems() * getType().getItemSize();
			
			try(final ComplexFloatMatrix	temp = new ComplexFloatMatrix(getExecutor(), getFileKeeper().getParentFile(), 1, 1)) {
				final long[]				taskSize = new long[1]; 
				File						tempStoreFile = null;
				
				temp.beginTransaction();
				try(final GPUBuffer			left = temp.getScheduler().allocateGPUBuffer(leftBufferSize * getType().getItemSize());
					final GPUBuffer			right = temp.getScheduler().allocateGPUBuffer(leftBufferSize * matrix.getType().getItemSize());
					final TemporaryStore	store = temp.getScheduler().allocateTemporaryStore(getFileKeeper().getParentFile(), 1L * occupiedBytes * numberOfRows() * numberOfColumns(), false)) {

					tempStoreFile = store.getContentFile();
					for (int leftIndex = 0, maxIndex = numberOfRows(); leftIndex < maxIndex; leftIndex += leftRows) {
						final int		leftPiece = leftIndex + leftRows > numberOfRows() ? numberOfRows() - leftIndex : leftRows;
						final Piece		currentPiece = Piece.of(leftIndex, 0, leftPiece, numberOfColumns());
						final long		blockSize = occupiedBytes * currentPiece.getWidth() * currentPiece.getHeight();
						
						try(final TemporaryBuffer	out = store.getBuffer(leftIndex * blockSize / leftRows, (int)blockSize);
							final GPUEvent 	downloadEventRight = right.download(currentPiece, matrix);
							final GPUEvent 	downloadEventLeft = left.download(currentPiece, this);
							final GPUEvent 	calcEvent = temp.getScheduler().createEvent()) {
							
							downloadEventRight.awaitCurrent();
							downloadEventLeft.awaitCurrent();

							taskSize[0] = leftPiece;
							prog.execute(calcEvent, taskSize, numberOfColumns(), left, right);
							calcEvent.awaitCurrent();
							left.upload(out, getType()).awaitCurrent().close();
						}
					}
				}
				return new ComplexFloatMatrix(getExecutor(), getFileKeeper().getParentFile(), numberOfRows(), numberOfColumns(), tempStoreFile, false);
			}
		} catch (ContentException | CalculationException exc) {
			throw new IllegalStateException("Internal error: "+exc.getLocalizedMessage(), exc); 				
		} catch (IOException | InterruptedException exc) {
			throw new IllegalStateException("Internal error: "+exc.getLocalizedMessage(), exc); 				
		}
	}

	private ComplexFloatMatrix lineByLineValue(final String programName, final String programCode, final float real, final float image) {
		try {
			final GPUExecutable	prog = getOrCreateProgram(programName, programCode);
			final int	leftRows = calcGPUBufferSize(getType(), numberOfRows(), numberOfColumns());
			final int	leftBufferSize = leftRows * numberOfColumns() * getType().getNumberOfItems();
			final int	occupiedBytes = getType().getNumberOfItems() * getType().getItemSize();
			
			try(final ComplexFloatMatrix	temp = new ComplexFloatMatrix(getExecutor(), getFileKeeper().getParentFile(), 1, 1)) {
				final long[]				taskSize = new long[1]; 
				File						tempStoreFile = null;
				
				temp.beginTransaction();
				try(final GPUBuffer			left = temp.getScheduler().allocateGPUBuffer(leftBufferSize * getType().getItemSize());
					final TemporaryStore	store = temp.getScheduler().allocateTemporaryStore(getFileKeeper().getParentFile(), 1L * occupiedBytes * numberOfRows() * numberOfColumns(), false)) {

					tempStoreFile = store.getContentFile();
					for (int leftIndex = 0, maxIndex = numberOfRows(); leftIndex < maxIndex; leftIndex += leftRows) {
						final int		leftPiece = leftIndex + leftRows > numberOfRows() ? numberOfRows() - leftIndex : leftRows;
						final Piece		currentPiece = Piece.of(leftIndex, 0, leftPiece, numberOfColumns());
						final long		blockSize = occupiedBytes * currentPiece.getWidth() * currentPiece.getHeight();
						
						try(final TemporaryBuffer	out = store.getBuffer(leftIndex * blockSize / leftRows, (int)blockSize);
							final GPUEvent 	downloadEventLeft = left.download(currentPiece, this);
							final GPUEvent 	calcEvent = temp.getScheduler().createEvent()) {
							
							downloadEventLeft.awaitCurrent();
							taskSize[0] = leftPiece;
							prog.execute(calcEvent, taskSize, numberOfColumns(), left, real, image);
							calcEvent.awaitCurrent();
							left.upload(out, getType()).awaitCurrent().close();
						}
					}
				}
				return new ComplexFloatMatrix(getExecutor(), getFileKeeper().getParentFile(), numberOfRows(), numberOfColumns(), tempStoreFile, false);
			}
		} catch (ContentException | CalculationException exc) {
			throw new IllegalStateException("Internal error: "+exc.getLocalizedMessage(), exc); 				
		} catch (IOException | InterruptedException exc) {
			throw new IllegalStateException("Internal error: "+exc.getLocalizedMessage(), exc); 				
		}
	}	
	
	private int calcArraySize() {
		return (int) Math.min(Integer.MAX_VALUE, 1L * numberOfRows() * numberOfColumns());
	}

	private int calcArraySize(Piece piece) {
		return (int) Math.min(Integer.MAX_VALUE, 1L * piece.getHeight() * piece.getWidth());
	}

	private GPUExecutable getOrCreateProgram(final String progName, final String kernel) throws SyntaxException {
		if (!getExecutor().hasProgram(progName)) {
			getExecutor().compile(progName, kernel);
		}
		return getExecutor().getProgram(progName);
	}
}
