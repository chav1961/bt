package chav1961.bt.mnemort.canvas.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import chav1961.bt.mnemort.interfaces.CanvasWrapper;
import chav1961.bt.mnemort.interfaces.DrawingCanvas;
import chav1961.purelib.basic.Utils;

public class SwingCanvas implements DrawingCanvas {
	private final Graphics2D 		g2d;
	private final DrawingMode		mode;
	private final SwingCanvas		parent;
	private final CanvasWrapper[]	oldWrappers = new CanvasWrapper[CanvasWrapper.WrapperType.values().length]; 
	private final CanvasWrapper[]	newWrappers = new CanvasWrapper[CanvasWrapper.WrapperType.values().length];
	private final AffineTransform	oldTransform, newTransform;
	
	public SwingCanvas(final Graphics2D g2d, final DrawingMode mode) {
		if (g2d == null) {
			throw new NullPointerException("Graphics can't be null");
		}
		else {
			this.g2d = g2d;
			this.mode = mode;
			this.parent = null;
			this.oldTransform = g2d.getTransform();
			this.newTransform = new AffineTransform(this.oldTransform); 
		}
	}

	private SwingCanvas(final SwingCanvas parent, final AffineTransform trans) {
		this.g2d = null;
		this.mode = null;
		this.parent = parent;
		this.oldTransform = getNativeGraphics().getTransform();
		this.newTransform = new AffineTransform(this.oldTransform);
		this.newTransform.concatenate(trans);
		getNativeGraphics().setTransform(this.newTransform);
	}

	@Override
	public DrawingMode getDrawingMode() {
		if (parent != null) {
			return parent.getDrawingMode();
		}
		else {
			return mode;
		}
	}
	
	public Graphics2D getNativeGraphics() {
		if (parent != null) {
			return parent.getNativeGraphics();
		}
		else {
			return g2d;
		}
	}
	
	@Override
	public DrawingCanvas transform(final AffineTransform... transforms) {
		if (transforms == null || Utils.checkArrayContent4Nulls(transforms) >= 0) {
			throw new IllegalArgumentException("Transforms list is null or contains nulls inside");
		}
		else {
			if (transforms.length > 0) {
				final AffineTransform	temp = new AffineTransform(newTransform);
				
				for (AffineTransform item : transforms) {
					temp.concatenate(item);
				}
				getNativeGraphics().setTransform(temp);
			}
			return this;
		}
	}

	@Override
	public DrawingCanvas with(final CanvasWrapper... wrappers) {
		if (wrappers == null || Utils.checkArrayContent4Nulls(wrappers) >= 0) {
			throw new IllegalArgumentException("Wrappers list is null or contains nulls inside");
		}
		else {
			for (CanvasWrapper item : wrappers) {
				if (item.getType().isEntity()) {
					throw new IllegalArgumentException("Item ["+item+"] is entity and can't be used in this method"); 
				}
				else {
					if (oldWrappers[item.getType().ordinal()] == null) {
						oldWrappers[item.getType().ordinal()] = getParameter(item.getType());
					}
					newWrappers[item.getType().ordinal()] = item;
					if (item.getType().isAttribute()) {
						setParameter(item);
					}
				}
			}
		}
		
		return this;
	}

	@Override
	public void draw(final boolean draw, final boolean fill, final CanvasWrapper... wrappers) {
		if (wrappers == null || Utils.checkArrayContent4Nulls(wrappers) >= 0) {
			throw new IllegalArgumentException("Wrappers list is null or contains nulls inside");
		}
		else if (!draw && !fill) {
			throw new IllegalArgumentException("Neither 'draw' nor 'fill' was typed");
		}
		else {
			if (fill) {
				for (CanvasWrapper item : wrappers) {
					if(!item.getType().isEntity()) {
						throw new IllegalArgumentException("Item ["+item+"] is not entity"); 
					}
					else {
						getGraphics().fill((Shape)item.getValue());
					}
				}
			}
			if (draw) {
				for (CanvasWrapper item : wrappers) {
					if(!item.getType().isEntity()) {
						throw new IllegalArgumentException("Item ["+item+"] is not entity"); 
					}
					else {
						getGraphics().draw((Shape)item.getValue());
					}
				}
			}
		}
	}

	@Override
	public DrawingCanvas push(final AffineTransform transform) {
		return new SwingCanvas(this, transform);
	}
	
	@Override
	public void close() throws RuntimeException {
		for (CanvasWrapper item : oldWrappers) {
			if (item != null) {
				setParameter(item);
			}
		}
		getNativeGraphics().setTransform(oldTransform);
	}

	private CanvasWrapper getParameter(final CanvasWrapper.WrapperType type) {
		switch (type) {
			case COLOR		:
				return CanvasWrapper.of(getNativeGraphics().getColor());
			case PAINT		:
				return CanvasWrapper.of(getNativeGraphics().getPaint());
			case STROKE		:
				return CanvasWrapper.of(getNativeGraphics().getStroke());
			default			:
				throw new UnsupportedOperationException("Item type ["+type+"] is not supported");
		}
	}
	
	private void setParameter(final CanvasWrapper item) {
		switch (item.getType()) {
			case COLOR		:
				getNativeGraphics().setColor((Color)item.getValue());
				break;
			case PAINT		:
				getNativeGraphics().setPaint((Paint)item.getValue());
				break;
			case STROKE		:
				getNativeGraphics().setStroke((Stroke)item.getValue());
				break;
			default			:
				throw new UnsupportedOperationException("Item type ["+item.getType()+"] is not supported");
		}
	}
	
	private Graphics2D getGraphics() {
		if (parent != null) {
			return parent.getGraphics();
		}
		else {
			return g2d;
		}
	}

}
