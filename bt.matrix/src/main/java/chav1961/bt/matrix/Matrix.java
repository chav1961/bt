package chav1961.bt.matrix;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.jocl.CL;
import org.jocl.CLException;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;

import chav1961.bt.matrix.utils.ProgramDescriptor;
import chav1961.bt.matrix.utils.ProgramRepo;
import chav1961.purelib.basic.exceptions.EnvironmentException;

// https://blogs.oracle.com/javamagazine/post/programming-the-gpu-in-java
public class Matrix implements AutoCloseable {
	public static enum Type {
		REAL_FLOAT(1, Sizeof.cl_float, "RF"),
		COMPLEX_FLOAT(2, Sizeof.cl_float, "CF"),
		REAL_DOUBLE(1, Sizeof.cl_double, "RD"),
		COMPLEX_DOUBLE(2, Sizeof.cl_double, "CD");
		
		private final int		numberOfItems;
		private final int		itemSize;
		private final String	suffix;
		
		private Type(final int numberOfItems, final int itemSize, final String suffix) {
			this.numberOfItems = numberOfItems;
			this.itemSize = itemSize;
			this.suffix = suffix;
		}
		
		public int getNumberOfItems() {
			return numberOfItems;
		}
		
		public int getItemSize() {
			return itemSize;
		}
		
		public String getProgramSuffix() {
			return suffix;
		}
	}

	public static enum AggregateDirection {
		ByRows,
		ByColumns
	}
	
	public static enum AggregateType {
		Sum,
		Avg, 
		Min,
		Max
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
	
	public Matrix.Type getType() {
		return type;
	}
	
	public int numberOfRows() {
		return rows;
	}

	public int numberOfColumns() {
		return cols;
	}

	public boolean deepEquals(final Matrix another) {
		if (this == another) {
			return true;
		}
		else if (another == null) {
			return false;
		}
		else if (getType() != another.getType() || numberOfRows() != another.numberOfRows() || numberOfColumns() != another.numberOfColumns()) {
			return false;
		}
		else {
			if (getType().itemSize == 4) {
				return Arrays.equals(extractFloats(), another.extractFloats());
			}
			else {
				return Arrays.equals(extractDoubles(), another.extractDoubles());
			}
		}
	}
	
	public float[] extractFloats() {
		ensureIsClosed();
		switch (type) {
			case REAL_FLOAT		:
			case COMPLEX_FLOAT	:
				return extractFloat(memory, type, numberOfRows() * numberOfColumns());
			case REAL_DOUBLE	:
			case COMPLEX_DOUBLE	:
				throw new IllegalStateException("Matrix type["+type+"] contains doubles, not floats. Use extractDoubles() instead");
			default :
				throw new UnsupportedOperationException("Matrix type["+type+"] is not supported yet"); 
		}
	}

	public double[] extractDoubles() {
		ensureIsClosed();
		switch (type) {
			case REAL_DOUBLE	:
			case COMPLEX_DOUBLE	:
				return extractDouble(memory, type, numberOfRows() * numberOfColumns());
			case REAL_FLOAT		:
			case COMPLEX_FLOAT	:
				throw new IllegalStateException("Matrix type["+type+"] contains floats, not doubles. Use extractFloats() instead");
			default :
				throw new UnsupportedOperationException("Matrix type["+type+"] is not supported yet"); 
		}
	}

	public Matrix fill(final double value) {
		final long	size = numberOfRows() * numberOfColumns() * type.getNumberOfItems() * type.getItemSize();
		
		ensureIsClosed();
		switch (type) {
			case REAL_DOUBLE	:
				lib.fillMemory(type, memory, size, Pointer.to(new double[] {value}));
				break;
			case COMPLEX_DOUBLE	:
				lib.fillMemory(type, memory, size, Pointer.to(new double[] {value, 0.0f}));
				break;
			case REAL_FLOAT		:
				lib.fillMemory(type, memory, size, Pointer.to(new float[] {(float)value}));
				break;
			case COMPLEX_FLOAT	:
				lib.fillMemory(type, memory, size, Pointer.to(new float[] {(float)value, 0.0f}));
				break;
			default :
				throw new UnsupportedOperationException("Matrix type["+type+"] is not supported yet"); 
		}
		return this;
	}
	
