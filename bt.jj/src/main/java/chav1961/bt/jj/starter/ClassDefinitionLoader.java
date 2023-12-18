package chav1961.bt.jj.starter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import chav1961.purelib.cdb.JavaByteCodeConstants;
import chav1961.purelib.cdb.JavaClassVersion;
import chav1961.purelib.cdb.JavaByteCodeConstants.JavaAttributeType;

class ClassDefinitionLoader {
	static final int	MAGIC = 0xCAFEBABE;
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
	
	static final byte	CONSTANT_RESOLVED_Class = 32 + 7;
	static final byte	CONSTANT_RESOLVED_Fieldref = 32 + 9;
	static final byte	CONSTANT_RESOLVED_Methodref = 32 + 10;
	static final byte	CONSTANT_RESOLVED_InterfaceMethodref = 32 + 11;
	static final byte	CONSTANT_RESOLVED_String = 32 + 8;
	static final byte	CONSTANT_RESOLVED_Integer = 32 + 3;
	static final byte	CONSTANT_RESOLVED_Float = 32 + 4;
	static final byte	CONSTANT_RESOLVED_Long = 32 + 5;
	static final byte	CONSTANT_RESOLVED_Double = 32 + 6;
	static final byte	CONSTANT_RESOLVED_NameAndType = 32 + 12;
	static final byte	CONSTANT_RESOLVED_Utf8 = 32 + 1;
	static final byte	CONSTANT_RESOLVED_MethodHandle = 32 + 15;
	static final byte	CONSTANT_RESOLVED_MethodType = 32 + 16;
	static final byte	CONSTANT_RESOLVED_InvokeDynamic = 32 + 18;
	
	static final char[]	VALID_CLINIT = {'<', 'c', 'l', 'i', 'n', 'i', 't', '>'};
	static final char[]	VALID_INIT = {'<', 'i', 'n', 'i', 't', '>'};

	
	static final Set<String>	SUPPORTED_ATTRIBUTES = new HashSet<>();
	
	static {
		for(JavaAttributeType item : JavaByteCodeConstants.JavaAttributeType.values()) {
			SUPPORTED_ATTRIBUTES.add(item.name());
		}
	}
	
	public static ClassDescriptor parse(final ByteArrayReader rdr) {
		if (rdr == null) {
			throw new NullPointerException("Reader can't be null");
		}
		else if (rdr.readU4() != MAGIC) {
			throw new VerifyError("Verification error - illegal magic"); 
		}
		else {
			final int	minor = rdr.readU2(), major = rdr.readU2();
			final JavaClassVersion	version = new JavaClassVersion(major, minor); 
			
			if (version.compareTo(CURRENT_VERSION) > 0) {
				throw new VerifyError("Verification error - class file version ["+version+"] is too new to support"); 
			}
			else {
				final ConstantPoolItem[]	pool = new ConstantPoolItem[rdr.readU2()];
				
				for (int index = 1/* NOT 0 !!!*/, maxIndex = pool.length; index < maxIndex; index++) {
					pool[index] = readConstantPoolItem(rdr, index);
					if (pool[index].itemType == CONSTANT_Long || pool[index].itemType == CONSTANT_Double) {
						index++;
					}
				}
				
				for (int index = 1/* NOT 0 !!!*/, maxIndex = pool.length; index < maxIndex; index++) {
					verifyConstantPoolItem(index, pool);
					if (pool[index].itemType == CONSTANT_Long || pool[index].itemType == CONSTANT_Double) {
						index++;
					}
				}
				
				final int	accessFlag = rdr.readU2();
				final int	thisClass = rdr.readU2();
				final int	superClass = rdr.readU2();
	
				if (pool[thisClass].itemType != CONSTANT_Class) {
					throw new VerifyError("Illegal constant pool entry for THIS CLASS item");
				}
				else if (superClass != 0 && pool[superClass].itemType != CONSTANT_Class) {
					throw new VerifyError("Illegal constant pool entry for SUPER CLASS item");
				}
				final InterfaceItem[]	interfaces = new InterfaceItem[rdr.readU2()];
				
				for (int index = 0, maxIndex = interfaces.length; index < maxIndex; index++) {
					final int	interfaceRef = rdr.readU2();
					
					if (pool[interfaceRef].itemType != CONSTANT_Class) {
						throw new VerifyError("Illegal constant pool entry for INTERFACE item at index ["+index+"]");
					}
					else {
						interfaces[index] = new InterfaceItem(interfaceRef, pool);
					}
				}
				final FieldItem[]	fields = new FieldItem[rdr.readU2()];
				
				for (int index = 0, maxIndex = fields.length; index < maxIndex; index++) {
					fields[index] = readFieldInfoItem(pool, rdr, index);
				}
				final MethodItem[]	methods = new MethodItem[rdr.readU2()];
				
				for (int index = 0, maxIndex = methods.length; index < maxIndex; index++) {
					methods[index] = readMethodInfoItem(pool, rdr, index);
				}
				final AttributeItem[]	attrs = new AttributeItem[rdr.readU2()];
				
				for (int index = 0, maxIndex = attrs.length; index < maxIndex; index++) {
					attrs[index] = readAttributeItem(pool, rdr, thisClass, index);
				}
				return new ClassDescriptor(pool, version, accessFlag, thisClass, superClass, interfaces, fields, methods, attrs);
			}
		}
	}

