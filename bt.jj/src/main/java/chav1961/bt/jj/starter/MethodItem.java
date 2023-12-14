package chav1961.bt.jj.starter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

class MethodItem {
	public final int	methodName;
	public final int	methodDesc;
	public final int	accessFlags;
	public final AttributeItem[]	attrs;
	public int 		parametersSize;
	public int 		localSize;
	public int 		stackSize;
	public byte[]	code;
	
	private final ConstantPoolItem[]	pool;
	
	public MethodItem(final int methodName, final int methodDesc, final int accessFlags, final ConstantPoolItem[] pool, final AttributeItem... attrs) {
		this.methodName = methodName;
		this.methodDesc = methodDesc;
		this.accessFlags = accessFlags;
		this.attrs = attrs;
		this.pool = pool;
		if (!Modifier.isAbstract(accessFlags)) {
			for(AttributeItem item : attrs) {
				switch (item.type) {
					case Code :
						break;
					default :
						break;
				}
			}
		}
	}
	
	public ConstantPoolItem prepareMethod() {
		return new ConstantPoolItem(accessFlags, methodName, methodDesc, accessFlags, null);
	}

	public Method getMethodInstance() {
		return null;
	}
	
	@Override
	public String toString() {
		return "MethodItem [methodName=" + ClassDefinitionLoader.resolveDescriptor(pool, methodName) + ", methodDesc=" + ClassDefinitionLoader.resolveDescriptor(pool, methodDesc) + ", accessFlags=" + Modifier.toString(accessFlags) + ", attrs=" + Arrays.toString(attrs) + "]";
	}
}
