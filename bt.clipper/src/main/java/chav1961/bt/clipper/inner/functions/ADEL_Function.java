package chav1961.bt.clipper.inner.functions;

import chav1961.bt.clipper.inner.AbstractClipperFunction;
import chav1961.bt.clipper.inner.AbstractClipperValue;
import chav1961.bt.clipper.inner.AnonymousClipperParameter;
import chav1961.bt.clipper.interfaces.ClipperType;
import chav1961.bt.clipper.interfaces.ClipperValue;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class ADEL_Function extends AbstractClipperFunction {
	private static final long serialVersionUID = -4730484660325717512L;
	
	protected ADEL_Function(ClipperType type, long id) {
		super(type, AnonymousClipperParameter.ANON_ARRAY, id, AnonymousClipperParameter.ANON_ARRAY, AnonymousClipperParameter.ANON_NUMBER);
	}

	@Override
	public ClipperValue invoke(final ClipperValue... parameters) throws ContentException {
		checkInputParameters(parameters);
		
		final ClipperValue[]	from = (ClipperValue[]) parameters[0].get(), to = from.clone();
		final int				index = ((Number)parameters[1].get()).intValue();
		if (index < 0 || index >= from.length) {
			throw new SyntaxException(0,0,""); 
		}
		else {
			System.arraycopy(from, index+1, to, index, from.length-index);
			to[to.length-1] = null;
		}
		return new AbstractClipperValue(ClipperType.C_Array) {
			@Override
			public <T> ClipperValue set(ClipperValue value) throws SyntaxException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public <T> ClipperValue set(T value) throws SyntaxException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public <T> T get() throws SyntaxException {
				return (T)to;
			}
		};
	}
}
