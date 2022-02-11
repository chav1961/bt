package chav1961.bt.clipper.inner.interfaces;

import chav1961.purelib.basic.exceptions.SyntaxException;

public interface ClipperBuiltinFunction extends ClipperFunction {
	@Override
	default <T> T get(final Class<T> awaited) throws SyntaxException {
		throw new IllegalStateException("Calling this method is not applicable with the class");
	}

	@Override
	default <T> ClipperValue set(T value) throws SyntaxException {
		throw new IllegalStateException("Calling this method is not applicable with the class");
	}

	@Override
	default <T> ClipperValue set(final ClipperValue value) throws SyntaxException {
		throw new IllegalStateException("Calling this method is not applicable with the class");
	}
}
