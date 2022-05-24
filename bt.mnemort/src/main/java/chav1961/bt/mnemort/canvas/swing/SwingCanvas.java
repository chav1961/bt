package chav1961.bt.mnemort.canvas.swing;

import java.awt.Graphics2D;
import java.awt.Shape;
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
	
	public SwingCanvas(final Graphics2D g2d, final DrawingMode mode) {
		if (g2d == null) {
			throw new NullPointerException("Graphics can't be null");
		}
		else {
			this.g2d = g2d;
			this.mode = mode;
			this.parent = null;
		}
	}

	private SwingCanvas(final SwingCanvas parent) {
		this.g2d = null;
		this.mode = null;
		this.parent = parent;
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
	
	@Override
	public DrawingCanvas transform(final AffineTransform... transforms) {
		// TODO Auto-generated method stub
		return null;
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
	public DrawingCanvas push() {
		return new SwingCanvas(this);
	}
	
	@Override
	public void close() throws RuntimeException {
		// TODO Auto-generated method stub
		for (CanvasWrapper item : oldWrappers) {
			if (item != null) {
				setParameter(item);
			}
		}
	}

	private CanvasWrapper getParameter(final CanvasWrapper.WrapperType type) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void setParameter(final CanvasWrapper item) {
		// TODO Auto-generated method stub
		
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
