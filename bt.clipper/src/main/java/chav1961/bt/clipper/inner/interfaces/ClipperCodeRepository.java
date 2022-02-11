package chav1961.bt.clipper.inner.interfaces;

public interface ClipperCodeRepository extends Iterable<ClipperFunction> {
	boolean contains(long id);
	ClipperFunction get(long id);
	ClipperFunction getMain();
	int size();
}
