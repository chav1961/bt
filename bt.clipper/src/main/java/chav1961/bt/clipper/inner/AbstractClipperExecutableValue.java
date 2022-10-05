package chav1961.bt.clipper.inner;

import chav1961.bt.clipper.inner.interfaces.ClipperExecutableValue;
import chav1961.bt.clipper.inner.interfaces.ClipperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;

public abstract class AbstractClipperExecutableValue extends AbstractClipperValue implements ClipperExecutableValue {
	private static final long serialVersionUID = -5509539388606711361L;
	
	private byte[]	content = null;
	
	protected AbstractClipperExecutableValue(final ClipperType type) {
		super(type);
	}

	@Override
	public Object get() {
		return content;
	}
	
	public <T> T get(Class<T> awaited) throws SyntaxException {
		if (awaited != byte[].class) {
			throw new SyntaxException(0, 0, "Only byte[] can be got");
		}
		else {
			return (T)content;
		}
	}
	
	public <T> ClipperValue set(final T value) throws SyntaxException {
		if (value instanceof byte[]) {
			throw new IllegalArgumentException("Value to assign must be byte[]");
		}
		else {
			content = (byte[])value;
			return this;
		}
	}
	
}
