package chav1961.bt.clipper.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;

public interface ClipperExecutableValue extends ClipperValue {
	ClipperValue invoke(ClipperValue... parameters) throws ContentException;
}
