package chav1961.bt.installer.utils;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import chav1961.bt.installer.interfaces.ExitType;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;

public class InstallerUtils {
	public static String class2JarEntryName(final Class<?> clazz) {
		if (clazz == null) {
			throw new NullPointerException("Class to get entry name can't be null or empty");
		}
		else {
			return clazz.getName().replace('.', '/')+".class";
		}
	}
	
	public static URL class2URL(final Class<?> clazz) {
		if (clazz == null) {
			throw new NullPointerException("Class to get entry name can't be null or empty");
		}
		else {
			return clazz.getResource(clazz.getSimpleName()+".class");
		}
	}
	
	public static ExitType callExit(final JDialog parent, final Localizer localizer) {
		if (parent == null) {
			throw new NullPointerException("Parent can't be null");
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			final JPanel	panel = new JPanel(new BorderLayout(5, 5));
			final JLabel	message = new JLabel(localizer.getValue(""));
			final JCheckBox	saveSettings = new JCheckBox(localizer.getValue(""), true);
			
			panel.add(message, BorderLayout.CENTER);
			panel.add(saveSettings, BorderLayout.SOUTH);
			
			switch (new JLocalizedOptionPane(localizer).confirm(parent, panel, localizer.getValue(""), JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION)) {
				case JOptionPane.OK_OPTION		:
					return saveSettings.isSelected() ? ExitType.EXIT_WITH_SAVING : ExitType.EXIT_WITHOUT_SAVING;
				case JOptionPane.CANCEL_OPTION	:
				default :
					return ExitType.CANCEL;
			}
		}
	}
}
