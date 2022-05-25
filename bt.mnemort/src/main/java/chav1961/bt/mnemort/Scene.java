package chav1961.bt.mnemort;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.UUID;

import javax.swing.JComponent;

import chav1961.bt.mnemort.canvas.swing.SwingCanvas;
import chav1961.bt.mnemort.entities.BasicContainer;
import chav1961.bt.mnemort.interfaces.DrawingCanvas.DrawingMode;

public class Scene extends JComponent {
	private static final long serialVersionUID = -2967936341926838676L;

	private final BasicContainer<SwingCanvas>	container;
	
	public Scene() {
		this.container = new SceneContainer(null, UUID.randomUUID());
		
		final CircleItem	item1 = new CircleItem(null, UUID.randomUUID());
		final CircleItem	item2 = new CircleItem(null, UUID.randomUUID());
		
		item1.getLocation().setX(-10);
		item2.getLocation().setX(10);
		
		this.container.addEntities(item1, item2);
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D		g2d = (Graphics2D)g;
		final AffineTransform	oldAt = g2d.getTransform();
		final AffineTransform	newAt = pickCoordinates(container);
		
		try {
			g2d.setTransform(newAt);
			try(final SwingCanvas	sc = new SwingCanvas(g2d, DrawingMode.BACKGROUND))  {
				container.draw(sc);
			}
		} finally {
			g2d.setTransform(oldAt);
		}
	}

	private AffineTransform pickCoordinates(final BasicContainer<SwingCanvas> container) {
		final Dimension			screenSize = this.getSize();
		final float				width = container.getWidth();
		final float				height = container.getWidth();
		final AffineTransform	result = new AffineTransform();
		
		result.scale(screenSize.getWidth()/width, -screenSize.getHeight()/height);
		result.translate(width/2, -height/2);
		
		return result;
	}
}
