package chav1961.bt.paint.script.interfaces;

public interface BufferWrapper extends AutoCloseable {
	void open() throws ScriptException;
	void clear() throws ScriptException;
	boolean hasImage() throws ScriptException;
	ImageWrapper getImage() throws ScriptException;
	void setImage(ImageWrapper image) throws ScriptException;
	ImageWrapper getImage(RectWrapper rect) throws ScriptException;
	void setImage(RectWrapper rect, ImageWrapper image, ImageWrapper.SetOptions... options) throws ScriptException;
	RectWrapper getSelection() throws ScriptException;
	void setSelection(RectWrapper rect) throws ScriptException;
	void clearSelection() throws ScriptException;
	boolean hasSelection() throws ScriptException;
	void close() throws ScriptException;
}