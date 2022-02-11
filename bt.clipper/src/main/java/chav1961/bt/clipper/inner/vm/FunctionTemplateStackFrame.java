package chav1961.bt.clipper.inner.vm;

import chav1961.bt.clipper.inner.interfaces.ClipperFunction;
import chav1961.bt.clipper.inner.interfaces.ClipperParameter;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.bt.clipper.inner.interfaces.StackFrame;
import chav1961.purelib.basic.LongIdMap;

public class FunctionTemplateStackFrame extends AbstractStackFrame {
	private final ClipperParameter[] 			privates;
	private final ClipperParameter[] 			locals;
	private final LongIdMap<ClipperParameter>	privateParameterMap = new LongIdMap<>(ClipperParameter.class);
	private final LongIdMap<ClipperParameter>	functionParameterMap = new LongIdMap<>(ClipperParameter.class);
	
	
	public FunctionTemplateStackFrame(final ClipperFunction func, final int fileAssoc, final int lineAssoc, final ClipperParameter[] privates, final ClipperParameter[] locals) {
		super(func, fileAssoc, lineAssoc);
		this.privates = privates;
		this.locals = locals;
		for (ClipperParameter item : privates) {
			privateParameterMap.put(item.getId(), item);
		}
		for (ClipperParameter item : func.getParameters()) {
			functionParameterMap.put(item.getId(), item);
		}
	}

	@Override
	public StackFrame getParent() {
		return null;
	}

	@Override
	public ClipperParameter[] getLocalDeclarations() {
		return locals;
	}

	@Override
	public ClipperParameter[] getPrivateDeclarations() {
		return privates;
	}

	@Override
	public ClipperParameter getLocalEntity(int number) {
		return getLocalDeclarations()[number];
	}

	@Override
	public ClipperParameter getEntity(final long id) {
		switch (getEntityLocation(id)) {
			case LOCAL		:
				return locals[(int) (1-id)];
			case PARAMETER	:
				return functionParameterMap.get(id);
			case PRIVATE	:
				return privateParameterMap.get(id);
			default:
				return null;
		}
	}

	@Override
	public Location getEntityLocation(final long id) {
		if (id < 0) {
			return Location.LOCAL;
		}
		else if (functionParameterMap.contains(id)) {
			return Location.PARAMETER;
		}
		else if (privateParameterMap.contains(id)) {
			return Location.PRIVATE;
		}
		else {
			return Location.UNKNOWN;
		}
	}
	
	@Override
	public ClipperValue getValueAssociated(final ClipperParameter parameter) {
		throw new IllegalThreadStateException("This method can't be called");
	}
	
	public StackFrame bildCallStackFrame(final StackFrame parent, final ClipperValue[] stack, final int stackParametersCount) {
		return new CallStackFrame(this, parent, stack, stackParametersCount);
	}
}
