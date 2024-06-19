package chav1961.bt.matrix;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jocl.CL;
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

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class MatrixLib implements AutoCloseable {
	private static final int 		platformIndex = 0;
	private static final long 		deviceType = CL.CL_DEVICE_TYPE_ALL;
	private static final int 		deviceIndex = 0;
	private static final String		PROGRAM_ZERO_NAME = "zeroMatrixKernel";
	private static final String		PROGRAM_ZERO =  "__kernel void "
								        + "zeroMatrixKernel(__global float *c)"
								        + "{"
								        + "    int gid = get_global_id(0);"
								        + "    c[gid] = 0;"
								        + "}";
	private static final String		PROGRAM_IDENTITY_NAME = "identityMatrixKernel";
	private static final String		PROGRAM_IDENTITY =  "__kernel void "
										+ "identityMatrixKernel(int dim,"
										+ "                  __global float *C){"
										+ "    int iCol = get_global_id(0);"
										+ "    int iRow = get_global_id(1);"
										+ "    C[iRow*dim + iCol] = iCol == iRow ? 1 : 0;"
										+ "}";
	private static final String		PROGRAM_ASSIGN_NAME = "assignMatrixKernel";
	private static final String		PROGRAM_ASSIGN =  "__kernel void "
								        + "assignMatrixKernel(__global const float *a,"
								        + "                __global float *c)"
								        + "{"
								        + "    int gid = get_global_id(0);"
								        + "    c[gid] = a[gid];"
								        + "}";
	private static final String		PROGRAM_ADD_NAME = "addMatrixKernel";
	private static final String		PROGRAM_ADD =  "__kernel void "
								        + "addMatrixKernel(__global const float *a,"
								        + "                __global const float *b,"
								        + "                __global float *c)"
								        + "{"
								        + "    int gid = get_global_id(0);"
								        + "    c[gid] = a[gid] + b[gid];"
								        + "}";
	private static final String		PROGRAM_SUBTRACT_NAME = "subtractMatrixKernel";
	private static final String		PROGRAM_SUBTRACT =  "__kernel void "
								        + "subtractMatrixKernel(__global const float *a,"
								        + "                __global const float *b,"
								        + "                __global float *c)"
								        + "{"
								        + "    int gid = get_global_id(0);"
								        + "    c[gid] = a[gid] - b[gid];"
								        + "}";
	private static final String		PROGRAM_ADD_SCALAR_NAME = "addScalarKernel";
	private static final String		PROGRAM_ADD_SCALAR =  "__kernel void "
										+ "addScalarKernel(__global const float *a,"
								        + "                __global const float scalar,"
								        + "                __global float *c)"
								        + "{"
								        + "    int gid = get_global_id(0);"
								        + "    c[gid] = a[gid] + scalar;"
								        + "}";
	private static final String		PROGRAM_MUL_NAME = "mulMatrixKernel";
	private static final String		PROGRAM_MUL =  "__kernel void "
										+ "mulMatrixKernel(int dim,"
										+ "                  __global float *A,"
										+ "                  __global float *B,"
										+ "                  __global float *C){"
										+ "    int iCol = get_global_id(0);"
										+ "    int iRow = get_global_id(1);"
										+ "    float result = 0.0;"
										+ "    for(int i = 0; i < dim; i++) {"
										+ "          result += A[iRow*dim + i] * B[i*dim + iCol];"
										+ "    }"
										+ "    C[iRow*dim + iCol] = result;"
										+ "}";
	private static final String		PROGRAM_MUL_HADAMARD_NAME = "mulHadamardMatrixKernel";
	private static final String		PROGRAM_MUL_HADAMARD =  "__kernel void "
								        + "mulHadamardMatrixKernel(__global const float *a,"
								        + "                __global const float *b,"
								        + "                __global float *c)"
								        + "{"
								        + "    int gid = get_global_id(0);"
								        + "    c[gid] = a[gid] * b[gid];"
								        + "}";
	private static final String		PROGRAM_MUL_SCALAR_NAME = "mulScalarKernel";
	private static final String		PROGRAM_MUL_SCALAR =  "__kernel void "
								        + "mulScalarKernel(__global const float *a,"
								        + "                __global const float scalar,"
								        + "                __global float *c)"
								        + "{"
								        + "    int gid = get_global_id(0);"
								        + "    c[gid] = a[gid] * scalar;"
								        + "}";
	private static final String		PROGRAM_TRANSPOSE_NAME = "transposeMatrixKernel";
	private static final String		PROGRAM_TRANSPOSE =  "__kernel void "
										+ "transposeMatrixKernel(int dim,"
										+ "                  __global float *C){"
										+ "    int iCol = get_global_id(0);"
										+ "    int iRow = get_global_id(1);"
										+ "    float temp = C[iRow*dim + iCol];"
										+ "    C[iRow*dim + iCol] = C[iCol*dim + iRow];"
										+ "    C[iCol*dim + iRow] = temp;"
										+ "}";
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
										{PROGRAM_TRANSPOSE_NAME, PROGRAM_TRANSPOSE},
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
		
		this.commandQueue = CL.clCreateCommandQueueWithProperties(context, device, queueProperties, null);
		
		// Prepare programs requested
		for(String[] item : PROGRAM_LIST) {
			programs.put(item[0], new ProgramDescriptor(context, item[0], item[1]));
		}
	}
	
	@Override
	public void close() throws Exception {
		for(Entry<String, ProgramDescriptor> item : programs.entrySet()) {
			item.getValue().close();
		}
		CL.clReleaseCommandQueue(commandQueue);
		CL.clReleaseContext(context);
	}
	
	public boolean isTypeSupported(final Matrix.Type type) {
		return false;
	}
	
	public Matrix getMatrix(final int rows, final int cols) {
		if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0");
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of columns ["+cols+"] must be greater than 0");
		}
		else {
			final long				totalSize = 1L * rows * cols;
			final cl_mem			mem = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, totalSize * Sizeof.cl_float, null, null);

			return new Matrix(rows, cols, mem);
		}
	}

	public Matrix getZeroMatrix(final int rows, final int cols) {
		if (rows <= 0) {
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
			
			return new Matrix(rows, cols, mem);
		}
	}

	public Matrix getIdentityMatrix(final int rows, final int cols) {
		if (rows <= 0) {
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
			
			return new Matrix(rows, cols, mem);
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
		return null;
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
	
	private static class ProgramDescriptor implements AutoCloseable {
		final String		programName;
		final cl_program	program;
		final cl_kernel		kernel;
		
		ProgramDescriptor(final cl_context context, final String programName, final String programBody) {
			this.programName = programName;
			this.program = CL.clCreateProgramWithSource(context, 1, new String[]{ programBody}, null, null);
			
			CL.clBuildProgram(program, 0, null, null, null, null);
			this.kernel = CL.clCreateKernel(program, programName, null);
		}
		
		@Override
		public void close() throws Exception {
			CL.clReleaseKernel(kernel);
		    CL.clReleaseProgram(program);	
		}
	}
}
