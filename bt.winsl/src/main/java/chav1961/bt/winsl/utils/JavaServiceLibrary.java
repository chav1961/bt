package chav1961.bt.winsl.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.PreparationException;

/**
 * <p>This class is an adapter to Windows service mechanism. It supports both service manipulation (installation, change configuration,
 * removing) and service life cycle (starting, pausing, resuming, stopping)</p>
 *
 */
public class JavaServiceLibrary {
	/**
	 * <p>Request to start application</p>
	 */
	public static final int RC_START = 0;
	/**
	 * <p>Request to pause application</p>
	 */
	public static final int RC_PAUSE = 1;
	/**
	 * <p>Request to resume application</p>
	 */
	public static final int RC_RESUME = 2;
	/**
	 * <p>Request to stop application</p>
	 */
	public static final int RC_STOP = 3;
	/**
	 * <p>Unclassified request</p>
	 */
	public static final int RC_UNKNOWN = 4;
	
	static {
		final File	toPath = new File(System.getProperty("java.io.tmpdir"),"srvmgr.dll"); 

		try {
			try(final InputStream	is = JavaServiceLibrary.class.getResourceAsStream("/srvmgr.dll"); 
				final OutputStream	os = new FileOutputStream(toPath)) {
				
				Utils.copyStream(is, os);
			} catch (IOException e) {
				throw new PreparationException(e.getLocalizedMessage(), e);
			}
			System.load(toPath.getAbsolutePath());
		} catch (UnsatisfiedLinkError e) {
			throw new PreparationException(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * <p>Install service into Windows</p>
	 * @param desc service descriptor
	 * @return 0 when successful, Windows error code otherwise
	 * @throws EnvironmentException when you have no administrative privileges to install service
	 * @throws ContentException when service already exists
	 */
	public static native int installService(final JavaServiceDescriptor desc) throws EnvironmentException, ContentException;
	
	/**
	 * <p>Update existent service in Windows</p>
	 * @param desc service descriptor
	 * @return 0 when successful, Windows error code otherwise
	 * @throws EnvironmentException when you have no administrative privileges to install service
	 * @throws ContentException when service is not exists
	 */
	public static native int updateService(final JavaServiceDescriptor desc) throws EnvironmentException, ContentException;

	/**
	 * <p>Enumerate services in the Windows</p>
	 * @param serviceType service type to enum
	 * @param serviceState service state to enum
	 * @return service list. Can be empty but not null
	 * @throws EnvironmentException when you have no administrative privileges to install service
	 */
	public static native ServiceEnumDescriptor[] enumServices(final int serviceType, final int serviceState) throws EnvironmentException;

	/**
	 * <p>Get service descriptor</p>
	 * @param serviceName service name to get descriptor for
	 * @return service descriptor
	 * @throws EnvironmentException when you have no administrative privileges to install service
	 * @throws ContentException when service is not exists
	 */
	public static native JavaServiceDescriptor queryService(final String serviceName) throws EnvironmentException, ContentException;

	/**
	 * <p>Remove service from the Windows</p>
	 * @param serviceName service name to remove
	 * @return 0 when successful, otherwise Windows error code
	 * @throws EnvironmentException when you have no administrative privileges to install service
	 * @throws ContentException when service is not exists
	 */
	public static native int removeService(final String serviceName) throws EnvironmentException, ContentException;
	
	/**
	 * <p>Prepare inter-thread communications. Must be the same first call when starting service </p>
	 * @param serviceName service name to prepare
	 * @return 0 when successful, otherwise Windows error code
	 * @throws EnvironmentException on any errors
	 */
	public static native int prepareService(final String serviceName, final Object queue) throws EnvironmentException;

	/**
	 * <p>Get callback service request from the Windows</p>
	 * @return service control code (see {@link #RC_START}, {@link #RC_STOP}, {@link #RC_PAUSE}, {@link #RC_RESUME})
	 * @throws EnvironmentException on any errors
	 */
	public static native int getServiceRequest() throws EnvironmentException;

	/**
	 * <p>Destroy inter-thread communications. Mist be the same last call when starting service
	 * @return 0 when successful, otherwise Windows error code.
	 * @throws EnvironmentException on any errors
	 */
	public static native int unprepareService() throws EnvironmentException;
	
	/**
	 * <p>Print message to service log</p>
	 * @param service service name to print message for
	 * @param message message to print
	 * @throws EnvironmentException on any errors
	 */
	public static native void print2ServiceLog(String service, String message) throws EnvironmentException;
}
