package chav1961.bt.jj.starter;

class ConstantPoolItem extends AbstractConstantPoolItem {
	public final int	ref1;
	public final int	ref2;
	public final long	value;
	public final char[]	content;
	final int 			offset; 
	
	public ConstantPoolItem(final int offset, final int itemType, final int ref1, final int ref2, final long value, final char[] content) {
		super(itemType);
		this.offset = offset;
		this.ref1 = ref1;
		this.ref2 = ref2;
		this.value = value;
		this.content = content;
	}

	@Override
	public String toString() {
		switch (itemType) {
			case DefinitionLoader.CONSTANT_Class				:
				return "CONSTANT_Class ["+ref1+"]"; 
			case DefinitionLoader.CONSTANT_Fieldref			:
				return "CONSTANT_Fieldref ["+ref1+","+ref2+"]"; 
			case DefinitionLoader.CONSTANT_Methodref			:
				return "CONSTANT_Methodref ["+ref1+","+ref2+"]"; 
			case DefinitionLoader.CONSTANT_InterfaceMethodref	:
				return "CONSTANT_InterfaceMethodref ["+ref1+","+ref2+"]"; 
			case DefinitionLoader.CONSTANT_String				:
				return "CONSTANT_String ["+ref1+"]"; 
			case DefinitionLoader.CONSTANT_Integer				:
				return "CONSTANT_Integer ["+value+"]"; 
			case DefinitionLoader.CONSTANT_Float				:
				return "CONSTANT_Float ["+Float.intBitsToFloat((int)value) +"]"; 
			case DefinitionLoader.CONSTANT_Long				:
				return "CONSTANT_Long ["+value+"]"; 
			case DefinitionLoader.CONSTANT_Double				:
				return "CONSTANT_Double ["+Double.longBitsToDouble(value) +"]"; 
			case DefinitionLoader.CONSTANT_NameAndType			:
				return "CONSTANT_NameAndType ["+ref1+","+ref2+"]"; 
			case DefinitionLoader.CONSTANT_Utf8				:
				return "CONSTANT_Utf8 ["+new String(content)+"]"; 
			case DefinitionLoader.CONSTANT_MethodHandle		:
				return "CONSTANT_MethodHandle ["+ref1+","+ref2+"]"; 
			case DefinitionLoader.CONSTANT_MethodType			:
				return "CONSTANT_MethodType ["+ref1+","+ref2+"]"; 
			case DefinitionLoader.CONSTANT_InvokeDynamic		:
				return "CONSTANT_InvokeDynamic ["+ref1+","+ref2+"]"; 
			default :
				return super.toString();
		}
	}
}
