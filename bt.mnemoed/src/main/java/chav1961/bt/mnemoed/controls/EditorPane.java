package chav1961.bt.mnemoed.controls;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Locale;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JStateString;

public class EditorPane extends JPanel implements LocaleChangeListener {
	private static final long serialVersionUID = -7722990722613564705L;

	private final ContentMetadataInterface	mdi;
	private final Localizer					localizer;
	private final JToolBar					leftToolbar;
	private final JToolBar					topToolbar;
	private final JToolBar					rightToolbar;
	private final JStateString				state;
	private final Plane						plane;
	
	public EditorPane(final ContentMetadataInterface mdi, final Localizer localizer) throws NullPointerException {
		if (mdi == null) {
			throw new NullPointerException("Metadata interface can't be null");
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.mdi = mdi;
			this.localizer = localizer;
			
			setLayout(new BorderLayout());
			
			this.leftToolbar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.leftToolbar")),JToolBar.class); 
			SwingUtils.assignActionListeners(this.leftToolbar, this);
			this.topToolbar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.topToolbar")),JToolBar.class); 
			this.rightToolbar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.rightToolbar")),JToolBar.class);
			
			this.state = new JStateString(localizer, true);
			this.plane = new Plane(localizer);
			
			leftToolbar.setOrientation(JToolBar.VERTICAL);
			leftToolbar.setFloatable(false);
			add(leftToolbar,BorderLayout.WEST);
			topToolbar.setOrientation(JToolBar.HORIZONTAL);
			topToolbar.setFloatable(false);
			add(topToolbar,BorderLayout.NORTH);
			rightToolbar.setOrientation(JToolBar.VERTICAL);
			rightToolbar.setFloatable(false);
			add(rightToolbar,BorderLayout.EAST);
			state.setBorder(new EtchedBorder());
			add(state,BorderLayout.SOUTH);
			add(new JScrollPane(plane),BorderLayout.CENTER);
			
			state.message(Severity.info, "Loaded");
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		if (leftToolbar instanceof LocaleChangeListener) {
			((LocaleChangeListener)leftToolbar).localeChanged(oldLocale, newLocale);
		}
		if (topToolbar instanceof LocaleChangeListener) {
			((LocaleChangeListener)topToolbar).localeChanged(oldLocale, newLocale);
		}
		if (rightToolbar instanceof LocaleChangeListener) {
			((LocaleChangeListener)rightToolbar).localeChanged(oldLocale, newLocale);
		}
		state.localeChanged(oldLocale, newLocale);
	}

	@OnAction("action:/newComponent")
	private void newComponent() {
		state.message(Severity.info, "component");
	}

	@OnAction("action:/newContainer")
	private void newContainer() {
		state.message(Severity.info, "container");
	}
}
