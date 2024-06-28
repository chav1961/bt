package chav1961.bt.matrix.utils;

import org.jocl.CL;
import org.jocl.CLException;
import org.jocl.cl_context;
import org.jocl.cl_kernel;
import org.jocl.cl_program;

import chav1961.purelib.basic.exceptions.EnvironmentException;

public class ProgramDescriptor implements AutoCloseable {
	public final String		programName;
	public final cl_program	program;
	public final cl_kernel	kernel;
	
	public ProgramDescriptor(final cl_context context, final String programName, final String programBody) throws EnvironmentException {
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