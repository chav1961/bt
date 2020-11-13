package chav1961.bt.xsys;

import java.io.IOException;
import java.io.InputStream;

import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class Application {
	public static final String	ACTION = "action";
	public static final String	CONTENT = "content";
	public static final String	FRB_LOCATION = "frb";
	public static final String	DEFAULT_FRB_LOCATION = "./xsys.frb";
	public static final String	FRB_SIZE = "frbsize";
	public static final String	DEFAULT_FRB_SIZE = "100m";
	public static final String	TO = "to";
	public static final String	DEFAULT_TO = "./xsys.frb.backup";
	public static final String	FROM = "from";
	public static final String	DEFAULT_FROM = "./xsys.frb.backup";
	public static final String	OUTPUT = "output";
	public static final String	DEFAULT_OUTPUT = "./output";
	
	public enum Actions {
		prepare,	// -frb <location> -frbsize <size>
		consult,	// <contentURI>,... -frb <location>
		backup,		// -to <backupFileName>
		restore,	// -from <backupFileName>
		build,		// -output <fileSystemURI>
		screen
	}

	private static CommandLineParametersException buildMandatoryMissingException(final Actions action, final String... parameters) {
		final StringBuilder sb = new StringBuilder();
		char				prefix = '[';

		sb.append("Action [").append(action).append("] requires mandatory parameters");
		for (String item : parameters) {
			sb.append(prefix).append(' ').append(item).append(' ');
			prefix = ',';
		}
		sb.append("], but some of them are missing in the command line");
		
		return new CommandLineParametersException(sb.toString());
	}
	
	public static void main(String[] args) {
		final ArgParser			argParser = new ApplicationArgParser();
		
		try(final InputStream				is = Application.class.getResourceAsStream("application.xml");
			final Localizer					localizer = PureLibSettings.PURELIB_LOCALIZER) {
			
			final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
			
			final ArgParser					parsed = argParser.parse(true,true,args);

			switch (parsed.getValue(ACTION, Actions.class)) {
				case backup		:
					if (parsed.allAreTyped(FRB_LOCATION, TO)) {
						
					}
					else {
						throw buildMandatoryMissingException(Actions.backup, FRB_LOCATION, TO);
					}
					break;
				case build		:
					if (parsed.allAreTyped(FRB_LOCATION, OUTPUT)) {
						
					}
					else {
						throw buildMandatoryMissingException(Actions.build, FRB_LOCATION, OUTPUT);
					}
					break;
				case consult	:
					if (parsed.allAreTyped(FRB_LOCATION, CONTENT)) {
						
					}
					else {
						throw buildMandatoryMissingException(Actions.consult, FRB_LOCATION, CONTENT);
					}
					break;
				case prepare	:
					if (parsed.allAreTyped(FRB_LOCATION, FRB_SIZE)) {
						
					}
					else {
						throw buildMandatoryMissingException(Actions.prepare, FRB_LOCATION, FRB_SIZE);
					}
					break;
				case restore	:
					if (parsed.allAreTyped(FRB_LOCATION, FROM)) {
						
					}
					else {
						throw buildMandatoryMissingException(Actions.restore, FRB_LOCATION, FROM);
					}
					break;
				case screen		:
					if (parsed.isTyped(FRB_LOCATION)) {
						
					}
					else {
						throw buildMandatoryMissingException(Actions.screen, FRB_LOCATION);
					}
					break;
				default:
					throw new CommandLineParametersException("Action ["+parsed.getValue(ACTION, Actions.class)+"] is not supported yet");
			}
		} catch (CommandLineParametersException e) {
			System.err.println("Error starting application: "+e.getLocalizedMessage());
			System.err.println(argParser.getUsage("xsys"));
			System.exit(129);
		} catch (IOException | EnvironmentException e) {
			System.err.println("Error starting application: "+e.getLocalizedMessage());
			System.exit(129);
		}
	}

	private static class ApplicationArgParser extends ArgParser {
		public ApplicationArgParser() {
			super(new EnumArg<Actions>(ACTION,Actions.class,true,true,"Action mode")
				 ,new StringListArg(CONTENT,false,true,"URI list to consult into FRB")
				 ,new URIArg(FRB_LOCATION,false,"FRB location",DEFAULT_FRB_LOCATION)
				 ,new StringArg(FRB_SIZE,false,"FRB size location",DEFAULT_FRB_SIZE)
				 ,new URIArg(TO,false,"backup file location to store to",DEFAULT_TO)
				 ,new URIArg(FROM,false,"backup file location to load from",DEFAULT_FROM)
			);
		}
	}
	
}
