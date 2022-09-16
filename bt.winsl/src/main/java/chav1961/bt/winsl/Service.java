package chav1961.bt.winsl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import chav1961.bt.winsl.echoserver.EchoServer;
import chav1961.bt.winsl.utils.JavaServiceLibrary;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class Service {
	public static final String	START_INI = "start";
	public static final String	PAUSE_INI = "pause";
	public static final String	RESUME_INI = "resume";
	public static final String	STOP_INI = "stop";
	public static final String	IN_REDIRECTION_INI = "inRedirection";
	public static final String	OUT_REDIRECTION_INI = "outRedirection";
	public static final String	ERR_REDIRECTION_INI = "errRedirection";
	
	private static final char	EOF = '\0';

	public static void main(final String[] args) throws IOException {
		final ArgParser		parser = new ApplicationArgParser();
		String				serviceNameErr = "unknown";

		try{final ArgParser	ap = parser.parse(false, false, args);
		
			if (!System.getProperty("os.name","unknown").toUpperCase().contains("WINDOWS")) {
				throw new CommandLineParametersException("This application can be used in the Windows-based systems only");
			}
		
			final SubstitutableProperties		sp = Application.getConfiguration(ap.getValue(Application.CONF_KEY, URI.class));
			final String						serviceName = serviceNameErr = sp.getProperty(Application.SERVICENAME_INI);
			final ArrayBlockingQueue<Integer>	queue =  new ArrayBlockingQueue<>(10);

			if (ap.getValue(Application.DEMO_KEY, boolean.class)) {
				sp.setProperty(START_INI, EchoServer.class.getCanonicalName()+".main");
				sp.setProperty(STOP_INI, EchoServer.class.getCanonicalName()+".terminate");
			}
			
			final Thread	t = new Thread(()->{
				try{prepareService(serviceName, queue);
				} catch (EnvironmentException e) {
				} finally {
					try{queue.put(JavaServiceLibrary.RC_STOP);
					} catch (InterruptedException e1) {
					}
				}
			});
			t.setDaemon(true);
			t.start();
			
			try{queue.put(JavaServiceLibrary.RC_START);
			
loop:			for(;;) {
					switch (queue.take()) {
						case JavaServiceLibrary.RC_START :
							callService(serviceName,START_INI,sp.getProperty(START_INI));
							break;
						case JavaServiceLibrary.RC_PAUSE :
							if (sp.containsKey(PAUSE_INI)) {
								callService(serviceName,PAUSE_INI,sp.getProperty(PAUSE_INI));
							}
							break;
						case JavaServiceLibrary.RC_RESUME :
							if (sp.containsKey(RESUME_INI)) {
								callService(serviceName,RESUME_INI,sp.getProperty(RESUME_INI));
							}
							break;
						case JavaServiceLibrary.RC_STOP : 
							if (sp.containsKey(STOP_INI)) {
								callService(serviceName,STOP_INI,sp.getProperty(STOP_INI));
							}
							break loop;
						default : 
							break loop;
					}
				}
			} finally {
				unprepareService();
			}
			System.exit(0);
		} catch (CommandLineParametersException e) {
			printError(128,serviceNameErr,"Command line parameter error: "+e.getLocalizedMessage());
		} catch (SyntaxException e) {
			printError(128,serviceNameErr,"Syntax error: "+e.getLocalizedMessage());
		} catch (IOException e) {
			printError(129,serviceNameErr,"I/O error processing config URI: "+e.getClass().getSimpleName()+" - "+e.getLocalizedMessage());
		} catch (EnvironmentException | InterruptedException e) {
			printError(129,serviceNameErr,"I/O error processing config URI: "+e.getClass().getSimpleName()+" - "+e.getLocalizedMessage());
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
			super(new URIArg(Application.CONF_KEY,false,"config source with service settings ("+Application.CONFIG_FILE+" if not typed)","file:c:/tmp/x.conf"),
				  new BooleanArg(Application.DEMO_KEY,false,"prepare demo service",false)
				);
		}
	}
}
