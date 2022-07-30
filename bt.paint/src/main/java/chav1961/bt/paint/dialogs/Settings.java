package chav1961.bt.paint.dialogs;

import java.io.File;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.bt.paint.dialogs.Settings/chav1961/bt/paint/i18n/localization.xml")
@LocaleResource(value="settings.title",tooltip="settings.title.tt",help="settings.title.help")
public class Settings implements FormManager<Object, Settings>, ModuleAccessor {
	private final LoggerFacade	facade;
	
	@LocaleResource(value="settings.plugindir",tooltip="settings.plugindir.tt")
	@Format("20m")
	public File					pluginDir = new File("./plugins");

	@LocaleResource(value="settings.imagesdir",tooltip="settings.imagesdir.tt")
	@Format("20m")
	public File 				defaultImagesDir =  new File("./");
	
	public Settings(final LoggerFacade facade) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			this.facade = facade;
		}
	}
	
	@Override
	public RefreshMode onField(Settings inst, Object id, String fieldName, Object oldValue, boolean beforeCommit) throws FlowException, LocalizationException {
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
}
