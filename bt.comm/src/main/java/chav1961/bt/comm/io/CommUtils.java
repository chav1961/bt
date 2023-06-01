package chav1961.bt.comm.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.fazecast.jSerialComm.SerialPort;

import chav1961.purelib.basic.SubstitutableProperties;

public class CommUtils {
	public static final String	DATA_BITS = "dataBits";
	public static final String	DEFAULT_DATA_BITS = "8";
	public static final String	STOP_BITS = "stopBits";
	public static final String	DEFAULT_STOP_BITS = StopBits.one.name();
	public static final String	PARITY = "stopBits";
	public static final String	DEFAULT_PARITY = Parity.none.name();
	public static final String	BAUD_RATE = "baudRate";
	public static final String	DEFAULT_BAUD_RATE = "1200";
	public static final String	FLOW_CONTROL = "flowControl";
	public static final String	DEFAULT_FLOW_CONTROL = "false";

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
	}

	private static final Set<Integer>	AVAILABLE_BAUDS = new HashSet<>(Arrays.asList(110, 300, 600, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200));
	
	public static Iterable<String> commPortsAvailable() {
		final List<String>	result = new ArrayList<>();
		
		for (SerialPort comPort : SerialPort.getCommPorts()) {
			result.add(comPort.getDescriptivePortName());
		}
		return result;
	}

	static SerialPort prepareCommPort(final String name, final SubstitutableProperties props) {
		for (SerialPort comPort : SerialPort.getCommPorts()) {
			if (comPort.getDescriptivePortName().equals(name)) {
				comPort.setComPortParameters(props.getProperty(CommUtils.BAUD_RATE, int.class), 
						props.getProperty(CommUtils.DATA_BITS, int.class), 
						props.getProperty(CommUtils.STOP_BITS, CommUtils.StopBits.class).getStopBitsMode(), 
						props.getProperty(CommUtils.PARITY, CommUtils.Parity.class).getParityMode()
						);
				return comPort;
			}
		}
		return null;
	}
	
	static SubstitutableProperties parseCommQueryParameters(final Hashtable<String,String[]> source) {
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
			result.setProperty(FLOW_CONTROL, Boolean.valueOf(source.remove(BAUD_RATE)[0]).toString());
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
