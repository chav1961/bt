package chav1961.bt.paint.script;

import java.awt.Point;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.PointWrapper;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class PointWrapperImpl implements PointWrapper {
	private static final Object[]	LEXEMAS = {CharUtils.ArgumentType.signedInt, ',', CharUtils.ArgumentType.signedInt}; 

	private final Point				point = new Point();

	public PointWrapperImpl(final Point point) {
		if (point == null) {
			throw new NullPointerException("Point to wrap can't be null"); 
		}
		else {
			this.point.setLocation(point);
		}
	}

	public PointWrapperImpl(final String point) throws PaintScriptException {
		if (point == null || point.isEmpty()) {
			throw new IllegalArgumentException("Point string can't be null or empty");
		}
		else {
			try{this.point.setLocation(parsePoint(point));
			} catch (SyntaxException e) {
				throw new PaintScriptException(e);
			}
		}
	}

	@Override
	public Class<Point> getContentType() {
		return Point.class;
	}

	@Override
	public Point getContent() throws PaintScriptException {
		return getPoint();
	}

	@Override
	public void setContent(Point content) throws PaintScriptException {
		setPoint(content);
	}

	@Override
	public Point getPoint() {
		return point;
	}

	@Override
	public PointWrapper setPoint(final String point) throws PaintScriptException {
		if (point == null || point.isEmpty()) {
			throw new NullPointerException("Point to set can't be null or empty"); 
		}
		else {
			try{this.point.setLocation(parsePoint(point));
				return this;
			} catch (SyntaxException e) {
				throw new PaintScriptException(e);
			}
		}
	}

	@Override
	public PointWrapper setPoint(Point point) throws PaintScriptException {
		if (point == null) {
			throw new NullPointerException("Point to set can't be null"); 
		}
		else {
			this.point.setLocation(point);
			return this;
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new PointWrapperImpl(point);
	}
	
	private static Point parsePoint(final String point) throws SyntaxException {
		final Object[]	p = new Object[4];
		final char[]	content = point.toCharArray();
		final int		pos = CharUtils.tryExtract(content, 0, LEXEMAS);
		
		if (pos < 0) {
			throw new SyntaxException(0,-pos,"POint format error ["+point+"]"); 
		}
		else {
			CharUtils.extract(content, 0, p, LEXEMAS);
			
			return new Point((Integer)p[0], (Integer)p[1]);
		}
	}
}
