package chav1961.bt.matrix.macros.runtime;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.bt.matrix.macros.runtime.interfaces.CharAppendable;
import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;
import chav1961.bt.matrix.macros.runtime.interfaces.ProgramStack;

public class SingleMacrosRuntime implements MacrosRuntime {
	private final ProgramStack	stack = new ProgramStackImpl();
	private final AtomicInteger	lockCount = new AtomicInteger(0);
	private final StringBuilder	sb = new StringBuilder();
	private final PrintStream	errPrint;
	
	
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
		return new CharAppendable() {
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
		};
	}
}
