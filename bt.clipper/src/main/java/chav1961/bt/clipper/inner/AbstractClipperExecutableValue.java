package chav1961.bt.clipper.inner;

import chav1961.bt.clipper.interfaces.ClipperExecutableValue;
import chav1961.bt.clipper.interfaces.ClipperType;
import chav1961.bt.clipper.interfaces.ClipperValue;
import chav1961.purelib.basic.exceptions.ContentException;

public abstract class AbstractClipperExecutableValue extends AbstractClipperValue implements ClipperExecutableValue {
	private static final long serialVersionUID = -5509539388606711361L;
	
	protected AbstractClipperExecutableValue(final ClipperType type) {
		super(type);
	}

	@Override public abstract ClipperValue invoke(ClipperValue... parameters) throws ContentException;
}
