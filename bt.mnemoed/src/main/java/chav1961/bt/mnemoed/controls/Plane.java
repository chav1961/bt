package chav1961.bt.mnemoed.controls;

import javax.swing.JComponent;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class Plane extends JComponent {
	private static final long serialVersionUID = -831480745460062449L;

	public Plane() {
		setLayout(null);
	}
	
	public void serialize(final JsonStaxPrinter printer) throws PrintingException {
		
	}

	public void deserialize(final JsonStaxParser printer) throws SyntaxException {
		
	}

}
