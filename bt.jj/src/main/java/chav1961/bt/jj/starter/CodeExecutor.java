package chav1961.bt.jj.starter;

import java.lang.reflect.Array;

class CodeExecutor {
	static final int	INSTANCE_PREFIX_SIZE = 24;
	static final int	LONG_SIZE = 8;
	static final int	INT_SIZE = 4;
	static final int	SHORT_SIZE = 2;
	static final int	BYTE_SIZE = 1;

	private static final int	FLOAT_0 = Float.floatToIntBits(0f); 
	private static final int	FLOAT_1 = Float.floatToIntBits(1f); 
	private static final int	FLOAT_2 = Float.floatToIntBits(2f); 
	private static final long	DOUBLE_0 = Double.doubleToLongBits(0f); 
	private static final long	DOUBLE_1 = Double.doubleToLongBits(1f); 
	
	public static native long getLong(final Object ref, final long displ);
	public static native void setLong(final Object ref, final long displ, final long value);
	public static native long getInt(final Object ref, final long displ);
	public static native void setInt(final Object ref, final long displ, final int value);
	public static native long getShort(final Object ref, final long displ);
	public static native void setShort(final Object ref, final long displ, final int value);
	public static native long getByte(final Object ref, final long displ);
	public static native void setByte(final Object ref, final long displ, final int value);
	public static native boolean testAndSetFinalBit(final Object ref, final int fieldIndex);
	public static native Object asObject(final long ref);
	public static native long asLong(final Object ref);
	public static native float asFloat(final int val);
	public static native int asInt(final float val);
	public static native double asDouble(final long val);
	public static native int asLong(final double val);
	
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
			case '[' :
				return 8;
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

	public static long execute(final short localSize, final short stackSize, final byte[] code, final ClassDescriptor clazz, final Object inst) {
		@JJFromStack
		final long[]	locals = new long[localSize];
		@JJFromStack
		final byte[]	localsContent = new byte[stackSize];
		@JJFromStack
		final long[]	stack = new long[stackSize];
		@JJFromStack
		final byte[]	stackContent = new byte[stackSize];
		
		return execute(locals, localsContent, stack, stackContent, code, clazz, inst);
	}
	
