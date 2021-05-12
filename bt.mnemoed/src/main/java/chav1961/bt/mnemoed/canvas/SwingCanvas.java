package chav1961.bt.mnemoed.canvas;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import chav1961.bt.mnemort.entities.interfaces.EntityInterface;
import chav1961.bt.mnemort.interfaces.EntitiesView;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public class SwingCanvas implements EntitiesView<Graphics> {
	@Override
	public void draw(final Graphics canvas, final int width, final int height, final int windowWidth, final int windowHeight, final EntityInterface... entities) {
		final Graphics2D		g2d = (Graphics2D)canvas;
		final AffineTransform	current = g2d.getTransform();
		
		g2d.setTransform(calculateTransform(g2d, width, height, windowWidth, windowHeight));
		fillBackground(g2d, 0, 0, width, height);
		for (EntityInterface<?> item : entities) {
			try{item.walkDown((mode,node)->process(g2d,mode,node));
			} catch (ContentException e) {
			}
		}
		g2d.setTransform(current);
	}

	private AffineTransform calculateTransform(final Graphics2D g2d, final int width, final int height, final int windowWidth, final int windowHeight) {
		// TODO Auto-generated method stub
		return null;
	}

	private void fillBackground(final Graphics2D g2d, final int x, final int y, final int width, final int height) {
		// TODO Auto-generated method stub
		
	}
	
	private ContinueMode process(final Graphics2D canvas, final NodeEnterMode mode, final EntityInterface<?> item) {
		switch (mode) {
			case ENTER	:
				return ContinueMode.CONTINUE;
			case EXIT	:
				return ContinueMode.CONTINUE;
			default : throw new UnsupportedOperationException("Enter mode ["+mode+"] is not supported yet");
		}
	}
}
