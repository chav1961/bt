package chav1961.bt.svgeditor.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.ui.swing.SwingUtils;

public class SVGEditor extends JPanel implements LocaleChangeListener, LoggerFacadeOwner, LocalizerOwner, InputStreamGetter, OutputStreamGetter {
	private static final long serialVersionUID = 6904531215744148397L;

	private static final String	APP_COMMAND_PROMPT = "chav1961.bt.svgeditor.screen.SVGEditor.command.prompt";
	private static final String	APP_COMMAND_PROMPT_TT = "chav1961.bt.svgeditor.screen.SVGEditor.command.prompt.tt";
	
	private final Localizer		localizer;
	private final JLabel		commandLabel = new JLabel();
	private final JTextField	command = new JTextField();
	private final SVGCanvas		canvas = new SVGCanvas();
	private byte[]	temp = new byte[0];
	
	public SVGEditor(final Localizer localizer) {
		super(new BorderLayout());
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			final JPanel	commandPanel = new JPanel(new BorderLayout());
			
			this.localizer = localizer;
			commandPanel.add(commandLabel, BorderLayout.WEST);
			commandPanel.add(command, BorderLayout.CENTER);
			canvas.setBackground(Color.black);
			add(new JScrollPane(canvas), BorderLayout.CENTER);
			add(commandPanel, BorderLayout.SOUTH);
			
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		SwingUtils.refreshLocale(canvas, oldLocale, newLocale);
		fillLocalizedStrings();
	}

	@Override
	public Localizer getLocalizer() {
		return localizer;
	}
	
	@Override
	public LoggerFacade getLogger() {
		return SwingUtils.getNearestLogger(getParent());
	}

	public boolean isEmpty() {
		return canvas.getItemCount() == 0;
	}
	
	public <T> T execute(final CharSequence seq, final Class<T> awaited) throws SyntaxException {
		return null;
	}

	private void fillLocalizedStrings() {
		commandLabel.setText(getLocalizer().getValue(APP_COMMAND_PROMPT));
		command.setToolTipText(getLocalizer().getValue(APP_COMMAND_PROMPT_TT));
	}

	@Override
	public OutputStream getOutputContent() throws IOException {
		// TODO Auto-generated method stub
		return new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				super.close();
				temp = toByteArray();
				getLogger().message(Severity.note, "Loading successful");
			}
		};
	}

	@Override
	public InputStream getInputContent() throws IOException {
		// TODO Auto-generated method stub
		return new ByteArrayInputStream(temp) {
			@Override
			public void close() throws IOException {
				super.close();
				getLogger().message(Severity.note, "Storing successful");
			}
		};
	}
}
