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
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to non-existent constant pool entry ["+ref1+"]");
				}
				else if (pool[ref1].itemType != ClassDefinitionLoader.CONSTANT_Integer) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to invalid constant pool entry ["+ref1+"] (CONSTANT_Integer awaited)");
				}
				else {
					return new AnnotationValue(tag, ref1);
				}
			case 'Z':
				ref1 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref1, pool)) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to non-existent constant pool entry ["+ref1+"]");
				}
				else if (pool[ref1].itemType != ClassDefinitionLoader.CONSTANT_Integer) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to invalid constant pool entry ["+ref1+"] (CONSTANT_Integer awaited)");
				}
				else if (pool[ref1].ref1 != 0 && pool[ref1].ref1 != 1) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to invalid constant pool entry ["+ref1+"] (integer value ["+pool[ref1].ref1+"] is neither false (0) nor true (1))");
				}
				else {
					return new AnnotationValue(tag, ref1);
				}
			case 'D':
				ref1 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref1, pool)) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to non-existent constant pool entry ["+ref1+"]");
				}
				else if (pool[ref1].itemType != ClassDefinitionLoader.CONSTANT_Double) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to invalid constant pool entry ["+ref1+"] (CONSTANT_Double awaited)");
				}
				else {
					return new AnnotationValue(tag, ref1);
				}
			case 'F':
				ref1 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref1, pool)) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to non-existent constant pool entry ["+ref1+"]");
				}
				else if (pool[ref1].itemType != ClassDefinitionLoader.CONSTANT_Float) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to invalid constant pool entry ["+ref1+"] (CONSTANT_Float awaited)");
				}
				else {
					return new AnnotationValue(tag, ref1);
				}
			case 'J':
				ref1 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref1, pool)) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to non-existent constant pool entry ["+ref1+"]");
				}
				else if (pool[ref1].itemType != ClassDefinitionLoader.CONSTANT_Long) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to invalid constant pool entry ["+ref1+"] (CONSTANT_Long awaited)");
				}
				else {
					return new AnnotationValue(tag, ref1);
				}
			case 's':
				ref1 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref1, pool)) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to non-existent constant pool entry ["+ref1+"]");
				}
				else if (!ClassDefinitionLoader.isValidUTF8Reference(ref1, true, pool)) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to invalid constant pool entry ["+ref1+"] (CONSTANT_UTF8 awaited)");
				}
				else {
					return new AnnotationValue(tag, ref1);
				}
			case 'e':
				ref1 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref1, pool)) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to non-existent constant pool entry ["+ref1+"]");
				}
				else if (!ClassDefinitionLoader.isValidUTF8Reference(ref1, false, pool)) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to invalid constant pool entry ["+ref1+"] (CONSTANT_UTF8 awaited)");
				}
				else if (ClassDefinitionLoader.isValidClassSignature(pool[ref1].content)) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to invalid constant pool entry ["+ref1+"] (invalid class signature ["+new String(pool[ref1].content)+"])");
				}
				ref2 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref2, pool)) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to non-existent constant pool entry ["+ref2+"]");
				}
				else if (!ClassDefinitionLoader.isValidUTF8Reference(ref2, false, pool)) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to invalid constant pool entry ["+ref2+"] (CONSTANT_UTF8 awaited)");
				}
				else if (ClassDefinitionLoader.isValidName(pool[ref1].content)) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to invalid constant pool entry ["+ref2+"] (invalid name ["+new String(pool[ref2].content)+"])");
				}
				else {
					return new AnnotationValue(tag, ref1, ref2);
				}
			case 'c':
				ref1 = rdr.readU2();
				if (!ClassDefinitionLoader.isValidReference(ref1, pool)) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to non-existent constant pool entry ["+ref1+"]");
				}
				else if (!ClassDefinitionLoader.isValidUTF8Reference(ref1, false, pool)) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to invalid constant pool entry ["+ref1+"] (CONSTANT_UTF8 awaited)");
				}
				else if (ClassDefinitionLoader.isValidClassSignature(pool[ref1].content)) {
					throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to invalid constant pool entry ["+ref1+"] (invalid class signature ["+new String(pool[ref1].content)+"])");
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
				throw new VerifyError("Constant pool entry for "+annotationName+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] has invalid tag value ["+tag+"]");
		}
	}

	public static class ConstantValue extends AttributeItem {
		public final int		constRef;
		
		public ConstantValue(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.ConstantValue, pool);
			final int	ref = new ByteArrayReader(content).readU2(); 
			
			if (!ClassDefinitionLoader.isValidReference(ref, pool)) {
				throw new VerifyError("Constant pool entry for CONSTANTVALUE ATTRIBUTE refers to non-existent constant pool entry ["+ref+"]");
			}
			else if (pool[ref].itemType != ClassDefinitionLoader.CONSTANT_Integer && pool[ref].itemType != ClassDefinitionLoader.CONSTANT_Long 
					 && pool[ref].itemType != ClassDefinitionLoader.CONSTANT_Float && pool[ref].itemType != ClassDefinitionLoader.CONSTANT_Double) {
				throw new VerifyError("Constant pool entry for CONSTANTVALUE ATTRIBUTE refers to invalid constant pool entry ["+ref+"] (CONSTANT_Integer, CONSTANT_Long, CONSTANT_Float or CONSTANT_Double awaited)");
			}
			else {
				this.constRef = ref; 
			}
		}
	}
	
	public static class Signature extends AttributeItem {
		public final int		signatureRef;
		
		public Signature(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.Signature, pool);
			final int	ref = new ByteArrayReader(content).readU2(); 
			
			if (!ClassDefinitionLoader.isValidReference(ref, pool)) {
				throw new VerifyError("Constant pool entry for SIGNATURE ATTRIBUTE refers to non-existent constant pool entry ["+ref+"]");
			}
			else if (!ClassDefinitionLoader.isValidUTF8Reference(ref, false, pool)) {
				throw new VerifyError("Constant pool entry for SIGNATURE ATTRIBUTE refers to invalid constant pool entry ["+ref+"] (CONSTANT_UTF8 awaited)");
			}
			else {
				this.signatureRef = ref; 
			}
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
					throw new VerifyError("Constant pool entry for "+getAnnotationName()+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] has invalid target type ["+Integer.toHexString(type)+"]");
			}
			final TypePathDescriptor[]	pathDesc = new TypePathDescriptor[rdr.read()]; 
			
			for(int typeIndex = 0; typeIndex < pathDesc.length; typeIndex++) {
				pathDesc[index] = new TypePathDescriptor(rdr.read(), rdr.read()); 
			}
			final int	typeIndex = rdr.readU2();
			
			if (!ClassDefinitionLoader.isValidReference(typeIndex, pool)) {
				throw new VerifyError("Constant pool entry for "+getAnnotationName()+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to non-existent constant pool entry ["+typeIndex+"]");
			}
			else if (!ClassDefinitionLoader.isValidUTF8Reference(typeIndex, false, pool)) {
				throw new VerifyError("Constant pool entry for "+getAnnotationName()+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to invalid constant pool entry ["+typeIndex+"] (CONSTANT_UTF8 awaited)");
			}
			else if (ClassDefinitionLoader.isValidClassSignature(pool[typeIndex].content)) {
				throw new VerifyError("Constant pool entry for "+getAnnotationName()+" ATTRIBUTE at index ["+index+"/"+pairIndex+"] refers to invalid constant pool entry ["+typeIndex+"] (invalid class signature ["+new String(pool[typeIndex].content)+"])");
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
}
