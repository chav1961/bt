package chav1961.bt.clipper.inner;

import chav1961.bt.clipper.inner.interfaces.ClipperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class MutableClipperValue extends AbstractClipperValue {
	private static final long serialVersionUID = -1479050654751220327L;

	private Object	value = null;

	public MutableClipperValue(final ClipperType type) {
		this(type, null);
	}
	
	public MutableClipperValue(final ClipperType type, final Object value) {
		super(type);
		this.value = value;
	}

	@Override
	public <T> T get(final Class<T> awaited) throws SyntaxException {
		return (T)value;
	}

	@Override
	public <T> ClipperValue set(final T value) throws SyntaxException {
		this.value = value;
		return this;
	}

	@Override
	public <T> ClipperValue set(final ClipperValue value) throws SyntaxException {
		return set(value.get(getType().getNativeClass()));
	}
}
