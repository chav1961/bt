package chav1961.bt.clipper.inner.interfaces;

import java.sql.Clob;
import java.sql.Date;
import java.util.function.Predicate;

import chav1961.bt.clipper.inner.ImmutableClipperValue;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.sql.SQLUtils;

public enum ClipperType {
	C_Any(Object.class, (t)->true),
	C_Void(Void.class, (t)->false),
	C_Array(ClipperValue[].class, (t)->t.getNativeClass() == ClipperValue[].class),
	C_Block(ClipperExecutableValue.class, (t)->t.getNativeClass() == ClipperExecutableValue.class),
	C_String(String.class, (t)->true),
	C_Number(Number.class, (t)->t.getNativeClass() == Number.class || t.getNativeClass() == String.class),
	C_Boolean(Boolean.class, (t)->t.getNativeClass() == Boolean.class || t.getNativeClass() == String.class),
	C_Date(Date.class, (t)->t.getNativeClass() == Date.class || t.getNativeClass() == String.class),
	C_Memo(Clob.class, (t)->t.getNativeClass() == Clob.class || t.getNativeClass() == String.class);
	
	private final Class<?>					awaited;
	private final Predicate<ClipperType>	compatibility;
	
	private ClipperType(final Class<?> awaited, final Predicate<ClipperType> compatibility) {
		this.awaited = awaited;
		this.compatibility = compatibility;
	}
	
	public Class<?> getNativeClass() {
		return awaited;
	}
	
	public boolean isCompatible(final ClipperType type) {
		if (type == null) {
			throw new NullPointerException("Type to test can't be null"); 
		}
		else {
			return compatibility.test(type);
		}
	}
	
	public ClipperValue convert(final ClipperValue value) throws SyntaxException {
		if (value == null) {
			throw new NullPointerException("Value to convert can't be null"); 
		}
		else if (!isCompatible(value.getType())) {
			throw new IllegalArgumentException("Value type ["+value.getType()+"] can't be converted to ["+this+"]"); 
		}
		else if (value.getType() == this) {	// Conversion is not required
			return value;
		}
		else {
			try{return new ImmutableClipperValue(this, SQLUtils.convert(this.getNativeClass(), value.get(value.getType().getNativeClass())));
			} catch (ContentException e) {
				throw new SyntaxException(0, 0, e.getLocalizedMessage());
			}
		}
	}
}