package chav1961.bt.paint.script.interfaces;

import chav1961.bt.paint.interfaces.PaintScriptException;

public interface ContentWrapper<T> extends Cloneable {
	Class<T> getContentType();
	T getContent() throws PaintScriptException;
	void setContent(T content) throws PaintScriptException;
	Object clone() throws CloneNotSupportedException;
}
