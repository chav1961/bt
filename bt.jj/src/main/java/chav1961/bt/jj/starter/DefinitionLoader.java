package chav1961.bt.jj.starter;

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
	
	public static void parse(final ByteArrayReader rdr, final JavaAttributeProcessing processing) {
		if (rdr == null) {
			throw new NullPointerException("Reader can't be null");
		}
		else {
			final VerifyErrorManager	err = new VerifyErrorManager();
			
			if (rdr.readU2() != MAGIC) {
				throw err.buildError(rdr.offset(), "", "magic");
			}
			else {
				final JavaClassVersion	version = new JavaClassVersion(rdr.readU4());
				
				if (version.compareTo(CURRENT_VERSION) > 0) {
					throw err.buildError(rdr.offset(), "", "version");
				}
				else {
					final ConstantPoolItem[]	pool = loadConstantPool(rdr, err);
					
					err.setConstantPool(pool);

				}
				
			}
		}
	}

	private static ConstantPoolItem[] loadConstantPool(final ByteArrayReader rdr, final VerifyErrorManager err) {
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
		
		err.pushIndices();
		for (int index = 1/* NOT 0 !!!*/, maxIndex = pool.length; index < maxIndex; index++) {
			err.setIndex(index);
			verifyConstantPoolItem(pool, index, err);
			if (pool[index].itemType == CONSTANT_Long || pool[index].itemType == CONSTANT_Double) {
				index++;
			}
		}
		err.pop();
		err.pop();	// section
		return pool;
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

	private static void verifyConstantPoolItem(final ConstantPoolItem[] pool, final int index, final VerifyErrorManager err) {
		final ConstantPoolItem	item = pool[index];
		
		switch (item.itemType) {
			case CONSTANT_Class					:
				if (!isValidReference(item.ref1, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "CLASS", index, item.ref1);
				}
				else if (!isValidUTF8Reference(item.ref1, false, pool)) {
					throw buildError(ERR_INVALID_REF_CPE, "CLASS", index, item.ref1, "non-zero CONSTANT_Utf8");
				}
				else if (!isValidClassSignature(pool[item.ref1].content) && !isValidClassRefSignature(pool[item.ref1].content)) {
					throw buildError(ERR_INVALID_CLASS_SIGNATURE_CPE, "CLASS", index, item.ref1, new String(pool[item.ref1].content)); 
				}
				break;
			case CONSTANT_Fieldref				:
				if (!isValidReference(item.ref1, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "FIELD REF", index, item.ref1);
				}
				else if (pool[item.ref1].itemType != CONSTANT_Class) {
					throw buildError(ERR_INVALID_REF_CPE, "FIELD REF", index, item.ref1, "non-zero CONSTANT_Utf8");
				}
				else if (!isValidReference(item.ref2, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "FIELD REF", index, item.ref2);
				}
				else if (pool[item.ref2].itemType != CONSTANT_NameAndType) {
					throw buildError(ERR_INVALID_REF_CPE, "FIELD REF", index, item.ref2, "CONSTANT_NameAndType");
				}
				break;
			case CONSTANT_Methodref				:
				if (!isValidReference(item.ref1, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "METHOD REF", index, item.ref1);
				}
				else if (pool[item.ref1].itemType != CONSTANT_Class) {
					throw buildError(ERR_INVALID_REF_CPE, "METHOD REF", index, item.ref1, "CONSTANT_Class");
				}
				else if (!isValidReference(item.ref2, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "METHOD REF", index, item.ref2);
				}
				else if (pool[item.ref2].itemType != CONSTANT_NameAndType) {
					throw buildError(ERR_INVALID_REF_CPE, "METHOD REF", index, item.ref2, "CONSTANT_NameAndType");
				}
				break;
			case CONSTANT_InterfaceMethodref	:
				if (!isValidReference(item.ref1, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "INTERFACE METHOD REF", index, item.ref1);
				}
				else if (pool[item.ref1].itemType != CONSTANT_Class) {
					throw buildError(ERR_INVALID_REF_CPE, "INTERFACE METHOD REF", index, item.ref1, "CONSTANT_Class");
				}
				else if (!isValidReference(item.ref2, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "INTERFACE METHOD REF", index, item.ref2);
				}
				else if (pool[item.ref2].itemType != CONSTANT_NameAndType) {
					throw buildError(ERR_INVALID_REF_CPE, "INTERFACE METHOD REF", index, item.ref2, "CONSTANT_NameAndType");
				}
				break;
			case CONSTANT_String				:
				if (!isValidReference(item.ref1, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "STRING", index, item.ref1);
				}
				else if (!isValidUTF8Reference(item.ref1, true, pool)) {
					throw buildError(ERR_INVALID_REF_CPE, "CLASS", index, item.ref1, "CONSTANT_Utf8");
				}
				break;
			case CONSTANT_NameAndType			:
				if (!isValidReference(item.ref1, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "NAME AND TYPE", index, item.ref1);
				}
				else if (!isValidUTF8Reference(item.ref1, false, pool)) {
					throw buildError(ERR_INVALID_REF_CPE, "CLASS", index, item.ref1, "non-zero CONSTANT_Utf8");
				}
				else if (!isValidReference(item.ref2, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "NAME AND TYPE", index, item.ref2);
				}
				else if (!isValidUTF8Reference(item.ref2, false, pool)) {
					throw buildError(ERR_INVALID_REF_CPE, "NAME AND TYPE", index, item.ref2, "non-zero CONSTANT_Utf8");
				}
				else if (!isValidName(pool[item.ref1].content)) {
					throw buildError(ERR_INVALID_NAME_CPE, "NAME AND TYPE", index, item.ref1, new String(pool[item.ref1].content)); 
				}
				else if (!isValidClassRefSignature(pool[item.ref2].content) && !isValidMethodSignature(pool[item.ref2].content)) {
					throw buildError(ERR_INVALID_CLASS_SIGNATURE_CPE, "NAME AND TYPE", index, item.ref2, new String(pool[item.ref2].content)); 
				}
				break;
			case CONSTANT_MethodHandle			:
				if (!isValidReference(item.ref2, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "METHOD HANDLE", index, item.ref2);
				}
				else {
					switch (item.ref1) {
						case 1 : case 2 : case 3 : case 4 :
							if (pool[item.ref2].itemType != CONSTANT_Fieldref) {
								throw buildError(ERR_INVALID_REF_CPE, "METHOD HANDLE", index, item.ref2, "CONSTANT_Fieldref");
							}
							break;
						case 5 :
							if (pool[item.ref2].itemType != CONSTANT_Methodref) {
								throw buildError(ERR_INVALID_REF_CPE, "METHOD HANDLE", index, item.ref2, "CONSTANT_Methodref");
							}
							break;
						case 6 : case 7 :
							if (pool[item.ref2].itemType != CONSTANT_Methodref && pool[item.ref2].itemType != CONSTANT_InterfaceMethodref) {
								throw buildError(ERR_INVALID_REF_CPE, "METHOD HANDLE", index, item.ref2, "CONSTANT_Methodref or CONSTANT_InterfaceMethodref");
							}
							else if (isInitMethodRef(pool, item.ref2) || isClInitMethodRef(pool, item.ref2)) {
								throw buildError(ERR_INVALID_REF_CPE, "METHOD HANDLE", index, item.ref2, "neither <clinit> nor <init>");
							}
							break;
						case 8 :
							if (pool[item.ref2].itemType != CONSTANT_Methodref) {
								throw buildError(ERR_INVALID_REF_CPE, "METHOD HANDLE", index, item.ref2, "CONSTANT_Methodref");
							}
							else if (!isInitMethodRef(pool, item.ref2)) {
								throw buildError(ERR_INVALID_REF_CPE, "METHOD HANDLE", index, item.ref2, "<init>");
							}
							break;
						case 9 :
							if (pool[item.ref2].itemType != CONSTANT_InterfaceMethodref) {
								throw buildError(ERR_INVALID_REF_CPE, "METHOD HANDLE", index, item.ref2, "CONSTANT_InterfaceMethodref");
							}
							else if (isInitMethodRef(pool, item.ref2) || isClInitMethodRef(pool, item.ref2)) {
								throw buildError(ERR_INVALID_REF_CPE, "METHOD HANDLE", index, item.ref2, "neither <clinit> nor <init>");
							}
							break;
						default :
							throw buildError(ERR_INVALID_METHOD_HANDLE_KIND_CPE, index, item.ref1);
					}
				}
				break;
			case CONSTANT_MethodType			:
				if (!isValidReference(item.ref1, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "METHOD TYPE", index, item.ref2);
				}
				else if (!isValidUTF8Reference(item.ref1, false, pool)) {
					throw buildError(ERR_INVALID_REF_CPE, "METHOD TYPE", index, item.ref1, "non-zero CONSTANT_Utf8");
				}
				else if (!isValidMethodSignature(pool[item.ref1].content)) {
					throw buildError(ERR_INVALID_METHOD_SIGNATURE_CPE, "METHOD TYPE", index, item.ref1, new String(pool[item.ref1].content)); 
				}
				break;
			case CONSTANT_Dynamic				:
				if (!isValidReference(item.ref2, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "DYNAMIC", index, item.ref2);
				}
				else if (pool[item.ref2].itemType != CONSTANT_Fieldref) {
					throw buildError(ERR_INVALID_REF_CPE, "DYNAMIC", index, item.ref2, "CONSTANT_Fieldref");
				}
				break;
			case CONSTANT_InvokeDynamic			:
				if (!isValidReference(item.ref2, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "INVOKE DYNAMIC", index, item.ref2);
				}
				else if (pool[item.ref2].itemType != CONSTANT_Methodref) {
					throw buildError(ERR_INVALID_REF_CPE, "INVOKE DYNAMIC", index, item.ref2, "CONSTANT_Methodref");
				}
				break;
			case CONSTANT_Module				:
				if (!isValidReference(item.ref1, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "MODULE", index, item.ref1);
				}
				else if (!isValidUTF8Reference(item.ref1, false, pool)) {
					throw buildError(ERR_INVALID_REF_CPE, "MODULE", index, item.ref1, "non-zero CONSTANT_Utf8");
				}
				else if (!isValidClassSignature(pool[item.ref1].content)) {
					throw buildError(ERR_INVALID_CLASS_SIGNATURE_CPE, "MODULE", index, item.ref1, new String(pool[item.ref1].content)); 
				}
				break;
			case CONSTANT_Package				:
				if (!isValidReference(item.ref1, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_CPE, "PACKAGE", index, item.ref1);
				}
				else if (!isValidUTF8Reference(item.ref1, false, pool)) {
					throw buildError(ERR_INVALID_REF_CPE, "PACKAGE", index, item.ref1, "non-zero CONSTANT_Utf8");
				}
				else if (!isValidClassSignature(pool[item.ref1].content)) {
					throw buildError(ERR_INVALID_CLASS_SIGNATURE_CPE, "PACKAGE", index, item.ref1, new String(pool[item.ref1].content)); 
				}
				break;
			case CONSTANT_Integer : case CONSTANT_Float : case CONSTANT_Long : case CONSTANT_Double : case CONSTANT_Utf8 :
				break;
			default :
				throw buildError(ERR_UNSUPPORTED_CONSTANT_POOL_ITEM_TYPE, item.itemType, index, 0);
		}
	}
}
