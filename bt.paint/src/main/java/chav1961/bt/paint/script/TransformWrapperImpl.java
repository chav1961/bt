package chav1961.bt.paint.script;

import java.awt.geom.AffineTransform;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.TransformWrapper;
import chav1961.purelib.basic.CSSUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class TransformWrapperImpl implements TransformWrapper {
	private final AffineTransform	transform = new AffineTransform();

	public TransformWrapperImpl(final AffineTransform transform) {
		if (transform == null) {
			throw new NullPointerException("Transform to set can't be null");
		}
		else {
			this.transform.setTransform(transform);
		}
	}

	public TransformWrapperImpl(final String transform) throws PaintScriptException {
		if (transform == null || transform.isEmpty()) {
			throw new IllegalArgumentException("Transform to set can't be null or empty");
		}
		else {
			try{
				this.transform.setTransform(CSSUtils.asTransform(transform));
			} catch (SyntaxException exc) {
				throw new PaintScriptException(exc.getLocalizedMessage(), exc); 
			}
		}
	}
	
	@Override
	public Class<AffineTransform> getContentType() {
		return AffineTransform.class;
	}

	@Override
	public AffineTransform getContent() throws PaintScriptException {
		return getTransform();
	}

	@Override
	public void setContent(AffineTransform content) throws PaintScriptException {
		setTransform(content);
	}

	@Override
	public TransformWrapper clear() {
		transform.setTransform(new AffineTransform());
		return this;
	}

	@Override
	public TransformWrapper move(final int x, final int y) {
		transform.translate(x, y);
		return this;
	}

	@Override
	public TransformWrapper scale(int sx, int sy) {
		transform.scale(sx, sy);
		return this;
	}

	@Override
	public TransformWrapper rotate(final int angle) {
		transform.rotate(angle);
		return this;
	}

	@Override
	public TransformWrapper setTransform(final AffineTransform transform) throws PaintScriptException {
		if (transform == null) {
			throw new NullPointerException("Transform to set can't be null");
		}
		else {
			this.transform.setTransform(transform);
			return this;
		}
	}
	
	@Override
	public TransformWrapper setTransform(String transform) throws PaintScriptException {
		if (transform == null || transform.isEmpty()) {
			throw new IllegalArgumentException("Transform to set can't be null or empty");
		}
		else {
			try{
				this.transform.setTransform(CSSUtils.asTransform(transform));
				return this;
			} catch (SyntaxException exc) {
				throw new PaintScriptException(exc.getLocalizedMessage(), exc); 
			}
		}
	}

	@Override
	public AffineTransform getTransform() {
		return transform;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new TransformWrapperImpl(transform);
	}
}
