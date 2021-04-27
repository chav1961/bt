package chav1961.bt.mnemoed.controls;

import java.awt.CardLayout;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JPanel;

import chav1961.purelib.i18n.interfaces.Localizer;

public class CardWindow extends JPanel {
	private static final long serialVersionUID = 2047364089229365985L;
	private final CardLayout	cardLayout = new CardLayout(); 
	
	public CardWindow(final Localizer localizer) {
		setLayout(cardLayout);
	}

	public InputStream getInputStream() {
		return null;
	}

	public OutputStream getOutputStream() {
		return null;
	}
	
}
