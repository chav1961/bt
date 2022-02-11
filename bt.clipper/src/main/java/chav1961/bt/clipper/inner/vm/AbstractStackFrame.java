package chav1961.bt.clipper.inner.vm;

import chav1961.bt.clipper.inner.interfaces.ClipperFunction;
import chav1961.bt.clipper.inner.interfaces.ClipperIdentifiedValue;
import chav1961.bt.clipper.inner.interfaces.ClipperParameter;
import chav1961.bt.clipper.inner.interfaces.StackFrame;

abstract class AbstractStackFrame implements StackFrame {
	private final ClipperFunction	func;
	private final int				fileAssoc, lineAssoc;
	
	protected AbstractStackFrame(final ClipperFunction func, final int fileAssoc, final int lineAssoc) {
		this.func = func;
		this.fileAssoc = fileAssoc;
		this.lineAssoc = lineAssoc;
	}
	
	@Override
	public int getFileAssociated() {
		return fileAssoc;
	}

	@Override
	public int getLineAssociated() {
		return lineAssoc;
	}

	@Override
	public ClipperFunction getFunctionAssociated() {
		return func;
	}

	@Override
	public StackFrame clone() throws CloneNotSupportedException {
		return (StackFrame)super.clone();
	}

	@Override
	public ClipperParameter[] getParametersDeclarations() {
		return getFunctionAssociated().getParameters();
	}

	@Override
	public ClipperParameter getEntity(final long id) {
		for (ClipperParameter item : getParametersDeclarations()) {
			if (item.getId() == id) {
				return item;
			}
		}
		for (ClipperParameter item : getPrivateDeclarations()) {
			if (item.getId() == id) {
				return item;
			}
		}
		final StackFrame	parent = getParent();
		
		if (parent != null) {
			return parent.getEntity(id);
		}
		else {
			return null;
		}
	}
}
