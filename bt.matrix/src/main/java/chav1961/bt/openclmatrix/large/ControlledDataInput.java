package chav1961.bt.openclmatrix.large;

import chav1961.purelib.streams.DataInputAdapter;

abstract class ControlledDataInput extends DataInputAdapter {
	public abstract long getReadAmount();
}
