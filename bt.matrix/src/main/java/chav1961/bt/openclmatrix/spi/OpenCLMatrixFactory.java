package chav1961.bt.openclmatrix.spi;

import java.net.URI;
import java.util.Hashtable;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.SpiService;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.Type;
import chav1961.purelib.matrix.interfaces.MatrixFactory;

public class OpenCLMatrixFactory implements MatrixFactory, SpiService<MatrixFactory> {
	public static final String		SUBSCHEME = "opencl";
	public static final String		LARGE_MATRIX = "largeMatrix";
	public static final URI			URI_SCHEME = URI.create(MatrixFactory.MATRIX_FACTORY_SCHEME+':'+SUBSCHEME+":/"); 
	public static final long		MAX_MATRIX_SIZE_MB = 128;
	public static final long		MAX_MATRIX_SIZE = MAX_MATRIX_SIZE_MB * 1024 * 1024;
	
	private static final String[]	DEFAULT_LARGE_MATRIX = new String[]{"false"};

	private final OpenCLLargeMatrixFactory	largeMatrixFactory = new OpenCLLargeMatrixFactory();
	
	@Override
	public boolean canServe(final URI resource) throws NullPointerException {
		if (resource == null) {
			throw new NullPointerException("Resource to test can't be null");
		}
		else {
			return URIUtils.canServeURI(resource, URI_SCHEME);
		}
	}

	@Override
	public MatrixFactory newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		if (resource == null) {
			throw new NullPointerException("Resource URI can't be null");
		}
		else if (!canServe(resource)) {
			throw new IllegalArgumentException("Resource ["+resource+"] is not supported with this service");
		}
		else {
			final Hashtable<String, String[]>	query = URIUtils.parseQuery(resource);
			
			if (Boolean.valueOf(query.getOrDefault(LARGE_MATRIX, DEFAULT_LARGE_MATRIX)[0])) {
				return largeMatrixFactory;
			}
			else {
				return this;
			}
		}
	}

	@Override
	public Matrix newMatrix(final Type type, final int rows, final int cols) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else {
			checkMatrixSize(rows, cols, type.getItemSizeInBits() * type.getNumberOfItems());
			
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
			checkMatrixSize(rows, cols, type.getItemSizeInBits() * type.getNumberOfItems());
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
			checkMatrixSize(rows, cols, type.getItemSizeInBits() * type.getNumberOfItems());
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
			checkMatrixSize(rows, cols, type.getItemSizeInBits() * type.getNumberOfItems());
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
			checkMatrixSize(rows, cols, type.getItemSizeInBits() * type.getNumberOfItems());
			final Matrix	result = newMatrix(type, rows, cols);
			
			result.assign(values);
			return result;
		}
	}
	
	private void checkMatrixSize(final int rows, final int cols, final int size) {
		if (1L * rows * cols * size >= MAX_MATRIX_SIZE) {
			throw new IllegalArgumentException("Matrix size "+cols+"x"+rows+" exceeded "+MAX_MATRIX_SIZE_MB+" MByte. Use large matrix mode to get factory ("+URI_SCHEME+"?"+LARGE_MATRIX+"=true)");
		}
	}
}
