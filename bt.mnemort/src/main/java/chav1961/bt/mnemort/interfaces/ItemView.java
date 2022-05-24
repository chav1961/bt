package chav1961.bt.mnemort.interfaces;

public interface ItemView <Canvas extends DrawingCanvas> {
	void draw(Canvas canvas);
	
	default void draw(Canvas canvas, float width, float height) {
		draw(canvas);
	}

	default void draw(Canvas canvas, float x, float y, float width, float height) {
		draw(canvas);
	}
}
