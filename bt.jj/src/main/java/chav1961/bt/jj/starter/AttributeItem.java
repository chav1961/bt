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

	public static class Code extends AttributeItem {
		public final int				stackSize;	
		public final int				localSize;
		public final byte[]				code;
		public final ExceptionTable[]	exceptions;
		public final AttributeItem[]	attributes;
		
		Code(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.Code, pool);
			final ByteArrayReader	rdr = new ByteArrayReader(content);
			
			this.stackSize = rdr.readU2();
			this.localSize = rdr.readU2();
			
			this.code = new byte[rdr.readU4()];
			rdr.read(code);
			
			this.exceptions = new ExceptionTable[rdr.readU2()];
			for(int index = 0; index < exceptions.length; index++) {
				final int	startPC = rdr.readU2();
				final int	endPC = rdr.readU2();
				final int	handlerPC = rdr.readU2();
				final int	catchType = rdr.readU2();
				
				if (startPC >= code.length) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_START_PC_EXCEPTIONS, index, startPC, code.length); 
				}
				else if (endPC > code.length) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_END_PC_EXCEPTIONS, index, endPC, code.length); 
				}
				else if (handlerPC > code.length) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_HANDLER_PC_EXCEPTIONS, index, handlerPC, code.length); 
				}
				else if (endPC < startPC) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_END_PC_LESS_START_PC_EXCEPTIONS, index, endPC, startPC); 
				}
				else if (catchType != 0) {
					if (!ClassDefinitionLoader.isValidReference(catchType, pool)) {
						throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_CATCH_EXCEPTIONS, index, catchType); 
					}
					else if (pool[catchType].itemType != ClassDefinitionLoader.CONSTANT_Class) {
						throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_CATCH_EXCEPTIONS, index, catchType); 
					}
				}
				
				exceptions[index] = new ExceptionTable(startPC, endPC, handlerPC, catchType);
			}
			
			this.attributes = new AttributeItem[rdr.readU2()];
			for(int index = 0; index < exceptions.length; index++) {
				// TODO:
			}					
		}
		
		public static class ExceptionTable {
			public final int	startPC;
			public final int	endPC;
			public final int	handlerPC;
			public final int	catchType;
			
			public ExceptionTable(final int startPC, final int endPC, final int handlerPC, final int catchType) {
				this.startPC = startPC;
				this.endPC = endPC;
				this.handlerPC = handlerPC;
				this.catchType = catchType;
			}
		}
	}	
	
	public static class StackMapTable extends AttributeItem {
		public final StackMapItem[]	items;

		StackMapTable(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.StackMapTable, pool);
			final ByteArrayReader	rdr = new ByteArrayReader(content);
			
			this.items = new StackMapItem[rdr.readU2()];
			
			for(int index = 0; index < items.length; index++) {
				final int	frameType = rdr.read();
				
				switch (frameType) {
					case 0 : case 1 : case 2 : case 3 : case 4 : case 5 : case 6 : case 7 : case 8 : case 9 : case 10 : case 11 : case 12 : case 13 : case 14 : case 15 :
					case 16 : case 17 : case 18 : case 19 : case 20 : case 21 : case 22 : case 23 : case 24 : case 25 : case 26 : case 27 : case 28 : case 29 : case 30 : case 31 :
					case 32 : case 33 : case 34 : case 35 : case 36 : case 37 : case 38 : case 39 : case 40 : case 41 : case 42 : case 43 : case 44 : case 45 : case 46 : case 47 :
					case 48 : case 49 : case 50 : case 51 : case 52 : case 53 : case 54 : case 55 : case 56 : case 57 : case 58 : case 59 : case 60 : case 61 : case 62 : case 63 :
						items[index] = new SameStackMapItem(frameType); 
						break;
					case 64 : case 65 : case 66 : case 67 : case 68 : case 69 : case 70 : case 71 : case 72 : case 73 : case 74 : case 75 : case 76 : case 77 : case 78 : case 79 :
					case 80 : case 81 : case 82 : case 83 : case 84 : case 85 : case 86 : case 87 : case 88 : case 89 : case 90 : case 91 : case 92 : case 93 : case 94 : case 95 :
					case 96 : case 97 : case 98 : case 99 : case 100 : case 101 : case 102 : case 103 : case 104 : case 105 : case 106 : case 107 : case 108 : case 109 : case 110 : case 111 :
					case 112 : case 113 : case 114 : case 115 : case 116 : case 117 : case 118 : case 119 : case 120 : case 121 : case 122 : case 123 : case 124 : case 125 : case 126 : case 127 :
						items[index] = new VarStackMapItem(frameType, readVerificationItem(rdr));
						break;
					case 247:
						items[index] = new VarStackMapItemX(frameType, rdr.readU2(), readVerificationItem(rdr));
						break;
					case 248 : case 249 : case 250 :
						items[index] = new ChopStackMapItem(frameType, rdr.readU2());
						break;
					case 251 :
						items[index] = new ExtendedStackMapItem(frameType, rdr.readU2());
						break;
					case 252 : 
						items[index] = new ExtendedStackMapItem(frameType, rdr.readU2(), readVerificationItem(rdr));
						break;
					case 253 : 
						items[index] = new ExtendedStackMapItem(frameType, rdr.readU2(), readVerificationItem(rdr), readVerificationItem(rdr));
						break;
					case 254 :
						items[index] = new ExtendedStackMapItem(frameType, rdr.readU2(), readVerificationItem(rdr), readVerificationItem(rdr), readVerificationItem(rdr));
						break;
					case 255 :
						final int	offset = rdr.readU2();
						final int	locals = rdr.readU2();
						final VerificationItem[]	localsArray = new VerificationItem[locals];
						
						for(int item =  0; item < locals; item++) {
							localsArray[item] = readVerificationItem(rdr);
						}
						final int	stack = rdr.readU2();
						final VerificationItem[]	stackArray = new VerificationItem[stack];
						
						for(int item =  0; item < stack; item++) {
							stackArray[item] = readVerificationItem(rdr);
						}
						items[index] = new FullStackMapItem(frameType, offset, localsArray, stackArray);
						break;
					default :
						if (frameType >= 128 && frameType <= 246) {
							throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_UNSUPPORTED_TAG_STACKMAPTABLE, index, frameType); 
						}
						else {
							throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_TAG_STACKMAPTABLE, index, frameType); 
						}
				}
			}
		}

		private VerificationItem readVerificationItem(final ByteArrayReader rdr) {
			return null;
		}
		
		public static abstract class StackMapItem {
			public final int	itemType;

			StackMapItem(final int itemType) {
				this.itemType = itemType;
			}
		}
		
		public static class SameStackMapItem extends StackMapItem {
			SameStackMapItem(final int itemType) {
				super(itemType);
			}
		}

		public static class ChopStackMapItem extends StackMapItem {
			public final int	offset;
			
			ChopStackMapItem(final int itemType, final int offset) {
				super(itemType);
				this.offset = offset;
			}
		}

		public static class ExtendedStackMapItem extends StackMapItem {
			public final int				offset;
			public final VerificationItem[]	items;
			
			ExtendedStackMapItem(final int itemType, final int offset, final VerificationItem... items) {
				super(itemType);
				this.offset = offset;
				this.items = items;
			}
		}

		public static class FullStackMapItem extends StackMapItem {
			public final int				offset;
			public final VerificationItem[]	locals;
			public final VerificationItem[]	stack;
			
			FullStackMapItem(final int itemType, final int offset, final VerificationItem[] locals, final VerificationItem[] stack) {
				super(itemType);
				this.offset = offset;
				this.locals = locals;
				this.stack = stack;
			}
		}
		
		public static class VarStackMapItem extends StackMapItem {
			public final VerificationItem	item;
			
			VarStackMapItem(final int itemType, final VerificationItem item) {
				super(itemType);
				this.item = item;
			}
		}

		public static class VarStackMapItemX extends StackMapItem {
			public final int offset;
			public final VerificationItem	item;
			
			VarStackMapItemX(final int itemType, final int offset, final VerificationItem item) {
				super(itemType);
				this.offset = offset;
				this.item = item;
			}
		}
		
		public static abstract class VerificationItem {
			public final int	verificationType;

			VerificationItem(int verificationType) {
				this.verificationType = verificationType;
			}
		}
		
	}

	public static class Exceptions extends AttributeItem {
		public final int[]	exceptions;
		
		public Exceptions(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.Exceptions, pool);
			final ByteArrayReader	rdr = new ByteArrayReader(content);
			
			this.exceptions = new int[rdr.readU2()];
			
			for(int index = 0; index < exceptions.length; index++) {
				final int	exception = rdr.readU2();
				
				if (!ClassDefinitionLoader.isValidReference(exception, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_EXCEPTIONS, exception); 
				}
				else if (pool[exception].itemType != ClassDefinitionLoader.CONSTANT_Class) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_EXCEPTIONS, index, exception); 
				}
				else {
					exceptions[index] = exception;
				}
			}
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

	public static class SourceDebugExtension extends AttributeItem {
		public final byte[]	content; 

		SourceDebugExtension(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.SourceDebugExtension, pool);
			this.content = content.clone();
		}
	}	

	public static class LineNumberTable extends AttributeItem {
		public final int[][]	pairs;

		public LineNumberTable(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.LineNumberTable, pool);
			final ByteArrayReader		rdr = new ByteArrayReader(content);
			
			this.pairs = new int[rdr.readU2()][]; 
			for(int index = 0; index < pairs.length; index++) {
				pairs[index] = new int[] {rdr.readU2(), rdr.readU2()};
			}
		}
	}

	public static class LocalVariablesTable extends AttributeItem {
		public final int[][]	pairs;

		public LocalVariablesTable(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.LocalVariableTable, pool);
			final ByteArrayReader		rdr = new ByteArrayReader(content);
			
			this.pairs = new int[rdr.readU2()][]; 
			for(int index = 0; index < pairs.length; index++) {
				final int	varName, varType;
				
				pairs[index] = new int[] {rdr.readU2(), rdr.readU2(), varName = rdr.readU2(), varType = rdr.readU2(), rdr.readU2()};
				if (!ClassDefinitionLoader.isValidReference(varName, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_LOCAL_VARIABLE, varName); 
				}
				else if (!ClassDefinitionLoader.isValidUTF8Reference(varName, false, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_LOCAL_VARIABLE, varName, "CONSTANT_Utf8"); 
				}
				else if (!ClassDefinitionLoader.isValidReference(varType, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_LOCAL_VARIABLE, varType); 
				}
				else if (pool[varType].itemType != ClassDefinitionLoader.CONSTANT_Fieldref) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_LOCAL_VARIABLE, varName, "CONSTANT_Fieldref"); 
				}
			}
		}
	}

	public static class LocalVariablesTypeTable extends AttributeItem {
		public final int[][]	pairs;

		public LocalVariablesTypeTable(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.LocalVariableTypeTable, pool);
			final ByteArrayReader		rdr = new ByteArrayReader(content);
			
			this.pairs = new int[rdr.readU2()][]; 
			for(int index = 0; index < pairs.length; index++) {
				final int	varName, varType;
				
				pairs[index] = new int[] {rdr.readU2(), rdr.readU2(), varName = rdr.readU2(), varType = rdr.readU2(), rdr.readU2()};
				
				if (!ClassDefinitionLoader.isValidReference(varName, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_LOCAL_VARIABLE_TYPE, varName); 
				}
				else if (!ClassDefinitionLoader.isValidUTF8Reference(varName, false, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_LOCAL_VARIABLE_TYPE, varName); 
				}
				else if (!ClassDefinitionLoader.isValidReference(varType, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_LOCAL_VARIABLE_TYPE, varType); 
				}
				else if (!ClassDefinitionLoader.isValidUTF8Reference(varType, false, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_LOCAL_VARIABLE_TYPE, varName); 
				}
				else if (!ClassDefinitionLoader.isValidClassSignature(pool[varType].content)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_SIGNATURE_LOCAL_VARIABLE_TYPE, varName, new String(pool[varType].content)); 
				}
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

	public static class AnnotationDefault extends AttributeItem {
		public final AnnotationValue	value;

		AnnotationDefault(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.AnnotationDefault, pool);
			this.value = getAnnotationValue(pool, "", new ByteArrayReader(content), 0, 0);
		}
	}

	public static class BootstrapMethods extends AttributeItem {
		public final BootstrapMethod[]	methods;

		BootstrapMethods(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.BootstrapMethods, pool);
			final ByteArrayReader		rdr = new ByteArrayReader(content); 

			this.methods = new BootstrapMethod[rdr.readU2()];
			
			for(int index = 0; index < methods.length; index++) {
				final int 	ref = rdr.readU2();
				
				if (!ClassDefinitionLoader.isValidReference(ref, pool)) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_REF_BOOTSTRAP_METHODS, index, ref);
				}
				else if (pool[ref].itemType != ClassDefinitionLoader.CONSTANT_MethodHandle) {
					throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_INVALID_REF_BOOTSTRAP_METHODS, index, ref);
				}

				final int[]	args = new int[rdr.readU2()];
				
				for(int argIndex = 0; argIndex < args.length; argIndex++) {
					args[index] = rdr.readU2();
					
					if (!ClassDefinitionLoader.isValidReference(args[index], pool)) {
						throw ClassDefinitionLoader.buildError(ClassDefinitionLoader.ERR_NON_EXISTENT_ARG_REF_BOOTSTRAP_METHODS, index, argIndex, args[index]);
					}
 				}
				methods[index] = new BootstrapMethod(ref, args);
			}
		}
		
		public static class BootstrapMethod {
			public final int	methodRef;
			public final int[]	arguments;
			
			public BootstrapMethod(final int methodRef, final int... arguments) {
				this.methodRef = methodRef;
				this.arguments = arguments;
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
	

	public static class Module extends AttributeItem {
		public final int	name;
		public final int	accessFlags;
		public final int	version;
		
		public final Requires[]	requires;
		public final Exports[]	exports;
		public final Opens[]	opens;
		public final int[]		uses;
		public final Provides[]	provides;
		

		Module(final byte[] content, final ConstantPoolItem[] pool) {
			super(AttributeKind.Module, pool);
			final ByteArrayReader	rdr = new ByteArrayReader(content);
			
			this.name = rdr.readU2();
			this.accessFlags = rdr.readU2();
			this.version = rdr.readU2();
			
			this.requires = new Requires[rdr.readU2()];
			for(int index = 0; index < requires.length; index++) {
				final int	name = rdr.readU2(), accessFlags = rdr.readU2(), version = rdr.readU2();
				
				requires[index] = new Requires(name, accessFlags, version);
			}
			
			this.exports = new Exports[rdr.readU2()];
			for(int index = 0; index < exports.length; index++) {
				final int	name = rdr.readU2(), accessFlags = rdr.readU2(), list[] = new int[rdr.readU2()];

				for(int listIndex = 0; listIndex < list.length; listIndex++) {
					list[listIndex] = rdr.readU2();
				}
				exports[index] = new Exports(name, accessFlags, list);
			}
			
			this.opens = new Opens[rdr.readU2()];
			for(int index = 0; index < opens.length; index++) {
				final int	name = rdr.readU2(), accessFlags = rdr.readU2(), list[] = new int[rdr.readU2()];
				
				for(int listIndex = 0; listIndex < list.length; listIndex++) {
					list[listIndex] = rdr.readU2();
				}
				opens[index] = new Opens(name, accessFlags, list);
			}
			
			this.uses = new int[rdr.readU2()];
			for(int index = 0; index < uses.length; index++) {
				uses[index] = rdr.readU2();
			}
			
			this.provides = new Provides[rdr.readU2()];
			for(int index = 0; index < provides.length; index++) {
				final int	name = rdr.readU2(), list[] = new int[rdr.readU2()];

				for(int listIndex = 0; listIndex < list.length; listIndex++) {
					list[listIndex] = rdr.readU2();
				}
				provides[index] = new Provides(name, list);
			}
		}

		public static class Requires {
			public final int	name;
			public final int	accessFlags;
			public final int	version;
			
			public Requires(final int name, final int accessFlags, final int version) {
				this.name = name;
				this.accessFlags = accessFlags;
				this.version = version;
			}
		}
		
		public static class Exports {
			public final int	name;
			public final int	accessFlags;
			public final int[]	exports;
			
			public Exports(final int name, final int accessFlags, final int... exports) {
				this.name = name;
				this.accessFlags = accessFlags;
				this.exports = exports;
			}
		}

		public static class Opens {
			public final int	name;
			public final int	accessFlags;
			public final int[]	opens;
			
			public Opens(final int name, final int accessFlags, final int... opens) {
				this.name = name;
				this.accessFlags = accessFlags;
				this.opens = opens;
			}
		}

		public static class Provides {
			public final int	name;
			public final int[]	provides;
			
			public Provides(final int name, final int... provides) {
				this.name = name;
				this.provides = provides;
			}
		}
	}
}
