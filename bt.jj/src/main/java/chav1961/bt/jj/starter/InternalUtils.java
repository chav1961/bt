package chav1961.bt.jj.starter;

class InternalUtils {
	private static final char[]	STRING_SIGNATURE = "Ljava/lang/String;".toCharArray();
	
	public static void memcpy(final long from, final long to, final long len) {
		for(long index = 0; index < len; index++) {
			JJ.store1(to+index, JJ.load1(from+index));
		}
	}
	
	public static int compareTo(final char[] left, final char[] right) {
		for(int index = 0, maxIndex = left.length > right.length ? right.length : left.length; index < maxIndex; index++) {
			final int	delta = right[index] - left[index];
			
			if (delta != 0) {
				return delta;
			}
		}
		return right.length - left.length;
	}
	
	public static TypeKind typeBySignature(final char[] signature) {
		switch (signature[0]) {
			case 'B' : return TypeKind.TYPE_BYTE;
			case 'C' : return TypeKind.TYPE_CHAR;
			case 'D' : return TypeKind.TYPE_DOUBLE;
			case 'F' : return TypeKind.TYPE_FLOAT;
			case 'I' : return TypeKind.TYPE_INT;
			case 'J' : return TypeKind.TYPE_LONG;
			case 'S' : return TypeKind.TYPE_SHORT;
			case 'V' : return TypeKind.TYPE_VOID;
			case 'Z' : return TypeKind.TYPE_BOOLEAN; 
			case '[' :
				if (signature[1] == 'L' || signature[1] == '[') {
					return TypeKind.TYPE_ARRAY_R;
				}
				else {
					return TypeKind.TYPE_ARRAY_P;
				}
			case 'L' :
				if (compareTo(signature, STRING_SIGNATURE) == 0) {
					return TypeKind.TYPE_STRING;
				}
				else {
					return TypeKind.TYPE_REF;
				}
			default :
				throw new IllegalArgumentException("Illegal field signature ["+new String(signature)+"]");
		}
	}
	
	public static int allocateStaticMemory(final int initialDisplacement, final FieldItem[] fields, final ConstantPool pool) {
		int displ = ((initialDisplacement + JJ.QWORD_SIZE - 1) / JJ.QWORD_SIZE) * JJ.QWORD_SIZE;
		
		for(int pass = 0; pass <= 3; pass++) {
			for (FieldItem item : fields) {
				if ((item.accessFlags & DefinitionLoader.ACC_STATIC) != 0) {
					switch (typeBySignature(pool.get(item.fieldDesc).content)) {
						case TYPE_DOUBLE : case TYPE_LONG : case TYPE_REF : case TYPE_STRING : case TYPE_ARRAY_P : case TYPE_ARRAY_R :
							if (pass == 0) {
								item.displacement = displ;
								item.length = JJ.QWORD_SIZE;
								displ += JJ.QWORD_SIZE;
							}
							break;
						case TYPE_INT : 
							if (pass == 1) {
								item.displacement = displ;
								item.length = JJ.DWORD_SIZE;
								displ += JJ.DWORD_SIZE;
							}
							break;
						case TYPE_CHAR : case TYPE_SHORT :
							if (pass == 2) {
								item.displacement = displ;
								item.length = JJ.WORD_SIZE;
								displ += JJ.WORD_SIZE;
							}
							break;
						case TYPE_BYTE : case TYPE_BOOLEAN :
							if (pass == 3) {
								item.displacement = displ;
								item.length = JJ.BYTE_SIZE;
								displ += JJ.BYTE_SIZE;
							}
							break;
						default :
							throw new IllegalArgumentException("Illegal field type ["+pool.deepToString(item.fieldDesc)+"]");
					}
				}
			}
		}
		return displ;
	}

	public static int allocateInstanceMemory(final int initialDisplacement, final FieldItem[] fields, final ConstantPool pool) {
		int displ = ((initialDisplacement + JJ.QWORD_SIZE - 1) / JJ.QWORD_SIZE) * JJ.QWORD_SIZE;
		
		for(int pass = 0; pass <= 3; pass++) {
			for (FieldItem item : fields) {
				if ((item.accessFlags & DefinitionLoader.ACC_STATIC) == 0) {
					switch (typeBySignature(pool.get(item.fieldDesc).content)) {
						case TYPE_DOUBLE : case TYPE_LONG : case TYPE_REF : case TYPE_STRING : case TYPE_ARRAY_P : case TYPE_ARRAY_R :
							if (pass == 0) {
								item.displacement = displ;
								item.length = JJ.QWORD_SIZE;
								displ += JJ.QWORD_SIZE;
							}
							break;
						case TYPE_INT : 
							if (pass == 1) {
								item.displacement = displ;
								item.length = JJ.DWORD_SIZE;
								displ += JJ.DWORD_SIZE;
							}
							break;
						case TYPE_CHAR : case TYPE_SHORT :
							if (pass == 2) {
								item.displacement = displ;
								item.length = JJ.WORD_SIZE;
								displ += JJ.WORD_SIZE;
							}
							break;
						case TYPE_BYTE : case TYPE_BOOLEAN :
							if (pass == 3) {
								item.displacement = displ;
								item.length = JJ.BYTE_SIZE;
								displ += JJ.BYTE_SIZE;
							}
							break;
						default :
							throw new IllegalArgumentException("Illegal field type ["+pool.deepToString(item.fieldDesc)+"]");
					}
				}
			}
		}
		return displ;
	}
}
