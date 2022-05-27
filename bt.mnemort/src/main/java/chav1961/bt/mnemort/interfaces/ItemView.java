package chav1961.bt.mnemort.interfaces;

import java.net.URI;

public interface ItemView <Canvas extends DrawingCanvas> {
	public enum ItemViewType {
		BACKGROUND,
		STATIC,
		DYNAMIC
	}
	
	ItemViewType getViewType();
	URI getViewURI();
	void draw(Canvas canvas);
	void draw(Canvas canvas, float width, float height);
}
