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
public class AskImageResize implements FormManager<Object, AskImageResize>, ModuleAccessor {
	private final LoggerFacade	facade;

	@LocaleResource(value="askimageresize.width",tooltip="askimageresize.width.tt")
	@Format("10ms")
	public int		width = 100;
	
	@LocaleResource(value="askimageresize.height",tooltip="askimageresize.height.tt")
	@Format("10ms")
	public int		height = 100;

	@LocaleResource(value="askimageresize.proportional",tooltip="askimageresize.proportional.tt")
	@Format("1s")
	public boolean	proportional = true;
	
	@LocaleResource(value="askimageresize.stretch",tooltip="askimageresize.stretch.tt")
	@Format("1s")
	public boolean	stretchContent = false;

	@LocaleResource(value="askimageresize.atcenter",tooltip="askimageresize.atcenter.tt")
	@Format("1s")
	public boolean	fromCenter = false;
	
	public AskImageResize(final LoggerFacade facade) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			this.facade = facade;
		}
	}
	
	@Override
	public RefreshMode onField(final AskImageResize inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		// TODO Auto-generated method stub
		switch (fieldName) {
			case "width" 	:
				if (proportional) {
					height = (int) (1.0 * height * width / ((Number)oldValue).intValue());
					return RefreshMode.RECORD_ONLY;
				}
				else {
					return RefreshMode.DEFAULT;
				}
			case "height"	:
				if (proportional) {
					width = (int) (1.0 * width * height / ((Number)oldValue).intValue());
					return RefreshMode.RECORD_ONLY;
				}
				else {
					return RefreshMode.DEFAULT;
				}
			default :
				return RefreshMode.DEFAULT;
		}
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
