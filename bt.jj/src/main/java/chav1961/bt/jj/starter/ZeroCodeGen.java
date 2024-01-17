package chav1961.bt.jj.starter;

import java.util.Arrays;

public abstract class ZeroCodeGen {
	public static final String	TYPE_ARRAY = "[";
	public static final String	TYPE_BOOLEAN = "Z";
	public static final String	TYPE_BOOLEAN_ARRAY = "[Z";
	public static final String	TYPE_BYTE = "B";
	public static final String	TYPE_BYTE_ARRAY = "[B";
	public static final String	TYPE_CHAR = "C";
	public static final String	TYPE_CHAR_ARRAY = "[C";
	public static final String	TYPE_DOUBLE = "D";
	public static final String	TYPE_DOUBLE_ARRAY = "[D";
	public static final String	TYPE_FLOAT = "F";
	public static final String	TYPE_FLOAT_ARRAY = "[F";
	public static final String	TYPE_INT = "I";
	public static final String	TYPE_INT_ARRAY = "[I";
	public static final String	TYPE_LONG = "J";
	public static final String	TYPE_LONG_ARRAY = "[J";
	public static final String	TYPE_NULL = "N";
	public static final String	TYPE_CLASS = "L";
	public static final String	TYPE_SHORT = "S";
	public static final String	TYPE_SHORT_ARRAY = "[S";
	public static final String	TYPE_TAIL = "*";
	public static final String	TYPE_VOID = "V";

	public static final String	BRANCH_EQ = "==";
	public static final String	BRANCH_NE = "!=";
	public static final String	BRANCH_LT = "<";
	public static final String	BRANCH_GE = ">=";
	public static final String	BRANCH_GT = ">";
	public static final String	BRANCH_LE = "<=";
	
	public static final 		int	STACK_POP = 0;
	public static final 		int	STACK_POP2 = 1;
	public static final 		int	STACK_DUP = 2;
	public static final 		int	STACK_DUP_X1 = 3;
	public static final 		int	STACK_DUP_X2 = 4;
	public static final 		int	STACK_DUP2 = 5;
	public static final 		int	STACK_DUP2_X1 = 6;
	public static final 		int	STACK_DUP2_X2 = 7;
	public static final 		int	STACK_SWAP = 8;
	
	private static final String[]	INFIX_TYPES_1 = {TYPE_INT, TYPE_LONG, TYPE_FLOAT, TYPE_DOUBLE}; 
	private static final char[]		INFIX_OPS_1 = {'+', '-', '*', '/', '%'}; 
	private static final String[]	INFIX_TYPES_2 = {TYPE_INT, TYPE_LONG}; 
	private static final char[]		INFIX_OPS_2 = {'<', '>', 'U', '&', '|', '^'}; 
	private static final String[]	CONVERT_INT = {TYPE_LONG, TYPE_FLOAT, TYPE_DOUBLE}; 
	private static final String[]	CONVERT_LONG = {TYPE_INT, TYPE_FLOAT, TYPE_DOUBLE}; 
	private static final String[]	CONVERT_FLOAT = {TYPE_INT, TYPE_LONG, TYPE_DOUBLE}; 
	private static final String[]	CONVERT_DOUBLE = {TYPE_INT, TYPE_LONG, TYPE_FLOAT}; 
	private static final String[]	CONVERT_INT_SPECIAL = {TYPE_BYTE, TYPE_CHAR, TYPE_SHORT}; 
	private static final String[]	BRANCH_TYPE = {BRANCH_EQ, BRANCH_NE, BRANCH_LT, BRANCH_GE, BRANCH_GT, BRANCH_LE}; 

	protected final String[]			localsContent;
	protected final String[]			stackContent;
	protected final ConstantPoolItem[]	cp;
	protected int		stackTop = 1;
	private int[][]		jumps = null;
	private int			jumpsIndex;
	
	public ZeroCodeGen(final int localSize, final int stackSize, final ConstantPoolItem[] cp) {
		this.localsContent = new String[localSize];
		this.stackContent = new String[stackSize];
		this.cp = cp;
	}

