package chav1961.bt.paint.script.interfaces;

public interface CanvasWrapper extends BufferWrapper {
	FontWrapper getCanvasFont();
	void setCanvasFont(FontWrapper font);
	ColorWrapper getCanvasForeground();
	void setCanvasForeground(ColorWrapper color);
	ColorWrapper getCanvasBackground();
	void setCanvasBackground(ColorWrapper color);
	StrokeWrapper getCanvasStroke();
	void setCanvasStroke(StrokeWrapper stroke);
}