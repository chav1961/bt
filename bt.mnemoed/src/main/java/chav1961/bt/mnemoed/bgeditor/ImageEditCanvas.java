package chav1961.bt.mnemoed.bgeditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;

import javax.swing.undo.UndoManager;

import chav1961.bt.mnemoed.interfaces.DrawingMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent;
import chav1961.purelib.ui.swing.useful.SelectionFrameManager;

public class ImageEditCanvas extends JBackgroundComponent {
	private static final long 			serialVersionUID = 3367119258812876786L;
	private static final String			NO_IMAGE = "NO IMAGE"; 
	
	private final UndoManager			mgr = new UndoManager();
	private final SelectionFrameManager	smgr = new SelectionFrameManager(this, false);
	private DrawingMode					currentDrawMode = DrawingMode.UNKNOWN;
	private String						prevComment = null, currentComment = "";
	
	public ImageEditCanvas(final Localizer localizer, final int undoHistoryLength) {
		super(localizer);
		if (undoHistoryLength < 0) {
			throw new IllegalArgumentException("Undo history can't be negative"); 
		}
		else {
			mgr.setLimit(undoHistoryLength);
			smgr.addSelectionFrameListener((start, end, rect)->processSelection(start, end, rect));
			setBackground(Color.black);
			setForeground(Color.white);
			super.setFillMode(FillMode.ORIGINAL);
		}
	}

	public void setCurrentDrawMode(final DrawingMode mode) throws IOException {
		if (mode == null) {
			throw new NullPointerException("Drawmode can't be null");
		}
		else {
			switch (getCurrentDrawMode()) {
				case BRUSH		:
					prevComment = "add brush(es)";
					break;
				case ELLIPSE	:
					prevComment = "add ellipse(s)";
					break;
				case ERASE		:
					prevComment = "erase";
					break;
				case FILL		:
					prevComment = "fill";
					break;
				case LINE		:
					prevComment = "add line(s)";
					break;
				case PEN		:
					prevComment = "add curve(s)";
					break;
				case RECT		:
					prevComment = "add rectangle(s)";
					break;
				case TEXT		:
					prevComment = "add text(s)";
					break;
				case UNKNOWN	:
					prevComment = null;
					break;
				default :
					throw new UnsupportedOperationException("Old drawing mode ["+getCurrentDrawMode()+"] is not supported yet");
			}
			currentDrawMode = mode;
			switch (getCurrentDrawMode()) {
				case BRUSH		:
					currentComment = "remove brush(es)";
					break;
				case ELLIPSE	:
					currentComment = "remove ellipse(s)";
					break;
				case ERASE		:
					currentComment = "revert erasing";
					break;
				case FILL		:
					currentComment = "revert filling";
					break;
				case LINE		:
					currentComment = "remove line(s)";
					break;
				case PEN		:
					currentComment = "remove curve(s)";
					break;
				case RECT		:
					currentComment = "remove rectangle(s)";
					break;
				case TEXT		:
					currentComment = "remove text(s)";
					break;
				case UNKNOWN	:
					currentComment = null;
					break;
				default :
					throw new UnsupportedOperationException("New drawing mode ["+getCurrentDrawMode()+"] is not supported yet");
			}
			if (currentComment == null) {
				if (prevComment == null) {
					getUndoManager().addEdit(new ImageUndoEdit(currentComment, getBackgroundImage(), (i)->super.setBackgroundImage(i)));
				}
				else {
					getUndoManager().addEdit(new ImageUndoEdit(currentComment, prevComment, getBackgroundImage(), (i)->super.setBackgroundImage(i)));
				}
			}
			else {
				getUndoManager().discardAllEdits();
			}
		}
	}
	
	public DrawingMode getCurrentDrawMode() {
		return currentDrawMode;
	}
	
	public UndoManager getUndoManager() {
		return mgr;
	}

	@Override
	public void setBackgroundImage(final Image image) {
		super.setBackgroundImage(image);
		setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
		setSize(new Dimension(image.getWidth(null), image.getHeight(null)));
		repaint();
	}
	
	@Override
	public void setFillMode(final FillMode fill) {
		throw new IllegalStateException("Dont' use this method, fill mode always will be ORIGINAL"); 
	}
	
	protected void processSelection(final Point start, final Point end, final Rectangle rect) {
		final Graphics2D	g2d = (Graphics2D)getBackgroundImage().getGraphics();
		final Color			oldColor = g2d.getColor(); 
		
		g2d.setColor(getForeground());
		switch (getCurrentDrawMode()) {
			case BRUSH		:
				break;
			case ELLIPSE	:
				g2d.drawOval(rect.x, rect.y, rect.width, rect.height);
				break;
			case ERASE		:
				break;
			case FILL		:
				break;
			case LINE		:
				g2d.drawLine(start.x, start.y, end.x, end.y);
				break;
			case PEN		:
				break;
			case RECT		:
				g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
				break;
			case TEXT		:
				break;
			case UNKNOWN	:
				break;
			default :
				throw new UnsupportedOperationException("Drawing mode ["+getCurrentDrawMode()+"] is not supported yet");
		}
		g2d.setColor(oldColor);
		repaint();
	}

	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D	g2d = (Graphics2D)g;

		if (getBackgroundImage() == null) {
			final Color		oldColor = g2d.getColor();
			final Rectangle	rect = g2d.getFontMetrics().getStringBounds(NO_IMAGE, g2d).getBounds();
			
			g2d.setColor(getBackground());
			g2d.fillRect(0, 0, getWidth(), getHeight());
			g2d.setColor(getForeground());
			g2d.drawString(NO_IMAGE, getWidth()/2 - rect.width/2, getHeight()/2 - rect.height/2);
			g2d.setColor(oldColor);
		}
		else {
			super.paintComponent(g2d);
			
			if (getCurrentDrawMode() != DrawingMode.UNKNOWN) {
				g2d.setXORMode(Color.white);
				smgr.paintSelection(g2d);
				g2d.setPaintMode();
			}
		}
	}	
}