	protected abstract int makePrefix(final byte[] generated, int where, final int localSize);
	protected abstract int loadImmediate(final byte[] generated, int where, final long value, final String type);
	protected abstract int loadImmediateF(final byte[] generated, int where, final double value, final String type);
	protected abstract int loadLocal(final byte[] generated, final int where, final int argument, final String type);
	protected abstract int storeLocal(final byte[] generated, final int where, final int argument, final String type);
	protected abstract int loadLocalA(final byte[] generated, final int where, final int argument, final String type);
	protected abstract int storeLocalA(final byte[] generated, final int where, final int argument, String type);
	protected abstract int loadLocalF(final byte[] generated, final int where, final int argument, final String type);
	protected abstract int storeLocalF(final byte[] generated, final int where, final int argument, final String type);
	protected abstract int loadLocalAF(final byte[] generated, final int where, final int argument, final String type);
	protected abstract int storeLocalAF(final byte[] generated, final int where, final int argument, String type);
	protected abstract int stack(final byte[] generated, final int where, final int action);
	protected abstract int infix(final byte[] generated, final int where, final String type, final char oper);
	protected abstract int prefix(final byte[] generated, final int where, final String type);
	protected abstract int increment(final byte[] generated, final int where, final int argument, final String type, final int value);
	protected abstract int compareLong(final byte[] generated, final int where);
	protected abstract int compareDouble(final byte[] generated, final int where, final String typeFloat, final boolean nanProcessing);
	protected abstract int convert(final byte[] generated, final int where, final String typeFrom, final String typeTo);
	protected abstract int conditionalBranch(final byte[] generated, final int where, final String operation, final int branchAddress, final boolean needSubtract);
	protected abstract int conditionalBranchLong(final byte[] generated, final int where, final int branchAddress, final boolean equals, final boolean needCompare);
	protected abstract int branch(final byte[] generated, final int where, final int branchAddress);
	protected abstract int jsr(final byte[] generated, final int where, final int branchAddress);
	protected abstract int ret(final byte[] generated, final int genPointer);
	protected abstract int tableSwitch(final byte[] generated, final int where, final byte[] code, final int commandPointer);
	protected abstract int lookupSwitch(final byte[] generated, final int where, final byte[] code, final int commandPointer);
	protected abstract int prepareReturn(final byte[] generated, final int where, String typeInt);
	protected abstract int makeReturn(final byte[] generated, final int where);
	protected abstract int getField(final byte[] generated, final int where, final int argument, final boolean isInstanceField);
	protected abstract int putField(final byte[] generated, final int where, final int argument, final boolean isInstanceField);
	protected abstract int invoke(final byte[] generated, final int where, final int argument, final boolean useThis, final boolean useVMT, final boolean staticVMT);
	protected abstract int invokeDynamic(final byte[] generated, final int where, final int argument);
	protected abstract int monitor(final byte[] generated, final int where, final boolean monitorEnter);
	protected abstract int allocate(final byte[] generated, final int where, final int argument);
	protected abstract int allocatePrimitiveArray(final byte[] generated, final int where, final int argument);
	protected abstract int allocateArray(final byte[] generated, final int where, final int argument);
	protected abstract int arrayLength(final byte[] generated, final int where);
	protected abstract int makeThrow(final byte[] generated, final int where);
	protected abstract int makeCheckCast(final byte[] generated, final int where, final int argument);
	protected abstract int makeInstanceOf(final byte[] generated, final int where, final int argument);
	protected abstract int allocateMultiArray(final byte[] generated, final int where, final int argument, final int dimensions);
	protected abstract int makePostfix(final byte[] generated, final int where);
	
