package chav1961.bt.jj.starter;

class CodeExecutor {
	static final int	INSTANCE_PREFIX_SIZE = 24;
	static final int	LONG_SIZE = 8;
	static final int	INT_SIZE = 4;
	static final int	SHORT_SIZE = 2;
	static final int	BYTE_SIZE = 1;
	
	public static native long getLong(final Object ref, final long displ);
	public static native void setLong(final Object ref, final long displ, final long value);
	public static native long getInt(final Object ref, final long displ);
	public static native void setInt(final Object ref, final long displ, final int value);
	public static native long getShort(final Object ref, final long displ);
	public static native void setShort(final Object ref, final long displ, final int value);
	public static native long getByte(final Object ref, final long displ);
	public static native void setByte(final Object ref, final long displ, final int value);
	public static native boolean testAndSetFinalBit(final Object ref, final int fieldIndex);
	
	public static int compare(final char[] left, final char[] right) {
		for(int index = 0, maxIndex = left.length > right.length ? right.length : left.length; index < maxIndex; index++) {
			int	result = left[index] - right[index];
			
			if (result != 0) {
				return result;
			}
		}
		return left.length - right.length;
	}
	
	public static int getArgumentSize(final char[] argType) {
		switch (argType[0]) {
			case 'B' :
				return 1;
			case 'C' :
				return 2;
			case 'D' :
				return 8;
			case 'F' :
				return 4;
			case 'I' :
				return 4;
			case 'J' :
				return 8;
			case 'L' :
				return 8;
			case 'S' :
				return 2;
			case 'Z' :
				return 1;
			default :
				throw new UnsupportedOperationException();
		}
	}

	public static int getArgumentSize(final String argType) {
		return getArgumentSize(argType.toCharArray());
	}
}