	public Matrix assign(final float[] content) {
		if (content == null) {
			throw new NullPointerException("Content to assign can't be null");
		}
		else {
			assign(content, 0, content.length - 1);
			return this;
		}
	}

	public Matrix assign(final double[] content) {
		if (content == null) {
			throw new NullPointerException("Content to assign can't be null");
		}
		else {
			assign(content, 0, content.length - 1);
			return this;
		}
	}
	
	public Matrix assign(float[] content, final int from, final int to) {
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
		else if (getType().getItemSize() != Sizeof.cl_float) {
			throw new IllegalArgumentException("Attempt to assign floats to double matrix");
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
			CL.clEnqueueWriteBuffer(lib.getCommandQueue(), memory, CL.CL_TRUE, 0, (to - from + 1) * getType().getItemSize(), Pointer.to(piece), 0, null, null);
			return this;
		}
		else {
			return this;
		}
	}

	public Matrix assign(double[] content, final int from, final int to) {
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
		else if (getType().getItemSize() != Sizeof.cl_double) {
			throw new IllegalArgumentException("Attempt to assign doubles to float matrix");
		}
		else if (to != from) {
			ensureIsClosed();
			final double[]	piece;
			
			if (from != 0) {
				piece = new double[to - from + 1];
				System.arraycopy(content, from, piece, 0, piece.length);
			}
			else {
				piece = content;
			}
			final long	contentSize = (to - from + 1) * getType().getItemSize() * getType().getNumberOfItems();
			
			CL.clEnqueueWriteBuffer(lib.getCommandQueue(), memory, CL.CL_TRUE, 0, contentSize, Pointer.to(piece), 0, null, null);
			return this;
		}
		else {
			return this;
		}
	}
	
