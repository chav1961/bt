package chav1961.bt.clipper.inner;

import java.util.Arrays;
import java.util.Iterator;

import chav1961.bt.clipper.ClipperRuntime;
import chav1961.bt.clipper.inner.interfaces.ClipperFunction;
import chav1961.bt.clipper.inner.interfaces.ClipperIdentifiedValue;
import chav1961.bt.clipper.inner.interfaces.ClipperParameter;
import chav1961.bt.clipper.inner.interfaces.ClipperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.purelib.basic.exceptions.SyntaxException;

public abstract class AbstractClipperFunction extends AbstractClipperExecutableValue implements ClipperFunction {
	private static final long serialVersionUID = 6056285750770707031L;
	
	private final long					id;
	private final ClipperParameter[]	parm;
	private final ClipperParameter		ret;
	
	protected AbstractClipperFunction(final ClipperType type, final long id, final ClipperParameter... parameters) {
		super(type);
		this.id = id;
		this.parm = parameters;
		this.ret = null;
	}

	protected AbstractClipperFunction(final ClipperType type, final ClipperParameter ret, final long id, final ClipperParameter... parameters) {
		super(type);
		this.id = id;
		this.parm = parameters;
		this.ret = ret;
	}
	
	@Override
	public Iterator<ClipperParameter> iterator() {
		return Arrays.asList(parm).iterator();
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public boolean isProcedure() {
		return getReturnType() == null;
	}

	@Override
	public int getMaxParameterCount() {
		return parm.length;
	}

	@Override
	public int getMinParameterCount() {
		for (int index = parm.length-1; index >= 0; index--) {
			if (!getParameter(index).isOptional()) {
				return index+1;
			}
		}
		return 0;
	}

	@Override
	public ClipperParameter[] getParameters() {
		return parm;
	}
	
	@Override
	public ClipperParameter getParameter(final int parameterNo) {
		return parm[parameterNo];
	}

	@Override
	public ClipperParameter getReturnType() {
		return ret;
	}

	protected void checkInputParameters(final ClipperValue... parameters) throws SyntaxException {
		final int		minLength = Math.min(parm.length, parameters.length);
		StringBuilder	sb = null;
		
		for(int index = 0; index < minLength; index++) {
			if (getParameter(index).isOptional()) {
				if (parameters[index] == ClipperRuntime.NULL) {
					continue;
				}
				else if (!getParameter(index).isCompatibleWith(parameters[index].getType())) {
					if (sb == null) {
						sb = new StringBuilder();
					}
					sb.append("Parameter [").append(getParameterName(index)).append("] - value type ["+parameters[index].getType()+"] is not compatible with "+Arrays.toString(getParameter(index).getAllSupportedTypes())+"\n");
				}
			}
			else if (!getParameter(index).isCompatibleWith(parameters[index].getType())) {
				if (sb == null) {
					sb = new StringBuilder();
				}
				sb.append("Parameter [").append(getParameterName(index)).append("] - value type ["+parameters[index].getType()+"] is not compatible with "+Arrays.toString(getParameter(index).getAllSupportedTypes())+"\n");
			}
		}
		for (int index = minLength; index < parm.length;index++) {
			if (!getParameter(index).isOptional()) {
				if (sb == null) {
					sb = new StringBuilder();
				}
				sb.append("Mandatory parameter [").append(index).append("] is missing in the call stack frame\n");
			}
		}
		if (sb != null) {
			throw new SyntaxException(0, 0, sb.toString());
		}
	}
	
	protected String getParameterName(final int index) {
		final ClipperParameter	p = getParameter(index);
		
		if (p instanceof ClipperIdentifiedValue) {
			return ClipperRuntime.DICTIONARY.getName(p.getId());
		}
		else {
			return ""+index;
		}
	}
}
