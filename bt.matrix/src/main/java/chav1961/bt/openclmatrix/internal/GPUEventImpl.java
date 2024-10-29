package chav1961.bt.openclmatrix.internal;

import java.util.function.Consumer;

import org.jocl.CL;
import org.jocl.CLException;
import org.jocl.cl_event;

import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUEvent;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;

class GPUEventImpl implements GPUEvent {
	final cl_event	event;
	private final Consumer<GPUEvent>	onCloseCallback;
	private boolean	closed = false;
	
	GPUEventImpl(final cl_event event, final Consumer<GPUEvent> onCloseCallback) {
		this.event = event;
		this.onCloseCallback = onCloseCallback;
	}

	@Override
	public void close() throws RuntimeException {
		if (!closed) {
			closed = true;
			onCloseCallback.accept(this);
			CL.clReleaseEvent(event);
		}
	}

	@Override
	public void post() {
		if (closed) {
			throw new IllegalStateException("Attempt to post closed event");
		}
		else {
			CL.clSetUserEventStatus(event, CL.CL_COMPLETE);
		}
	}
	
	@Override
	public GPUEvent awaitAll(boolean closeAfterComplete, final GPUEvent... events) throws InterruptedException, CalculationException {
		if (events == null || events.length == 0 || Utils.checkArrayContent4Nulls(events) >= 0) {
			throw new IllegalArgumentException("Events list is null, empty, or contains nulls inside");
		}
		else {
			for(GPUEvent item : events) {
				item.awaitCurrent();
			}
			if (closeAfterComplete) {
				for(GPUEvent item : events) {
					item.close();
				}
			}
			post();
			return this;
		}
	}

	@Override
	public GPUEvent awaitCurrent() throws InterruptedException, CalculationException { 
		if (closed) {
			throw new IllegalStateException("Attempt to wait closed event");
		}
		else {
			try {
				CL.clWaitForEvents(1, new cl_event[] {event});
				return this;
			} catch (CLException exc) {
				throw new CalculationException(exc.getLocalizedMessage());
			}
		}
	}
}