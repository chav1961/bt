package chav1961.bt.clipper.inner.vm;

import java.util.ArrayList;
import java.util.List;

import chav1961.bt.clipper.inner.interfaces.ClipperValue;

public class ConstantPool extends ArrayList<ClipperValue> {
	private static final long serialVersionUID = 7428622990022169551L;
	
	private final List<Integer>	stackPoints = new ArrayList<>();

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
