package chav1961.bt.jj.starter;

import java.util.Arrays;

import chav1961.purelib.cdb.JavaByteCodeConstants;

public class DefinitionLoader {
	static final int				MAGIC = 0xCAFEBABE;
	static final JavaClassVersion	CURRENT_VERSION = new JavaClassVersion(65,0);

	static final byte	CONSTANT_Utf8 = 1;
	static final byte	CONSTANT_Integer = 3;
	static final byte	CONSTANT_Float = 4;
	static final byte	CONSTANT_Long = 5;
	static final byte	CONSTANT_Double = 6;
	static final byte	CONSTANT_Class = 7;
	static final byte	CONSTANT_String = 8;
	static final byte	CONSTANT_Fieldref = 9;
	static final byte	CONSTANT_Methodref = 10;
	static final byte	CONSTANT_InterfaceMethodref = 11;
	static final byte	CONSTANT_NameAndType = 12;
	static final byte	CONSTANT_MethodHandle = 15;
	static final byte	CONSTANT_MethodType = 16;
	static final byte	CONSTANT_Dynamic = 17;
	static final byte	CONSTANT_InvokeDynamic = 18;
	static final byte	CONSTANT_Module = 19;
	static final byte	CONSTANT_Package = 20;

	static final short	ACC_PUBLIC	= 0x0001;
	static final short	ACC_PRIVATE	= 0x0002;
	static final short	ACC_PROTECTED = 0x0004;
	static final short	ACC_STATIC = 0x0008;
	static final short	ACC_FINAL = 0x0010;
	static final short	ACC_SUPER = 0x0020;
	static final short	ACC_SYNCHRONIZED = 0x0020;
	static final short	ACC_VOLATILE = 0x0040;
	static final short	ACC_BRIDGE = 0x0040;
	static final short	ACC_TRANSIENT = 0x0080;
	static final short	ACC_VARARGS = 0x0080;
	static final short	ACC_NATIVE = 0x0100;
	static final short	ACC_INTERFACE = 0x0200;
	static final short	ACC_ABSTRACT = 0x0400;
	static final short	ACC_STRICT = 0x0800;
	static final short	ACC_SYNTHETIC = 0x1000;
	static final short	ACC_ANNOTATION = 0x2000;
	static final short	ACC_ENUM = 0x4000;
	static final short	ACC_MANDATED = (short)0x8000;
	static final short	ACC_MODULE = (short)0x8000;
	
	static final char[]	VALID_CLINIT = "<clinit>".toCharArray();
	static final char[]	VALID_INIT = "<init>".toCharArray();
	
	public static ClassDescriptor parse(final ByteArrayReader rdr) {
		return parse(rdr, true, JavaAttributeProcessing.Always);
	}
	
