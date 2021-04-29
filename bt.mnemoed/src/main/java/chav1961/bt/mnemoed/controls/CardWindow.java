package chav1961.bt.mnemoed.controls;

import java.awt.CardLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import javax.swing.JPanel;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.SwingUtils;

public class CardWindow extends JPanel implements LocaleChangeListener {
	private static final long serialVersionUID = 2047364089229365985L;
	private final CardLayout	cardLayout = new CardLayout(); 
	
	public CardWindow(final Localizer localizer) {
		setLayout(cardLayout);
	}

	public void select(final String item) {
		cardLayout.show(this, item);
	}
	
	public InputStream getInputStream() {
		return new InputStream() {@Override public int read() throws IOException {return -1;}};
	}

	public OutputStream getOutputStream() {
		return new OutputStream() {@Override public void write(int b) throws IOException {}};
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		SwingUtils.walkDown(this, (mode, node) -> {
			switch (mode) {
				case ENTER	:
					if (node instanceof LocaleChangeListener) {
						try {((LocaleChangeListener)node).localeChanged(oldLocale, newLocale);
						} catch (LocalizationException e) {
							e.printStackTrace();
						}
						return ContinueMode.SKIP_CHILDREN;
					}
					else {
						return ContinueMode.CONTINUE;
					}
				case EXIT	: return ContinueMode.CONTINUE;
				default		: throw new UnsupportedOperationException("Node enter mode ["+mode+"] is not supported yet"); 
			}
		});
	}
}
