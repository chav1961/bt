package chav1961.bt.comm.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.fazecast.jSerialComm.SerialPort;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.PureLibSettings.CurrentOS;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;

public class CommUtils {
	public static final String	PORT_NAME = "name";
	public static final String	PORT_DESCRIPTION = "description";
	public static final String	DATA_BITS = "dataBits";
	public static final String	DEFAULT_DATA_BITS = "8";
	public static final String	STOP_BITS = "stopBits";
	public static final String	DEFAULT_STOP_BITS = StopBits.one.name();
	public static final String	PARITY = "parity";
	public static final String	DEFAULT_PARITY = Parity.none.name();
	public static final String	BAUD_RATE = "baudRate";
	public static final String	DEFAULT_BAUD_RATE = "1200";
	public static final String	FLOW_CONTROL = "flowControl";
	public static final String	DEFAULT_FLOW_CONTROL = "none";

	public static enum StopBits {
		one(SerialPort.ONE_STOP_BIT),
		oneAndHalf(SerialPort.ONE_POINT_FIVE_STOP_BITS),
		two(SerialPort.TWO_STOP_BITS);
		
		private final int	stopBitsMode;
		
		private StopBits(final int stopBitsMode) {
			this.stopBitsMode = stopBitsMode;
		}
		
		int getStopBitsMode() {
			return stopBitsMode;
		}
		
		public static StopBits valueOf(final float value) {
			if (value == 1) {
				return one;
			}
			else if (value == 1.5) {
				return oneAndHalf;
			}
			else if (value == 2) {
				return two;
			}
			else {
				throw new IllegalArgumentException("Illegal stop bit value ["+value+"]. Only 1, 1.5 and 2 are available"); 
			}
		}
	
		public static StopBits of(final int stopBitsMode) {
			for (StopBits item : values()) {
				if (stopBitsMode == item.getStopBitsMode()) {
					return item;
				}
			}
			throw new IllegalArgumentException("Unsupported stop bits mode ["+stopBitsMode+"]");
		}
	}
	
	public static enum Parity {
		even(SerialPort.EVEN_PARITY), 
		odd(SerialPort.ODD_PARITY),
		none(SerialPort.NO_PARITY),
		mark(SerialPort.MARK_PARITY), 
		space(SerialPort.SPACE_PARITY);
		
		private final int	parityMode;
		
		private Parity(final int parityMode) {
			this.parityMode = parityMode;
		}
		
		int getParityMode() {
			return parityMode;
		}
		
		public static Parity of(final int parityMode) {
			for (Parity item : values()) {
				if (parityMode == item.getParityMode()) {
					return item;
				}
			}
			throw new IllegalArgumentException("Unsupported parity mode ["+parityMode+"]");
		}
	}
	
	public static enum FlowControl {
		none(SerialPort.FLOW_CONTROL_DISABLED, CurrentOS.values()),
		cts(SerialPort.FLOW_CONTROL_CTS_ENABLED, CurrentOS.WINDOWS),
		rts_cts(SerialPort.FLOW_CONTROL_RTS_ENABLED | SerialPort.FLOW_CONTROL_CTS_ENABLED, CurrentOS.WINDOWS, CurrentOS.LINUX, CurrentOS.MACOS),
		dsr(SerialPort.FLOW_CONTROL_DSR_ENABLED, CurrentOS.WINDOWS),
		dtr_dsr(SerialPort.FLOW_CONTROL_DTR_ENABLED | SerialPort.FLOW_CONTROL_DSR_ENABLED, CurrentOS.WINDOWS),
		XonXoff(SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED, CurrentOS.WINDOWS, CurrentOS.LINUX, CurrentOS.MACOS);
		
		private final int			flowMask;
		private final CurrentOS[] 	supports;
		
		private FlowControl(final int flowMask, final CurrentOS... supports) {
			this.flowMask = flowMask;
			this.supports = supports;
		}

		public int getFlowControlMask() {
			return flowMask;
		}
		
		public boolean isControlSupportedFor(final CurrentOS os) {
			if (os == null) {
				throw new NullPointerException("OS to test can't be null");
			}
			else {
				for(CurrentOS item : supports) {
					if (item == os) {
						return true;
					}
				}
				return false;
			}
		}
		
		public static FlowControl of(final int mode) {
			for(FlowControl item : values()) {
				if ((item.getFlowControlMask() & mode) == item.getFlowControlMask() && item.isControlSupportedFor(PureLibSettings.CURRENT_OS)) {
					return item;
				}
			}
			throw new IllegalArgumentException("Flow control mask is not identified or is not available for current OS"); 
		}
	}

	private static final Set<Integer>	AVAILABLE_BAUDS = new HashSet<>(Arrays.asList(110, 300, 600, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200));
	
	public static Iterable<String> commPortsAvailable() {
		final List<String>	result = new ArrayList<>();
		
		for (SerialPort comPort : SerialPort.getCommPorts()) {
			result.add(comPort.getSystemPortName());
		}
		return result;
	}
	
