package chav1961.bt.paint.control;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import chav1961.purelib.ui.swing.SwingUtils;

class JCommandField extends JTextField {
	private static final long 	serialVersionUID = 1L;
	private static final int	MAX_HISTORY_LENGTH = 50;
	
	private final List<String>	history = new ArrayList<>();
	private int					historyCursor = -1;
	
	public JCommandField() {
		SwingUtils.assignActionKey(this, SwingUtils.KS_UP, (e)->getHistory(+1), SwingUtils.ACTION_UP);
		SwingUtils.assignActionKey(this, SwingUtils.KS_DOWN, (e)->getHistory(-1), SwingUtils.ACTION_DOWN);
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
