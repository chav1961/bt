package chav1961.bt.openclmatrix.large;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import chav1961.bt.openclmatrix.internal.GPUExecutor;
import chav1961.bt.openclmatrix.internal.InternalUtils;
import chav1961.purelib.matrix.interfaces.Matrix;

public class ComplexDoubleMatrix extends LargeMatrix implements Matrix {
	private final long[]	longBuffer = new long[2];
	
	public ComplexDoubleMatrix(final GPUExecutor executor, final int rows, final int cols) {
		this(executor, InternalUtils.TEMP_DIR_LOCATION, rows, cols);
	}	
	
	public ComplexDoubleMatrix(final GPUExecutor executor, final File contentDir, final int rows, final int cols) {
		super(executor, contentDir, Type.COMPLEX_DOUBLE, rows, cols);
	}

	private ComplexDoubleMatrix(final GPUExecutor executor, final File contentDir, final int rows, final int cols, final File fill) {
		super(executor, contentDir, Type.COMPLEX_DOUBLE, rows, cols, fill);
	}	
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		ensureTransactionCompleted();
		return new ComplexDoubleMatrix(getExecutor(), getFileKeeper().getParentFile(), numberOfRows(), numberOfColumns(), getFileKeeper());
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
			final long[]	temp = new long[2];
			
