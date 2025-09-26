package chav1961.bt.svgeditor.primitives;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import chav1961.bt.svgeditor.screen.SVGCanvas;

public class LineWrapper extends PrimitiveWrapper {
	private Point2D	from;
	private Point2D	to;
	private Line2D	line;
	
	public LineWrapper(int x1, int y1, int x2, int y2) {
		this.from = new Point2D.Double(x1,y1);
		this.to = new Point2D.Double(x2,y2);
		setAreaOccupied(calcAreaOccupied(from, to));
		refreshLine();
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
			refreshLine();
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
			refreshLine();
		}
	}

	@Override
	public void draw(final Graphics2D g2d, final SVGCanvas canvas) {
		final Color				oldColor = g2d.getColor();
		final AffineTransform	oldTransform = g2d.getTransform();
		final AffineTransform	newTransform = new AffineTransform(oldTransform);
	
		newTransform.concatenate(getTransform());
		g2d.setTransform(newTransform);
		g2d.setColor(getEffectiveForeColor());
		g2d.draw(line);
		g2d.setColor(oldColor);
		g2d.setTransform(oldTransform);
	}

	@Override
	public boolean isAbout(final Point2D point, final float delta) {
		if (point == null) {
			throw new NullPointerException("Point to check can't be null");
		}
		else if (delta < 0) {
			throw new IllegalArgumentException("Delta ["+delta+"] can't be less than 0");
		}
		else {
			return line.ptSegDist(point) < delta;
		}
	}

	@Override
	public void commitChanges() {
		getTransform().transform((Point2D)from.clone(), from);
		getTransform().transform((Point2D)to.clone(), to);
		setAreaOccupied(calcAreaOccupied(from, to));
		refreshLine();
	}
	
	
	private void refreshLine() {
		line = new Line2D.Double(getFrom().getX(), getFrom().getY(), getTo().getX(), getTo().getY());
	}

}
