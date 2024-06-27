package chav1961.bt.matrix;

import java.util.Arrays;

import org.jocl.CL;
import org.jocl.CLException;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;

import chav1961.bt.matrix.MatrixLib.ProgramDescriptor;
import chav1961.purelib.basic.exceptions.EnvironmentException;

// https://blogs.oracle.com/javamagazine/post/programming-the-gpu-in-java
public class Matrix implements AutoCloseable {
	public static enum Type {
		REAL_FLOAT,
		COMPLEX_FLOAT,
		REAL_DOUBLE,
		COMPLEX_DOUBLE
	}
	
	private final MatrixLib		lib;
	private final Matrix.Type	type;
	private final int			rows;
	private final int			cols;
	private final cl_mem		memory;
	private volatile boolean	isClosed = false;
	
	Matrix(final MatrixLib lib, final Matrix.Type type, final int rows, final int cols, final cl_mem memory) {
		this.lib = lib;
		this.type = type;
		this.rows = rows;
		this.cols = cols;
		this.memory = memory;
	}
	
	@Override
	public void close() throws EnvironmentException {
		try {
			CL.clReleaseMemObject(memory);
		} catch (CLException exc) {
			throw new EnvironmentException("Error releasing matrix content: "+exc.getLocalizedMessage().trim());
		} finally {
			isClosed = true;
		}
	}
	
	public Matrix.Type type() {
		return type;
	}
	
	public int numberOfRows() {
		return rows;
	}

	public int numberOfColumns() {
		return cols;
	}

	public float[] extract() {
		ensureIsClosed();
		final int		totalSize = numberOfRows() * numberOfColumns();
		final float[]	result = new float[totalSize];
		
        CL.clEnqueueReadBuffer(lib.getCommandQueue(), memory, CL.CL_TRUE, 0, totalSize * Sizeof.cl_float, Pointer.to(result), 0, null, null);
        return result;
	}
	
	public void assign(final float[] content) {
		if (content == null) {
			throw new NullPointerException("Content to assign can't be null");
		}
		else {
			assign(content, 0, content.length - 1);
		}
	}

