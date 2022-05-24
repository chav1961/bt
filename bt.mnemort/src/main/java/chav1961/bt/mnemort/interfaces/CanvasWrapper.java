package chav1961.bt.mnemort.interfaces;

import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;

import chav1961.purelib.ui.ColorPair;

public interface CanvasWrapper {
	public static enum WrapperType {
		COLOR_PAIR(false, true, false),
		COLOR(true, false, false),
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

	static CanvasWrapper of(Color color) {
		return null;
	}

	static CanvasWrapper of(Color foreground, Color background) {
		return of(new ColorPair(foreground, background));
	}

	static CanvasWrapper of(ColorPair pair) {
		return null;
	}
	
	static CanvasWrapper of(Stroke stroke) {
		return null;
	}

	static CanvasWrapper of(GeneralPath path) {
		return null;
	}
}
