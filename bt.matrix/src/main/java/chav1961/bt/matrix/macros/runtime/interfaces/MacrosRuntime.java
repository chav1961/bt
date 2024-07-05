package chav1961.bt.matrix.macros.runtime.interfaces;

public interface MacrosRuntime extends Appendable {
	MacrosRuntime resetBuffer();
	@Override MacrosRuntime append(char symbol);
	@Override MacrosRuntime append(CharSequence seq);
	@Override MacrosRuntime append(CharSequence seq, int start, int end);

	MacrosRuntime append(char[] content);
	MacrosRuntime append(char[] content, int start, int end);
	int length();
	int read(char[] content, int start, int end);

	int incLockCount();
	int getLockCount();
	int decLockCount();
	
	ProgramStack getProgramStack();
}
