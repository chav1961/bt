package chav1961.bt.clipper.inner.functions;

import chav1961.bt.clipper.inner.AbstractBuiltinClipperFunction;
import chav1961.bt.clipper.inner.AnonymousClipperParameter;
import chav1961.bt.clipper.inner.ImmutableClipperValue;
import chav1961.bt.clipper.inner.interfaces.ClipperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;

class ADEL_Function extends AbstractBuiltinClipperFunction {
	private static final long serialVersionUID = -4730484660325717512L;
	
	protected ADEL_Function(final ClipperType type, final long id) {
		super(type, AnonymousClipperParameter.ANON_ARRAY, id, AnonymousClipperParameter.ANON_ARRAY, AnonymousClipperParameter.ANON_NUMBER);
	}

	@Override
	public ClipperValue invoke(final int parameterCount, final ClipperValue... parameters) throws ContentException {
		checkInputParameters(parameterCount, parameters);
		
		final ClipperValue[]	from = parameters[0].get(ClipperValue[].class), to = from.clone();
		final int				index = parameters[1].get(Number.class).intValue();
		
		if (index < 0 || index >= from.length) {
			throw new SyntaxException(0,0,""); 
		}
		else {
			System.arraycopy(from, index + 1, to, index, from.length - index);
			to[to.length - 1] = null;
			return new ImmutableClipperValue(ClipperType.C_Array, to);
		}
	}
}
