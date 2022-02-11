package chav1961.bt.clipper.inner.interfaces;

public interface ClipperParameter extends ClipperValue, ClipperIdentifiedValue {
	boolean isOptional();
	boolean isCompatibleWith(ClipperType type);
	ClipperType[] getAllSupportedTypes();
}
