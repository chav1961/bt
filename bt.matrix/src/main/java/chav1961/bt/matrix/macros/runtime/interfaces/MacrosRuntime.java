package chav1961.bt.matrix.macros.runtime.interfaces;

import java.io.PrintStream;

public interface MacrosRuntime {
	StringBuilder getBuffer();
	PrintStream getPrintStream();
	
	int incLockCount();
	int getLockCount();
	int decLockCount();
	
	ProgramStack getProgramStack();
}
