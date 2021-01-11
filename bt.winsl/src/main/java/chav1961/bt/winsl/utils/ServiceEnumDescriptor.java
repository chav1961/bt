package chav1961.bt.winsl.utils;

public class ServiceEnumDescriptor {
	public static final int		SERVICE_CONTINUE_PENDING = 0x00000005;
	public static final int		SERVICE_PAUSE_PENDING = 0x00000006;
	public static final int		SERVICE_PAUSED = 0x00000007;
	public static final int		SERVICE_RUNNING = 0x00000004;
	public static final int		SERVICE_START_PENDING = 0x00000002;
	public static final int		SERVICE_STOP_PENDING = 0x00000003;
	public static final int		SERVICE_STOPPED = 0x00000001;

	public static final int		SERVICE_ACCEPT_NETBINDCHANGE = 0x00000010;
	public static final int		SERVICE_ACCEPT_PARAMCHANGE = 0x00000008;
	public static final int		SERVICE_ACCEPT_PAUSE_CONTINUE = 0x00000002;
	public static final int		SERVICE_ACCEPT_PRESHUTDOWN = 0x00000100;
	public static final int		SERVICE_ACCEPT_SHUTDOWN = 0x00000004;
	public static final int		SERVICE_ACCEPT_STOP = 0x00000001;
	
	public static final int		SERVICE_ACCEPT_HARDWAREPROFILECHANGE = 0x00000020;
	public static final int		SERVICE_ACCEPT_POWEREVENT = 0x00000040;
	public static final int		SERVICE_ACCEPT_SESSIONCHANGE = 0x00000080;
	public static final int		SERVICE_ACCEPT_TIMECHANGE = 0x00000200;
	public static final int		SERVICE_ACCEPT_TRIGGEREVENT = 0x00000400;
	public static final int		SERVICE_ACCEPT_USERMODEREBOOT = 0x00000800;
	
	public String	lpServiceName;
	public String	lpDisplayName;
	public int		dwServiceType;
	public int		dwCurrentState;
	public int		dwControlsAccepted;
	public int		dwWin32ExitCode;
	public int		dwServiceSpecificExitCode;
	public int		dwCheckPoint;
	public int		dwWaitHint;
}
