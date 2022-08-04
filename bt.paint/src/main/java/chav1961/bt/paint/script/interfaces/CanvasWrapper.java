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
	
	void startImageAction(String beforeActionType, String afterActionType);
	void endImageAction(String beforeActionType, String afterActionType);
	void startPropertyAction(String beforeActionType, String afterActionType);
	void endPropertyAction(String beforeActionType, String afterActionType);
}