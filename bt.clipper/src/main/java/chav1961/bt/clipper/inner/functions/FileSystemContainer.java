package chav1961.bt.clipper.inner.functions;

import chav1961.bt.clipper.inner.AbstractBuiltinClipperFunction;
import chav1961.bt.clipper.inner.AnonymousClipperParameter;
import chav1961.bt.clipper.inner.ImmutableClipperValue;
import chav1961.bt.clipper.inner.interfaces.ClipperSyntaxEntity;
import chav1961.bt.clipper.inner.interfaces.ClipperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.bt.clipper.inner.interfaces.FunctionsContainer;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class FileSystemContainer implements FunctionsContainer {
	private static final long serialVersionUID = -4730484660325717512L;
	
	private final FileSystemInterface	fsi;
	
	public FileSystemContainer(final FileSystemInterface fsi) {
		if (fsi == null) {
			throw new NullPointerException("File system interface can't be null"); 
		}
		else {
			this.fsi = fsi;
		}
	}

	@Override
	public void prepare(final SyntaxTreeInterface<ClipperSyntaxEntity> tree) {
		if (tree == null) {
			throw new NullPointerException("Tree can't be null");
		}
		else {
			InternalUtils.placeBuiltin(tree, "FErase", (id)->new FERASE_Function(ClipperType.C_Void, id));
			InternalUtils.placeBuiltin(tree, "FRename", (id)->new FRENAME_Function(ClipperType.C_Void, id));
			InternalUtils.placeBuiltin(tree, "__CopyFile", (id)->new COPYFILE_Function(ClipperType.C_Void, id));
			InternalUtils.placeBuiltin(tree, "__Dir", (id)->new DIR_Function(ClipperType.C_Array, id));
			InternalUtils.placeBuiltin(tree, "__TypeFile", (id)->new TYPEFILE_Function(ClipperType.C_Void, id));
		}
	}

	private class FERASE_Function extends AbstractBuiltinClipperFunction {
		private static final long serialVersionUID = FileSystemContainer.serialVersionUID;
		
		private FERASE_Function(final ClipperType type, final long id) {
			super(type, AnonymousClipperParameter.ANON_VOID, id, AnonymousClipperParameter.ANON_STRING);
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
	
	private class FRENAME_Function extends AbstractBuiltinClipperFunction {
		private static final long serialVersionUID = FileSystemContainer.serialVersionUID;
		
		private FRENAME_Function(final ClipperType type, final long id) {
			super(type, AnonymousClipperParameter.ANON_VOID, id, AnonymousClipperParameter.ANON_STRING);
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

	private class COPYFILE_Function extends AbstractBuiltinClipperFunction {
		private static final long serialVersionUID = FileSystemContainer.serialVersionUID;
		
		private COPYFILE_Function(final ClipperType type, final long id) {
			super(type, AnonymousClipperParameter.ANON_VOID, id, AnonymousClipperParameter.ANON_STRING, AnonymousClipperParameter.ANON_STRING);
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

	private class DIR_Function extends AbstractBuiltinClipperFunction {
		private static final long serialVersionUID = FileSystemContainer.serialVersionUID;
		
		private DIR_Function(final ClipperType type, final long id) {
			super(type, AnonymousClipperParameter.ANON_ARRAY, id, AnonymousClipperParameter.ANON_STRING);
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

	private class TYPEFILE_Function extends AbstractBuiltinClipperFunction {
		private static final long serialVersionUID = FileSystemContainer.serialVersionUID;
		
		private TYPEFILE_Function(final ClipperType type, final long id) {
			super(type, AnonymousClipperParameter.ANON_VOID, id, AnonymousClipperParameter.ANON_STRING);
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
}
