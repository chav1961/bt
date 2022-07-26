package chav1961.bt.paint.script;

import java.awt.BasicStroke;
import java.awt.Stroke;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.StrokeWrapper;

public class StrokeWrapperImpl implements StrokeWrapper {
	private Stroke		stroke;
	private int			width = 1;
	private LineStroke	style = LineStroke.SOLID;
	private LineCaps	caps = LineCaps.BUTT;
	private LineJoin 	join = LineJoin.MITER;
	
	public StrokeWrapperImpl() {
		this(new BasicStroke());
	}
	
	public StrokeWrapperImpl(final Stroke stroke) {
		if (stroke == null) {
			throw new NullPointerException("Stroke to set can't be null");
		}
		else {
			this.stroke = stroke;
		}
	}
	
	@Override
	public Stroke getStroke() {
		return stroke;
	}

	@Override
	public StrokeWrapper setWidth(final int width) throws PaintScriptException {
		if (width <= 0) {
			throw new IllegalArgumentException("Line width ["+width+"] must be positive"); 
		}
		else {
			this.width = width;
			return this;
		}
	}

	@Override
	public StrokeWrapper setStyle(final LineStroke style) throws PaintScriptException {
		if (style == null) {
			throw new NullPointerException("Line style can't be null"); 
		}
		else {
			this.style = style;
			return this;
		}
	}

	@Override
	public StrokeWrapper setCaps(final LineCaps caps) throws PaintScriptException {
		if (caps == null) {
			throw new NullPointerException("Line caps can't be null"); 
		}
		else {
			this.caps = caps;
			return this;
		}
	}

	@Override
	public StrokeWrapper setJoin(final LineJoin join) throws PaintScriptException {
		if (join == null) {
			throw new NullPointerException("Line join can't be null"); 
		}
		else {
			this.join = join;
			return this;
		}
	}
	
	@Override
	public StrokeWrapper setStroke(final Stroke stroke) {
		if (stroke == null) {
			throw new NullPointerException("Stroke to set can't be null");
		}
		else {
			this.stroke = stroke;
			return this;
		}
	}

	@Override
	public StrokeWrapper setStroke(final String stroke) throws PaintScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return "StrokeWrapperImpl [stroke=" + stroke + "]";
	}
}
