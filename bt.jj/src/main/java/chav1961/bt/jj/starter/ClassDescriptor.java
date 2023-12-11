package chav1961.bt.jj.starter;

import java.lang.reflect.Modifier;

import chav1961.purelib.cdb.JavaClassVersion;

class ClassDescriptor {
	private final ConstantPoolItem[]	pool;
	private final JavaClassVersion	version;
	private final int				accessFlags;
	private final int				thisClass;
	private final int				superClass;
	private final InterfaceItem[]	interfaces;
	private final FieldItem[]		fields;
	private final MethodItem[]		methods;
	private final AttributeItem[]	attributes;
	private final int				classDescSize;
	private final int				instanceDescSize;
	
	ClassDescriptor(final ConstantPoolItem[] pool, final JavaClassVersion version, final int accessFlags, final int thisClass, final int superClass, final InterfaceItem[] interfaces, final FieldItem[] fields, final MethodItem[] methods, final AttributeItem[] attributes) {
		this.pool = pool;
		this.version = version;
		this.accessFlags = accessFlags;
		this.thisClass = thisClass;
		this.superClass = superClass;
		this.interfaces = interfaces;
		this.fields = fields;
		this.methods = methods;
		this.attributes = attributes;
		
		int staticLongCount = 0, staticIntCount = 0, staticShortCount = 0, staticByteCount = 0;
		int staticLongDispl, staticIntDispl, staticShortDispl, staticByteDispl;
		int instanceLongCount = 0, instanceIntCount = 0, instanceShortCount = 0, instanceByteCount = 0;
		int instanceLongDispl, instanceIntDispl, instanceShortDispl, instanceByteDispl;
		
		for(FieldItem item : fields) {
			switch (CodeExecutor.getArgumentSize(ClassDefinitionLoader.resolveDescriptor(pool, item.fieldDesc))) {
				case CodeExecutor.BYTE_SIZE :
					if (Modifier.isStatic(item.accessFlags)) {
						staticByteCount++;
					}
					else {
						instanceByteCount++;
					}
					break;
				case CodeExecutor.SHORT_SIZE :
					if (Modifier.isStatic(item.accessFlags)) {
						staticShortCount++;
					}
					else {
						instanceShortCount++;
					}
					break;
				case CodeExecutor.INT_SIZE :
					if (Modifier.isStatic(item.accessFlags)) {
						staticIntCount++;
					}
					else {
						instanceIntCount++;
					}
					break;
				case CodeExecutor.LONG_SIZE :
					if (Modifier.isStatic(item.accessFlags)) {
						staticLongCount++;
					}
					else {
						instanceLongCount++;
					}
					break;
				default :
					throw new UnsupportedOperationException();
			}
		}
		
		staticLongDispl = 0;
		staticIntDispl = staticLongDispl + CodeExecutor.LONG_SIZE * staticLongCount;
		staticShortDispl = staticIntDispl + CodeExecutor.INT_SIZE * staticIntCount;
		staticByteDispl = staticShortDispl + CodeExecutor.SHORT_SIZE * staticShortCount;
		instanceLongDispl = 0;
		instanceIntDispl = instanceLongDispl + CodeExecutor.LONG_SIZE * instanceLongCount;
		instanceShortDispl = instanceIntDispl + CodeExecutor.INT_SIZE * instanceIntCount;
		instanceByteDispl = instanceShortDispl + CodeExecutor.SHORT_SIZE * instanceShortCount;
		
		for(FieldItem item : fields) {
			switch (CodeExecutor.getArgumentSize(ClassDefinitionLoader.resolveDescriptor(pool, item.fieldDesc))) {
				case CodeExecutor.BYTE_SIZE :
					if (Modifier.isStatic(item.accessFlags)) {
						item.displacement = staticByteDispl;
						item.length = CodeExecutor.BYTE_SIZE; 
						staticByteDispl += CodeExecutor.BYTE_SIZE;
					}
					else {
						item.displacement = instanceByteDispl;
						item.length = CodeExecutor.BYTE_SIZE; 
						instanceByteDispl += CodeExecutor.BYTE_SIZE;
					}
					break;
				case CodeExecutor.SHORT_SIZE :
					if (Modifier.isStatic(item.accessFlags)) {
						item.displacement = staticShortDispl;
						item.length = CodeExecutor.SHORT_SIZE; 
						staticShortDispl += CodeExecutor.SHORT_SIZE;
					}
					else {
						item.displacement = instanceShortDispl;
						item.length = CodeExecutor.SHORT_SIZE; 
						instanceShortDispl += CodeExecutor.SHORT_SIZE;
					}
					break;
				case CodeExecutor.INT_SIZE :
					if (Modifier.isStatic(item.accessFlags)) {
						item.displacement = staticIntDispl;
						item.length = CodeExecutor.INT_SIZE; 
						staticIntDispl += CodeExecutor.INT_SIZE;
					}
					else {
						item.displacement = instanceIntDispl;
						item.length = CodeExecutor.INT_SIZE; 
						instanceIntDispl += CodeExecutor.INT_SIZE;
					}
					break;
				case CodeExecutor.LONG_SIZE :
					if (Modifier.isStatic(item.accessFlags)) {
						item.displacement = staticLongDispl;
						item.length = CodeExecutor.LONG_SIZE; 
						staticLongDispl += CodeExecutor.LONG_SIZE;
					}
					else {
						item.displacement = instanceLongDispl;
						item.length = CodeExecutor.LONG_SIZE; 
						instanceLongDispl += CodeExecutor.LONG_SIZE;
					}
					break;
				default :
					throw new UnsupportedOperationException();
			}
		}
		
		this.classDescSize = staticByteDispl;
		this.instanceDescSize = instanceByteDispl;
	}

	public ConstantPoolItem[] getConstantPool() {
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
		return classDescSize;
	}

	public int getInstanceDescSize() {
		return instanceDescSize;
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

	@Override
	public String toString() {
		final StringBuilder	sb = new StringBuilder();
		
		sb.append(Modifier.toString(accessFlags)).append(' ').append(ClassDefinitionLoader.resolveDescriptor(pool, thisClass));
		if (superClass != 0) {
			sb.append(" extends ").append(ClassDefinitionLoader.resolveDescriptor(pool, superClass));
		}
		if (interfaces.length > 0) {
			String	prefix = " implements ";
			for (InterfaceItem item : interfaces) {
				sb.append(prefix).append(ClassDefinitionLoader.resolveDescriptor(pool, item.interfaceRef));
				prefix = " , ";
			}
		}
		sb.append(" {\n");

		for (FieldItem item : fields) {
			sb.append('\t').append(Modifier.toString(item.accessFlags)).append(' ').append(ClassDefinitionLoader.resolveDescriptor(pool, item.fieldDesc))
			  .append(' ').append(ClassDefinitionLoader.resolveDescriptor(pool, item.fieldName)).append(";\n");
		}

		for (MethodItem item : methods) {
			sb.append('\t').append(Modifier.toString(item.accessFlags)).append(' ').append(ClassDefinitionLoader.resolveDescriptor(pool, item.methodName))
			  .append(ClassDefinitionLoader.resolveDescriptor(pool, item.methodDesc)).append(";\n");
		}
		return sb.append("}\n").toString();
	}

}
