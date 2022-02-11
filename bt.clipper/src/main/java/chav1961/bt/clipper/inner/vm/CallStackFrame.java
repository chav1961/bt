package chav1961.bt.clipper.inner.vm;

import java.util.IdentityHashMap;

import chav1961.bt.clipper.ClipperRuntime;
import chav1961.bt.clipper.inner.MutableClipperValue;
import chav1961.bt.clipper.inner.interfaces.ClipperParameter;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.bt.clipper.inner.interfaces.StackFrame;

public class CallStackFrame extends AbstractStackFrame {

	private final FunctionTemplateStackFrame 	template;
	private final StackFrame					parent;
	private final ClipperValue[]				locals;
	private final IdentityHashMap<ClipperParameter, ClipperValue>	parameterValueMap = new IdentityHashMap<>();
	private final IdentityHashMap<ClipperParameter, ClipperValue>	privateValueMap = new IdentityHashMap<>();
	
	CallStackFrame(final FunctionTemplateStackFrame template, final StackFrame parent, final ClipperValue[] topStack, final int topStackSize) {
		super(template.getFunctionAssociated(), template.getFileAssociated(), template.getLineAssociated());
		this.parent = parent;
		this.template = template;
		this.locals = new ClipperValue[template.getLocalDeclarations().length];
		for (int index = 0; index < template.getFunctionAssociated().getMaxParameterCount(); index++) {
			if (topStackSize + index < topStack.length) {
				parameterValueMap.put(template.getFunctionAssociated().getParameter(index), topStack[index+topStackSize]);
			}
			else {
				parameterValueMap.put(template.getFunctionAssociated().getParameter(index), ClipperRuntime.NULL);
			}
		}
	}

	@Override
	public StackFrame getParent() {
		return parent;
	}

	@Override
	public ClipperParameter[] getLocalDeclarations() {
		return template.getLocalDeclarations();
	}

	@Override
	public ClipperParameter[] getPrivateDeclarations() {
		return template.getLocalDeclarations();
	}

	@Override
	public ClipperParameter getLocalEntity(final int number) {
		return template.getLocalEntity(number);
	}
	
	@Override
	public ClipperValue getValueAssociated(final ClipperParameter parameter) {
		switch (getEntityLocation(parameter.getId())) {
			case LOCAL		:
				if (locals[(int) (1-parameter.getId())] == null) {
					return locals[(int) (1-parameter.getId())] = new MutableClipperValue(parameter.getType());
				}
				else {
					return locals[(int) (1-parameter.getId())];
				}
			case NONLOCAL	:
				return getParent().getValueAssociated(parameter);
			case PARAMETER	:
				return parameterValueMap.get(parameter);
			case PRIVATE	:
				if (!privateValueMap.containsKey(parameter)) {
					privateValueMap.put(parameter, new MutableClipperValue(parameter.getType()));
				}
				return privateValueMap.get(parameter);
			case UNKNOWN	:
				return ClipperRuntime.NULL;
			default	: throw new UnsupportedOperationException("Entity location ["+getEntityLocation(parameter.getId())+"] is not supported yet");
		}
	}

	@Override
	public Location getEntityLocation(final long id) {
		switch (template.getEntityLocation(id)) {
			case LOCAL		:
				return Location.LOCAL;
			case PARAMETER	:
				return Location.PARAMETER;
			case PRIVATE	:
				return Location.PRIVATE;
			default:
				return getParent().getEntityLocation(id);
		}
	}
}
