package chav1961.bt.paint.control;

import java.awt.BasicStroke;
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
	private int							lineThickness = 1;
	private LineStroke					lineStroke = LineStroke.SOLID;
	private LineCaps					lineCaps = LineCaps.BUTT;
	private LineJoin					lineJoin = LineJoin.MITER;
	private boolean						fillContours = false;
	private String						prevComment = null, currentComment = "";
	
	public static enum LineStroke {
		SOLID, DASHED, DOTTED;
	}

	public static enum LineCaps {
		BUTT(BasicStroke.CAP_BUTT), 
		ROUND(BasicStroke.CAP_ROUND), 
		SQUARE(BasicStroke.CAP_SQUARE);
		
		private final int	capsType;
		
		private LineCaps(final int capsType) {
			this.capsType = capsType;
		}
		
		public int getCapsType() {
			return capsType;
		}
	}

	public static enum LineJoin {
		MITER(BasicStroke.JOIN_MITER), 
		ROUND(BasicStroke.JOIN_ROUND), 
		BEVEL(BasicStroke.JOIN_BEVEL);

		private final int	joinType;
		
		private LineJoin(final int joinType) {
			this.joinType = joinType;
		}
		
		public int getJoinType() {
			return joinType;
		}
	}
	
	public ImageEditCanvas(final Localizer localizer) {
		super(localizer);
		setBackground(Color.black);
		setForeground(Color.white);
		super.setFillMode(FillMode.ORIGINAL);
	}

	public SelectionFrameManager getSelectionManager() {
		return smgr;
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

	public LineCaps getLineCaps() {
		return lineCaps;
	}
	
	public void setLineCaps(final LineCaps caps) {
		if (caps == null) {
			throw new NullPointerException("Line caps to set can't be null");
		}
		else {
			lineCaps = caps;
			listeners.fireEvent((l)->l.stateChanged(ce));
		}
	}

	public LineJoin getLineJoin() {
		return lineJoin;
	}	
	
	public void setLineJoin(final LineJoin join) {
		if (join == null) {
			throw new NullPointerException("Line join to set can't be null");
		}
		else {
			lineJoin = join;
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
	
//	protected void processSelection(final Point start, final Point end, final Rectangle rect) {
//		final Graphics2D	g2d = (Graphics2D)getBackgroundImage().getGraphics();
//		final Color			oldColor = g2d.getColor(); 
//		
//		g2d.setColor(getForeground());
//		switch (getCurrentDrawMode()) {
//			case BRUSH		:
//				break;
//			case ELLIPSE	:
//				g2d.drawOval(rect.x, rect.y, rect.width, rect.height);
//				break;
//			case FILL		:
//				break;
//			case LINE		:
//				g2d.drawLine(start.x, start.y, end.x, end.y);
//				break;
//			case PEN		:
//				break;
//			case RECT		:
//				g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
//				break;
//			case TEXT		:
//				break;
//			case SELECT		:
//				break;
//			case UNKNOWN	:
//				break;
//			default :
//				throw new UnsupportedOperationException("Drawing mode ["+getCurrentDrawMode()+"] is not supported yet");
//		}
//		g2d.setColor(oldColor);
//		repaint();
//	}

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
			
			if (smgr.isVisible()) {
				g2d.setXORMode(Color.white);
				smgr.paintSelection(g2d);
				g2d.setPaintMode();
			}
			g2d.setColor(oldColor);
		}
	}

}
