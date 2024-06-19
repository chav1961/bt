package chav1961.bt.etc;


import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.StepRequest;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;

public class JDIExampleDebugger implements Closeable, LoggerFacadeOwner {
	private static final String	SOCKET_CONNECTOR = "com.sun.tools.jdi.SocketAttachingConnector"; 
	private static final String	SHARED_MEMORY_CONNECTOR = "com.sun.tools.jdi.SharedMemoryAttachingConnector"; 
	private static final String	PROCESS_CONNECTOR = "com.sun.tools.jdi.ProcessAttachingConnector"; 
	
	public enum VMEvents {
		ACCESS_WATCHPOINT_EVENT(com.sun.jdi.event.AccessWatchpointEvent.class),
		BREAKPOINT_EVENT(com.sun.jdi.event.BreakpointEvent.class),
		CLASS_PREPARE_EVENT(com.sun.jdi.event.ClassPrepareEvent.class),
		CLASS_UNLOAD_EVENT(com.sun.jdi.event.ClassUnloadEvent.class),
		EXCEPTION_EVENT(com.sun.jdi.event.ExceptionEvent.class),
		METHOD_ENTRY_EVENT(com.sun.jdi.event.MethodEntryEvent.class),
		METHOD_EXIT_EVENT(com.sun.jdi.event.MethodExitEvent.class),
		MODIFICATION_WATCHPOINT_EVENT(com.sun.jdi.event.ModificationWatchpointEvent.class),
		MONITOR_CONTENDED_ENTERED_EVENT(com.sun.jdi.event.MonitorContendedEnteredEvent.class),
		MONITOR_CONTENDED_ENTER_EVENT(com.sun.jdi.event.MonitorContendedEnterEvent.class),
		MONITOR_WAITED_EVENT(com.sun.jdi.event.MonitorWaitedEvent.class),
		MONITOR_WAIT_EVENT(com.sun.jdi.event.MonitorWaitEvent.class),
		STEP_EVENT(com.sun.jdi.event.StepEvent.class),
		THREAD_DEATH_EVENT(com.sun.jdi.event.ThreadDeathEvent.class),
		THREAD_START_EVENT(com.sun.jdi.event.ThreadStartEvent.class),
		VM_DEATH_EVENT(com.sun.jdi.event.VMDeathEvent.class),
		VM_DISCONNECT_EVENT(com.sun.jdi.event.VMDisconnectEvent.class),
		VM_START_EVENT(com.sun.jdi.event.VMStartEvent.class),
		WATCHPOINT_EVENT(com.sun.jdi.event.WatchpointEvent.class);
		
		private final Class<? extends com.sun.jdi.event.Event>	cl;
		
		private VMEvents(final Class<? extends com.sun.jdi.event.Event> cl) {
			this.cl = cl;
		}
		
		public Class<? extends com.sun.jdi.event.Event> getEventClass() {
			return cl;
		}
		
		public static <T extends com.sun.jdi.event.Event> VMEvents valueOf(final T value) {
			if (value == null) {
				throw new NullPointerException("Value can't be null"); 
			}
			else {
				final Class<? extends com.sun.jdi.event.Event>	cl = value.getClass();
				
				for (VMEvents event : values()) {
					if (event.getEventClass().isAssignableFrom(cl)) {
						return event;
					}
				}
				throw new IllegalArgumentException("Value ["+value+"] has wrong class");
			}
		}
	}
	
	
    /**
     * Creates a request to prepare the debug class, add filter as the debug class and enables it
     * @param vm
     */
    public void enableClassPrepareRequest(final Class<?> cl) {
        ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
        classPrepareRequest.addClassFilter(cl.getName());
        classPrepareRequest.enable();
    }

    /**
     * Sets the break points at the line numbers mentioned in breakPointLines array
     * @param vm
     * @param event
     * @throws AbsentInformationException
     */
    public void setBreakPoints(ClassPrepareEvent event, int... breakPointLines) throws AbsentInformationException {
        ClassType classType = (ClassType) event.referenceType();
        for(int lineNumber: breakPointLines) {
            Location location = classType.locationsOfLine(lineNumber).get(0);
            BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(location);
            bpReq.enable();
        }
    }

