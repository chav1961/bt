package chav1961.bt.paint.script;

import java.awt.Color;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.ColorWrapper;
import chav1961.purelib.basic.PureLibSettings;

public class ColorWrapperImpl implements ColorWrapper {
	private Color	color;

	public ColorWrapperImpl() {
		this(Color.BLACK);
	}

	public ColorWrapperImpl(final Color color) {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			this.color = color;
		}
	}
	
	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public ColorWrapper setColor(final ColorWrapper colorWrapper) {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			color = new Color(colorWrapper.getColor().getRed(), colorWrapper.getColor().getGreen(), colorWrapper.getColor().getBlue(), colorWrapper.getColor().getAlpha());
			return this;
		}
	}
	
	@Override
	public ColorWrapper setColor(final String colorStr) throws PaintScriptException {
		if (colorStr == null || colorStr.isEmpty()) {
			throw new IllegalArgumentException("Color string can't be null or empty");
		}
		else {
			color = PureLibSettings.colorByName(colorStr, Color.BLACK);
			return this;
		}
	}

	@Override
	public String toString() {
		return "ColorWrapperImpl [color=" + color + "]";
	}
}
