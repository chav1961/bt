package chav1961.bt.clipper.inner.vm;

import java.util.IdentityHashMap;

import chav1961.bt.clipper.inner.MutableClipperValue;
import chav1961.bt.clipper.inner.NamedClipperParameter;
import chav1961.bt.clipper.inner.interfaces.ClipperParameter;
import chav1961.bt.clipper.inner.interfaces.ClipperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.bt.clipper.inner.interfaces.StackFrame;
import chav1961.purelib.basic.LongIdMap;

public class TopStackFrame extends AbstractStackFrame {
	private final ClipperParameter[]	parameters, privates, locals;
	private final ClipperValue[] 		parameterValues, localValues;
	private final LongIdMap<ClipperParameter>						globalParameterMap = new LongIdMap<>(ClipperParameter.class);
	private final IdentityHashMap<ClipperParameter, ClipperValue>	privateValueMap = new IdentityHashMap<>();
	private final IdentityHashMap<ClipperParameter, ClipperValue>	globalValueMap = new IdentityHashMap<>();
	
	protected TopStackFrame(final ClipperParameter[] parameters, final ClipperValue[] values, final ClipperParameter[] privates, final ClipperParameter[] locals) {
		super(null, 0, 0);
		this.parameters = parameters;
		this.privates = privates;
		this.locals = locals;
		this.parameterValues = values;
		this.localValues = new ClipperValue[locals.length];
	}

	private static final ClipperParameter[]	EMPTY = new ClipperParameter[0]; 

	@Override
	public StackFrame getParent() {
		return null;
	}

	@Override
	public ClipperParameter[] getParametersDeclarations() {
		return parameters;
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
	public ClipperParameter getLocalEntity(final int number) {
		return locals[number];
	}

	@Override
	public ClipperParameter getEntity(final long id) {
		final ClipperParameter	parm = super.getEntity(id);
		
		if (parm == null) {	// Global variables support
			final ClipperParameter	newParm;
			
			if (!globalParameterMap.contains(id)) {
				newParm = new NamedClipperParameter(id, true, ClipperType.C_Any);
				globalParameterMap.put(id, newParm);
			}
			else {
				newParm = globalParameterMap.get(id);
			}
			return newParm;
		}
		else {
			return parm;
		}
	}	
	
	@Override
	public ClipperValue getValueAssociated(final ClipperParameter parameter) {
		if (parameter.getId() < 0) {	// locals
			if (localValues[(int) (1-parameter.getId())] == null) {
				return localValues[(int) (1-parameter.getId())] = new MutableClipperValue(parameter.getType());
			}
			else {
				return localValues[(int) (1-parameter.getId())];
			}
		}
		else {
			ClipperValue	val = privateValueMap.get(parameter);
			
			if (val == null) {			// private value is missing
				if (globalParameterMap.contains(parameter.getId())) {
					val = globalValueMap.get(parameter);
					
					if (val == null) {
						val = new MutableClipperValue(parameter.getType());
						globalValueMap.put(parameter, val);
					}
					return val;
				}
				else {
					val = new MutableClipperValue(parameter.getType());
					privateValueMap.put(parameter, val);
					return val;
				}
			}
			else {
				return val;
			}
		}
	}

	@Override
	public Location getEntityLocation(final long id) {
		// TODO Auto-generated method stub
		if (id < 0) {
			return Location.LOCAL;
		}
		else {
			return null;
		}
	}
}