	public byte[] compile(final byte[] code) {
		final byte[]	generated = new byte[100000];
		final int[]		displ2Line = new int[code.length];
		int				commandPointer = 0, genPointer = 0, argument = 0, value; 
		boolean			wideDetected = false;

		jumps = new int[100][];
		jumpsIndex = 0;
		genPointer = makePrefix(generated, genPointer, localsContent.length);
loop:	while(commandPointer < code.length) {
			displ2Line[commandPointer] = genPointer;
			switch ((short)code[commandPointer]) {
				case 0x00: // nop
					break;
				case 0x01: // aconst_null
					genPointer = loadImmediate(generated, genPointer, 0, TYPE_NULL);
					stackContent[stackTop++] = TYPE_NULL;
					break;
				case 0x02: // iconst_m1
				case 0x03: // iconst_0
				case 0x04: // iconst_1
				case 0x05: // iconst_2
				case 0x06: // iconst_3
				case 0x07: // iconst_4
				case 0x08: // iconst_5
					genPointer = loadImmediate(generated, genPointer, code[commandPointer] - 0x03, TYPE_INT);
					break;
				case 0x09: // lconst_0
				case 0x0A: // lconst_1
					genPointer = loadImmediate(generated, genPointer, code[commandPointer] - 0x09, TYPE_LONG);
					break;
				case 0x0B: // fconst_0
				case 0x0C: // fconst_1
				case 0x0D: // fconst_2
					genPointer = loadImmediateF(generated, genPointer, Double.longBitsToDouble(code[commandPointer] - 0x0B), TYPE_FLOAT);
					break;
				case 0x0E: // dconst_0
				case 0x0F: // dconst_1
					genPointer = loadImmediateF(generated, genPointer, Double.longBitsToDouble(code[commandPointer] - 0x0E), TYPE_DOUBLE);
					break;
				case 0x10: // bipush
					genPointer = loadImmediate(generated, genPointer, code[commandPointer + 1], TYPE_INT);
					commandPointer++;
					break;
				case 0x11: // sipush
					genPointer = loadImmediate(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF), TYPE_INT);
					commandPointer += 2;
					break;
				case 0x12: // ldc
					argument = code[commandPointer + 1];
					switch (cp[argument].itemType) {
						case DefinitionLoader.CONSTANT_Integer	:
							genPointer = loadImmediate(generated, genPointer, cp[argument].value, TYPE_INT);
							break;
						case DefinitionLoader.CONSTANT_Float	:
							genPointer = loadImmediateF(generated, genPointer, cp[argument].value, TYPE_FLOAT);
							break;
						default :
							throw new Error("Illegal const ref");
					}
					commandPointer++;
					break;
				case 0x13: // ldc_w
					argument = (code[commandPointer + 1] << 8) | (code[commandPointer + 2] & 0xFF);
					switch (cp[argument].itemType) {
						case DefinitionLoader.CONSTANT_Integer	:
							genPointer = loadImmediate(generated, genPointer, cp[argument].value, TYPE_INT);
							break;
						case DefinitionLoader.CONSTANT_Float	:
							genPointer = loadImmediateF(generated, genPointer, cp[argument].value, TYPE_FLOAT);
							break;
						default :
							throw new Error("Illegal const ref");
					}
					commandPointer += 2;
					break;
				case 0x14: // ldc2_w
					argument = (code[commandPointer + 1] << 8) | (code[commandPointer + 2] & 0xFF);
					switch (cp[argument].itemType) {
						case DefinitionLoader.CONSTANT_Long	:
							genPointer = loadImmediate(generated, genPointer, cp[argument].value, TYPE_LONG);
							break;
						case DefinitionLoader.CONSTANT_Double	:
							genPointer = loadImmediateF(generated, genPointer, cp[argument].value, TYPE_DOUBLE);
							break;
						default :
							throw new Error("Illegal const ref");
					}
					commandPointer += 2;
					break;
				case 0x15: // iload
					if (wideDetected) {
						argument = ((code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF));
						commandPointer += 2;
						wideDetected = false;
					}
					else {
						argument = (code[commandPointer+2] & 0xFF);
						commandPointer++;
					}
				case 0x1D: // iload_3
					argument++;
				case 0x1C: // iload_2
					argument++;
				case 0x1B: // iload_1
					argument++;
				case 0x1A: // iload_0
					if (localsContent[argument].equals(TYPE_INT)) {
						genPointer = loadLocal(generated, genPointer, argument, TYPE_INT);
					}
					else {
						throw new Error("Local integer not initialized");
					}
					break;
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
					if (localsContent[argument].equals(TYPE_LONG) && localsContent[argument + 1].equals(TYPE_TAIL)) {
						genPointer = loadLocal(generated, genPointer, argument, TYPE_LONG);
					}
					else {
						throw new Error("Local long not initialized");
					}
				case 0x17: // fload
					if (wideDetected) {
						argument = ((code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF));
						commandPointer += 2;
						wideDetected = false;
					}
					else {
						argument = (code[commandPointer+2] & 0xFF);
						commandPointer++;
					}
				case 0x25: // fload_3
					argument++;
				case 0x24: // fload_2
					argument++;
				case 0x23: // fload_1
					argument++;
				case 0x22: // fload_0
					if (localsContent[argument].equals(TYPE_FLOAT)) {
						genPointer = loadLocalF(generated, genPointer, argument, TYPE_FLOAT);
					}
					else {
						throw new Error("Local float not initialized");
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
					if (localsContent[argument].equals(TYPE_DOUBLE) && localsContent[argument + 1].equals(TYPE_TAIL)) {
						genPointer = loadLocalF(generated, genPointer, argument, TYPE_DOUBLE);
					}
					else {
						throw new Error("Local double not initialized");
					}
					break;
				case 0x19: // aload
					if (wideDetected) {
						argument = ((code[commandPointer + 1] << 8) | (code[commandPointer + 2] & 0xFF));
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
					if (localsContent[argument].startsWith(TYPE_CLASS) || localsContent[argument].startsWith(TYPE_ARRAY)) {
						genPointer = loadLocal(generated, genPointer, argument, TYPE_CLASS);
					}
					else {
						throw new Error("Local reference not initialized");
					}
					break;
				case 0x2E: // iaload
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].equals(TYPE_INT_ARRAY)) {
						genPointer = loadLocalA(generated, genPointer, argument, TYPE_INT);
					}
					else {
						throw new Error("Int array ref is missing on the stack");
					}
					break;
				case 0x2F: // laload
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].equals(TYPE_LONG_ARRAY)) {
						genPointer = loadLocalA(generated, genPointer, argument, TYPE_LONG);
					}
					else {
						throw new Error("Int array ref is missing on the stack");
					}
					break;
				case 0x30: // faload
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].equals(TYPE_FLOAT_ARRAY)) {
						genPointer = loadLocalAF(generated, genPointer, argument, TYPE_FLOAT);
					}
					else {
						throw new Error("Int array ref is missing on the stack");
					}
					break;
				case 0x31: // daload
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].equals(TYPE_DOUBLE_ARRAY)) {
						genPointer = loadLocalAF(generated, genPointer, argument, TYPE_DOUBLE);
					}
					else {
						throw new Error("Int array ref is missing on the stack");
					}
					break;
				case 0x32: // aaload
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].startsWith(TYPE_ARRAY)) {
						genPointer = loadLocalA(generated, genPointer, argument, TYPE_CLASS);
					}
					else {
						throw new Error("Referenced array ref is missing on the stack");
					}
					break;
				case 0x33: // baload
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].startsWith(TYPE_BYTE_ARRAY)) {
						genPointer = loadLocalA(generated, genPointer, argument, TYPE_BYTE);
					}
					else {
						throw new Error("Byte array ref is missing on the stack");
					}
					break;
				case 0x34: // caload
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].startsWith(TYPE_CHAR_ARRAY)) {
						genPointer = loadLocalA(generated, genPointer, argument, TYPE_CHAR);
					}
					else {
						throw new Error("Char array ref is missing on the stack");
					}
					break;
				case 0x35: // saload
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].startsWith(TYPE_SHORT_ARRAY)) {
						genPointer = loadLocalA(generated, genPointer, argument, TYPE_SHORT);
					}
					else {
						throw new Error("Char array ref is missing on the stack");
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
					if (stackContent[stackTop].equals(TYPE_INT)) {
						genPointer = storeLocal(generated, genPointer, argument, TYPE_INT);
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
					if (stackContent[stackTop - 1].equals(TYPE_LONG) && stackContent[stackTop].equals(TYPE_TAIL)) {
						genPointer = storeLocal(generated, genPointer, argument, TYPE_LONG);
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
					if (stackContent[stackTop].equals(TYPE_FLOAT)) {
						genPointer = storeLocalF(generated, genPointer, argument, TYPE_FLOAT);
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
					if (stackContent[stackTop - 1].equals(TYPE_DOUBLE) && stackContent[stackTop].equals(TYPE_TAIL)) {
						genPointer = storeLocalF(generated, genPointer, argument, TYPE_DOUBLE);
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
					if (stackContent[stackTop].startsWith(TYPE_ARRAY) || stackContent[stackTop].startsWith(TYPE_CLASS)) {
						genPointer = storeLocal(generated, genPointer, argument, stackContent[stackTop]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x4F: // iastore
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].equals(TYPE_INT) && stackContent[stackTop - 2].equals(TYPE_INT_ARRAY)) {
						genPointer = storeLocalA(generated, genPointer, argument, TYPE_INT);
					}
					else {
						throw new Error();
					}
					break;
				case 0x50: // lastore
					if (stackContent[stackTop].equals(TYPE_TAIL) && stackContent[stackTop - 1].equals(TYPE_LONG) && stackContent[stackTop - 2].equals(TYPE_INT) && stackContent[stackTop - 3].equals(TYPE_LONG_ARRAY)) {
						genPointer = storeLocalA(generated, genPointer, argument, TYPE_LONG);
					}
					else {
						throw new Error();
					}
					break;
				case 0x51: // fastore
					if (stackContent[stackTop].equals(TYPE_FLOAT) && stackContent[stackTop - 1].equals(TYPE_INT) && stackContent[stackTop - 2].equals(TYPE_FLOAT_ARRAY)) {
						genPointer = storeLocalAF(generated, genPointer, argument, TYPE_FLOAT);
					}
					else {
						throw new Error();
					}
					break;
				case 0x52: // dastore
					if (stackContent[stackTop].equals(TYPE_TAIL) && stackContent[stackTop - 1].equals(TYPE_DOUBLE) && stackContent[stackTop - 2].equals(TYPE_INT) && stackContent[stackTop - 3].equals(TYPE_DOUBLE_ARRAY)) {
						genPointer = storeLocalAF(generated, genPointer, argument, TYPE_DOUBLE);
					}
					else {
						throw new Error();
					}
					break;
				case 0x53: // aastore
					if ((stackContent[stackTop].startsWith(TYPE_ARRAY) || stackContent[stackTop].startsWith(TYPE_CLASS)) && stackContent[stackTop - 1].equals(TYPE_INT) && stackContent[stackTop - 2].startsWith(TYPE_ARRAY) && stackContent[stackTop].equals(stackContent[stackTop - 2].substring(1))) {
						genPointer = storeLocalA(generated, genPointer, argument, stackContent[stackTop]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x54: // bastore
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].equals(TYPE_INT) && stackContent[stackTop - 2].equals(TYPE_BYTE_ARRAY)) {
						genPointer = storeLocalA(generated, genPointer, argument, TYPE_BYTE);
					}
					else {
						throw new Error();
					}
					break;
				case 0x55: // castore
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].equals(TYPE_INT) && stackContent[stackTop - 2].equals(TYPE_CHAR_ARRAY)) {
						genPointer = storeLocalA(generated, genPointer, argument, TYPE_CHAR);
					}
					else {
						throw new Error();
					}
					break;
				case 0x56: // sastore
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].equals(TYPE_INT) && stackContent[stackTop - 2].equals(TYPE_SHORT_ARRAY)) {
						genPointer = storeLocalA(generated, genPointer, argument, TYPE_SHORT);
					}
					else {
						throw new Error();
					}
					break;
				case 0x57: // pop
				case 0x58: // pop2
				case 0x59: // dup
				case 0x5A: // dup_x1
				case 0x5B: // dup_x2
				case 0x5C: // dup2
				case 0x5D: // dup2_x1
				case 0x5E: // dup2_x2
				case 0x5F: // swap
					genPointer = stack(generated, genPointer, code[commandPointer] - 0x57);
					break;
				case 0x60:	// iadd
				case 0x64:	// isub
				case 0x68:	// imul
				case 0x6C:	// idiv
				case 0x70:	// irem
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].equals(TYPE_INT)) {
						genPointer = infix(generated, genPointer, INFIX_TYPES_1[code[commandPointer] & 0x03], INFIX_OPS_1[(code[commandPointer] >>2 ) % 12]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x61:	// ladd
				case 0x65:	// lsub
				case 0x69:	// lmul
				case 0x6D:	// ldiv
				case 0x71:	// lrem
					if (stackContent[stackTop].equals(TYPE_TAIL) && stackContent[stackTop - 1].equals(TYPE_LONG) && stackContent[stackTop - 2].equals(TYPE_TAIL) && stackContent[stackTop - 3].equals(TYPE_LONG)) {
						genPointer = infix(generated, genPointer, INFIX_TYPES_1[code[commandPointer] & 0x03], INFIX_OPS_1[(code[commandPointer] >>2 ) % 12]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x62:	// fadd
				case 0x66:	// fsub
				case 0x6A:	// fmul
				case 0x6E:	// fdiv
				case 0x72:	// frem
					if (stackContent[stackTop].equals(TYPE_FLOAT) && stackContent[stackTop - 1].equals(TYPE_FLOAT)) {
						genPointer = infix(generated, genPointer, INFIX_TYPES_1[code[commandPointer] & 0x03], INFIX_OPS_1[(code[commandPointer] >>2 ) % 12]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x63:	// dadd
				case 0x67:	// dsub
				case 0x6B:	// dmul
				case 0x6F:	// ddiv
				case 0x73:	// drem
					if (stackContent[stackTop].equals(TYPE_TAIL) && stackContent[stackTop - 1].equals(TYPE_DOUBLE) && stackContent[stackTop - 2].equals(TYPE_TAIL) && stackContent[stackTop - 3].equals(TYPE_DOUBLE)) {
						genPointer = infix(generated, genPointer, INFIX_TYPES_1[code[commandPointer] & 0x03], INFIX_OPS_1[(code[commandPointer] >>2 ) % 12]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x74:	// ineg
					if (stackContent[stackTop].equals(TYPE_INT)) {
						genPointer = prefix(generated, genPointer, TYPE_INT);
					}
					else {
						throw new Error();
					}
					break;
				case 0x75:	// lneg
					if (stackContent[stackTop].equals(TYPE_TAIL) && stackContent[stackTop - 1].equals(TYPE_LONG)) {
						genPointer = prefix(generated, genPointer, TYPE_LONG);
					}
					else {
						throw new Error();
					}
					break;
				case 0x76:	// fneg
					if (stackContent[stackTop].equals(TYPE_FLOAT)) {
						genPointer = prefix(generated, genPointer, TYPE_FLOAT);
					}
					else {
						throw new Error();
					}
					break;
				case 0x77:	// dneg
					if (stackContent[stackTop].equals(TYPE_TAIL) && stackContent[stackTop - 1].equals(TYPE_DOUBLE)) {
						genPointer = prefix(generated, genPointer, TYPE_DOUBLE);
					}
					else {
						throw new Error();
					}
					break;
				case 0x78:	// ishl
				case 0x7A:	// ishr
				case 0x7C:	// iushr
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].equals(TYPE_INT)) {
						genPointer = infix(generated, genPointer, INFIX_TYPES_2[code[commandPointer] & 0x01], INFIX_OPS_2[(code[commandPointer] >>2 ) % 6]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x79:	// lshl
				case 0x7B:	// lshr
				case 0x7D:	// lushr
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].equals(TYPE_TAIL) && stackContent[stackTop - 2].equals(TYPE_LONG)) {
						genPointer = infix(generated, genPointer, INFIX_TYPES_2[code[commandPointer] & 0x01], INFIX_OPS_2[(code[commandPointer] >>2 ) % 6]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x7E:	// iand
				case 0x80:	// ior
				case 0x82:	// ixor
					if (stackContent[stackTop].equals(TYPE_INT) && stackContent[stackTop - 1].equals(TYPE_INT)) {
						genPointer = infix(generated, genPointer, INFIX_TYPES_2[code[commandPointer] & 0x01], INFIX_OPS_2[(code[commandPointer] >>2 ) % 6]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x7F:	// land
				case 0x81:	// lor
				case 0x83:	// lxor
					if (stackContent[stackTop].equals(TYPE_TAIL) && stackContent[stackTop - 1].equals(TYPE_LONG) && stackContent[stackTop - 2].equals(TYPE_TAIL) && stackContent[stackTop - 3].equals(TYPE_LONG)) {
						genPointer = infix(generated, genPointer, INFIX_TYPES_2[code[commandPointer] & 0x01], INFIX_OPS_2[(code[commandPointer] >>2 ) % 6]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x84:	// iinc
					if (wideDetected) {
						argument = (code[commandPointer + 1] << 8) | (code[commandPointer + 2] & 0xFF);
						value = (code[commandPointer + 3] << 8) | (code[commandPointer + 4] & 0xFF);
						commandPointer += 4;
						wideDetected = false;
					}
					else {
						argument = code[commandPointer + 1];
						value = code[commandPointer + 2];
						commandPointer += 2;
					}
					genPointer = increment(generated, genPointer, argument, localsContent[argument], value);
					break;
				case 0x85:	// i2l
				case 0x86:	// i2f
				case 0x87:	// i2d
					if (stackContent[stackTop].equals(TYPE_INT)) {
						genPointer = convert(generated, genPointer, TYPE_INT, CONVERT_INT[code[commandPointer] - 0x85]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x88:	// l2i
				case 0x89:	// l2f
				case 0x8A:	// l2d
					if (stackContent[stackTop].equals(TYPE_TAIL) && stackContent[stackTop].equals(TYPE_LONG)) {
						genPointer = convert(generated, genPointer, TYPE_LONG, CONVERT_LONG[code[commandPointer] - 0x88]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x8B:	// f2i
				case 0x8C:	// f2l
				case 0x8D:	// f2d
					if (stackContent[stackTop].equals(TYPE_FLOAT)) {
						genPointer = convert(generated, genPointer, TYPE_FLOAT, CONVERT_FLOAT[code[commandPointer] - 0x8B]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x8E:	// d2i
				case 0x8F:	// d2l
				case 0x90:	// d2f
					if (stackContent[stackTop].equals(TYPE_TAIL) && stackContent[stackTop].equals(TYPE_DOUBLE)) {
						genPointer = convert(generated, genPointer, TYPE_LONG, CONVERT_DOUBLE[code[commandPointer] - 0x8E]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x91:	// i2b
				case 0x92:	// i2c
				case 0x93:	// i2s
					if (stackContent[stackTop].equals(TYPE_INT)) {
						genPointer = convert(generated, genPointer, TYPE_INT, CONVERT_INT_SPECIAL[code[commandPointer] - 0x91]);
					}
					else {
						throw new Error();
					}
					break;
				case 0x94:	// lcmp
					if (stackContent[stackTop].equals(TYPE_TAIL) && stackContent[stackTop - 1].equals(TYPE_LONG) && stackContent[stackTop - 2].equals(TYPE_TAIL) && stackContent[stackTop - 3].equals(TYPE_LONG)) {
						genPointer = compareLong(generated, genPointer);
					}
					else {
						throw new Error();
					}
					break;
				case 0x95:	// fcmpl
				case 0x96:	// fcmpg
					if (stackContent[stackTop].equals(TYPE_FLOAT) && stackContent[stackTop - 1].equals(TYPE_FLOAT)) {
						genPointer = compareDouble(generated, genPointer, TYPE_FLOAT, code[commandPointer] == 0x95);
					}
					else {
						throw new Error();
					}
					break;
				case 0x97:	// dcmpl
				case 0x98:	// dcmpg
					if (stackContent[stackTop].equals(TYPE_TAIL) && stackContent[stackTop - 1].equals(TYPE_DOUBLE) && stackContent[stackTop - 2].equals(TYPE_TAIL) && stackContent[stackTop - 3].equals(TYPE_DOUBLE)) {
						genPointer = compareDouble(generated, genPointer, TYPE_DOUBLE, code[commandPointer] == 0x95);
					}
					else {
						throw new Error();
					}
					break;
				case 0x99:	// ifeq
				case 0x9A:	// ifne
				case 0x9B:	// iflt
				case 0x9C:	// ifge
				case 0x9D:	// ifgt
				case 0x9E:	// ifle
					if (stackContent[stackTop].equals(TYPE_INT)) {
						genPointer = conditionalBranch(generated, genPointer, BRANCH_TYPE[code[commandPointer] - 0x99], (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF), false);
						commandPointer += 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0x9F:	// if_icmpeq
				case 0xA0:	// if_icmpne
				case 0xA1:	// if_icmplt
				case 0xA2:	// if_icmpge
				case 0xA3:	// if_icmpgt
				case 0xA4:	// if_icmple
					if (stackContent[stackTop].equals(TYPE_INT)) {
						genPointer = conditionalBranch(generated, genPointer, BRANCH_TYPE[code[commandPointer] - 0x99], (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF), true);
						commandPointer += 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0xA5:	// if_acmpeq
					if ((stackContent[stackTop].startsWith(TYPE_CLASS) || stackContent[stackTop].startsWith(TYPE_ARRAY) || stackContent[stackTop].equals(TYPE_NULL)) && (stackContent[stackTop - 1].startsWith(TYPE_CLASS) || stackContent[stackTop - 1].startsWith(TYPE_ARRAY) || stackContent[stackTop - 1].equals(TYPE_NULL))) {
						genPointer = conditionalBranchLong(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF), true, true);
						commandPointer += 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0xA6:	// if_acmpne
					if ((stackContent[stackTop].startsWith(TYPE_CLASS) || stackContent[stackTop].startsWith(TYPE_ARRAY) || stackContent[stackTop].equals(TYPE_NULL)) && (stackContent[stackTop - 1].startsWith(TYPE_CLASS) || stackContent[stackTop - 1].startsWith(TYPE_ARRAY) || stackContent[stackTop - 1].equals(TYPE_NULL))) {
						genPointer = conditionalBranchLong(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+1] & 0xFF), false, true);
						commandPointer += 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0xA7:	// goto
					genPointer = branch(generated, genPointer, (code[commandPointer + 1] << 8) | (code[commandPointer + 1] & 0xFF));
					commandPointer += 2;
					break;
				case 0xA8:	// jsr
					genPointer = jsr(generated, genPointer, (code[commandPointer + 1] << 8) | (code[commandPointer + 1] & 0xFF));
					commandPointer += 2;
					break;
				case 0xA9:	// ret
					genPointer = ret(generated, genPointer);
					commandPointer += 2;
					break;
				case 0xAA:	// tableswitch
					if (stackContent[stackTop].equals(TYPE_INT)) {
						while((commandPointer % 0x03) != 0) {
							commandPointer++;
						}
						genPointer = tableSwitch(generated, genPointer, code, commandPointer);
						commandPointer = calculateTableSwitchSize(code, commandPointer);
					}
					else {
						throw new Error();
					}
					break;
				case 0xAB:	// lookupswitch
					if (stackContent[stackTop].equals(TYPE_INT)) {
						while((commandPointer % 0x03) != 0) {
							commandPointer++;
						}
						genPointer = lookupSwitch(generated, genPointer, code, commandPointer);
						commandPointer = calculateLookupSwitchSize(code, commandPointer);
					}
					else {
						throw new Error();
					}
					break;
				case 0xAC:	// ireturn
					if (stackContent[stackTop].equals(TYPE_INT)) {
						genPointer = prepareReturn(generated, genPointer, TYPE_INT);
						genPointer = makeReturn(generated, genPointer);
					}
					else {
						throw new Error();
					}
					break;
				case 0xAD:	// lreturn
					if (stackContent[stackTop].equals(TYPE_TAIL) && stackContent[stackTop-1].equals(TYPE_LONG)) {
						genPointer = prepareReturn(generated, genPointer, TYPE_LONG);
						genPointer = makeReturn(generated, genPointer);
					}
					else {
						throw new Error();
					}
				case 0xAE:	// freturn
					if (stackContent[stackTop].equals(TYPE_INT)) {
						genPointer = prepareReturn(generated, genPointer, TYPE_FLOAT);
						genPointer = makeReturn(generated, genPointer);
					}
					else {
						throw new Error();
					}
					break;
				case 0xAF:	// dreturn
					if (stackContent[stackTop].equals(TYPE_TAIL) && stackContent[stackTop-1].equals(TYPE_DOUBLE)) {
						genPointer = prepareReturn(generated, genPointer, TYPE_DOUBLE);
						genPointer = makeReturn(generated, genPointer);
					}
					else {
						throw new Error();
					}
				case 0xB0:	// areturn
					if (stackContent[stackTop].startsWith(TYPE_ARRAY) || stackContent[stackTop].startsWith(TYPE_CLASS) || stackContent[stackTop].equals(TYPE_NULL)) {
						genPointer = prepareReturn(generated, genPointer, TYPE_CLASS);
						genPointer = makeReturn(generated, genPointer);
					}
					else {
						throw new Error();
					}
					break;
				case 0xB1:	// return
					genPointer = makeReturn(generated, genPointer);
					break;
				case 0xB2:	// getstatic
					genPointer = getField(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF), false); 
					commandPointer += 2;
					break;
				case 0xB3:	// putstatic
					genPointer = putField(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF), false); 
					commandPointer += 2;
					break;
				case 0xB4:	// getfield
					if (stackContent[stackTop].startsWith(TYPE_CLASS) || stackContent[stackTop].equals(TYPE_NULL)) {
						genPointer = getField(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF), true); 
						commandPointer += 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0xB5:	// putfield
					genPointer = putField(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF), true); 
					commandPointer += 2;
					break;
				case 0xB6:	// invokevirtual
					genPointer = invoke(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF), true, true, false); 
					commandPointer += 2;
					break;
				case 0xB7:	// invokespecial
					genPointer = invoke(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF), true, false, false); 
					commandPointer += 2;
					break;
				case 0xB8:	// invokestatic
					genPointer = invoke(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF), false, false, false); 
					commandPointer += 2;
					break;
				case 0xB9:	// invokeinterface
					genPointer = invoke(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF), true, true, true); 
					commandPointer += 2;
					break;
				case 0xBA:	// invokedynamic
					genPointer = invokeDynamic(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF)); 
					commandPointer += 2;
					break;
				case 0xBB:	// new
					genPointer = allocate(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF));
					commandPointer += 2;
					break;
				case 0xBC:	// newarray
					if (stackContent[stackTop].equals(TYPE_INT)) {
						genPointer = allocatePrimitiveArray(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF)); 
						commandPointer += 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0xBD:	// anewarray
					if (stackContent[stackTop].equals(TYPE_INT)) {
						genPointer = allocateArray(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF)); 
						commandPointer += 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0xBE:	// arraylength
					if (stackContent[stackTop].startsWith(TYPE_ARRAY)) {
						genPointer = arrayLength(generated, genPointer); 
					}
					else {
						throw new Error();
					}
					break;
				case 0xBF:	// athrow
					if (stackContent[stackTop].startsWith(TYPE_CLASS)) {
						genPointer = makeThrow(generated, genPointer); 
					}
					else {
						throw new Error();
					}
					break;
				case 0xC0:	// checkcast
					if (stackContent[stackTop].startsWith(TYPE_CLASS) || stackContent[stackTop].startsWith(TYPE_ARRAY) || stackContent[stackTop].equals(TYPE_NULL)) {
						genPointer = makeCheckCast(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF)); 
						commandPointer += 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0xC1:	// instanceof
					if (stackContent[stackTop].startsWith(TYPE_CLASS) || stackContent[stackTop].startsWith(TYPE_ARRAY) || stackContent[stackTop].equals(TYPE_NULL)) {
						genPointer = makeInstanceOf(generated, genPointer, (code[commandPointer+1] << 8) | (code[commandPointer+2] & 0xFF)); 
						commandPointer += 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0xC2:	// monitorenter
					if (stackContent[stackTop].startsWith(TYPE_CLASS) || stackContent[stackTop].startsWith(TYPE_ARRAY) || stackContent[stackTop].equals(TYPE_NULL)) {
						genPointer = monitor(generated, genPointer, true); 
					}					
					else {
						throw new Error();
					}
					break;
				case 0xC3:	// monitorexit
					if (stackContent[stackTop].startsWith(TYPE_CLASS) || stackContent[stackTop].startsWith(TYPE_ARRAY) || stackContent[stackTop].equals(TYPE_NULL)) {
						genPointer = monitor(generated, genPointer, false); 
					}					
					else {
						throw new Error();
					}
					break;
				case 0xC4:	// wide
					wideDetected = true;
					commandPointer++;
					continue loop;
				case 0xC5:	// multianewarray
					final int	size = code[commandPointer + 2] & 0xFF;
					
					argument = (code[commandPointer + 1] << 8) | (code[commandPointer + 1] & 0xFF);
					
					for(int index = 0; index <  size; index++) {
						if (!stackContent[stackTop - index].equals(TYPE_INT)) {
							throw new Error();
						}
					}
					genPointer = allocateMultiArray(generated, genPointer, argument, size);
					commandPointer += 3;
					throw new Error();
				case 0xC6:	// ifnull
					if (stackContent[stackTop].startsWith(TYPE_CLASS) || stackContent[stackTop].startsWith(TYPE_ARRAY) || stackContent[stackTop].equals(TYPE_NULL)) {
						genPointer = conditionalBranchLong(generated, genPointer, (code[commandPointer + 1] << 8) | (code[commandPointer + 1] & 0xFF), true, false);
						commandPointer += 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0xC7:	// ifnonnull
					if (stackContent[stackTop].startsWith(TYPE_CLASS) || stackContent[stackTop].startsWith(TYPE_ARRAY) || stackContent[stackTop].equals(TYPE_NULL)) {
						genPointer = conditionalBranchLong(generated, genPointer, (code[commandPointer + 1] << 8) | (code[commandPointer + 1] & 0xFF), false, false);
						commandPointer += 2;
					}
					else {
						throw new Error();
					}
					break;
				case 0xC8:	// goto_w
					genPointer = branch(generated, genPointer, code[commandPointer + 1] << 24) | (code[commandPointer + 2] << 16) | (code[commandPointer + 3] << 8) | (code[commandPointer + 4] & 0xFF);
					commandPointer += 4;
					break;
				case 0xC9:	// jsr_w
					genPointer = jsr(generated, genPointer, code[commandPointer + 1] << 24) | (code[commandPointer + 2] << 16) | (code[commandPointer + 3] << 8) | (code[commandPointer + 4] & 0xFF);
					commandPointer += 4;
					break;
				case 0xCA:	// break
				case 0xFE:	// impdep1
				case 0xFF:	// impdep2
					break;
				default :
					throw new Error();
			}
		}
		genPointer = makePostfix(generated, genPointer);

		for(int index = 0; index < jumpsIndex; index++) {
			final int[]	jump = jumps[index];
			
		}
		
		return Arrays.copyOfRange(generated, 0, genPointer);
	}

	public static byte[] compile(final MethodItem item) {
		return null;
	}
	
	protected void push(final String type) {
		if (stackTop >= stackContent.length) {
			throw new Error("Stack overflow");
		}
		else {
			switch (type) {
				case TYPE_BOOLEAN		:
					stackContent[stackTop++] = TYPE_INT;
					break;
				case TYPE_BOOLEAN_ARRAY	:
					stackContent[stackTop++] = type;
					break;
				case TYPE_BYTE			:
					stackContent[stackTop++] = TYPE_INT;
					break;
				case TYPE_BYTE_ARRAY	:
					stackContent[stackTop++] = type;
					break;
				case TYPE_CHAR			:
					stackContent[stackTop++] = TYPE_INT;
					break;
				case TYPE_CHAR_ARRAY	:
					stackContent[stackTop++] = type;
					break;
				case TYPE_DOUBLE		:
					stackContent[stackTop++] = TYPE_DOUBLE;
					stackContent[stackTop++] = TYPE_TAIL;
					break;
				case TYPE_DOUBLE_ARRAY	:
					stackContent[stackTop++] = type;
					break;
				case TYPE_FLOAT			:
					stackContent[stackTop++] = TYPE_FLOAT;
					break;
				case TYPE_FLOAT_ARRAY	:
					stackContent[stackTop++] = type;
					break;
				case TYPE_INT			:
					stackContent[stackTop++] = TYPE_INT;
					break;
				case TYPE_INT_ARRAY		:
					stackContent[stackTop++] = type;
					break;
				case TYPE_LONG			:
					stackContent[stackTop++] = TYPE_LONG;
					stackContent[stackTop++] = TYPE_TAIL;
					break;
				case TYPE_LONG_ARRAY	:
					stackContent[stackTop++] = type;
					break;
				case TYPE_NULL			:
					stackContent[stackTop++] = TYPE_NULL;
					break;
				case TYPE_SHORT			:
					stackContent[stackTop++] = TYPE_INT;
					break;
				case TYPE_SHORT_ARRAY	:
					stackContent[stackTop++] = TYPE_NULL;
					break;
				case TYPE_TAIL			:
				case TYPE_VOID			:
					throw new Error();
				default :
					if (type.startsWith(TYPE_ARRAY) || type.startsWith(TYPE_CLASS)) {
						stackContent[stackTop++] = type;
					}
					else {
						throw new Error("Illegal type to push");
					}
			}
		}
	}

	protected String pop() {
		if (stackTop <= 0) {
			throw new Error("Stack exhausted");
		}
		else {
			switch (stackContent[stackTop]) {
				case TYPE_BOOLEAN		:
				case TYPE_BOOLEAN_ARRAY	:
				case TYPE_BYTE			:
				case TYPE_BYTE_ARRAY	:
				case TYPE_CHAR			:
				case TYPE_CHAR_ARRAY	:
				case TYPE_DOUBLE_ARRAY	:
				case TYPE_FLOAT			:
				case TYPE_FLOAT_ARRAY	:
				case TYPE_INT			:
				case TYPE_INT_ARRAY		:
				case TYPE_LONG_ARRAY	:
				case TYPE_NULL			:
				case TYPE_SHORT			:
				case TYPE_SHORT_ARRAY	:
					stackTop--;
					break;
				case TYPE_DOUBLE		:
				case TYPE_LONG			:
					stackTop -= 2;
					break;
				case TYPE_TAIL			:
				case TYPE_VOID			:
					throw new Error();
				default :
					stackTop--;
			}
			return stackContent[stackTop];
		}
	}

	protected void registerJump(final int codeLocation, final int size, final int byteCodeTarget) {
		if (jumpsIndex >= jumps.length) {
			jumps = Arrays.copyOf(jumps, 2 * jumps.length);
		}
		jumps[jumpsIndex++] = new int[] {codeLocation, size, byteCodeTarget};
	}
	
	private int calculateLookupSwitchSize(final byte[] code, final int commandPointer) {
		final int	nPairs = (code[commandPointer + 4] << 24) | (code[commandPointer + 5] << 16) | (code[commandPointer + 6] << 8)  | code[commandPointer + 7]; 
		
		return 3 * 4 + nPairs * 8;
	}

	private int calculateTableSwitchSize(final byte[] code, final int commandPointer) {
		final int	lowBytes = (code[commandPointer + 4] << 24) | (code[commandPointer + 5] << 16) | (code[commandPointer + 6] << 8)  | code[commandPointer + 7]; 
		final int	highBytes = (code[commandPointer + 8] << 24) | (code[commandPointer + 9] << 16) | (code[commandPointer + 10] << 8)  | code[commandPointer + 11];
		
		return 3 * 4 + (highBytes - lowBytes) * 4;
	}

}

