package chav1961.bt.jj.starter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import chav1961.purelib.cdb.JavaByteCodeConstants;
import chav1961.purelib.cdb.JavaClassVersion;
import chav1961.purelib.cdb.JavaByteCodeConstants.JavaAttributeType;

class ClassDefinitionLoader {
	static final int	MAGIC = 0xCAFEBABE;
	static final JavaClassVersion	CURRENT_VERSION = new JavaClassVersion(65,0);
	
	static final short	ACC_PUBLIC	= 0x0001;
	static final short	ACC_PRIVATE	= 0x0002;
	static final short	ACC_PROTECTED = 0x0004;
	static final short	ACC_STATIC = 0x0008;
	static final short	ACC_FINAL = 0x0010;
	static final short	ACC_VOLATILE = 0x0040;
	static final short	ACC_TRANSIENT = 0x0080;
	static final short	ACC_SYNTHETIC = 0x1000;
	static final short	ACC_ENUM = 0x4000;	

	static final short	ACC_FIELD = ACC_PUBLIC|ACC_PRIVATE|ACC_PROTECTED|ACC_STATIC|ACC_FINAL|ACC_VOLATILE|ACC_TRANSIENT|ACC_SYNTHETIC|ACC_ENUM;	
	
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
	
	static final char[]	VALID_CLINIT = {'<', 'c', 'l', 'i', 'n', 'i', 't', '>'};
	static final char[]	VALID_INIT = {'<', 'i', 'n', 'i', 't', '>'};
	
	static final Set<String>	SUPPORTED_ATTRIBUTES = new HashSet<>();

