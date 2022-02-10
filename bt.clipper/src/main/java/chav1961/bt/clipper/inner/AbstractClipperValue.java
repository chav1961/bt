package chav1961.bt.clipper.inner;

import chav1961.bt.clipper.interfaces.ClipperType;
import chav1961.bt.clipper.interfaces.ClipperValue;
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

	@Override public abstract <T> T get() throws SyntaxException;
	@Override public abstract <T> ClipperValue set(T value) throws SyntaxException;
	@Override public abstract <T> ClipperValue set(ClipperValue value) throws SyntaxException;

	@Override
	public ClipperValue clone() throws CloneNotSupportedException {
		return (ClipperValue) super.clone();
	}
}
