package chav1961.bt.pythonwrapper;

import org.python.util.PythonInterpreter;

public class Application {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	    try(final PythonInterpreter 	pyInterp = new PythonInterpreter()) {
	    	
	    	pyInterp.exec("print('Hello Python World!')");
	    	
	    	String	line = "from java.lang import System # Java import\n"
	    				+  "print('Running on Java version: ' + System.getProperty('java.version'))\n"
	    				+  "print('Unix time from Java: ' + str(System.currentTimeMillis()))\n";
	    	pyInterp.exec(line);
	    }
	}
}
