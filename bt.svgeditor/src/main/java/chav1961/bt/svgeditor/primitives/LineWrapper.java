package chav1961.bt.svgeditor.primitives;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import chav1961.bt.svgeditor.screen.SVGCanvas;

public class LineWrapper extends PrimitiveWrapper {
	private Point2D	from;
	private Point2D	to;
	
	public LineWrapper(int x1, int y1, int x2, int y2) {
		this.from = new Point2D.Double(x1,y1);
		this.to = new Point2D.Double(x2,y2);
		setAreaOccupied(calcAreaOccupied(from, to));
	}

	public Point2D getFrom() {
		return from;
	}

	public void setFrom(final Point2D from) {
		if (from == null) {
			throw new NullPointerException("From point can't be null");
		}
		else {
			this.from = from;
			setAreaOccupied(calcAreaOccupied(from, to));
		}
	}

	public Point2D getTo() {
		return to;
	}

	public void setTo(final Point2D to) {
		if (to == null) {
			throw new NullPointerException("To point can't be null");
		}
		else {
			this.to = to;
			setAreaOccupied(calcAreaOccupied(from, to));
		}
	}

	@Override
	public void draw(final Graphics2D g2d, final SVGCanvas canvas) {
		final Color	oldColor = g2d.getColor();
		
		g2d.setColor(getForeColor());
		g2d.draw(new Line2D.Double(getFrom().getX(), getFrom().getY(), getTo().getX(), getTo().getY()));
		g2d.setColor(oldColor);
	}
}
