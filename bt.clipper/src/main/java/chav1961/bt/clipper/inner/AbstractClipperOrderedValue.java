package chav1961.bt.clipper.inner;

import chav1961.bt.clipper.interfaces.ClipperOrderedValue;
import chav1961.bt.clipper.interfaces.ClipperType;

public abstract class AbstractClipperOrderedValue extends AbstractClipperValue implements ClipperOrderedValue {
	private static final long serialVersionUID = -9080678477157692061L;
	
	protected AbstractClipperOrderedValue(final ClipperType type) {
		super(type);
	}

	@Override public abstract int compareTo(ClipperOrderedValue o);
}
