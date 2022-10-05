package chav1961.bt.clipper.inner.functions;

import java.util.Arrays;

import chav1961.bt.clipper.inner.AbstractBuiltinClipperFunction;
import chav1961.bt.clipper.inner.AnonymousClipperParameter;
import chav1961.bt.clipper.inner.ImmutableClipperValue;
import chav1961.bt.clipper.inner.interfaces.ClipperParameter;
import chav1961.bt.clipper.inner.interfaces.ClipperSyntaxEntity;
import chav1961.bt.clipper.inner.interfaces.ClipperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.bt.clipper.inner.interfaces.FunctionsContainer;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class RuntimeContainer implements FunctionsContainer {
	public RuntimeContainer() {
		
	}

	@Override
	public void prepare(final SyntaxTreeInterface<ClipperSyntaxEntity> tree) {
		if (tree == null) {
			throw new NullPointerException("Tree can't be null"); 
		}
		else {
			InternalUtils.placeBuiltin(tree, "AADD", (id)->new AADD_Function(ClipperType.C_Array, id));
			InternalUtils.placeBuiltin(tree, "ABS", (id)->new ABS_Function(ClipperType.C_Number, id));
			InternalUtils.placeBuiltin(tree, "ACLONE", (id)->new ACLONE_Function(ClipperType.C_Array, id));
			InternalUtils.placeBuiltin(tree, "ACOPY", (id)->new ACOPY_Function(ClipperType.C_Array, id));
			InternalUtils.placeBuiltin(tree, "ADEL", (id)->new ADEL_Function(ClipperType.C_Array, id));
			InternalUtils.placeBuiltin(tree, "AFILL", (id)->new AFILL_Function(ClipperType.C_Array, id));
			InternalUtils.placeBuiltin(tree, "AINS", (id)->new AINS_Function(ClipperType.C_Array, id));
			InternalUtils.placeBuiltin(tree, "ARRAY", (id)->new ARRAY_Function(ClipperType.C_Array, id));
			InternalUtils.placeBuiltin(tree, "ASIZE", (id)->new ASIZE_Function(ClipperType.C_Array, id));
		}
	}

	private static boolean isIntegerType(final Number value) {
		return (value instanceof Byte) || (value instanceof Short) || (value instanceof Integer) || (value instanceof Long);  
	}
	
	private static boolean isFloatType(final Number value) {
		return (value instanceof Float) || (value instanceof Double);  
	}
	
	private class AADD_Function extends AbstractBuiltinClipperFunction {
		private static final long serialVersionUID = -4730484660325717512L;
		
		private AADD_Function(final ClipperType type, final long id) {
			super(type, AnonymousClipperParameter.ANON_ARRAY, id, AnonymousClipperParameter.ANON_ARRAY, AnonymousClipperParameter.ANON_ANY);
		}

		@Override
		public ClipperValue invoke(final int parameterCount, final ClipperValue... parameters) throws ContentException {
			checkInputParameters(parameterCount, parameters);
			
			final ClipperValue[]	from = parameters[0].get(ClipperValue[].class);
			final ClipperValue[]	to = new ClipperValue[from.length + 1]; 
			final ClipperValue		item = parameters[1].get(ClipperValue.class);
			
			System.arraycopy(from, 0, to, 0, from.length);
			to[to.length - 1] = item;
			return new ImmutableClipperValue(ClipperType.C_Array, to);
		}
	}

	private class ABS_Function extends AbstractBuiltinClipperFunction {
		private static final long serialVersionUID = -4730484660325717512L;
		
		private ABS_Function(final ClipperType type, final long id) {
			super(type, AnonymousClipperParameter.ANON_NUMBER, id, AnonymousClipperParameter.ANON_NUMBER);
		}

		@Override
		public ClipperValue invoke(final int parameterCount, final ClipperValue... parameters) throws ContentException {
			checkInputParameters(parameterCount, parameters);
			
			final Number	value = parameters[0].get(Number.class);
			
			if (isIntegerType(value)) {
				return new ImmutableClipperValue(ClipperType.C_Number, -value.longValue());
			}
			else if (isFloatType(value)) {
				return new ImmutableClipperValue(ClipperType.C_Number, -value.doubleValue());
			}
			else {
				throw new SyntaxException(0,0,"???"); 
			}
		}
	}

	private class ACLONE_Function extends AbstractBuiltinClipperFunction {
		private static final long serialVersionUID = -4730484660325717512L;
		
		private ACLONE_Function(final ClipperType type, final long id) {
			super(type, AnonymousClipperParameter.ANON_ARRAY, id, AnonymousClipperParameter.ANON_ARRAY);
		}

		@Override
		public ClipperValue invoke(final int parameterCount, final ClipperValue... parameters) throws ContentException {
			checkInputParameters(parameterCount, parameters);
			
			final ClipperValue[]	from = parameters[0].get(ClipperValue[].class), to = from.clone();

			return new ImmutableClipperValue(ClipperType.C_Array, to);
		}
	}
	
	private class ACOPY_Function extends AbstractBuiltinClipperFunction {
		private static final long serialVersionUID = -4730484660325717512L;
		
		private ACOPY_Function(final ClipperType type, final long id) {
			super(type, AnonymousClipperParameter.ANON_ARRAY, id, AnonymousClipperParameter.ANON_ARRAY);
		}

		@Override
		public ClipperValue invoke(final int parameterCount, final ClipperValue... parameters) throws ContentException {
			checkInputParameters(parameterCount, parameters);
			
			final ClipperValue[]	from = parameters[0].get(ClipperValue[].class), to = from.clone();

			return new ImmutableClipperValue(ClipperType.C_Array, to);
		}
	}
	
	private class ADEL_Function extends AbstractBuiltinClipperFunction {
		private static final long serialVersionUID = -4730484660325717512L;
		
		private ADEL_Function(final ClipperType type, final long id) {
			super(type, AnonymousClipperParameter.ANON_ARRAY, id, AnonymousClipperParameter.ANON_ARRAY, AnonymousClipperParameter.ANON_NUMBER);
		}

		@Override
		public ClipperValue invoke(final int parameterCount, final ClipperValue... parameters) throws ContentException {
			checkInputParameters(parameterCount, parameters);
			
			final ClipperValue[]	from = parameters[0].get(ClipperValue[].class), to = from.clone();
			final int				index = parameters[1].get(Number.class).intValue();
			
			if (index < 1 || index > from.length) {
				throw new SyntaxException(0,0,"Array index to delete ["+index+"] out of range 1.."+from.length); 
			}
			else {
				System.arraycopy(from, index, to, index - 1, from.length - index);
				to[to.length - 1] = null;
				return new ImmutableClipperValue(ClipperType.C_Array, to);
			}
		}
	}

	private class AFILL_Function extends AbstractBuiltinClipperFunction {
		private static final long serialVersionUID = -4730484660325717512L;
		
		private AFILL_Function(final ClipperType type, final long id) {
			super(type, AnonymousClipperParameter.ANON_ARRAY, id, AnonymousClipperParameter.ANON_ARRAY, AnonymousClipperParameter.ANON_ANY);
		}

		@Override
		public ClipperValue invoke(final int parameterCount, final ClipperValue... parameters) throws ContentException {
			checkInputParameters(parameterCount, parameters);
			
			final ClipperValue[]	from = parameters[0].get(ClipperValue[].class);
			final ClipperValue		item = parameters[1].get(ClipperValue.class);

			for (int index = 0; index < from.length; index++) {
				try{from[index] = item.clone();
				} catch (CloneNotSupportedException e) {
					throw new SyntaxException(0,0,"Array index to delete ["+index+"] out of range 1.."+from.length); 
				}
			}
			return new ImmutableClipperValue(ClipperType.C_Array, from);
		}
	}

	private class AINS_Function extends AbstractBuiltinClipperFunction {
		private static final long serialVersionUID = -4730484660325717512L;
		
		private AINS_Function(final ClipperType type, final long id) {
			super(type, AnonymousClipperParameter.ANON_ARRAY, id, AnonymousClipperParameter.ANON_ARRAY, AnonymousClipperParameter.ANON_NUMBER);
		}

		@Override
		public ClipperValue invoke(final int parameterCount, final ClipperValue... parameters) throws ContentException {
			checkInputParameters(parameterCount, parameters);
			
			final ClipperValue[]	from = parameters[0].get(ClipperValue[].class), to = from.clone();
			final int				index = parameters[1].get(Number.class).intValue();
			
			if (index < 1 || index > from.length) {
				throw new SyntaxException(0,0,"Array index to delete ["+index+"] out of range 1.."+from.length); 
			}
			else {
				if (index < from.length) {
					System.arraycopy(from, index - 1, to, index, from.length - index);
				}
				to[index-1] = null;
				return new ImmutableClipperValue(ClipperType.C_Array, to);
			}
		}
	}
	
	private class ARRAY_Function extends AbstractBuiltinClipperFunction {
		private static final long serialVersionUID = -4730484660325717512L;
		
		private ARRAY_Function(final ClipperType type, final long id) {
			super(type, AnonymousClipperParameter.ANON_ARRAY, id, AnonymousClipperParameter.ANON_NUMBER);
		}

		@Override
		public int getMinParameterCount() {
			return 1;
		}

		@Override
		public int getMaxParameterCount() {
			return 255;
		}
		
		@Override
		public ClipperParameter getParameter(int parameterNo) {
			return AnonymousClipperParameter.ANON_NUMBER;
		}
		
		@Override
		public ClipperValue invoke(final int parameterCount, final ClipperValue... parameters) throws ContentException {
			checkInputParameters(parameterCount, parameters);

			final int[]		dimensions = new int[parameters.length];
			
			for (int index = 0; index < dimensions.length; index++) {
				dimensions[index] = parameters[index].get(Number.class).intValue();
			}
			
			return new ImmutableClipperValue(ClipperType.C_Array, new ClipperValue[dimensions[0]]);
		}
	}

	private class ASIZE_Function extends AbstractBuiltinClipperFunction {
		private static final long serialVersionUID = -4730484660325717512L;
		
		private ASIZE_Function(final ClipperType type, final long id) {
			super(type, AnonymousClipperParameter.ANON_ARRAY, id, AnonymousClipperParameter.ANON_ARRAY, AnonymousClipperParameter.ANON_NUMBER);
		}

		@Override
		public ClipperValue invoke(final int parameterCount, final ClipperValue... parameters) throws ContentException {
			checkInputParameters(parameterCount, parameters);
			
			final ClipperValue[]	from = parameters[0].get(ClipperValue[].class);
			final int				size = parameters[1].get(Number.class).intValue();
			
			return new ImmutableClipperValue(ClipperType.C_Array, Arrays.copyOf(from, size));
		}
	}
}