	public void assign(float[] content, final int from, final int to) {
		if (content == null) {
			throw new NullPointerException("Content to assign can't be null");
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length-1));
		}
		else if (to < 0 || to >= content.length) {
			throw new IllegalArgumentException("To position ["+to+"] out of range 0.."+(content.length-1));
		}
		else if (to < from) {
			throw new IllegalArgumentException("To position ["+to+"] is less than from position ["+from+"]");
		}
		else if (to != from) {
			ensureIsClosed();
			final float[]	piece;
			
			if (from != 0) {
				piece = new float[to - from + 1];
				System.arraycopy(content, from, piece, 0, piece.length);
			}
			else {
				piece = content;
			}
			CL.clEnqueueWriteBuffer(lib.getCommandQueue(), memory, CL.CL_TRUE, 0, (to - from + 1) * Sizeof.cl_float, Pointer.to(piece), 0, null, null);
		}
	}

	public Matrix assign(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to assign can't be null");
		}
		else if (numberOfRows() != another.numberOfRows() || numberOfColumns() != another.numberOfColumns()) {
			throw new IllegalArgumentException("Different matrix size: own "+numberOfRows()+'x'+numberOfColumns()+" and another "+another.numberOfRows()+'x'+another.numberOfColumns());
		}
		else {
			ensureIsClosed();
			final ProgramDescriptor	desc = lib.getProgramDescriptor(MatrixLib.PROGRAM_ASSIGN_NAME);
			final long				totalSize = 1L * numberOfRows() * numberOfColumns();
			final long 				global_work_size[] = new long[]{totalSize};
		    final long 				local_work_size[] = new long[]{1, 1};

		    // Set arguments
			CL.clSetKernelArg(desc.kernel, 0, Sizeof.cl_mem, Pointer.to(another.memory));
			CL.clSetKernelArg(desc.kernel, 1, Sizeof.cl_mem, Pointer.to(memory));
	        // Execute the kernel
			CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), desc.kernel, 1, null, global_work_size, local_work_size, 0, null, null);
			
			return this;
		}
	}
	
	public Matrix add(final double value) {
		ensureIsClosed();
		switch (type) {
			case REAL_FLOAT		:
				return addFloat((float)value);
			case COMPLEX_DOUBLE	:
			case COMPLEX_FLOAT	:
			case REAL_DOUBLE	:
			default :
				throw new UnsupportedOperationException("Marix type ["+type+"] is not supported yet");
		}
	}

	private Matrix addFloat(final float value) {
		final long				totalSize = 1L * numberOfRows() * numberOfColumns();
		final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * Sizeof.cl_float, null, null);

	    executeProgram(lib.getProgramDescriptor(MatrixLib.PROGRAM_ADD_SCALAR_NAME), new long[]{totalSize}, memory, value, newMemory);
		return new Matrix(lib, type(), rows, cols, newMemory);
	}
	
	public Matrix subFrom(final double value) {
		ensureIsClosed();
		final long				totalSize = 1L * numberOfRows() * numberOfColumns();
		final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * Sizeof.cl_float, null, null);

	    executeProgram(lib.getProgramDescriptor(MatrixLib.PROGRAM_SUBTRACT_FROM_SCALAR_NAME), new long[]{totalSize}, memory, (float)value, newMemory);
		return new Matrix(lib, type(), rows, cols, newMemory);
	}
	
	public Matrix add(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to add can't be null");
		}
		else if (another.numberOfRows() != numberOfRows() || another.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Different matrix size: own "+numberOfRows()+'x'+numberOfColumns()+" and another "+another.numberOfRows()+'x'+another.numberOfColumns());
		}
		else {
			ensureIsClosed();
			final long				totalSize = 1L * numberOfRows() * numberOfColumns();
			final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * Sizeof.cl_float, null, null);

		    executeProgram(lib.getProgramDescriptor(MatrixLib.PROGRAM_ADD_NAME), new long[]{totalSize}, memory, another.memory, newMemory);
			return new Matrix(lib, type(), rows, cols, newMemory);
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
			ensureIsClosed();
			final ProgramDescriptor	desc = lib.getProgramDescriptor(MatrixLib.PROGRAM_SUBTRACT_NAME);
			final long				totalSize = 1L * numberOfRows() * numberOfColumns();
			final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * Sizeof.cl_float, null, null);
			final long 				global_work_size[] = new long[]{totalSize};
		    final long 				local_work_size[] = new long[]{1, 1};

		    // Set arguments
			CL.clSetKernelArg(desc.kernel, 0, Sizeof.cl_mem, Pointer.to(memory));
			CL.clSetKernelArg(desc.kernel, 1, Sizeof.cl_mem, Pointer.to(another.memory));
			CL.clSetKernelArg(desc.kernel, 2, Sizeof.cl_mem, Pointer.to(newMemory));
	        // Execute the kernel
			CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), desc.kernel, 1, null, global_work_size, local_work_size, 0, null, null);
			
			return new Matrix(lib, type(), rows, cols, newMemory);
		}
	}

	public Matrix mul(final double value) {
		ensureIsClosed();
		final ProgramDescriptor	desc = lib.getProgramDescriptor(MatrixLib.PROGRAM_MUL_SCALAR_NAME);
		final long				totalSize = 1L * numberOfRows() * numberOfColumns();
		final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * Sizeof.cl_float, null, null);
		final long 				global_work_size[] = new long[]{totalSize};
	    final long 				local_work_size[] = new long[]{1, 1};

	    // Set arguments
		CL.clSetKernelArg(desc.kernel, 0, Sizeof.cl_mem, Pointer.to(memory));
		CL.clSetKernelArg(desc.kernel, 1, Sizeof.cl_float, Pointer.to(new float[] {(float)value}));
		CL.clSetKernelArg(desc.kernel, 2, Sizeof.cl_mem, Pointer.to(newMemory));
        // Execute the kernel
		CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), desc.kernel, 1, null, global_work_size, local_work_size, 0, null, null);
		
		return new Matrix(lib, type(), rows, cols, newMemory);
	}	
	
	public Matrix mul(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to multiply can't be null");
		}
		else if (numberOfColumns() != another.numberOfRows()) {
			throw new IllegalArgumentException("Illegal matrix size: own "+numberOfRows()+'x'+numberOfColumns()+" and another "+another.numberOfRows()+'x'+another.numberOfColumns());
		}
		else {
			ensureIsClosed();
			final ProgramDescriptor	desc = lib.getProgramDescriptor(MatrixLib.PROGRAM_MUL_NAME);
			final long				totalSize = 1L * numberOfRows() * another.numberOfColumns();
			final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * Sizeof.cl_float, null, null);
			final long 				global_work_size[] = new long[]{numberOfRows(), another.numberOfColumns()};
		    final long 				local_work_size[] = new long[]{1, 1};

		    // Set arguments
			CL.clSetKernelArg(desc.kernel, 0, Sizeof.cl_int, Pointer.to(new int[] {numberOfColumns()}));
			CL.clSetKernelArg(desc.kernel, 1, Sizeof.cl_mem, Pointer.to(memory));
			CL.clSetKernelArg(desc.kernel, 2, Sizeof.cl_mem, Pointer.to(another.memory));
			CL.clSetKernelArg(desc.kernel, 3, Sizeof.cl_mem, Pointer.to(newMemory));
	        // Execute the kernel
			CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), desc.kernel, 2, null, global_work_size, local_work_size, 0, null, null);
			
			return new Matrix(lib, type(), rows, cols, newMemory);
		}
	}

	public Matrix mulH(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to Hadamard multiply can't be null");
		}
		else if (another.numberOfRows() != numberOfRows() || another.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Different matrix size: own "+numberOfRows()+'x'+numberOfColumns()+" and another "+another.numberOfRows()+'x'+another.numberOfColumns());
		}
		else {
			ensureIsClosed();
			final ProgramDescriptor	desc = lib.getProgramDescriptor(MatrixLib.PROGRAM_MUL_HADAMARD_NAME);
			final long				totalSize = 1L * numberOfRows() * numberOfColumns();
			final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * Sizeof.cl_float, null, null);
			final long 				global_work_size[] = new long[]{totalSize};
		    final long 				local_work_size[] = new long[]{1, 1};

		    // Set arguments
			CL.clSetKernelArg(desc.kernel, 0, Sizeof.cl_mem, Pointer.to(memory));
			CL.clSetKernelArg(desc.kernel, 1, Sizeof.cl_mem, Pointer.to(another.memory));
			CL.clSetKernelArg(desc.kernel, 2, Sizeof.cl_mem, Pointer.to(newMemory));
	        // Execute the kernel
			CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), desc.kernel, 1, null, global_work_size, local_work_size, 0, null, null);
			
			return new Matrix(lib, type(), rows, cols, newMemory);
		}
	}

	public Matrix mulK(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to Kroneker multiply can't be null");
		}
		else {
			ensureIsClosed();
			final ProgramDescriptor	desc = lib.getProgramDescriptor(MatrixLib.PROGRAM_MUL_TENZOR_NAME);
			final int				totalRows = numberOfRows() * another.numberOfRows();
			final int				totalCols = numberOfColumns() * another.numberOfColumns();
			final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalRows * totalCols * Sizeof.cl_float, null, null);
			final long 				global_work_size[] = new long[]{numberOfRows(), numberOfColumns()};
		    final long 				local_work_size[] = new long[]{1, 1};
			
		    // Set arguments
			CL.clSetKernelArg(desc.kernel, 0, Sizeof.cl_mem, Pointer.to(memory));
			CL.clSetKernelArg(desc.kernel, 1, Sizeof.cl_mem, Pointer.to(another.memory));
			CL.clSetKernelArg(desc.kernel, 2, Sizeof.cl_mem, Pointer.to(newMemory));
			CL.clSetKernelArg(desc.kernel, 3, Sizeof.cl_int, Pointer.to(new int[] {numberOfRows()}));
			CL.clSetKernelArg(desc.kernel, 4, Sizeof.cl_int, Pointer.to(new int[] {numberOfColumns()}));
			CL.clSetKernelArg(desc.kernel, 5, Sizeof.cl_int, Pointer.to(new int[] {another.numberOfRows()}));
			CL.clSetKernelArg(desc.kernel, 6, Sizeof.cl_int, Pointer.to(new int[] {another.numberOfColumns()}));
	        // Execute the kernel
			CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), desc.kernel, 2, null, global_work_size, local_work_size, 0, null, null);
			
			return new Matrix(lib, type(), totalRows, totalCols, newMemory);
		}
	}

	public Matrix inv() {
		if (numberOfRows() != numberOfColumns()) {
			throw new IllegalStateException("Inverted matrix can be calculated for quadratic matrices only");
		}
		else {
			ensureIsClosed();
			try(final Matrix	copy = lib.getMatrix(numberOfRows(), numberOfColumns())) {
				
				copy.assign(this);
				
				final Matrix			identity = lib.getIdentityMatrix(numberOfRows(), numberOfColumns());
				final ProgramDescriptor	divide1 = lib.getProgramDescriptor(MatrixLib.PROGRAM_INV_DIVIDE1_NAME);
				final ProgramDescriptor	divide2 = lib.getProgramDescriptor(MatrixLib.PROGRAM_INV_DIVIDE2_NAME);
				final ProgramDescriptor	subtract = lib.getProgramDescriptor(MatrixLib.PROGRAM_INV_SUBTRACT_NAME);

				for(int index = 0; index < numberOfRows(); index++) {
					invIterate(divide1, divide2, subtract, index, copy, identity);
				}
				return identity;
			}
		}
	}

	public Matrix trans() {
		ensureIsClosed();
		final ProgramDescriptor	desc = lib.getProgramDescriptor(MatrixLib.PROGRAM_TRANSPOSE_NAME);
		final long				totalSize = 1L * numberOfRows() * numberOfColumns();
		final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * Sizeof.cl_float, null, null);
		final long 				global_work_size[] = new long[]{numberOfRows(), numberOfColumns()};
	    final long 				local_work_size[] = new long[]{1, 1};

	    // Set arguments
		CL.clSetKernelArg(desc.kernel, 0, Sizeof.cl_mem, Pointer.to(memory));
		CL.clSetKernelArg(desc.kernel, 1, Sizeof.cl_int, Pointer.to(new int[] {numberOfColumns()}));
		CL.clSetKernelArg(desc.kernel, 2, Sizeof.cl_mem, Pointer.to(newMemory));
        // Execute the kernel
		CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), desc.kernel, 2, null, global_work_size, local_work_size, 0, null, null);
		
		return new Matrix(lib, type(), rows, cols, newMemory);
	}

	public Matrix power(final double power) {
		ensureIsClosed();
		final ProgramDescriptor	desc = lib.getProgramDescriptor(MatrixLib.PROGRAM_POWER_NAME);
		final long				totalSize = 1L * numberOfRows() * numberOfColumns();
		final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * Sizeof.cl_float, null, null);
		final long 				global_work_size[] = new long[]{numberOfRows(), numberOfColumns()};
	    final long 				local_work_size[] = new long[]{1, 1};

	    // Set arguments
		CL.clSetKernelArg(desc.kernel, 0, Sizeof.cl_mem, Pointer.to(memory));
		CL.clSetKernelArg(desc.kernel, 1, Sizeof.cl_int, Pointer.to(new int[] {numberOfColumns()}));
		CL.clSetKernelArg(desc.kernel, 2, Sizeof.cl_float, Pointer.to(new float[] {(float)power}));
		CL.clSetKernelArg(desc.kernel, 3, Sizeof.cl_mem, Pointer.to(newMemory));
        // Execute the kernel
		CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), desc.kernel, 2, null, global_work_size, local_work_size, 0, null, null);
		
		return new Matrix(lib, type(), rows, cols, newMemory);
	}
	
	public double track() {
		if (numberOfRows() != numberOfColumns()) {
			throw new IllegalStateException("Track can be calculated for quadratic matrices only");
		}
		else {
			ensureIsClosed();
			final ProgramDescriptor	desc = lib.getProgramDescriptor(MatrixLib.PROGRAM_TRACK_NAME);
			final int				groupSize = (int)Math.sqrt(numberOfRows());
			final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, groupSize * Sizeof.cl_float, null, null);
			final long 				global_work_size[] = new long[]{groupSize};
		    final long 				local_work_size[] = new long[]{1};

		    try {
			    // Set arguments
				CL.clSetKernelArg(desc.kernel, 0, Sizeof.cl_mem, Pointer.to(memory));
				CL.clSetKernelArg(desc.kernel, 1, Sizeof.cl_int, Pointer.to(new int[] {numberOfColumns()}));
				CL.clSetKernelArg(desc.kernel, 2, Sizeof.cl_int, Pointer.to(new int[] {groupSize}));
				CL.clSetKernelArg(desc.kernel, 3, Sizeof.cl_mem, Pointer.to(newMemory));
		        // Execute the kernel
				CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), desc.kernel, 1, null, global_work_size, local_work_size, 0, null, null);
				
				final float[]		group = new float[groupSize];
		        float	sum = 0;
		        
		        // Extract result and calculate sum
		        CL.clEnqueueReadBuffer(lib.getCommandQueue(), newMemory, CL.CL_TRUE, 0, groupSize * Sizeof.cl_float, Pointer.to(group), 0, null, null);
		        
		        for(float item : group) {
		        	sum += item;
		        }
		        return sum;
		    } finally {
				CL.clReleaseMemObject(newMemory);
		    }
		}
	}

	public double det() {
		if (numberOfRows() != numberOfColumns()) {
			throw new IllegalStateException("Determinant can be calculated for quadratic matrices only");
		}
		else {
			ensureIsClosed();
			try(final Matrix			temp = lib.getMatrix(numberOfRows(), numberOfColumns())) {
				final ProgramDescriptor	descIterate = lib.getProgramDescriptor(MatrixLib.PROGRAM_DET_REDUCE_NAME);
				
				temp.assign(this);	// Make triangle matrix from source;
				for(int index = 0; index < numberOfRows() - 1; index++) {
					detIterate(descIterate, index, temp);
				}
				
				final ProgramDescriptor	desc = lib.getProgramDescriptor(MatrixLib.PROGRAM_DET_TRIANGLE_NAME);
				final int				groupSize = (int)Math.sqrt(numberOfRows());
				final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, groupSize * Sizeof.cl_float, null, null);
				final long 				global_work_size[] = new long[]{groupSize};
			    final long 				local_work_size[] = new long[]{1};
	
			    try {
				    // Set arguments
					CL.clSetKernelArg(desc.kernel, 0, Sizeof.cl_mem, Pointer.to(temp.memory));
					CL.clSetKernelArg(desc.kernel, 1, Sizeof.cl_int, Pointer.to(new int[] {numberOfColumns()}));
					CL.clSetKernelArg(desc.kernel, 2, Sizeof.cl_int, Pointer.to(new int[] {groupSize}));
					CL.clSetKernelArg(desc.kernel, 3, Sizeof.cl_mem, Pointer.to(newMemory));
			        // Execute the kernel
					CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), desc.kernel, 1, null, global_work_size, local_work_size, 0, null, null);
					
					final float[]		group = new float[groupSize];
			        float	prod = 1;
			        
			        // Extract result and calculate prod
			        CL.clEnqueueReadBuffer(lib.getCommandQueue(), newMemory, CL.CL_TRUE, 0, groupSize * Sizeof.cl_float, Pointer.to(group), 0, null, null);
			        
			        for(float item : group) {
			        	prod *= item;
			        }
			        return prod;
			    } finally {
					CL.clReleaseMemObject(newMemory);
			    }
			}
		}
	}
	
	public Matrix cast(final Matrix.Type target) {
		if (target == null) {
			throw new NullPointerException("Target matrix type can't be null");
		}
		else if (!lib.isTypeSupported(target)) {
			throw new IllegalArgumentException("Target matrix type ["+target+"] is not supported by library");
		}
		else if (type() == target){
			return this;
		}
		else {
			return null;
		}
	}

	private void detIterate(final ProgramDescriptor desc, final int index, final Matrix temp) {
		final int				groupSize = numberOfRows() - index - 1;
		final long 				global_work_size[] = new long[]{groupSize};
	    final long 				local_work_size[] = new long[]{1};

	    // Set arguments
		CL.clSetKernelArg(desc.kernel, 0, Sizeof.cl_mem, Pointer.to(temp.memory));
		CL.clSetKernelArg(desc.kernel, 1, Sizeof.cl_int, Pointer.to(new int[] {numberOfColumns()}));
		CL.clSetKernelArg(desc.kernel, 2, Sizeof.cl_int, Pointer.to(new int[] {index}));
        // Execute the kernel
		CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), desc.kernel, 1, null, global_work_size, local_work_size, 0, null, null);
	}

	private void invIterate(final ProgramDescriptor divide1, final ProgramDescriptor divide2, final ProgramDescriptor subtract, final int cell, final Matrix source, final Matrix target) {
		// TODO Auto-generated method stub
		final int	groupSize = numberOfRows();
	    final long 	local_work_size[] = new long[]{1, 1};
		
	    // Set arguments
		CL.clSetKernelArg(divide1.kernel, 0, Sizeof.cl_mem, Pointer.to(source.memory));
		CL.clSetKernelArg(divide1.kernel, 1, Sizeof.cl_mem, Pointer.to(target.memory));
		CL.clSetKernelArg(divide1.kernel, 2, Sizeof.cl_int, Pointer.to(new int[] {groupSize}));
		CL.clSetKernelArg(divide1.kernel, 3, Sizeof.cl_int, Pointer.to(new int[] {cell}));
        // Execute the kernel
		CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), divide1.kernel, 1, null, new long[]{groupSize}, local_work_size, 0, null, null);

	    // Set arguments
		CL.clSetKernelArg(divide2.kernel, 0, Sizeof.cl_mem, Pointer.to(source.memory));
		CL.clSetKernelArg(divide2.kernel, 1, Sizeof.cl_mem, Pointer.to(target.memory));
		CL.clSetKernelArg(divide2.kernel, 2, Sizeof.cl_int, Pointer.to(new int[] {groupSize}));
		CL.clSetKernelArg(divide2.kernel, 3, Sizeof.cl_int, Pointer.to(new int[] {cell}));
        // Execute the kernel
		CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), divide2.kernel, 1, null, new long[] {1}, local_work_size, 0, null, null);

	    // Set arguments
		CL.clSetKernelArg(subtract.kernel, 0, Sizeof.cl_mem, Pointer.to(source.memory));
		CL.clSetKernelArg(subtract.kernel, 1, Sizeof.cl_mem, Pointer.to(target.memory));
		CL.clSetKernelArg(subtract.kernel, 2, Sizeof.cl_int, Pointer.to(new int[] {groupSize}));
		CL.clSetKernelArg(subtract.kernel, 3, Sizeof.cl_int, Pointer.to(new int[] {cell}));
        // Execute the kernel
		CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), subtract.kernel, 2, null, new long[] {groupSize, groupSize}, local_work_size, 0, null, null);
	}

	private void ensureIsClosed() {
		if (isClosed) {
			throw new IllegalStateException("Can't perform oepration - matrix is already closed");
		}
	}

	private void executeProgram(final ProgramDescriptor desc, final long[] workSize, final Object... parameters) {
		int	argNo = 0;
		
		for(Object item : parameters) {
			if (item instanceof cl_mem) {
				CL.clSetKernelArg(desc.kernel, argNo++, Sizeof.cl_mem, Pointer.to((cl_mem)item));
			}
			else if (item instanceof Float) {
				CL.clSetKernelArg(desc.kernel, argNo++, Sizeof.cl_float, Pointer.to(new float[] {((Float)item).floatValue()}));
			}
			else if (item instanceof Integer) {
				CL.clSetKernelArg(desc.kernel, argNo++, Sizeof.cl_int, Pointer.to(new int[] {((Integer)item).intValue()}));
			}
			else {
				throw new UnsupportedOperationException("Unwupported instance class ["+item.getClass()+"]"); 
			}
		}
		CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), desc.kernel, workSize.length, null, workSize, new long[] {1, 1, 1}, 0, null, null);
	}
}