    /**
     * Displays the visible variables
     * @param event
     * @throws IncompatibleThreadStateException
     * @throws AbsentInformationException
     */
    public void displayVariables(LocatableEvent event, Class<?> debugClass) throws IncompatibleThreadStateException, AbsentInformationException {
        StackFrame stackFrame = event.thread().frame(0);
        if(stackFrame.location().toString().contains(debugClass.getName())) {
            Map<LocalVariable, Value> visibleVariables = stackFrame.getValues(stackFrame.visibleVariables());
            System.out.println("Variables at " +stackFrame.location().toString() +  " > ");
            for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
                System.out.println(entry.getKey().name() + " = " + entry.getValue());
            }
        }
    }

    /**
     * Enables step request for a break point
     * @param vm
     * @param event
     */
    public StepRequest enableStepRequest(BreakpointEvent event, Class<?> debugClass, int... breakPointLines) {
        //enable step request for last break point
        if(event.location().toString().contains(debugClass.getName()+":"+breakPointLines[breakPointLines.length-1])) {
            StepRequest stepRequest = vm.eventRequestManager().createStepRequest(event.thread(), StepRequest.STEP_LINE, StepRequest.STEP_OVER);
            return stepRequest;
        }
        else {
        	throw new RuntimeException(); 
        }
    }

    public static void main(String[] args) throws Exception {
        final ProcessBuilder	pb = new ProcessBuilder("java","-jar","test.jar");
        VirtualMachine 			vm = null;
        Process					p = null;
        JDIExampleDebugger 		debuggerInstance = null; 

        pb.directory(new File("./"));
        
        try {
            vm = launchApplication(pb, 30000);
            p = vm.process();

            debuggerInstance = new JDIExampleDebugger(PureLibSettings.CURRENT_LOGGER, vm);
//            debuggerInstance.setDebugClass(JDIExampleDebuggee.class);
            int[] breakPoints = {8, 11};
//            debuggerInstance.setBreakPointLines(breakPoints);
            
            debuggerInstance.enableClassPrepareRequest(JDIExampleDebuggee.class);

            boolean	exitLoop = false;
            EventSet eventSet = null;
            
            while (!exitLoop && (eventSet = vm.eventQueue().remove()) != null) {
                for (Event event : eventSet) {
                	switch (VMEvents.valueOf(event)) {
						case ACCESS_WATCHPOINT_EVENT	:
							break;
						case BREAKPOINT_EVENT			:
	                        event.request().disable();
	                        debuggerInstance.displayVariables((BreakpointEvent) event, JDIExampleDebuggee.class);
	                        debuggerInstance.enableStepRequest((BreakpointEvent)event, JDIExampleDebuggee.class, breakPoints);
							break;
						case CLASS_PREPARE_EVENT		:
	                        debuggerInstance.setBreakPoints((ClassPrepareEvent)event);
							break;
						case CLASS_UNLOAD_EVENT			:
							break;
						case EXCEPTION_EVENT			:
							break;
						case METHOD_ENTRY_EVENT			:
							break;
						case METHOD_EXIT_EVENT			:
							break;
						case MODIFICATION_WATCHPOINT_EVENT	:
							break;
						case MONITOR_CONTENDED_ENTERED_EVENT:
							break;
						case MONITOR_CONTENDED_ENTER_EVENT	:
							break;
						case MONITOR_WAITED_EVENT		:
							break;
						case MONITOR_WAIT_EVENT			:
							break;
						case STEP_EVENT					:
	                        debuggerInstance.displayVariables((StepEvent) event, JDIExampleDebuggee.class);
							break;
						case THREAD_DEATH_EVENT			:
							break;
						case THREAD_START_EVENT			:
							break;
						case VM_DEATH_EVENT				:
							exitLoop = true;
							break;
						case VM_DISCONNECT_EVENT		:
							exitLoop = true;
							break;
						case VM_START_EVENT				:
							break;
						case WATCHPOINT_EVENT			:
							break;
						default:
							throw new UnsupportedOperationException("Event type ["+VMEvents.valueOf(event)+"] is not supported yet");
                	}
                    vm.resume();
                }
            }
        } catch (VMDisconnectedException e) {
            System.out.println("Virtual Machine is disconnected.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	if (p != null) {
        		p.destroyForcibly();
        		p.waitFor();
        	}
        }
    }

    private final LoggerFacade		logger;
    private final VirtualMachine	vm;
    private final Map<Class<?>,Map<Method,int[]>>	breakpoints = new IdentityHashMap<>(); 
    private final Thread			t = new Thread(this::run);
    
    public JDIExampleDebugger(final LoggerFacade logger, final VirtualMachine vm) {
    	if (logger == null) {
    		throw new NullPointerException("Logger can't be null"); 
    	}
    	else if (vm == null) {
    		throw new NullPointerException("Virtual machine can't be null"); 
    	}
    	else {
    		this.logger = logger;
    		this.vm = vm;
    		this.t.setName(getClass().getName()+"-debugger");
    		this.t.setDaemon(true);
    	}
    }

	@Override
	public LoggerFacade getLogger() {
		return logger;
	}

	@Override
	public void close() throws IOException {
		t.interrupt();
		vm.dispose();
	}

	public StepRequest processBreakpoint(final BreakpointEvent event) throws IncompatibleThreadStateException, AbsentInformationException {
		displayVariables((BreakpointEvent) event, JDIExampleDebuggee.class);
	    return enableStepRequest((BreakpointEvent)event, JDIExampleDebuggee.class, new int[]{8,11});
	}
	
	public void processVMEvent() {
	}	
	
	public void setBreakPoints(final Class<?> cl, final Method m, final int... lines) {
	}
	
	public void setBreakPoints(final Class<?> cl, final int... lines) {

	}
	
	public void clearBreakPoints(final Class<?> cl, final Method m) {

	}
	
	public void clearBreakPoints(final Class<?> cl) {

	}

	protected VirtualMachine getVM() {
		return vm;
	}

	private void run() {
        EventSet eventSet = null;
        
        try{while (!Thread.interrupted() && (eventSet = vm.eventQueue().remove()) != null) {
	            for (Event event : eventSet) {
	            	switch (VMEvents.valueOf(event)) {
						case ACCESS_WATCHPOINT_EVENT	:
							break;
						case BREAKPOINT_EVENT			:
	                        event.request().disable();
	                        processBreakpoint((BreakpointEvent) event).enable();
							break;
						case CLASS_PREPARE_EVENT		:
	                        setBreakPoints((ClassPrepareEvent)event);
							break;
						case CLASS_UNLOAD_EVENT			:
							break;
						case EXCEPTION_EVENT			:
							break;
						case METHOD_ENTRY_EVENT			:
							break;
						case METHOD_EXIT_EVENT			:
							break;
						case MODIFICATION_WATCHPOINT_EVENT	:
							break;
						case MONITOR_CONTENDED_ENTERED_EVENT:
							break;
						case MONITOR_CONTENDED_ENTER_EVENT	:
							break;
						case MONITOR_WAITED_EVENT		:
							break;
						case MONITOR_WAIT_EVENT			:
							break;
						case STEP_EVENT					:
	                        displayVariables((StepEvent) event, JDIExampleDebuggee.class);
							break;
						case THREAD_DEATH_EVENT			:
							break;
						case THREAD_START_EVENT			:
							break;
						case VM_DEATH_EVENT				:
							break;
						case VM_DISCONNECT_EVENT		:
							break;
						case VM_START_EVENT				:
							break;
						case WATCHPOINT_EVENT			:
							break;
						default:
							throw new UnsupportedOperationException("Event type ["+VMEvents.valueOf(event)+"] is not supported yet");
	            	}
	                vm.resume();
	            }
	        }
        } catch (IncompatibleThreadStateException | AbsentInformationException exc) {
        	
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static VirtualMachine launchApplication(final ProcessBuilder builder, final int timeout) throws IOException, IllegalConnectorArgumentsException {
		if (builder == null) {
			throw new NullPointerException("Process builder can't be null"); 
		}
    	else if (timeout < 0) {
    		throw new IllegalArgumentException("Timeout [timeout] can't be negative"); 
    	}
    	else {
    		final List<String>	commands = builder.command();

    		commands.add(1, "-Xdebug");
    		commands.add(1, "-Xrunjdwp:server=y");
    		builder.command(commands);
    		
    		final Process	p = builder.start();
    		
    		return attach2Application(p.pid(), timeout);
    	}
	}
	
    public static VirtualMachine attach2Application(final InetSocketAddress addr, final int timeout) throws IOException, IllegalConnectorArgumentsException {
    	if (addr == null) {
    		throw new NullPointerException("Inet address to attach can't be null"); 
    	}
    	else if (timeout < 0) {
    		throw new IllegalArgumentException("Timeout [timeout] can't be negative"); 
    	}
    	else {
    		for (AttachingConnector con : Bootstrap.virtualMachineManager().attachingConnectors()) {
    		    if (SOCKET_CONNECTOR.equals(con.getClass().getName())) {
    		    	final Map<String, Connector.Argument> 	arguments = con.defaultArguments();

    		    	for (Entry<String, Connector.Argument> item : arguments.entrySet()) {
    		    		switch (item.getKey()) {
    		    			case "host" 	:
    		    				item.getValue().setValue(addr.getHostName());
    		    				break;
    		    			case "port" 	: 
    		    				item.getValue().setValue(""+addr.getPort());
    		    				break;
    		    			case "timeout"	: 
    		    				item.getValue().setValue(""+timeout);
    		    				break;
    		    		}
    		    	}
    		    	return con.attach(arguments);
    		    }
    		}
    		throw new IOException("No TCP connectors available in the system");
    	}
    }

    public static VirtualMachine attach2Application(final long pid, final int timeout) throws IOException, IllegalConnectorArgumentsException {
    	if (pid <= 0) {
    		throw new IllegalArgumentException("Process id ["+pid+"] must be positive"); 
    	}
    	else if (timeout < 0) {
    		throw new IllegalArgumentException("Timeout [timeout] can't be negative"); 
    	}
    	else {
    		for (AttachingConnector con : Bootstrap.virtualMachineManager().attachingConnectors()) {
    		    if (PROCESS_CONNECTOR.equals(con.getClass().getName())) {
    		    	final Map<String, Connector.Argument> 	arguments = con.defaultArguments();

    		    	for (Entry<String, Connector.Argument> item : arguments.entrySet()) {
    		    		switch (item.getKey()) {
    		    			case "pid" 	:
    		    				item.getValue().setValue(""+pid);
    		    				break;
    		    			case "timeout"	: 
    		    				item.getValue().setValue(""+timeout);
    		    				break;
    		    		}
    		    	}
    		    	return con.attach(arguments);
    		    }
    		}
    		throw new IOException("No process connectors available in the system");
    	}
    }

    public static VirtualMachine attach2Application(final String name, final int timeout) throws IOException, IllegalConnectorArgumentsException {
    	if (name == null || name.isEmpty()) {
    		throw new IllegalArgumentException("Shared name to attach can't be null"); 
    	}
    	else if (timeout < 0) {
    		throw new IllegalArgumentException("Timeout [timeout] can't be negative"); 
    	}
    	else {
    		for (AttachingConnector con : Bootstrap.virtualMachineManager().attachingConnectors()) {
    		    if (SHARED_MEMORY_CONNECTOR.equals(con.getClass().getName())) {
    		    	final Map<String, Connector.Argument> 	arguments = con.defaultArguments();

    		    	for (Entry<String, Connector.Argument> item : arguments.entrySet()) {
    		    		switch (item.getKey()) {
    		    			case "name" 	:
    		    				item.getValue().setValue(name);
    		    				break;
    		    			case "timeout"	: 
    		    				item.getValue().setValue(""+timeout);
    		    				break;
    		    		}
    		    	}
    		    	return con.attach(arguments);
    		    }
    		}
    		throw new IOException("No shared memory connectors available in the system");
    	}
    }
}
