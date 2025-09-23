package chav1961.bt.svgeditor.primitives;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import chav1961.bt.svgeditor.screen.SVGCanvas;

public abstract class PrimitiveWrapper {
	private Color		foreColor = Color.WHITE;
	private Color		backColor = Color.BLACK;
	private boolean		useBackground = true;
	private Rectangle2D	areaOccupied = new Rectangle2D.Double(0,0,1,1);
	
	public PrimitiveWrapper() {
		
	}

	public abstract void draw(final Graphics2D g, final SVGCanvas canvas);

	
	public Color getForeColor() {
		return foreColor;
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

	public Rectangle2D getAreaOccupied() {
		return areaOccupied;
	}

	public void setAreaOccupied(final Rectangle2D areaOccupied) {
		this.areaOccupied = areaOccupied;
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
