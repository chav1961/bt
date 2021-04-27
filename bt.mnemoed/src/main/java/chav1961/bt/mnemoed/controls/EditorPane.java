package chav1961.bt.mnemoed.controls;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.swing.useful.JStateString;

public class EditorPane extends JPanel {
	private static final long serialVersionUID = -7722990722613564705L;

	private final LeftToolbar	leftToolbar;
	private final TopToolbar	topToolbar;
	private final RightToolbar	rightToolbar;
	private final JStateString	state;
	private final Plane			plane;
	
	public EditorPane(final Localizer localizer) throws NullPointerException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			setLayout(new BorderLayout());
			this.leftToolbar = new LeftToolbar(localizer);
			add(leftToolbar,BorderLayout.WEST);
			this.topToolbar = new TopToolbar(localizer);
			add(topToolbar,BorderLayout.NORTH);
			this.rightToolbar = new RightToolbar(localizer);
			add(rightToolbar,BorderLayout.EAST);
			this.state = new JStateString(localizer, true);
			add(state,BorderLayout.SOUTH);
			this.plane = new Plane(localizer);
			add(new JScrollPane(plane),BorderLayout.CENTER);
		}
	}
}
