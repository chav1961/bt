package chav1961.bt.mnemort.interfaces;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;

import chav1961.purelib.ui.ColorPair;

public interface CanvasWrapper {
	public static enum WrapperType {
		COLOR_PAIR(false, true, false),
		COLOR(true, false, false),
		STROKE(true, false, false),
		PAINT(true, false, false),
		SHAPE(false, false, true),
		STRING(false, false, true);
		
		private final boolean	isAttribute;
		private final boolean	isAttributeContainer;
		private final boolean	isEntity;
		
		private WrapperType(final boolean attribute, final boolean attributeContainer, final boolean entity) {
			this.isAttribute = attribute;
			this.isAttributeContainer = attributeContainer;
			this.isEntity = entity;
		}
		
		public boolean isAttribute() {
			return isAttribute;
		}

		public boolean isAttributeContainer() {
			return isAttributeContainer;
		}
		
		public boolean isEntity() {
			return isEntity;
		}
	}
	
	WrapperType getType();
	<T> T getValue();
	
	static CanvasWrapper of(Font font) {
		return null;
	}

	static CanvasWrapper of(final Paint paint) {
		if (paint == null) {
			throw new NullPointerException("Paint descriptor can't be null"); 
		}
		else {
			return new CanvasWrapperImpl(WrapperType.PAINT, paint);
		}
	}

	static CanvasWrapper of(final Color color) {
		if (color == null) {
			throw new NullPointerException("Color descriptor can't be null"); 
		}
		else {
			return new CanvasWrapperImpl(WrapperType.COLOR, color);
		}
	}
	
	static CanvasWrapper of(final Color foreground, final Color background) {
		if (foreground == null) {
			throw new NullPointerException("Forground color descriptor can't be null"); 
		}
		else if (background == null) {
			throw new NullPointerException("Background color descriptor can't be null"); 
		}
		else {
			return of(new ColorPair(foreground, background));
		}
	}

	static CanvasWrapper of(final ColorPair pair) {
		if (pair == null) {
			throw new NullPointerException("Color pair descriptor can't be null"); 
		}
		else {
			return new CanvasWrapperImpl(WrapperType.COLOR_PAIR, pair);
		}
	}
	
	static CanvasWrapper of(final Stroke stroke) {
		if (stroke == null) {
			throw new NullPointerException("Stroke descriptor can't be null"); 
		}
		else {
			return new CanvasWrapperImpl(WrapperType.STROKE, stroke);
		}
	}

	static CanvasWrapper of(final Shape shape) {
		if (shape == null) {
			throw new NullPointerException("Shape descriptor can't be null"); 
		}
		else {
			return new CanvasWrapperImpl(WrapperType.SHAPE, shape);
		}
	}
	
	static class CanvasWrapperImpl implements CanvasWrapper {
		private final WrapperType	type;
		private final Object		value;
		
		private CanvasWrapperImpl(final WrapperType type, final Object value) {
			this.type = type;
			this.value = value;
		}
		
		@Override
		public WrapperType getType() {
			return type;
		}

		@Override
		public <T> T getValue() {
			return (T)value;
		}
		
	}
}
