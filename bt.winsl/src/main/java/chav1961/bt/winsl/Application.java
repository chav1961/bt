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
	public static final String	SERVICE_NAME_KEY = "name";	
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

	private static final String	INSTALL_MANDATORIES[] = {SERVICENAME_INI, DISPLAYNAME_INI, STARTTYPE_INI, PATH_INI}; 
	
	public enum ApplicationMode {
		install, remove, update, manage, start
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final ArgParser		parser = new ApplicationArgParser();

		try{final ArgParser	ap = parser.parse(false, false, args);
		
			switch (ap.getValue(MODE_KEY, ApplicationMode.class)) {
				case manage		:
					if (ap.isTyped(CONF_KEY)) {
						try(final InputStream	is = ap.getValue(CONF_KEY, URI.class).toURL().openStream()) {
							manageService(getConfiguration(is));
						}
					}
					else {
						try(final InputStream	is = new InputStream() {@Override public int read() throws IOException {return -1;}}) {
							manageService(getConfiguration(is));
						}
					}
					break;
				case install	:
					try(final InputStream	is = ap.getValue(CONF_KEY, URI.class).toURL().openStream()) {
						installService(getConfiguration(is));
					}
					break;
				case remove		:
					removeService(ap.getValue(SERVICE_NAME_KEY, String.class));
					break;
				case update		:
					try(final InputStream	is = ap.getValue(CONF_KEY, URI.class).toURL().openStream()) {
						updateService(ap.getValue(SERVICE_NAME_KEY, String.class), getConfiguration(is));
					}
					break;
				default	:
					throw new UnsupportedOperationException("Application mode ["+ap.getValue(MODE_KEY, ApplicationMode.class)+"] is not supported yet");
			}
		} catch (CommandLineParametersException e) {
			System.err.println("Command line parameter error: "+e.getLocalizedMessage());
			System.err.println(parser.getUsage("winsl"));
			System.exit(128);
		} catch (ContentException e) {
			System.err.println("Action error: "+e.getLocalizedMessage());
			System.err.println(parser.getUsage("winsl"));
			System.exit(128);
		} catch (IOException | EnvironmentException e) {
			System.err.println("I/O error processing config URI: "+e.getLocalizedMessage());
			System.err.println(parser.getUsage("winsl"));
			System.exit(129);
		}
	}

	private static SubstitutableProperties getConfiguration(final InputStream is) throws IOException {
		final SubstitutableProperties	props = new SubstitutableProperties();
		
		props.load(is);
		return props;
	}
	
	private static void installService(final SubstitutableProperties conf) throws ContentException, EnvironmentException {
		if (conf.containsKey(SERVICENAME_INI) && conf.containsKey(DISPLAYNAME_INI) && conf.containsKey(STARTTYPE_INI) && conf.containsKey(PATH_INI)) {
			final JavaServiceDescriptor	desc = new JavaServiceDescriptor();
			
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

			JavaServiceLibrary.installService(desc);
		}
		else {
			throw new ContentException("Configuration error: some mandatory parameters are missing in the config file. At least "+Arrays.toString(INSTALL_MANDATORIES)+" must be typed");
		}
	}

	private static void updateService(final String name, final SubstitutableProperties conf) {
		// TODO Auto-generated method stub
		
	}
	
	private static void removeService(final String serviceName) throws EnvironmentException, ContentException {
		JavaServiceLibrary.removeService(serviceName);
	}

	private static void manageService(final SubstitutableProperties conf) {
		// TODO Auto-generated method stub
		
	}

	static class ApplicationArgParser extends ArgParser {
		public ApplicationArgParser() {
			super(new EnumArg<ApplicationMode>(MODE_KEY,ApplicationMode.class,true,true,"Mode to start application. "+Arrays.toString(ApplicationMode.values())+" are available"),
			  	  new URIArg(CONF_KEY,false,"config source with service settings ("+CONFIG_FILE+" if not typed)",CONFIG_FILE),
			  	  new StringArg(SERVICE_NAME_KEY,false,"service name to manage","unknown")
			);
		}
	}
}
