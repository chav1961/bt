package chav1961.bt.openclmatrix.ordinal;

import java.io.DataInput;
import java.io.IOException;

import chav1961.purelib.matrix.interfaces.Matrix;

public class ComplexDoubleMatrix implements Matrix {
	private final int			rows;
	private final int			cols;
	private final double[][]	content;
	
	public ComplexDoubleMatrix(final int rows, final int cols) {
		if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0");
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of columns ["+cols+"] must be greater than 0");
		}
		else if (2L * rows * cols > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Matrix size is greater than ["+Integer.MAX_VALUE+"] items");
		}
		else {
			this.rows = rows;
			this.cols = cols;
			this.content = new double[rows][2*cols]; 
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
	
	@Override
	public void close() throws RuntimeException {
		// TODO Auto-generated method stub
		
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
		if (another == this) {
			return true;
		}
		else if (another == null) {
			return false;
		}
		else if (another.getType() != getType() || another.numberOfRows() != numberOfRows() || another.numberOfColumns() != numberOfColumns()) {
			return false;
		}
		else {
			// TODO Auto-generated method stub
			return false;
		}
	}

	@Override
	public int[] extractInts(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Pi");
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long[] extractLongs(Piece piece) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float[] extractFloats(Piece piece) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] extractDoubles(Piece piece) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix assign(Piece piece, int... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix assign(Piece piece, long... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix assign(Piece piece, float... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix assign(Piece piece, double... content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix assign(Piece piece, Matrix content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix assign(Piece piece, DataInput content, Type type) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix fill(Piece piece, int value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix fill(Piece piece, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix fill(Piece piece, float value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix fill(Piece piece, float real, float image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix fill(Piece piece, double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix fill(Piece piece, double real, double image) {
		// TODO Auto-generated method stub
		return null;
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
	public Matrix add(Matrix content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix addValue(int value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix addValue(long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix addValue(float value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix addValue(float real, float image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix addValue(double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix addValue(double real, double image) {
		// TODO Auto-generated method stub
		return null;
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
	public Matrix subtract(Matrix content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractValue(int value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractValue(long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractValue(float value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractValue(float real, float image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractValue(double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix subtractValue(double real, double image) {
		// TODO Auto-generated method stub
		return null;
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
	public Matrix subtractFrom(Matrix content) {
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
	public Matrix mul(Matrix content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulFrom(Matrix content) {
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
	public Matrix mulHadamard(Matrix content) {
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
	public Matrix mulInvHadamard(Matrix content) {
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
	public Matrix mulInvFromHadamard(Matrix content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix tensorMul(Matrix content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix tensorMulFrom(Matrix content) {
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number track() {
		// TODO Auto-generated method stub
		return null;
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
	public Matrix apply(Piece piece, ApplyBit callback) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix apply(Piece piece, ApplyInt callback) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix apply(Piece piece, ApplyLong callback) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix apply(Piece piece, ApplyFloat callback) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix apply(Piece piece, ApplyDouble callback) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix apply2(Piece piece, ApplyFloat2 callback) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix apply2(Piece piece, ApplyDouble2 callback) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toHumanReadableString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix done() {
		// TODO Auto-generated method stub
		return null;
	}


}
