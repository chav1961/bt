package chav1961.bt.paint.script.interfaces;

import chav1961.bt.paint.interfaces.PaintScriptException;

public interface BufferWrapper extends AutoCloseable {
	void open() throws PaintScriptException;
	void clear() throws PaintScriptException;
	boolean hasImage() throws PaintScriptException;
	ImageWrapper getImage() throws PaintScriptException;
	void setImage(ImageWrapper image) throws PaintScriptException;
	ImageWrapper getImage(RectWrapper rect) throws PaintScriptException;
	void setImage(RectWrapper rect, ImageWrapper image, ImageWrapper.SetOptions... options) throws PaintScriptException;
	RectWrapper getSelection() throws PaintScriptException;
	void setSelection(RectWrapper rect) throws PaintScriptException;
	void clearSelection() throws PaintScriptException;
	boolean hasSelection() throws PaintScriptException;
	void close() throws PaintScriptException;
}