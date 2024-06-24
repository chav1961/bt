package chav1961.bt.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

/*
 * <expr>::=<add>[{'+'|'-'}<add>...]
 * <add>::=<mul>[{'*'|'**'|'***'}<mul>...]
 * <mul>::=['-']<term>['^'<value>]
 * <term>::={<name>['.'{'T'|'inv'|'det'|'sp'}]|<value>|'('<expr>')'}
 * <name>::='#'<number>
 */

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
	
	private static final char[]		SUFFIX_T = "t".toCharArray();
	private static final int		PREDEF_T = 0;
	private static final char[]		SUFFIX_INV = "inv".toCharArray();
	private static final int		PREDEF_INV = 1;
	private static final char[]		SUFFIX_DET = "det".toCharArray();
	private static final int		PREDEF_DET = 2;
	private static final char[]		SUFFIX_SP = "sp".toCharArray();
	private static final int		PREDEF_SP = 3;
									
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
		final Lexema[]	parsed = parse(expression);
		final SyntaxNode<Operation, SyntaxNode<?,?>>	root = new SyntaxNode<>(0, 0, Operation.UNKNOWN, 0, null);
		final int		theEnd = buildTree(OperType.ADD, parsed, 0, root); 

		if (parsed[theEnd].type != LexType.EOF) {
			throw new SyntaxException(0, parsed[theEnd].pos, "Unparsed tail");
		}
		else {
			final List<Command>	commands = new ArrayList<>();
			
			buildCommands(root, commands);
		}
		return null;
	}
	
	static Lexema[] parse(final char[] source) throws SyntaxException {
		final double[]		forValues = new double[1];
		final List<Lexema>	result =  new ArrayList<>();
		int		from = 0;
		
loop:	for (;;) {
			while (source[from] <= ' ' && source[from] != '\0') {
				from++;
			}
			switch (source[from]) {
				case '\0' :
					result.add(new Lexema(from, LexType.EOF));
					break loop;
				case '+' :
					result.add(new Lexema(from++, LexType.PLUS));
					break;
				case '-' :
					result.add(new Lexema(from++, LexType.MINUS));
					break;
				case '.' :
					result.add(new Lexema(from++, LexType.DOT));
					break;
				case '^' :
					result.add(new Lexema(from++, LexType.POWER));
					break;
				case '*' :
					if (source[from + 1] == '*') {
						if (source[from + 2] == '*') {
							result.add(new Lexema(from, LexType.MUL_K));
							from += 3;
						}
						else {
							result.add(new Lexema(from, LexType.MUL_H));
							from += 2;
						}
					}
					else {
						result.add(new Lexema(from++, LexType.MUL));
					}
					break;
				case '#' :
					int		value = 0;
					
					while (source[++from] >= '0' && source[from] <= '9') {
						value = 10 * value + source[from] - '0';
					}
					result.add(new Lexema(from, LexType.NAME, value));
					break;
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					from = CharUtils.parseDouble(source, from, forValues, false);
					result.add(new Lexema(from, LexType.VALUE, Double.doubleToLongBits(forValues[0])));
					break;
				default :
					if (Character.isJavaIdentifierStart(source[from])) {
						if (CharUtils.compare(source, from, SUFFIX_T)) {
							result.add(new Lexema(from, LexType.PREDEFINED, PREDEF_T));
							from += SUFFIX_T.length;
						}
						else if (CharUtils.compare(source, from, SUFFIX_INV)) {
							result.add(new Lexema(from, LexType.PREDEFINED, PREDEF_INV));
							from += SUFFIX_INV.length;
						}
						else if (CharUtils.compare(source, from, SUFFIX_DET)) {
							result.add(new Lexema(from, LexType.PREDEFINED, PREDEF_DET));
							from += SUFFIX_DET.length;
						}
						else if (CharUtils.compare(source, from, SUFFIX_SP)) {
							result.add(new Lexema(from, LexType.PREDEFINED, PREDEF_SP));
							from += SUFFIX_SP.length;
						}
						else {
							throw new SyntaxException(0, from, "Unknown lexema");
						}
					}
					else {
						throw new SyntaxException(0, from, "Unknown lexema");
					}
			}
		}
		return result.toArray(new Lexema[result.size()]);
	}

	static int buildTree(final OperType type, final Lexema[] source, int from, final SyntaxNode<Operation, SyntaxNode<?, ?>> node) throws SyntaxException {
		// TODO Auto-generated method stub
		switch (type) {
			case ADD	:
				from = buildTree(OperType.MUL, source, from, node);
				if (source[from].type == LexType.PLUS || source[from].type == LexType.MINUS) {
					final List<SyntaxNode<Operation, SyntaxNode<?, ?>>>	list = new ArrayList<>();
					final List<LexType>	opers = new ArrayList<>();
					
					list.add((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.clone());
					do {
						final SyntaxNode<Operation, SyntaxNode<?, ?>>	right = (SyntaxNode<Operation, SyntaxNode<?, ?>>) node.clone();
						opers.add(source[from].type);
						
						from = buildTree(OperType.MUL, source, from + 1, right);
					} while (source[from].type == LexType.PLUS || source[from].type == LexType.MINUS);
					node.type = Operation.ADD;
					node.cargo = opers.toArray(new LexType[opers.size()]);
					node.children = list.toArray(new SyntaxNode[list.size()]);
				}
				break;
			case MUL	:
				from = buildTree(OperType.UNARY, source, from, node);
				if (source[from].type == LexType.MUL || source[from].type == LexType.MUL_H || source[from].type == LexType.MUL_K) {
					final List<SyntaxNode<Operation, SyntaxNode<?, ?>>>	list = new ArrayList<>();
					final List<LexType>	opers = new ArrayList<>();
					
					list.add((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.clone());
					do {
						final SyntaxNode<Operation, SyntaxNode<?, ?>>	right = (SyntaxNode<Operation, SyntaxNode<?, ?>>) node.clone();
						opers.add(source[from].type);
						
						from = buildTree(OperType.UNARY, source, from + 1, right);
					} while (source[from].type == LexType.MUL || source[from].type == LexType.MUL_H || source[from].type == LexType.MUL_K);
					node.type = Operation.MUL;
					node.cargo = opers.toArray(new LexType[opers.size()]);
					node.children = list.toArray(new SyntaxNode[list.size()]);
				}
				break;
			case UNARY	:
				if (source[from].type == LexType.PLUS) {
					from = buildTree(OperType.TERM, source, from + 1, node);
				}
				else if (source[from].type == LexType.MINUS) {
					final SyntaxNode<Operation, SyntaxNode<?, ?>>	child = (SyntaxNode<Operation, SyntaxNode<?, ?>>) node.clone();
					
					from = buildTree(OperType.TERM, source, from + 1, child);
					node.type = Operation.NEGATE;
					node.cargo = null;
					node.children = new SyntaxNode[] {child};
				}
				else {
					from = buildTree(OperType.TERM, source, from, node);
				}
				if (source[from].type == LexType.POWER) {
					if (source[from + 1].type == LexType.VALUE) {
						final SyntaxNode<Operation, SyntaxNode<?, ?>>	child = (SyntaxNode<Operation, SyntaxNode<?, ?>>) node.clone();
						
						node.type = Operation.POWER;
						node.cargo = null;
						node.value = source[from + 1].value;
						node.children = new SyntaxNode[] {child};
						from += 2;
					}
					else {
						throw new SyntaxException(0, source[from].pos, "Power (^) requires number at the second operand");
					}
				}
				break;
			case TERM	:
				switch (source[from].type) {
					case VALUE	:
						node.type = Operation.LOAD_VALUE;
						node.value = source[from].value;
						from++;
						break;
					case NAME	:
						final long	index = source[from].value;
						
						if (source[from + 1].type == LexType.DOT) {
							if (source[from + 2].type == LexType.PREDEFINED) {
								final SyntaxNode<Operation, SyntaxNode<?, ?>>	child = (SyntaxNode<Operation, SyntaxNode<?, ?>>) node.clone();

								switch ((int)source[from + 2].value) {
									case PREDEF_T	:
										node.type = Operation.TRANSPOSE;
										break;
									case PREDEF_INV	:
										node.type = Operation.INVERT;
										break;
									case PREDEF_DET	:
										node.type = Operation.DET;
										break;
									case PREDEF_SP	:
										node.type = Operation.SPOOR;
										break;
									default :
										throw new UnsupportedOperationException("Predefined code ["+source[from + 2].value+"] is not supported yet");
								}
								node.cargo = null;
								node.value = source[from + 1].value;
								node.children = new SyntaxNode[] {child};
								from += 3;
							}
							else {
								throw new SyntaxException(0, source[from].pos, "Missing predefined name");
							}
						}
						else {
							node.type = Operation.LOAD_MATRIX;
							node.value = index;
							from++;
						}
						break;
					case OPEN	:
						from = buildTree(OperType.ADD, source, from + 1, node);
						if (source[from].type == LexType.CLOSE) {
							from++;
						}
						else {
							throw new SyntaxException(0, source[from].pos, "Missing ')'");
						}
						break;
					default:
						throw new SyntaxException(0, source[from].pos, "Missing operand");
				}
				break;
			default :
				throw new UnsupportedOperationException("Oper type ["+type+"] is not supported yet"); 
		}
		return from;
	}

	static void buildCommands(final SyntaxNode<Operation, SyntaxNode<?, ?>> node, final List<Command> commands) throws SyntaxException {
		switch (node.type) {
			case ADD 		:
				final LexType[]	addOpers = (LexType[])node.cargo;
				
				if (calcOperandType(node) == OperandType.VALUE) {					
					for(int index = 0; index < node.children.length; index++) {
						buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[index], commands);
						if (index > 0) {
							if (addOpers[index-1] == LexType.PLUS) {
								commands.add(new Command(Operation.ADD_VAL, 0));
							}
							else if (addOpers[index-1] == LexType.MINUS) {
								commands.add(new Command(Operation.SUB_VAL, 0));
							}
							else {
								throw new UnsupportedOperationException("Lex type ["+addOpers[index-1]+"] is not supported yet");
							}
						}
					}
				}
				else {
					OperandType	current = calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]);
					
					buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
					for(int index = 1; index < node.children.length; index++) {
						OperandType	next = calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[index]);
						
						buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[index], commands);
						if (current == OperandType.VALUE && next == OperandType.VALUE) {
							commands.add(new Command(addOpers[index - 1] == LexType.PLUS ? Operation.ADD_VAL : Operation.SUB_VAL, 0));
						}
						else if (current == OperandType.VALUE && next == OperandType.MATRIX) {
							commands.add(new Command(addOpers[index - 1] == LexType.PLUS ? Operation.ADD_VAL_MATRIX : Operation.SUB_VAL_MATRIX, 0));
							current = OperandType.MATRIX;
						}
						else if (current == OperandType.MATRIX && next == OperandType.VALUE) {
							commands.add(new Command(addOpers[index - 1] == LexType.PLUS ? Operation.ADD_MATRIX_VAL : Operation.SUB_MATRIX_VAL, 0));
							current = OperandType.MATRIX;
						}
						else {
							commands.add(new Command(addOpers[index - 1] == LexType.PLUS ? Operation.ADD : Operation.SUB, 0));
						}
					}
				}
				break;
			case MUL 		:
				final LexType[]	mulOpers = (LexType[])node.cargo;
				
				if (calcOperandType(node) == OperandType.VALUE) {
					for(int index = 0; index < node.children.length; index++) {
						buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[index], commands);
						if (index > 0) {
							if (mulOpers[index-1] == LexType.MUL) {
								commands.add(new Command(Operation.MUL_VAL, 0));
							}
							else {
								throw new UnsupportedOperationException("Lex type ["+mulOpers[index-1]+"] is not supported yet");
							}
						}
					}
				}
				else {
					OperandType	current = calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]);
				
					buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
					for(int index = 1; index < node.children.length; index++) {
						OperandType	next = calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[index]);
						
						buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[index], commands);
						if (current == OperandType.VALUE && next == OperandType.VALUE) {
							if (mulOpers[index-1] == LexType.MUL) {
								commands.add(new Command(Operation.MUL_VAL, 0));
							}
							else {
								throw new SyntaxException(0, node.col, "This operation is applicable for matrices only");
							}
						}
						else if (current == OperandType.VALUE && next == OperandType.MATRIX) {
							if (mulOpers[index-1] == LexType.MUL) {
								commands.add(new Command(Operation.MUL_VAL_MATRIX, 0));
								current = OperandType.MATRIX;
							}
							else {
								throw new SyntaxException(0, node.col, "This operation is applicable for matrices only");
							}
						}
						else if (current == OperandType.MATRIX && next == OperandType.VALUE) {
							if (mulOpers[index-1] == LexType.MUL) {
								commands.add(new Command(Operation.MUL_MATRIX_VAL, 0));
								current = OperandType.MATRIX;
							}
							else {
								throw new SyntaxException(0, node.col, "This operation is applicable for matrices only");
							}
						}
						else {
							commands.add(new Command(mulOpers[index-1] == LexType.MUL ? Operation.MUL : (mulOpers[index-1] == LexType.MUL_H ? Operation.MUL_H : Operation.MUL_K), 0));
						}
					}
				}
				break;
			case DET		:
				buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					commands.add(new Command(Operation.DET, 0));
				}
				break;
			case INVERT		:
				buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					commands.add(new Command(Operation.INVERT, 0));
				}
				break;
			case LOAD_MATRIX:
				commands.add(new Command(Operation.LOAD_MATRIX, node.value));
				break;
			case LOAD_VALUE	:
				commands.add(new Command(Operation.LOAD_VALUE, node.value));
				break;
			case NEGATE		:
				buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					commands.add(new Command(Operation.NEGATE, 0));
				}
				else {
					commands.add(new Command(Operation.MINUS, 0));
				}
				break;
			case POWER		:
				buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					commands.add(new Command(Operation.POWER, node.value));
				}
				else {
					commands.add(new Command(Operation.POWER_VAL, node.value));
				}
				break;
			case SPOOR		:
				buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					commands.add(new Command(Operation.SPOOR, 0));
				}
				break;
			case ADD_VAL : case MUL_VAL : case SUB_VAL : case MUL_H : case MUL_K : case SUB :
				throw new SyntaxException(0, node.col, "Unwaited node type"); 
			case TRANSPOSE	:
				buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					commands.add(new Command(Operation.TRANSPOSE, 0));
				}
				break;
			case UNKNOWN	:
				throw new SyntaxException(0, node.col, "Unknown node type"); 
			default :
				throw new UnsupportedOperationException("Node type ["+node.type+"] is not supported yet");
		}
	}
	
	private static OperandType calcOperandType(final SyntaxNode<Operation, SyntaxNode<?, ?>> node) throws SyntaxException {
		switch (node.type) {
			case ADD : case MUL :
				for(SyntaxNode item : node.children) {
					if (calcOperandType(item) == OperandType.MATRIX) {
						return OperandType.MATRIX;
					}
				}
				return OperandType.VALUE;
			case DET		:
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					return OperandType.VALUE;
				}
				else {
					throw new SyntaxException(0, node.col, "Determinant doesn't have matrix parameters"); 
				}
			case INVERT		:
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					return OperandType.MATRIX;
				}
				else {
					throw new SyntaxException(0, node.col, "Inversion doesn't have matrix parameters"); 
				}
			case LOAD_MATRIX:
				return OperandType.MATRIX;
			case LOAD_VALUE	:
				return OperandType.VALUE;
			case NEGATE		:
				return calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]);
			case POWER		:
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					return OperandType.MATRIX;
				}
				else {
					throw new SyntaxException(0, node.col, "Power doesn't have matrix parameters"); 
				}
			case SPOOR		:
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					return OperandType.VALUE;
				}
				else {
					throw new SyntaxException(0, node.col, "Spoor doesn't have matrix parameters"); 
				}
			case ADD_VAL : case MUL_VAL : case SUB_VAL : case MUL_H : case MUL_K : case SUB : case MINUS : case POWER_VAL :
				throw new SyntaxException(0, node.col, "Unwaited node type"); 
			case TRANSPOSE	:
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					return OperandType.MATRIX;
				}
				else {
					throw new SyntaxException(0, node.col, "Transpose doesn't have matrix parameters"); 
				}
			case UNKNOWN	:
				throw new SyntaxException(0, node.col, "Unknown node type"); 
			default :
				throw new UnsupportedOperationException("Node type ["+node.type+"] is not supported yet");
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

	static enum LexType {
		EOF,
		OPEN,
		CLOSE,
		PLUS,
		MINUS,
		MUL,
		MUL_H,
		MUL_K,
		POWER,
		NAME,
		DOT,
		PREDEFINED,
		VALUE		
	}
	
	static enum OperType {
		ADD,
		MUL,
		UNARY,
		TERM
	}

	static enum Operation {
		LOAD_VALUE,
		LOAD_MATRIX,
		ADD,
		ADD_VAL,
		ADD_VAL_MATRIX,
		ADD_MATRIX_VAL,
		SUB,
		SUB_VAL,
		SUB_VAL_MATRIX,
		SUB_MATRIX_VAL,
		MUL,
		MUL_VAL,
		MUL_VAL_MATRIX,
		MUL_MATRIX_VAL,
		MUL_H,
		MUL_K,
		TRANSPOSE,
		INVERT,
		DET,
		SPOOR,
		NEGATE,
		MINUS,
		POWER,
		POWER_VAL,
		UNKNOWN
	}

	static enum OperandType {
		VALUE,
		MATRIX,
		UNKNOWN
	}
	
	static class Lexema {
		final int		pos;
		final LexType	type;
		final long		value;
		
		Lexema(final int pos, final LexType type, final long value) {
			this.pos = pos;
			this.type = type;
			this.value = value;
		}

		Lexema(final int pos, final LexType type) {
			this.pos = pos;
			this.type = type;
			this.value = 0;
		}
		
		@Override
		public String toString() {
			return "Lexema [pos=" + pos + ", type=" + type + ", value=" + value + "]";
		}
	}
	
	static class Command {
		final Operation	op;
		final long		operand;
		
		Command(Operation op, long operand) {
			this.op = op;
			this.operand = operand;
		}
	}
}
