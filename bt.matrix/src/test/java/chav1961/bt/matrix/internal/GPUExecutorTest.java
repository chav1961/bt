package chav1961.bt.matrix.internal;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.openclmatrix.internal.GPUExecutor;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUEvent;
import chav1961.bt.openclmatrix.internal.GPUExecutor.GPUScheduler;
import chav1961.bt.openclmatrix.internal.InternalUtils;
import chav1961.bt.openclmatrix.spi.OpenCLDescriptor;
import chav1961.purelib.basic.SimpleTimerTask;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;

public class GPUExecutorTest {

	@Test
	public void basicTest() throws ContentException {
		try(final OpenCLDescriptor	desc = new OpenCLDescriptor();
			final GPUExecutor		exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION)) {

			try(final GPUScheduler	sched = exec.startTransaction()) {
				
			}
		}
	}

	@Test
	public void gpuEventsTest() throws ContentException, IOException, InterruptedException, CalculationException {
		try(final OpenCLDescriptor		desc = new OpenCLDescriptor();
			final GPUExecutor			exec = new GPUExecutor(desc.getContext(true), InternalUtils.TEMP_DIR_LOCATION)) {
			final boolean[]				marks = new boolean[] {false, false, false};

			try(final GPUScheduler		sched = exec.startTransaction();
				final GPUEvent			ev1 = sched.createEvent((ev, stat, cargo)->marks[0] = true);
				final GPUEvent			ev2 = sched.createEvent((ev, stat, cargo)->marks[1] = true);
				final GPUEvent			ev3 = sched.createEvent((ev, stat, cargo)->marks[2] = true)) {
				
				SimpleTimerTask.start(()->{ev1.post();}, 500);
				SimpleTimerTask.start(()->{ev2.post();}, 500);
				ev3.awaitAll(true, ev1, ev2);
				Assert.assertArrayEquals(new boolean[] {true,  true, true}, marks);
				
				try{sched.createEvent(null);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				
				try{ev1.awaitCurrent();
					Assert.fail("Mandatory exception was not detected (event already closed)");
				} catch (IllegalStateException exc) {
				}
				try{ev1.awaitAll(false, ev2);
					Assert.fail("Mandatory exception was not detected (event already closed)");
				} catch (IllegalStateException exc) {
				}
				try{ev1.post();
					Assert.fail("Mandatory exception was not detected (event already closed)");
				} catch (IllegalStateException exc) {
				}
				
				try{ev3.awaitAll(false, (GPUEvent[])null);
					Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
				} catch (IllegalArgumentException exc) {
				}
				try{ev3.awaitAll(false);
					Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
				} catch (IllegalArgumentException exc) {
				}
				try{ev3.awaitAll(false, (GPUEvent)null);
					Assert.fail("Mandatory exception was not detected (nulls inside 2-nd argument)");
				} catch (IllegalArgumentException exc) {
				}
			}
		}
	}	
}
