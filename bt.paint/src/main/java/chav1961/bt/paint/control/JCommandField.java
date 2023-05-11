package chav1961.bt.paint.control;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.ui.swing.SwingUtils;

class JCommandField extends JTextField {
	private static final long 	serialVersionUID = 1L;
	private static final int	MAX_HISTORY_LENGTH = 50;
	private static final String	KEY_COMMAND_HELP = "chav1961.bt.paint.bgeditor.JCommandField.help";
	
	private final List<String>	history = new ArrayList<>();
	private int					historyCursor = -1;
	
	public JCommandField() {
		SwingUtils.assignActionKey(this, SwingUtils.KS_UP, (e)->getHistory(+1), SwingUtils.ACTION_UP);
		SwingUtils.assignActionKey(this, SwingUtils.KS_DOWN, (e)->getHistory(-1), SwingUtils.ACTION_DOWN);
		SwingUtils.assignActionKey(this, SwingUtils.KS_HELP, (e)->{
			try{
				SwingUtils.showCreoleHelpWindow(JCommandField.this, 
						LocalizerFactory.getLocalizer(URI.create("i18n:xml:root://"+getClass().getCanonicalName()+"/chav1961/bt/paint/i18n/localization.xml")), 
						KEY_COMMAND_HELP);
			} catch (IOException exc) {
				SwingUtils.getNearestLogger(JCommandField.this).message(Severity.error, exc, exc.getLocalizedMessage());
			}
		}, SwingUtils.ACTION_HELP);
	}

	@Override
	protected void fireActionPerformed() {
		final String	text = getText();
		
		if (!text.trim().isEmpty()) {
			history.add(0,text);
			historyCursor = -1;
			while (history.size() >= MAX_HISTORY_LENGTH) {
				history.remove(history.size() - 1);
			}
		}
		super.fireActionPerformed();
	}

	private void getHistory(final int direction) {
		if (direction < 0 && historyCursor >= 1) {
			setText(history.get(historyCursor += direction));
		}
		else if (direction > 0 && historyCursor < history.size() - 1) {
			setText(history.get(historyCursor += direction));
		}
	}
}
