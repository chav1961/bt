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
				case DefinitionLoader.CONSTANT_Class				:
					return deepToString(content[ref].ref1); 
				case DefinitionLoader.CONSTANT_Fieldref			:
				case DefinitionLoader.CONSTANT_Methodref			:
				case DefinitionLoader.CONSTANT_InterfaceMethodref	:
				case DefinitionLoader.CONSTANT_MethodHandle		:
				case DefinitionLoader.CONSTANT_MethodType			:
				case DefinitionLoader.CONSTANT_InvokeDynamic		:
					return deepToString(content[ref].ref1)+"."+deepToString(content[ref].ref2); 
				case DefinitionLoader.CONSTANT_String				:
					return deepToString(content[ref].ref1); 
				case DefinitionLoader.CONSTANT_Integer				:
				case DefinitionLoader.CONSTANT_Long				:
					return String.valueOf(content[ref].value); 
				case DefinitionLoader.CONSTANT_Float				:
					return String.valueOf(Float.intBitsToFloat((int)content[ref].value)); 
				case DefinitionLoader.CONSTANT_Double				:
					return String.valueOf(Double.longBitsToDouble(content[ref].value)); 
				case DefinitionLoader.CONSTANT_NameAndType			:
					return deepToString(content[ref].ref1)+" "+deepToString(content[ref].ref1); 
				case DefinitionLoader.CONSTANT_Utf8					:
					return new String(content[ref].content); 
				default :
					throw new IllegalArgumentException("illegal constain pool item type ["+content[ref].itemType+"] at index ["+ref+"]");
			}
		}		
	}
}
