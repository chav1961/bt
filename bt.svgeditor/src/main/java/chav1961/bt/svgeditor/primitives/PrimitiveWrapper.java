package chav1961.bt.svgeditor.primitives;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import chav1961.bt.svgeditor.screen.SVGCanvas;

public abstract class PrimitiveWrapper implements Cloneable {
	private Color			foreColor = Color.WHITE;
	private Color			backColor = Color.BLACK;
	private boolean			useBackground = true;
	private boolean			dragMode = false;
	private boolean			highlight = false;
	private Point2D			startDragPoint = null;
	private Rectangle2D		areaOccupied = new Rectangle2D.Double(0,0,1,1);
	private AffineTransform	at = new AffineTransform();
	
	public PrimitiveWrapper() {
		
	}

	public abstract void draw(final Graphics2D g, final SVGCanvas canvas, final boolean selected);
	public abstract void commitChanges();
	
	public abstract boolean isIntersects(final Rectangle2D rect);
	public abstract boolean isAbout(final Point2D point, final float delta);
	public abstract Point2D getNearest(final Point2D point, final double dist, final boolean anchorsOnly);

	@Override
	public Object clone() throws CloneNotSupportedException {
		final PrimitiveWrapper	clone = (PrimitiveWrapper) super.clone();
		
		clone.areaOccupied = (Rectangle2D) clone.areaOccupied.clone();
		clone.at = (AffineTransform) clone.at.clone();
		if (clone.startDragPoint != null) {
			clone.startDragPoint = (Point2D) clone.startDragPoint.clone();
		}
		return clone;
	}
	
	public Color getForeColor() {
		return foreColor;
	}

	public Color getEffectiveForeColor() {
		return isHighlight() ? foreColor.darker() : foreColor;
	}
	
	public void setForeColor(final Color foreColor) {
		if (foreColor == null) {
			throw new NullPointerException("Foreground color can't be null");
		}
		else {
			this.foreColor = foreColor;
		}
	}

	public Color getBackColor() {
		return backColor;
	}

	public Color getEffectiveBackColor() {
		return isHighlight() ? backColor.darker() : backColor;
	}
	
	public void setBackColor(final Color backColor) {
		if (backColor == null) {
			throw new NullPointerException("Background color can't be null");
		}
		else {
			this.backColor = backColor;
		}
	}

	public boolean isUseBackground() {
		return useBackground;
	}

	public void setUseBackground(final boolean useBackground) {
		this.useBackground = useBackground;
	}

	public boolean isDragMode() {
		return dragMode;
	}

	public void setDragMode(boolean dragMode) {
		this.dragMode = dragMode;
	}

	public Rectangle2D getAreaOccupied() {
		return areaOccupied;
	}

	public void setAreaOccupied(final Rectangle2D areaOccupied) {
		this.areaOccupied = areaOccupied;
	}

	public boolean isHighlight() {
		return highlight;
	}

	public void setHighlight(final boolean highlight) {
		this.highlight = highlight;
	}

	public void startDrag(final Point2D from) {
		if (from == null) {
			throw new NullPointerException("From point can't be null"); 
		}
		else if (isDragMode()) {
			throw new IllegalStateException("This primitive is already in drag mode");
		}
		else {
			setDragMode(true);
			startDragPoint = from;
		}
	}
	
	public void endDrag() {
		if (!isDragMode()) {
			throw new IllegalStateException("This primitive is not in drag mode");
		}
		else {
			setDragMode(false);
		}
	}
	
	public AffineTransform getTransform() {
		return at;
	}

	public void setTransform(final AffineTransform at) {
		if (at == null) {
			throw new NullPointerException("Transform to set can't be null");
		}
		else if (!this.at.equals(at)) {
			this.at = at;
		}
	}

	public void clearTransform() {
		this.at = new AffineTransform();
	}

	public boolean isInside(final Rectangle2D rect) {
		if (rect == null) {
			throw new NullPointerException("Rectangle to test can't be null"); 
		}
		else {
			return rect.contains(getAreaOccupied());
		}
	}
	
	protected Point2D getStartDragPoint() {
		return startDragPoint;
	}
	
	protected static Rectangle2D calcAreaOccupied(final Point2D... points) {
		double xMin = points[0].getX(), xMax = xMin;
		double yMin = points[0].getY(), yMax = yMin;
		
		for(Point2D item : points) {
			if (xMin > item.getX()) {
				xMin = item.getX();
			}
			if (xMax < item.getX()) {
				xMax = item.getX();
			}
			if (yMin > item.getY()) {
				yMin = item.getY();
			}
			if (yMax < item.getY()) {
				yMax = item.getY();
			}
		}
		return new Rectangle2D.Double(xMin, yMin, xMax-xMin, yMax-yMin);
	}
}