	private static void verifyConstantPoolItem(final int index, final ConstantPoolItem[] pool) {
		final ConstantPoolItem	item = pool[index];
		
		switch (item.itemType) {
			case CONSTANT_Class					:
				if (!isValidReference(item.ref1, pool)) {
					throw new VerifyError("Constant pool entry for CLASS at index ["+index+"] refers to non-existent constant pool entry ["+item.ref1+"]");
				}
				else if (!isValidUTF8Reference(item.ref1, false, pool)) {
					throw new VerifyError("Constant pool entry for CLASS at index ["+index+"] refers to constant pool entry ["+item.ref1+"], which is not an UTF8 entry or contains zero length string");
				}
				else if (!isValidClassSignature(pool[item.ref1].content) && !isValidClassRefSignature(pool[item.ref1].content)) {
					throw new VerifyError("Constant pool entry for CLASS at index ["+index+"] refers to constant pool entry ["+item.ref1+"], contains invalid class signature ["+new String(pool[item.ref1].content)+"]");
				}
				break;
			case CONSTANT_Fieldref				:
				if (!isValidReference(item.ref1, pool)) {
					throw new VerifyError("Constant pool entry for FIELD REF at index ["+index+"] refers to non-existent constant pool entry ["+item.ref1+"]");
				}
				else if (pool[item.ref1].itemType != CONSTANT_Class) {
					throw new VerifyError("Constant pool entry for FIELD REF at index ["+index+"] refers to constant pool entry ["+item.ref1+"], which is not a class descriptor");
				}
				else if (!isValidReference(item.ref2, pool)) {
					throw new VerifyError("Constant pool entry for FIELD REF at index ["+index+"] refers to non-existent constant pool entry ["+item.ref2+"]");
				}
				else if (pool[item.ref2].itemType != CONSTANT_NameAndType) {
					throw new VerifyError("Constant pool entry for FIELD REF at index ["+index+"] refers to constant pool entry ["+item.ref2+"], which is not a NAME AND TYPE entry");
				}
				break;
			case CONSTANT_Methodref				:
				if (!isValidReference(item.ref1, pool)) {
					throw new VerifyError("Constant pool entry for METHOD REF at index ["+index+"] refers to non-existent constant pool entry ["+item.ref1+"]");
				}
				else if (pool[item.ref1].itemType != CONSTANT_Class) {
					throw new VerifyError("Constant pool entry for METHOD REF at index ["+index+"] refers to constant pool entry ["+item.ref1+"], which is not a CLASS entry");
				}
				else if (!isValidReference(item.ref2, pool)) {
					throw new VerifyError("Constant pool entry for METHOD REF at index ["+index+"] refers to non-existent constant pool entry ["+item.ref2+"]");
				}
				else if (pool[item.ref2].itemType != CONSTANT_NameAndType) {
					throw new VerifyError("Constant pool entry for METHOD REF at index ["+index+"] refers to constant pool entry ["+item.ref2+"], which is not a NAME AND TYPE entry");
				}
				break;
			case CONSTANT_InterfaceMethodref	:
				if (!isValidReference(item.ref1, pool)) {
					throw new VerifyError("Constant pool entry for INTERFACE METHOD REF at index ["+index+"] refers to non-existent constant pool entry ["+item.ref1+"]");
				}
				else if (pool[item.ref1].itemType != CONSTANT_Class) {
					throw new VerifyError("Constant pool entry for INTERFACE METHOD REF at index ["+index+"] refers to constant pool entry ["+item.ref1+"], which is not a CLASS entry");
				}
				else if (!isValidReference(item.ref2, pool)) {
					throw new VerifyError("Constant pool entry for INTERFACE METHOD REF at index ["+index+"] refers to non-existent constant pool entry ["+item.ref2+"]");
				}
				else if (pool[item.ref2].itemType != CONSTANT_NameAndType) {
					throw new VerifyError("Constant pool entry for INTERFACE METHOD REF at index ["+index+"] refers to constant pool entry ["+item.ref2+"], which is not a NAME AND TYPE entry");
				}
				break;
			case CONSTANT_String				:
				if (!isValidReference(item.ref1, pool)) {
					throw new VerifyError("Constant pool entry for STRING at index ["+index+"] refers to non-existent constant pool entry ["+item.ref1+"]");
				}
				else if (!isValidUTF8Reference(item.ref1, true, pool)) {
					throw new VerifyError("Constant pool entry for STRING at index ["+index+"] refers to constant pool entry ["+item.ref1+"], which is not an UTF8 entry");
				}
				break;
			case CONSTANT_NameAndType			:
				if (!isValidReference(item.ref1, pool)) {
					throw new VerifyError("Constant pool entry for NAME AND TYPE at index ["+index+"] refers to non-existent constant pool entry ["+item.ref1+"]");
				}
				else if (!isValidUTF8Reference(item.ref1, false, pool)) {
					throw new VerifyError("Constant pool entry for NAME AND TYPE at index ["+index+"] refers to constant pool entry ["+item.ref1+"] for name, which is not an UTF8 entry or contants empty string");
				}
				else if (!isValidReference(item.ref2, pool)) {
					throw new VerifyError("Constant pool entry for NAME AND TYPE at index ["+index+"] refers to non-existent constant pool entry ["+item.ref2+"]");
				}
				else if (!isValidUTF8Reference(item.ref1, false, pool)) {
					throw new VerifyError("Constant pool entry for NAME AND TYPE at index ["+index+"] refers to constant pool entry ["+item.ref1+"] for type, which is not an UTF8 entry");
				}
				else if (!isValidName(pool[item.ref1].content)) {
					throw new VerifyError("Constant pool entry for CLASS at index ["+index+"] refers to constant pool entry ["+item.ref1+"], contains invalid entity name");
				}
				else if (!isValidUTF8Reference(item.ref2, false, pool)) {
					throw new VerifyError("Constant pool entry for CLASS at index ["+index+"] refers to constant pool entry ["+item.ref1+"] for type, which is not an UTF8 entry or contants empty string");
				}
				else if (!isValidClassRefSignature(pool[item.ref2].content) && !isValidMethodSignature(pool[item.ref2].content)) {
					isValidMethodSignature(pool[item.ref2].content);
					throw new VerifyError("Constant pool entry for CLASS at index ["+index+"] refers to constant pool entry ["+item.ref1+"], contains invalid field or method signature ["+new String(pool[item.ref2].content)+"]");
				}
				break;
				
			case CONSTANT_MethodHandle			:
			case CONSTANT_MethodType			:
			case CONSTANT_Dynamic				:
			case CONSTANT_InvokeDynamic			:
			case CONSTANT_Module				:
			case CONSTANT_Package				:
				break;
			case CONSTANT_Integer				:
			case CONSTANT_Float					:
			case CONSTANT_Long					:
			case CONSTANT_Double				:
			case CONSTANT_Utf8					:
				break;
			default :
				throw new VerifyError("Unsupported constant pool entry type ["+item.itemType+"] at index ["+index+"]");
		}
	}

