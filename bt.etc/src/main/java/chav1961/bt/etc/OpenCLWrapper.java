package chav1961.bt.etc;

import org.jocl.CL;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.cdb.CompilerUtils;

import org.jocl.*;
import static java.lang.System.nanoTime;

import java.util.Arrays;


public class OpenCLWrapper implements AutoCloseable{
    private static String programSource =
		    "__kernel void "+
		    "matrixMul(__global float* A,"+ 
		    "          __global float* B,"+ 
		    "          __global float* C,"+ 
		    "          int wA, int wB)"+
		    "{"+
		       "int tx = get_global_id(0);"+ 
		       "int ty = get_global_id(1);"+
		       "for (int i = 0; i < wA; i++) {"+
		       "}"+
		       "float value = 0;"+
		       "for (int k = 0; k < wA; ++k)"+
		       "{"+
		       "   float elementA = A[ty * wA + k];"+
		       "   float elementB = B[k * wB + tx];"+
		       "   value += elementA * elementB;"+
		       "}"+
		
		      "C[ty * wA + tx] = value;"+
		    "}";

    private static String invSource =
    		"__kernel void invert(\n"
    		+ "__global float *source,\n"
			+ "__global float *identity,\n"
			+ "unsigned int n) {\n"
			+ "	\n"
			+ "	unsigned int j = get_global_id(0);\n"
			+ "	unsigned int i;\n"
			+ "	unsigned int k;\n"
			+ "	float temp;\n"
			+ "	\n"
			+ "	for (k = 0; k < n; k++) {\n"
			+ "		temp = 1.0f/source[n*k+k];\n"
			+ "		\n"
			+ "		source[n*k+j] *= temp;\n"
			+ "		identity[n*k+j] *= temp;\n"
			+ "		\n"
			+ "		barrier(CLK_GLOBAL_MEM_FENCE);\n"
			+ "		\n"
			+ "		for (i = 0; i < n; i++) {\n"
			+ "			if (i != k) {\n"
			+ "				temp = source[n*i+k];\n"
			+ "				source[n*i+j] -= source[n*k+j] * temp;\n"
			+ "				identity[n*i+j] -= identity[n*k+j] * temp;\n"
			+ "			}\n"
			+ "		}\n"
			+ "		barrier(CLK_GLOBAL_MEM_FENCE);\n"
			+ "	}\n"
			+ "}\n";    
    
    private final cl_context 		context;
    private final cl_command_queue	commandQueue;
    
    public OpenCLWrapper(final int platformIndex, final int deviceIndex) {
       // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        CL.clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        CL.clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL.CL_CONTEXT_PLATFORM, platform);

        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        CL.clGetDeviceIDs(platform, CL.CL_DEVICE_TYPE_ALL, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        // Obtain a device ID 
        cl_device_id devices[] = new cl_device_id[numDevices];
        CL.clGetDeviceIDs(platform, CL.CL_DEVICE_TYPE_ALL, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // Create a context for the selected device
        this.context = CL.clCreateContext(contextProperties, 1, new cl_device_id[]{device}, null, null, null);

        // Create a command-queue for the selected device
        this.commandQueue = CL.clCreateCommandQueue(context, device, 0, null);
    }

	public cl_mem getBuffer(final Class<?> clazz, final int size, final long flags) {
		if (clazz == null) {
			throw new NullPointerException("CLass awaited can't be null");
		}
    	else if (size <= 0) {
    		throw new IllegalArgumentException("Buffer size ["+size+"] must be greater than 0");
    	}
    	else {
			switch (CompilerUtils.defineClassType(clazz)) {
				case CompilerUtils.CLASSTYPE_INT	:	
					return CL.clCreateBuffer(context, flags, Sizeof.cl_int * size, null, null);
				case CompilerUtils.CLASSTYPE_LONG	:	
					return CL.clCreateBuffer(context, flags, Sizeof.cl_long * size, null, null);
				case CompilerUtils.CLASSTYPE_FLOAT	:	
					return CL.clCreateBuffer(context, flags, Sizeof.cl_float * size, null, null);
				case CompilerUtils.CLASSTYPE_DOUBLE	:	
					return CL.clCreateBuffer(context, flags, Sizeof.cl_double * size, null, null);
				default :
					throw new IllegalArgumentException("Class type ["+clazz.getCanonicalName()+"] is not supported for buffer content"); 
			}
    	}
	}

	public cl_mem getBuffer(final int[] content, final long flags) {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			return CL.clCreateBuffer(context, flags, Sizeof.cl_int * content.length, Pointer.to(content), null);
		}
	}

	public cl_mem getBuffer(final long[] content, final long flags) {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			return CL.clCreateBuffer(context, flags, Sizeof.cl_long * content.length, Pointer.to(content), null);
		}
	}

