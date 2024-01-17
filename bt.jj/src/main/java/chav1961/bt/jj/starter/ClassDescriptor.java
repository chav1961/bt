package chav1961.bt.jj.starter;

import java.lang.reflect.Modifier;

import chav1961.purelib.cdb.JavaClassVersion;

public class ClassDescriptor {
	private final ConstantPool			pool;
	private final JavaClassVersion		version;
	private final int					accessFlags;
	private final int					thisClass;
	private final int					superClass;
	private final InterfaceItem[]		interfaces;
	private final FieldItem[]			fields;
	private final MethodItem[]			methods;
	private final AttributeItem[]		attributes;
	private final int					staticPart;
	private final int					instancePiecePart;
	
	ClassDescriptor(final ConstantPool pool, final JavaClassVersion version, final int accessFlags, final int thisClass, final int superClass, final InterfaceItem[] interfaces, final FieldItem[] fields, final MethodItem[] methods, final AttributeItem[] attributes) {
		this.pool = pool;
		this.version = version;
		this.accessFlags = accessFlags;
		this.thisClass = thisClass;
		this.superClass = superClass;
		this.interfaces = interfaces;
		this.fields = fields;
		this.methods = methods;
		this.attributes = attributes;
		this.staticPart = InternalUtils.allocateStaticMemory(0, fields, pool); 
		this.instancePiecePart = InternalUtils.allocateInstanceMemory(0, fields, pool); 
	}

	public ConstantPool getConstantPool() {
		return pool;
	}
	
	public JavaClassVersion getVersion() {
		return version;
	}

	public int getAccessFlags() {
		return accessFlags;
	}

	public int getThisClass() {
		return thisClass;
	}

	public int getSuperClass() {
		return superClass;
	}

	public InterfaceItem[] getInterfaces() {
		return interfaces;
	}

	public int getClassDescSize() {
		return staticPart;
	}

	public int getInstanceDescSize() {
		return instancePiecePart;
	}
	
	public FieldItem[] getFields() {
		return fields;
	}

	public MethodItem[] getMethods() {
		return methods;
	}

	public AttributeItem[] getAttributes() {
		return attributes;
	}
	
	public Class<?> getClassInstance() {
		return null;
	}

	@Override
	public String toString() {
		final StringBuilder	sb = new StringBuilder();
		
		sb.append(Modifier.toString(accessFlags)).append(' ').append(pool.deepToString(thisClass));
		if (superClass != 0) {
			sb.append(" extends ").append(pool.deepToString(superClass));
		}
		if (interfaces.length > 0) {
			String	prefix = " implements ";
			for (InterfaceItem item : interfaces) {
				sb.append(prefix).append(pool.deepToString(item.interfaceRef));
				prefix = " , ";
			}
		}
		sb.append(" {\n");

		for (FieldItem item : fields) {
			sb.append('\t').append(Modifier.toString(item.accessFlags)).append(' ').append(pool.deepToString(item.fieldDesc))
			  .append(' ').append(pool.deepToString(item.fieldName)).append(";\n");
		}

		for (MethodItem item : methods) {
			sb.append('\t').append(Modifier.toString(item.accessFlags)).append(' ').append(pool.deepToString(item.methodName))
			  .append(pool.deepToString(item.methodDesc)).append(";\n");
		}
		return sb.append("}\n").toString();
	}

}
