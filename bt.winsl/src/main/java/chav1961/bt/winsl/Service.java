package chav1961.bt.winsl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import chav1961.bt.winsl.utils.JavaServiceLibrary;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class Service {
	private static final char	EOF = '\0';

	public static void main(String[] args) {
		final ArgParser		parser = new ApplicationArgParser();
		String				serviceName = "unknown";

		try{final ArgParser	ap = parser.parse(false, false, args);
		
			if (!System.getProperty("os.name","unknown").toUpperCase().contains("WINDOWS")) {
				throw new CommandLineParametersException("This application can be used in the Windows-based systems only");
			}
		
			final SubstitutableProperties	sp = Application.getConfiguration(ap.getValue(Application.CONF_KEY, URI.class));
			
			serviceName = sp.getProperty(Application.SERVICE_NAME_KEY); 
			
			prepareService();
loop:		for(;;) {
				switch (getServiceRequest()) {
					case JavaServiceLibrary.RC_START :
						callService(serviceName,Application.START_INI,sp.getProperty(Application.START_INI));
						break;
					case JavaServiceLibrary.RC_PAUSE :
						if (sp.containsKey(Application.PAUSE_INI)) {
							callService(serviceName,Application.PAUSE_INI,sp.getProperty(Application.PAUSE_INI));
						}
						break;
					case JavaServiceLibrary.RC_RESUME :
						if (sp.containsKey(Application.RESUME_INI)) {
							callService(serviceName,Application.RESUME_INI,sp.getProperty(Application.RESUME_INI));
						}
						break;
					case JavaServiceLibrary.RC_STOP : 
						if (sp.containsKey(Application.STOP_INI)) {
							callService(serviceName,Application.STOP_INI,sp.getProperty(Application.STOP_INI));
						}
					default : 
						break loop;
				}
			}
			unprepareService();
		} catch (CommandLineParametersException e) {
			printError(128,serviceName,"Command line parameter error: "+e.getLocalizedMessage());
		} catch (SyntaxException e) {
			printError(128,serviceName,"Syntax error: "+e.getLocalizedMessage());
		} catch (IOException | EnvironmentException e) {
			printError(129,serviceName,"I/O error processing config URI: "+e.getClass().getSimpleName()+" - "+e.getLocalizedMessage());
		}
	}

	static void printError(final int rc, final String serviceName, final String error) {
		try{JavaServiceLibrary.print2ServiceLog(serviceName, error);
		} catch (EnvironmentException e) {
		}
		System.exit(rc);
	}
	
	private static void prepareService() throws EnvironmentException {
		JavaServiceLibrary.prepareService();
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
			super(new URIArg(Application.CONF_KEY,false,"config source with service settings ("+Application.CONFIG_FILE+" if not typed)",Application.CONFIG_FILE));
		}
	}
}
