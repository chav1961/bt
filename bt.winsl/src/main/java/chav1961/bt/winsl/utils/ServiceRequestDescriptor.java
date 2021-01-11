package chav1961.bt.winsl.utils;

public class ServiceRequestDescriptor {
	public static final int	RQ_START = 0;
	public static final int	RQ_PAUSE = 1;
	public static final int	RQ_CONTINUE = 2;
	public static final int	RQ_STOP = 3;
	
	public int		request;
	public String[]	arguments;
}
