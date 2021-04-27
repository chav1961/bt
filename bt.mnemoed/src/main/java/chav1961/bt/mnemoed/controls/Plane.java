package chav1961.bt.mnemoed.controls;

import java.awt.Graphics;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent;

public class Plane extends JBackgroundComponent {
	private static final long serialVersionUID = -831480745460062449L;

	public Plane(final Localizer localizer) {
		super(localizer);
		setLayout(null);
	}
	
	@Override
	public void paintComponents(Graphics g) {
		super.paintComponents(g);
	}
	
	public void serialize(final JsonStaxPrinter printer) throws PrintingException {
		
	}

	public void deserialize(final JsonStaxParser printer) throws SyntaxException {
		
	}

}
