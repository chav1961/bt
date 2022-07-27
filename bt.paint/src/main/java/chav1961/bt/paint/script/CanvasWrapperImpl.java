package chav1961.bt.paint.script;

import chav1961.bt.paint.script.interfaces.CanvasWrapper;
import chav1961.bt.paint.script.interfaces.ColorWrapper;
import chav1961.bt.paint.script.interfaces.FontWrapper;
import chav1961.bt.paint.script.interfaces.FontWrapper.FontStyle;
import chav1961.bt.paint.script.interfaces.StrokeWrapper;

public class CanvasWrapperImpl extends BufferWrapperImpl implements CanvasWrapper {
	private final FontWrapper	fw = new FontWrapperImpl();
	private final ColorWrapper	fore = new ColorWrapperImpl();
	private final ColorWrapper	back = new ColorWrapperImpl();
	private final StrokeWrapper	stroke = new StrokeWrapperImpl();
	
	public CanvasWrapperImpl() {
	}

	@Override
	public FontWrapper getCanvasFont() {
		return fw;
	}

	@Override
	public void setCanvasFont(final FontWrapper font) {
		if (font == null) {
			throw new NullPointerException("Font to set can't be null");
		}
		else {
			fw.setFamily(font.getFont().getFamily());
			fw.setStyle(FontStyle.of(font.getFont().getStyle()));
			fw.setSize(font.getFont().getSize());
		}
	}

	@Override
	public ColorWrapper getCanvasForeground() {
		return fore;
	}

	@Override
	public void setCanvasForeground(final ColorWrapper color) {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			fore.setColor(color);
		}
	}

	@Override
	public ColorWrapper getCanvasBackground() {
		return back;
	}

	@Override
	public void setCanvasBackground(final ColorWrapper color) {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			back.setColor(color);
		}
	}

	@Override
	public StrokeWrapper getCanvasStroke() {
		return stroke;
	}

	@Override
	public void setCanvasStroke(final StrokeWrapper strokeWrapper) {
		if (strokeWrapper == null) {
			throw new NullPointerException("Stroke to set can't be null");
		}
		else {
			stroke.setStroke(strokeWrapper.getStroke());
		}
	}
}
