package chav1961.bt.paint.dialogs;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.bt.paint.dialogs.AskImageResize/chav1961/bt/paint/i18n/localization.xml")
@LocaleResource(value="askimageresize.title",tooltip="askimageresize.title.tt",help="askimageresize.title.help")
public class AskImageResize implements FormManager<Object, AskImageSize>, ModuleAccessor {
	private final LoggerFacade	facade;

	@LocaleResource(value="askimagesize.width",tooltip="askimagesize.width.tt")
	@Format("10ms")
	public int		width = 100;
	
	@LocaleResource(value="askimagesize.width",tooltip="askimagesize.width.tt")
	@Format("10ms")
	public int		height = 100;

	@LocaleResource(value="askimagesize.width",tooltip="askimagesize.width.tt")
	@Format("1s")
	public boolean	proprtional = true;
	
	@LocaleResource(value="askimagesize.width",tooltip="askimagesize.width.tt")
	@Format("1s")
	public boolean	stretchContent = false;

	public AskImageResize(final LoggerFacade facade) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			this.facade = facade;
		}
	}
	
	@Override
	public RefreshMode onField(AskImageSize inst, Object id, String fieldName, Object oldValue, boolean beforeCommit) throws FlowException, LocalizationException {
		// TODO Auto-generated method stub
		return null;
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
