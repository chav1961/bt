package chav1961.bt.clipper.inner;

import chav1961.bt.clipper.interfaces.ClipperParameter;
import chav1961.bt.clipper.interfaces.ClipperType;
import chav1961.bt.clipper.interfaces.ClipperValue;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class AnonymousClipperParameter implements ClipperParameter {
	public static final ClipperParameter	ANON_ARRAY = new AnonymousClipperParameter(false, ClipperType.C_Array); 
	public static final ClipperParameter	ANON_NUMBER = new AnonymousClipperParameter(false, ClipperType.C_Number); 
	
	private static final long serialVersionUID = 909299429159738445L;

	private final boolean		isOptional;
	private final ClipperType[]	types; 
	
	public AnonymousClipperParameter(final boolean isOptional, final ClipperType... types) {
		this.isOptional = isOptional;
		this.types = types;
	}
	
	@Override
	public ClipperType getType() {
		return types[0];
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
	public <T> ClipperValue set(ClipperValue value) throws SyntaxException {
		throw new IllegalStateException("Calling this method is not applicable with the class");
	}

	@Override
	public ClipperValue clone() throws CloneNotSupportedException {
		return (ClipperValue)super.clone();
	}

	@Override
	public long getId() {
		return -1;
	}

	@Override
	public boolean isOptional() {
		return isOptional;
	}

	@Override
	public boolean isCompatibleWith(ClipperType type) {
		for (ClipperType item : getAllSupportedTypes()) {
			if (type == item) {
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