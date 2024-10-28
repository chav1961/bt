package chav1961.bt.openclmatrix.spi;

import java.util.ArrayList;
import java.util.List;

import org.jocl.CL;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;

public class OpenCLDescriptor implements AutoCloseable {
	private final List<OpenCLContext>	contexts = new ArrayList<>();	
	
	public OpenCLDescriptor() throws EnvironmentException {
        CL.setExceptionsEnabled(true);
        final cl_platform_id		platforms[] = new cl_platform_id[getNumberOfPlatforms()];
        
        CL.clGetPlatformIDs(platforms.length, platforms, null);
        for(cl_platform_id platform : platforms) {
            final cl_context_properties contextProperties = new cl_context_properties();
            
            contextProperties.addProperty(CL.CL_CONTEXT_PLATFORM, platform);
            final cl_device_id	devices[] = new cl_device_id[getNumberOfDevicesInPlatform(platform)];

            CL.clGetDeviceIDs(platform, CL.CL_DEVICE_TYPE_ALL, devices.length, devices, null);
            for (cl_device_id device : devices) {
                final cl_context 		context = CL.clCreateContext(contextProperties, 1, new cl_device_id[]{device}, null, null, null);
                final cl_command_queue 	commandQueue = CL.clCreateCommandQueue(context, device, 0, null);
            	
                contexts.add(new OpenCLContext(context, commandQueue));
            }
        }
        if (contexts.isEmpty()) {
        	throw new EnvironmentException("OpenCL devices not found");
        }
	}

	public OpenCLContext getContext(final boolean supportsDouble) throws ContentException {
		return contexts.get(0);
	}
	
	@Override
	public void close() throws RuntimeException {
		for(OpenCLContext item : contexts) {
			item.close();
		}
		contexts.clear();
	}
	
	private static int getNumberOfPlatforms() {
        int numPlatformsArray[] = new int[1];
        
        CL.clGetPlatformIDs(0, null, numPlatformsArray);
        return numPlatformsArray[0];
	}
	
	private static int getNumberOfDevicesInPlatform(cl_platform_id platform) {
        int numDevicesArray[] = new int[1];
        
        CL.clGetDeviceIDs(platform, CL.CL_DEVICE_TYPE_ALL, 0, null, numDevicesArray);
        return numDevicesArray[0];
	}
	
	public static class OpenCLContext implements AutoCloseable {
		public final cl_context			context;
		public final cl_command_queue	queue;
		
		OpenCLContext(final cl_context context, final cl_command_queue queue) {
			this.context = context;
			this.queue = queue;
		}

		@Override
		public void close() throws RuntimeException {
			try {
		        CL.clReleaseCommandQueue(queue);
		        CL.clReleaseContext(context);
			} catch (RuntimeException exc) {
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((context == null) ? 0 : context.hashCode());
			result = prime * result + ((queue == null) ? 0 : queue.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			OpenCLContext other = (OpenCLContext) obj;
			if (context == null) {
				if (other.context != null) return false;
			} else if (!context.equals(other.context)) return false;
			if (queue == null) {
				if (other.queue != null) return false;
			} else if (!queue.equals(other.queue)) return false;
			return true;
		}

		@Override
		public String toString() {
			return "OpenCLContext [context=" + context + ", queue=" + queue + "]";
		}
	}
}
