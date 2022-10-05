package chav1961.bt.clipper.inner;

import chav1961.bt.clipper.inner.interfaces.ClipperParameter;
import chav1961.bt.clipper.inner.interfaces.ClipperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class AnonymousClipperParameter implements ClipperParameter {
	public static final ClipperParameter	ANON_ARRAY = new AnonymousClipperParameter(-1, false, ClipperType.C_Array); 
	public static final ClipperParameter	ANON_NUMBER = new AnonymousClipperParameter(-1, false, ClipperType.C_Number); 
	public static final ClipperParameter	ANON_STRING = new AnonymousClipperParameter(-1, false, ClipperType.C_String); 
	public static final ClipperParameter	ANON_VOID = new AnonymousClipperParameter(-1, false, ClipperType.C_Void); 
	public static final ClipperParameter	ANON_ANY = new AnonymousClipperParameter(-1, false, ClipperType.C_Any); 
	
	private static final long serialVersionUID = 909299429159738445L;

	private final int			order;
	private final boolean		isOptional;
	private final ClipperType[]	types; 
	
	public AnonymousClipperParameter(final int order, final boolean isOptional, final ClipperType... types) {
		this.order = order;
		this.isOptional = isOptional;
		this.types = types;
	}
	
	@Override
	public ClipperType getType() {
		return types[0];
	}

	@Override
	public Object get() {
		throw new IllegalStateException("Calling this method is not applicable with the class");
	}
	
	@Override
	public <T> T get(final Class<T> awaited) throws SyntaxException {
		throw new IllegalStateException("Calling this method is not applicable with the class");
	}

	@Override
	public <T> ClipperValue set(T value) throws SyntaxException {
		throw new IllegalStateException("Calling this method is not applicable with the class");
	}

	@Override
	public <T> ClipperValue set(ClipperValue value) throws SyntaxException {
		throw new IllegalStateException("Calling this method is not applicable with the class");
	}

	@Override
	public ClipperValue clone() throws CloneNotSupportedException {
		return (ClipperValue)super.clone();
	}

	@Override
	public long getId() {
		return -order;
	}

	@Override
	public boolean isOptional() {
		return isOptional;
	}

	@Override
	public boolean isCompatibleWith(final ClipperType type) {
		for (ClipperType item : getAllSupportedTypes()) {
			if (type == item || item == ClipperType.C_Any) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ClipperType[] getAllSupportedTypes() {
		return types;
	}
}