	public static long execute(final long[] locals, final byte[] localsContent, final long[] stack, final byte[] stackContent, final byte[] code, final ClassDescriptor clazz, final Object inst) {
		Object	ref;
		boolean	wideDetected = false;
		int		stackTop = 0;
		int		commandPointer = 0;
		int		argument = 0;
		long	value;
		
loop:	for(;;) {
			argument = 0;
			switch ((short)code[commandPointer]) {
				case 0x00: // nop
					break;
				case 0x01: // aconst_null
					stack[stackTop] = 0;
					stackContent[stackTop] = 'L';
					stackTop++;
					break;
				case 0x02: // iconst_m1
					stack[stackTop] = -1;
					stackContent[stackTop] = 'I';
					stackTop++;
					break;
				case 0x03: // iconst_0
					stack[stackTop] = 0;
					stackContent[stackTop] = 'I';
					stackTop++;
					break;
				case 0x04: // iconst_1
					stack[stackTop] = 1;
					stackContent[stackTop] = 'I';
					stackTop++;
					break;
				case 0x05: // iconst_2
					stack[stackTop] = 2;
					stackContent[stackTop] = 'I';
					stackTop++;
					break;
				case 0x06: // iconst_3
					stack[stackTop] = 3;
					stackContent[stackTop] = 'I';
					stackTop++;
					break;
				case 0x07: // iconst_4
					stack[stackTop] = 4;
					stackContent[stackTop] = 'I';
					stackTop++;
					break;
				case 0x08: // iconst_5
					stack[stackTop] = 5;
					stackContent[stackTop] = 'I';
					stackTop++;
					break;
				case 0x09: // lconst_0
					stack[stackTop] = 0;
					stackContent[stackTop] = 'J';
					stackTop++;
					break;
				case 0x0A: // lconst_1
					stack[stackTop] = 1;
					stackContent[stackTop] = 'J';
					stackTop++;
					break;
				case 0x0B: // fconst_0
					stack[stackTop] = FLOAT_0;
					stackContent[stackTop] = 'F';
					stackTop++;
					break;
				case 0x0C: // fconst_1
					stack[stackTop] = FLOAT_1;
					stackContent[stackTop] = 'F';
					stackTop++;
					break;
				case 0x0D: // fconst_2
					stack[stackTop] = FLOAT_2;
					stackContent[stackTop] = 'F';
					stackTop++;
					break;
				case 0x0E: // dconst_0
					stack[stackTop] = DOUBLE_0;
					stackContent[stackTop] = 'D';
					stackTop++;
					break;
				case 0x0F: // dconst_1
					stack[stackTop] = DOUBLE_1;
					stackContent[stackTop] = 'D';
					stackTop++;
					break;
				case 0x10: // bipush
					stack[stackTop] = code[commandPointer+1];
					stackContent[stackTop] = 'I';
					stackTop++;
					commandPointer++;
					break;
				case 0x11: // sipush
					stack[stackTop] = (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF);
					stackContent[stackTop] = 'I';
					stackTop++;
					commandPointer += 2;
					break;
				case 0x12: // ldc
					stackTop = loadConst(clazz, code[commandPointer+1], stack, stackContent, stackTop, false);
					commandPointer++;
					break;
				case 0x13: // ldc_w
					stackTop = loadConst(clazz, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF), stack, stackContent, stackTop, false);
					commandPointer += 2;
					break;
				case 0x14: // ldc2_w
					stackTop = loadConst(clazz, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF), stack, stackContent, stackTop, true);
					commandPointer += 2;
					break;
				case 0x15: // iload
					if (wideDetected) {
						argument = ((code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF)) - 3;
						commandPointer += 2;
						wideDetected = false;
					}
					else {
						argument = (code[commandPointer+2] & 0xFF) - 3;
						commandPointer++;
					}
				case 0x1D: // iload_3
					argument++;
				case 0x1C: // iload_2
					argument++;
				case 0x1B: // iload_1
					argument++;
				case 0x1A: // iload_0
					if (localsContent[argument] == 'I') {
						stack[stackTop] = locals[argument];
						stackContent[stackTop] = 'I';
						stackTop++;
					}
					else {
						throw new Error();
					}
				case 0x16: // lload
					if (wideDetected) {
						argument = ((code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF)) - 3;
						commandPointer += 2;
						wideDetected = false;
					}
					else {
						argument = (code[commandPointer+2] & 0xFF) - 3;
						commandPointer++;
					}
					break;
				case 0x21: // lload_3
					argument++;
				case 0x20: // lload_2
					argument++;
				case 0x1F: // lload_1
					argument++;
				case 0x1E: // lload_0
					if (localsContent[argument] == 'J' && localsContent[argument+1] == '*') {
						stack[stackTop] = locals[argument];
						stackContent[stackTop] = 'J';
						stack[stackTop + 1] = 0;
						stackContent[stackTop + 1] = '*';
						stackTop += 2;
					}
					else {
						throw new Error();
					}
				case 0x17: // fload
					if (wideDetected) {
						argument = ((code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF)) - 3;
						commandPointer += 2;
						wideDetected = false;
					}
					else {
						argument = (code[commandPointer+2] & 0xFF) - 3;
						commandPointer++;
					}
					break;
				case 0x25: // fload_3
					argument++;
				case 0x24: // fload_2
					argument++;
				case 0x23: // fload_1
					argument++;
				case 0x22: // fload_0
					if (localsContent[argument] == 'F') {
						stack[stackTop] = locals[argument];
						stackContent[stackTop] = 'F';
						stackTop++;
					}
					else {
						throw new Error();
					}
					break;
				case 0x18: // dload
					if (wideDetected) {
						argument = ((code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF)) - 3;
						commandPointer += 2;
						wideDetected = false;
					}
					else {
						argument = (code[commandPointer+2] & 0xFF) - 3;
						commandPointer++;
					}
				case 0x29: // dload_3
					argument++;
				case 0x28: // dload_2
					argument++;
				case 0x27: // dload_1
					argument++;
				case 0x26: // dload_0
					if (localsContent[argument] == 'D' && localsContent[argument+1] == '*') {
						stack[stackTop] = locals[argument];
						stackContent[stackTop] = 'D';
						stack[stackTop+1] = 0;
						stackContent[stackTop+1] = '*';
						stackTop += 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x19: // aload
					if (wideDetected) {
						argument = ((code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF)) - 3;
						commandPointer += 2;
						wideDetected = false;
					}
					else {
						argument = (code[commandPointer+2] & 0xFF) - 3;
						commandPointer++;
					}
				case 0x2D: // aload_3
					argument++;
				case 0x2C: // aload_2
					argument++;
				case 0x2B: // aload_1
					argument++;
				case 0x2A: // aload_0
					if (localsContent[argument] == 'L' || localsContent[argument] == '[') {
						stack[stackTop] = locals[argument];
						stackContent[stackTop] = localsContent[argument];
						stackTop++;
					}
					else {
						throw new Error();
					}
					break;
				case 0x2E: // iaload
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == '[') {
						argument = (int)stack[stackTop];
						ref = asObject(stack[stackTop-1]);
						if (ref instanceof int[]) {
							final int[]	temp = (int[])ref;
							
							if (argument < 0 | argument >= temp.length) {
								throw new Error();
							}
							else {
								stack[stackTop-1] = temp[argument];
								stackContent[stackTop-1] = 'I';
								stackTop--;
							}
						}
						else {
							throw new Error();
						}
					}
					break;
				case 0x2F: // laload
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == '[') {
						argument = (int)stack[stackTop];
						ref = asObject(stack[stackTop-1]);
						if (ref instanceof long[]) {
							final long[]	temp = (long[])ref;
							
							if (argument < 0 | argument >= temp.length) {
								throw new Error();
							}
							else {
								stack[stackTop-1] = temp[argument];
								stackContent[stackTop-1] = 'J';
								stack[stackTop] = 0;
								stackContent[stackTop] = '*';
							}
						}
						else {
							throw new Error();
						}
					}
					break;
				case 0x30: // faload
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == '[') {
						argument = (int)stack[stackTop];
						ref = asObject(stack[stackTop-1]);
						if (ref instanceof float[]) {
							final float[]	temp = (float[])ref;
							
							if (argument < 0 | argument >= temp.length) {
								throw new Error();
							}
							else {
								stack[stackTop-1] = asInt(temp[argument]);
								stackContent[stackTop-1] = 'F';
								stackTop--;
							}
						}
						else {
							throw new Error();
						}
					}
					break;
				case 0x31: // daload
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == '[') {
						argument = (int)stack[stackTop];
						ref = asObject(stack[stackTop-1]);
						if (ref instanceof double[]) {
							final double[]	temp = (double[])ref;
							
							if (argument < 0 | argument >= temp.length) {
								throw new Error();
							}
							else {
								stack[stackTop-1] = asLong(temp[argument]);
								stackContent[stackTop-1] = 'D';
								stack[stackTop] = 0;
								stackContent[stackTop] = '*';
							}
						}
						else {
							throw new Error();
						}
					}
					break;
				case 0x32: // aaload
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == '[') {
						argument = (int)stack[stackTop];
						ref = asObject(stack[stackTop-1]);
						if (argument < 0 | argument >= Array.getLength(ref)) {
							throw new Error();
						}
						else {
							ref = Array.get(ref, argument);
							stack[stackTop-1] = asLong(ref);
							stackContent[stackTop-1] = (byte)(ref.getClass().isArray() ? '[' : 'L');
							stackTop--;
						}
					}
					break;
				case 0x33: // baload
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == '[') {
						argument = (int)stack[stackTop];
						ref = asObject(stack[stackTop-1]);
						if (ref instanceof byte[]) {
							final byte[]	temp = (byte[])ref;
							
							if (argument < 0 | argument >= temp.length) {
								throw new Error();
							}
							else {
								stack[stackTop-1] = temp[argument];
								stackContent[stackTop-1] = 'I';
								stackTop--;
							}
						}
						else {
							throw new Error();
						}
					}
					break;
				case 0x34: // caload
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == '[') {
						argument = (int)stack[stackTop];
						ref = asObject(stack[stackTop-1]);
						if (ref instanceof char[]) {
							final char[]	temp = (char[])ref;
							
							if (argument < 0 | argument >= temp.length) {
								throw new Error();
							}
							else {
								stack[stackTop-1] = temp[argument];
								stackContent[stackTop-1] = 'I';
								stackTop--;
							}
						}
						else {
							throw new Error();
						}
					}
					break;
				case 0x35: // saload
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == '[') {
						argument = (int)stack[stackTop];
						ref = asObject(stack[stackTop-1]);
						if (ref instanceof short[]) {
							final short[]	temp = (short[])ref;
							
							if (argument < 0 | argument >= temp.length) {
								throw new Error();
							}
							else {
								stack[stackTop-1] = temp[argument];
								stackContent[stackTop-1] = 'I';
								stackTop--;
							}
						}
						else {
							throw new Error();
						}
					}
					break;
				case 0x36: // istore
					if (wideDetected) {
						argument = ((code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF)) - 3;
						commandPointer += 2;
						wideDetected = false;
					}
					else {
						argument = (code[commandPointer+2] & 0xFF) - 3;
						commandPointer++;
					}
				case 0x3E: // istore_3
					argument++;
				case 0x3D: // istore_2
					argument++;
				case 0x3C: // istore_1
					argument++;
				case 0x3B: // istore_0
					if (stackContent[stackTop] == 'I') {
						locals[argument] = (int)stackContent[stackTop];
						localsContent[argument] = 'I';
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x37: // lstore
					if (wideDetected) {
						argument = ((code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF)) - 3;
						commandPointer += 2;
						wideDetected = false;
					}
					else {
						argument = (code[commandPointer+2] & 0xFF) - 3;
						commandPointer++;
					}
				case 0x42: // lstore_3
					argument++;
				case 0x41: // lstore_2
					argument++;
				case 0x40: // lstore_1
					argument++;
				case 0x3F: // lstore_0
					if (stackContent[stackTop-1] == 'J' && stackContent[stackTop] == '*') {
						locals[argument] = stackContent[stackTop];
						localsContent[argument] = 'I';
						locals[argument+1] = 0;
						localsContent[argument+1] = '*';
						stackTop -= 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x38: // fstore
					if (wideDetected) {
						argument = ((code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF)) - 3;
						commandPointer += 2;
						wideDetected = false;
					}
					else {
						argument = (code[commandPointer+2] & 0xFF) - 3;
						commandPointer++;
					}
				case 0x46: // fstore_3
					argument++;
				case 0x45: // fstore_2
					argument++;
				case 0x44: // fstore_1
					argument++;
				case 0x43: // fstore_0
					if (stackContent[stackTop] == 'F') {
						locals[argument] = stackContent[stackTop];
						localsContent[argument] = 'F';
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x39: // dstore
					if (wideDetected) {
						argument = ((code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF)) - 3;
						commandPointer += 2;
						wideDetected = false;
					}
					else {
						argument = (code[commandPointer+2] & 0xFF) - 3;
						commandPointer++;
					}
				case 0x4A: // dstore_3
					argument++;
				case 0x49: // dstore_2
					argument++;
				case 0x48: // dstore_1
					argument++;
				case 0x47: // dstore_0
					if (stackContent[stackTop-1] == 'D' && stackContent[stackTop] == '*') {
						locals[argument] = stackContent[stackTop];
						localsContent[argument] = 'D';
						locals[argument+1] = 0;
						localsContent[argument+1] = '*';
						stackTop -= 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x3A: // astore
					if (wideDetected) {
						argument = ((code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF)) - 3;
						commandPointer += 2;
						wideDetected = false;
					}
					else {
						argument = (code[commandPointer+2] & 0xFF) - 3;
						commandPointer++;
					}
				case 0x4E: // astore_3
					argument++;
				case 0x4D: // astore_2
					argument++;
				case 0x4C: // astore_1
					argument++;
				case 0x4B: // astore_0
					if (stackContent[stackTop] == 'L' || stackContent[stackTop] == '[') {
						locals[argument] = stackContent[stackTop];
						localsContent[argument] = stackContent[stackTop];
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x4F: // iastore
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I' && stackContent[stackTop-2] == '[') {
						argument = (int)stack[stackTop-1];
						ref = asObject(stack[stackTop-2]);
						if (ref instanceof int[]) {
							final int[]	temp = (int[])ref;
							
							if (argument < 0 | argument >= temp.length) {
								throw new Error();
							}
							else {
								temp[argument] = (int)stack[stackTop];
								stackTop -= 3;
							}
						}
						else {
							throw new Error();
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0x50: // lastore
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'J' && stackContent[stackTop-2] == 'I' && stackContent[stackTop-3] == '[') {
						argument = (int)stack[stackTop-2];
						ref = asObject(stack[stackTop-3]);
						if (ref instanceof long[]) {
							final long[]	temp = (long[])ref;
							
							if (argument < 0 | argument >= temp.length) {
								throw new Error();
							}
							else {
								temp[argument] = stack[stackTop];
								stackTop -= 4;
							}
						}
						else {
							throw new Error();
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0x51: // fastore
					if (stackContent[stackTop] == 'F' && stackContent[stackTop-1] == 'I' && stackContent[stackTop-2] == '[') {
						argument = (int)stack[stackTop-1];
						ref = asObject(stack[stackTop-2]);
						if (ref instanceof float[]) {
							final float[]	temp = (float[])ref;
							
							if (argument < 0 | argument >= temp.length) {
								throw new Error();
							}
							else {
								temp[argument] = asFloat((int)stack[stackTop]);
								stackTop -= 3;
							}
						}
						else {
							throw new Error();
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0x52: // dastore
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'D' && stackContent[stackTop-2] == 'I' && stackContent[stackTop-3] == '[') {
						argument = (int)stack[stackTop-2];
						ref = asObject(stack[stackTop-3]);
						if (ref instanceof double[]) {
							final double[]	temp = (double[])ref;
							
							if (argument < 0 | argument >= temp.length) {
								throw new Error();
							}
							else {
								temp[argument] = stack[stackTop];
								stackTop -= 4;
							}
						}
						else {
							throw new Error();
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0x53: // aastore
					if ((stackContent[stackTop] == 'L' || stackContent[stackTop] == '[') && stackContent[stackTop-1] == 'I' && stackContent[stackTop-2] == '[') {
						argument = (int)stack[stackTop-1];
						ref = asObject(stack[stackTop-2]);
							
						if (argument < 0 | argument >= Array.getLength(ref)) {
							throw new Error();
						}
						else {
							Array.set(ref, argument, asObject(stack[stackTop]));
							stackTop -= 3;
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0x54: // bastore
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I' && stackContent[stackTop-2] == '[') {
						argument = (int)stack[stackTop-1];
						ref = asObject(stack[stackTop-2]);
						if (ref instanceof byte[]) {
							final byte[]	temp = (byte[])ref;
							
							if (argument < 0 | argument >= temp.length) {
								throw new Error();
							}
							else {
								temp[argument] = (byte)stack[stackTop];
								stackTop -= 3;
							}
						}
						else {
							throw new Error();
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0x55: // castore
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I' && stackContent[stackTop-2] == '[') {
						argument = (int)stack[stackTop-1];
						ref = asObject(stack[stackTop-2]);
						if (ref instanceof char[]) {
							final char[]	temp = (char[])ref;
							
							if (argument < 0 | argument >= temp.length) {
								throw new Error();
							}
							else {
								temp[argument] = (char)stack[stackTop];
								stackTop -= 3;
							}
						}
						else {
							throw new Error();
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0x56: // sastore
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I' && stackContent[stackTop-2] == '[') {
						argument = (int)stack[stackTop-1];
						ref = asObject(stack[stackTop-2]);
						if (ref instanceof short[]) {
							final short[]	temp = (short[])ref;
							
							if (argument < 0 | argument >= temp.length) {
								throw new Error();
							}
							else {
								temp[argument] = (short)stack[stackTop];
								stackTop -= 3;
							}
						}
						else {
							throw new Error();
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0x57: // pop
					stackTop--;
					break;
				case 0x58: // pop2
					stackTop--;
					break;
				case 0x59: // dup
					stackTop++;
					stack[stackTop] = stack[stackTop-1];
					stackContent[stackTop] = stackContent[stackTop-1]; 
					break;
// ------------------------------------------					
				case 0x5A: // dup_x1
				case 0x5B: // dup_x2
				case 0x5C: // dup2
				case 0x5D: // dup2_x1
				case 0x5E: // dup2_x2
// ------------------------------------------					
				case 0x5F: // swap
					value = stack[stackTop];
					stack[stackTop] = stack[stackTop-1];
					stack[stackTop-1] = value;
					argument = stackContent[stackTop];
					stackContent[stackTop] = stackContent[stackTop-1];
					stackContent[stackTop-1] = (byte)argument;
					break;
				case 0x60:	// iadd
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						stack[stackTop-1] = ((int)stack[stackTop-1]) + ((int)stack[stackTop]);
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x61:	// ladd
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'J' && stackContent[stackTop-2] == '*' && stackContent[stackTop-3] == 'J') {
						stack[stackTop-3] = stack[stackTop-3] + stack[stackTop - 1];
						stackTop -= 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x62:	// fadd
					if (stackContent[stackTop] == 'F' && stackContent[stackTop-1] == 'F') {
						stack[stackTop-1] = asInt((float)(asFloat((int)stack[stackTop-1]) + asFloat((int)stack[stackTop])));
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x63:	// dadd
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'D' && stackContent[stackTop-2] == '*' && stackContent[stackTop-3] == 'D') {
						stack[stackTop-3] = asLong(asDouble(stack[stackTop-3]) + asDouble(stack[stackTop - 1]));
						stackTop -= 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x64:	// isub
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						stack[stackTop-1] = ((int)stack[stackTop-1]) - ((int)stack[stackTop]);
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x65:	// lsub
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'J' && stackContent[stackTop-2] == '*' && stackContent[stackTop-3] == 'J') {
						stack[stackTop-3] = stack[stackTop-3] - stack[stackTop - 1];
						stackTop -= 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x66:	// fsub
					if (stackContent[stackTop] == 'F' && stackContent[stackTop-1] == 'F') {
						stack[stackTop-1] = asInt((float)(asFloat((int)stack[stackTop-1]) - asFloat((int)stack[stackTop])));
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x67:	// dsub
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'D' && stackContent[stackTop-2] == '*' && stackContent[stackTop-3] == 'D') {
						stack[stackTop-3] = asLong(asDouble(stack[stackTop-3]) - asDouble(stack[stackTop - 1]));
						stackTop -= 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x68:	// imul
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						stack[stackTop-1] = ((int)stack[stackTop-1]) * ((int)stack[stackTop]);
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x69:	// lmul
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'J' && stackContent[stackTop-2] == '*' && stackContent[stackTop-3] == 'J') {
						stack[stackTop-3] = stack[stackTop-3] * stack[stackTop - 1];
						stackTop -= 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x6A:	// fmul
					if (stackContent[stackTop] == 'F' && stackContent[stackTop-1] == 'F') {
						stack[stackTop-1] = asInt((float)(asFloat((int)stack[stackTop-1]) * asFloat((int)stack[stackTop])));
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x6B:	// dmul
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'D' && stackContent[stackTop-2] == '*' && stackContent[stackTop-3] == 'D') {
						stack[stackTop-3] = asLong(asDouble(stack[stackTop-3]) * asDouble(stack[stackTop - 1]));
						stackTop -= 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x6C:	// idiv
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						stack[stackTop-1] = ((int)stack[stackTop-1]) / ((int)stack[stackTop]);
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x6D:	// ldiv
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'J' && stackContent[stackTop-2] == '*' && stackContent[stackTop-3] == 'J') {
						stack[stackTop-3] = stack[stackTop-3] / stack[stackTop - 1];
						stackTop -= 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x6E:	// fdiv
					if (stackContent[stackTop] == 'F' && stackContent[stackTop-1] == 'F') {
						stack[stackTop-1] = asInt((float)(asFloat((int)stack[stackTop-1]) / asFloat((int)stack[stackTop])));
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x6F:	// ddiv
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'D' && stackContent[stackTop-2] == '*' && stackContent[stackTop-3] == 'D') {
						stack[stackTop-3] = asLong(asDouble(stack[stackTop-3]) / asDouble(stack[stackTop - 1]));
						stackTop -= 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x70:	// irem
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						stack[stackTop-1] = ((int)stack[stackTop-1]) % ((int)stack[stackTop]);
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x71:	// lrem
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'J' && stackContent[stackTop-2] == '*' && stackContent[stackTop-3] == 'J') {
						stack[stackTop-3] = stack[stackTop-3] % stack[stackTop - 1];
						stackTop -= 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x72:	// frem
					if (stackContent[stackTop] == 'F' && stackContent[stackTop-1] == 'F') {
						stack[stackTop-1] = asInt((float)(asFloat((int)stack[stackTop-1]) % asFloat((int)stack[stackTop])));
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x73:	// drem
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'D' && stackContent[stackTop-2] == '*' && stackContent[stackTop-3] == 'D') {
						stack[stackTop-3] = asLong(asDouble(stack[stackTop-3]) % asDouble(stack[stackTop - 1]));
						stackTop -= 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x74:	// ineg
					if (stackContent[stackTop] == 'I') {
						stack[stackTop] = -((int)stack[stackTop]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x75:	// lneg
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'J') {
						stack[stackTop-1] = -stack[stackTop-1];
					}
					else {
						throw new Error();
					}
					break;
				case 0x76:	// fneg
					if (stackContent[stackTop] == 'F') {
						stack[stackTop] = asInt(-((float)(asFloat((int)stack[stackTop]))));
					}
					else {
						throw new Error();
					}
					break;
				case 0x77:	// dneg
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'D') {
						stack[stackTop-1] = asLong(-asDouble(stack[stackTop-1]));
					}
					else {
						throw new Error();
					}
					break;
				case 0x78:	// ishl
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						stack[stackTop-1] = (int)(stack[stackTop-1] << (int)stack[stackTop]);
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x79:	// lshl
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == '*' && stackContent[stackTop-2] == 'J') {
						stack[stackTop-1] = stack[stackTop-2] << (int)stack[stackTop];
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x7A:	// ishr
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						stack[stackTop-1] = (int)(stack[stackTop-1] >> (int)stack[stackTop]);
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x7B:	// lshr
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == '*' && stackContent[stackTop-2] == 'J') {
						stack[stackTop-1] = stack[stackTop-2] >> (int)stack[stackTop];
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x7C:	// iushr
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						stack[stackTop-1] = (int)(stack[stackTop-1] >>> (int)stack[stackTop]);
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x7D:	// lushr
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == '*' && stackContent[stackTop-2] == 'J') {
						stack[stackTop-1] = stack[stackTop-2] >>> (int)stack[stackTop];
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x7E:	// iand
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						stack[stackTop-1] = stack[stackTop-1] & stack[stackTop];
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x7F:	// land
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'J' && stackContent[stackTop-2] == '*' && stackContent[stackTop-3] == 'J') {
						stack[stackTop-3] = stack[stackTop-3] & stack[stackTop-1];
						stackTop -= 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x80:	// ior
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						stack[stackTop-1] = stack[stackTop-1] | stack[stackTop];
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x81:	// lor
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'J' && stackContent[stackTop-2] == '*' && stackContent[stackTop-3] == 'J') {
						stack[stackTop-3] = stack[stackTop-3] | stack[stackTop-1];
						stackTop -= 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x82:	// ixor
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						stack[stackTop-1] = stack[stackTop-1] ^ stack[stackTop];
						stackTop--;
					}
					else {
						throw new Error();
					}
					break;
				case 0x83:	// lxor
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'J' && stackContent[stackTop-2] == '*' && stackContent[stackTop-3] == 'J') {
						stack[stackTop-3] = stack[stackTop-3] ^ stack[stackTop-1];
						stackTop -= 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x84:	// iinc
					if (wideDetected) {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF);
						value = (code[commandPointer+3] << 8) | (code[commandPointer+4] & 0xFF);
						locals[argument] += value;
						commandPointer += 4;
						wideDetected = false;
					}
					else {
						argument = code[commandPointer+1];
						value = code[commandPointer+2];
						locals[argument] += value;
						commandPointer += 2;
					}
					break;
				case 0x85:	// i2l
					if (stackContent[stackTop] == 'I') {
						stackContent[stackTop] = 'J';
						stackTop++;
						stack[stackTop] = 0;
						stackContent[stackTop] = '*';
					}
					else {
						throw new Error();
					}
					break;
				case 0x86:	// i2f
					if (stackContent[stackTop] == 'I') {
						stack[stackTop] = asInt(((float)stack[stackTop]));
						stackContent[stackTop] = 'F';
					}
					else {
						throw new Error();
					}
					break;
				case 0x87:	// i2d
					if (stackContent[stackTop] == 'I') {
						stack[stackTop] = asLong(((double)stack[stackTop]));
						stackContent[stackTop] = 'D';
						stackTop++;
						stack[stackTop] = 0;
						stackContent[stackTop] = '*';
					}
					else {
						throw new Error();
					}
					break;
				case 0x88:	// l2i
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'J') {
						stackTop--;
						stackContent[stackTop] = 'I';
						stack[stackTop] = (int)stack[stackTop];
					}
					else {
						throw new Error();
					}
					break;
				case 0x89:	// l2f
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'J') {
						stackTop--;
						stackContent[stackTop] = 'F';
						stack[stackTop] = asInt((float)stack[stackTop]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x8A:	// l2d
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'J') {
						stackContent[stackTop-1] = 'D';
						stack[stackTop-1] = asLong((double)stack[stackTop]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x8B:	// f2i
					if (stackContent[stackTop] == 'F') {
						stack[stackTop] = (int)asFloat((int)stack[stackTop]);
						stackContent[stackTop] = 'I';
					}
					else {
						throw new Error();
					}
					break;
				case 0x8C:	// f2l
					if (stackContent[stackTop] == 'F') {
						stack[stackTop] = (long)asFloat((int)stack[stackTop]);
						stackContent[stackTop] = 'J';
						stackTop++;
						stack[stackTop] = 0;
						stackContent[stackTop] = '*';
					}
					else {
						throw new Error();
					}
					break;
				case 0x8D:	// f2d
					if (stackContent[stackTop] == 'F') {
						stack[stackTop] = asLong((double)asFloat((int)stack[stackTop]));
						stackContent[stackTop] = 'D';
						stackTop++;
						stack[stackTop] = 0;
						stackContent[stackTop] = '*';
					}
					else {
						throw new Error();
					}
					break;
				case 0x8E:	// d2i
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'D') {
						stackTop--;
						stack[stackTop] = (int)asDouble(stack[stackTop]);
						stackContent[stackTop] = 'I';
					}
					else {
						throw new Error();
					}
					break;
				case 0x8F:	// d2l
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'D') {
						stack[stackTop-1] = (long)asDouble(stack[stackTop-1]);
						stackContent[stackTop-1] = 'J';
					}
					else {
						throw new Error();
					}
					break;
				case 0x90:	// d2f
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'D') {
						stackTop--;
						stack[stackTop] = asInt((float)asDouble(stack[stackTop]));
						stackContent[stackTop] = 'F';
					}
					else {
						throw new Error();
					}
					break;
				case 0x91:	// i2b
					if (stackContent[stackTop] == 'I') {
						stack[stackTop] = (byte)stack[stackTop];
					}
					else {
						throw new Error();
					}
					break;
				case 0x92:	// i2c
					if (stackContent[stackTop] == 'I') {
						stack[stackTop] = (char)stack[stackTop];
					}
					else {
						throw new Error();
					}
					break;
				case 0x93:	// i2s
					if (stackContent[stackTop] == 'I') {
						stack[stackTop] = (short)stack[stackTop];
					}
					else {
						throw new Error();
					}
					break;
				case 0x94:	// lcmp
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'L' && stackContent[stackTop-2] == '*' && stackContent[stackTop-3] == 'L') {
						if (stack[stackTop-3] < stack[stackTop-1]) {
							argument = -1;
						}
						else if (stack[stackTop-3] > stack[stackTop-1]) {
							argument = 1;
						}
						else {
							argument = 0;
						}
						stackTop -= 3;
						stack[stackTop] = argument;
						stackContent[stackTop] = 'I';
					}
					else {
						throw new Error();
					}
					break;
				case 0x95:	// fcmpl
				case 0x96:	// fcmpg
					if (stackContent[stackTop] == 'F' && stackContent[stackTop-1] == 'F') {
						if (asFloat((int)stack[stackTop-1]) < asFloat((int)stack[stackTop])) {
							argument = -1;
						}
						else if (asFloat((int)stack[stackTop-1]) > asFloat((int)stack[stackTop])) {
							argument = 1;
						}
						else {
							argument = 0;
						}
						stackTop--;
						stack[stackTop] = argument;
						stackContent[stackTop] = 'I';
					}
					else {
						throw new Error();
					}
					break;
				case 0x97:	// dcmpl
				case 0x98:	// dcmpg
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'D' && stackContent[stackTop-2] == '*' && stackContent[stackTop-3] == 'D') {
						if (asDouble(stack[stackTop-3]) < asDouble(stack[stackTop-1])) {
							argument = -1;
						}
						else if (asDouble(stack[stackTop-3]) > asDouble(stack[stackTop-1])) {
							argument = 1;
						}
						else {
							argument = 0;
						}
						stackTop -= 3;
						stack[stackTop] = argument;
						stackContent[stackTop] = 'I';
					}
					else {
						throw new Error();
					}
					break;
				case 0x99:	// ifeq
					if (stackContent[stackTop] == 'I') {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF);
						value = stackContent[stackTop];
						stackTop--;
						commandPointer += 2;
						if (value == 0) {
							commandPointer = argument-1;
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0x9A:	// ifne
					if (stackContent[stackTop] == 'I') {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF);
						value = stackContent[stackTop];
						stackTop--;
						commandPointer += 2;
						if (value != 0) {
							commandPointer = argument-1;
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0x9B:	// iflt
					if (stackContent[stackTop] == 'I') {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF);
						value = stackContent[stackTop];
						stackTop--;
						commandPointer += 2;
						if (value < 0) {
							commandPointer = argument-1;
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0x9C:	// ifge
					if (stackContent[stackTop] == 'I') {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF);
						value = stackContent[stackTop];
						stackTop--;
						commandPointer += 2;
						if (value >= 0) {
							commandPointer = argument-1;
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0x9D:	// ifgt
					if (stackContent[stackTop] == 'I') {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF);
						value = stackContent[stackTop];
						stackTop--;
						commandPointer += 2;
						if (value > 0) {
							commandPointer = argument-1;
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0x9E:	// ifle
					if (stackContent[stackTop] == 'I') {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF);
						value = stackContent[stackTop];
						stackTop--;
						commandPointer += 2;
						if (value <= 0) {
							commandPointer = argument-1;
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0x9F:	// if_icmpeq
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF);
						value = (int)stackContent[stackTop-1] - (int)stackContent[stackTop];
						stackTop -= 2;
						commandPointer += 2;
						if (value == 0) {
							commandPointer = argument-1;
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0xA0:	// if_icmpne
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF);
						value = (int)stackContent[stackTop-1] - (int)stackContent[stackTop];
						stackTop -= 2;
						commandPointer += 2;
						if (value != 0) {
							commandPointer = argument-1;
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0xA1:	// if_icmplt
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF);
						value = (int)stackContent[stackTop-1] - (int)stackContent[stackTop];
						stackTop -= 2;
						commandPointer += 2;
						if (value < 0) {
							commandPointer = argument-1;
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0xA2:	// if_icmpge
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF);
						value = (int)stackContent[stackTop-1] - (int)stackContent[stackTop];
						stackTop -= 2;
						commandPointer += 2;
						if (value >= 0) {
							commandPointer = argument-1;
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0xA3:	// if_icmpgt
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF);
						value = (int)stackContent[stackTop-1] - (int)stackContent[stackTop];
						stackTop -= 2;
						commandPointer += 2;
						if (value > 0) {
							commandPointer = argument-1;
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0xA4:	// if_icmple
					if (stackContent[stackTop] == 'I' && stackContent[stackTop-1] == 'I') {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF);
						value = (int)stackContent[stackTop-1] - (int)stackContent[stackTop];
						stackTop -= 2;
						commandPointer += 2;
						if (value <= 0) {
							commandPointer = argument-1;
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0xA5:	// if_acmpeq
					if ((stackContent[stackTop] == 'L' || stackContent[stackTop] == '[') && (stackContent[stackTop-1] == 'L' || stackContent[stackTop-1] == '[')) {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF);
						value = stackContent[stackTop-1] - stackContent[stackTop];
						stackTop -= 2;
						commandPointer += 2;
						if (value == 0) {
							commandPointer = argument-1;
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0xA6:	// if_acmpne
					if ((stackContent[stackTop] == 'L' || stackContent[stackTop] == '[') && (stackContent[stackTop-1] == 'L' || stackContent[stackTop-1] == '[')) {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF);
						value = stackContent[stackTop-1] - stackContent[stackTop];
						stackTop -= 2;
						commandPointer += 2;
						if (value != 0) {
							commandPointer = argument-1;
						}
					}
					else {
						throw new Error();
					}
					break;
				case 0xA7:	// goto
					argument = (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF);
					commandPointer = argument - 1;
					break;
// ------------------------------------------					
				case 0xA8:	// jsr
				case 0xA9:	// ret
				case 0xAA:	// tableswitch
				case 0xAB:	// lookupswitch
// ------------------------------------------
				case 0xAC:	// ireturn
					if (stackContent[stackTop] == 'I') {
						value = (int)stackContent[stackTop];
						break loop;
					}
					else {
						throw new Error();
					}
				case 0xAD:	// lreturn
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'J') {
						value = stackContent[stackTop-1];
						break loop;
					}
					else {
						throw new Error();
					}
				case 0xAE:	// freturn
					if (stackContent[stackTop] == 'F') {
						value = asLong(asFloat((int)stackContent[stackTop]));
						break loop;
					}
					else {
						throw new Error();
					}
				case 0xAF:	// dreturn
					if (stackContent[stackTop] == '*' && stackContent[stackTop-1] == 'D') {
						value = stackContent[stackTop-1];
						break loop;
					}
					else {
						throw new Error();
					}
				case 0xB0:	// areturn
					if (stackContent[stackTop] == 'L' || stackContent[stackTop] == '[') {
						value = stackContent[stackTop];
						break loop;
					}
					else {
						throw new Error();
					}
				case 0xB1:	// return
					value = 0;
					break loop;
				case 0xB2:	// getstatic
					argument = (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF);
					commandPointer += 2;
					stackTop = load(clazz, clazz, argument, stack, stackContent, stackTop);
					break;
				case 0xB3:	// putstatic
					argument = (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF);
					commandPointer += 2;
					stackTop = store(clazz, clazz, argument, stack, stackContent, stackTop);
					break;
				case 0xB4:	// getfield
					argument = (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF);
					commandPointer += 2;
					stackTop = load(clazz, inst, argument, stack, stackContent, stackTop);
					break;
				case 0xB5:	// putfield
					argument = (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF);
					commandPointer += 2;
					stackTop = store(clazz, inst, argument, stack, stackContent, stackTop);
					break;
				case 0xB6:	// invokevirtual
					argument = (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF);
					stackTop = invoke(clazz, inst, argument, code[commandPointer], stack, stackContent, stackTop);
					commandPointer += 2;
					break;
				case 0xB7:	// invokespecial
					argument = (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF);
					stackTop = invoke(clazz, inst, argument, code[commandPointer], stack, stackContent, stackTop);
					commandPointer += 2;
					break;
				case 0xB8:	// invokestatic
					argument = (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF);
					stackTop = invoke(clazz, inst, argument, code[commandPointer], stack, stackContent, stackTop);
					commandPointer += 2;
					break;
				case 0xB9:	// invokeinterface
					argument = (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF);
					stackTop = invoke(clazz, inst, argument, code[commandPointer], stack, stackContent, stackTop);
					commandPointer += 4;
					break;
// ------------------------------------------
				case 0xBA:	// invokedynamic
					throw new Error();
// ------------------------------------------
					
					
				case 0xBB:	// new
				case 0xBC:	// newarray
				case 0xBD:	// anewarray
				case 0xBE:	// arraylength
				case 0xBF:	// athrow
				case 0xC0:	// checkcast
				case 0xC1:	// instanceof
				case 0xC2:	// monitorenter
				case 0xC3:	// monitorexit
					throw new Error();
// ------------------------------------------
				case 0xC4:	// wide
					wideDetected = true;
					commandPointer++;
					continue loop;
// ------------------------------------------
				case 0xC5:	// multianewarray
					throw new Error();
// ------------------------------------------
				case 0xC6:	// ifnull
					if (stackContent[stackTop] == 'L' || stackContent[stackTop] == '[') {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF);
						commandPointer += 2;
						if (stack[stackTop] == 0) {
							commandPointer = argument - 1; 
						}
					}
					break;
				case 0xC7:	// ifnonnull
					if (stackContent[stackTop] == 'L' || stackContent[stackTop] == '[') {
						argument = (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF);
						commandPointer += 2;
						if (stack[stackTop] != 0) {
							commandPointer = argument - 1; 
						}
					}
					break;
				case 0xC8:	// goto_w
					argument = (code[commandPointer+1] << 24) | (code[commandPointer+2] << 16) | (code[commandPointer+3] << 8) | (code[commandPointer+4] & 0xFF);
					commandPointer = argument - 1;
					break;
// ------------------------------------------
				case 0xC9:	// jsr_w
					throw new Error();
// ------------------------------------------
				case 0xCA:	// break
				case 0xFE:	// impdep1
				case 0xFF:	// impdep2
					break;
				default :
					throw new Error();
			}
			if (wideDetected) {
				throw new Error();
			}
			else {
				commandPointer++;
			}
		}
		return value;
	}
	
	private static int loadConst(final ClassDescriptor clazz, final int index, final long[] stack, final byte[] stackContent, int stackTop, final boolean twice) {
		final ConstantPoolItem	item = clazz.getConstantPool().get(index);
		
		switch (item.itemType) {
			case DefinitionLoader.CONSTANT_Class	:
				stack[stackTop] = 0;//item.resolveClass(item.ref1);
				stackContent[stackTop] = 'L';
				stackTop++;
				break;
			case DefinitionLoader.CONSTANT_String	:
				stack[stackTop] = 0;//item.resolveString(item.ref1);
				stackContent[stackTop] = 'L';
				stackTop++;
				break;
			case DefinitionLoader.CONSTANT_Integer	:
				stack[stackTop] = item.ref1;
				stackContent[stackTop] = 'I';
				stackTop++;
				break;
			case DefinitionLoader.CONSTANT_Float	:
				stack[stackTop] = item.ref1;
				stackContent[stackTop] = 'F';
				stackTop++;
				break;
			case DefinitionLoader.CONSTANT_Long	:
				stackTop += 2;
				stack[stackTop - 2] = item.value;
				stackContent[stackTop - 2] = 'J';
				stack[stackTop - 1] = 0;
				stackContent[stackTop - 1] = '*';
				break;
			case DefinitionLoader.CONSTANT_Double	:
				stackTop += 2;
				stack[stackTop - 2] = item.value;
				stackContent[stackTop - 2] = 'D';
				stack[stackTop - 1] = 0;
				stackContent[stackTop] = '*';
				break;
			default :
				throw new Error();		
		}
		return stackTop;
	}
	
	private static int load(ClassDescriptor clazz, Object inst, int argument, long[] stack, byte[] stackContent, int stackTop) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private static int store(ClassDescriptor clazz, Object inst, int argument, long[] stack, byte[] stackContent, int stackTop) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static int invoke(ClassDescriptor clazz, Object inst, int argument, byte code, long[] stack, byte[] stackContent, int stackTop) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
	public static void main(String[] args) {
		
	}
}