	private static ConstantPoolItem readConstantPoolItem(final ByteArrayReader rdr, final int index) {
		final int	itemType = rdr.read();
		
		switch (itemType) {
			case CONSTANT_Class					:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_Class, rdr.readU2(), 0, 0, null); 
			case CONSTANT_Fieldref				:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_Fieldref, rdr.readU2(), rdr.readU2(), 0, null); 
			case CONSTANT_Methodref				:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_Methodref, rdr.readU2(), rdr.readU2(), 0, null); 
			case CONSTANT_InterfaceMethodref	:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_InterfaceMethodref, rdr.readU2(), rdr.readU2(), 0, null); 
			case CONSTANT_String				:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_String, rdr.readU2(), 0, 0, null); 
			case CONSTANT_Integer				:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_Integer, 0, 0, rdr.readU4(), null); 
			case CONSTANT_Float					:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_Float, 0, 0, Float.floatToIntBits(rdr.readU4()), null); 
			case CONSTANT_Long					:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_Long, 0, 0, rdr.readU8(), null); 
			case CONSTANT_Double				:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_Double, 0, 0, Double.doubleToLongBits(rdr.readU8()), null); 
			case CONSTANT_NameAndType			:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_NameAndType, rdr.readU2(), rdr.readU2(), 0, null); 
			case CONSTANT_Utf8					:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_Utf8, 0, 0, 0, rdr.readUTF()); 
			case CONSTANT_MethodHandle			:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_MethodHandle, rdr.read(), rdr.readU2(), 0, null); 
			case CONSTANT_MethodType			:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_MethodType, rdr.readU2(), 0, 0, null); 
			case CONSTANT_Dynamic				:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_InvokeDynamic, rdr.readU2(), rdr.readU2(), 0, null); 
			case CONSTANT_InvokeDynamic			:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_InvokeDynamic, rdr.readU2(), rdr.readU2(), 0, null); 
			case CONSTANT_Module				:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_Module, rdr.readU2(), 0, 0, null); 
			case CONSTANT_Package				:
				return new ConstantPoolItem(JavaByteCodeConstants.CONSTANT_Package, rdr.readU2(), 0, 0, null); 
			default :
				throw new VerifyError("Verification error - unsupported constant pool item type ["+itemType+"] at index ["+index+"], file displ=0x"+Integer.toHexString(rdr.getFP()));
		}
	}

	private static FieldItem readFieldInfoItem(final ConstantPoolItem[] pool, final ByteArrayReader rdr, final int index) {
		final int	accessFlags	= rdr.readU2();
		final int	name = rdr.readU2();
		final int	description	= rdr.readU2();
		
		if (pool[name].itemType != CONSTANT_Utf8) {
			throw new VerifyError("Verification error - illegal constant pool entry for FIELD NAME at index ["+index+"]");
		}
		if (pool[description].itemType != CONSTANT_Utf8) {
			throw new VerifyError("Verification error - illegal constant pool entry for FIELD DESCRIPTION of field ["+resolveDescriptor(pool, name)+"]");
		}
		
		final AttributeItem[]	attrs = new AttributeItem[rdr.readU2()];
		
		for(int attrIndex = 0, maxAttrIndex = attrs.length; attrIndex < maxAttrIndex; attrIndex++) {
			attrs[attrIndex] = readAttributeItem(pool, rdr, name, attrIndex);
		}
		return new FieldItem(name, description, accessFlags, pool, attrs);
	}
	
	private static MethodItem readMethodInfoItem(final ConstantPoolItem[] pool, final ByteArrayReader rdr, final int index) {
		final int	accessFlags	= rdr.readU2();
		final int	name = rdr.readU2();
		final int	description	= rdr.readU2();
		
		if (pool[name].itemType != CONSTANT_Utf8) {
			throw new VerifyError("Verification error - illegal constant pool entry for METHOD NAME at index ["+index+"]");
		}
		if (pool[description].itemType != CONSTANT_Utf8) {
			throw new VerifyError("Verification error - illegal constant pool entry for METHOD DESCRIPTION of method ["+resolveDescriptor(pool, name)+"] at index ["+index+"]");
		}
		
		final AttributeItem[]	attrs = new AttributeItem[rdr.readU2()];
		
		for(int attrIndex = 0, maxAttrIndex = attrs.length; attrIndex < maxAttrIndex; attrIndex++) {
			attrs[attrIndex] = readAttributeItem(pool, rdr, name, attrIndex);
		}
		return new MethodItem(name, description, accessFlags, pool, attrs);
	}

	private static AttributeItem readAttributeItem(final ConstantPoolItem[] pool, final ByteArrayReader rdr, final int ownerNameIndex, final int attrIndex) {
		final int		name = rdr.readU2();
		final byte[]	content = new byte[rdr.readU4()];
		
		if (pool[name].itemType != CONSTANT_Utf8) {
			throw new VerifyError("Verification error - illegal constant pool entry for owner ["+resolveDescriptor(pool, ownerNameIndex)+"]: illegal constant pool entry for it's attribute name at index ["+attrIndex+"]");
		}
		rdr.read(content);
		return new AttributeItem(name, content, pool);
	}

	private static boolean isValidReference(final int refIndex, final ConstantPoolItem[] cp) {
		return !(refIndex <= 0 || refIndex >= cp.length || cp[refIndex] == null);
	}

	private static boolean isValidUTF8Reference(final int refIndex, final boolean zeroLengthIsValid, final ConstantPoolItem[] cp) {
		if (isValidReference(refIndex, cp)) {
			return cp[refIndex].itemType == CONSTANT_Utf8 && (zeroLengthIsValid || cp[refIndex].content.length > 0);
		}
		else {
			return false;
		}
	}
	
	private static boolean isValidName(final char[] content) {
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
	
	private static boolean isValidClassSignature(final char[] content) {
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

	private static boolean isValidClassRefSignature(final char[] content) {
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
	
	private static boolean isValidMethodSignature(final char[] content) {
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

	static String resolveDescriptor(final ConstantPoolItem[] pool, final int index) {
		switch (pool[index].itemType) {
			case CONSTANT_Class					:
				return resolveDescriptor(pool, pool[index].ref1); 
			case CONSTANT_Fieldref				:
			case CONSTANT_Methodref				:
			case CONSTANT_InterfaceMethodref	:
			case CONSTANT_MethodHandle			:
			case CONSTANT_MethodType			:
			case CONSTANT_InvokeDynamic			:
				return resolveDescriptor(pool, pool[index].ref1)+"."+resolveDescriptor(pool, pool[index].ref2); 
			case CONSTANT_String				:
				return resolveDescriptor(pool, pool[index].ref1); 
			case CONSTANT_Integer				:
			case CONSTANT_Long					:
				return String.valueOf(pool[index].value); 
			case CONSTANT_Float					:
				return String.valueOf(Float.intBitsToFloat((int)pool[index].value)); 
			case CONSTANT_Double				:
				return String.valueOf(Double.longBitsToDouble(pool[index].value)); 
			case CONSTANT_NameAndType			:
				return resolveDescriptor(pool, pool[index].ref1)+" "+resolveDescriptor(pool, pool[index].ref1); 
			case CONSTANT_Utf8					:
				return new String(pool[index].content); 
			default :
				throw new VerifyError("Verification error - unsupported constant pool item type ["+pool[index].itemType+"]");
		}
	}
	
	public static void main(final String[] args) throws IOException {
		final ClassDescriptor	def = parse(new ByteArrayReader(Files.readAllBytes(new File("c:/tmp/Layer.class").toPath()))); 
		
		System.err.println(def);
	}
}

