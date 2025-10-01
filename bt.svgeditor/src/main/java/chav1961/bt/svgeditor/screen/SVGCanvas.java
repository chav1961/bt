package chav1961.bt.svgeditor.screen;

import java.awt.Color;
import java.awt.Cursor;
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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.Scrollable;

import chav1961.bt.svgeditor.interfaces.StateChangedListener;
import chav1961.bt.svgeditor.primitives.LineWrapper;
import chav1961.bt.svgeditor.primitives.PrimitiveWrapper;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class SVGCanvas extends JComponent implements LocaleChangeListener, MouseListener, MouseMotionListener, MouseWheelListener {
	private static final long 	serialVersionUID = -4725462263857678033L;
	private static final double DEFAULT_MOUSE_WHEEL_SPEED = 10;

	private final LightWeightListenerList<StateChangedListener>	listeners = new LightWeightListenerList<>(StateChangedListener.class);
	private final double		mouseWheelSpeed;
	private final List<ItemDescriptor>	content = new ArrayList<>();
	private final float			delta = 3;
	private Dimension			conventionalSize = new Dimension(100,100);
	private double				currentScale = 1;
	private Point2D				startMousePoint = new Point(0,0);
	private Point2D				currentMousePoint = new Point(0,0);
	private Actions				currentAction = Actions.NONE;
	private PrimitiveWrapper	currentWrapper = null;
	private double				currentEntityScale = 1;

	private static enum Actions {
		NONE,
		MOVE,
		ROTATE,
		SCALE
	}
	
	public SVGCanvas() {
		this(DEFAULT_MOUSE_WHEEL_SPEED);
	}
	
	public SVGCanvas(final double mouseWheelSpeed) {
		content.add(new ItemDescriptor(new LineWrapper(20,20,80,80)));
		this.mouseWheelSpeed = mouseWheelSpeed;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		setEnabled(true);
		setFocusable(true);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		refreshDimension();
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		switch (currentAction) {
			case NONE	:
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
				break;
			case ROTATE	:
				break;
			case MOVE	:
				currentAction = Actions.SCALE;
			case SCALE	:
				switch (e.getScrollType()) {
					case MouseWheelEvent.WHEEL_UNIT_SCROLL :
						scaleEntity(currentWrapper, startMousePoint, e.getPreciseWheelRotation());
						break;
					case MouseWheelEvent.WHEEL_BLOCK_SCROLL:
						scaleEntity(currentWrapper, startMousePoint, e.getPreciseWheelRotation() * e.getScrollAmount());
						break;
					default:
						throw new UnsupportedOperationException("Scroll type ["+e.getScrollType()+"] is not supported yet");
				}
				break;
			default :
				throw new UnsupportedOperationException("Action ["+currentAction+"] is not supported yet");
		}
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		final Point2D		p = toScaledPoint(e.getPoint());

		currentMousePoint = p;
		switch (currentAction) {
			case MOVE	:
				moveEntity(currentWrapper, startMousePoint, p);
				listeners.fireEvent((l)->l.locationChanged(e));
				break;
			case NONE	:
				break;
			case ROTATE	:
				final double angle	= Math.atan2(getEffectiveY(currentAction, p.getY()-startMousePoint.getY()), getEffectiveX(currentAction, p.getX()-startMousePoint.getX()));
				
				rotateEntity(currentWrapper, startMousePoint, getEffectiveAngle(currentAction, angle));				
				break;
			case SCALE	:
				break;
			default :
				throw new UnsupportedOperationException("Action ["+currentAction+"] is not supported yet");
		}
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		highlightSelections(e);
		currentMousePoint = toScaledPoint(e.getPoint());
		listeners.fireEvent((l)->l.locationChanged(e));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		final Point2D	p = toScaledPoint(e.getPoint());
		
		startMousePoint = p;
		currentWrapper = null;
		currentAction = Actions.NONE;
				
		for(ItemDescriptor item : content) {
			if (item.wrapper.isAbout(p, delta)) {
				currentWrapper = item.wrapper;
				currentAction = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0 ? Actions.ROTATE : Actions.MOVE;
				currentEntityScale = 1;				
				currentWrapper.startDrag(p);
				break;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		switch (currentAction) {
			case MOVE	:
				currentWrapper.endDrag();
				currentWrapper.commitChanges();
				currentWrapper.clearTransform();
				currentWrapper = null;
				break;
			case NONE	:
				break;
			case ROTATE	:
				currentWrapper.endDrag();
				currentWrapper.commitChanges();
				currentWrapper.clearTransform();
				currentWrapper = null;
				break;
			case SCALE	:
				currentWrapper.endDrag();
				currentWrapper.commitChanges();
				currentWrapper.clearTransform();
				currentWrapper = null;
				break;
			default:
				throw new UnsupportedOperationException("Action ["+currentAction+"] is not supported yet");
		}
		highlightSelections(e);
		currentAction = Actions.NONE;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void addStateChangedListener(final StateChangedListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			listeners.addListener(l);
		}
	}

	public void removeStateChangedListener(final StateChangedListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			listeners.removeListener(l);
		}
	}

	public void add(final PrimitiveWrapper wrapper) {
		if (wrapper == null) {
			throw new NullPointerException("Wrapper to add can't be null");
		}
		else {
			content.add(new ItemDescriptor(wrapper));
			refreshDimension();
		}
	}

	public boolean isSelected(final PrimitiveWrapper wrapper) {
		if (wrapper == null) {
			throw new NullPointerException("Wrapper to test can't be null");
		}
		else {
			for(ItemDescriptor item : content) {
				if (item.wrapper.equals(wrapper)) {
					return item.selected;
				}
			}
			return false;
		}
	}
	
	public void setSelected(final PrimitiveWrapper wrapper, final boolean selected) {
		if (wrapper == null) {
			throw new NullPointerException("Wrapper to set can't be null");
		}
		else {
			for(ItemDescriptor item : content) {
				if (item.wrapper.equals(wrapper)) {
					item.selected = selected;
					refreshDimension();
					return;
				}
			}
		}
	}
	
	public void forEach(final Consumer<PrimitiveWrapper> consumer) {
		if (consumer == null) {
			throw new NullPointerException("Consumer can't be null");
		}
		else {
			for(ItemDescriptor item : content.toArray(new ItemDescriptor[content.size()])) {
				consumer.accept(item.wrapper);
			}
		}
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

	public Point2D startMousePoint() {
		return startMousePoint;
	}
	
	public Point2D currentMousePoint() {
		return currentMousePoint;
	}

	public double currentScale() {
		return currentScale;
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D		g2d = (Graphics2D)g;
		final AffineTransform	oldAt = g2d.getTransform();
		
		pickCoordinates(g2d);
		fillBackground(g2d);

		for(ItemDescriptor item : content) {
			item.wrapper.draw(g2d, this, item.selected);
		}
		
//		g2d.setColor(Color.red);
//		g2d.drawLine(0, 0, (int)getConventionalSize().getWidth(), (int)getConventionalSize().getHeight());
		
		g2d.setTransform(oldAt);
	}

	protected Point2D toScaledPoint(final Point2D point) {
		return new Point2D.Double(point.getX() * currentScale, point.getY() * currentScale);
	}
	
	private void scaleCanvas(final double value) {
		final double	oldScale = currentScale;
		
		currentScale = calculateScale(currentScale, value);
		refreshDimension();
		if (oldScale != currentScale) {
			listeners.fireEvent((l)->l.scaleChanged(oldScale, currentScale));
		}
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
		
		at.translate(0, 0);
		at.scale(size.getWidth()/awaitedSize.getWidth(), size.getHeight()/awaitedSize.getHeight());
		g2d.setTransform(at);
	}

	private void fillBackground(final Graphics2D g2d) {
		final Color	oldColor = g2d.getColor(); 
		final Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, getConventionalSize().getWidth(), getConventionalSize().getHeight());
		
		g2d.setColor(getBackground());
		g2d.fill(rect);
		g2d.setColor(oldColor);
	}

	private void highlightSelections(final MouseEvent e) {
		final Point2D		p = toScaledPoint(e.getPoint());
		PrimitiveWrapper	oldItem = null, newItem = null;
		Cursor	c = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		
		for(ItemDescriptor item : content) {
			if (item.wrapper.isHighlight()) {
				oldItem = item.wrapper;
				break;
			}
		}
		
		for(ItemDescriptor item : content) {
			if (item.wrapper.isAbout(p, delta)) {
				c = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
				newItem = item.wrapper;
				break;
			}
		}
		setCursor(c);
		if (oldItem != newItem) {
			if (oldItem != null) {
				oldItem.setHighlight(false);
			}
			if (newItem != null) {
				newItem.setHighlight(true);
			}
			repaint();
		}
	}

	private void moveEntity(final PrimitiveWrapper entity, final Point2D anchor, final Point2D current) {
		final AffineTransform	at = new AffineTransform();
		
		at.translate(getEffectiveX(Actions.MOVE, current.getX()) - anchor.getX(), 
					 getEffectiveY(Actions.MOVE, current.getY()) - anchor.getY());
		entity.setTransform(at);
		repaint();
	}
	
	
	private void scaleEntity(final PrimitiveWrapper entity, final Point2D anchor, final double scale) {
		final AffineTransform	at = new AffineTransform();
		
		currentEntityScale = getEffectiveScale(Actions.SCALE, calculateScale(currentEntityScale, scale));
		at.translate(anchor.getX(), anchor.getY());
		at.scale(currentEntityScale, currentEntityScale);
		at.translate(-anchor.getX(), -anchor.getY());
		
		entity.setTransform(at);
		repaint();
	}

	private void rotateEntity(final PrimitiveWrapper entity, final Point2D anchor, double angle) {
		final AffineTransform	at = new AffineTransform();
		
		at.translate(anchor.getX(), anchor.getY());
		at.rotate(angle);
		at.translate(-anchor.getX(), -anchor.getY());
		
		entity.setTransform(at);
		repaint();
	}
	
	private double getEffectiveX(final Actions action, final double x) {
		return x;
	}

	private double getEffectiveY(final Actions action, final double y) {
		return y;
	}

	private double getEffectiveScale(final Actions action, final double scale) {
		return scale;
	}

	private double getEffectiveAngle(final Actions action, final double angle) {
		return angle;
	}

	private double calculateScale(final double currentScale, final double step) {
		double	scale = currentScale;
		
		if (step < 0) {
			scale /= (1 - step/mouseWheelSpeed);
		}
		else if (step == 0) {
			scale = 1;
		}
		else {
			scale *= (1 + step/mouseWheelSpeed);
		}
		if (scale < 0.001) {
			scale = 0.001;
		}
		else if (scale > 10) {
			scale = 10;
		}
		return scale;
	}
	
	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
		
	}

	private static class ItemDescriptor {
		private final PrimitiveWrapper	wrapper;
		private boolean					selected;
		
		public ItemDescriptor(final PrimitiveWrapper wrapper) {
			this(wrapper, false);
		}
		
		public ItemDescriptor(final PrimitiveWrapper wrapper, final boolean selected) {
			this.wrapper = wrapper;
			this.selected = selected;
		}
	}
}
