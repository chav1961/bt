package chav1961.bt.paint.script;

import java.awt.BasicStroke;
import java.awt.Stroke;

import chav1961.bt.paint.control.ImageUtils;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.StrokeWrapper;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class StrokeWrapperImpl implements StrokeWrapper {
	private BasicStroke	stroke;
	private int			width = 1;
	private LineStroke	style = LineStroke.SOLID;
	private LineCaps	caps = LineCaps.BUTT;
	private LineJoin 	join = LineJoin.MITER;
	
	public StrokeWrapperImpl() {
		this(new BasicStroke());
	}
	
	public StrokeWrapperImpl(final BasicStroke stroke) {
		if (stroke == null) {
			throw new NullPointerException("Stroke to set can't be null");
		}
		else {
			this.stroke = stroke;
		}
	}

	@Override
	public Class<BasicStroke> getContentType() {
		return BasicStroke.class;
	}

	@Override
	public BasicStroke getContent() throws PaintScriptException {
		return getStroke();
	}

	@Override
	public void setContent(final BasicStroke content) throws PaintScriptException {
		setStroke(content);
	}
	
	@Override
	public BasicStroke getStroke() {
		return stroke;
	}

	@Override
	public StrokeWrapper setWidth(final int width) throws PaintScriptException {
		if (width <= 0) {
			throw new IllegalArgumentException("Line width ["+width+"] must be positive"); 
		}
		else {
			this.width = width;
			stroke = ImageUtils.buildStroke(width, style, caps, join);
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
			stroke = ImageUtils.buildStroke(width, style, caps, join);
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
			stroke = ImageUtils.buildStroke(width, style, caps, join);
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
			stroke = ImageUtils.buildStroke(width, style, caps, join);
			return this;
		}
	}
	
	@Override
	public StrokeWrapper setStroke(final BasicStroke stroke) {
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
		if (stroke == null || stroke.isEmpty()) {
			throw new IllegalArgumentException("Stroke to set can't be null or empty"); 
		}
		else {
			try{
				this.stroke = ImageUtils.buildStroke(stroke);
				return this;
			} catch (SyntaxException e) {
				throw new PaintScriptException(e);
			}
		}
	}

	@Override
	public String toString() {
		return "StrokeWrapperImpl [stroke=" + stroke + "]";
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
