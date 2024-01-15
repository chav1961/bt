package chav1961.bt.jj.starter;

public class ConstantPool {
	private final ConstantPoolItem[]	content;

	public ConstantPool(ConstantPoolItem[] content) {
		this.content = content;
	}
	
	public int length() {
		return content.length;
	}
	
	public boolean isRefValid(final int ref) {
		return ref >= 1 && ref < content.length && content[ref] != null; 
	}
	
	public ConstantPoolItem get(final int ref) {
		if (!isRefValid(ref)) {
			throw new IllegalArgumentException("Ref ["+ref+"] is outside constant pool size 1.."+(content.length-1)+" or points to null entry");
		}
		else {
			return content[ref];
		}
	}
	
	public boolean hasType(final int ref, final int... types) {
		if (types == null || types.length == 0) {
			throw new IllegalArgumentException("Type list can't be null or empty array"); 
		}
		else if (!isRefValid(ref)) {
			throw new IllegalArgumentException("Ref ["+ref+"] is outside constant pool size 1.."+(content.length-1)+" or points to null entry");
		}
		else {
			final int	type = content[ref].itemType;
			
			for(int item : types) {
				if (type == item) {
					return true;
				}
			}
			return false;
		}
	}
	
	public String deepToString(final int ref) {
		if (!isRefValid(ref)) {
			throw new IllegalArgumentException("Ref ["+ref+"] is outside constant pool size 1.."+(content.length-1)+" or points to null entry");
		}
		else {
			switch (content[ref].itemType) {
				case ClassDefinitionLoader.CONSTANT_Class				:
					return deepToString(content[ref].ref1); 
				case ClassDefinitionLoader.CONSTANT_Fieldref			:
				case ClassDefinitionLoader.CONSTANT_Methodref			:
				case ClassDefinitionLoader.CONSTANT_InterfaceMethodref	:
				case ClassDefinitionLoader.CONSTANT_MethodHandle		:
				case ClassDefinitionLoader.CONSTANT_MethodType			:
				case ClassDefinitionLoader.CONSTANT_InvokeDynamic		:
					return deepToString(content[ref].ref1)+"."+deepToString(content[ref].ref2); 
				case ClassDefinitionLoader.CONSTANT_String				:
					return deepToString(content[ref].ref1); 
				case ClassDefinitionLoader.CONSTANT_Integer				:
				case ClassDefinitionLoader.CONSTANT_Long				:
					return String.valueOf(content[ref].value); 
				case ClassDefinitionLoader.CONSTANT_Float				:
					return String.valueOf(Float.intBitsToFloat((int)content[ref].value)); 
				case ClassDefinitionLoader.CONSTANT_Double				:
					return String.valueOf(Double.longBitsToDouble(content[ref].value)); 
				case ClassDefinitionLoader.CONSTANT_NameAndType			:
					return deepToString(content[ref].ref1)+" "+deepToString(content[ref].ref1); 
				case ClassDefinitionLoader.CONSTANT_Utf8					:
					return new String(content[ref].content); 
				default :
					throw new IllegalArgumentException("illegal constain pool item type ["+content[ref].itemType+"] at index ["+ref+"]");
			}
		}		
	}
}
