package chav1961.bt.clipper.inner;

import chav1961.bt.clipper.inner.interfaces.ClipperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.purelib.basic.exceptions.SyntaxException;

public abstract class AbstractClipperValue implements ClipperValue {
	private static final long serialVersionUID = 496465876729612599L;

	private final ClipperType	type;
	
	protected AbstractClipperValue(final ClipperType type) {
		this.type = type;
	}
	
	@Override
	public ClipperType getType() {
		return type;
	}

	@Override
	public <T> ClipperValue set(final ClipperValue value) throws SyntaxException {
		if (value == null) {
			throw new NullPointerException("Value to set can't be null"); 
		}
		else {
			return set(value.get(getType().getNativeClass()));
		}
	}
	
	@Override
	public ClipperValue clone() throws CloneNotSupportedException {
		return (ClipperValue) super.clone();
	}
}
