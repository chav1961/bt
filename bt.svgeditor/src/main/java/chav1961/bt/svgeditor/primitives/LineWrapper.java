package chav1961.bt.svgeditor.primitives;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import chav1961.bt.svgeditor.screen.SVGCanvas;

public class LineWrapper extends PrimitiveWrapper {
	private static final float[]	DASHED_LINE = new float[] {3.0f, 3.0f};
	
	private Point2D	from;
	private Point2D	to;
	private Line2D	line;
	private float	lineWidth = 1.0f;
	
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
	public Object clone() throws CloneNotSupportedException {
		final LineWrapper	clone = (LineWrapper) super.clone();
		
		clone.from = (Point2D) clone.from.clone(); 
		clone.to = (Point2D) clone.to.clone(); 
		clone.line = (Line2D) clone.line.clone(); 
		return clone;
	}
	
	@Override
	public void draw(final Graphics2D g2d, final SVGCanvas canvas, final boolean selected) {
		final Color				oldColor = g2d.getColor();
		final AffineTransform	oldTransform = g2d.getTransform();
		final AffineTransform	newTransform = new AffineTransform(oldTransform);
		final Stroke			oldStroke = g2d.getStroke();
	
		newTransform.concatenate(getTransform());
		g2d.setTransform(newTransform);
		g2d.setColor(getEffectiveForeColor());
		g2d.setStroke(selected 
				? new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, DASHED_LINE, 0)
				: new BasicStroke(lineWidth));
		g2d.draw(line);
		g2d.setStroke(oldStroke);
		g2d.setColor(oldColor);
		g2d.setTransform(oldTransform);
	}

	@Override
	public boolean isAbout(final Point2D point, final double delta) {
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
		clearTransform();
		setAreaOccupied(calcAreaOccupied(from, to));
		refreshLine();
	}

	@Override
	public boolean isIntersects(final Rectangle2D rect) {
		return rect.intersectsLine(line);
	}

	@Override
	public Point2D getNearest(final Point2D point, final double dist, final boolean anchorsOnly) {
		if (anchorsOnly) {
			if (point.distance(from) <= dist) {
				return from;
			}
			else if (point.distance(to) <= dist) {
				return to;
			}
			else {
				return null;
			}
		}
		else if (line.ptSegDist(point) < dist) { // https://otvet.mail.ru/question/228851600
			if (from.getX() == to.getX()) {
				return new Point2D.Double(from.getX(), point.getY());
			}
			else if (from.getY() == to.getY()) {
				return new Point2D.Double(point.getX(), from.getY());
			}
			else {
				final double	a = (to.getY() - from.getY())/(to.getX() - from.getX());
				final double	b = from.getY() - 2* from.getX();
				final double	a2 = a * a;
				final double	a2p1 = 1/a2+1;
				final double	xP = (a*point.getY()+point.getX()-a*b)*a2p1;
				final double	yP = (a2*point.getY()+a*point.getX()+b)*a2p1;

				return new Point2D.Double(xP, yP);				
			}
		}
		else {
			return null;
		}
	}
	
	private void refreshLine() {
		line = new Line2D.Double(getFrom().getX(), getFrom().getY(), getTo().getX(), getTo().getY());
	}
}