	public static ClassDescriptor parse(final ByteArrayReader rdr, final boolean verify, final JavaAttributeProcessing processing) {
		if (rdr == null) {
			throw new NullPointerException("Reader can't be null");
		}
		else {
			final VerifyErrorManager	err = new VerifyErrorManager();
			final int					magic;
			
			if ((magic = rdr.readU2()) != MAGIC) {
				throw err.buildError(rdr.offset(), VerifyErrorManager.ERR_ILLEGAL_MAGIC, "", magic, MAGIC);
			}
			else {
				final JavaClassVersion	version = new JavaClassVersion(rdr.readU4());
				
				if (version.compareTo(CURRENT_VERSION) > 0) {
					throw err.buildError(rdr.offset(), VerifyErrorManager.ERR_VERSION_TOO_NEW, "", version, CURRENT_VERSION);
				}
				else {
					final ConstantPool	pool = loadConstantPool(rdr, verify, err);
					
					err.setConstantPool(pool);
					
					final int	accessFlags = rdr.readU2();
					
					if (verify) {
						checkClassAccessFlags(rdr.offset(), accessFlags, err);
					}
					
					final int	thisClass = rdr.readU2();

					if (verify) {
						if (!pool.isRefValid(thisClass)) {
							throw err.buildError(rdr.offset(), VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "thisClass", thisClass);
						}
						else if (!pool.hasType(thisClass, CONSTANT_Class)) {
							throw err.buildError(rdr.offset(), VerifyErrorManager.ERR_INVALID_REF_CP, "thisClass", thisClass);
						}
					}
					
					final int	superClass = rdr.readU2();
					
					if (verify && superClass != 0) {
						if (!pool.isRefValid(superClass)) {
							throw err.buildError(rdr.offset(), VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "superClass", superClass);
						}
						else if (pool.hasType(superClass, CONSTANT_Class)) {
							throw err.buildError(rdr.offset(), VerifyErrorManager.ERR_INVALID_REF_CP, "superClass", superClass);
						}
					}
					
					final InterfaceItem[]	interfaces = new InterfaceItem[rdr.readU2()];
					
					err.pushNames();
					err.setName(thisClass);
					err.pushSection("<interfaces>");
					err.pushIndices();
					for (int index = 0, maxIndex = interfaces.length; index < maxIndex; index++) {
						final int	interfaceRef = rdr.readU2();
						
						err.setIndex(index);
						interfaces[index] = new InterfaceItem(rdr.offset(), interfaceRef, pool);
						if (verify) {
							if (!pool.isRefValid(interfaceRef)) {
								throw err.buildError(rdr.offset(), VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "interface", interfaceRef);
							}
							else if (!pool.hasType(interfaceRef, CONSTANT_Class)) {
								throw err.buildError(rdr.offset(), VerifyErrorManager.ERR_INVALID_REF_CP, "interface", interfaceRef);
							}
						}
					}
					err.pop();
					err.pop();	// section

					final FieldItem[]	fields = new FieldItem[rdr.readU2()];
					
					err.pushSection("<fields>");
					err.pushIndices();
					for (int index = 0, maxIndex = interfaces.length; index < maxIndex; index++) {
						err.setIndex(index);
						fields[index] = readFieldDescriptor(rdr, pool);
						if (verify) {
							verifyFieldDescriptor(fields[index], (accessFlags & ACC_INTERFACE) != 0,  (accessFlags & ACC_ENUM) != 0, pool, err);
						}
					}
					err.pop();
					err.pop();	// section

					final MethodItem[]	methods = new MethodItem[rdr.readU2()];
					
					err.pushSection("<methods>");
					err.pushIndices();
					for (int index = 0, maxIndex = interfaces.length; index < maxIndex; index++) {
						err.setIndex(index);
						methods[index] = readMethodDescriptor(rdr, pool, err);
						if (verify) {
							verifyMethodDescriptor(methods[index], (accessFlags & ACC_INTERFACE) != 0, version, pool, err);
						}
					}
					err.pop();
					err.pop();	// section
					
					final AttributeItem[]	attrs = new AttributeItem[rdr.readU2()];
					
					err.pushSection("<attributes>");
					err.pushIndices();
					for (int index = 0, maxIndex = interfaces.length; index < maxIndex; index++) {
						err.setIndex(index);
						attrs[index] = readAttributeItem(rdr, pool);
						if (verify) {
							verifyAttributeItem(attrs[index], pool, err);
						}
					}
					err.pop();
					err.pop();	// section
					
					err.pop();	// name
				}
			}
			return null;
		}
	}

	private static void checkClassAccessFlags(final int offset, final int accessFlags, final VerifyErrorManager err) {
		if ((accessFlags & ACC_INTERFACE) != 0) {
			if ((accessFlags & ACC_ABSTRACT) == 0 || (accessFlags & (ACC_FINAL | ACC_SUPER | ACC_ENUM | ACC_MODULE)) != 0) {
				throw err.buildError(offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "class", accessFlags);
			}
		}
		else {
			if ((accessFlags & (ACC_ANNOTATION | ACC_MODULE)) != 0) {
				throw err.buildError(offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "class", accessFlags);
			}
			else if ((accessFlags & (ACC_FINAL | ACC_ABSTRACT)) == (ACC_FINAL | ACC_ABSTRACT)) {
				throw err.buildError(offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "class", accessFlags);
			}
			else if ((accessFlags & ACC_ANNOTATION) != 0) {
				throw err.buildError(offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "class", accessFlags);
			}
		}
	}

	private static ConstantPool loadConstantPool(final ByteArrayReader rdr, final boolean verify, final VerifyErrorManager err) {
		final ConstantPoolItem[]	pool = new ConstantPoolItem[rdr.readU2()];

		err.pushSection("<constantpool>");
		err.pushIndices();
		for (int index = 1/* NOT 0 !!!*/, maxIndex = pool.length; index < maxIndex; index++) {
			err.setIndex(index);
			pool[index] = readConstantPoolItem(rdr, err);
			if (pool[index].itemType == CONSTANT_Long || pool[index].itemType == CONSTANT_Double) {
				index++;
			}
		}
		err.pop();
	
		final ConstantPool	cp = new ConstantPool(pool);
		
		if (verify) {
			err.pushIndices();
			for (int index = 1/* NOT 0 !!!*/, maxIndex = pool.length; index < maxIndex; index++) {
				err.setIndex(index);
				verifyConstantPoolItem(cp, index, err);
				if (pool[index].itemType == CONSTANT_Long || pool[index].itemType == CONSTANT_Double) {
					index++;
				}
			}
			err.pop();
		}
		err.pop();	// section
		return cp;
	}
	
