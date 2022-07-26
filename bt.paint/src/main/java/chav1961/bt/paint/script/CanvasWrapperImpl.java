package chav1961.bt.paint.script;

import java.awt.BasicStroke;

import chav1961.bt.paint.control.ImageEditPanel;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.CanvasWrapper;
import chav1961.bt.paint.script.interfaces.ColorWrapper;
import chav1961.bt.paint.script.interfaces.FontWrapper;
import chav1961.bt.paint.script.interfaces.FontWrapper.FontStyle;
import chav1961.bt.paint.script.interfaces.StrokeWrapper;

public class CanvasWrapperImpl extends BufferWrapperImpl implements CanvasWrapper {
	private final ImageEditPanel delegate;
	private final FontWrapper	fw = new FontWrapperImpl();
	private final ColorWrapper	fore = new ColorWrapperImpl();
	private final ColorWrapper	back = new ColorWrapperImpl();
	private final StrokeWrapper	stroke = new StrokeWrapperImpl();
	
	public CanvasWrapperImpl() {
		this.delegate = null;
	}

	public CanvasWrapperImpl(final ImageEditPanel delegate) {
		if (delegate == null) {
			throw new NullPointerException("Delegate can't be null"); 
		}
		else {
			this.delegate = delegate;
		}
	}
	
	@Override
	public FontWrapper getFont() {
		return fw;
	}

	@Override
	public void setFont(final FontWrapper font) {
		if (font == null) {
			throw new NullPointerException("Font to set can't be null");
		}
		else {
			fw.setFamily(font.getFont().getFamily());
			fw.setStyle(FontStyle.of(font.getFont().getStyle()));
			fw.setSize(font.getFont().getSize());
			if (delegate != null) {
				delegate.setFont(fw.getFont());
			}
		}
	}

	@Override
	public ColorWrapper getForeground() {
		return fore;
	}

	@Override
	public void setForeground(final ColorWrapper color) {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			fore.setColor(color);
			if (delegate != null) {
				delegate.setForeground(color.getColor());
			}
		}
	}

	@Override
	public ColorWrapper getBackground() {
		return back;
	}

	@Override
	public void setBackground(final ColorWrapper color) {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			back.setColor(color);
			if (delegate != null) {
				delegate.setBackground(color.getColor());
			}
		}
	}

	@Override
	public StrokeWrapper getStroke() {
		return stroke;
	}

	@Override
	public void setStroke(final StrokeWrapper strokeWrapper) {
		if (strokeWrapper == null) {
			throw new NullPointerException("Stroke to set can't be null");
		}
		else {
			stroke.setStroke(strokeWrapper.getStroke());
		}
//		if (delegate != null) {
//			delegate.setLStroke(strokeWrapper.getStroke());
//		}
	}
}
