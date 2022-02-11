package chav1961.bt.clipper.inner;

import chav1961.bt.clipper.inner.interfaces.ClipperBuiltinFunction;
import chav1961.bt.clipper.inner.interfaces.ClipperParameter;
import chav1961.bt.clipper.inner.interfaces.ClipperType;

public abstract class AbstractBuiltinClipperFunction extends AbstractClipperFunction implements ClipperBuiltinFunction {
	private static final long serialVersionUID = 7868849675255908416L;
	
	protected AbstractBuiltinClipperFunction(final ClipperType type, final ClipperParameter ret, final long id, final ClipperParameter... parameters) {
		super(type, ret, id, parameters);
	}
	
	@Override
	public int getLocalStackSize() {
		return 0;
	}
}
