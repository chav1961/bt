package chav1961.bt.clipper.inner.vm;

import java.util.ArrayList;
import java.util.List;

import chav1961.bt.clipper.inner.ImmutableClipperValue;
import chav1961.bt.clipper.inner.interfaces.ClipperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;

public class ConstantPool extends ArrayList<ClipperValue> {
	private static final long serialVersionUID = 7428622990022169551L;
	
	public static final int		FALSE_ID = 0;
	public static final int		TRUE_ID = 1;
	public static final int		ZERO_ID = 2;
	public static final int		ONE_ID = 3;
	
	private final List<Integer>	stackPoints = new ArrayList<>();

	public ConstantPool() {
		add(new ImmutableClipperValue(ClipperType.C_Boolean, false));
		add(new ImmutableClipperValue(ClipperType.C_Boolean, true));
		add(new ImmutableClipperValue(ClipperType.C_Number, 0));
		add(new ImmutableClipperValue(ClipperType.C_Number, 1));
	}
	
	public void push() {
		stackPoints.add(0,size());
	}
	
	public void pop() {
		final int	top = stackPoints.remove(0);
		
		while (size() > top) {
			remove(size()-1);
		}
	}
}
