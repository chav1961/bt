package chav1961.bt.svgeditor.dialogs;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.bt.svgeditor.dialogs.SettingsDialog/chav1961/bt/svgeditor/i18n/localization.xml")
@LocaleResource(value="settingsdialog.title",tooltip="settingsdialog.title.tt",help="settingsdialog.title.help")
public class SettingsDialog implements FormManager<Object, SettingsDialog>, ModuleAccessor {
	private static final int	MINIMUM_DEFAULT_GRID = 1;
	private static final String	ERROR_AT_DISTANCE_TOO_SMALL = "settingsdialog.error.atDistanceTooSmall";
	private static final int	MINIMUM_DEFAULT_DISTANCE = 1;
	private static final String	ERROR_GRID_DISTANCE_TOO_SMALL = "settingsdialog.error.gridDistanceTooSmall";
	
	
	
	@FunctionalInterface
	private static interface EditorCallback {
		void process (SubstitutableProperties props, String key, SettingsDialog settings);
	}

	public static enum PropKeys {
		UNKNOWN("", Object.class, "", (p,k,s)->{},(p,k,s)->{}),
		GRID_MODE("grid.mode", boolean.class, "gridOn", 
				(p,k,s)->s.gridOn = p.getProperty(k, boolean.class, "false"),
				(p,k,s)->p.put(k, String.valueOf(s.gridOn))
				),
		ORTHO_MODE("ortho.mode", boolean.class, "orthoOn", 
				(p,k,s)->s.orthoOn = p.getProperty(k, boolean.class, "false"),
				(p,k,s)->p.put(k, String.valueOf(s.orthoOn))
				),
		DEFAULT_GRID("default.grid", int.class, "defaultGrid", 
				(p,k,s)->s.defaultGrid = p.getProperty(k, int.class, ""+MINIMUM_DEFAULT_GRID),
				(p,k,s)->p.put(k, String.valueOf(s.defaultGrid))
				),
		DEFAULT_DISTANCE("default.dist", int.class, "defaultDistance", 
				(p,k,s)->s.defaultDistance = p.getProperty(k, int.class, ""+MINIMUM_DEFAULT_DISTANCE),
				(p,k,s)->p.put(k, String.valueOf(s.defaultDistance))
				);
		
		private final String	propKey;
		private final Class<?>	propClass;
		private final String	fieldName;
		private final EditorCallback	load;
		private final EditorCallback	store;
		
		private PropKeys(final String propKey, final Class<?> propClass, final String fieldName, final EditorCallback load, final EditorCallback store) {
			this.propKey = propKey;
			this.fieldName = fieldName;
			this.propClass = propClass;
			this.load = load;
			this.store = store;
		}
		
		public String getPropKey() {
			return propKey;
		}
		
		public Class<?> getPropClass() {
			return propClass;
		}
		
		public static PropKeys byName(final String name) {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Name can be neither null nor empty");
			}
			else {
				for(PropKeys item : values()) {
					if (item.getPropKey().equals(name)) {
						return item;
					}
				}
				return PropKeys.UNKNOWN;
			}
		}
	}
	
	private final LoggerFacade	facade;
	private final SubstitutableProperties	props; 

	@LocaleResource(value="settingsdialog.gridOn",tooltip="settingsdialog.gridOn.tt")
	@Format("1ms")
	public boolean	gridOn = false;
	
	@LocaleResource(value="settingsdialog.orthoOn",tooltip="settingsdialog.orthoOn.tt")
	@Format("1ms")
	public boolean	orthoOn = false;

	@LocaleResource(value="settingsdialog.defaultGrid",tooltip="settingsdialog.defaultGrid.tt")
	@Format("5ms")
	public int		defaultGrid = 10;
	
	@LocaleResource(value="settingsdialog.defaultDistance",tooltip="settingsdialog.defaultDistance.tt")
	@Format("5ms")
	public int		defaultDistance = 1;
	
	public SettingsDialog(final LoggerFacade facade, final SubstitutableProperties props) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null");
		}
		else if (props == null) {
			throw new NullPointerException("Properties can't be null");
		}
		else {
			this.facade = facade;
			this.props = props;
			for(PropKeys item : PropKeys.values()) {
				item.load.process(props, item.getPropKey(), this);
			}
		}
	}
	
	@Override
	public RefreshMode onField(final SettingsDialog inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		if (beforeCommit) {
			for(PropKeys item : PropKeys.values()) {
				if (item.fieldName.equals(fieldName)) {
					item.store.process(props, item.getPropKey(), this);
					break;
				}
			}
		}
		else {
			switch (fieldName) {
				case "defaultGrid" :
					if (defaultGrid < MINIMUM_DEFAULT_GRID) {
						getLogger().message(Severity.warning, ERROR_GRID_DISTANCE_TOO_SMALL, MINIMUM_DEFAULT_GRID);
						return RefreshMode.REJECT;
					}
					break;
				case "defaultDistance" :
					if (defaultDistance < MINIMUM_DEFAULT_DISTANCE) {
						getLogger().message(Severity.warning, ERROR_AT_DISTANCE_TOO_SMALL, MINIMUM_DEFAULT_DISTANCE);
						return RefreshMode.REJECT;
					}
					break;
			}
		}
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
