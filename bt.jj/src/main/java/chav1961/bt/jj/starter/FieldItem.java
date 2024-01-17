package chav1961.bt.jj.starter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

class FieldItem {
	public final int	fieldName;
	public final int	fieldDesc;
	public final int	accessFlags;
	public final AttributeItem[]	attrs;
	public long			displacement = 0;
	public long			length = 0;
	final int			offset;
	
	private final ConstantPool	pool;
	
	public FieldItem(final int offset, final int fieldName, final int fieldDesc, final int accessFlags, final ConstantPool pool, final AttributeItem... attrs) {
		this.offset = offset;
		this.fieldName = fieldName;
		this.fieldDesc = fieldDesc;
		this.accessFlags = accessFlags;
		this.pool = pool;
		this.attrs = attrs;
	}
	
	public Field getFieldInstance() {
		return null;
	}

	@Override
	public String toString() {
		return "FieldItem [fieldName=" +  pool.deepToString(fieldName) + ", fieldDesc=" +  pool.deepToString(fieldDesc) + ", accessFlags=" + Modifier.toString(accessFlags) + ", attrs=" + Arrays.toString(attrs) + "]";
	}
}
