package chav1961.bt.preproc.runtime.interfaces;

import java.io.PrintStream;

public interface MacrosRuntime {
	CharAppendable getBuffer();
	PrintStream getPrintStream();
	
	int incLockCount();
	int getLockCount();
	int decLockCount();
	
	ProgramStack getProgramStack();
	char[] extractLine();
}
