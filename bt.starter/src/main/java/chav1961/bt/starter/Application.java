package chav1961.bt.starter;

import java.util.Arrays;

import chav1961.bt.starter.impl.GitStarter;
import chav1961.bt.starter.impl.LocalStarter;
import chav1961.bt.starter.impl.SVNStarter;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

public class Application {
	public static final String	CONFIG_FILE	= ".starter.properties";	
	public static final String	MODE_KEY = "mode";	
	public static final String	SOURCE_KEY = "source";	
	public static final String	URI_KEY = "uri";	
	
	public enum ApplicationMode {
		start
	}

	public enum SourceMode {
		local, svn, git
	}
	
	
	public static void main(final String[] args) {
		final ArgParser	parser = new ApplicationArgParser();
		
		try{ArgParser	ap = parser.parse(true,false,args);

			switch (ap.getValue(MODE_KEY,ApplicationMode.class)) {
				case start	:
					if (!ap.isTyped(URI_KEY)) {
						throw new CommandLineParametersException("Repository URI is missing, but it is a mandatory for the given application mode");
					}
					else {
						final String[]	trimmedArgs = args.length > 3 ? Arrays.copyOfRange(args,3,args.length) : new String[0];
						
						switch (ap.getValue(SOURCE_KEY,SourceMode.class)) {
							case git	:
								GitStarter.start(ap,trimmedArgs);
								break;
							case local	:
								LocalStarter.start(ap,trimmedArgs);
								break;
							case svn	:
								SVNStarter.start(ap,trimmedArgs);
								break;
							default:
								throw new UnsupportedOperationException("Source mode ["+ap.getValue(SOURCE_KEY,ApplicationMode.class)+"] is not supported yet"); 
						}
					}
					break;
				default		:
					throw new UnsupportedOperationException("Application mode ["+ap.getValue(MODE_KEY,ApplicationMode.class)+"] is not supported yet"); 
			}
		} catch (CommandLineParametersException e) {
			PureLibSettings.SYSTEM_ERR_LOGGER.message(Severity.severe,"Errors parsing arguments: "+e.getLocalizedMessage());
			PureLibSettings.SYSTEM_ERR_LOGGER.message(Severity.info,parser.getUsage("starter"));
			System.exit(128);
		}
	}

	static class ApplicationArgParser extends ArgParser {
		public ApplicationArgParser() {
			super(new EnumArg<ApplicationMode>(MODE_KEY,ApplicationMode.class,true,true,"Mode to start application. "+Arrays.toString(ApplicationMode.values())+" are available"),
				  new EnumArg<SourceMode>(SOURCE_KEY,SourceMode.class,true,true,"Source to start application. "+Arrays.toString(SourceMode.values())+" are available"),
				  new URIArg(URI_KEY,true,false,"Repository URI in any available format"),
			  	  new ConfigArg("conf",false,"config file with defaults ("+CONFIG_FILE+" if not typed)",CONFIG_FILE)
			);
		}
	}
}
