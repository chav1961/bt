package chav1961.bt.paint.dialogs;

import java.io.File;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.bt.paint.dialogs.AskSettings/chav1961/bt/paint/i18n/localization.xml")
@LocaleResource(value="asksettings.title",tooltip="asksettings.title.tt",help="asksettings.title.help")
public class AskSettings implements FormManager<Object, AskSettings>, ModuleAccessor {
	private static final String	SETTINGS_PLUGIN_DIR = "settings.pluginDir";
	private static final String	SETTINGS_IMAGE_DIR = "settings.imageDir";
	
	private final LoggerFacade	facade;
	
	@LocaleResource(value="asksettings.plugindir",tooltip="asksettings.plugindir.tt")
	@Format("20m")
	public File					pluginDir = new File("./plugins");

	@LocaleResource(value="asksettings.imagesdir",tooltip="asksettings.imagesdir.tt")
	@Format("20m")
	public File 				defaultImagesDir =  new File("./");
	
	public AskSettings(final LoggerFacade facade) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			this.facade = facade;
		}
	}
	
	@Override
	public RefreshMode onField(AskSettings inst, Object id, String fieldName, Object oldValue, boolean beforeCommit) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}
	
	@Override
	public LoggerFacade getLogger() {
		return facade;
	}
	
	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}
	
	public void fillFrom(final SubstitutableProperties props) {
		pluginDir = props.getProperty(SETTINGS_PLUGIN_DIR, File.class, pluginDir.getAbsolutePath());
		defaultImagesDir = props.getProperty(SETTINGS_IMAGE_DIR, File.class, defaultImagesDir.getAbsolutePath());
	}

	public void saveTo(final SubstitutableProperties props) {
		props.setProperty(SETTINGS_PLUGIN_DIR, pluginDir.getAbsolutePath());
		props.setProperty(SETTINGS_IMAGE_DIR, defaultImagesDir.getAbsolutePath());
	}
}
