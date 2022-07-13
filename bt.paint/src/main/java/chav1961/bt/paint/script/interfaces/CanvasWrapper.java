package chav1961.bt.paint.script.interfaces;

public interface CanvasWrapper extends BufferWrapper {
	FontWrapper getFont();
	void setFont(FontWrapper font);
	ColorWrapper getForeground();
	void setForeground(ColorWrapper color);
	ColorWrapper getBackground();
	void setBackground(ColorWrapper color);
	StrokeWrapper getStroke();
	void setStroke(StrokeWrapper stroke);
}