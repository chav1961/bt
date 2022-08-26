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

@LocaleResourceLocation("i18n:xml:root://chav1961.bt.paint.dialogs.AskFindAndReplace/chav1961/bt/paint/i18n/localization.xml")
@LocaleResource(value="askfindandreplace.title",tooltip="askfindandreplace.title.tt",help="askfindandreplace.title.help")
public class AskFindAndReplace implements FormManager<Object, AskFindAndReplace>, ModuleAccessor {
	private final LoggerFacade	facade;

	@LocaleResource(value="askfindandreplace.image",tooltip="askfindandreplace.image.tt")
	@Format("100*100ms")
	public ImageKeeperImpl	image = new ImageKeeperImpl();
	
	@LocaleResource(value="askfindandreplace.scaled",tooltip="askfindandreplace.scaled.tt")
	@Format("1s")
	public boolean			findScaled = false;

	@LocaleResource(value="askfindandreplace.rotated",tooltip="askfindandreplace.rotated.tt")
	@Format("1s")
	public boolean			findRotated = false;

	@LocaleResource(value="askfindandreplace.replacedimage",tooltip="askfindandreplace.replacedimage.tt")
	@Format("100*100m")
	public ImageKeeperImpl	replaced = new ImageKeeperImpl();

	@LocaleResource(value="askfindandreplace.fit",tooltip="askfindandreplace.fit.tt")
	@Format("1s")
	public boolean			fit = false;

	@LocaleResource(value="askfindandreplace.proportional",tooltip="askfindandreplace.proportional.tt")
	@Format("1s")
	public boolean			proprotional = false;

	@LocaleResource(value="askfindandreplace.replaceAll",tooltip="askfindandreplace.replaceAll.tt")
	@Format("1s")
	public boolean			replaceAll = false;
	
	public AskFindAndReplace(final LoggerFacade facade) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			this.facade = facade;
		}
	}
	
	@Override
	public RefreshMode onField(final AskFindAndReplace inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
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
