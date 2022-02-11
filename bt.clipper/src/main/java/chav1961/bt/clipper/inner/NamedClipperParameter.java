package chav1961.bt.clipper.inner;

import chav1961.bt.clipper.inner.interfaces.ClipperType;

public class NamedClipperParameter extends AnonymousClipperParameter {
	private static final long serialVersionUID = 2997467580645184913L;

	private final long	id;
	
	public NamedClipperParameter(final long id, final boolean isOptional, final ClipperType... types) {
		super(0, isOptional, types);
		this.id = id;
	}

	@Override
	public long getId() {
		return id;
	}
}
