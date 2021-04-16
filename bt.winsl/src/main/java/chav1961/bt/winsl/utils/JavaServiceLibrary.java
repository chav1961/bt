package chav1961.bt.winsl.utils;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;

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
		try {
			System.loadLibrary("JavaServiceLibrary");
		} catch (UnsatisfiedLinkError err) {
			System.loadLibrary("JavaServiceLibrary32");
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
	 * @return 0 when successful, otherwise Windows error code
	 * @throws EnvironmentException on any errors
	 */
	public static native int prepareService() throws EnvironmentException;

	/**
	 * <p>Get callback service request from the Windows</p>
	 * @return service control code (see {@link #RC_START}, {@link #RC_STOP}, {@link #RC_PAUSE}, {@link #RC_RESUME})
	 * @throws EnvironmentException on any errors
	 */
	public static native int getServiceRequest() throws EnvironmentException;

	/**
	 * <p>Start service. Must be the second call when starting service</p>
	 * @param serviceName service name to start
	 * @return 0 when successful, otherwise Windows error code. Stops calling thread before service will be terminated
	 * @throws EnvironmentException on any errors
	 * @throws ContentException when service name is not exists
	 */
	public static native int startService(final String serviceName) throws EnvironmentException, ContentException;

	/**
	 * <p>Pause service</p>
	 * @param serviceName service name to pause
	 * @return 0 when successful, otherwise Windows error code.
	 * @throws EnvironmentException on any errors
	 * @throws ContentException when service name is not exists
	 */
	public static native int pauseService(final String serviceName) throws EnvironmentException, ContentException;

	/**
	 * <p>Resume service</p>
	 * @param serviceName service name to resume
	 * @return 0 when successful, otherwise Windows error code.
	 * @throws EnvironmentException on any errors
	 * @throws ContentException when service name is not exists
	 */
	public static native int resumeService(final String serviceName) throws EnvironmentException, ContentException;

	/**
	 * <p>Stop service</p>
	 * @param serviceName service name to stop
	 * @return 0 when successful, otherwise Windows error code.
	 * @throws EnvironmentException on any errors
	 * @throws ContentException when service name is not exists
	 */
	public static native int stopService(final String serviceName) throws EnvironmentException, ContentException;

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
