package chav1961.bt.openclmatrix.spi;

import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.Type;
import chav1961.purelib.matrix.interfaces.MatrixFactory;

public class OpenCLLargeMatrixFactory implements MatrixFactory {
	@Override
	public Matrix newMatrix(final Type type, final int rows, final int cols) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else {
			switch (type) {
				case BIT			:
					break;
				case COMPLEX_DOUBLE	:
					break;
				case COMPLEX_FLOAT	:
					break;
				case REAL_DOUBLE	:
					break;
				case REAL_FLOAT		:
					break;
				case REAL_INT		:
					break;
				case REAL_LONG		:
					break;
				default:
					throw new UnsupportedOperationException("Matrix type ["+type+"] is not supported yet");
			}
			return null;
		}
	}

	@Override
	public Matrix newMatrix(final Type type, final int rows, final int cols, final int... values) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else if (values == null) {
			throw new NullPointerException("Values can't be null");
		}
		else if (type.getContentClass() != int.class) {
			throw new IllegalArgumentException("Matrix content type ["+type.getContentClass()+"] is not compatible with values type");
		}
		else {
			final Matrix	result = newMatrix(type, rows, cols);
			
			result.assign(values);
			return result;
		}
	}

	@Override
	public Matrix newMatrix(final Type type, final int rows, final int cols, final long... values) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else if (values == null) {
			throw new NullPointerException("Values can't be null");
		}
		else if (type.getContentClass() != long.class) {
			throw new IllegalArgumentException("Matrix content type ["+type.getContentClass()+"] is not compatible with values type");
		}
		else {
			final Matrix	result = newMatrix(type, rows, cols);
			
			result.assign(values);
			return result;
		}
	}

	@Override
	public Matrix newMatrix(final Type type, final int rows, final int cols, final float... values) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else if (values == null) {
			throw new NullPointerException("Values can't be null");
		}
		else if (type.getContentClass() != float.class) {
			throw new IllegalArgumentException("Matrix content type ["+type.getContentClass()+"] is not compatible with values type");
		}
		else {
			final Matrix	result = newMatrix(type, rows, cols);
			
			result.assign(values);
			return result;
		}
	}

	@Override
	public Matrix newMatrix(final Type type, final int rows, final int cols, final double... values) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else if (values == null) {
			throw new NullPointerException("Values can't be null");
		}
		else if (type.getContentClass() != double.class) {
			throw new IllegalArgumentException("Matrix content type ["+type.getContentClass()+"] is not compatible with values type");
		}
		else {
			final Matrix	result = newMatrix(type, rows, cols);
			
			result.assign(values);
			return result;
		}
	}
}
