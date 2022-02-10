package chav1961.bt.clipper.interfaces;

import java.io.Serializable;

import chav1961.purelib.basic.exceptions.SyntaxException;

public interface ClipperValue extends Serializable, Cloneable {
	ClipperType getType();
	<T> T get() throws SyntaxException;
	<T> ClipperValue set(T value) throws SyntaxException;
	<T> ClipperValue set(ClipperValue value) throws SyntaxException;
	ClipperValue clone() throws CloneNotSupportedException;
}
