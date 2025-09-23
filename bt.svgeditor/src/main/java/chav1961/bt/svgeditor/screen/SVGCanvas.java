package chav1961.bt.svgeditor.screen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.Scrollable;

import chav1961.bt.svgeditor.primitives.LineWrapper;
import chav1961.bt.svgeditor.primitives.PrimitiveWrapper;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class SVGCanvas extends JComponent implements LocaleChangeListener, MouseListener, MouseMotionListener, MouseWheelListener {
	private static final long 	serialVersionUID = -4725462263857678033L;
	private static final double DEFAULT_MOUSE_WHEEL_SPEED = 10;

	private final double	mouseWheelSpeed;
	private final List<PrimitiveWrapper>	content = new ArrayList<>();
	private Dimension		conventionalSize = new Dimension(100,100);
	private double			currentScale = 1;

	public SVGCanvas() {
		this(DEFAULT_MOUSE_WHEEL_SPEED);
	}
	
	public SVGCanvas(final double mouseWheelSpeed) {
		content.add(new LineWrapper(0,0,100,100));
		this.mouseWheelSpeed = mouseWheelSpeed;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		setEnabled(true);
		setFocusable(true);
		refreshDimension();
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		switch (e.getScrollType()) {
			case MouseWheelEvent.WHEEL_UNIT_SCROLL :
				scaleCanvas(e.getPreciseWheelRotation());
				break;
			case MouseWheelEvent.WHEEL_BLOCK_SCROLL:
				scaleCanvas(e.getPreciseWheelRotation() * e.getScrollAmount());
				break;
			default:
				throw new UnsupportedOperationException("Scroll type ["+e.getScrollType()+"] is not supported yet");
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public Dimension getConventionalSize() {
		return conventionalSize;
	}
	
	public int getItemCount() {
		return content.size();
	}
	
	public void setConventionalSize(final Dimension size) {
		if (size == null) {
			throw new NullPointerException("Conventional size to set can't be null");
		}
		else {
			this.conventionalSize = size;
			refreshDimension();
		}
	}

	public Dimension getEffectiveSize() {
		return new Dimension((int)(getConventionalSize().getWidth() / currentScale), (int)(getConventionalSize().getHeight() / currentScale));
	}
	
	public Point toConventinalPoint(final Point source) {
		if (source == null) {
			throw new NullPointerException("Source point can't be null");
		}
		else {
			return new Point((int)(source.getX() / currentScale), (int)(getHeight() - source.getY() / currentScale));
		}
	}

	public Point fromConventinalPoint(final Point source) {
		if (source == null) {
			throw new NullPointerException("Source point can't be null");
		}
		else {
			return new Point((int)(source.getX() * currentScale), (int)(getHeight() - source.getY() * currentScale));
		}
	}

	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D		g2d = (Graphics2D)g;
		final AffineTransform	oldAt = g2d.getTransform();
		
		pickCoordinates(g2d);
		fillBackground(g2d);

		for(PrimitiveWrapper item : content) {
			item.draw(g2d, this);
		}
		
//		g2d.setColor(Color.red);
//		g2d.drawLine(0, 0, (int)getConventionalSize().getWidth(), (int)getConventionalSize().getHeight());
		
		g2d.setTransform(oldAt);
	}
	
	private void scaleCanvas(final double value) {
		if (value < 0) {
			currentScale /= (1 - value/mouseWheelSpeed);
		}
		else if (value == 0) {
			currentScale = 1;
		}
		else {
			currentScale *= (1 + value/mouseWheelSpeed);
		}
		if (currentScale < 0.001) {
			currentScale = 0.001;
		}
		else if (currentScale > 10) {
			currentScale = 10;
		}
		refreshDimension();
	}

	private void refreshDimension() {
		setSize(getEffectiveSize());
		setPreferredSize(getEffectiveSize());
		repaint();
	}

	private void pickCoordinates(final Graphics2D g2d) {
		final AffineTransform	at = new AffineTransform(g2d.getTransform());
		final Dimension	awaitedSize = getConventionalSize();
		final Dimension	effectiveSize = getEffectiveSize();
		final Dimension	realSize = getSize();
		final Dimension	size = new Rectangle(new Point(0,0), effectiveSize).intersection(new Rectangle(new Point(0,0), realSize)).getSize();
		
		at.translate(0, getHeight());
		at.scale(size.getWidth()/awaitedSize.getWidth(), -size.getHeight()/awaitedSize.getHeight());
		g2d.setTransform(at);
	}

	private void fillBackground(final Graphics2D g2d) {
		final Color	oldColor = g2d.getColor(); 
		final Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, getConventionalSize().getWidth(), getConventionalSize().getHeight());
		
		g2d.setColor(getBackground());
		g2d.fill(rect);
		g2d.setColor(oldColor);
	}
	
	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
		
	}

}
