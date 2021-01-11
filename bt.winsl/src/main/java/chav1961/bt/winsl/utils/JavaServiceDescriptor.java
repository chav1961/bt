package chav1961.bt.winsl.utils;

public class JavaServiceDescriptor {
	// dwDesiredAccess group
	
	public static final int		SC_MANAGER_ALL_ACCESS = 0xF003F;
	public static final int		SC_MANAGER_CREATE_SERVICE = 0x0002;
	public static final int		SC_MANAGER_CONNECT = 0x0001;
	public static final int		SC_MANAGER_ENUMERATE_SERVICE = 0x0004;
	public static final int		SC_MANAGER_LOCK = 0x0008;
	public static final int		SC_MANAGER_MODIFY_BOOT_CONFIG = 0x0020;
	public static final int		SC_MANAGER_QUERY_LOCK_STATUS = 0x0010;
	
	public static final int		GENERIC_READ = 0x80000000;
	public static final int		GENERIC_WRITE = 0x40000000;
	public static final int		GENERIC_EXECUTE = 0x20000000;
	public static final int		GENERIC_ALL = 0x10000000;
	
	public static final int		SERVICE_ALL_ACCESS = 0xF01FF;
	public static final int		SERVICE_CHANGE_CONFIG = 0x0002;
	public static final int		SERVICE_ENUMERATE_DEPENDENTS = 0x0008;
	public static final int		SERVICE_INTERROGATE = 0x0080;
	public static final int		SERVICE_PAUSE_CONTINUE = 0x0040;
	public static final int		SERVICE_QUERY_CONFIG = 0x0001;
	public static final int		SERVICE_QUERY_STATUS = 0x0004;
	public static final int		SERVICE_START = 0x0010;
	public static final int		SERVICE_STOP = 0x0020;
	public static final int		SERVICE_USER_DEFINED_CONTROL =0x0100;	

	public static final int		ACCESS_SYSTEM_SECURITY = 0x01000000;
	public static final int		DELETE = 0x10000;
	public static final int		READ_CONTROL = 0x20000;
	public static final int		WRITE_DAC = 0x40000;
	public static final int		WRITE_OWNER = 0x80000; 	
	
	// dwServiceType group

	public static final int		SERVICE_ADAPTER = 0x00000004;
	public static final int		SERVICE_FILE_SYSTEM_DRIVER = 0x00000002;
	public static final int		SERVICE_KERNEL_DRIVER = 0x00000001;
	public static final int		SERVICE_RECOGNIZER_DRIVER = 0x00000008;
	public static final int		SERVICE_WIN32_OWN_PROCESS = 0x00000010;
	public static final int		SERVICE_WIN32_SHARE_PROCESS = 0x00000020;
	public static final int		SERVICE_USER_OWN_PROCESS = 0x00000050;
	public static final int		SERVICE_USER_SHARE_PROCESS = 0x00000060;
	public static final int		SERVICE_INTERACTIVE_PROCESS = 0x00000100;
	
	// dwStartType group
	
	public static final int		SERVICE_AUTO_START = 0x00000002;
	public static final int		SERVICE_BOOT_START = 0x00000000;
	public static final int		SERVICE_DEMAND_START = 0x00000003;
	public static final int		SERVICE_DISABLED = 0x00000004;
	public static final int		SERVICE_SYSTEM_START = 0x00000001;

	// dwErrorControl group
	
	public static final int		SERVICE_ERROR_CRITICAL = 0x00000003;
	public static final int		SERVICE_ERROR_IGNORE = 0x00000000;
	public static final int		SERVICE_ERROR_NORMAL = 0x00000001;
	public static final int		SERVICE_ERROR_SEVERE = 0x00000002;

	public String	lpServiceName;
	public String	lpDisplayName;
	public int		dwDesiredAccess;
	public int		dwServiceType;
	public int		dwStartType;
	public int		dwErrorControl;
	public String	lpBinaryPathName;
	public String	lpLoadOrderGroup;
	public int		lpdwTagId;
	public String	lpDependencies;
	public String	lpServiceStartName;
	public String	lpPassword;
}
