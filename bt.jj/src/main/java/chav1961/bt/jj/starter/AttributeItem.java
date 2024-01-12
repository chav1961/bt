package chav1961.bt.jj.starter;

import chav1961.bt.jj.starter.AnnotationItem.AnnotationValue;
import chav1961.bt.jj.starter.AnnotationTypeItem.AnnotationTypeValue;
import chav1961.bt.jj.starter.AnnotationTypeItem.LocalVarTypeDescriptor;
import chav1961.bt.jj.starter.AnnotationTypeItem.TypePathDescriptor;

class AttributeItem {
	public final AttributeKind	name;
	final ConstantPoolItem[]	pool;
	
	AttributeItem(final AttributeKind name, final ConstantPoolItem[] pool) {
		this.name = name;
		this.pool = pool;
	}
	
	private static AnnotationValue getAnnotationValue(final ConstantPoolItem[] pool, final String annotationName, final ByteArrayReader rdr, final int index, final int pairIndex) {
		final char				tag = (char)rdr.read();
		final int				ref1, ref2;
		
		switch (tag) {
			case 'B': case 'C': case 'I': case 'S':
				ref1 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref1, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1);
				}
				else if (pool[ref1].itemType != ClassDefinitionLoader.CONSTANT_Integer) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1, "CONSTANT_Integer");
				}
				else {
					return new AnnotationValue(tag, ref1);
				}
			case 'Z':
				ref1 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref1, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1);
				}
				else if (pool[ref1].itemType != ClassDefinitionLoader.CONSTANT_Integer) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1, "CONSTANT_Integer");
				}
				else if (pool[ref1].ref1 != 0 && pool[ref1].ref1 != 1) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_BOOL_VALUE, annotationName, index, pairIndex, ref1, pool[ref1].ref1);
				}
				else {
					return new AnnotationValue(tag, ref1);
				}
			case 'D':
				ref1 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref1, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1);
				}
				else if (pool[ref1].itemType != ClassDefinitionLoader.CONSTANT_Double) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1, "CONSTANT_Double");
				}
				else {
					return new AnnotationValue(tag, ref1);
				}
			case 'F':
				ref1 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref1, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1);
				}
				else if (pool[ref1].itemType != ClassDefinitionLoader.CONSTANT_Float) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1, "CONSTANT_Float");
				}
				else {
					return new AnnotationValue(tag, ref1);
				}
			case 'J':
				ref1 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref1, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1);
				}
				else if (pool[ref1].itemType != ClassDefinitionLoader.CONSTANT_Long) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1, "CONSTANT_Long");
				}
				else {
					return new AnnotationValue(tag, ref1);
				}
			case 's':
				ref1 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref1, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1);
				}
				else if (!ClassDefinitionLoader.isValidUTF8Reference(ref1, true, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1, "CONSTANT_UTF8");
				}
				else {
					return new AnnotationValue(tag, ref1);
				}
			case 'e':
				ref1 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref1, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1);
				}
				else if (!ClassDefinitionLoader.isValidUTF8Reference(ref1, false, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1, "CONSTANT_UTF8");
				}
				else if (ClassDefinitionLoader.isValidClassSignature(pool[ref1].content)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_CLASS_SIGNATURE, annotationName, index, pairIndex, ref1, new String(pool[ref1].content));
				}
				ref2 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref2, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref2);
				}
				else if (!ClassDefinitionLoader.isValidUTF8Reference(ref2, false, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref2, "CONSTANT_UTF8");
				}
				else if (ClassDefinitionLoader.isValidName(pool[ref2].content)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_NAME, annotationName, index, pairIndex, ref2, new String(pool[ref2].content));
				}
				else {
					return new AnnotationValue(tag, ref1, ref2);
				}
			case 'c':
				ref1 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref1, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1);
				}
				else if (!ClassDefinitionLoader.isValidUTF8Reference(ref1, false, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_VALUE, annotationName, index, pairIndex, ref1, "CONSTANT_UTF8");
				}
				else if (ClassDefinitionLoader.isValidClassSignature(pool[ref1].content)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_CLASS_SIGNATURE, annotationName, index, pairIndex, ref1, new String(pool[ref1].content));
				}
				else {
					return new AnnotationValue(tag, ref1);
				}
			case '@':
				return new AnnotationValue(tag, getAnnotationValue(pool, annotationName, rdr, index, pairIndex));
			case '[':
				final AnnotationValue[]	list = new AnnotationValue[rdr.readU2()];
				
				for(int arrIndex = 0; arrIndex < list.length; arrIndex++) {
					list[arrIndex] = getAnnotationValue(pool, annotationName, rdr, index, pairIndex);
				}
				return new AnnotationValue(tag, list);
			default :
				throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_TAG, annotationName, index, pairIndex, tag);
		}
	}

	public static class ConstantValue extends AttributeItem {
		private static final int	VALID_CONSTANT_MASK = (1 << ClassDefinitionLoader.CONSTANT_Integer) | (1 << ClassDefinitionLoader.CONSTANT_Long)
														| (1 << ClassDefinitionLoader.CONSTANT_Float) | (1 << ClassDefinitionLoader.CONSTANT_Double)
														| (1 << ClassDefinitionLoader.CONSTANT_String);
		public final int			constRef;
		
		public ConstantValue(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.ConstantValue, pool);
			final int	ref = new ByteArrayReader(content).readU2(); 
			
			if (!ClassDefinitionLoader.isValidReference(ref, pool)) {
				throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_CONSTVALUE, ref); 
			}
			else if (((1 << pool[ref].itemType) & VALID_CONSTANT_MASK) == 0) {
				throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_CONSTVALUE, ref); 
			}
			else {
				this.constRef = ref; 
			}
		}
	}

	public static class Synthetic extends AttributeItem {
		public Synthetic(final ConstantPoolItem[] pool) {
			super(AttributeKind.Synthetic, pool);
		}
	}
	
	public static class Signature extends AttributeItem {
		public final int		signatureRef;
		
		public Signature(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.Signature, pool);
			final int	ref = new ByteArrayReader(content).readU2(); 
			
			if (!ClassDefinitionLoader.isValidReference(ref, pool)) {
				throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_SIGNATURE, ref); 
			}
			else if (!ClassDefinitionLoader.isValidUTF8Reference(ref, false, pool)) {
				throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_SIGNATURE, ref); 
			}
			else {
				this.signatureRef = ref; 
			}
		}
	}

	public static class SourceFile extends AttributeItem {
		public final int		sourceFileRef;
		
		public SourceFile(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.SourceFile, pool);
			final int	ref = new ByteArrayReader(content).readU2(); 
			
			if (!ClassDefinitionLoader.isValidReference(ref, pool)) {
				throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_SOURCEFILE, ref); 
			}
			else if (!ClassDefinitionLoader.isValidUTF8Reference(ref, false, pool)) {
				throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_SOURCEFILE, ref); 
			}
			else {
				this.sourceFileRef = ref; 
			}
		}
	}
	
	public static class MethodParameters extends AttributeItem {
		private static final int	AVAILABLE_ACC = ClassDefinitionLoader.ACC_FINAL | ClassDefinitionLoader.ACC_SYNTHETIC | ClassDefinitionLoader.ACC_MANDATED; 
		
		public final int[][]		parameters;
		
		public MethodParameters(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.MethodParameters, pool);
			final ByteArrayReader	bar = new ByteArrayReader(content);
			final int[][]			parameters = new int[bar.read()][];

			for (int index = 0; index < parameters.length; index++) {
				final int[]			desc = new int[] {bar.readU2(), bar.readU2()};
				
				if (!ClassDefinitionLoader.isValidReference(desc[0], pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_METHOD_PARAMETERS, desc[0]); 
				}
				else if (!ClassDefinitionLoader.isValidUTF8Reference(desc[0], false, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_METHOD_PARAMETERS, desc[0]); 
				}
				else if (!ClassDefinitionLoader.isValidName(pool[desc[0]].content)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_METHOD_PARAMETERS_NAME, desc[0], new String(pool[desc[0]].content)); 
				}
				else if ((desc[1] & ~AVAILABLE_ACC) != 0) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_EXTRA_ACCESS_FLAGS_METHOD_PARAMETERS, desc[1] & ~AVAILABLE_ACC); 
				}
				else {
					parameters[index] = desc;
				}
			}
			this.parameters = parameters;
		}
	}
	
	static abstract class RuntimeAnnotations extends AttributeItem {
		private final AnnotationItem[]	list;
		
		public RuntimeAnnotations(final AttributeKind name, final byte[] content, final ConstantPoolItem[] pool) {
			super(name, pool);
			final ByteArrayReader	rdr = new ByteArrayReader(content); 
			final int				amount = rdr.readU2();
			final AnnotationItem[]	items = new AnnotationItem[amount];
			
			for(int index = 0; index < amount; index++) {
				final int				type = rdr.readU2();
				final int				pairAmount = rdr.readU2();
				final AnnotationValue[]	pairs = new AnnotationValue[pairAmount];
				
				for (int pairIndex = 0; pairIndex < pairAmount; pairIndex++) {
					pairs[pairIndex] = getAnnotationValue(pool, getAnnotationName(), rdr, index, pairIndex);
				}
				items[index] = new AnnotationItem(type, pairs);
			}
			this.list = items;
		}

		public AnnotationItem[] getList() {
			return list;
		}

		abstract String getAnnotationName();
	}
	
	public static class RuntimeVisibleAnnotations extends RuntimeAnnotations {
		public RuntimeVisibleAnnotations(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.RuntimeVisibleAnnotations, content, pool);
		}
		
		@Override
		String getAnnotationName() {
			return "RUNTIMEVISIBLEANNOTATIONS";
		}
	}

	public static class RuntimeInvisibleAnnotations extends RuntimeAnnotations {
		public RuntimeInvisibleAnnotations(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.RuntimeInvisibleAnnotations, content, pool);
		}

		@Override
		String getAnnotationName() {
			return "RUNTIMEINVISIBLEANNOTATIONS";
		}
	}

	static abstract class RuntimeTypeAnnotations extends AttributeItem {
		private final AnnotationTypeItem[]	list;

		RuntimeTypeAnnotations(final AttributeKind name, final byte[] content, final ConstantPoolItem[] pool) {
			super(name, pool);
			final ByteArrayReader		rdr = new ByteArrayReader(content); 
			final int					amount = rdr.readU2();
			final AnnotationTypeItem[]	items = new AnnotationTypeItem[amount];
			
			for(int index = 0; index < amount; index++) {
				final int	type = rdr.readU2();
				final AnnotationTypeValue[]	pairs = new AnnotationTypeValue[rdr.readU2()];
				
				for (int pairIndex = 0; pairIndex < pairs.length; pairIndex++) {
					pairs[pairIndex] = getAnnotationTypeValue(rdr, index, pairIndex);
				}
				items[index] = new AnnotationTypeItem(type, pairs);
			}
			this.list = items;
		}

		public AnnotationTypeItem[] getList() {
			return list;
		}
		
		private AnnotationTypeValue getAnnotationTypeValue(final ByteArrayReader rdr, final int index, final int pairIndex) {
			// TODO Auto-generated method stub
			final byte	type = (byte)rdr.read();
			final int	ref1, ref2;
			final LocalVarTypeDescriptor[]	localDesc;
			
			switch (type) {
				case 0x00 : case 0x01 : case 0x10 : case 0x17 : case 0x42 : case 0x43 : case 0x44 : case 0x45 : case 0x46 :
					ref1 = rdr.readU2();
					ref2 =  -1;
					localDesc = null;
					break;
				case 0x11 : case 0x12 :
					ref1 = rdr.readU2();
					ref2 = rdr.readU2();
					localDesc = null;
					break;
				case 0x13 : case 0x14 : case 0x15 :
					ref1 =  -1;
					ref2 =  -1;
					localDesc = null;
					break;
				case 0x16 :
					ref1 = rdr.read();
					ref2 =  -1;
					localDesc = null;
					break;
				case 0x40 : case 0x41 :
					ref1 =  -1;
					ref2 =  -1;
					localDesc = new LocalVarTypeDescriptor[rdr.readU2()];
					
					for(int localIndex = 0; localIndex < localDesc.length; localIndex++) {
						localDesc[index] = new LocalVarTypeDescriptor(rdr.readU2(), rdr.readU2(), rdr.readU2());
					}
					break;
				case 0x47 : case 0x48 : case 0x49 : case 0x4A : case 0x4B :
					ref1 = rdr.readU2();
					ref2 = rdr.read();
					localDesc = null;
					break;
				default :
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_TAG, getAnnotationName(), index, pairIndex, type);
			}
			final TypePathDescriptor[]	pathDesc = new TypePathDescriptor[rdr.read()]; 
			
			for(int typeIndex = 0; typeIndex < pathDesc.length; typeIndex++) {
				pathDesc[index] = new TypePathDescriptor(rdr.read(), rdr.read()); 
			}
			final int	typeIndex = rdr.readU2();
			
			if (!ClassDefinitionLoader.isValidReference(typeIndex, pool)) {
				throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_ANNOTATION_VALUE, getAnnotationName(), index, pairIndex, typeIndex);
			}
			else if (!ClassDefinitionLoader.isValidUTF8Reference(typeIndex, false, pool)) {
				throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_VALUE, getAnnotationName(), index, pairIndex, typeIndex, "CONSTANT_UTF8");
			}
			else if (!ClassDefinitionLoader.isValidClassSignature(pool[typeIndex].content)) {
				throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ANNOTATION_CLASS_SIGNATURE, getAnnotationName(), index, pairIndex, typeIndex, new String(pool[typeIndex].content));
			}
			final AnnotationValue[]	pairs = new AnnotationValue[rdr.readU2()];
			
			for (int annoIndex = 0; annoIndex < pairs.length; annoIndex++) {
				pairs[annoIndex] = getAnnotationValue(pool, getAnnotationName(), rdr, index, pairIndex);
			}
				
			return new AnnotationTypeValue(type, ref1, ref2, localDesc, pathDesc, typeIndex, pairs);
		}

		abstract String getAnnotationName();
	}

	static class RuntimeVisibleTypeAnnotations extends RuntimeTypeAnnotations {
		public RuntimeVisibleTypeAnnotations(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.RuntimeVisibleTypeAnnotations, content, pool);
		}

		@Override
		String getAnnotationName() {
			return "RUNTIMEVISIBLETYPEANNOTATIONS";
		}
	}

	static class RuntimeInvisibleTypeAnnotations extends RuntimeTypeAnnotations {
		public RuntimeInvisibleTypeAnnotations(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.RuntimeInvisibleTypeAnnotations, content, pool);
		}

		@Override
		String getAnnotationName() {
			return "RUNTIMEINVISIBLETYPEANNOTATIONS";
		}
	}
	
	public static class InnerClasses extends AttributeItem {
		private static final int	AVAILABLE_ACC = ClassDefinitionLoader.ACC_PUBLIC | ClassDefinitionLoader.ACC_PRIVATE | 
													ClassDefinitionLoader.ACC_PROTECTED | ClassDefinitionLoader.ACC_STATIC | 
													ClassDefinitionLoader.ACC_FINAL | ClassDefinitionLoader.ACC_INTERFACE | 
													ClassDefinitionLoader.ACC_ABSTRACT | ClassDefinitionLoader.ACC_SYNTHETIC | 
													ClassDefinitionLoader.ACC_ANNOTATION | ClassDefinitionLoader.ACC_ENUM;
		
		public final int[][]		classes;

		InnerClasses(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.InnerClasses, pool);
			final ByteArrayReader		rdr = new ByteArrayReader(content); 
			final int					amount = rdr.readU2();
			
			this.classes = new int[amount][];
			
			for(int index = 0; index < amount; index++) {
				final int 	inner = rdr.readU2(), outer = rdr.readU2();
				final int	innerName = rdr.readU2(), accessFlags = rdr.readU2();
				
				if (!ClassDefinitionLoader.isValidReference(inner, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_INNER_CLASSES, inner); 
				}
				else if (pool[inner].itemType != ClassDefinitionLoader.CONSTANT_Class) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_INNER_CLASSES, inner, "CONSTANT_Class"); 
				}
				else if (outer != 0 && !ClassDefinitionLoader.isValidReference(outer, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_INNER_CLASSES, outer); 
				}
				else if (outer != 0 && pool[outer].itemType != ClassDefinitionLoader.CONSTANT_Class) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_INNER_CLASSES, outer, "CONSTANT_Class"); 
				}
				else if (innerName != 0 && !ClassDefinitionLoader.isValidReference(innerName, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_INNER_CLASSES, innerName); 
				}
				else if (innerName != 0 && !ClassDefinitionLoader.isValidName(pool[innerName].content)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_NAME_REF_INNER_CLASSES, innerName); 
				}
				else if ((accessFlags & ~AVAILABLE_ACC) != 0) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_EXTRA_ACCESS_FLAGS_INNER_CLASSES, accessFlags & ~AVAILABLE_ACC); 
				}
				classes[index] = new int[] {inner, outer, innerName, accessFlags};
			}
		}
	}	

	public static class EnclosingMethod extends AttributeItem {
		public final int	clazz;
		public final int	method;

		EnclosingMethod(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.EnclosingMethod, pool);
			final ByteArrayReader		rdr = new ByteArrayReader(content);
			
			this.clazz = rdr.readU2();
			this.method = rdr.readU2();
			if (!ClassDefinitionLoader.isValidReference(clazz, pool)) {
				throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_INNER_CLASSES, clazz); 
			}
			else if (pool[clazz].itemType != ClassDefinitionLoader.CONSTANT_Class) {
				throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ENCLOSING_METHOD, clazz, "CONSTANT_Class"); 
			}
			else if (method != 0 && !ClassDefinitionLoader.isValidReference(method, pool)) {
				throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_INNER_CLASSES, method); 
			}
			else if (method != 0 && pool[method].itemType != ClassDefinitionLoader.CONSTANT_NameAndType) {
				throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_ENCLOSING_METHOD, method, "CONSTANT_NameAndType"); 
			}
		}
	}	
}
