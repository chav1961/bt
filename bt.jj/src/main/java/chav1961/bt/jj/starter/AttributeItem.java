package chav1961.bt.jj.starter;

import chav1961.purelib.cdb.JavaByteCodeConstants;

class AttributeItem {
	public JavaByteCodeConstants.JavaAttributeType	type;
	public final int		name;
	public final byte[]		content;
	private final ConstantPoolItem[]	pool;
	
	public AttributeItem(final int name, final byte[] content, final ConstantPoolItem[] pool) {
		this.name = name;
		this.content = content;
		this.pool = pool;
		final String	humanReadableName = ClassDefinitionLoader.resolveDescriptor(pool, name);
		
		if (ClassDefinitionLoader.SUPPORTED_ATTRIBUTES.contains(humanReadableName)) {
			this.type = JavaByteCodeConstants.JavaAttributeType.valueOf(humanReadableName); 
		}
		else {
			this.type = null;
		}
	}

	@Override
	public String toString() {
		return "AttributeItem [name=" + ClassDefinitionLoader.resolveDescriptor(pool, name) + ", type=" + type + "]";
	}
}
