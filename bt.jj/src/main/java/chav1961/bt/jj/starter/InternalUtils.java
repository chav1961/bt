package chav1961.bt.jj.starter;

public class InternalUtils {
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
	
	public static int allocateStaticMemory(final int initialDisplacement, final FieldItem[] fields, final ConstantPoolItem[] pool) {
		int displ = ((initialDisplacement + JJ.QWORD_SIZE - 1) / JJ.QWORD_SIZE) * JJ.QWORD_SIZE;
		
		for(int pass = 0; pass <= 3; pass++) {
			for (FieldItem item : fields) {
				if ((item.accessFlags & 0x01) != 0) {
					final String	type = ClassDefinitionLoader.resolveDescriptor(pool, item.fieldDesc);
					
					switch (type.charAt(0)) {
						case 'D' : case 'J' : case 'L' : case '[' :
							if (pass == 0) {
								item.displacement = displ;
								item.length = JJ.QWORD_SIZE;
								displ += JJ.QWORD_SIZE;
							}
							break;
						case 'I' : 
							if (pass == 1) {
								item.displacement = displ;
								item.length = JJ.DWORD_SIZE;
								displ += JJ.DWORD_SIZE;
							}
							break;
						case 'S' : case 'C' :
							if (pass == 2) {
								item.displacement = displ;
								item.length = JJ.WORD_SIZE;
								displ += JJ.WORD_SIZE;
							}
							break;
						case 'B' : case 'Z' :
							if (pass == 3) {
								item.displacement = displ;
								item.length = JJ.BYTE_SIZE;
								displ += JJ.BYTE_SIZE;
							}
							break;
						default :
							throw new IllegalArgumentException("Illegal field type ["+type+"]");
					}
					
				}
			}
		}
		return displ;
	}

	public static int allocateInstanceMemory(final int initialDisplacement, final FieldItem[] fields, final ConstantPoolItem[] pool) {
		int displ = ((initialDisplacement + JJ.QWORD_SIZE - 1) / JJ.QWORD_SIZE) * JJ.QWORD_SIZE;
		
		for(int pass = 0; pass <= 3; pass++) {
			for (FieldItem item : fields) {
				if ((item.accessFlags & 0x01) == 0) {
					final String	type = ClassDefinitionLoader.resolveDescriptor(pool, item.fieldDesc);
					
					switch (type.charAt(0)) {
						case 'D' : case 'J' : case 'L' : case '[' :
							if (pass == 0) {
								item.displacement = displ;
								item.length = JJ.QWORD_SIZE;
								displ += JJ.QWORD_SIZE;
							}
							break;
						case 'I' : 
							if (pass == 1) {
								item.displacement = displ;
								item.length = JJ.DWORD_SIZE;
								displ += JJ.DWORD_SIZE;
							}
							break;
						case 'S' : case 'C' :
							if (pass == 2) {
								item.displacement = displ;
								item.length = JJ.WORD_SIZE;
								displ += JJ.WORD_SIZE;
							}
							break;
						case 'B' : case 'Z' :
							if (pass == 3) {
								item.displacement = displ;
								item.length = JJ.BYTE_SIZE;
								displ += JJ.BYTE_SIZE;
							}
							break;
						default :
							throw new IllegalArgumentException("Illegal field type ["+type+"]");
					}
					
				}
			}
		}
		return displ;
	}
}
