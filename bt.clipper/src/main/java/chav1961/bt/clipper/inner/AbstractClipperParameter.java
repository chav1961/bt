package chav1961.bt.clipper.inner;

import chav1961.bt.clipper.interfaces.ClipperIdentifiedValue;
import chav1961.bt.clipper.interfaces.ClipperParameter;
import chav1961.bt.clipper.interfaces.ClipperType;

public abstract  class AbstractClipperParameter extends AbstractClipperValue implements ClipperParameter, ClipperIdentifiedValue  {
	private static final long serialVersionUID = -6086605059895368478L;

	private final long			id;
	private final boolean		optional;
	private final ClipperType[]	supports;
	
	protected AbstractClipperParameter(final ClipperType type, final long id, final boolean optional) {
		super(type);
		this.id = id;
		this.optional = optional;
		this.supports = new ClipperType[]{type};
	}

	protected AbstractClipperParameter(final long id, final boolean optional, final ClipperType... types) {
		super(extractMostCommonType(types));
		this.id = id;
		this.optional = optional;
		this.supports = types;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public boolean isOptional() {
		return optional;
	}

	@Override
	public boolean isCompatibleWith(final ClipperType type) {
		if (type == null) {
			throw new NullPointerException("Type to test compatibility can't be null");
		}
		else {
			for (ClipperType item : getAllSupportedTypes()) {
				if (type == item) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public ClipperType[] getAllSupportedTypes() {
		return supports;
	}

	private static ClipperType extractMostCommonType(final ClipperType[] types) {
		return null;
	}
}
