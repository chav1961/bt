package chav1961.bt.clipper.inner.vm;

import java.util.Arrays;

import chav1961.bt.clipper.ClipperRuntime;
import chav1961.bt.clipper.inner.AbstractBuiltinClipperFunction;
import chav1961.bt.clipper.inner.AbstractClipperFunction;
import chav1961.bt.clipper.inner.interfaces.ClipperBuiltinFunction;
import chav1961.bt.clipper.inner.interfaces.ClipperFunction;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.bt.clipper.inner.interfaces.StackFrame;
import chav1961.purelib.basic.exceptions.ContentException;

public class PCodeExecutor {
	public static final byte		CMD_NOP = 0;
	public static final byte		CMD_RET = 1;
	public static final byte		CMD_RETVAL = 2;
	public static final byte		CMD_CONST = 3;
	public static final byte		CMD_CONST2 = 4;
	public static final byte		CMD_CONST3 = 5;
	public static final byte		CMD_LOCAL = 6;
	public static final byte		CMD_LOCAL2 = 7;
	public static final byte		CMD_VAR = 8;
	public static final byte		CMD_STORE = 9;
	public static final byte		CMD_UNARY = 10;
	public static final byte		CMD_BINARY = 11;
	public static final byte		CMD_BUILTIN = 12;
	public static final byte		CMD_SPECIAL = 13;
	public static final byte		CMD_POP = 14;
	public static final byte		CMD_UNARY_TEST = 15;
	public static final byte		CMD_BINARY_TEST = 16;
	public static final byte		CMD_GOTO = 17;
	public static final byte		CMD_CALL = 18;
	
	public static final byte		OP_B_ADD = 1;
	public static final byte		OP_B_SUB = 2;
	public static final byte		OP_B_MUL = 3;
	public static final byte		OP_B_DIV = 4;
	public static final byte		OP_B_MOD = 5;
	public static final byte		OP_B_AND = 6;
	public static final byte		OP_B_OR = 7;
	public static final byte		OP_B_XOR = 8;
	
	public static final byte		OP_U_NEG = 1;
	public static final byte		OP_U_INV = 2;
	public static final byte		OP_U_NOT = 3;

	public static final byte		OP_BT_EQ = 1;
	public static final byte		OP_BT_NE = 2;
	public static final byte		OP_BT_GT = 3;
	public static final byte		OP_BT_GE = 4;
	public static final byte		OP_BT_LT = 5;
	public static final byte		OP_BT_LE = 6;

	public static final byte		OP_UT_EQZ = 1;
	public static final byte		OP_UT_NEZ = 2;
	public static final byte		OP_UT_GTZ = 3;
	public static final byte		OP_UT_GEZ = 4;
	public static final byte		OP_UT_LTZ = 5;
	public static final byte		OP_UT_LEZ = 6;
	public static final byte		OP_UT_TRUE = 7;
	public static final byte		OP_UT_FALSE = 8;
	
	private final ClipperRuntime	runtime;
	
	public PCodeExecutor(final ClipperRuntime runtime) {
		if (runtime == null) {
			throw new NullPointerException("Runtime can't be nul"); 
		}
		else {
			this.runtime = runtime;
		}
	}
	
	public ClipperRuntime getRuntime() {
		return runtime;
	}
	