	private static ConstantPoolItem readConstantPoolItem(final ByteArrayReader rdr, final VerifyErrorManager err) {
		final int	itemType = rdr.read();
		final int	offset = rdr.offset();
		
		switch (itemType) {
			case CONSTANT_Class					:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_Class, rdr.readU2(), 0, 0, null); 
			case CONSTANT_Fieldref				:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_Fieldref, rdr.readU2(), rdr.readU2(), 0, null); 
			case CONSTANT_Methodref				:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_Methodref, rdr.readU2(), rdr.readU2(), 0, null); 
			case CONSTANT_InterfaceMethodref	:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_InterfaceMethodref, rdr.readU2(), rdr.readU2(), 0, null); 
			case CONSTANT_String				:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_String, rdr.readU2(), 0, 0, null); 
			case CONSTANT_Integer				:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_Integer, 0, 0, rdr.readU4(), null); 
			case CONSTANT_Float					:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_Float, 0, 0, Float.floatToIntBits(rdr.readU4()), null); 
			case CONSTANT_Long					:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_Long, 0, 0, rdr.readU8(), null); 
			case CONSTANT_Double				:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_Double, 0, 0, Double.doubleToLongBits(rdr.readU8()), null); 
			case CONSTANT_NameAndType			:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_NameAndType, rdr.readU2(), rdr.readU2(), 0, null); 
			case CONSTANT_Utf8					:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_Utf8, 0, 0, 0, rdr.readUTF()); 
			case CONSTANT_MethodHandle			:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_MethodHandle, rdr.read(), rdr.readU2(), 0, null); 
			case CONSTANT_MethodType			:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_MethodType, rdr.readU2(), 0, 0, null); 
			case CONSTANT_Dynamic				:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_InvokeDynamic, rdr.readU2(), rdr.readU2(), 0, null); 
			case CONSTANT_InvokeDynamic			:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_InvokeDynamic, rdr.readU2(), rdr.readU2(), 0, null); 
			case CONSTANT_Module				:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_Module, rdr.readU2(), 0, 0, null); 
			case CONSTANT_Package				:
				return new ConstantPoolItem(offset, JavaByteCodeConstants.CONSTANT_Package, rdr.readU2(), 0, 0, null); 
			default :
				throw err.buildError(offset, "", "");
		}
	}

	private static void verifyConstantPoolItem(final ConstantPool pool, final int index, final VerifyErrorManager err) {
		final ConstantPoolItem	item = pool.get(index);
		
		switch (item.itemType) {
			case CONSTANT_Class					:
				if (!pool.isRefValid(item.ref1)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_Class", item.ref1);
				}
				else if (!isValidUTF8Reference(item.ref1, false, pool)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_Class", item.ref1, "non-zero CONSTANT_Utf8");
				}
				else if (!isValidClassSignature(pool.get(item.ref1).content) && !isValidClassRefSignature(pool.get(item.ref1).content)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_CLASS_OR_METHOD_SIGNATURE_CP, "CONSTANT_Class", new String(pool.get(item.ref1).content)); 
				}
				break;
			case CONSTANT_Fieldref				:
				if (!pool.isRefValid(item.ref1)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_Fieldref.class", item.ref1);
				}
				else if (!pool.hasType(item.ref1, CONSTANT_Class)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_Fieldref.class", item.ref1, "non-zero CONSTANT_Utf8");
				}
				else if (!pool.isRefValid(item.ref2)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_Fieldref.nameAndType", item.ref2);
				}
				else if (!pool.hasType(item.ref2, CONSTANT_NameAndType)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_Fieldref.nameAndType", item.ref2, "CONSTANT_NameAndType");
				}
				break;
			case CONSTANT_Methodref				:
				if (!pool.isRefValid(item.ref1)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_Methodref.class", item.ref1);
				}
				else if (!pool.hasType(item.ref1, CONSTANT_Class)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_Methodref.class", item.ref1, "CONSTANT_Class");
				}
				else if (!pool.isRefValid(item.ref2)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_Methodref.nameAndType", item.ref2);
				}
				else if (!pool.hasType(item.ref2, CONSTANT_NameAndType)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_Methodref.nameAndType", item.ref2, "CONSTANT_NameAndType");
				}
				break;
			case CONSTANT_InterfaceMethodref	:
				if (!pool.isRefValid(item.ref1)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_InterfaceMethodref.class", item.ref1);
				}
				else if (!pool.hasType(item.ref1, CONSTANT_Class)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_InterfaceMethodref.class", item.ref1, "CONSTANT_Class");
				}
				else if (!pool.isRefValid(item.ref2)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_InterfaceMethodref.nameAndType", item.ref2);
				}
				else if (!pool.hasType(item.ref2, CONSTANT_NameAndType)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_InterfaceMethodref.nameAndType", item.ref2, "CONSTANT_NameAndType");
				}
				break;
			case CONSTANT_String				:
				if (!pool.isRefValid(item.ref1)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_String", item.ref1);
				}
				else if (!isValidUTF8Reference(item.ref1, true, pool)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_String", item.ref1, "CONSTANT_Utf8");
				}
				break;
			case CONSTANT_NameAndType			:
				if (!pool.isRefValid(item.ref1)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_NameAndType.name", item.ref1);
				}
				else if (!isValidUTF8Reference(item.ref1, false, pool)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_NameAndType.name", item.ref1, "non-zero CONSTANT_Utf8");
				}
				else if (!pool.isRefValid(item.ref2)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_NameAndType.type", item.ref2);
				}
				else if (!isValidUTF8Reference(item.ref2, false, pool)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_NameAndType.type", item.ref2, "non-zero CONSTANT_Utf8");
				}
				else if (!isValidName(pool.get(item.ref1).content)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_NAME_CP, "CONSTANT_NameAndType.name", pool.get(item.ref1).content); 
				}
				else if (!isValidClassRefSignature(pool.get(item.ref2).content) && !isValidMethodSignature(pool.get(item.ref2).content)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_CLASS_OR_METHOD_SIGNATURE_CP, "CONSTANT_NameAndType.type", new String(pool.get(item.ref2).content)); 
				}
				break;
			case CONSTANT_MethodHandle			:
				if (!pool.isRefValid(item.ref2)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_MethodHandle.ref", item.ref2);
				}
				else {
					switch (item.ref1) {
						case 1 : case 2 : case 3 : case 4 :
							if (!pool.hasType(item.ref2, CONSTANT_Fieldref)) {
								throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_MethodHandle.ref", item.ref2, "CONSTANT_Fieldref");
							}
							break;
						case 5 :
							if (!pool.hasType(item.ref2, CONSTANT_Methodref)) {
								throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_MethodHandle.ref", item.ref2, "CONSTANT_Methodref");
							}
							break;
						case 6 : case 7 :
							if (!pool.hasType(item.ref2, CONSTANT_Methodref, CONSTANT_InterfaceMethodref)) {
								throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_MethodHandle.ref", item.ref2, "CONSTANT_Methodref or CONSTANT_InterfaceMethodref");
							}
							else if (isInitMethodRef(pool, item.ref2) || isClInitMethodRef(pool, item.ref2)) {
								throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_MethodHandle.ref", item.ref2, "neither <clinit> nor <init>");
							}
							break;
						case 8 :
							if (!pool.hasType(item.ref2, CONSTANT_Methodref)) {
								throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_MethodHandle.ref", item.ref2, "CONSTANT_Methodref");
							}
							else if (!isInitMethodRef(pool, item.ref2)) {
								throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_MethodHandle.ref", item.ref2, "<init>");
							}
							break;
						case 9 :
							if (!pool.hasType(item.ref2, CONSTANT_InterfaceMethodref)) {
								throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_MethodHandle.ref", item.ref2, "CONSTANT_InterfaceMethodref");
							}
							else if (isInitMethodRef(pool, item.ref2) || isClInitMethodRef(pool, item.ref2)) {
								throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_MethodHandle.ref", item.ref2, "neither <clinit> nor <init>");
							}
							break;
						default :
							throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_METHOD_HANDLE_KIND_CP, "CONSTANT_MethodHandle", item.ref1);
					}
				}
				break;
			case CONSTANT_MethodType			:
				if (!pool.isRefValid(item.ref1)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_MethodType", item.ref2);
				}
				else if (!isValidUTF8Reference(item.ref1, false, pool)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_MethodType", item.ref1, "non-zero CONSTANT_Utf8");
				}
				else if (!isValidMethodSignature(pool.get(item.ref1).content)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_CLASS_OR_METHOD_SIGNATURE_CP, "CONSTANT_MethodType", new String(pool.get(item.ref1).content)); 
				}
				break;
			case CONSTANT_Dynamic				:
				if (!pool.isRefValid(item.ref2)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_Dynamic", item.ref2);
				}
				else if (!pool.hasType(item.ref2, CONSTANT_Fieldref)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_Dynamic", item.ref2, "CONSTANT_Fieldref");
				}
				break;
			case CONSTANT_InvokeDynamic			:
				if (!pool.isRefValid(item.ref2)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_InvokeDynamic", item.ref2);
				}
				else if (!pool.hasType(item.ref2, CONSTANT_Methodref)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "ICONSTANT_InvokeDynamic", item.ref2, "CONSTANT_Methodref");
				}
				break;
			case CONSTANT_Module				:
				if (!pool.isRefValid(item.ref2)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_Module", item.ref1);
				}
				else if (!isValidUTF8Reference(item.ref1, false, pool)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_Module", item.ref1, "non-zero CONSTANT_Utf8");
				}
				else if (!isValidClassSignature(pool.get(item.ref1).content)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_CLASS_OR_METHOD_SIGNATURE_CP, "CONSTANT_Module", new String(pool.get(item.ref1).content)); 
				}
				break;
			case CONSTANT_Package				:
				if (!pool.isRefValid(item.ref1)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "CONSTANT_Package", item.ref1);
				}
				else if (!isValidUTF8Reference(item.ref1, false, pool)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "CONSTANT_Package", item.ref1, "non-zero CONSTANT_Utf8");
				}
				else if (!isValidClassSignature(pool.get(item.ref1).content)) {
					throw err.buildError(item.offset, VerifyErrorManager.ERR_INVALID_CLASS_OR_METHOD_SIGNATURE_CP, "CONSTANT_Package", new String(pool.get(item.ref1).content)); 
				}
				break;
			case CONSTANT_Integer : case CONSTANT_Float : case CONSTANT_Long : case CONSTANT_Double : case CONSTANT_Utf8 :
				break;
			default :
				throw err.buildError(item.offset, VerifyErrorManager.ERR_UNSUPPORTED_CONSTANT_POOL_ITEM_TYPE, "ConstantPool", item.itemType);
		}
	}

	private static FieldItem readFieldDescriptor(final ByteArrayReader rdr, final ConstantPool pool) {
		final int	accessFlags	= rdr.readU2();
		final int	offset = rdr.offset();
		final int	name = rdr.readU2();
		final int	description	= rdr.readU2();
		final AttributeItem[]	attrs = new AttributeItem[rdr.readU2()];
		
		for(int attrIndex = 0, maxAttrIndex = attrs.length; attrIndex < maxAttrIndex; attrIndex++) {
			attrs[attrIndex] = readAttributeItem(rdr, pool);
		}
		return new FieldItem(offset, name, description, accessFlags, pool, attrs);
	}

	private static void verifyFieldDescriptor(final FieldItem fieldItem, final boolean isInterface, final boolean isEnum, final ConstantPool pool, final VerifyErrorManager err) {
		final int	accessFlags = fieldItem.accessFlags;

		if (isInterface) {
			if ((accessFlags & (ACC_PUBLIC | ACC_STATIC | ACC_FINAL)) != (ACC_PUBLIC | ACC_STATIC | ACC_FINAL)) {
				throw err.buildError(fieldItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Field.access", accessFlags);
			}
			if ((accessFlags & (ACC_PRIVATE | ACC_PROTECTED | ACC_VOLATILE | ACC_TRANSIENT | ACC_ENUM)) != 0) {
				throw err.buildError(fieldItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Field.access", accessFlags);
			}
			if ((accessFlags & ACC_ENUM) != 0) {
				throw err.buildError(fieldItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Field.access", accessFlags);
			}
		}
		else {
			if ((accessFlags & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED)) != 0) {
				if ((accessFlags & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED)) != ACC_PUBLIC && (accessFlags & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED)) != ACC_PRIVATE && (accessFlags & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED)) != ACC_PROTECTED) {
					throw err.buildError(fieldItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Field.access", accessFlags);
				}
			}
			if ((accessFlags & (ACC_FINAL | ACC_VOLATILE)) == (ACC_FINAL | ACC_VOLATILE)) {
				throw err.buildError(fieldItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Field.access", accessFlags);
			}
			if (isEnum != ((accessFlags & ACC_ENUM) == ACC_ENUM)) {
				throw err.buildError(fieldItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Field.access", accessFlags);
			}
		}
		
		if (!pool.isRefValid(fieldItem.fieldName)) {
			throw err.buildError(fieldItem.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "Field.name", fieldItem.fieldName);
		}
		else if (!isValidUTF8Reference(fieldItem.fieldName, false, pool)) {
			throw err.buildError(fieldItem.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "Field.name", fieldItem.fieldName, "CONSTANT_Utf8"); 
		}
		else if (!isValidName(pool.get(fieldItem.fieldName).content)) {
			throw err.buildError(fieldItem.offset, VerifyErrorManager.ERR_INVALID_NAME_CP, "Field.name", fieldItem.fieldName, new String(pool.get(fieldItem.fieldName).content));
		}
		
		if (!pool.isRefValid(fieldItem.fieldDesc)) {
			throw err.buildError(fieldItem.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "Field.descriptor", fieldItem.fieldDesc);
		}
		else if (!isValidUTF8Reference(fieldItem.fieldDesc, false, pool)) {
			throw err.buildError(fieldItem.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "Field.descriptor", fieldItem.fieldDesc, "CONSTANT_Utf8"); 
		}
		else if (!isValidClassRefSignature(pool.get(fieldItem.fieldDesc).content)) {
			throw err.buildError(fieldItem.offset, VerifyErrorManager.ERR_INVALID_CLASS_OR_METHOD_SIGNATURE_CP, "Field.descriptor", fieldItem.fieldDesc, new String(pool.get(fieldItem.fieldDesc).content));
		}

		// TODO Auto-generated method stub
	}
	
	private static MethodItem readMethodDescriptor(final ByteArrayReader rdr, final ConstantPool pool, final VerifyErrorManager err) {
		final int	accessFlags	= rdr.readU2();
		final int	offset = rdr.offset();
		final int	name = rdr.readU2();
		final int	description	= rdr.readU2();
		final AttributeItem[]	attrs = new AttributeItem[rdr.readU2()];
		
		for(int attrIndex = 0, maxAttrIndex = attrs.length; attrIndex < maxAttrIndex; attrIndex++) {
			attrs[attrIndex] = readAttributeItem(rdr, pool);
		}
		return new MethodItem(offset, name, description, accessFlags, pool, attrs);
	}

	private static void verifyMethodDescriptor(final MethodItem methodItem, final boolean isInterface, final JavaClassVersion version, final ConstantPool pool, final VerifyErrorManager err) {
		final int	accessFlags = methodItem.accessFlags;

		if (isInterface) {
			if ((accessFlags & (ACC_PROTECTED | ACC_FINAL | ACC_SYNCHRONIZED | ACC_NATIVE)) != 0) {
				throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Method.access", accessFlags);
			}
			if (version.major < 52 && ((accessFlags & (ACC_PUBLIC | ACC_ABSTRACT)) != (ACC_PUBLIC | ACC_ABSTRACT))) {
				throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Method.access", accessFlags);
			}
			if (version.major >= 52 && ((accessFlags & (ACC_PUBLIC | ACC_PRIVATE)) != ACC_PUBLIC && (accessFlags & (ACC_PUBLIC | ACC_PRIVATE)) != ACC_PRIVATE)) {
				throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Method.access", accessFlags);
			}
			if ((accessFlags & ACC_ABSTRACT) != 0 && (accessFlags & (ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_SYNCHRONIZED | ACC_NATIVE)) != 0) {
				throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Method.access", accessFlags);
			}
		}
		else {
			if ((accessFlags & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED)) != 0) {
				if ((accessFlags & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED)) != ACC_PUBLIC && (accessFlags & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED)) != ACC_PRIVATE && (accessFlags & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED)) != ACC_PROTECTED) {
					throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Method.access", accessFlags);
				}
			}
			if ((accessFlags & ACC_ABSTRACT) != 0 && (accessFlags & (ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_SYNCHRONIZED | ACC_NATIVE)) != 0) {
				throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Method.access", accessFlags);
			}
		}
		
		if (!pool.isRefValid(methodItem.methodName)) {
			throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "Method.name", methodItem.methodName);
		}
		else if (!isValidUTF8Reference(methodItem.methodName, false, pool)) {
			throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "Method.name", methodItem.methodName); 
		}
		else if (!isValidName(pool.get(methodItem.methodName).content)) {
			throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_INVALID_NAME_CP, "Method.name", methodItem.methodName, new String(pool.get(methodItem.methodName).content));
		}
		
		if (!pool.isRefValid(methodItem.methodDesc)) {
			throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_NON_EXISTENT_REF_CP, "Method.descriptor", methodItem.methodDesc);
		}
		else if (!isValidUTF8Reference(methodItem.methodDesc, false, pool)) {
			throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_INVALID_REF_CP, "Method.descriptor", methodItem.methodDesc); 
		}
		else if (!isValidMethodSignature(pool.get(methodItem.methodDesc).content)) {
			throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_INVALID_CLASS_OR_METHOD_SIGNATURE_CP, "Method.descriptor", methodItem.methodDesc, new String(pool.get(methodItem.methodDesc).content));
		}
		
		if (InternalUtils.compareTo(pool.get(methodItem.methodName).content, VALID_INIT) == 0) {
			if ((accessFlags & (ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_NATIVE)) != 0) {
				throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Method.access", accessFlags);
			}
			if ((version.major < 46 || version.major > 60) && (accessFlags & ACC_STRICT) != 0) {
				throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Method.access", accessFlags);
			}
		}
		else if (InternalUtils.compareTo(pool.get(methodItem.methodName).content, VALID_CLINIT) == 0) {
			if (version.major >= 52 && (accessFlags & ACC_STATIC) == 0) {
				throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Method.access", accessFlags);
			}
			if ((version.major < 46 || version.major > 60) && (accessFlags & ACC_STRICT) != 0) {
				throw err.buildError(methodItem.offset, VerifyErrorManager.ERR_INCOMPATIBLE_ACCESS_FLAGS, "Method.access", accessFlags);
			}
		}

		// TODO Auto-generated method stub
	}

	static AttributeItem readAttributeItem(final ByteArrayReader rdr, final ConstantPool pool) {
		final int	attrType = rdr.readU2();
		final int	size = rdr.readU4();
		
		final JavaAttributeType	type = toAttribute(attrType, pool);
		
		if (type == null) {
			rdr.skip(size);
			return null;
		}
		else {
			switch (type) {
				case AnnotationDefault	:
					return new AttributeItem.AnnotationDefault(rdr, pool);
				case BootstrapMethods	:
					return new AttributeItem.BootstrapMethods(rdr, pool);
				case Code				:
					return new AttributeItem.Code(rdr, pool);
				case ConstantValue		:
					return new AttributeItem.ConstantValue(rdr, pool);
				case Deprecated			:
					return new AttributeItem.Deprecated(rdr.offset(), pool);
				case EnclosingMethod	:
					return new AttributeItem.EnclosingMethod(rdr, pool);
				case Exceptions			:
					return new AttributeItem.Exceptions(rdr, pool);
				case InnerClasses		:
					return new AttributeItem.InnerClasses(rdr, pool);
				case LineNumberTable	:
					return new AttributeItem.LineNumberTable(rdr, pool);
				case LocalVariableTable	:
					return new AttributeItem.LocalVariablesTable(rdr, pool);
				case LocalVariableTypeTable	:
					return new AttributeItem.LocalVariablesTypeTable(rdr, pool);
				case MethodParameters	:
					return new AttributeItem.MethodParameters(rdr, pool);
				case Module				:
					return new AttributeItem.Module(rdr, pool);
				case ModuleMainClass	:
					return new AttributeItem.ModuleMainClass(rdr, pool);
				case ModulePackages		:
					return new AttributeItem.ModulePackages(rdr, pool);
				case NestHost			:
					return new AttributeItem.NestHost(rdr, pool);
				case NestMembers		:
					return new AttributeItem.NestMembers(rdr, pool);
				case PermittedSubclasses:
					return new AttributeItem.PermittedSubclasses(rdr, pool);
				case Record				:
					return new AttributeItem.Record(rdr, pool);
				case RuntimeInvisibleAnnotations	:
					return new AttributeItem.RuntimeInvisibleAnnotations(rdr, pool);
				case RuntimeInvisibleParameterAnnotations	:
					return new AttributeItem.RuntimeInvisibleParameterAnnotations(rdr, pool);
				case RuntimeInvisibleTypeAnnotations	:
					return new AttributeItem.RuntimeInvisibleTypeAnnotations(rdr, pool);
				case RuntimeVisibleAnnotations		:
					return new AttributeItem.RuntimeVisibleAnnotations(rdr, pool);
				case RuntimeVisibleParameterAnnotations	:
					return new AttributeItem.RuntimeVisibleParameterAnnotations(rdr, pool);
				case RuntimeVisibleTypeAnnotations	:
					return new AttributeItem.RuntimeVisibleTypeAnnotations(rdr, pool);
				case Signature			:
					return new AttributeItem.Signature(rdr, pool);
				case SourceDebugExtension	:
					return new AttributeItem.SourceDebugExtension(rdr, size, pool);
				case SourceFile			:
					return new AttributeItem.SourceFile(rdr, pool);
				case StackMapTable		:
					return new AttributeItem.StackMapTable(rdr, pool);
				case Synthetic			:
					return new AttributeItem.Synthetic(rdr.offset(), pool);
				default			:
					throw new UnsupportedOperationException("Attribute type ["+type+"] is not supported yet");
			}
		}
	}

	private static JavaAttributeType toAttribute(final int attrType, final ConstantPool pool) {
		try {
			return JavaAttributeType.valueOf(pool.deepToString(attrType));
		} catch (IllegalArgumentException exc) {
			return null;
		}
	}
	

	private static void verifyAttributeItem(final AttributeItem attributeItem, final ConstantPool pool, final VerifyErrorManager err) {
		// TODO Auto-generated method stub
		
	}

	static boolean isValidUTF8Reference(final int refIndex, final boolean zeroLengthIsValid, final ConstantPool cp) {
		if (cp.isRefValid(refIndex)) {
			return cp.get(refIndex).itemType == CONSTANT_Utf8 && (zeroLengthIsValid || cp.get(refIndex).content.length > 0);
		}
		else {
			return false;
		}
	}
	
	static boolean isValidName(final char[] content) {
		if (content.length == 0) {
			return false;
		}
		else if (!Character.isJavaIdentifierStart(content[0])) {
			if (Arrays.equals(content, VALID_CLINIT) || Arrays.equals(content, VALID_INIT)) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			for(int index = 1; index < content.length; index++) {
				if (!Character.isJavaIdentifierPart(content[index])) {
					return false;
				}
			}
			return true;
		}
	}
	
	static boolean isValidClassSignature(final char[] content) {
		int	index = 0, maxIndex = content.length;
		
		while (index < maxIndex && content[index] == '[') {
			index++;
		}
		while(index < maxIndex) {
			if (Character.isJavaIdentifierStart(content[index])) {
				while (index < maxIndex && Character.isJavaIdentifierPart(content[index])) {
					index++;
				}
				if (index < maxIndex && content[index] == '/') {
					index++;
				}
			}
			else {
				return false;
			}
		}
		return index > 0 && content[index - 1] != '/';
	}

	static boolean isValidClassRefSignature(final char[] content) {
		int	index = 0, maxIndex = content.length;
		
		while (index < maxIndex && content[index] == '[') {
			index++;
		}
		if (index < maxIndex) {
			switch (content[index]) {
				case 'B' : case 'C' : case 'D' : case 'F' : case 'I' : case 'J' : case 'S' : case 'V' : case 'Z' :
					index++;
					break;
				case 'L' :
					index++;
					while(index < maxIndex) {
						if (Character.isJavaIdentifierStart(content[index])) {
							while (index < maxIndex && Character.isJavaIdentifierPart(content[index])) {
								index++;
							}
							if (index < maxIndex && content[index] == ';') {
								index++;
								break;
							}
							if (index < maxIndex && content[index] == '/') {
								index++;
							}
						}
						else {
							return false;
						}
					}
					break;
				default :
					return false;
			}
			return index >= maxIndex;
		}
		else {
			return false;
		}
	}
	
	static boolean isValidMethodSignature(final char[] content) {
		int	index = 0, maxIndex = content.length;

		if (index < maxIndex && content[index] == '(') {
			index++;
			while (index < maxIndex && content[index] != ')') {
				while (index < maxIndex && content[index] == '[') {
					index++;
				}
				if (index < maxIndex) {
					switch (content[index]) {
						case 'B' : case 'C' : case 'D' : case 'F' : case 'I' : case 'J' : case 'S' : case 'Z' :
							index++;
							break;
						case 'L' :
							index++;
							while(index < maxIndex) {
								if (Character.isJavaIdentifierStart(content[index])) {
									while (index < maxIndex && Character.isJavaIdentifierPart(content[index])) {
										index++;
									}
									if (index < maxIndex && content[index] == ';') {
										index++;
										break;
									}
									if (index < maxIndex && content[index] == '/') {
										index++;
									}
								}
								else {
									return false;
								}
							}
							break;
						default :
							return false;
					}
				}
				else {
					return false;
				}
			}
			if (index < maxIndex && content[index] == ')') {
				index++;
				if (index < maxIndex && content[index] == 'V') {
					index++;
				}
				else {
					while (index < maxIndex && content[index] == '[') {
						index++;
					}
					if (index < maxIndex) {
						switch (content[index]) {
							case 'B' : case 'C' : case 'D' : case 'F' : case 'I' : case 'J' : case 'S' : case 'Z' :
								index++;
								break;
							case 'L' :
								index++;
								while(index < maxIndex) {
									if (Character.isJavaIdentifierStart(content[index])) {
										while (index < maxIndex && Character.isJavaIdentifierPart(content[index])) {
											index++;
										}
										if (index < maxIndex && content[index] == ';') {
											index++;
											break;
										}
										if (index < maxIndex && content[index] == '/') {
											index++;
										}
									}
									else {
										return false;
									}
								}
								break;
							default :
								return false;
						}
					}
					else {
						return false;
					}
				}
				return index >= maxIndex;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	private static boolean isClInitMethodRef(final ConstantPool pool, final int index) {
		if (!pool.hasType(index, CONSTANT_Methodref)) {
			return false;
		}
		else if (!pool.isRefValid(pool.get(index).ref1)) {
			return false;
		}
		else if (!pool.hasType(pool.get(index).ref1, CONSTANT_NameAndType)) {
			return false;
		}
		else if (!pool.isRefValid(pool.get(pool.get(index).ref1).ref1) || !isValidUTF8Reference(pool.get(pool.get(index).ref1).ref1, true, pool)) {
			return false;
		}
		else {
			return Arrays.equals(VALID_CLINIT, pool.get(pool.get(index).ref1).content);
		}
	}

	private static boolean isInitMethodRef(final ConstantPool pool, int index) {
		if (!pool.hasType(index, CONSTANT_Methodref)) {
			return false;
		}
		else if (!pool.isRefValid(pool.get(index).ref1)) {
			return false;
		}
		else if (!pool.hasType(pool.get(index).ref1, CONSTANT_NameAndType)) {
			return false;
		}
		else if (!pool.isRefValid(pool.get(pool.get(index).ref1).ref1) || !isValidUTF8Reference(pool.get(pool.get(index).ref1).ref1, true, pool)) {
			return false;
		}
		else {
			return Arrays.equals(VALID_INIT, pool.get(pool.get(index).ref1).content);
		}
	}
}
