package chav1961.bt.jj.starter;

public class JJ {
	public static final int	BYTE_SIZE = 1;
	public static final int	WORD_SIZE = 2;
	public static final int	DWORD_SIZE = 4;
	public static final int	QWORD_SIZE = 8;
	
	public static long	mapFileHandler;
	public static long	managedMemoryAddress;
	public static int	argCount;
	public static long 	argArrayRef;
	
	public static native Object asObject(final long ref);
	public static native long asLong(final Object ref);
	public static native byte unname1(final long ref);
	public static native char unname2(final long ref);
	public static native int unname4(final long ref);
	public static native long unname8(final long ref);
	public static native void place1(final long ref, final long value);
	public static native void place2(final long ref, final long value);
	public static native void place4(final long ref, final long value);
	public static native void place8(final long ref, final long value);
	public static native long mmap(final char[] file);
	public static native void munmap(final long ref);
	
	public static int startup(final long mapFileHandler, final long managedMemoryAddress, final int argCount, final long argArrayRef) {
		JJ.mapFileHandler = mapFileHandler;
		JJ.managedMemoryAddress = managedMemoryAddress;
		JJ.argCount = argCount;
		JJ.argArrayRef = argArrayRef;

		final char[][]	parameters = new char[argCount][];
		
		for(int index = 0; index < parameters.length; index++) {
			final long 		startAddr = unname8(argArrayRef + QWORD_SIZE * index);
			long			currentAddr = startAddr;
			
			while (unname1(currentAddr) != 0) {
				currentAddr++;
			}
			final char[]	parm = new char[(int)(currentAddr - startAddr)];
			
			for(int pos = 0; pos < parm.length; pos++) {
				parm[index] = (char)unname1(startAddr + index); 
			}
			parameters[index] = parm;
		}
		
		return 0;
	}
	
	public static void shutdown() {
	}
}