	public ClipperValue process(final ClipperValue[] localStack, final StackFrame frame, final ConstantPool constants, final byte[] pcode, final int pc) {
		int	localSP = 0, localPC = pc;

		try{
			for (;;) {
				switch (pcode[localPC]) {
					case CMD_NOP 	:
						localPC++;
						break;
					case CMD_RET 	:
						return ClipperRuntime.NULL;
					case CMD_RETVAL	:
						return localStack[localSP];
					case CMD_CONST 	:
						localStack[localSP++] = constants.get(pcode[localPC+1]);
						localPC += 2;
						break;
					case CMD_CONST2	:
						localStack[localSP++] = constants.get((pcode[localPC+1] << 8) & 0xFF00 | (pcode[localPC+2] << 0) & 0xFF);
						localPC += 3;
						break;
					case CMD_CONST3	:
						localStack[localSP++] = constants.get((pcode[localPC+1] << 16) & 0xFF0000 | (pcode[localPC+2] << 8) & 0xFF00 | (pcode[localPC+3] << 0) & 0xFF);
						localPC += 4;
						break;
					case CMD_LOCAL 	:
						localStack[localSP++] = frame.getValueAssociated(frame.getEntity(-((pcode[localPC+1] << 0) & 0xFF)));
						localPC += 2;
						break;
					case CMD_LOCAL2	:
						localStack[localSP++] = frame.getValueAssociated(frame.getEntity(-((pcode[localPC+1] << 8) & 0xFF00 | (pcode[localPC+2] << 0) & 0xFF)));
						localPC += 3;
						break;
					case CMD_VAR	:
						localStack[localSP++] = frame.getValueAssociated(frame.getEntity((pcode[localPC+1] << 16) & 0xFF0000 | (pcode[localPC+2] << 8) & 0xFF00 | (pcode[localPC+3] << 0) & 0xFF));
						localPC += 4;
						break;
					case CMD_STORE	:
						if (localSP < 2) {
							throw new IllegalArgumentException(); 
						}
						else {
							localStack[localSP-1].set(localStack[localSP]);
							localSP -= 2;
							localPC++;
						}
						break;
					case CMD_UNARY	:
						if (localSP < 1) {
							throw new IllegalArgumentException(); 
						}
						else {
							localStack[localSP] = processUnary(pcode[localPC+1], localStack[localSP]);
							localPC += 2;
						}
						break;
					case CMD_BINARY	:
						if (localSP < 2) {
							throw new IllegalArgumentException(); 
						}
						else {
							localStack[localSP-1] = processBinary(pcode[localPC+1], localStack[localSP-1], localStack[localSP]);
							localSP--;
							localPC += 2;
						}
						break;
					case CMD_BUILTIN	:
						final ClipperBuiltinFunction	abcf = getRuntime().getBuiltins()[(pcode[localPC+1] << 8) & 0xFF00 | (pcode[localPC+2] << 0) & 0xFF];
						final int						builtinStackSize = pcode[localPC+3];
						
						localStack[localSP-1] = abcf.invoke(Arrays.copyOfRange(localStack, localSP-builtinStackSize, localPC));
						break;
					case CMD_SPECIAL	:
						final ClipperBuiltinFunction	abcfS = getRuntime().getBuiltins()[(pcode[localPC+1] << 8) & 0xFF00 | (pcode[localPC+2] << 0) & 0xFF];
						final int						builtinStackSizeS = pcode[localPC+3];
						
						localStack[localSP-1] = abcfS.invoke(this, getRuntime(), frame, Arrays.copyOfRange(localStack, localSP-builtinStackSizeS, localPC));
						break;
					case CMD_POP		:
						if (localSP < 1) {
							throw new IllegalArgumentException(); 
						}
						else {
							localSP--;
						}
						break;
					case CMD_UNARY_TEST	:
						if (localSP < 1) {
							throw new IllegalArgumentException(); 
						}
						else {
							final boolean	result = testUnary(pcode[localPC+1], localStack[localSP]);
							final int		gotoAddress = (pcode[localPC+2] << 8) & 0xFF00 | (pcode[localPC+3] << 0) & 0xFF;
							
							localSP--;
							if (result) {
								localPC = gotoAddress;
							}
							else {
								localPC += 4;
							}
						}
						break;
					case CMD_BINARY_TEST	:
						if (localSP < 2) {
							throw new IllegalArgumentException(); 
						}
						else {
							final boolean	result = testBinary(pcode[localPC+1], localStack[localSP-1], localStack[localSP]);
							final int		gotoAddress = (pcode[localPC+2] << 8) & 0xFF00 | (pcode[localPC+3] << 0) & 0xFF;
							
							localSP -= 2;
							if (result) {
								localPC = gotoAddress;
							}
							else {
								localPC += 4;
							}
						}
						break;
					case CMD_GOTO	:
						localPC = (pcode[localPC+1] << 8) & 0xFF00 | (pcode[localPC+2] << 0) & 0xFF;
						break;
					case CMD_CALL	:
						final ClipperFunction	acf = getRuntime().getCodeRepository().get((pcode[localPC+1] << 8) & 0xFF00 | (pcode[localPC+2] << 0) & 0xFF);
						final int				functionStackSize = pcode[localPC+3];
						final StackFrame		newStackFrame = ((FunctionTemplateStackFrame)frame).bildCallStackFrame(frame, localStack, localSP-functionStackSize);
						final ClipperValue[]	newStack = new ClipperValue[functionStackSize]; 
						
						localStack[localSP-=functionStackSize] = process(newStack, newStackFrame, constants, acf.get(byte[].class), 0);
						localPC += 4;
						break;
					default :
						throw new UnsupportedOperationException("PCode ["+pcode[localPC]+"] is not supported yet");
				}
			}
		} catch (ContentException e) {
			return null;
		}
	}

	private ClipperValue processUnary(final byte opCode, final ClipperValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	private ClipperValue processBinary(final byte b, final ClipperValue left, final ClipperValue right) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean testUnary(final byte opCode, final ClipperValue value) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean testBinary(final byte opCode, final ClipperValue left, final ClipperValue right) {
		// TODO Auto-generated method stub
		return false;
	}
}
