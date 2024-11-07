package chav1961.bt.openclmatrix.ordinal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

import chav1961.bt.openclmatrix.internal.GPUExecutor;
import chav1961.purelib.matrix.AbstractMatrix;
import chav1961.purelib.matrix.interfaces.Matrix;

public class InMemoryComplexFloatMatrix extends AbstractMatrix {
	private final GPUExecutor	executor;
	private final long			totalSize;
	private final float[][]		content;
	
	InMemoryComplexFloatMatrix(final GPUExecutor executor, final Type type, final int rows, final int cols) {
		super(Type.COMPLEX_FLOAT, rows, cols);
		if (executor == null) {
			throw new NullPointerException("GPU executor can't be null");
		}
		else {
			this.executor = executor;
			this.totalSize = 1L * getType().getItemSize() * getType().getNumberOfItems() * rows * cols;
			this.content = new float[rows][getType().getNumberOfItems() * cols];
		}
	}

	@Override public Object clone() throws CloneNotSupportedException{return null;}
	
	@Override
	public boolean deepEquals(Matrix another) {
		// TODO Auto-generated method stub
		return false;
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
			final int[]	result = new int[calculateArraySize(piece)];
	
			apply2(piece, new ApplyFloat2() {
				int	where = 0;
				
				@Override
				public void apply(int row, int col, float[] values) {
					result[where++] = (int)values[0];
					result[where++] = (int)values[1];
				}
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
			try {
				apply2(piece, new ApplyFloat2() {
					@Override
					public void apply(int row, int col, float[] values) {
						try {
							dataOutput.writeInt((int)values[0]);
							dataOutput.writeInt((int)values[1]);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				});
			} catch (RuntimeException exc) {
				if (exc.getCause() instanceof IOException) {
					throw (IOException)exc.getCause(); 
				}
				else {
					throw exc;
				}
			}
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
			final long[]	result = new long[calculateArraySize(piece)];
	
			apply2(piece, new ApplyFloat2() {
				int	where = 0;
				
				@Override
				public void apply(int row, int col, float[] values) {
					result[where++] = (long)values[0];
					result[where++] = (long)values[1];
				}
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
			try {
				apply2(piece, new ApplyFloat2() {
					@Override
					public void apply(int row, int col, float[] values) {
						try {
							dataOutput.writeLong((long)values[0]);
							dataOutput.writeLong((long)values[1]);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				});
			} catch (RuntimeException exc) {
				if (exc.getCause() instanceof IOException) {
					throw (IOException)exc.getCause(); 
				}
				else {
					throw exc;
				}
			}
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
			final float[]	result = new float[calculateArraySize(piece)];
	
			apply2(piece, new ApplyFloat2() {
				int	where = 0;
				
				@Override
				public void apply(int row, int col, float[] values) {
					result[where++] = values[0];
					result[where++] = values[1];
				}
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
			try {
				apply2(piece, new ApplyFloat2() {
					@Override
					public void apply(int row, int col, float[] values) {
						try {
							dataOutput.writeFloat(values[0]);
							dataOutput.writeFloat(values[1]);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				});
			} catch (RuntimeException exc) {
				if (exc.getCause() instanceof IOException) {
					throw (IOException)exc.getCause(); 
				}
				else {
					throw exc;
				}
			}
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
			final double[]	result = new double[calculateArraySize(piece)];
	
			apply2(piece, new ApplyFloat2() {
				int	where = 0;
				
				@Override
				public void apply(int row, int col, float[] values) {
					result[where++] = values[0];
					result[where++] = values[1];
				}
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
			try {
				apply2(piece, new ApplyFloat2() {
					@Override
					public void apply(int row, int col, float[] values) {
						try {
							dataOutput.writeDouble(values[0]);
							dataOutput.writeDouble(values[1]);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				});
			} catch (RuntimeException exc) {
				if (exc.getCause() instanceof IOException) {
					throw (IOException)exc.getCause(); 
				}
				else {
					throw exc;
				}
			}
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
		else {
			try{
				apply2(piece, new ApplyFloat2() {
					int	from = 0;
					
					@Override
					public void apply(int row, int col, float[] values) {
						if (from < content.length - 2) {
							values[0] = content[from++];
							values[1] = content[from++];
						}
						else {
							throw new RuntimeException(new EOFException());
						}
					}
				});
			} catch (RuntimeException exc) {
				if (!(exc.getCause() instanceof EOFException)) {
					throw exc;
				}
			}
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
		else {
			try{
				apply2(piece, new ApplyFloat2() {
					int	from = 0;
					
					@Override
					public void apply(int row, int col, float[] values) {
						if (from < content.length - 2) {
							values[0] = content[from++];
							values[1] = content[from++];
						}
						else {
							throw new RuntimeException(new EOFException());
						}
					}
				});
			} catch (RuntimeException exc) {
				if (!(exc.getCause() instanceof EOFException)) {
					throw exc;
				}
			}
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
		else {
			try{
				apply2(piece, new ApplyFloat2() {
					int	from = 0;
					
					@Override
					public void apply(int row, int col, float[] values) {
						if (from < content.length - 2) {
							values[0] = content[from++];
							values[1] = content[from++];
						}
						else {
							throw new RuntimeException(new EOFException());
						}
					}
				});
			} catch (RuntimeException exc) {
				if (!(exc.getCause() instanceof EOFException)) {
					throw exc;
				}
			}
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
		else {
			try{
				apply2(piece, new ApplyFloat2() {
					int	from = 0;
					
					@Override
					public void apply(int row, int col, float[] values) {
						if (from < content.length - 2) {
							values[0] = (float)content[from++];
							values[1] = (float)content[from++];
						}
						else {
							throw new RuntimeException(new EOFException());
						}
					}
				});
			} catch (RuntimeException exc) {
				if (!(exc.getCause() instanceof EOFException)) {
					throw exc;
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(Piece piece, Matrix matrix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix assign(Piece piece, DataInput content, Type type) throws IOException {
		// TODO Auto-generated method stub
		return null;
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
			apply2(piece, new ApplyFloat2() {
				@Override
				public void apply(int row, int col, float[] values) {
					values[0] = real;
					values[1] = image;
				}
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
	public Matrix cast(Type type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix add(int... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix add(long... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix add(float... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix add(double... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix add(Matrix matrix) {
		// TODO Auto-generated method stub
		return null;
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
	public Matrix addValue(final float value) {
		return addValue(value, 0f);
	}

	@Override
	public Matrix addValue(final float real, final float image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix addValue(final double value) {
		return addValue((float)value, 0f);
	}

	@Override
	public Matrix addValue(final double real, final double image) {
		return addValue((float)real, (float)image);
	}

	@Override
	public Matrix subtract(int... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtract(long... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtract(float... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtract(double... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtract(Matrix matrix) {
		// TODO Auto-generated method stub
		return null;
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
		return subtractValue(value, 0f);
	}

	@Override
	public Matrix subtractValue(float real, float image) {
		// TODO Auto-generated method stub
		return null;
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
	public Matrix subtractFromValue(final int value) {
		return subtractFromValue((float)value, 0f);
	}

	@Override
	public Matrix subtractFromValue(final long value) {
		return subtractFromValue((float)value, 0f);
	}

	@Override
	public Matrix subtractFromValue(final float value) {
		return subtractFromValue(value, 0f);
	}

	@Override
	public Matrix subtractFromValue(float real, float image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractFromValue(final double value) {
		return subtractFromValue((float)value, 0f);
	}

	@Override
	public Matrix subtractFromValue(final double real, final double image) {
		return subtractFromValue((float)real, (float)image);
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
	public Matrix mulValue(final int value) {
		return mulValue((float)value, 0f);
	}

	@Override
	public Matrix mulValue(final long value) {
		return mulValue((float)value, 0f);
	}

	@Override
	public Matrix mulValue(final float value) {
		return mulValue(value, 0f);
	}

	@Override
	public Matrix mulValue(float real, float image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulValue(final double value) {
		return mulValue((float)value, 0f);
	}

	@Override
	public Matrix mulValue(final double real, final double image) {
		return mulValue((float)real, (float)real);
	}

	@Override
	public Matrix divValue(final int value) {
		return divValue((float)value, 0f);
	}

	@Override
	public Matrix divValue(final long value) {
		return divValue((float)value, 0f);
	}

	@Override
	public Matrix divValue(final float value) {
		return divValue(value, 0f);
	}

	@Override
	public Matrix divValue(float real, float image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix divValue(final double value) {
		return divValue((float)value, 0f);
	}

	@Override
	public Matrix divValue(final double real, final double image) {
		return divValue((float)real, (float)image);
	}

	@Override
	public Matrix divFromValue(final int value) {
		return divFromValue((float)value, 0f);
	}

	@Override
	public Matrix divFromValue(final long value) {
		return divFromValue((float)value, 0f);
	}

	@Override
	public Matrix divFromValue(final float value) {
		return divFromValue(value, 0f);
	}

	@Override
	public Matrix divFromValue(float real, float image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix divFromValue(final double value) {
		return divFromValue((float)value, 0f);
	}

	@Override
	public Matrix divFromValue(final double real, final double image) {
		return divFromValue((float)real, (float)image);
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
		// TODO Auto-generated method stub
		return null;
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
		throw new UnsupportedOperationException("Real determinant is not applicable for complex matrix");
	}

	@Override
	public Number track() {
		throw new UnsupportedOperationException("Real track is not applicable for complex matrix");
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
		return super.apply2(piece, callback);
	}
	
	@Override
	protected void lastCall() {
		// TODO Auto-generated method stub
		
	}

	private int calculateArraySize(final Piece piece) {
		return (int) Math.min(Integer.MAX_VALUE, 1L * piece.getWidth() * piece.getHeight() * getType().getNumberOfItems());
	}
}
