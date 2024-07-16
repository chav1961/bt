package chav1961.bt.preproc.runtime;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.bt.preproc.runtime.interfaces.CharAppendable;
import chav1961.bt.preproc.runtime.interfaces.MacrosRuntime;
import chav1961.bt.preproc.runtime.interfaces.ProgramStack;

public class SingleMacrosRuntime implements MacrosRuntime {
	private final ProgramStack		stack = new ProgramStackImpl();
	private final AtomicInteger		lockCount = new AtomicInteger(0);
	private final StringBuilder		sb = new StringBuilder();
	private final PrintStream		errPrint;
	private final CharAppendable	app = new CharAppendable() {
											@Override
											public CharAppendable append(char[] content) {
												sb.append(content);
												return this;
											}
											
											@Override
											public CharAppendable append(CharSequence csq) {
												sb.append(csq);
												return this;
											}
											
											@Override
											public CharAppendable append(CharSequence csq, int start, int end) {
												sb.append(csq, start, end);
												return this;
											}
											
											@Override
											public CharAppendable append(char c) {
												sb.append(c);
												return this;
											}
											
											@Override
											public String toString() {
												return sb.toString();
											}
										};

	
	
	public SingleMacrosRuntime() {
		this(System.err);
	}

	public SingleMacrosRuntime(final PrintStream ps) {
		if (ps == null) {
			throw new NullPointerException("Print stream can't be null");
		}
		else {
			this.errPrint = ps;
		}
	}
	
	@Override
	public int incLockCount() {
		return lockCount.incrementAndGet();
	}

	@Override
	public int getLockCount() {
		return lockCount.get();
	}

	@Override
	public int decLockCount() {
		return lockCount.decrementAndGet();
	}

	@Override
	public ProgramStack getProgramStack() {
		return stack;
	}

	@Override
	public PrintStream getPrintStream() {
		return errPrint;
	}

	@Override
	public CharAppendable getBuffer() {
		return app;
	}
	
	@Override
	public char[] extractLine() {
		int	eol = sb.length()-1;
		
		for(int index = 0, maxIndex = sb.length(); index < maxIndex; index++) {
			if (sb.charAt(index) == '\n') {
				eol = index;
				break;
			}
		}
		final char[]	line = new char[eol + 1];

		sb.getChars(0, eol + 1, line, 0);
		sb.delete(0, eol + 1);
 		return line;
	}
}
