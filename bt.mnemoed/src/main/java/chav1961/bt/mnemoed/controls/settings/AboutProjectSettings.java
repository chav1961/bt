package chav1961.bt.mnemoed.controls.settings;

import java.net.URI;
import java.util.Date;

import chav1961.bt.mnemort.interfaces.ApplicationUIType;
import chav1961.bt.mnemort.interfaces.NavigatorStyle;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.bt.mnemoed.controls.settings.AboutProjectSettings/chav1961/mnemoed/i18n/localization.xml")
@LocaleResource(value="chav1961.bt.mnemoed.controls.settings.AboutProjectSettings",tooltip="chav1961.bt.mnemoed.controls.settings.AboutProjectSettings.tt",help="help.aboutApplication")
public class AboutProjectSettings implements FormManager<Object,AboutProjectSettings>, ModuleAccessor {
	private final LoggerFacade 	logger;
	
	@LocaleResource(value="chav1961.bt.mnemoed.controls.settings.AboutProjectSettings.splashBackground",tooltip="chav1961.bt.mnemoed.controls.settings.AboutProjectSettings.splashBackground.tt")
	@Format("30m")
	public URI					splashBackground = URI.create("file:./");

	@LocaleResource(value="chav1961.bt.mnemoed.controls.settings.AboutProjectSettings.uiType",tooltip="chav1961.bt.mnemoed.controls.settings.AboutProjectSettings.uiType.tt")
	@Format("30m")
	public ApplicationUIType	uiType = ApplicationUIType.WEB; 

	@LocaleResource(value="chav1961.bt.mnemoed.controls.settings.AboutProjectSettings.navStyle",tooltip="chav1961.bt.mnemoed.controls.settings.AboutProjectSettings.navStyle.tt")
	@Format("30m")
	public NavigatorStyle		navStyle = NavigatorStyle.ICON_SQUARE; 

	@LocaleResource(value="chav1961.bt.mnemoed.controls.settings.AboutProjectSettings.copyright",tooltip="chav1961.bt.mnemoed.controls.settings.AboutProjectSettings.copyright.tt")
	@Format("30m")
	public String				copyright = "(c) "+new Date(System.currentTimeMillis()).getYear(); 

	@LocaleResource(value="chav1961.bt.mnemoed.controls.settings.AboutProjectSettings.license",tooltip="chav1961.bt.mnemoed.controls.settings.AboutProjectSettings.license.tt")
	@Format("30m")
	public String				license = "?"; 
	
	public AboutProjectSettings(final LoggerFacade logger) {
		this.logger = logger;
	}
	
	@Override
	public RefreshMode onField(final AboutProjectSettings inst, final Object id, final String fieldName, final Object oldValue, boolean beforeCommit) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}

	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}

	@Override
	public LoggerFacade getLogger() {
		return logger;
	}
}