	public cl_mem getBuffer(final float[] content, final long flags) {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			return CL.clCreateBuffer(context, flags, Sizeof.cl_float * content.length, Pointer.to(content), null);
		}
	}
	
	public cl_mem getBuffer(final double[] content, final long flags) {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			return CL.clCreateBuffer(context, flags, Sizeof.cl_double * content.length, Pointer.to(content), null);
		}
	}
    
	public void freeBuffer(final cl_mem buffer) {
		if (buffer == null) {
			throw new NullPointerException("Buffer to free can't be null");
		}
		else {
			CL.clReleaseMemObject(buffer);
		}
	}
	
    public OpenCLExecutor getExecutor(final String programName, final String program, final int globalSize, final int localSize) {
    	if (Utils.checkEmptyOrNullString(programName)) {
    		throw new IllegalArgumentException("Program name can't be null or empty"); 
    	}
    	else if (Utils.checkEmptyOrNullString(program)) {
    		throw new IllegalArgumentException("Program content can't be null or empty"); 
    	}
    	else if (globalSize <= 0) {
    		throw new IllegalArgumentException("Global size ["+globalSize+"] must be greater than 0");
    	}
    	else if (localSize < 0) {
    		throw new IllegalArgumentException("Local size ["+localSize+"] must be greater than or equals 0");
    	}
    	else {
        	return new OpenCLExecutor(programName, program, new long[]{globalSize}, new long[]{localSize});
    	}
    }

    public OpenCLExecutor getExecutor(final String programName, final String program, final long[] globalSize, final long[] localSize) {
    	if (Utils.checkEmptyOrNullString(programName)) {
    		throw new IllegalArgumentException("Program name can't be null or empty"); 
    	}
    	else if (Utils.checkEmptyOrNullString(program)) {
    		throw new IllegalArgumentException("Program content can't be null or empty"); 
    	}
    	else if (globalSize == null || globalSize.length == 0 || globalSize.length > 3) {
    		throw new IllegalArgumentException("Global size array can't be null and must have 1..3 elements");
    	}
    	else if (localSize == null || localSize.length == 0 || localSize.length > 3) {
    		throw new IllegalArgumentException("Local size array can't be null and must have 1..3 elements");
    	}
    	else {
        	return new OpenCLExecutor(programName, program, globalSize, localSize);
    	}
    }
    
    
    @Override
    public void close() throws RuntimeException {
    	CL.clReleaseCommandQueue(commandQueue);
    	CL.clReleaseContext(context);
   }
    
	public static void main(String args[]) {
	   	try(final OpenCLWrapper	t = new OpenCLWrapper(0, 0)) {
	        int n = 1000;
	        float srcArrayA[] = new float[n*n];
	        float srcArrayB[] = new float[n*n];
	        float dstArray[] = new float[n*n];
	        for (int i=0; i<n*n; i++) {
	            srcArrayA[i] = i;
	            srcArrayB[i] = i;
	        }
	        
	        try(final OpenCLExecutor	ex = t.getExecutor("matrixMul", programSource, n, 1)) {
	        	System.err.println("Duration1="+ex.execute(new SourceFloatArray(srcArrayA)
	        					, new SourceFloatArray(srcArrayB)
	        					, new TargetFloatArray(dstArray)
	        					, new SourceInt(n)
	        					, new SourceInt(n)
	        	));
	        	System.err.println("Duration2="+ex.execute(new SourceFloatArray(srcArrayA)
    					, new SourceFloatArray(srcArrayB)
    					, new TargetFloatArray(dstArray)
    					, new SourceInt(n)
    					, new SourceInt(n)
	        	));
	        }
	   	}

	   	try(final OpenCLWrapper	t = new OpenCLWrapper(0, 0)) {
	        int n = 3;
	        float srcArray[] = new float[] {1, -2, 3, 0, 4, -1, 5, 0, 0};
	        float identity[] = new float[n*n];
	        for (int i=0; i<n; i++) {
	        	identity[n*i+i] = 1;
	        }
	        
	        try(final OpenCLExecutor	ex = t.getExecutor("invert", invSource, new long[] {n,n}, new long[] {1, 1})) {
	        	System.err.println("Duration3="+ex.execute(new FloatArray(srcArray)
	        					, new FloatArray(identity)
	        					, new SourceInt(n)
	        	));
	        	System.err.println("Inv="+Arrays.toString(identity));
	        }
	   	}
	}
    
   public class OpenCLExecutor implements AutoCloseable {
	   private final cl_program program;
	   private final cl_kernel	kernel;
	   private final long[]		global_work_size;
	   private final long[]		local_work_size;		
	   
	   private OpenCLExecutor(final String programName, final String source, final long[] globalSize, final long[] localSize) {
	        // Create the program from the source code
	        this.program = CL.clCreateProgramWithSource(context, 1, new String[]{ source }, null, null);

	        // Build the program
	        CL.clBuildProgram(program, 0, null, null, null, null);

	        // Create the kernel
	        this.kernel = CL.clCreateKernel(program, programName, null);

	        // Set the work-item dimensions
	        this.global_work_size = globalSize.clone();
	        this.local_work_size = localSize.clone();
	   }
	   
	   public long execute(final ExecutorArgument... parameters) {
	        final long 		time = nanoTime();
	        
	        // Allocate and fill buffers for arrays
	 	   	final cl_mem 	memObjects[] = new cl_mem[parameters.length];
	 	   	
	 	   	for(int index = 0; index < parameters.length; index++) {
	 	   		switch (parameters[index].getArgumentType()) {
					case FLOAT_ARRAY	:
						switch (parameters[index].getAccessMode()) {
							case IN	:
								final float[]	sourceContent = ((SourceFloatArray)parameters[index]).getContent();
						        
								memObjects[index] = getBuffer(sourceContent, CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR); 
								break;
							case OUT	:
								final float[]	targetContent = ((TargetFloatArray)parameters[index]).getContent();
								
						        memObjects[index] = getBuffer(float.class, targetContent.length, CL.CL_MEM_READ_WRITE); 
								break;
							case IN_OUT	:
								final float[]	content = ((FloatArray)parameters[index]).getContent();
								
								memObjects[index] = getBuffer(content, CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR);
								break;
							default:
								throw new UnsupportedOperationException("Access mode  ["+parameters[index].getAccessMode()+"] is not supported yet"); 
						}
						break;
					case INT_VALUE		:
						break;
					default :
						throw new UnsupportedOperationException("Argument type ["+parameters[index].getArgumentType()+"] is not supported yet"); 
	 	   		}
	 	   	}

	        // Set the arguments for the kernel
	 	   	for(int index = 0; index < parameters.length; index++) {
	 	   		switch (parameters[index].getArgumentType()) {
					case FLOAT_ARRAY	:
				        CL.clSetKernelArg(kernel, index, Sizeof.cl_mem, Pointer.to(memObjects[index]));
						break;
					case INT_VALUE		:
				        CL.clSetKernelArg(kernel, index, Sizeof.cl_int, Pointer.to(new int[] {((SourceInt)parameters[index]).getContent()}));
						break;
					default:
						throw new UnsupportedOperationException("Argument type ["+parameters[index].getArgumentType()+"] is not supported yet"); 
	 	   		}
	 	   	}
	        
	        CL.clEnqueueNDRangeKernel(commandQueue, kernel, global_work_size.length, null, global_work_size, local_work_size, 0, null, null);

	        // Read the output data
	 	   	for(int index = 0; index < parameters.length; index++) {
	 	   		switch (parameters[index].getArgumentType()) {
					case FLOAT_ARRAY	:
						switch (parameters[index].getAccessMode()) {
							case IN		:
								break;
							case IN_OUT	:
								final float[]	content = ((FloatArray)parameters[index]).getContent();
								
						        CL.clEnqueueReadBuffer(commandQueue, memObjects[index], CL.CL_TRUE, 0, Sizeof.cl_float * content.length, Pointer.to(content), 0, null, null);
								break;
							case OUT	:
								final float[]	targetContent = ((TargetFloatArray)parameters[index]).getContent();
								
						        CL.clEnqueueReadBuffer(commandQueue, memObjects[index], CL.CL_TRUE, 0, Sizeof.cl_float * targetContent.length, Pointer.to(targetContent), 0, null, null);
								break;
							default:
								break;
						}
						break;
					case INT_VALUE		:
						break;
					default:
						throw new UnsupportedOperationException("Argument type ["+parameters[index].getArgumentType()+"] is not supported yet"); 
	 	   		}
	 	   	}
	 	   	
	        // Release memory objects
	 	   	for (cl_mem item : memObjects) {
	 	   		if (item != null) {
	 		        freeBuffer(item);
	 	   		}
	 	   	}
	        return nanoTime() - time;
	   }

	@Override
	public void close() throws RuntimeException {
        // Release kernel and program
		CL.clReleaseKernel(kernel);
		CL.clReleaseProgram(program);
	}
   }
   
   private static enum ArgumentType {
	   FLOAT_ARRAY,
	   INT_VALUE
   }

   private static enum AccessMode {
	   IN,
	   OUT,
	   IN_OUT
   }
   
   static class ExecutorArgument {
	   private final ArgumentType 	argType;
	   private final AccessMode		access;
	   
	   ExecutorArgument(final ArgumentType type, final AccessMode accessMode) {
		   this.argType = type;
		   this.access = accessMode;
	   }
	   
	   ArgumentType getArgumentType() {
		   return argType;
	   }
	   
	   AccessMode getAccessMode() {
		   return access;
	   }
   }
   
   public static class SourceFloatArray extends ExecutorArgument {
	   private final float[]	content;

	   public SourceFloatArray(final float[] content) {
			super(ArgumentType.FLOAT_ARRAY, AccessMode.IN);
			this.content = content;
	   }

	   float[] getContent() {
		   return content;
	   }
   }

   public static class TargetFloatArray extends ExecutorArgument {
	   private final float[]	content;

	   public TargetFloatArray(final float[] content) {
			super(ArgumentType.FLOAT_ARRAY, AccessMode.OUT);
			this.content = content;
	   }
	   
	   float[] getContent() {
		   return content;
	   }
   }

   public static class FloatArray extends ExecutorArgument {
	   private final float[]	content;

	   public FloatArray(final float[] content) {
			super(ArgumentType.FLOAT_ARRAY, AccessMode.IN_OUT);
			this.content = content;
	   }

	   float[] getContent() {
		   return content;
	   }
   }
   
   public static class SourceInt extends ExecutorArgument {
	   private final int	content;

	   public SourceInt(final int content) {
			super(ArgumentType.INT_VALUE, AccessMode.IN);
			this.content = content;
	   }
	   
	   int getContent() {
		   return content;
	   }
   }
}