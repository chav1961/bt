package chav1961.bt.matrix.utils;

import org.jocl.CL;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;

import chav1961.purelib.basic.exceptions.EnvironmentException;

public class OpenCLUtils {
	private static final long 		DEVICE_TYPE = CL.CL_DEVICE_TYPE_ALL;
	
	public static cl_context getContext(final cl_device_id[] device) {
		final int[]	temp = new int[1];
		
	    // Enable exceptions and subsequently omit error checks in this sample
	    CL.setExceptionsEnabled(true);

	    // Obtain the number of platforms and platform IDs
	    CL.clGetPlatformIDs(0, null, temp);
	    final cl_platform_id 	platforms[] = new cl_platform_id[temp[0]];
	    
	    CL.clGetPlatformIDs(platforms.length, platforms, null);

	    for (int platformIndex = 0; platformIndex < platforms.length; platformIndex++) {
		    final cl_platform_id 	platform = platforms[platformIndex];
	
		    // Initialize the context properties
		    final cl_context_properties 	contextProperties = new cl_context_properties();
		    
		    contextProperties.addProperty(CL.CL_CONTEXT_PLATFORM, platform);
	
		    // Obtain the number of devices for the platform and device IDs 
		    CL.clGetDeviceIDs(platform, DEVICE_TYPE, 0, null, temp);
		    final cl_device_id 		devices[] = new cl_device_id[temp[0]];
		    
		    CL.clGetDeviceIDs(platform, DEVICE_TYPE, devices.length, devices, null);

		    for (int deviceIndex = 0; deviceIndex < devices.length; deviceIndex++) {
			    device[0] = devices[deviceIndex];
				
			    // Create platform context 
				return CL.clCreateContext(contextProperties, 1, device, null, null, null);		
		    }		    
	    }
	    throw new EnvironmentException("No any OpenCL context found"); 
	}
}
