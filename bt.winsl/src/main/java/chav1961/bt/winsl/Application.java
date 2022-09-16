package chav1961.bt.winsl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

import chav1961.bt.winsl.interfaces.ErrorControl;
import chav1961.bt.winsl.interfaces.StartType;
import chav1961.bt.winsl.utils.JavaServiceDescriptor;
import chav1961.bt.winsl.utils.JavaServiceLibrary;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;

public class Application {
	public static final String	MODE_KEY = "mode";	
	public static final String	CONF_KEY = "conf";	
	public static final String	DEMO_KEY = "demo";	
	public static final String	CONFIG_FILE = "./service.conf";	

	public static final String	SERVICENAME_INI = "serviceName";
	public static final String	DISPLAYNAME_INI = "displayName";
	public static final String	STARTTYPE_INI  = "startType";
	public static final String	ERRORCONTROL_INI  = "errorControl";
	public static final String	PATH_INI = "path";
	public static final String	ORDERGROUP_INI = "orderGroup";
	public static final String	DEPENDENCIES_INI = "dependencies";
	public static final String	USER_INI = "user";
	public static final String	PASSWORD_INI = "password";

	public static final String	START_INI = "start";
	public static final String	PAUSE_INI = "pause";
	public static final String	RESUME_INI = "resume";
	public static final String	STOP_INI = "stop";
	
	private static final Object	INSTALL_MANDATORIES[] = {SERVICENAME_INI, DISPLAYNAME_INI, STARTTYPE_INI, PATH_INI, START_INI}; 
	private static final Object	UPDATE_MANDATORIES[] = {SERVICENAME_INI}; 
	private static final Object	REMOVE_MANDATORIES[] = {SERVICENAME_INI}; 
	
	public enum ApplicationMode {
		install, remove, update, reinstall
	}

	public static void main(String[] args) {
		final ArgParser		parser = new ApplicationArgParser();

		try{final ArgParser	ap = parser.parse(false, false, args);
		
			if (!System.getProperty("os.name","unknown").toUpperCase().contains("WINDOWS")) {
				throw new CommandLineParametersException("This application can be used in the Windows-based systems only");
			}
		
			final SubstitutableProperties	sp = getConfiguration(ap.getValue(CONF_KEY, URI.class)); 
			final String					serviceName = sp.getProperty(SERVICENAME_INI, String.class);
			
			switch (ap.getValue(MODE_KEY, ApplicationMode.class)) {
				case install	:
					installService(serviceName, sp);
					break;
				case remove		:
					removeService(serviceName,  sp);
					break;
				case update		:
					updateService(serviceName, sp);
					break;
				case reinstall		:
					try{removeService(serviceName,  sp);
					} catch (ContentException exc) {
					}
					installService(serviceName, sp);
					break;
				default	:
					throw new UnsupportedOperationException("Application mode ["+ap.getValue(MODE_KEY, ApplicationMode.class)+"] is not supported yet");
			}
		} catch (CommandLineParametersException e) {
			printError(128,"Command line parameter error: "+e.getLocalizedMessage(),parser.getUsage("winsl"));
		} catch (ContentException e) {
			printError(128,"Action error: "+e.getLocalizedMessage(),parser.getUsage("winsl"));
		} catch (IOException | EnvironmentException e) {
			printError(129,"I/O error processing config URI: "+e.getClass().getSimpleName()+" - "+e.getLocalizedMessage(),parser.getUsage("winsl"));
		}
	}

	private static void printError(final int rc, final String error, final String usage) {
		System.err.println(error);
		System.err.println(usage);
		System.exit(rc);
	}
	
	static SubstitutableProperties getConfiguration(final URI source) throws IOException {
		try(final InputStream	is = source.toURL().openStream()) {
			final SubstitutableProperties	props = new SubstitutableProperties();
			
			props.load(is);
			return props;
		} catch (IllegalArgumentException exc) {
			throw new IOException("Error converting URI ["+source+"]: "+exc.getLocalizedMessage());
		}
	}
	
