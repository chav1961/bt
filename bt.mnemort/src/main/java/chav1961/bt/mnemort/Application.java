package chav1961.bt.mnemort;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import chav1961.purelib.ui.swing.SwingUtils;

public class Application extends JFrame {
	private static final long serialVersionUID = 4789618233414662196L;

	public Application() {
		getContentPane().add(new Scene(),BorderLayout.CENTER);
		SwingUtils.centerMainWindow(this,0.75f);
	}
	
	public static void main(String[] args) {
		final Application	app = new Application();
		
		app.setVisible(true);
	}
}
