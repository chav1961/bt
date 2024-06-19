package chav1961.bt.matrix;

import org.jocl.cl_mem;

// https://blogs.oracle.com/javamagazine/post/programming-the-gpu-in-java
public class Matrix implements AutoCloseable {
	public static enum Type {
		REAL_FLOAT,
		COMPLEX_FLOAT,
		REAL_DOUBLE,
		COMPLEX_DOUBLE
	}
	
	private final int		rows;
	private final int		cols;
	private final cl_mem	memory;
	
	Matrix(final int rows, final int cols, final cl_mem memory) {
		this.rows = rows;
		this.cols = cols;
		this.memory = memory;
	}
	
	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public int numberOfRows() {
		return rows;
	}

	public int numberOfColumns() {
		return cols;
	}

	public Matrix add(final double value) {
		return null;
	}
	
	public Matrix add(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to add can't be null");
		}
		else if (another.numberOfRows() != numberOfRows() || another.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Different matrix size: own "+numberOfRows()+'x'+numberOfColumns()+" and another "+another.numberOfRows()+'x'+another.numberOfColumns());
		}
		else {
			return null;
		}
	}

	public Matrix sub(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to subtract can't be null");
		}
		else if (another.numberOfRows() != numberOfRows() || another.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Different matrix size: own "+numberOfRows()+'x'+numberOfColumns()+" and another "+another.numberOfRows()+'x'+another.numberOfColumns());
		}
		else {
			return null;
		}
	}

	public Matrix mul(final double value) {
		return null;
	}	
	
	public Matrix mul(final Matrix another) {
		
//		kernel void matrix_mult( global float4 *a_mat,
//				global float4 *b_mat, global float *c_mat) {
//
//				float sum;
//
//				int num_rows = get_global_size(0);
//				int vectors_per_row = num_rows/4;
//
//				int start = get_global_id(0) * vectors_per_row;
//				a_mat += start;
//				c_mat += start*4;
//
//				for(int i=0; i < num_rows; i++) {
//				    sum = 0.0f;
//				    for(int j=0; < vectors_per_row; j++) {
//				        sum += dot(a_mat[j], b_mat[i*vectors_per_row + j]);
//
//				    }
//				    c_mat[i] = sum;
//				}		
		
		return null;
	}

	public Matrix mulH(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to Hadamard multiply can't be null");
		}
		else if (another.numberOfRows() != numberOfRows() || another.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Different matrix size: own "+numberOfRows()+'x'+numberOfColumns()+" and another "+another.numberOfRows()+'x'+another.numberOfColumns());
		}
		else {
			return null;
		}
	}

	public Matrix mulK(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to Kroneker multiply can't be null");
		}
		else {
			return null;
		}
	}

	public Matrix inv() {
		return null;
	}

	public Matrix trans() {
		return null;
	}

	public Matrix spoor() {
		return null;
	}

	public double det() {
		return 0;
	}
}
