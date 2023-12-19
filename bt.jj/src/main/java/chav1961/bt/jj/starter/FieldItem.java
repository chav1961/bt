package chav1961.bt.jj.starter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

class FieldItem {
	public final int	fieldName;
	public final int	fieldDesc;
	public final int	accessFlags;
	public final AttributeItem[]	attrs;
	public int 	displacement = 0;
	public int 	length = 0;
	
	private final ConstantPoolItem[]	pool;
	
	public FieldItem(final int fieldName, final int fieldDesc, final int accessFlags, final ConstantPoolItem[] pool, final AttributeItem... attrs) {
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
		return "FieldItem [fieldName=" + ClassDefinitionLoader.resolveDescriptor(pool, fieldName) + ", fieldDesc=" + ClassDefinitionLoader.resolveDescriptor(pool, fieldDesc) + ", accessFlags=" + Modifier.toString(accessFlags) + ", attrs=" + Arrays.toString(attrs) + "]";
	}
}
