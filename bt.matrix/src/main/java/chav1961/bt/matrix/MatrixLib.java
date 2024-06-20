package chav1961.bt.matrix;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jocl.CL;
import org.jocl.CLException;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;
import org.jocl.cl_queue_properties;

import chav1961.bt.matrix.Matrix.Type;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class MatrixLib implements AutoCloseable {
	private static final int 		platformIndex = 0;
	private static final long 		deviceType = CL.CL_DEVICE_TYPE_ALL;
	private static final int 		deviceIndex = 0;
	private static final String		PROGRAM_ZERO_NAME = "zeroMatrixKernel";
	private static final String		PROGRAM_ZERO =  "__kernel void \n"
								        + "zeroMatrixKernel(__global float *c)\n"
								        + "{\n"
								        + "    int gid = get_global_id(0);\n"
								        + "    c[gid] = 0;\n"
								        + "}\n";
	private static final String		PROGRAM_IDENTITY_NAME = "identityMatrixKernel";
	private static final String		PROGRAM_IDENTITY =  "__kernel void \n"
										+ "identityMatrixKernel(int dim,\n"
										+ "                  __global float *C){\n"
										+ "    int iCol = get_global_id(0);\n"
										+ "    int iRow = get_global_id(1);\n"
										+ "    C[iRow*dim + iCol] = iCol == iRow ? 1 : 0;\n"
										+ "}\n";
	static final String				PROGRAM_ASSIGN_NAME = "assignMatrixKernel";
	private static final String		PROGRAM_ASSIGN =  "__kernel void \n"
								        + "assignMatrixKernel(__global const float *a,\n"
								        + "                __global float *c)\n"
								        + "{\n"
								        + "    int gid = get_global_id(0);\n"
								        + "    c[gid] = a[gid];\n"
								        + "}\n";
	static final String				PROGRAM_ADD_NAME = "addMatrixKernel";
	private static final String		PROGRAM_ADD =  "__kernel void \n"
								        + "addMatrixKernel(__global const float *a,\n"
								        + "                __global const float *b,\n"
								        + "                __global float *c\n)"
								        + "{\n"
								        + "    int gid = get_global_id(0);\n"
								        + "    c[gid] = a[gid] + b[gid];\n"
								        + "}\n";
	static final String				PROGRAM_SUBTRACT_NAME = "subtractMatrixKernel";
	private static final String		PROGRAM_SUBTRACT =  "__kernel void \n"
								        + "subtractMatrixKernel(__global const float *a,\n"
								        + "                __global const float *b,\n"
								        + "                __global float *c)\n"
								        + "{\n"
								        + "    int gid = get_global_id(0);\n"
								        + "    c[gid] = a[gid] - b[gid];\n"
								        + "}\n";
	static final String				PROGRAM_ADD_SCALAR_NAME = "addScalarKernel";
	private static final String		PROGRAM_ADD_SCALAR =  "__kernel void \n"
										+ "addScalarKernel(__global const float *a,\n"
								        + "                const float scalar,\n"
								        + "                __global float *c)\n"
								        + "{\n"
								        + "    int gid = get_global_id(0);\n"
								        + "    c[gid] = a[gid] + scalar;\n"
								        + "}\n";
	static final String				PROGRAM_MUL_NAME = "mulMatrixKernel";
	private static final String		PROGRAM_MUL =  "__kernel void \n"
										+ "mulMatrixKernel(int dim,\n"
										+ "                  __global const float *A,\n"
										+ "                  __global const float *B,\n"
										+ "                  __global float *C){\n"
										+ "    int iCol = get_global_id(0);\n"
										+ "    int iRow = get_global_id(1);\n"
										+ "    float result = 0.0;\n"
										+ "    for(int i = 0; i < dim; i++) {\n"
										+ "          result += A[iRow*dim + i] * B[i*dim + iCol];\n"
										+ "    }\n"
										+ "    C[iRow*dim + iCol] = result;\n"
										+ "}\n";
	static final String				PROGRAM_MUL_HADAMARD_NAME = "mulHadamardMatrixKernel";
	private static final String		PROGRAM_MUL_HADAMARD =  "__kernel void \n"
								        + "mulHadamardMatrixKernel(__global const float *a,\n"
								        + "                __global const float *b,\n"
								        + "                __global float *c)\n"
								        + "{\n"
								        + "    int gid = get_global_id(0);\n"
								        + "    c[gid] = a[gid] * b[gid];\n"
								        + "}\n";
	static final String				PROGRAM_MUL_SCALAR_NAME = "mulScalarKernel";
	private static final String		PROGRAM_MUL_SCALAR =  "__kernel void \n"
								        + "mulScalarKernel(__global const float *a,\n"
								        + "                const float scalar,\n"
								        + "                __global float *c)\n"
								        + "{"
								        + "    int gid = get_global_id(0);\n"
								        + "    c[gid] = a[gid] * scalar;\n"
								        + "}\n";
	static final String				PROGRAM_MUL_TENZOR_NAME = "mulKronekerMatrixKernel";
	private static final String		PROGRAM_MUL_TENZOR =  "__kernel void \n"
								        + "mulKronekerMatrixKernel(__global const float *a,\n"
								        + "                __global const float *b,\n"
								        + "                __global float *C,\n"
								        + "                const int rowDim,\n"
								        + "                const int colDim,\n"
								        + "                const int bRows,\n"
								        + "                const int bCols)\n"
								        + "{"
								        + "    int aRow = get_global_id(0);\n"
								        + "    int aCol = get_global_id(1);\n"
								        + "    int lineSize = colDim * bCols;\n"
								        + "    float k = a[aRow*colDim + aCol];\n"
								        + "    for (int bRow = 0; bRow < bRows; bRow++) {\n"
								        + "        for (int bCol = 0; bCol < bCols; bCol++) {\n"
								        + "            int rowIndex = aRow*rowDim + bRow;\n"
								        + "            int colIndex = aCol*colDim + bCol;\n"
								        + "            C[rowIndex*lineSize + colIndex] = k * b[bRow*bCols + bCol];\n"
								        + "        }\n"
								        + "    }\n"
								        + "}\n";
	static final String				PROGRAM_TRANSPOSE_NAME = "transposeMatrixKernel";
	private static final String		PROGRAM_TRANSPOSE =  "__kernel void \n"
										+ "transposeMatrixKernel(__global const float *a,\n"
										+ "                  const int dim,\n"
										+ "                  __global float *C){\n"
										+ "    int iCol = get_global_id(0);\n"
										+ "    int iRow = get_global_id(1);\n"
										+ "    C[iRow*dim + iCol] = a[iCol*dim + iRow];\n"
										+ "}\n";
	static final String				PROGRAM_TRACK_NAME = "trackMatrixKernel";
	private static final String		PROGRAM_TRACK =  "__kernel void \n"
										+ "trackMatrixKernel(__global const float *a,\n"
										+ "                  const int dim,\n"
										+ "                  const int groupSize,\n"
										+ "                  __global float *C){\n"
										+ "    int group = get_global_id(0);\n"
										+ "    float sum = 0;\n"
										+ "    for(int i = group; i < dim; i += groupSize) {\n"
										+ "        sum += a[i*dim + i];\n"
										+ "    }\n"
										+ "    C[group] = sum;\n"
										+ "}\n";
	static final String				PROGRAM_DET_TRIANGLE_NAME = "detTriangleMatrixKernel";
	private static final String		PROGRAM_DET_TRIANGLE =  "__kernel void \n"
										+ "detTriangleMatrixKernel(__global const float *a,\n"
										+ "                  const int dim,\n"
										+ "                  const int groupSize,\n"
										+ "                  __global float *C){\n"
										+ "    int group = get_global_id(0);\n"
										+ "    float prod = 1;\n"
										+ "    for(int i = group; i < dim; i += groupSize) {\n"
										+ "        prod *= a[i*dim + i];\n"
										+ "    }\n"
										+ "    C[group] = prod;\n"
										+ "}\n";
	static final String				PROGRAM_DET_REDUCE_NAME = "detReduceMatrixKernel";
	private static final String		PROGRAM_DET_REDUCE =  "__kernel void \n"
										+ "detReduceMatrixKernel(__global float *a,\n"
										+ "                  const int dim,\n"
										+ "                  const int from){\n"
										+ "    __global float *line = a + dim * from;\n"
										+ "    int group = get_global_id(0) + from + 1;\n"
										+ "    float k = a[group * dim + from] / line[from];\n"
										+ "    for(int i = 0; i < dim; i++) {\n"
										+ "        a[group * dim + i] -= k * line[i];\n"
										+ "    }\n"
										+ "}\n";
	static final String				PROGRAM_INV_REDUCE_DOWN_NAME = "invReduceDownMatrixKernel";
	private static final String		PROGRAM_INV_REDUCE_DOWN =  "__kernel void \n"
										+ "invReduceDownMatrixKernel(__global float *a,\n"
										+ "                  const int dim,\n"
										+ "                  const int from){\n"
										+ "    __global float *line = a + dim * from;\n"
										+ "    int group = get_global_id(0) + from + 1;\n"
										+ "    float k = a[group * dim + from] / line[from];\n"
										+ "    for(int i = 0; i < dim; i++) {\n"
										+ "        a[group * dim + i] -= k * line[i];\n"
										+ "    }\n"
										+ "}\n";
	static final String				PROGRAM_INV_REDUCE_UP_NAME = "invReduceUpMatrixKernel";
	private static final String		PROGRAM_INV_REDUCE_UP =  "__kernel void \n"
										+ "invReduceUpMatrixKernel(__global float *a,\n"
										+ "                  const int dim,\n"
										+ "                  const int from){\n"
										+ "    __global float *line = a + dim * from;\n"
										+ "    int group = get_global_id(0) + from + 1;\n"
										+ "    float k = a[group * dim + from] / line[from];\n"
										+ "    for(int i = 0; i < dim; i++) {\n"
										+ "        a[group * dim + i] -= k * line[i];\n"
										+ "    }\n"
										+ "}\n";
	static final String				PROGRAM_INV_NORMALIZE_NAME = "invNormalizeMatrixKernel";
	private static final String		PROGRAM_INV_NORMALIZE =  "__kernel void \n"
										+ "invNormalizeMatrixKernel(__global float *a,\n"
										+ "                  __global float *b,\n"
										+ "                  const int dim){\n"
										+ "    int line = get_global_id(0);\n"
										+ "    float k = 1 / a[line * dim + line];\n"
										+ "    for(int i = 0; i < dim; i++) {\n"
										+ "        a[line * dim + i] *= k;\n"
										+ "        b[line * dim + i] *= k;\n"
										+ "    }\n"
										+ "}\n";

	private static final String[][]	PROGRAM_LIST = new String[][] {
										{PROGRAM_ZERO_NAME, PROGRAM_ZERO},
										{PROGRAM_IDENTITY_NAME, PROGRAM_IDENTITY},
										{PROGRAM_ASSIGN_NAME, PROGRAM_ASSIGN},
										{PROGRAM_ADD_NAME, PROGRAM_ADD},
										{PROGRAM_SUBTRACT_NAME, PROGRAM_SUBTRACT},
										{PROGRAM_ADD_SCALAR_NAME, PROGRAM_ADD_SCALAR},
										{PROGRAM_MUL_NAME, PROGRAM_MUL},
										{PROGRAM_MUL_HADAMARD_NAME, PROGRAM_MUL_HADAMARD},
										{PROGRAM_MUL_SCALAR_NAME, PROGRAM_MUL_SCALAR},
										{PROGRAM_MUL_TENZOR_NAME, PROGRAM_MUL_TENZOR},
										{PROGRAM_TRANSPOSE_NAME, PROGRAM_TRANSPOSE},
										{PROGRAM_TRACK_NAME, PROGRAM_TRACK},
										{PROGRAM_DET_REDUCE_NAME, PROGRAM_DET_REDUCE},
										{PROGRAM_DET_TRIANGLE_NAME, PROGRAM_DET_TRIANGLE},
										{PROGRAM_INV_REDUCE_DOWN_NAME, PROGRAM_INV_REDUCE_DOWN},
										{PROGRAM_INV_REDUCE_UP_NAME, PROGRAM_INV_REDUCE_UP},
										{PROGRAM_INV_NORMALIZE_NAME, PROGRAM_INV_NORMALIZE},
									} ;
	
	private final cl_context 						context;
	private final cl_command_queue 					commandQueue;
	private final Map<String, ProgramDescriptor>	programs = new HashMap<>();
	
	private MatrixLib() {
	    // Enable exceptions and subsequently omit error checks in this sample
	    CL.setExceptionsEnabled(true);

	    // Obtain the number of platforms
	    final int 	numPlatformsArray[] = new int[1];
	    
	    CL.clGetPlatformIDs(0, null, numPlatformsArray);
	    int numPlatforms = numPlatformsArray[0];

	    // Obtain a platform ID
	    final cl_platform_id 	platforms[] = new cl_platform_id[numPlatforms];
	    
	    CL.clGetPlatformIDs(platforms.length, platforms, null);
	    final cl_platform_id 	platform = platforms[platformIndex];

	    // Initialize the context properties
	    final cl_context_properties 	contextProperties = new cl_context_properties();
	    
	    contextProperties.addProperty(CL.CL_CONTEXT_PLATFORM, platform);
	        
	    // Obtain the number of devices for the platform
	    final int 				numDevicesArray[] = new int[1];
	    
	    CL.clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
	    final int 				numDevices = numDevicesArray[0];
	        
	    // Obtain a device ID 
	    final cl_device_id 		devices[] = new cl_device_id[numDevices];
	    
	    CL.clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
	    final cl_device_id 		device = devices[deviceIndex];
		
	    // Create platform context 
		this.context = CL.clCreateContext(contextProperties, 1, new cl_device_id[]{device}, null, null, null);		

		  // Create a command queue for the selected device
		final cl_queue_properties		queueProperties = new cl_queue_properties();
		
		this.commandQueue = CL.clCreateCommandQueue(context, device, 0L, null);
		
		// Prepare programs requested
		for(String[] item : PROGRAM_LIST) {
			programs.put(item[0], new ProgramDescriptor(context, item[0], item[1]));
		}
	}
	
	@Override
	public void close() throws EnvironmentException {
		for(Entry<String, ProgramDescriptor> item : programs.entrySet()) {
			item.getValue().close();
		}
		CL.clReleaseCommandQueue(commandQueue);
		CL.clReleaseContext(context);
	}
	
	public boolean isTypeSupported(final Matrix.Type type) {
		return type == Type.REAL_FLOAT || type == Type.COMPLEX_FLOAT;
	}

	public Matrix getMatrix(final int rows, final int cols) {
		return getMatrix(Type.REAL_FLOAT, rows, cols);
	}	
	
	public Matrix getMatrix(final Matrix.Type type, final int rows, final int cols) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else if (!isTypeSupported(type)) {
			throw new IllegalArgumentException("Matrix type ["+type+"] is not supported with this library");
		}
		else if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0");
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of columns ["+cols+"] must be greater than 0");
		}
		else {
			final long				totalSize = 1L * rows * cols;
			final cl_mem			mem = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, totalSize * Sizeof.cl_float, null, null);

			return new Matrix(this, type, rows, cols, mem);
		}
	}

	public Matrix getZeroMatrix(final int rows, final int cols) {
		return getZeroMatrix(Matrix.Type.REAL_FLOAT, rows, cols);
	}
	
	public Matrix getZeroMatrix(final Matrix.Type type, final int rows, final int cols) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else if (!isTypeSupported(type)) {
			throw new IllegalArgumentException("Matrix type ["+type+"] is not supported with this library");
		}
		else if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0");
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of columns ["+cols+"] must be greater than 0");
		}
		else {
			final ProgramDescriptor	desc = programs.get(PROGRAM_ZERO_NAME);
			final long				totalSize = 1L * rows * cols;
			final cl_mem			mem = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, totalSize * Sizeof.cl_float, null, null);
			final long 				global_work_size[] = new long[]{totalSize};
		    final long 				local_work_size[] = new long[]{1};

		    // Set arguments
			CL.clSetKernelArg(desc.kernel, 0, Sizeof.cl_mem, Pointer.to(mem));
	        // Execute the kernel
			CL.clEnqueueNDRangeKernel(commandQueue, desc.kernel, 1, null, global_work_size, local_work_size, 0, null, null);
			
			return new Matrix(this, type, rows, cols, mem);
		}
	}

	public Matrix getIdentityMatrix(final int rows, final int cols) {
		return getIdentityMatrix(Matrix.Type.REAL_FLOAT, rows, cols);
	}	
	
	public Matrix getIdentityMatrix(final Matrix.Type type, final int rows, final int cols) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else if (!isTypeSupported(type)) {
			throw new IllegalArgumentException("Matrix type ["+type+"] is not supported with this library");
		}
		else if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0");
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of columns ["+cols+"] must be greater than 0");
		}
		else {
			final ProgramDescriptor	desc = programs.get(PROGRAM_IDENTITY_NAME);
			final long				totalSize = 1L * rows * cols;
			final cl_mem			mem = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, totalSize * Sizeof.cl_float, null, null);
			final long 				global_work_size[] = new long[]{rows, cols};
		    final long 				local_work_size[] = new long[]{1, 1};

		    // Set arguments
			CL.clSetKernelArg(desc.kernel, 0, Sizeof.cl_int, Pointer.to(new int[] {rows}));
			CL.clSetKernelArg(desc.kernel, 1, Sizeof.cl_mem, Pointer.to(mem));
	        // Execute the kernel
			CL.clEnqueueNDRangeKernel(commandQueue, desc.kernel, 2, null, global_work_size, local_work_size, 0, null, null);
			
			return new Matrix(this, type, rows, cols, mem);
		}
	}
	
	public Matrix calculate(final String expression, final Matrix... operands) throws SyntaxException, CalculationException {
		if (Utils.checkEmptyOrNullString(expression)) {
			throw new IllegalArgumentException("Expression string cam't be null or empty");
		}
		else if (operands == null || Utils.checkArrayContent4Nulls(operands) >= 0) {
			throw new IllegalArgumentException("Operands list is null or contains nulls inside");
		}
		else {
			try(final Calculator	calc = compile(CharUtils.terminateAndConvert2CharArray(expression, (char)0))) {
				
				return calc.calculate(operands);
			}
		}
	}

	public Calculator compile(final String expression) throws SyntaxException {
		if (Utils.checkEmptyOrNullString(expression)) {
			throw new IllegalArgumentException("Expression string cam't be null or empty");
		}
		else {
			return compile(CharUtils.terminateAndConvert2CharArray(expression, (char)0));
		}
	}

	
	public static MatrixLib getInstance() {
		return new MatrixLib();
	}

	ProgramDescriptor getProgramDescriptor(final String programName) {
		return programs.get(programName);
	}
	
	cl_context getContext() {
		return context;
	}
	
	cl_command_queue getCommandQueue() {
		return commandQueue;
	}

	static Calculator compile(final char[] expression) throws SyntaxException {
		return null;
	}
	
	public static class Calculator implements AutoCloseable {
		private Calculator() {
		}
		
		public Matrix calculate(final Matrix... operands) throws CalculationException {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void close() throws CalculationException {
			// TODO Auto-generated method stub
			
		}
	}
	
	static class ProgramDescriptor implements AutoCloseable {
		final String		programName;
		final cl_program	program;
		final cl_kernel		kernel;
		
		ProgramDescriptor(final cl_context context, final String programName, final String programBody) {
			try {
				this.programName = programName;
				this.program = CL.clCreateProgramWithSource(context, 1, new String[]{ programBody}, null, null);
				
				CL.clBuildProgram(program, 0, null, null, null, null);
				this.kernel = CL.clCreateKernel(program, programName, null);
			} catch (CLException exc) {
				throw new EnvironmentException("Error creating program ["+programName+"]: "+exc.getLocalizedMessage().trim()+"\nProgram code is:\n"+programBody);
			}
		}
		
		@Override
		public void close() throws EnvironmentException {
			try {
				CL.clReleaseKernel(kernel);
			    CL.clReleaseProgram(program);	
			} catch (CLException exc) {
				throw new EnvironmentException("Error closing program ["+programName+"]: "+exc.getLocalizedMessage().trim());
			}
		}
	}
}
