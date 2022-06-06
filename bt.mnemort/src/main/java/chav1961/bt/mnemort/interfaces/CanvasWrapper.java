package chav1961.bt.mnemort.interfaces;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import chav1961.purelib.ui.ColorPair;

public interface CanvasWrapper {
	public static enum DrawingType {
		UNKNOWN,
		SHAPE,
		TEXT,
		IMAGE
	}
	
	public static enum WrapperType {
		COLOR_PAIR(false, true, false, DrawingType.UNKNOWN),
		COLOR(true, false, false, DrawingType.UNKNOWN),
		STROKE(true, false, false, DrawingType.UNKNOWN),
		FONT(true, false, false, DrawingType.UNKNOWN),
		PAINT(true, false, false, DrawingType.UNKNOWN),
		SHAPE(false, false, true, DrawingType.SHAPE),
		STRING(false, false, true, DrawingType.TEXT);
		
		private final boolean		isAttribute;
		private final boolean		isAttributeContainer;
		private final boolean		isEntity;
		private final DrawingType	drawingType;
		
		private WrapperType(final boolean attribute, final boolean attributeContainer, final boolean entity, final DrawingType drawingType) {
			this.isAttribute = attribute;
			this.isAttributeContainer = attributeContainer;
			this.isEntity = entity;
			this.drawingType = drawingType;
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
		
		public DrawingType getDrawingType() {
			return drawingType;
		}
	}
	
	WrapperType getType();
	<T> T getValue();
	
	default <T> T getParameter() {
		return null;
	}
	
	static CanvasWrapper of(final Font font) {
		if (font == null) {
			throw new NullPointerException("Font descriptor can't be null"); 
		}
		else {
			return new CanvasWrapperImpl(WrapperType.FONT, font);
		}
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

	static CanvasWrapper of(final String content, final Rectangle2D parameter) {
		if (content == null) {
			throw new NullPointerException("String content can't be null"); 
		}
		else if (parameter == null) {
			throw new NullPointerException("Rectangle parameter can't be null"); 
		}
		else {
			return new CanvasWrapperImpl(WrapperType.STRING, content, parameter);
		}
	}
	
	static class CanvasWrapperImpl implements CanvasWrapper {
		private final WrapperType	type;
		private final Object		value;
		private final Object		parameter;
		
		private CanvasWrapperImpl(final WrapperType type, final Object value) {
			this.type = type;
			this.value = value;
			this.parameter = null;
		}

		private CanvasWrapperImpl(final WrapperType type, final Object value, final Object parameter) {
			this.type = type;
			this.value = value;
			this.parameter = parameter;
		}
		
		@Override
		public WrapperType getType() {
			return type;
		}

		@Override
		public <T> T getValue() {
			return (T)value;
		}

		@Override
		public <T> T getParameter() {
			return (T)parameter;
		}
	}
}
