package chav1961.bt.paint.script.intern;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import chav1961.bt.paint.script.intern.interfaces.ExecuteScriptCallback;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;

class DebuggerCallback implements ExecuteScriptCallback, ExecutionControl {
	static final int		MODE_SUSPENDED = 0;
	static final int		MODE_NEXT = 1;
	static final int		MODE_INTO = 2;
	static final int		MODE_OUT = 3;
	static final int		MODE_RUN = 4;
	
	private final List<Integer>	bp = new ArrayList<>();
	private Consumer<Object>	notification;
	private AtomicInteger		lastRow = new AtomicInteger(-1);
	private AtomicInteger		lastLevel = new AtomicInteger(0);
	private AtomicBoolean		paused = new AtomicBoolean(false);
	private AtomicInteger		lastMode = new AtomicInteger(MODE_SUSPENDED);
	private Semaphore			suspender = new Semaphore(1);
	private Semaphore			stepper = new Semaphore(1);

	private boolean			started = false;
	
	DebuggerCallback(final Consumer<Object> notification) {
		this.notification = notification;
	}

	@Override
	public void process(final int level, final SyntaxNode node) throws InterruptedException {
		if (isSuspended()) {
			waitResume();
		}
		
		final int	mode = lastMode.get(); 
		
		switch (mode) {
			case MODE_SUSPENDED	:
				break;
			case MODE_NEXT		:
				if (node.row != lastRow.get()) {
					waitAction();
				}
				break;
			case MODE_INTO		:
				if (level > lastLevel.get()) {
					waitAction();
				}
				break;
			case MODE_OUT		:
				if (level < lastLevel.get()) {
					waitAction();
				}
				break;
			case MODE_RUN		:
				for (Integer item : bp) {
					if (node.row == item) {
						waitAction();
						break;
					}
				}
				break;
			default :
				throw new UnsupportedOperationException("Mode ["+mode+"] is not supported yet"); 
		}
		lastRow.set(node.row);
		lastLevel.set(level);
	}

	public void reset() {
		bp.clear();
		lastRow.set(-1);
	}

	@Override
	public void start() throws RuntimeException {
		if (isStarted()) {
			throw new IllegalStateException("Entity was started earlier");
		}
		else {
			started = true;
		}
	}

	@Override
	public void suspend() throws RuntimeException {
		if (!isStarted()) {
			throw new IllegalStateException("Entity was not started or already stopped");
		}
		else if (isSuspended()) {
			throw new IllegalStateException("Entity is already suspended");
		}
		else {
			paused.set(true);
		}
	}

	@Override
	public void resume() throws RuntimeException {
		if (!isStarted()) {
			throw new IllegalStateException("Entity was not started or already stopped");
		}
		else if (!isSuspended()) {
			throw new IllegalStateException("Entity is not suspended yet");
		}
		else {
			paused.set(false);
			if (suspender.availablePermits() <= 0) {
				suspender.release();
			}
		}
	}

	@Override
	public void stop() throws RuntimeException {
		if (!isStarted()) {
			throw new IllegalStateException("Entity was not started or already stopped");
		}
		else {
			paused.set(false);
			if (suspender.availablePermits() <= 0) {
				suspender.release();
			}
			started = false;
		}
	}

	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public boolean isSuspended() {
		return paused.get();
	}

	public int getMode() {
		return lastMode.get();
	}
	
	public void setModeAndGo(final int mode) {
		lastMode.set(mode);
		go();
	}
	
	public void go() {
		stepper.release();
	}
	
	private void waitResume() throws InterruptedException {
		suspender.acquire();
	}

	private void waitAction() throws InterruptedException {
		SwingUtilities.invokeLater(()->notification.accept(null));
		stepper.acquire();
	}
}
