package chav1961.bt.openclmatrix.internal;

import java.util.function.Consumer;

import org.jocl.CL;
import org.jocl.CLException;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_event;
import org.jocl.cl_kernel;
import org.jocl.cl_program;

import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUEvent;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUExecutable;
import chav1961.bt.openclmatrix.spi.OpenCLDescriptor.OpenCLContext;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;

class GPUExecutableImpl implements GPUExecutable {
	private final OpenCLContext				owner;
	private final Consumer<GPUExecutable> 	onCloseCallback;
	private final String					name;
	private final cl_program				program; 
	private final cl_kernel					kernel; 

	GPUExecutableImpl(final OpenCLContext owner, final String name, final String program, final Consumer<GPUExecutable> onCloseCallback) throws SyntaxException {
		this.owner = owner;
		this.onCloseCallback = onCloseCallback;
		this.name = name;
	        
		try {
			this.program = CL.clCreateProgramWithSource(owner.context, 1, new String[]{ program }, null, null);
			
	        CL.clBuildProgram(this.program, 0, null, null, null, null);
		    this.kernel = CL.clCreateKernel(this.program, name, null);		
		} catch (CLException exc) {
			throw new SyntaxException(0, 0, exc.getLocalizedMessage());
		}
	}
	
	@Override
	public void close() throws RuntimeException {
		onCloseCallback.accept(this);
		CL.clReleaseKernel(kernel);
        CL.clReleaseProgram(program);
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void execute(final GPUEvent event, final long[] dimensions, final Object... parameters) {
		if (event == null) {
			throw new NullPointerException("Event can't be null");
		}
		else if (dimensions == null || dimensions.length == 0 || dimensions.length > 3) {
			throw new IllegalArgumentException("Dimensions are null, empty or contains more than 3 elements");
		}
		else if (parameters == null || parameters.length == 0 || Utils.checkArrayContent4Nulls(parameters) >= 0) {
			throw new IllegalArgumentException("Parameters are be null, empty or contain nulls inside");
		}
		else {
			executeInternal(null, event, dimensions, parameters);
		}
	}
	
	@Override
	public void executeAfter(final GPUEvent[] events, final GPUEvent event, final long[] dimensions, final Object... parameters) {
		if (event == null) {
			throw new NullPointerException("Event can't be null");
		}
		else if (dimensions == null || dimensions.length == 0 || dimensions.length > 3) {
			throw new IllegalArgumentException("Dimensions are null, empty or contains more than 3 elements");
		}
		else if (parameters == null || parameters.length == 0 || Utils.checkArrayContent4Nulls(parameters) >= 0) {
			throw new IllegalArgumentException("Parameters are be null, empty or contain nulls inside");
		}
		else {
			final cl_event[]	eventList = new cl_event[events.length];
			
			for(int index = 0; index < eventList.length; index++) {
				eventList[index] = ((GPUEventImpl[])events)[index].event;
			}
			executeInternal(eventList, event, dimensions, parameters);
		}
	}
	
	private void executeInternal(cl_event[] events, GPUEvent event, long[] dimensions, Object... parameters) {
		for(int index = 0; index < parameters.length; index++) {
			if (parameters[index] instanceof GPUBufferImpl) {
				final GPUBufferImpl		buf = (GPUBufferImpl)parameters[index];
				
				CL.clSetKernelArg(kernel, index, Sizeof.cl_mem, Pointer.to(buf.buffer));
			}
			else if (parameters[index] instanceof Number) {
				if (parameters[index] instanceof Integer) {
					CL.clSetKernelArg(kernel, index, Sizeof.cl_int, Pointer.to(new int[]{((Number)parameters[index]).intValue()}));
				}
				else if (parameters[index] instanceof Long) {
					CL.clSetKernelArg(kernel, index, Sizeof.cl_long, Pointer.to(new long[]{((Number)parameters[index]).longValue()}));
				}
				else if (parameters[index] instanceof Float) {
					CL.clSetKernelArg(kernel, index, Sizeof.cl_float, Pointer.to(new float[]{((Number)parameters[index]).floatValue()}));
				}
				else if (parameters[index] instanceof Double) {
					CL.clSetKernelArg(kernel, index, Sizeof.cl_double, Pointer.to(new double[]{((Number)parameters[index]).doubleValue()}));
				}
				else {
					throw new IllegalArgumentException("Parameter ["+index+"] has wrong type [], only GPUBuffer or Number children are available");
				}
			}
			else {
				throw new IllegalArgumentException("Parameter ["+index+"] has wrong type [], only GPUBuffer or Number children are available");
			}
		}
		CL.clEnqueueNDRangeKernel(owner.queue, kernel, dimensions.length, null, dimensions, null, events == null ? 0 : events.length, events, ((GPUEventImpl)event).event);
	}
}
