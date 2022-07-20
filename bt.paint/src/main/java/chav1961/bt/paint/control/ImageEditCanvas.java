package chav1961.bt.paint.control;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;

import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import chav1961.bt.paint.control.ImageUtils.DrawingType;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent;
import chav1961.purelib.ui.swing.useful.SelectionFrameManager;
import chav1961.purelib.ui.swing.useful.interfaces.SelectionFrameListener.SelectionStyle;

public class ImageEditCanvas extends JBackgroundComponent {
	private static final long 			serialVersionUID = 3367119258812876786L;
	private static final String			NO_IMAGE = "NO IMAGE"; 
	
	private final LightWeightListenerList<ChangeListener>	listeners = new LightWeightListenerList<>(ChangeListener.class);
	private final SelectionFrameManager	smgr = new SelectionFrameManager(this, false);
	private final ChangeEvent			ce = new ChangeEvent(this);
	private DrawingType					currentDrawMode = DrawingType.UNKNOWN;
	private int							lineThickness = 1;
	private LineStroke					lineStroke = LineStroke.SOLID;
	private boolean						fillContours = false;
	private Rectangle					selection = null;
	private String						prevComment = null, currentComment = "";
	
	public static enum LineStroke {
		SOLID, DASHED, DOTTED;
	}
	
	public ImageEditCanvas(final Localizer localizer) {
		super(localizer);
		smgr.addSelectionFrameListener((style, start, end, parameters)->processSelection(start, end, (Rectangle)parameters[0]));
		setBackground(Color.black);
		setForeground(Color.white);
		super.setFillMode(FillMode.ORIGINAL);
	}

	public void setCurrentDrawMode(final DrawingType mode) throws IOException {
		if (mode == null) {
			throw new NullPointerException("Drawmode can't be null");
		}
		else {
			switch (getCurrentDrawMode()) {
				case SELECT		:
					prevComment = "add selection";
					break;
				case BRUSH		:
					prevComment = "add brush(es)";
					break;
				case ELLIPSE	:
					prevComment = "add ellipse(s)";
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
			selection = null;
			switch (getCurrentDrawMode()) {
				case SELECT		:
					smgr.setSelectionStyle(SelectionStyle.RECTANGLE);
					smgr.enableSelection(true);
					currentComment = "remove selection(s)";
					break;
				case ELLIPSE	:
					currentComment = "remove ellipse(s)";
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
					smgr.setSelectionStyle(SelectionStyle.RECTANGLE);
					smgr.enableSelection(true);
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
//			if (currentComment == null) {
//				getUndoManager().addEdit(new ImageUndoEdit(currentComment, prevComment, getBackgroundImage(), (i)->super.setBackgroundImage(i)));
//			}
//			else {
//				getUndoManager().discardAllEdits();
//			}
			listeners.fireEvent((l)->l.stateChanged(ce));
		}
	}
	
	public DrawingType getCurrentDrawMode() {
		return currentDrawMode;
	}
	
	@Override
	public void setForeground(final Color color) {
		super.setForeground(color);
		listeners.fireEvent((l)->l.stateChanged(ce));
	}

	@Override
	public void setBackground(final Color color) {
		super.setBackground(color);
		listeners.fireEvent((l)->l.stateChanged(ce));
	}
	
	@Override
	public void setBackgroundImage(final Image image) {
		super.setBackgroundImage(image);
		selection = null;
		setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
		setSize(new Dimension(image.getWidth(null), image.getHeight(null)));
		listeners.fireEvent((l)->l.stateChanged(ce));
		repaint();
	}

	@Override
	public void setFont(final Font font) {
		super.setFont(font);
		listeners.fireEvent((l)->l.stateChanged(ce));
	}
	
	@Override
	public void setFillMode(final FillMode fill) {
		throw new IllegalStateException("Dont' use this method, fill mode always will be ORIGINAL"); 
	}

	public int getLineThickness() {
		return lineThickness;
	}
	
	public void setLineThickness(final int thickness) {
		if (thickness <= 0) {
			throw new IllegalArgumentException("Line thickness ["+thickness+"] must be positive"); 
		}
		else {
			lineThickness = thickness;
			listeners.fireEvent((l)->l.stateChanged(ce));
		}
	}
	
	public LineStroke getLineStroke() {
		return lineStroke;
	}
	
	public void setLineStroke(final LineStroke stroke) {
		if (stroke == null) {
			throw new NullPointerException("Line stroke to set can't be null");
		}
		else {
			lineStroke = stroke;
			listeners.fireEvent((l)->l.stateChanged(ce));
		}
	}
	
	public boolean getContourFilling() {
		return fillContours;
	}
	
	public void setContourFilling(final boolean contourFilling) {
		fillContours = contourFilling;
		listeners.fireEvent((l)->l.stateChanged(ce));
	}

	public Rectangle getSelection() {
		return selection;
	}
	
	public void addChangeListener(final ChangeListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			listeners.addListener(l);
		}
	}

	public void removeChangeListener(final ChangeListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			listeners.removeListener(l);
		}
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
			case SELECT		:
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
			final Color		oldColor = g2d.getColor();

			super.paintComponent(g2d);
			g2d.setColor(Color.BLACK);
			g2d.drawRect(0, 0, getBackgroundImage().getWidth(null), getBackgroundImage().getHeight(null));
			
			if (getCurrentDrawMode() != DrawingType.UNKNOWN) {
				g2d.setXORMode(Color.white);
				smgr.paintSelection(g2d);
				g2d.setPaintMode();
			}
			g2d.setColor(oldColor);
		}
	}	
}
