package chav1961.bt.winsl.utils;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;

public class JavaServiceLibraryTest {
	volatile int 	val;

	@Test
//	@Ignore
	public void serviceLifeCycleTest() throws EnvironmentException, InterruptedException, ContentException {
		final JavaServiceDescriptor	desc = new JavaServiceDescriptor();
		
		desc.lpServiceName = "test";
		desc.lpDisplayName = "test service";
		desc.dwDesiredAccess = JavaServiceDescriptor.SERVICE_ALL_ACCESS | JavaServiceDescriptor.GENERIC_EXECUTE | JavaServiceDescriptor.SERVICE_PAUSE_CONTINUE; 
		desc.dwServiceType = JavaServiceDescriptor.SERVICE_WIN32_OWN_PROCESS;
		desc.dwStartType = JavaServiceDescriptor.SERVICE_DEMAND_START;
		desc.dwErrorControl = JavaServiceDescriptor.SERVICE_ERROR_NORMAL;
		desc.lpBinaryPathName = "java -jar xxx";
		desc.lpLoadOrderGroup = null;
		desc.lpDependencies = null;
		desc.lpServiceStartName = null;
		desc.lpPassword = null;
		
		try{ 
			Assert.assertEquals(0,JavaServiceLibrary.installService(desc));

		} finally {
			Assert.assertEquals(0,JavaServiceLibrary.removeService("test"));
		}
	}
}
