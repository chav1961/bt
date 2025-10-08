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
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.JComponent;

import chav1961.bt.svgeditor.interfaces.StateChangedListener;
import chav1961.bt.svgeditor.primitives.PrimitiveWrapper;
import chav1961.purelib.basic.SubstitutableProperties.PropertyGroupChangeEvent;
import chav1961.purelib.basic.SubstitutableProperties.PropertyGroupChangeListener;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.concurrent.LightWeightListenerList.LightWeightListenerCallback;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.Undoable;

public class SVGCanvas extends JComponent implements LocaleChangeListener, LoggerFacadeOwner, LocalizerOwner, PropertyGroupChangeListener {
	private static final long 	serialVersionUID = -4725462263857678033L;
	private static final double DEFAULT_MOUSE_WHEEL_SPEED = 10;

	private final LightWeightListenerList<StateChangedListener>	listeners = new LightWeightListenerList<>(StateChangedListener.class);
	private final Localizer		localizer;
	private final double		mouseWheelSpeed;
	private final List<ItemDescriptor>	content = new ArrayList<>();
	private final float			delta = 3;
	private final CanvasHistory	cHistory = new CanvasHistory();
	private final List<MouseManager>	mmList = new ArrayList<>();
	private Dimension			conventionalSize = new Dimension(100,100);
	private double				currentScale = 1;
	private Point2D				currentMousePoint = new Point2D.Double(0, 0);
	private CanvasSnapshot[]	currentCanvasSnapshot = null;

	public SVGCanvas(final Localizer localizer) {
		this(localizer, DEFAULT_MOUSE_WHEEL_SPEED);
	}
	
	public SVGCanvas(final Localizer localizer, final double mouseWheelSpeed) {
		if (localizer == null) {
			throw new NullPointerException("Loalzier can't be null");
		}
		else if (mouseWheelSpeed <= 0) {
			throw new IllegalArgumentException("Mouse wheel speed ["+mouseWheelSpeed+"] must be greater than 0");
		}
		else {
			this.localizer = localizer;
			this.mouseWheelSpeed = mouseWheelSpeed;
			this.mmList.add(new EditManager(this, mouseWheelSpeed));
			addMouseMotionListener(new MouseMotionListener() {
				@Override
				public void mouseMoved(MouseEvent e) {
					currentMousePoint = e.getPoint();
				}
				
				@Override
				public void mouseDragged(MouseEvent e) {
					currentMousePoint = e.getPoint();
				}
			});
			setEnabled(true);
			setFocusable(true);
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			refreshDimension();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		for (MouseManager item : mmList) {
			item.localeChanged(oldLocale, newLocale);
		}
		fillLocalizedStrings();
	}

	@Override
	public Localizer getLocalizer() {
		return localizer;
	}

	@Override
	public LoggerFacade getLogger() {
		return SwingUtils.getNearestLogger(this);
	}
	
	public Undoable<CanvasSnapshot[]> getUndoable() {
		return cHistory;
	}

	public void pushMouseManager(final MouseManager mm) {
		if (mm == null) {
			throw new NullPointerException("Mouse manager to push can't be null");
		}
		else {
			if (!mmList.isEmpty()) {
				mmList.get(0).removeListeners(this);
			}
			mmList.add(0, mm);
			mm.addListeners(this);
		}
	}
	
	public void popMouseManager() {
		if (mmList.isEmpty()) {
			throw new IllegalStateException("Mouse manager stack exhausted");
		}
		else {
			final MouseManager	mm = mmList.remove(0);
			
			mm.removeListeners(this);
			mm.close();
			if (!mmList.isEmpty()) {
				mmList.get(0).addListeners(this);
			}
		}
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
			beginTransaction("Add ["+wrapper.getClass().getSimpleName()+"]");
			content.add(new ItemDescriptor(wrapper));
			commit();
			fireEvent((l)->l.contentChanged());
		}
	}

