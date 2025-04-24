package chav1961.bt.comm.utils;

import chav1961.bt.comm.utils.CommUtils.StopBits;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.bt.comm.utils.CommUtils.Parity;
import chav1961.bt.comm.utils.CommUtils.FlowControl;

import java.util.Hashtable;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fazecast.jSerialComm.SerialPort;

public class CommUtilsTest {

	@Test
	public void enumsTest() {
		Assert.assertEquals(StopBits.oneAndHalf, StopBits.valueOf("oneAndHalf"));
		try{StopBits.valueOf("1.5");
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertEquals(StopBits.oneAndHalf, StopBits.valueOf(1.5f));
		try{StopBits.valueOf(1.6f);
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertEquals(StopBits.oneAndHalf, StopBits.of(SerialPort.ONE_POINT_FIVE_STOP_BITS));
		try{StopBits.valueOf(-1);
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(Parity.even, Parity.valueOf("even"));
		try{Parity.valueOf("unknown");
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertEquals(Parity.even, Parity.of(SerialPort.EVEN_PARITY));
		try{Parity.of(-1);
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(FlowControl.XonXoff, FlowControl.valueOf("XonXoff"));
		try{FlowControl.valueOf("unknown");
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(FlowControl.XonXoff, FlowControl.of(SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED));
		try{FlowControl.of(-1);
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void enumerationTest() {
		for (String item : CommUtils.commPortsAvailable()) {
			Assert.assertNotNull(item);
			
			final SubstitutableProperties	sp = CommUtils.getCommPortProperties(item);
			final SubstitutableProperties	newSp = new SubstitutableProperties(sp);

			Assert.assertEquals(item, sp.getProperty(CommUtils.PORT_NAME, String.class, "???"));
			newSp.setProperty(CommUtils.BAUD_RATE, "115200");
			final SerialPort 	port = CommUtils.prepareCommPort(item, newSp);

			Assert.assertEquals(115200, port.getBaudRate());

			try{CommUtils.prepareCommPort(item, null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
		}

		try{CommUtils.getCommPortProperties(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CommUtils.getCommPortProperties("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CommUtils.getCommPortProperties("unknown");
			Assert.fail("Mandatory exception was not detected (unknown 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		final SubstitutableProperties	temp = new SubstitutableProperties();
		
		try{CommUtils.prepareCommPort(null, temp);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CommUtils.prepareCommPort("", temp);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}	

	@Test
	public void parseParametersTest() {
		final Hashtable<String,String[]>	parms = new Hashtable<>();
		
		parms.put(CommUtils.BAUD_RATE, new String[]{"115200"});
		SubstitutableProperties		props = CommUtils.parseCommQueryParameters(parms);
		Assert.assertTrue(props.containsKey(CommUtils.BAUD_RATE));
		Assert.assertEquals(Integer.valueOf(115200), props.getProperty(CommUtils.BAUD_RATE, int.class));
		Assert.assertEquals(0, parms.size());
		
		parms.put(CommUtils.BAUD_RATE, new String[]{"unknown"});
		try{CommUtils.parseCommQueryParameters(parms);
			Assert.fail("Mandatory exception was not detected (illegal BAUD_RATE argument)");
		} catch (IllegalArgumentException exc) {
		}
		parms.put(CommUtils.BAUD_RATE, new String[]{"13666"});
		try{CommUtils.parseCommQueryParameters(parms);
			Assert.fail("Mandatory exception was not detected (illegal BAUD_RATE argument)");
		} catch (IllegalArgumentException exc) {
		}
		parms.remove(CommUtils.BAUD_RATE);
		
		parms.put(CommUtils.DATA_BITS, new String[]{"8"});
		props = CommUtils.parseCommQueryParameters(parms);
		Assert.assertTrue(props.containsKey(CommUtils.DATA_BITS));
		Assert.assertEquals(Integer.valueOf(8), props.getProperty(CommUtils.DATA_BITS, int.class));
		Assert.assertEquals(0, parms.size());
		
		parms.put(CommUtils.DATA_BITS, new String[]{"unknown"});
		try{CommUtils.parseCommQueryParameters(parms);
			Assert.fail("Mandatory exception was not detected (illegal DATA_BITS argument)");
		} catch (IllegalArgumentException exc) {
		}
		parms.put(CommUtils.DATA_BITS, new String[]{"13666"});
		try{CommUtils.parseCommQueryParameters(parms);
			Assert.fail("Mandatory exception was not detected (illegal DATA_BITS argument)");
		} catch (IllegalArgumentException exc) {
		}
		parms.remove(CommUtils.DATA_BITS);

		parms.put(CommUtils.STOP_BITS, new String[]{"oneAndHalf"});
		props = CommUtils.parseCommQueryParameters(parms);
		Assert.assertTrue(props.containsKey(CommUtils.STOP_BITS));
		Assert.assertEquals(StopBits.oneAndHalf, props.getProperty(CommUtils.STOP_BITS, StopBits.class));
		Assert.assertEquals(0, parms.size());
		
		parms.put(CommUtils.STOP_BITS, new String[]{"unknown"});
		try{CommUtils.parseCommQueryParameters(parms);
			Assert.fail("Mandatory exception was not detected (illegal STOP_BITS argument)");
		} catch (IllegalArgumentException exc) {
		}
		parms.put(CommUtils.STOP_BITS, new String[]{"13666"});
		try{CommUtils.parseCommQueryParameters(parms);
			Assert.fail("Mandatory exception was not detected (illegal STOP_BITS argument)");
		} catch (IllegalArgumentException exc) {
		}
		parms.remove(CommUtils.STOP_BITS);

		parms.put(CommUtils.PARITY, new String[]{"odd"});
		props = CommUtils.parseCommQueryParameters(parms);
		Assert.assertTrue(props.containsKey(CommUtils.PARITY));
		Assert.assertEquals(Parity.odd, props.getProperty(CommUtils.PARITY, Parity.class));
		Assert.assertEquals(0, parms.size());
		
		parms.put(CommUtils.PARITY, new String[]{"unknown"});
		try{CommUtils.parseCommQueryParameters(parms);
			Assert.fail("Mandatory exception was not detected (illegal PARITY argument)");
		} catch (IllegalArgumentException exc) {
		}
		parms.remove(CommUtils.PARITY);

		parms.put(CommUtils.BAUD_RATE, new String[]{"115200"});
		props = CommUtils.parseCommQueryParameters(parms);
		Assert.assertTrue(props.containsKey(CommUtils.BAUD_RATE));
		Assert.assertEquals(Integer.valueOf(115200), props.getProperty(CommUtils.BAUD_RATE, int.class));
		Assert.assertEquals(0, parms.size());
		
		parms.put(CommUtils.BAUD_RATE, new String[]{"unknown"});
		try{CommUtils.parseCommQueryParameters(parms);
			Assert.fail("Mandatory exception was not detected (illegal BAUD_RATE argument)");
		} catch (IllegalArgumentException exc) {
		}
		parms.put(CommUtils.BAUD_RATE, new String[]{"13666"});
		try{CommUtils.parseCommQueryParameters(parms);
			Assert.fail("Mandatory exception was not detected (illegal BAUD_RATE argument)");
		} catch (IllegalArgumentException exc) {
		}
		parms.remove(CommUtils.BAUD_RATE);

		parms.put(CommUtils.FLOW_CONTROL, new String[]{"XonXoff"});
		props = CommUtils.parseCommQueryParameters(parms);
		Assert.assertTrue(props.containsKey(CommUtils.FLOW_CONTROL));
		Assert.assertEquals(FlowControl.XonXoff, props.getProperty(CommUtils.FLOW_CONTROL, FlowControl.class));
		Assert.assertEquals(0, parms.size());
		
		parms.put(CommUtils.FLOW_CONTROL, new String[]{"unknown"});
		try{CommUtils.parseCommQueryParameters(parms);
			Assert.fail("Mandatory exception was not detected (illegal FLOW_CONTROL argument)");
		} catch (IllegalArgumentException exc) {
		}
		parms.remove(CommUtils.FLOW_CONTROL);

	}
}
