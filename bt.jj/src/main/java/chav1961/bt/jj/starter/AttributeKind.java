package chav1961.bt.jj.starter;

enum AttributeKind {
	ConstantValue,
	Code,
	StackMapTable,
	Exceptions,
	InnerClasses,
	EnclosingMethod,
	Synthetic,
	Signature,
	Signatures,
	SourceFile,
	SourceDebugExtension,
	LineNumberTable,
	LocalVariableTable,
	LocalVariableTypeTable,
	Deprecated,
	RuntimeVisibleAnnotations,
	RuntimeInvisibleAnnotations,
	RuntimeVisibleParameterAnnotations,
	RuntimeInvisibleParameterAnnotations,
	RuntimeVisibleTypeAnnotations,
	RuntimeInvisibleTypeAnnotations,
	AnnotationDefault,
	BootstrapMethods,
	MethodParameters,
	Module,
	ModulePackages,
	ModuleMainClass,
	NestHost,
	NestMembers,
	Record,
	PermittedSubclasses,
	Unknown;
	
	private final char[]	nameArray = name().toCharArray();
	
	public static AttributeKind valueOf(final int index, final ConstantPoolItem[] items) {
		if (items == null) {
			throw new NullPointerException("Items can'yt be null");
		}
		else if (index < 0 || index >= items.length) {
			throw new IllegalArgumentException("Item index ["+index+"] out of range 0.."+(items.length-1));
		}
		else if (items[index].itemType != ClassDefinitionLoader.CONSTANT_Utf8) {
			throw new VerifyError("Item index ["+index+"] in the constant pool refres to invalid entyty (CONSTANT_Utf8 awaited)");
		}
		else {
			final char[]	toFind = items[index].content;
			
			for(AttributeKind item : values()) {
				if (InternalUtils.compareTo(toFind, item.nameArray) == 0) {
					return item;
				}
			}
			return Unknown;
		}
	}
}
