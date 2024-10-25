package chav1961.bt.openclmatrix.spi;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.openclmatrix.spi.OpenCLDescriptor.OpenCLContext;
import chav1961.purelib.basic.exceptions.ContentException;

public class OpenCLDescriptorTest {

	@Test
	public void basicTest() throws ContentException {
		try(final OpenCLDescriptor	desc = new OpenCLDescriptor()) {
			final OpenCLContext 	context = desc.getContext(true);
		}
	}
}
