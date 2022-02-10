package chav1961.bt.clipper.inner;

import java.util.Arrays;
import java.util.Iterator;

import chav1961.bt.clipper.interfaces.ClipperFunction;
import chav1961.bt.clipper.interfaces.ClipperParameter;
import chav1961.bt.clipper.interfaces.ClipperType;
import chav1961.bt.clipper.interfaces.ClipperValue;
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
	public ClipperParameter getParameter(final int parameterNo) {
		return parm[parameterNo];
	}

	@Override
	public ClipperParameter getReturnType() {
		return ret;
	}

	@Override
	public <T> T get() throws SyntaxException {
		throw new IllegalStateException("Calling this method is not applicable with the class");
	}

	@Override
	public <T> ClipperValue set(T value) throws SyntaxException {
		throw new IllegalStateException("Calling this method is not applicable with the class");
	}

	@Override
	public <T> ClipperValue set(final ClipperValue value) throws SyntaxException {
		throw new IllegalStateException("Calling this method is not applicable with the class");
	}
	
	protected void checkInputParameters(final ClipperValue... parameters) throws SyntaxException {
		
	}
}
