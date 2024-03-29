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

@LocaleResourceLocation("i18n:xml:root://chav1961.bt.paint.dialogs.AskImageSize/chav1961/bt/paint/i18n/localization.xml")
@LocaleResource(value="askimagesize.title",tooltip="askimagesize.title.tt",help="askimagesize.title.help")
public class AskImageSize implements FormManager<Object, AskImageSize>, ModuleAccessor {
	private final LoggerFacade	facade;
	
	@LocaleResource(value="askimagesize.width",tooltip="askimagesize.width.tt")
	@Format("10ms")
	public int		width = 640;
	
	@LocaleResource(value="askimagesize.height",tooltip="askimagesize.height.tt")
	@Format("10ms")
	public int		height = 480;
	
	@LocaleResource(value="askimagesize.fillbackground",tooltip="askimagesize.fillbackground.tt")
	@Format("1s")
	public boolean	fillBackgroung = true;

	public AskImageSize(final LoggerFacade facade) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			this.facade = facade;
		}
	}
	
	@Override
	public RefreshMode onField(final AskImageSize inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
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
