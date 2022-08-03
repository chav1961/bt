package chav1961.bt.paint.control;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.border.EmptyBorder;

import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent;

public class ResizableImageContainer extends JBackgroundComponent {
	private static final long 	serialVersionUID = 1L;
	private static final int	THUMB_SIZE = 3;
	
	public ResizableImageContainer(final Localizer localizer, final Image image) {
		super(localizer);
		if (image == null) {
			throw new NullPointerException(); 
		}
		else {
			setSize(image.getWidth(null), image.getHeight(null));
			setBackgroundImage(image);
			setFillMode(FillMode.FILL);
			setLayout(null);
			setBorder(new EmptyBorder(0,  0,  0,  0));
		}
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
	}
	
	
}
