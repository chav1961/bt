package chav1961.bt.installer;

import java.awt.SplashScreen;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.XMLLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;
import chav1961.purelib.ui.swing.useful.JDialogContainer;
import chav1961.purelib.ui.swing.useful.JDialogContainer.JDialogContainerOption;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class Application {
	private static final long serialVersionUID = -9219912076315302665L;
	
	public static final String	ARG_FORCE = "f";

	public static final String	SETTINGS_RESOURCE_NAME = "/settings.props"; 
	public static final String	LOCALIZING_STRINGS_RESOURCE_NAME = "/i18n.xml"; 
	
	public static final String	SETTINGS_TITLE = "installer.title"; 
	public static final String	SETTINGS_SCREEN_SIZE = "screen.size"; 
	public static final String	SETTINGS_DEFAULT_SCREEN_SIZE = "640x480";

	public static final File	PREV_SETTINGS = new File(".prev.settings");
	
	public static void processMain(final SplashScreen splash, final String[] args) {
		final Args			arguments = new Args();
		
		try {
			final URI		localizerURI = URI.create(XMLLocalizer.LOCALIZER_SCHEME+":xml:"+Application.class.getResource(LOCALIZING_STRINGS_RESOURCE_NAME).toURI().toASCIIString());
			
			try(final Localizer		localizer = LocalizerFactory.getLocalizer(localizerURI, Application.class.getClassLoader())) {
				final ArgParser 	parms = arguments.parse(args);
				final Properties	settings = SubstitutableProperties.of(Application.class.getResourceAsStream(SETTINGS_RESOURCE_NAME));
				final Properties	prevSettings = new Properties();

				System.err.println("Settings: "+settings);
				
				PureLibSettings.PURELIB_LOCALIZER.push(localizer);
				if (!parms.getValue(ARG_FORCE, boolean.class) && PREV_SETTINGS.exists() && PREV_SETTINGS.isFile() && PREV_SETTINGS.canRead()) {
					if (new JLocalizedOptionPane(localizer).confirm(null, prevSettings, SETTINGS_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						try(final InputStream	is = new FileInputStream(PREV_SETTINGS)) {
							
							prevSettings.load(is);
						}
					}
				}
				final List<WizardStep>	steps = new ArrayList<>();
				final ErrorProcessing	errProc = new ErrorProcessing() {
											@Override
											public void processWarning(Object content, Enum err, Object... parameters) throws LocalizationException {
												// TODO Auto-generated method stub
												
											}
										};
				final JDialogContainer	window = new JDialogContainer(localizer, (JFrame)null, null, errProc, steps.toArray(new WizardStep[steps.size()]));
				
				Thread.sleep(5000);
				
				if (splash != null) {
					splash.close();
				}
				window.showDialog(JDialogContainerOption.DONT_USE_ENTER_AS_OK);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (CommandLineParametersException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			if (splash != null && splash.isVisible()) {
				splash.close();
			}
		}
	}
	
	private static class Args extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new BooleanArg(ARG_FORCE, false, false, "force previous settings"),
		};
		
		private Args() {
			super(KEYS);
		}
	}
}
