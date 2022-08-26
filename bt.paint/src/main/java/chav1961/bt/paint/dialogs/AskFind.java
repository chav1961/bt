package chav1961.bt.paint.dialogs;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.model.ImageKeeperImpl;
import chav1961.purelib.model.interfaces.ImageKeeper;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.bt.paint.dialogs.AskFind/chav1961/bt/paint/i18n/localization.xml")
@LocaleResource(value="askfind.title",tooltip="askfind.title.tt",help="askfind.title.help")
public class AskFind implements FormManager<Object, AskFind>, ModuleAccessor {
	private final LoggerFacade	facade;

	@LocaleResource(value="askfind.image",tooltip="askfind.image.tt")
	@Format("100*100m")
	public ImageKeeperImpl	image = new ImageKeeperImpl();
	
	@LocaleResource(value="askfind.scaled",tooltip="askfind.scaled.tt")
	@Format("1s")
	public boolean			findScaled = false;

	@LocaleResource(value="askfind.rotated",tooltip="askfind.rotated.tt")
	@Format("1s")
	public boolean			findRotated = false;
	
	public AskFind(final LoggerFacade facade) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			this.facade = facade;
		}
	}
	
	@Override
	public RefreshMode onField(final AskFind inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
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
