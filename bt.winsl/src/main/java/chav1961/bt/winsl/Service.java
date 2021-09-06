package chav1961.bt.winsl;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import chav1961.bt.winsl.utils.JavaServiceLibrary;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class Service {
	private static final char	EOF = '\0';

	public static void main(String[] args) throws IOException {
		final ArgParser		parser = new ApplicationArgParser();
		String				serviceNameErr = "unknown";

		try(final FileWriter	fw = new FileWriter("c:/tmp/x.txt")) {
			fw.write("start 1: "+Arrays.toString(args)+"\n");
			
			try {final ArgParser	ap = parser.parse(false, false, args);
			
				fw.write("p1="+System.getProperty("java.library.path")+"\n");
			
			if (!System.getProperty("os.name","unknown").toUpperCase().contains("WINDOWS")) {
				throw new CommandLineParametersException("This application can be used in the Windows-based systems only");
			}
			fw.write("p2\n");
		
			final SubstitutableProperties		sp = Application.getConfiguration(ap.getValue(Application.CONF_KEY, URI.class));
			
			fw.write("p3\n");
			
			final String						serviceName = serviceNameErr = sp.getProperty(Application.SERVICENAME_INI);
			final ArrayBlockingQueue<Integer>	queue =  new ArrayBlockingQueue<>(10);
			
			fw.write("p4 created!!!\n");
			final Thread	t = new Thread(()->{
				try{fw.write("Service preapre="+prepareService(serviceName, queue)+"\n");
					fw.flush();
				} catch (EnvironmentException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			t.start();
			fw.write("p5 prepared 2!!!\n");
			fw.flush();
			
//			queue.put(JavaServiceLibrary.RC_STOP);
			
			Integer  i;
			
loop:		for(;;) {
				switch (i=queue.take()) {
					case JavaServiceLibrary.RC_START :
						fw.write("p6 start\n");
						fw.flush();
//						JavaServiceLibrary.startService(serviceName);
//						callService(serviceName,Application.START_INI,sp.getProperty(Application.START_INI));
						break;
					case JavaServiceLibrary.RC_PAUSE :
						fw.write("p6 pause\n");
						fw.flush();
//						if (sp.containsKey(Application.PAUSE_INI)) {
//							callService(serviceName,Application.PAUSE_INI,sp.getProperty(Application.PAUSE_INI));
//						}
						break;
					case JavaServiceLibrary.RC_RESUME :
						fw.write("p6 resume\n");
						fw.flush();
//						if (sp.containsKey(Application.RESUME_INI)) {
//							callService(serviceName,Application.RESUME_INI,sp.getProperty(Application.RESUME_INI));
//						}
						break;
					case JavaServiceLibrary.RC_STOP : 
						fw.write("p6 stop\n");
						fw.flush();
//						if (sp.containsKey(Application.STOP_INI)) {
//							callService(serviceName,Application.STOP_INI,sp.getProperty(Application.STOP_INI));
//						}
					default : 
						fw.write("Alles: "+i+"\n");
						fw.flush();
						break loop;
				}
			}
			fw.write("p7 EXIT!!!!\n");
			unprepareService();
			fw.write("p8\n");
			fw.flush();
			
		} catch (CommandLineParametersException e) {
			fw.write("ERROR!!!!: "+e);
			printError(128,serviceNameErr,"Command line parameter error: "+e.getLocalizedMessage());
//		} catch (SyntaxException e) {
//			printError(128,serviceName,"Syntax error: "+e.getLocalizedMessage());
		} catch (IOException e) {
			printError(129,serviceNameErr,"I/O error processing config URI: "+e.getClass().getSimpleName()+" - "+e.getLocalizedMessage());
//		} catch (EnvironmentException e) {
//			printError(129,serviceName,"I/O error processing config URI: "+e.getClass().getSimpleName()+" - "+e.getLocalizedMessage());
		} catch (Throwable t) {
			fw.write("ACHTUNG!!!!"+t);
		} finally {
			fw.flush();
		}
			fw.flush();
		}
	}

	static void printError(final int rc, final String serviceName, final String error) {
		try{JavaServiceLibrary.print2ServiceLog(serviceName, error);
		} catch (EnvironmentException e) {
		}
		System.exit(rc);
	}
	
	private static int prepareService(final String serviceName, final Object queue) throws EnvironmentException {
		return JavaServiceLibrary.prepareService(serviceName, queue);
	}

	private static int getServiceRequest() throws EnvironmentException {
		return JavaServiceLibrary.getServiceRequest();
	}
	
	private static void unprepareService() throws EnvironmentException {
		JavaServiceLibrary.unprepareService();
	}

	private static void callService(final String serviceName, final String keyName, final String parm) throws SyntaxException {
		final List<String>	parameters = parseParameters(CharUtils.terminateAndConvert2CharArray(parm, EOF));
		
		if (parameters.isEmpty()) {
			throw new SyntaxException(0,0,"Class and method is not defined in the ["+keyName+"] key"); 
		}
		else {
			final String	classAndMethod = parameters.remove(0);
			final String[] 	parms = parameters.toArray(new String[parameters.size()]);
			
			if (!classAndMethod.contains(".")) {
				throw new SyntaxException(0,0,"No any dots in the ["+classAndMethod+"] class and method names for the ["+keyName+"] key"); 
			}
			else {
				final int			lastDot = classAndMethod.lastIndexOf('.'); 
				final String		className = classAndMethod.substring(0, lastDot);
				final String		methodName = classAndMethod.substring(lastDot + 1);
				
				try{final Class<?>	clazz = Class.forName(className);
					final Method	m = clazz.getMethod(methodName, String[].class);
					
					if (Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())) {
						final Thread	t = new Thread(()->{
											try{m.invoke(null, (Object)parms);
											} catch (IllegalAccessException | IllegalArgumentException e) {
												try{JavaServiceLibrary.print2ServiceLog(serviceName, e.getClass().getSimpleName()+": "+e.getLocalizedMessage());
												} catch (EnvironmentException e1) {
												}
											} catch (InvocationTargetException e) {
												try{JavaServiceLibrary.print2ServiceLog(serviceName, e.getTargetException().getClass().getSimpleName()+": "+e.getTargetException().getLocalizedMessage());
												} catch (EnvironmentException e1) {
												}
											}
										});
						t.setDaemon(true);
						t.start();
					}
					else {
						throw new SyntaxException(0,0,"Method ["+methodName+"(String[])] in the class ["+className+"] must be public and static in the ["+keyName+"] key"); 
					}
				} catch (ClassNotFoundException e) {
					throw new SyntaxException(0,0,"Unknown class name ["+className+"] in the ["+keyName+"] key"); 
				} catch (NoSuchMethodException e) {
					throw new SyntaxException(0,0,"Unknown method ["+methodName+"(String[])] for the class ["+className+"] in the ["+keyName+"] key"); 
				}
			}
		}
	}

	private static List<String> parseParameters(final char[] parms) {
		final List<String>	result = new ArrayList<>();
		int					start = 0, current = 0;
		
		while (parms[current] != EOF) {
			while (parms[current] != EOF && parms[current] <= ' ') {
				current++;
			}
			if (parms[current] == '\"') {
				start = ++current;
				while (parms[current] != EOF && parms[current] != '\"') {
					current++;
				}
				result.add(new String(parms,start,current-start-1));
				if (parms[current] == '\"') {
					current++;
				}
			}
			else {
				start = current;
				while (parms[current] != EOF && parms[current] > ' ') {
					current++;
				}
				result.add(new String(parms,start,current-start));		
			}
		}
		return result;
	}

	static class ApplicationArgParser extends ArgParser {
		public ApplicationArgParser() {
			super(new URIArg(Application.CONF_KEY,false,"config source with service settings ("+Application.CONFIG_FILE+" if not typed)","file:/c:/tmp/x.conf"/*Application.CONFIG_FILE*/));
		}
	}
}
