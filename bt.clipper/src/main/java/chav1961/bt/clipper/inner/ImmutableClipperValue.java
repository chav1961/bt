package chav1961.bt.clipper.inner;

import chav1961.bt.clipper.inner.interfaces.ClipperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class ImmutableClipperValue extends AbstractClipperValue {
	private static final long serialVersionUID = -8913957013996836790L;

	private final Object	value;
	
	public ImmutableClipperValue(final ClipperType type, final Object value) {
		super(type);
		this.value = value;
	}

	@Override
	public <T> T get(final Class<T> awaited) throws SyntaxException {
		if (getType().getNativeClass() != awaited) {
			throw new SyntaxException(0,0,"Get type ["+awaited+"] is not compatible with stored type ["+getType()+"]");
		}
		else {
			return (T)value;
		}
	}
	
	@Override
	public <T> ClipperValue set(final T value) throws SyntaxException {
		throw new IllegalStateException("Calling this method is not applicable with the class");
	}

	@Override
	public <T> ClipperValue set(final ClipperValue value) throws SyntaxException {
		throw new IllegalStateException("Calling this method is not applicable with the class");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ImmutableClipperValue other = (ImmutableClipperValue) obj;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "ImmutableClipperValue [value=" + value + ", getType()=" + getType() + "]";
	}
}
