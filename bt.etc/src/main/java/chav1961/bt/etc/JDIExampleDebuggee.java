package chav1961.bt.etc;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class JDIExampleDebuggee {

    public static void main(String[] args) throws IOException, InterruptedException {
        String jpda = "Java Platform Debugger Architecture";
        System.out.println("Hi Everyone, Welcome to " + jpda); //add a break point here

        String jdi = "Java Debug Interface"; //add a break point here and also stepping in here
        String text = "Today, we'll dive into " + jdi;
        System.out.println(text);

        System.err.println(System.getProperties());
        
        ProcessHandle.allProcesses().forEach(JDIExampleDebuggee::print);
        // https://github.com/scijava/native-lib-loader
    }
    
    private static void print(final ProcessHandle ph) {
    	if (!ph.info().command().equals(Optional.empty())) {
        	System.err.println("PID="+ph.pid());
        	System.err.println("Cmd="+ph.info().command());
        	Optional<String[]> args = ph.info().arguments();
        	if (args.isPresent()) {
            	System.err.println("Args="+Arrays.toString(args.get()));
        	}
    	}
    }
    
}