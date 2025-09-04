package chav1961.bt.svgeditor.screen;

import java.util.Locale;

import javax.swing.JPanel;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class SVGCanvas extends JPanel implements LocaleChangeListener {
	private static final long serialVersionUID = -4725462263857678033L;

	public SVGCanvas() {
		super(null);
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		
	}
}