	private static final String	ERR_ILLEGAL_MAGIC = "Illegal MAGIC";
	private static final String	ERR_VERSION_TOO_NEW = "Class file version [%1$s] is too new to support, max supported is [%2$s]";
	private static final String	ERR_NON_EXISTENT_REF_THIS_CLASS = "THIS CLASS item referes to non-existent constant pool entry [%1$d]";
	private static final String	ERR_INVALID_REF_THIS_CLASS = "Illegal constant pool entry [%1$d] for THIS CLASS item (CONSTANT_Class awaited)"; 
	private static final String	ERR_NON_EXISTENT_REF_SUPER_CLASS = "SUPER CLASS item referes to non-existent constant pool entry [%1$d]";
	private static final String	ERR_INVALID_REF_SUPER_CLASS = "Illegal constant pool entry [%1$d] for SUPER CLASS item (CONSTANT_Class awaited)"; 
	private static final String	ERR_NON_EXISTENT_REF_INTERFACE = "INTERFACE item at index [%1$d] referes to non-existent constant pool entry [%2$d]";
	private static final String	ERR_INVALID_REF_INTERFACE = "Illegal constant pool entry [%1$d] for INTERFACE item at index [%2$d] (CONSTANT_Class awaited)"; 
	private static final String	ERR_UNSUPPORTED_CONSTANT_POOL_ITEM_TYPE = "Unsupported constant pool item type [%1$d] at index [%2$d], byte code displacement=[%3$x]";	
	private static final String	ERR_NON_EXISTENT_REF_FIELD = "FIELD item at index [%1$d] refers to non-existent constant pool entry [%2$d]";
	private static final String	ERR_INVALID_REF_FIELD = "Illegal constant pool entry [%1$d] for FIELD item at index [%2$d] (CONSTANT_Utf8 awaited)"; 
	private static final String	ERR_INVALID_NAME_FIELD = "Illegal constant pool entry [%1$d] for FIELD item at index [%2$d] (invalid field name '%3$s')"; 
	private static final String	ERR_INVALID_SIGNATURE_FIELD = "Illegal constant pool entry [%1$d] for FIELD item at index [%2$d] (invalid field signature '%3$s')"; 
	private static final String	ERR_NON_EXISTENT_REF_METHOD = "METHOD item at index [%1$d] refers to non-existent constant pool entry [%2$d]";
	private static final String	ERR_INVALID_REF_METHOD = "Illegal constant pool entry [%1$d] for METHOD item at index [%2$d] (CONSTANT_Utf8 awaited)"; 
	private static final String	ERR_INVALID_NAME_METHOD = "Illegal constant pool entry [%1$d] for METHOD item at index [%2$d] (invalid method name '%3$s')"; 
	private static final String	ERR_INVALID_SIGNATURE_METHOD = "Illegal constant pool entry [%1$d] for METHOD item at index [%2$d] (invalid method signature '%3$s')"; 
	private static final String	ERR_NON_EXISTENT_REF_ATTRIBUTE = "ATTRIBUTE [%1$d/%2$d] refers to non-existent constant pool entry [%3$d]";
	private static final String	ERR_INVALID_REF_ATTRIBUTE = "Illegal constant pool entry [%1$d] for ATTRIBUTE item [%2$d/%3$d] (CONSTANT_Utf8 awaited)"; 
	private static final String	ERR_NON_EXISTENT_REF_CPE = "Constant pool entry for [%1$s] at [%2$d] refers to non-existent constant pool entry [%3$d]";
	private static final String	ERR_INVALID_REF_CPE = "Illegal constant pool entry for [%1$s] at index [%2$d], reference index = [%3$d] ([%4$s] awaited)"; 
	private static final String	ERR_INVALID_NAME_CPE = "Constant pool entry for [%1$s] at index [%2$d] refers to constant pool entry [%3$d], contains invalid entity name [%4$s]"; 
	private static final String	ERR_INVALID_CLASS_SIGNATURE_CPE = "Constant pool entry for [%1$s] at index [%2$d] refers to constant pool entry [%3$d], contains invalid class signature [%4$s]"; 
	private static final String	ERR_INVALID_METHOD_HANDLE_KIND_CPE = "Constant pool entry for METHOD HANDLE at index [%1$d] contains reference kind [%2$d] out of range 1..9"; 
	private static final String	ERR_INVALID_METHOD_SIGNATURE_CPE = "Constant pool entry for [%1$s] at index [%2$d] refers to constant pool entry [%3$d] contains invalid method signature [%4$s]"; 
	private static final String	ERR_EXTRA_ACCESS_FLAGS = "Entity [%1$s] contains extra access flags [%2$x]"; 
	private static final String	ERR_DUPLICATE_ATTRIBUTE = "Entity [%1$s] with name [%2$s] contains duplicate attribute [%3$s]"; 
	private static final String	ERR_NOT_APPLICABLE_ATTRIBUTE = "Entity [%1$s] with name [%2$s] contains unapplicable attribute [%3$s]"; 
	private static final String	ERR_INVALIF_REF_FIELD_ATTR = "Entity [%1$s] with name [%2$s] and signature [%3$s] referenced to invalid constant pool entry [%4$d] ([%5$s] awaited)"; 
	private static final String	ERR_INVALID_CLASS_SIGNATURE_ENTITY = "Entity [%1$s] with name [%2$s] refers to constant pool entry [%3$d], contains invalid class signature [%4$s]"; 

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
			throw buildError(ERR_ILLEGAL_MAGIC); 
		}
		else {
			final int	minor = rdr.readU2(), major = rdr.readU2();
			final JavaClassVersion	version = new JavaClassVersion(major, minor); 
			
			if (version.compareTo(CURRENT_VERSION) > 0) {
				throw buildError(ERR_VERSION_TOO_NEW, version, CURRENT_VERSION); 
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
	
				if (!isValidReference(thisClass, pool)) {
					throw buildError(ERR_NON_EXISTENT_REF_THIS_CLASS, thisClass);
				}
				else if (pool[thisClass].itemType != CONSTANT_Class) {
					throw buildError(ERR_INVALID_REF_THIS_CLASS, thisClass);
				}
				if (superClass != 0) {
					if (!isValidReference(superClass, pool)) {
						throw buildError(ERR_NON_EXISTENT_REF_SUPER_CLASS, superClass);
					}
					else if (pool[superClass].itemType != CONSTANT_Class) {
						throw buildError(ERR_INVALID_REF_SUPER_CLASS, superClass);
					}
				}
				
				final InterfaceItem[]	interfaces = new InterfaceItem[rdr.readU2()];
				
				for (int index = 0, maxIndex = interfaces.length; index < maxIndex; index++) {
					final int	interfaceRef = rdr.readU2();
					
					if (!isValidReference(interfaceRef, pool)) {
						throw buildError(ERR_NON_EXISTENT_REF_INTERFACE, index, interfaceRef);
					}
					else if (pool[interfaceRef].itemType != CONSTANT_Class) {
						throw buildError(ERR_INVALID_REF_INTERFACE, interfaceRef, index);
					}
					else {
						interfaces[index] = new InterfaceItem(interfaceRef, pool);
					}
				}
				final FieldItem[]	fields = new FieldItem[rdr.readU2()];
				
				for (int index = 0, maxIndex = fields.length; index < maxIndex; index++) {
					fields[index] = readFieldInfoItem(pool, rdr, index);
					verifyFieldItem(fields[index], pool);
				}
				final MethodItem[]	methods = new MethodItem[rdr.readU2()];
				
				for (int index = 0, maxIndex = methods.length; index < maxIndex; index++) {
					methods[index] = readMethodInfoItem(pool, rdr, index);
					verifyMethodItem(methods[index], pool);
				}
				final AttributeItem[]	attrs = new AttributeItem[rdr.readU2()];
				
				for (int index = 0, maxIndex = attrs.length; index < maxIndex; index++) {
					attrs[index] = readAttributeItem(pool, rdr, thisClass, index);
					verifyClassAttributes(attrs[index], pool);
				}
				return new ClassDescriptor(pool, version, accessFlag, thisClass, superClass, interfaces, fields, methods, attrs);
			}
		}
	}

	static boolean isValidReference(final int refIndex, final ConstantPoolItem[] cp) {
		return !(refIndex <= 0 || refIndex >= cp.length || cp[refIndex] == null);
	}

	static boolean isValidUTF8Reference(final int refIndex, final boolean zeroLengthIsValid, final ConstantPoolItem[] cp) {
		if (isValidReference(refIndex, cp)) {
			return cp[refIndex].itemType == CONSTANT_Utf8 && (zeroLengthIsValid || cp[refIndex].content.length > 0);
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
				throw buildError(ERR_UNSUPPORTED_CONSTANT_POOL_ITEM_TYPE, itemType, index, rdr.getFP());
		}
	}

	private static FieldItem readFieldInfoItem(final ConstantPoolItem[] pool, final ByteArrayReader rdr, final int index) {
		final int	accessFlags	= rdr.readU2();
		final int	name = rdr.readU2();
		final int	description	= rdr.readU2();
		
		if (!isValidReference(name, pool)) {
			throw buildError(ERR_NON_EXISTENT_REF_FIELD, index, name);
		}
		else if (!isValidUTF8Reference(name, false, pool)) {
			throw buildError(ERR_INVALID_REF_FIELD, name, index); 
		}
		else if (!isValidName(pool[name].content)) {
			throw buildError(ERR_INVALID_NAME_FIELD, name, index, new String(pool[name].content));
		}
		
		if (!isValidReference(description, pool)) {
			throw buildError(ERR_NON_EXISTENT_REF_FIELD, index, description);
		}
		else if (!isValidUTF8Reference(description, false, pool)) {
			throw buildError(ERR_INVALID_REF_FIELD, description, index); 
		}
		else if (!isValidClassRefSignature(pool[description].content)) {
			throw buildError(ERR_INVALID_SIGNATURE_FIELD, name, index, new String(pool[name].content));
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
		
		if (!isValidReference(name, pool)) {
			throw buildError(ERR_NON_EXISTENT_REF_METHOD, index, name);
		}
		else if (!isValidUTF8Reference(name, false, pool)) {
			throw buildError(ERR_INVALID_REF_METHOD, name, index); 
		}
		else if (!isValidName(pool[name].content)) {
			throw buildError(ERR_INVALID_NAME_METHOD, name, index, new String(pool[name].content));
		}
		
		if (!isValidReference(description, pool)) {
			throw buildError(ERR_NON_EXISTENT_REF_METHOD, index, description);
		}
		else if (!isValidUTF8Reference(description, false, pool)) {
			throw buildError(ERR_INVALID_REF_METHOD, description, index); 
		}
		else if (!isValidMethodSignature(pool[description].content)) {
			throw buildError(ERR_INVALID_SIGNATURE_METHOD, description, index, new String(pool[description].content));
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
		
		if (!isValidReference(name, pool)) {
			throw buildError(ERR_NON_EXISTENT_REF_ATTRIBUTE, ownerNameIndex, attrIndex, name);
		}
		else if (!isValidUTF8Reference(name, false, pool)) {
			throw buildError(ERR_INVALID_REF_ATTRIBUTE, name, ownerNameIndex, attrIndex);
		}
		else {
			final AttributeKind	kind = AttributeKind.valueOf(name, pool); 
			
			rdr.read(content);
			switch (kind) {
				case ConstantValue 	:
					return new AttributeItem.ConstantValue(content, pool);
				case Signature		:
					return new AttributeItem.Signature(content, pool);
				case Deprecated		:
					return new AttributeItem(kind, pool);
				case RuntimeVisibleAnnotations	:
					return new AttributeItem.RuntimeVisibleAnnotations(content, pool);
				case RuntimeInvisibleAnnotations	:
					return new AttributeItem.RuntimeInvisibleAnnotations(content, pool);
				case RuntimeVisibleTypeAnnotations	:
					return new AttributeItem.RuntimeVisibleTypeAnnotations(content, pool);
				case RuntimeInvisibleTypeAnnotations	:
					return new AttributeItem.RuntimeInvisibleTypeAnnotations(content, pool);
				default :
					return new AttributeItem(kind, pool);
			}
		}
	}

	private static void verifyConstantPoolItem(final int index, final ConstantPoolItem[] pool) {
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

	private static void verifyFieldItem(final FieldItem field, final ConstantPoolItem[] pool) {
		if ((field.accessFlags & ~ACC_FIELD) != 0) {
			throw buildError(ERR_EXTRA_ACCESS_FLAGS, InternalUtils.resolveDescriptor(pool, field.fieldName), field.accessFlags & ~ACC_FIELD); 
		}
		else {
			final char[]	fieldName = pool[field.fieldName].content;
			final EnumSet<AttributeKind>	detected = EnumSet.noneOf(AttributeKind.class);
			
			for (AttributeItem item : field.attrs) {
				if (item.name != AttributeKind.Unknown && detected.contains(item.name)) {
					throw buildError(ERR_DUPLICATE_ATTRIBUTE, "FIELD", new String(fieldName), item.name); 
				}
				else {
					detected.add(item.name);
					switch (item.name) {
						case ConstantValue	:
							final TypeKind	tk = InternalUtils.typeBySignature(pool[field.fieldDesc].content); 
							
							switch (tk) {
								case TYPE_BYTE : case TYPE_CHAR : case TYPE_SHORT : case TYPE_INT : case TYPE_DOUBLE : case TYPE_FLOAT : case TYPE_LONG : case TYPE_STRING :
									if (pool[((AttributeItem.ConstantValue)item).constRef].itemType != tk.getConstantType()) {
										throw buildError(ERR_INVALIF_REF_FIELD_ATTR, "FIELD.ConstantValue", new String(fieldName), new String(pool[field.fieldDesc].content), ((AttributeItem.ConstantValue)item).constRef, tk.getConstantName()); 
									}
									break;
								case TYPE_BOOLEAN	:
									if (pool[((AttributeItem.ConstantValue)item).constRef].itemType != CONSTANT_Integer) {
										throw buildError(ERR_INVALIF_REF_FIELD_ATTR, "FIELD.ConstantValue", new String(fieldName), new String(pool[field.fieldDesc].content), ((AttributeItem.ConstantValue)item).constRef, tk.getConstantName()); 
									}
									else if (pool[((AttributeItem.ConstantValue)item).constRef].ref1 != 0 && pool[((AttributeItem.ConstantValue)item).constRef].ref1 != 1) {
										throw buildError(ERR_INVALIF_REF_FIELD_ATTR, "FIELD.ConstantValue", new String(fieldName), new String(pool[field.fieldDesc].content), ((AttributeItem.ConstantValue)item).constRef, tk.getConstantName()+" with 0 (false) or 1 (true) "); 
									}
									break;
								default :
									throw buildError(ERR_NOT_APPLICABLE_ATTRIBUTE, "FIELD", new String(fieldName), item.name);
							}
							break;
						case Signature	:
							if (!ClassDefinitionLoader.isValidClassSignature(pool[((AttributeItem.Signature)item).signatureRef].content)) {
								throw buildError(ERR_INVALID_CLASS_SIGNATURE_ENTITY, "FIELD", new String(fieldName), ((AttributeItem.Signature)item).signatureRef, new String(pool[((AttributeItem.Signature)item).signatureRef].content));
							}
							break;
						case Deprecated	:
							break;
						case RuntimeVisibleAnnotations	:
							break;
						case RuntimeInvisibleAnnotations	:
							break;
						case RuntimeVisibleTypeAnnotations	:
							break;
						case RuntimeInvisibleTypeAnnotations	:
							break;
						case Unknown	:
							break;
						default :
							throw buildError(ERR_NOT_APPLICABLE_ATTRIBUTE, "FIELD", new String(fieldName), item.name);
					}
				}
			}
		}
	}

	private static void verifyMethodItem(final MethodItem methodItem, final ConstantPoolItem[] pool) {
		// TODO Auto-generated method stub
		
	}

	private static void verifyClassAttributes(final AttributeItem attribute, final ConstantPoolItem[] pool) {
		// TODO Auto-generated method stub
		
	}
	
	private static boolean isClInitMethodRef(final ConstantPoolItem[] pool, final int index) {
		if (pool[index].itemType != CONSTANT_Methodref) {
			return false;
		}
		else if (!isValidReference(pool[index].ref1, pool)) {
			return false;
		}
		else if (pool[pool[index].ref1].itemType != CONSTANT_NameAndType) {
			return false;
		}
		else if (!isValidReference(pool[pool[index].ref1].ref1, pool) || !isValidUTF8Reference(pool[pool[index].ref1].ref1, true, pool)) {
			return false;
		}
		else {
			return Arrays.equals(VALID_CLINIT, pool[pool[pool[index].ref1].ref1].content);
		}
	}

	private static boolean isInitMethodRef(final ConstantPoolItem[] pool, final int index) {
		if (pool[index].itemType != CONSTANT_Methodref) {
			return false;
		}
		else if (!isValidReference(pool[index].ref1, pool)) {
			return false;
		}
		else if (pool[pool[index].ref1].itemType != CONSTANT_NameAndType) {
			return false;
		}
		else if (!isValidReference(pool[pool[index].ref1].ref1, pool) || !isValidUTF8Reference(pool[pool[index].ref1].ref1, true, pool)) {
			return false;
		}
		else {
			return Arrays.equals(VALID_INIT, pool[pool[pool[index].ref1].ref1].content);
		}
	}

	static VerifyError buildError(final String format, final Object... parameters) {
		return new VerifyError(String.format(format, parameters));
	}
	
	
	public static void main(final String[] args) throws IOException {
		final ClassDescriptor	def = parse(new ByteArrayReader(Files.readAllBytes(new File("c:/tmp/Layer.class").toPath()))); 
		
		System.err.println(def);
	}
}

