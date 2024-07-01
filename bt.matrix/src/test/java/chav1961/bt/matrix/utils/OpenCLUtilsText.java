package chav1961.bt.matrix.utils;

import org.jocl.CL;
import org.jocl.cl_context;
import org.jocl.cl_device_id;
import org.junit.Assert;
import org.junit.Test;

public class OpenCLUtilsText {

	@Test
	public void basicTest() {
		final cl_device_id[]	ids = new cl_device_id[1];  
		final cl_context		cc = OpenCLUtils.getContext(ids);
		
		Assert.assertNotNull(cc);
		CL.clReleaseContext(cc);
	}

}