	private static void installService(final String name, final SubstitutableProperties conf) throws ContentException, EnvironmentException {
		if (conf.containsAllKeys(INSTALL_MANDATORIES)) {
			if (JavaServiceLibrary.queryService(name) == null) {
				final JavaServiceDescriptor	desc = new JavaServiceDescriptor();
				
				desc.lpServiceName = conf.getProperty(SERVICENAME_INI, String.class);
				desc.lpDisplayName = conf.getProperty(DISPLAYNAME_INI, String.class);
				desc.dwStartType = conf.getProperty(STARTTYPE_INI, StartType.class).getStartType();
				desc.lpBinaryPathName = conf.getProperty(PATH_INI, String.class);
				
				desc.dwDesiredAccess = JavaServiceDescriptor.SERVICE_ALL_ACCESS;
				desc.dwServiceType = JavaServiceDescriptor.SERVICE_WIN32_OWN_PROCESS;
				desc.dwErrorControl = conf.getProperty(ERRORCONTROL_INI, ErrorControl.class, "normal").getErrorControl();
				desc.lpLoadOrderGroup = conf.getProperty(ORDERGROUP_INI, String.class);
				desc.lpDependencies = conf.getProperty(DEPENDENCIES_INI, String.class);
				desc.lpServiceStartName = conf.getProperty(USER_INI, String.class);
				desc.lpPassword = conf.getProperty(PASSWORD_INI, String.class);
	
				JavaServiceLibrary.installService(desc);
			}
			else {
				throw new ContentException("Service to install ["+name+"] already exists on your computer");
			}
		}
		else {
			throw new ContentException("Configuration error: some mandatory parameters are missing in the config file. At least "+Arrays.toString(INSTALL_MANDATORIES)+" must be typed");
		}
	}

	private static void updateService(final String name, final SubstitutableProperties conf) throws ContentException, EnvironmentException {
		if (conf.containsAllKeys(UPDATE_MANDATORIES)) {
			final JavaServiceDescriptor	desc = JavaServiceLibrary.queryService(name);
			
			if (desc != null) {
				desc.lpServiceName = conf.getProperty(SERVICENAME_INI, String.class);
				desc.lpDisplayName = conf.getProperty(DISPLAYNAME_INI, String.class);
				desc.dwStartType = conf.getProperty(STARTTYPE_INI, StartType.class).getStartType();
				desc.lpBinaryPathName = conf.getProperty(PATH_INI, String.class);
				
				desc.dwDesiredAccess = JavaServiceDescriptor.GENERIC_ALL;
				desc.dwServiceType = JavaServiceDescriptor.SERVICE_WIN32_OWN_PROCESS;
				desc.dwErrorControl = conf.getProperty(ERRORCONTROL_INI, ErrorControl.class, "normal").getErrorControl();
				desc.lpLoadOrderGroup = conf.getProperty(ORDERGROUP_INI, String.class);
				desc.lpDependencies = conf.getProperty(DEPENDENCIES_INI, String.class);
				desc.lpServiceStartName = conf.getProperty(USER_INI, String.class);
				desc.lpPassword = conf.getProperty(PASSWORD_INI, String.class);

				JavaServiceLibrary.updateService(desc);
			}
			else {
				throw new ContentException("Service to update ["+name+"] is not installed on your computer");
			}
		}
		else {
			throw new ContentException("Configuration error: some mandatory parameters are missing in the config file. At least "+Arrays.toString(UPDATE_MANDATORIES)+" must be typed");
		}
	}
	
	private static void removeService(final String name, final SubstitutableProperties conf) throws EnvironmentException, ContentException {
		if (conf.containsAllKeys(REMOVE_MANDATORIES)) {
			JavaServiceDescriptor  d;
			
			if ((d = JavaServiceLibrary.queryService(name)) != null) {
				JavaServiceLibrary.removeService(name);
			}
			else {
				throw new ContentException("Service to remove ["+name+"] is not installed on your computer");
			}
		}
		else {
			throw new ContentException("Configuration error: some mandatory parameters are missing in the config file. At least "+Arrays.toString(UPDATE_MANDATORIES)+" must be typed");
		}
	}

	static class ApplicationArgParser extends ArgParser {
		public ApplicationArgParser() {
			super(new EnumArg<ApplicationMode>(MODE_KEY,ApplicationMode.class,true,true,"Mode to start application. "+Arrays.toString(ApplicationMode.values())+" are available"),
			  	  new URIArg(CONF_KEY,false,"config source with service settings ("+CONFIG_FILE+" if not typed)",CONFIG_FILE),
			  	  new StringArg(SERVICENAME_INI,false,"service name to manage","unknown"),
			  	  new BooleanArg(DEMO_KEY,false,"prepare demo service",false)
			);
		}
	}
}