	public void delete(final PrimitiveWrapper wrapper) {
		if (wrapper == null) {
			throw new NullPointerException("Wrapper to delete can't be null");
		}
		else {
			beginTransaction("Delete ["+wrapper.getClass().getSimpleName()+"]");
			for(int index = content.size()-1; index <= 0; index--) {
				if (content.get(index).wrapper == wrapper) {
					content.remove(index);
					commit();
					fireEvent((l)->l.contentChanged());
					return;
				}
			}
			rollback();
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

	public Point2D currentMousePoint() {
		return currentMousePoint;
	}

	public double currentScale() {
		return currentScale;
	}
	
	public void beginTransaction(final String comment) {
		if (Utils.checkEmptyOrNullString(comment)) {
			throw new IllegalArgumentException("Comment can be neither null nor empty"); 
		}
		else {
			cHistory.appendUndo(currentCanvasSnapshot = new CanvasSnapshot[] {getSnapshot(comment), null});
		}
	}

	public void commit() {
		forEach((item)->item.commitChanges());
		refreshDimension();
		currentCanvasSnapshot[1] = getSnapshot("Commit");
		repaint();
	}
	
	public void rollback() {
		forEach((item)->item.clearTransform());
		cHistory.removeLastSnapshot();
		refreshDimension();
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

	Point2D toScaledPoint(final Point2D point) {
		return new Point2D.Double(point.getX() * currentScale, point.getY() * currentScale);
	}

	CanvasSnapshot getSnapshot(final String comment) {
		return new CanvasSnapshot(this, comment);
	}

	void restoreSnapshot(final CanvasSnapshot snapshot) {
		final double	oldScale = this.currentScale;
		
		this.currentScale = snapshot.currentScale;
		content.clear();
		content.addAll(Arrays.asList(snapshot.items));
		refreshDimension();
		if (oldScale != currentScale) {
			fireEvent((l)->l.scaleChanged(oldScale, currentScale));
		}
		fireEvent((l)->l.contentChanged());
	}
	
	void scaleCanvas(final double value) {
		final double	oldScale = currentScale;
		
		currentScale = calculateScale(currentScale, value);
		refreshDimension();
		if (oldScale != currentScale) {
			fireEvent((l)->l.scaleChanged(oldScale, currentScale));
		}
	}

	Iterable<ItemDescriptor> getDescriptors() {
		return content;
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

	void fireEvent(final LightWeightListenerCallback<StateChangedListener> callback) {
		listeners.fireEvent(callback);
	}

	double getDelta() {
		return delta;
	}
	
	double getEffectiveX(final double x) {
		return x;
	}

	double getEffectiveY(final double y) {
		return y;
	}

	double getEffectiveScale(final double scale) {
		return scale;
	}

	double getEffectiveAngle(final double angle) {
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

	private void refreshDimension() {
		setSize(getEffectiveSize());
		setPreferredSize(getEffectiveSize());
		repaint();
	}
	
	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
		
	}

	static class ItemDescriptor {
		final PrimitiveWrapper	wrapper;
		boolean					selected;
		
		public ItemDescriptor(final PrimitiveWrapper wrapper) {
			this(wrapper, false);
		}
		
		public ItemDescriptor(final PrimitiveWrapper wrapper, final boolean selected) {
			this.wrapper = wrapper;
			this.selected = selected;
		}
	}
	
	static class CanvasSnapshot {
		private final String			comment; 
		private final double			currentScale;
		private final ItemDescriptor[]	items;
		
		private CanvasSnapshot(final SVGCanvas canvas, final String comment) {
			this.comment = comment;
			this.currentScale = canvas.currentScale;
			this.items = new ItemDescriptor[canvas.content.size()];
			int	index = 0;
			
			for (ItemDescriptor item : canvas.content) {
				try {
					items[index++] = new ItemDescriptor((PrimitiveWrapper) item.wrapper.clone(), item.selected);
				} catch (CloneNotSupportedException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}

		@Override
		public String toString() {
			return "CanvasSnapshot [comment=" + comment + ", currentScale=" + currentScale + ", items="
					+ Arrays.toString(items) + "]";
		}
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		for(MouseManager item : mmList) {
			item.propertyChange(event);
		}
	}

	@Override
	public void propertiesChange(final PropertyGroupChangeEvent event) {
		for(MouseManager item : mmList) {
			item.propertiesChange(event);
		}
	}
}
