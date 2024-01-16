package chav1961.bt.jj.starter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

class MethodItem {
	public final int	methodName;
	public final int	methodDesc;
	public final int	accessFlags;
	public final AttributeItem[]	attrs;
	final int			offset;
	
	private final ConstantPool	pool;
	
	public MethodItem(final int offset, final int methodName, final int methodDesc, final int accessFlags, final ConstantPool pool, final AttributeItem... attrs) {
		this.offset = offset;
		this.methodName = methodName;
		this.methodDesc = methodDesc;
		this.accessFlags = accessFlags;
		this.attrs = attrs;
		this.pool = pool;
	}
	
	public Method getMethodInstance() {
		return null;
	}
	
	@Override
	public String toString() {
		return "MethodItem [methodName=" +  pool.deepToString(methodName) + ", methodDesc=" + pool.deepToString(methodDesc) + ", accessFlags=" + Modifier.toString(accessFlags) + "]";
	}
}
