package chav1961.bt.jj.starter;

public enum TypeKind {
	TYPE_BYTE(true, false, false, ClassDefinitionLoader.CONSTANT_Integer, "CONSTANT_Integer"),
	TYPE_CHAR(true, false, false, ClassDefinitionLoader.CONSTANT_Integer, "CONSTANT_Integer"),
	TYPE_DOUBLE(true, false, false, ClassDefinitionLoader.CONSTANT_Double, "CONSTANT_Double"),
	TYPE_FLOAT(true, false, false, ClassDefinitionLoader.CONSTANT_Float, "CONSTANT_Float"),
	TYPE_INT(true, false, false, ClassDefinitionLoader.CONSTANT_Integer, "CONSTANT_Integer"),
	TYPE_LONG(true, false, false, ClassDefinitionLoader.CONSTANT_Long, "CONSTANT_Long"),
	TYPE_SHORT(true, false, false, ClassDefinitionLoader.CONSTANT_Integer, "CONSTANT_Integer"),
	TYPE_BOOLEAN(true, false, false, ClassDefinitionLoader.CONSTANT_Integer, "CONSTANT_Integer"),
	TYPE_VOID(false, false, false, (byte)-1, ""),
	TYPE_STRING(false, true, false, ClassDefinitionLoader.CONSTANT_String, "CONSTANT_String"),
	TYPE_ARRAY_P(false, true, true, (byte)-1, ""),
	TYPE_ARRAY_R(false, true, true, (byte)-1, ""),
	TYPE_REF(false, true, false, (byte)-1, "");
	
	private final boolean 	isPrimitive;
	private final boolean 	isReference;
	private final boolean	isArray;
	private final byte 		constantType;
	private final String	constantName;
	
	private TypeKind(final boolean isPrimitive, final boolean isReference, final boolean isArray, final byte constantType, final String constantName) {
		this.isPrimitive = isPrimitive;
		this.isReference = isReference;
		this.isArray = isArray;
		this.constantType = constantType;
		this.constantName = constantName;
	}

	public boolean isPrimitive() {
		return isPrimitive;
	}

	public boolean isReference() {
		return isReference;
	}

	public boolean isArray() {
		return isArray;
	}

	public byte getConstantType() {
		return constantType;
	}

	public String getConstantName() {
		return constantName;
	}
}
