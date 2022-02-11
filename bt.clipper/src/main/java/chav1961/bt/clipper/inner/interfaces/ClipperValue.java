package chav1961.bt.clipper.inner.interfaces;

import java.io.Serializable;

import chav1961.purelib.basic.exceptions.SyntaxException;

public interface ClipperValue extends Serializable, Cloneable {
	ClipperType getType();
	<T> T get(Class<T> awaited) throws SyntaxException;
	<T> ClipperValue set(T value) throws SyntaxException;
	<T> ClipperValue set(ClipperValue value) throws SyntaxException;
	ClipperValue clone() throws CloneNotSupportedException;
}