	public Matrix assign(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to assign can't be null");
		}
		else if (another.type != type) {
			throw new IllegalArgumentException("Incompatible matrix types: own "+getType()+" and another "+another.type);
		}
		else if (numberOfRows() != another.numberOfRows() || numberOfColumns() != another.numberOfColumns()) {
			throw new IllegalArgumentException("Different matrix size: own "+numberOfRows()+'x'+numberOfColumns()+" and another "+another.numberOfRows()+'x'+another.numberOfColumns());
		}
		else {
			ensureIsClosed();
			final long				totalSize = getType().getNumberOfItems() * numberOfRows() * numberOfColumns();

			executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_ASSIGN_NAME), new long[]{totalSize}, another.memory, memory);
			return this;
		}
	}
	
	public Matrix add(final double... value) {
		if (value == null || value.length == 0) {
			 throw new IllegalArgumentException("Values to add can't be null or empty");
		}
		else {
			ensureIsClosed();
			switch (type) {
				case REAL_FLOAT		:
				case COMPLEX_FLOAT	:
					return addFloat(toFloat(value));
				case COMPLEX_DOUBLE	:
				case REAL_DOUBLE	:
				default :
					throw new UnsupportedOperationException("Marix type ["+type+"] is not supported yet");
			}
		}
	}
	
	public Matrix subtractFrom(final double... value) {
		if (value == null || value.length == 0) {
			 throw new IllegalArgumentException("Values to add can't be null or empty");
		}
		else {
			ensureIsClosed();
			switch (type) {
				case REAL_FLOAT		:
				case COMPLEX_FLOAT	:
					return subtractFromFloat(toFloat(value));
				case COMPLEX_DOUBLE	:
				case REAL_DOUBLE	:
				default :
					throw new UnsupportedOperationException("Marix type ["+type+"] is not supported yet");
			}
		}
	}
	
	public Matrix subtractFromFloat(final float[] value) {
		ensureIsClosed();
		final long				totalSize = 1L * numberOfRows() * numberOfColumns();
		final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * getType().getNumberOfItems() * getType().getItemSize(), null, null);

	    executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_SUBTRACT_FROM_SCALAR_NAME), new long[]{totalSize}, memory, value, newMemory);
		return new Matrix(lib, getType(), rows, cols, newMemory);
	}
	
	public Matrix add(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to add can't be null");
		}
		else if (getType() != another.getType()) {
			throw new IllegalArgumentException("Incompatible matrix type ["+another.getType()+"] to add, must be ["+getType()+"]. Try to call cast(...) method before");
		}
		else if (another.numberOfRows() != numberOfRows() || another.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Different matrix size: own "+numberOfRows()+'x'+numberOfColumns()+" and another "+another.numberOfRows()+'x'+another.numberOfColumns());
		}
		else {
			ensureIsClosed();
			final long				totalSize = 1L * numberOfRows() * numberOfColumns();
			final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * getType().getNumberOfItems() * getType().getItemSize(), null, null);

		    executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_ADD_NAME), new long[]{totalSize}, memory, another.memory, newMemory);
			return new Matrix(lib, getType(), rows, cols, newMemory);
		}
	}

	public Matrix subtract(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to subtract can't be null");
		}
		else if (getType() != another.getType()) {
			throw new IllegalArgumentException("Incompatible matrix type ["+another.getType()+"] to subtract, must be ["+getType()+"]. Try to call cast(...) method before");
		}
		else if (another.numberOfRows() != numberOfRows() || another.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Different matrix size: own "+numberOfRows()+'x'+numberOfColumns()+" and another "+another.numberOfRows()+'x'+another.numberOfColumns());
		}
		else {
			ensureIsClosed();
			final long				totalSize = 1L * numberOfRows() * numberOfColumns();
			final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * getType().getNumberOfItems() * getType().getItemSize(), null, null);
			
			executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_SUBTRACT_NAME), new long[]{totalSize}, memory, another.memory, newMemory);
			return new Matrix(lib, getType(), rows, cols, newMemory);
		}
	}
	
	public Matrix mul(final double... value) {
		if (value == null || value.length == 0) {
			 throw new IllegalArgumentException("Values to multiply can't be null or empty");
		}
		else {
			ensureIsClosed();
			switch (type) {
				case REAL_FLOAT		:
				case COMPLEX_FLOAT	:
					return mulFloat(toFloat(value));
				case COMPLEX_DOUBLE	:
				case REAL_DOUBLE	:
				default :
					throw new UnsupportedOperationException("Marix type ["+type+"] is not supported yet");
			}
		}
	}
	
	public Matrix mul(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to multiply can't be null");
		}
		else if (getType() != another.getType()) {
			throw new IllegalArgumentException("Incompatible matrix type ["+another.getType()+"] to multiply, must be ["+getType()+"]. Try to call cast(...) method before");
		}
		else if (numberOfColumns() != another.numberOfRows()) {
			throw new IllegalArgumentException("Illegal matrix size: own "+numberOfRows()+'x'+numberOfColumns()+" and another "+another.numberOfRows()+'x'+another.numberOfColumns());
		}
		else {
			ensureIsClosed();
			final long		totalSize = 1L * numberOfRows() * another.numberOfColumns();
			final cl_mem	newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * getType().getNumberOfItems() * getType().getItemSize(), null, null);
			
			executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_MUL_NAME), new long[]{numberOfRows(), another.numberOfColumns()}, numberOfColumns(), memory, another.memory, newMemory);
			return new Matrix(lib, getType(), numberOfRows(), another.numberOfColumns(), newMemory);
		}
	}

	public Matrix div(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to multiply can't be null");
		}
		else if (getType() != another.getType()) {
			throw new IllegalArgumentException("Incompatible matrix type ["+another.getType()+"] to multiply, must be ["+getType()+"]. Try to call cast(...) method before");
		}
		else if (numberOfColumns() != another.numberOfRows()) {
			throw new IllegalArgumentException("Illegal matrix size: own "+numberOfRows()+'x'+numberOfColumns()+" and another "+another.numberOfRows()+'x'+another.numberOfColumns());
		}
		else {
			ensureIsClosed();
			final long		totalSize = 1L * numberOfRows() * another.numberOfColumns();
			final cl_mem	newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * getType().getNumberOfItems() * getType().getItemSize(), null, null);
			
			executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_MUL_NAME), new long[]{numberOfRows(), another.numberOfColumns()}, numberOfColumns(), memory, another.memory, newMemory);
			return new Matrix(lib, getType(), numberOfRows(), another.numberOfColumns(), newMemory);
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
			final long				totalSize = 1L * numberOfRows() * numberOfColumns();
			final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * getType().getNumberOfItems() * getType().getItemSize(), null, null);

			executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_MUL_HADAMARD_NAME), new long[]{totalSize}, memory, another.memory, newMemory);
			return new Matrix(lib, getType(), rows, cols, newMemory);
		}
	}

	public Matrix divH(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to Hadamard multiply can't be null");
		}
		else if (another.numberOfRows() != numberOfRows() || another.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Different matrix size: own "+numberOfRows()+'x'+numberOfColumns()+" and another "+another.numberOfRows()+'x'+another.numberOfColumns());
		}
		else {
			ensureIsClosed();
			final long				totalSize = 1L * numberOfRows() * numberOfColumns();
			final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * getType().getNumberOfItems() * getType().getItemSize(), null, null);

			executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_MUL_HADAMARD_NAME), new long[]{totalSize}, memory, another.memory, newMemory);
			return new Matrix(lib, getType(), rows, cols, newMemory);
		}
	}
	
	public Matrix mulK(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to Kroneker multiply can't be null");
		}
		else {
			ensureIsClosed();
			final int				totalRows = numberOfRows() * another.numberOfRows();
			final int				totalCols = numberOfColumns() * another.numberOfColumns();
			final long				bufferSize = 1L * totalRows * totalCols * getType().getNumberOfItems() * getType().getItemSize();
			final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, bufferSize, null, null);
			
			executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_MUL_TENZOR_NAME), new long[]{numberOfRows(), numberOfColumns()}, memory, another.memory, newMemory, numberOfRows(), numberOfColumns(), another.numberOfRows(), another.numberOfColumns());
			return new Matrix(lib, getType(), totalRows, totalCols, newMemory);
		}
	}

	public Matrix inv() {
		if (numberOfRows() != numberOfColumns()) {
			throw new IllegalStateException("Inverted matrix can be calculated for quadratic matrices only");
		}
		else {
			ensureIsClosed();
			try(final Matrix	copy = lib.getMatrix(getType(), numberOfRows(), numberOfColumns())) {
				final Matrix	identity = lib.getIdentityMatrix(getType(), numberOfRows(), numberOfColumns());
				
				copy.assign(this);
				
				final ProgramDescriptor	divide1 = lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_INV_DIVIDE1_NAME);
				final ProgramDescriptor	divide2 = lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_INV_DIVIDE2_NAME);
				final ProgramDescriptor	subtract = lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_INV_SUBTRACT_NAME);

				for(int index = 0; index < numberOfRows(); index++) {
					invIterate(divide1, divide2, subtract, index, copy, identity);
				}
				return identity;
			}
		}
	}

	public Matrix trans() {
		ensureIsClosed();
		final long				totalSize = 1L * numberOfRows() * numberOfColumns();
		final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * getType().getNumberOfItems() * getType().getItemSize(), null, null);

		executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_TRANSPOSE_NAME), new long[]{numberOfRows(), numberOfColumns()}, memory, numberOfColumns(), numberOfRows(), newMemory);
		return new Matrix(lib, getType(), numberOfColumns(), numberOfRows(), newMemory);
	}

	//https://cyclowiki.org/wiki/%D0%92%D0%BE%D0%B7%D0%B2%D0%B5%D0%B4%D0%B5%D0%BD%D0%B8%D0%B5_%D0%B2_%D0%BA%D0%BE%D0%BC%D0%BF%D0%BB%D0%B5%D0%BA%D1%81%D0%BD%D1%83%D1%8E_%D1%81%D1%82%D0%B5%D0%BF%D0%B5%D0%BD%D1%8C_%D0%BA%D0%BE%D0%BC%D0%BF%D0%BB%D0%B5%D0%BA%D1%81%D0%BD%D0%BE%D0%B3%D0%BE_%D1%87%D0%B8%D1%81%D0%BB%D0%B0	
	public Matrix power(final double power) {
		throw new UnsupportedOperationException("Not implemented yet");
//		ensureIsClosed();
//		final ProgramDescriptor	desc = lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_POWER_NAME);
//		final long				totalSize = 1L * numberOfRows() * numberOfColumns();
//		final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * Sizeof.cl_float, null, null);
//		final long 				global_work_size[] = new long[]{numberOfRows(), numberOfColumns()};
//	    final long 				local_work_size[] = new long[]{1, 1};
//
//	    // Set arguments
//		CL.clSetKernelArg(desc.kernel, 0, Sizeof.cl_mem, Pointer.to(memory));
//		CL.clSetKernelArg(desc.kernel, 1, Sizeof.cl_int, Pointer.to(new int[] {numberOfColumns()}));
//		CL.clSetKernelArg(desc.kernel, 2, Sizeof.cl_float, Pointer.to(new float[] {(float)power}));
//		CL.clSetKernelArg(desc.kernel, 3, Sizeof.cl_mem, Pointer.to(newMemory));
//        // Execute the kernel
//		CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), desc.kernel, 2, null, global_work_size, local_work_size, 0, null, null);
//		
//		return new Matrix(lib, getType(), rows, cols, newMemory);
	}
	
	public double[] track() {
		if (numberOfRows() != numberOfColumns()) {
			throw new IllegalStateException("Track can be calculated for quadratic matrices only");
		}
		else {
			ensureIsClosed();
			final int				groupSize = (int)Math.sqrt(numberOfRows());
			final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, groupSize * getType().getNumberOfItems() * getType().getItemSize(), null, null);

			try {
				executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_TRACK_NAME), new long[]{groupSize}, memory, numberOfColumns(), groupSize, newMemory);
				
				switch (getType()) {
					case COMPLEX_DOUBLE	:
						return calc(extractDouble(newMemory, getType(), groupSize), new double[] {0.0, 0.0}, (target,source)->{target[0] += source[0]; target[1] += source[1];});
					case COMPLEX_FLOAT	:
						return calc(extractFloat(newMemory, getType(), groupSize), new double[] {0.0, 0.0}, (target,source)->{target[0] += source[0]; target[1] += source[1];});
					case REAL_DOUBLE	:
						return calc(extractDouble(newMemory, getType(), groupSize), new double[] {0.0}, (target,source)->{target[0] += source[0];});
					case REAL_FLOAT		:
						return calc(extractFloat(newMemory, getType(), groupSize), new double[] {0.0}, (target,source)->{target[0] += source[0];});
					default :
						throw new UnsupportedOperationException("Matrix type ["+getType()+"] is not supported yet");
				}
		    } finally {
				CL.clReleaseMemObject(newMemory);
		    }
		}
	}

	public double[] det() {
		if (numberOfRows() != numberOfColumns()) {
			throw new IllegalStateException("Determinant can be calculated for quadratic matrices only");
		}
		else {
			ensureIsClosed();
			try(final Matrix			temp = lib.getMatrix(getType(), numberOfRows(), numberOfColumns())) {
				final ProgramDescriptor	descIterate = lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_DET_REDUCE_NAME);
				
				temp.assign(this);	// Make triangle matrix from source;
				for(int index = 0; index < numberOfRows() - 1; index++) {
					detIterate(descIterate, index, temp);
				}
				
				final int				groupSize = (int)Math.sqrt(numberOfRows());
				final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, groupSize * getType().getNumberOfItems() * getType().getItemSize(), null, null);
	
			    try {
			    	executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_DET_TRIANGLE_NAME), new long[]{groupSize}, temp.memory, numberOfColumns(), groupSize, newMemory);
			    	
					switch (getType()) {
						case COMPLEX_DOUBLE	:
							return calc(extractDouble(newMemory, getType(), groupSize), new double[] {1.0, 0.0}, (target,source)->{
								final double	re = source[0] * target[0] - source[1] * target[1];  
								final double	im = source[0] * target[1] + source[1] * target[0];  
								target[0] = re; 
								target[1] = im;
							});
						case COMPLEX_FLOAT	:
							return calc(extractFloat(newMemory, getType(), groupSize), new double[] {1.0, 0.0}, (target,source)->{
								final double	re = source[0] * target[0] - source[1] * target[1];  
								final double	im = source[0] * target[1] + source[1] * target[0];  
								target[0] = re; 
								target[1] = im;
							});
						case REAL_DOUBLE	:
							return calc(extractDouble(newMemory, getType(), groupSize), new double[] {1.0}, (target,source)->{target[0] *= source[0];});
						case REAL_FLOAT		:
							return calc(extractFloat(newMemory, getType(), groupSize), new double[] {1.0}, (target,source)->{target[0] *= source[0];});
						default :
							throw new UnsupportedOperationException("Matrix type ["+getType()+"] is not supported yet");
					}
			    } finally {
					CL.clReleaseMemObject(newMemory);
			    }
			}
		}
	}
	
	public Matrix aggregate(final Matrix.AggregateDirection dir, final Matrix.AggregateType aggType) {
		if (dir == null) {
			throw new NullPointerException("Aggregate direction can't be null");
		}
		else if (aggType == null) {
			throw new NullPointerException("Aggregate type can't be null");
		}
		else {
			ensureIsClosed();
			switch (dir) {
				case ByColumns	:
					final long		totalColSize = 1L * numberOfColumns();
					final cl_mem	newColMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalColSize * getType().getNumberOfItems() * getType().getItemSize(), null, null);

					switch (aggType) {
						case Avg	:
							executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_AGG_AVG_COL_NAME), new long[]{numberOfColumns()}, memory, numberOfRows(), newColMemory);
							break;
						case Max	:
							executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_AGG_MAX_COL_NAME), new long[]{numberOfColumns()}, memory, numberOfRows(), newColMemory);
							break;
						case Min	:
							executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_AGG_MIN_COL_NAME), new long[]{numberOfColumns()}, memory, numberOfRows(), newColMemory);
							break;
						case Sum	:
							executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_AGG_SUM_COL_NAME), new long[]{numberOfColumns()}, memory, numberOfRows(), newColMemory);
							break;
						default :
							throw new UnsupportedOperationException("Aggregate type ["+aggType+"] is not supported yet");
					}
					return new Matrix(lib, getType(), 1, numberOfColumns(), newColMemory);
				case ByRows		:
					final long		totalRowSize = 1L * numberOfRows();
					final cl_mem	newRowMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalRowSize * getType().getNumberOfItems() * getType().getItemSize(), null, null);

					switch (aggType) {
						case Avg	:
							executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_AGG_AVG_ROW_NAME), new long[]{numberOfRows()}, memory, numberOfColumns(), newRowMemory);
							break;
						case Max	:
							executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_AGG_MAX_ROW_NAME), new long[]{numberOfRows()}, memory, numberOfColumns(), newRowMemory);
							break;
						case Min	:
							executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_AGG_MIN_ROW_NAME), new long[]{numberOfRows()}, memory, numberOfColumns(), newRowMemory);
							break;
						case Sum	:
							executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_AGG_SUM_ROW_NAME), new long[]{numberOfRows()}, memory, numberOfColumns(), newRowMemory);
							break;
						default :
							throw new UnsupportedOperationException("Aggregate type ["+aggType+"] is not supported yet");
					}
					return new Matrix(lib, getType(), numberOfRows(), 1, newRowMemory);
				default :
					throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is not supported yet");
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
		else if (getType() == target){
			return this;
		}
		else {
			throw new UnsupportedOperationException("Not implemented yet");
		}
	}

	public static float[] toFloat(final double[] value) {
		final float[]	result = new float[value.length];
		
		for(int index = 0; index < result.length; index++) {
			result[index] = (float)value[index];
		}
		return result;
	}

	public static double[] toDouble(final float[] value) {
		final double[]	result = new double[value.length];
		
		for(int index = 0; index < result.length; index++) {
			result[index] = value[index];
		}
		return result;
	}
	
	
	private Matrix addFloat(final float[] value) {
		final long				totalSize = 1L * numberOfRows() * numberOfColumns();
		final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * type.getNumberOfItems() * type.getItemSize(), null, null);

	    executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_ADD_SCALAR_NAME), new long[]{totalSize}, memory, value, newMemory);
		return new Matrix(lib, getType(), rows, cols, newMemory);
	}

	private Matrix mulFloat(final float[] value) {
		ensureIsClosed();
		final long				totalSize = 1L * numberOfRows() * numberOfColumns();
		final cl_mem			newMemory = CL.clCreateBuffer(lib.getContext(), CL.CL_MEM_READ_WRITE, totalSize * getType().getNumberOfItems() * getType().getItemSize(), null, null);
	
		executeProgram(lib.getProgramDescriptor(getType(), ProgramRepo.PROGRAM_MUL_SCALAR_NAME), new long[]{totalSize}, memory, value, newMemory);
		return new Matrix(lib, getType(), rows, cols, newMemory);
	}	
	
	private void detIterate(final ProgramDescriptor desc, final int index, final Matrix temp) {
		final int				groupSize = numberOfRows() - index - 1;

		executeProgram(desc, new long[]{groupSize}, temp.memory, numberOfColumns(), index);
	}

	private void invIterate(final ProgramDescriptor divide1, final ProgramDescriptor divide2, final ProgramDescriptor subtract, final int cell, final Matrix source, final Matrix target) {
		final int	groupSize = numberOfRows();
		
		executeProgram(divide1, new long[]{groupSize}, source.memory, target.memory, groupSize, cell);
		executeProgram(divide2, new long[]{1}, source.memory, target.memory, groupSize, cell);
		executeProgram(subtract, new long[]{groupSize, groupSize}, source.memory, target.memory, groupSize, cell);
	}

	private void ensureIsClosed() {
		if (isClosed) {
			throw new IllegalStateException("Can't perform operation - matrix is already closed");
		}
	}

	private void executeProgram(final ProgramDescriptor desc, final long[] workSize, final Object... parameters) {
		int	argNo = 0;
		
		try {
			for(Object item : parameters) {
				if (item instanceof cl_mem) {
					CL.clSetKernelArg(desc.kernel, argNo, Sizeof.cl_mem, Pointer.to((cl_mem)item));
				}
				else if (item instanceof Float) {
					CL.clSetKernelArg(desc.kernel, argNo, Sizeof.cl_float, Pointer.to(new float[] {((Float)item).floatValue()}));
				}
				else if (item instanceof Double) {
					CL.clSetKernelArg(desc.kernel, argNo, Sizeof.cl_double, Pointer.to(new double[] {((Float)item).floatValue()}));
				}
				else if (item instanceof float[]) {
					final int	size = Array.getLength(item);
					
					if (size != type.getNumberOfItems()) {
						throw new IllegalArgumentException("The ["+argNo+"] parameter in the ["+desc.programName+"] program has ["+size+"] number of items, but must have ["+type.getNumberOfItems()+"]");
					}
					else {
						CL.clSetKernelArg(desc.kernel, argNo, size * Sizeof.cl_float, Pointer.to((float[])item));
					}
				}
				else if (item instanceof double[]) {
					final int	size = Array.getLength(item);
					
					if (size != type.getNumberOfItems()) {
						throw new IllegalArgumentException("The ["+argNo+"] parameter in the ["+desc.programName+"] program has ["+size+"] number of items, but must have ["+type.getNumberOfItems()+"]");
					}
					else {
						CL.clSetKernelArg(desc.kernel, argNo, size * Sizeof.cl_double, Pointer.to((float[])item));
					}
				}
				else if (item instanceof Integer) {
					CL.clSetKernelArg(desc.kernel, argNo, Sizeof.cl_int, Pointer.to(new int[] {((Integer)item).intValue()}));
				}
				else {
					throw new UnsupportedOperationException("Unwupported instance class ["+item.getClass()+"]"); 
				}
				argNo++;
			}
		} catch (CLException exc) {
			throw new EnvironmentException("Program execution ["+desc.programName+"] for ["+type+"] type and arg index ["+argNo+"] failed: "+exc.getLocalizedMessage()); 
		}
		try {
			CL.clEnqueueNDRangeKernel(lib.getCommandQueue(), desc.kernel, workSize.length, null, workSize, null, 0, null, null);
			CL.clFinish(lib.getCommandQueue());
		} catch (CLException exc) {
			throw new EnvironmentException("Program execution ["+desc.programName+"] for ["+type+"] type failed: "+exc.getLocalizedMessage()); 
		}
	}

	private float[] extractFloat(final cl_mem memory, final Type type, final int size) {
		final float[] 	result = new float[type.getNumberOfItems() * size];
		
        CL.clEnqueueReadBuffer(lib.getCommandQueue(), memory, CL.CL_TRUE, 0, size * type.getNumberOfItems() * type.getItemSize(), Pointer.to(result), 0, null, null);
		return result;
	}

	private double[] extractDouble(final cl_mem memory, final Type type, final int size) {
		final double[] 	result = new double[type.getNumberOfItems() * size];
		
        CL.clEnqueueReadBuffer(lib.getCommandQueue(), memory, CL.CL_TRUE, 0, size * type.getNumberOfItems() * type.getItemSize(), Pointer.to(result), 0, null, null);
		return result;
	}
	
	@FunctionalInterface
	private static interface FloatProcessor {
		void process(double[] target, float[] source);
	}

	@FunctionalInterface
	private static interface DoubleProcessor {
		void process(double[] target, double[] source);
	}
	
	private static double[] calc(final float[] source, final double[] initial, final FloatProcessor callback) {
		final double[]	result = initial.clone();
		final float[]	current = new float[result.length];
		
		for(int index = 0; index < source.length; index += result.length) {
			for (int innerIndex = 0; innerIndex < current.length; innerIndex++) {
				current[innerIndex] = source[index + innerIndex];
			}
			callback.process(result, current);
		}
		return result;
	}

	private static double[] calc(final double[] source, final double[] initial, final DoubleProcessor callback) {
		final double[]	result = initial.clone();
		final double[]	current = new double[result.length];
		
		for(int index = 0; index < source.length; index += result.length) {
			for (int innerIndex = 0; innerIndex < current.length; innerIndex++) {
				current[innerIndex] = source[index + innerIndex];
			}
			callback.process(result, current);
		}
		return result;
	}
}
