package chav1961.bt.mnemort.interfaces;

import java.awt.geom.AffineTransform;

public interface DrawingCanvas extends AutoCloseable, Cloneable {
	public static enum DrawingMode {
		BACKGROUND,
		STATIC_PART,
		DYNAMIC_PART
	}
	
	DrawingMode getDrawingMode();
	DrawingCanvas transform(AffineTransform... transforms);
	DrawingCanvas with(CanvasWrapper... wrappers);

	void draw(boolean draw, boolean fill, CanvasWrapper... wrappers);
	
	default void draw(CanvasWrapper... wrappers) {
		draw(true, true, wrappers);
	}
	
	DrawingCanvas push(AffineTransform transform);
	
	@Override
	void close() throws RuntimeException;
}