			ensureTransactionCompleted();
			try(final FileChannel	left = FileChannel.open(getFileKeeper().toPath(), StandardOpenOption.READ);
				final FileChannel	right = FileChannel.open(((ComplexDoubleMatrix)another).getFileKeeper().toPath(), StandardOpenOption.READ)) {

				if (left.size() != right.size()) {
					return false;
				}
				else {
					return scanContentReadOnly2(left, right, Piece.of(0, 0, numberOfRows(), numberOfColumns()), (y, x, leftBuffer, rightBuffer)->{
						deserialize(leftBuffer, longBuffer, 2);
						deserialize(rightBuffer, temp, 2);
						
						return longBuffer[0] == temp[0] && longBuffer[1] == temp[1];
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
			final long	size = 1L * numberOfRows() * numberOfColumns(), maxSize = Integer.MAX_VALUE;
			final int[]	result = new int[(int) Math.min(size, maxSize)];
			
			extractAny(piece, new ProcessFCContent() {
				int index = 0;
			
				public boolean process(final int row, final int col, final ByteBuffer source) throws IOException {
					deserialize(source, longBuffer, 2);
					
					if (index >= result.length - 1) {
						return false;
					}
					else {
						result[index++] = (int)Double.longBitsToDouble(longBuffer[0]);
						result[index++] = (int)Double.longBitsToDouble(longBuffer[1]);
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
			extractAny(piece, (y, x, source) -> {
				deserialize(source, longBuffer, 2);
				dataOutput.writeInt((int)Double.longBitsToDouble(longBuffer[0]));
				dataOutput.writeInt((int)Double.longBitsToDouble(longBuffer[1]));
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
			
			extractAny(piece, new ProcessFCContent() {
				int index = 0;
			
				public boolean process(final int row, final int col, final ByteBuffer source) throws IOException {
					deserialize(source, longBuffer, 2);
					
					if (index >= result.length - 1) {
						return false;
					}
					else {
						result[index++] = (long)Double.longBitsToDouble(longBuffer[0]);
						result[index++] = (long)Double.longBitsToDouble(longBuffer[1]);
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
			extractAny(piece, (y, x, source) -> {
				deserialize(source, longBuffer, 2);
				dataOutput.writeLong((long)Double.longBitsToDouble(longBuffer[0]));
				dataOutput.writeLong((long)Double.longBitsToDouble(longBuffer[1]));
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
			
			extractAny(piece, new ProcessFCContent() {
				int index = 0;
			
				public boolean process(final int row, final int col, final ByteBuffer source) throws IOException {
					deserialize(source, longBuffer, 2);
					
					if (index >= result.length - 1) {
						return false;
					}
					else {
						result[index++] = (float)Double.longBitsToDouble(longBuffer[0]);
						result[index++] = (float)Double.longBitsToDouble(longBuffer[1]);
						return true;
					}
				};
			});
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
			extractAny(piece, (y, x, source) -> {
				deserialize(source, longBuffer, 2);
				dataOutput.writeFloat((float)Double.longBitsToDouble(longBuffer[0]));
				dataOutput.writeFloat((float)Double.longBitsToDouble(longBuffer[1]));
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
			
			extractAny(piece, new ProcessFCContent() {
				int index = 0;
			
				public boolean process(final int row, final int col, final ByteBuffer source ) throws IOException {
					deserialize(source, longBuffer, 2);
					
					if (index >= result.length - 1) {
						return false;
					}
					else {
						result[index++] = Double.longBitsToDouble(longBuffer[0]);
						result[index++] = Double.longBitsToDouble(longBuffer[1]);
						return true;
					}
				};
			});
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
			extractAny(piece, (y, x, source) -> {
				deserialize(source, longBuffer, 2);
				dataOutput.writeDouble(Double.longBitsToDouble(longBuffer[0]));
				dataOutput.writeDouble(Double.longBitsToDouble(longBuffer[1]));
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
			final int[]	counter = new int[] {0};
			
			ensureTransactionCompleted();
			
			assignAny(piece, (x, y, target)->{
				if (counter[0] > content.length - 2) {
					return false;
				}
				else {
					longBuffer[0] = Double.doubleToLongBits(content[counter[0]++]);
					longBuffer[1] = Double.doubleToLongBits(content[counter[0]++]);
					serialize(longBuffer, 2, target);
					return true;
				}
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
			final int[]	counter = new int[] {0};
			
			ensureTransactionCompleted();
			
			assignAny(piece, (x, y, target)->{
				if (counter[0] > content.length - 2) {
					return false;
				}
				else {
					longBuffer[0] = Double.doubleToLongBits(content[counter[0]++]);
					longBuffer[1] = Double.doubleToLongBits(content[counter[0]++]);
					serialize(longBuffer, 2, target);
					return true;
				}
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
			final int[]	counter = new int[] {0};
			
			ensureTransactionCompleted();
			
			assignAny(piece, (x, y, target)->{
				if (counter[0] > content.length - 2) {
					return false;
				}
				else {
					longBuffer[0] = Double.doubleToLongBits(content[counter[0]++]);
					longBuffer[1] = Double.doubleToLongBits(content[counter[0]++]);
					serialize(longBuffer, 2, target);
					return true;
				}
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
			final int[]	counter = new int[] {0};
			
			ensureTransactionCompleted();
			
			assignAny(piece, (x, y, target)->{
				if (counter[0] > content.length - 2) {
					return false;
				}
				else {
					longBuffer[0] = Double.doubleToLongBits(content[counter[0]++]);
					longBuffer[1] = Double.doubleToLongBits(content[counter[0]++]);
					serialize(longBuffer, 2, target);
					return true;
				}
			});
			return this;
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
			final ProcessFCContent	pfc;
			
			switch (type) {
				case COMPLEX_DOUBLE	:
				case REAL_DOUBLE	:
					pfc = (y, x, target)->{
						longBuffer[0] = Double.doubleToLongBits(content.readDouble());
						longBuffer[1] = Double.doubleToLongBits(content.readDouble());
						serialize(longBuffer, 2, target);
						return true;
					};
					break;
				case COMPLEX_FLOAT	:
				case REAL_FLOAT		:
					pfc = (y, x, target)->{
						longBuffer[0] = Double.doubleToLongBits(content.readFloat());
						longBuffer[1] = Double.doubleToLongBits(content.readFloat());
						serialize(longBuffer, 2, target);
						return true;
					};
					break;
				case REAL_INT		:
					pfc = (y, x, target)->{
						longBuffer[0] = Double.doubleToLongBits(content.readInt());
						longBuffer[1] = Double.doubleToLongBits(content.readInt());
						serialize(longBuffer, 2, target);
						return true;
					};
					break;
				case REAL_LONG		:
					pfc = (y, x, target)->{
						longBuffer[0] = Double.doubleToLongBits(content.readLong());
						longBuffer[1] = Double.doubleToLongBits(content.readLong());
						serialize(longBuffer, 2, target);
						return true;
					};
					break;
				default:
					throw new UnsupportedOperationException("Matrix type ["+type+"] is not supported yet"); 
			}
			assignAny(piece, pfc);
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
			
			assignAny(piece, (x, y, target)->{
				longBuffer[0] = Double.doubleToLongBits(real);
				longBuffer[1] = Double.doubleToLongBits(image);
				serialize(longBuffer, 2, target);
				return true;
			});
			return this;
		}
	}

	@Override
	public Matrix cast(Type type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix add(final int... content) {
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
	public Matrix add(final long... content) {
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
	public Matrix add(final float... content) {
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
	public Matrix add(final double... content) {
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
	public Matrix apply2(final Piece piece, final ApplyFloat2 callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply2(final Piece piece, final ApplyDouble2 callback) {
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
			final double[]	temp = new double[2];
			ensureTransactionCompleted();

			applyAny(piece, (y, x, buffer)->{
				final int	position = buffer.position();
				
				deserialize(buffer, longBuffer, 2);
				temp[0] = Double.longBitsToDouble(longBuffer[0]);
				temp[1] = Double.longBitsToDouble(longBuffer[1]);
				callback.apply(y, x, temp);
				longBuffer[0] = Double.doubleToLongBits(temp[0]);
				longBuffer[1] = Double.doubleToLongBits(temp[1]);
				buffer.position(position);
				serialize(longBuffer, 2, buffer);
				return true;
			});
			return this;
		}
	}

	@Override
	public String toString() {
		return "CompleDoubleMatrix["+numberOfRows()+"x"+numberOfColumns()+" based on "+getFileKeeper().getAbsolutePath()+", total size="+(getFileKeeper().length()/S_1GIGABYTE)+"Gb, transaction mode is "+!areAllAsyncCompleted()+"]";
	}
	
	@Override
	public Matrix done() {
		// TODO Auto-generated method stub
		return null;
	}
}
