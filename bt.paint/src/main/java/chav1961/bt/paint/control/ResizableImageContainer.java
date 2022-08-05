package chav1961.bt.paint.control;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent;

public class ResizableImageContainer<T extends ResizableImageContainer<?>> extends JBackgroundComponent implements MouseInputListener, FocusListener, KeyListener {
	private static final long 	serialVersionUID = 1L;
	private static final int	THUMB_SIZE = 3;
	private static final Stroke	DASHED = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[] {3, 1}, 0);

	private static enum CursorLocation {
		NORTH(Cursor.N_RESIZE_CURSOR, false, true, 1, true, (c)->new Rectangle(c.getWidth()/2 - THUMB_SIZE/2, 0, THUMB_SIZE, THUMB_SIZE)),
		NORTH_EAST(Cursor.NE_RESIZE_CURSOR, true, true, 1, true, (c)->new Rectangle(c.getWidth() - THUMB_SIZE, 0, THUMB_SIZE, THUMB_SIZE)),
		EAST(Cursor.E_RESIZE_CURSOR, true, false, 0, true, (c)->new Rectangle(c.getWidth() - THUMB_SIZE, c.getHeight()/2 - THUMB_SIZE/2, THUMB_SIZE, THUMB_SIZE)),
		SOUTH_EAST(Cursor.SE_RESIZE_CURSOR, true, true, 0, true, (c)->new Rectangle(c.getWidth() - THUMB_SIZE, c.getHeight() - THUMB_SIZE, THUMB_SIZE, THUMB_SIZE)),
		SOUTH(Cursor.S_RESIZE_CURSOR, false, true, 0, true, (c)->new Rectangle(c.getWidth()/2 - THUMB_SIZE/2, c.getHeight() - THUMB_SIZE, THUMB_SIZE, THUMB_SIZE)),
		SOUTH_WEST(Cursor.SW_RESIZE_CURSOR, true, true, 1, true, (c)->new Rectangle(0, c.getHeight() - THUMB_SIZE, THUMB_SIZE, THUMB_SIZE)),
		WEST(Cursor.W_RESIZE_CURSOR, true, false, 1, true, (c)->new Rectangle(0, c.getHeight()/2 - THUMB_SIZE/2, THUMB_SIZE, THUMB_SIZE)),
		NORTH_WEST(Cursor.NW_RESIZE_CURSOR, true, true, 1, true, (c)->new Rectangle(0, 0, THUMB_SIZE, THUMB_SIZE)),
		CENTER(Cursor.MOVE_CURSOR, true, true, 1, false, (c)->new Rectangle(0, 0, c.getWidth(), c.getHeight()));
		
		private final int		cursorId;
		private final boolean	canChangeX;
		private final boolean	canChangeY;
		private final int		locationMultiplier;
		private final boolean	needPaint;
		private final Function<JComponent,Rectangle>	f;		
		
		private CursorLocation(final int cursorId, final boolean canChangeX, final boolean canChangeY, final int locationMultiplier, final boolean needPaint, final Function<JComponent,Rectangle> f) {
			this.cursorId = cursorId;
			this.canChangeX = canChangeX;
			this.canChangeY = canChangeY;
			this.locationMultiplier = locationMultiplier;
			this.needPaint = needPaint;
			this.f = f;
		}
		
		public int getCursorId() {
			return cursorId;
		}
		
		public boolean canChangeX() {
			return canChangeX;
		}

		public boolean canChangeY() {
			return canChangeY;
		}
		
		public int getLocationMultiplier() {
			return locationMultiplier;
		}
		
		public boolean needPaint() {
			return needPaint;
		}
		
		public Rectangle getRectangle(final JComponent component) {
			if (component == null) {
				throw new NullPointerException("Component can't be null"); 
			}
			else {
				return f.apply(component);
			}
		}
	}
	
	private final Consumer<T> 	consumer;
	private final Point			pressPoint = new Point();
	private CursorLocation		pressLocation;
	
	public ResizableImageContainer(final Localizer localizer, final Image image, final Consumer<T> consumer) {
		super(localizer);
		if (image == null) {
			throw new NullPointerException("Image can't be null"); 
		}
		else if (consumer == null) {
			throw new NullPointerException("Consumer can't be null"); 
		}
		else {
			this.consumer = consumer;

			enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK | AWTEvent.INPUT_METHOD_EVENT_MASK);
			setSize(image.getWidth(null), image.getHeight(null));
			setBackgroundImage(image);
			setFillMode(FillMode.FILL);
			setLayout(null);
			SwingUtils.assignActionKey(this, SwingUtils.KS_EXIT, (e)->exit(), SwingUtils.ACTION_EXIT);
			SwingUtils.assignActionKey(this, SwingUtils.KS_ACCEPT, (e)->accept(), SwingUtils.ACTION_ACCEPT);
			addKeyListener(this);
			addMouseListener(this);
			addMouseMotionListener(this);
			setFocusable(true);
			setRequestFocusEnabled(true);
			addFocusListener(this);
		}
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (e.getClickCount() >= 2) {
			accept();
		}
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		pressPoint.setLocation(e.getPoint());
		pressLocation = getCursorLocation(pressPoint);
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(getCursorLocation(e.getPoint()).getCursorId()));
	}

	private CursorLocation getCursorLocation(final Point point) {
		for (CursorLocation item : CursorLocation.values()) {
			if (item.getRectangle(this).contains(point)) {
				return item;
			}
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		setCursor(Cursor.getDefaultCursor());
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		final int	deltaX = pressLocation.canChangeX() ? e.getPoint().x - pressPoint.x : 0; 
		final int	deltaY = pressLocation.canChangeY() ? e.getPoint().y - pressPoint.y : 0; 

		System.err.println("DeltaX = "+deltaX+", deltaY = "+deltaY);
		System.err.println("DeltaLX = "+(pressLocation.getLocationMultiplier() * deltaX)+", deltaLY = "+(pressLocation.getLocationMultiplier() * deltaY));
		
		setSize(getWidth() + deltaX, getHeight() + deltaY);
		setLocation(getX() + pressLocation.getLocationMultiplier() * deltaX, getY() + pressLocation.getLocationMultiplier() * deltaY);
		pressPoint.setLocation(e.getPoint());
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(getCursorLocation(e.getPoint()).getCursorId()));
	}

	@Override
	public void focusGained(final FocusEvent e) {
	}

	@Override
	public void focusLost(final FocusEvent e) {
		exit();
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		final int	step = (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0 ? 10 : 1;
		
		switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT 	:
				if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
					setSize(Math.max(0, getWidth() - step), getHeight());
				}
				else {
					setLocation(Math.max(0, getX() - step), step);
				}
				break;
			case KeyEvent.VK_RIGHT	:
				if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
					setSize(getWidth() + step, getHeight());
				}
				else {
					setLocation(getX() + step, step);
				}
				break;
			case KeyEvent.VK_DOWN 	:
				if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
					setSize(getWidth(), getHeight() + step);
				}
				else {
					setLocation(getX(), getY() + step);
				}
				break;
			case KeyEvent.VK_UP		:
				if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
					setSize(getWidth(), Math.max(0, getHeight() - step));
				}
				else {
					setLocation(getX(), Math.max(0, getY() - step));
				}
				break;
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		paintFrame((Graphics2D)g);
		paintThumbs((Graphics2D)g);
	}

	private void paintFrame(final Graphics2D g2d) {
		final Stroke	oldStroke = g2d.getStroke();
		
		g2d.setXORMode(Color.WHITE);
		g2d.setStroke(DASHED);
		g2d.drawRect(0, 0, getWidth(), getHeight());
		g2d.setStroke(oldStroke);
		g2d.setPaintMode();
	}
	
	private void paintThumbs(final Graphics2D g2d) {
		final Color	oldColor = g2d.getColor();
		
		for (CursorLocation item : CursorLocation.values()) {
			if (item.needPaint()) {
				final Rectangle	rect = item.getRectangle(this);
				
				g2d.setColor(Color.WHITE);
				g2d.fill(rect);
				g2d.setColor(Color.BLACK);
				g2d.draw(rect);
			}
		}
		g2d.setColor(oldColor);
	}

	private void accept() {
		consumer.accept((T) this);
		exit();
	}
	
	private void exit() {
		getParent().remove(this);
		setVisible(false);
	}
}