	public static SubstitutableProperties getCommPortProperties(final String portName) {
		if (Utils.checkEmptyOrNullString(portName)) {
			throw new IllegalArgumentException("Port name can't be null or empty"); 
		}
		else {
			for (SerialPort comPort : SerialPort.getCommPorts()) {
				if (portName.equals(comPort.getSystemPortName())) {
					final SubstitutableProperties	props = new SubstitutableProperties();
					
					props.setProperty(PORT_NAME, comPort.getSystemPortName());
					props.setProperty(PORT_DESCRIPTION, comPort.getPortDescription());
					props.setProperty(DATA_BITS, String.valueOf(comPort.getNumDataBits()));
					props.setProperty(PARITY, Parity.of(comPort.getParity()).name());
					props.setProperty(STOP_BITS, StopBits.of(comPort.getNumStopBits()).name());
					props.setProperty(BAUD_RATE, String.valueOf(comPort.getBaudRate()));
					props.setProperty(FLOW_CONTROL, FlowControl.of(comPort.getFlowControlSettings()).name());
					return props;
				}
			}
			throw new IllegalArgumentException("Port name ["+portName+"] not found"); 
		}
	}

	public static SerialPort prepareCommPort(final String name, final SubstitutableProperties props) {
		for (SerialPort comPort : SerialPort.getCommPorts()) {
			if (comPort.getSystemPortName().equals(name)) {
				comPort.setComPortParameters(props.getProperty(BAUD_RATE, int.class, DEFAULT_BAUD_RATE), 
						props.getProperty(DATA_BITS, int.class, DEFAULT_DATA_BITS), 
						props.getProperty(STOP_BITS, CommUtils.StopBits.class, DEFAULT_STOP_BITS).getStopBitsMode(), 
						props.getProperty(PARITY, CommUtils.Parity.class, DEFAULT_PARITY).getParityMode()
						);
				comPort.setFlowControl(props.getProperty(FLOW_CONTROL, CommUtils.FlowControl.class, DEFAULT_FLOW_CONTROL).getFlowControlMask());
				return comPort;
			}
		}
		return null;
	}
	
	public static SubstitutableProperties parseCommQueryParameters(final Hashtable<String,String[]> source) {
		final SubstitutableProperties	result = new SubstitutableProperties();
		
		if (source.containsKey(DATA_BITS)) {
			try {
				final int bits = Integer.valueOf(source.get(DATA_BITS)[0]);
				
				if (bits != 5 && bits != 7 && bits != 8) {
					throw new IllegalArgumentException("Query option ["+DATA_BITS+"] has illegal value ["+source.get(DATA_BITS)[0]+"]. Only 5, 7 and 8 are valid here"); 
				}
				else {
					result.setProperty(DATA_BITS, source.remove(DATA_BITS)[0]);
				}
			} catch (NumberFormatException exc) {
				throw new IllegalArgumentException("Query option ["+DATA_BITS+"] has illegal number ["+source.get(DATA_BITS)[0]+"]"); 
			}
		}
		else {
			result.setProperty(DATA_BITS, DEFAULT_DATA_BITS);
		}

		if (source.containsKey(STOP_BITS)) {
			try {
				result.setProperty(STOP_BITS, StopBits.valueOf(source.remove(STOP_BITS)[0]).name());
			} catch (IllegalArgumentException exc) {
				throw new IllegalArgumentException("Query option ["+STOP_BITS+"] has illegal number ["+source.get(STOP_BITS)[0]+"]. Only 1, 1.5 and 2 are valid here"); 
			}
		}
		else {
			result.setProperty(STOP_BITS, DEFAULT_STOP_BITS);
		}

		if (source.containsKey(PARITY)) {
			try {
				result.setProperty(PARITY, Parity.valueOf(source.remove(PARITY)[0]).name());
			} catch (IllegalArgumentException exc) {
				throw new IllegalArgumentException("Query option ["+PARITY+"] has illegal value ["+source.get(PARITY)[0]+"]. Only "+Arrays.toString(Parity.values())+" are available here"); 
			}
		}
		else {
			result.setProperty(PARITY, DEFAULT_PARITY);
		}

		if (source.containsKey(BAUD_RATE)) {
			try {
				final int baudRate = Integer.valueOf(source.get(BAUD_RATE)[0]);
				
				if (!AVAILABLE_BAUDS.contains(baudRate)) {
					throw new IllegalArgumentException("Query option ["+BAUD_RATE+"] has unsupported value ["+source.get(BAUD_RATE)[0]+"]. Only "+AVAILABLE_BAUDS+" are valid here"); 
				}
				else {
					result.setProperty(BAUD_RATE, source.remove(BAUD_RATE)[0]);
				}
			} catch (NumberFormatException exc) {
				throw new IllegalArgumentException("Query option ["+BAUD_RATE+"] has illegal number ["+source.get(BAUD_RATE)[0]+"]"); 
			}
		}
		else {
			result.setProperty(BAUD_RATE, DEFAULT_BAUD_RATE);
		}

		if (source.containsKey(FLOW_CONTROL)) {
			result.setProperty(FLOW_CONTROL, FlowControl.valueOf(source.remove(FLOW_CONTROL)[0]).name());
		}
		else {
			result.setProperty(FLOW_CONTROL, DEFAULT_FLOW_CONTROL);
		}

		if (!source.isEmpty()) {
			throw new IllegalArgumentException("Unknown query option(s) "+source.keySet()+" detected");
		}
		else {
			return result;
		}
	}
}
