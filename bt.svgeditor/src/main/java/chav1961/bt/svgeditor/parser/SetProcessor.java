package chav1961.bt.svgeditor.parser;

import chav1961.bt.svgeditor.dialogs.SettingsDialog;
import chav1961.bt.svgeditor.dialogs.SettingsDialog.PropKeys;
import chav1961.bt.svgeditor.internal.AppWindow;
import chav1961.bt.svgeditor.screen.SVGCanvas;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.ui.swing.SwingUtils;

public class SetProcessor extends MenuItemProcessor {
	private String	key = null;
	private String	value = null;
	
	private final Content<?>[]	VARIANT_1 = {
			new Content<String>(String.class, (c,v)->key = v.trim()),
			new Content<String>(String.class, (c,v)->value = v.trim().replace("\uFFFF", ""))
		};
	
	public SetProcessor(final String menuItem, final Object... parameters) throws CommandLineParametersException {
		super(menuItem);
		if (parameters[0] != null) {
			prepareProcessor(parameters, VARIANT_1);
		}
	}
	
	@Override
	public void execute(final SVGCanvas canvas) throws CalculationException {
		if (key != null) {
			final SubstitutableProperties	props = SwingUtils.getNearestOwner(canvas, AppWindow.class).getProperties();
			final PropKeys	propKey = SettingsDialog.PropKeys.byName(key); 

			if (canConvertTo(propKey.getPropClass(), value)) {
				try {
					switch (propKey) {
						case ORTHO_MODE:
							props.setProperty(key, SQLUtils.convert(propKey.getPropClass(), value).toString());
							break;
						case UNKNOWN:
							throw new CalculationException("Unknown property name ["+key+"] to set"); 
						default:
							throw new UnsupportedOperationException("Property key ["+propKey+"] is not supported yet");
					}
				} catch (ContentException e) {
					throw new CalculationException("Property value ["+value+"] can't convert to type ["+propKey.getPropClass()+"]"); 
				}
			}
			else {
				throw new CalculationException("Property value ["+value+"] can't convert to type ["+propKey.getPropClass()+"]"); 
			}
		}
		else {
			super.execute(canvas);
		}
	}

	private boolean canConvertTo(final Class<?> clazz, final String value) {
		switch (CompilerUtils.defineClassType(CompilerUtils.fromWrappedClass(clazz))) {
			case CompilerUtils.CLASSTYPE_REFERENCE	:
				return true;
			case CompilerUtils.CLASSTYPE_BYTE		:
				try {
					Byte.valueOf(value);
					return true;
				} catch (IllegalArgumentException exc) {
					return false;
				}
			case CompilerUtils.CLASSTYPE_SHORT		:
				try {
					Short.valueOf(value);
					return true;
				} catch (IllegalArgumentException exc) {
					return false;
				}
			case CompilerUtils.CLASSTYPE_CHAR		:
				return value.length() == 1;
			case CompilerUtils.CLASSTYPE_INT		:	
				try {
					Integer.valueOf(value);
					return true;
				} catch (IllegalArgumentException exc) {
					return false;
				}
			case CompilerUtils.CLASSTYPE_LONG		:	
				try {
					Long.valueOf(value);
					return true;
				} catch (IllegalArgumentException exc) {
					return false;
				}
			case CompilerUtils.CLASSTYPE_FLOAT		:	
				try {
					Float.valueOf(value);
					return true;
				} catch (IllegalArgumentException exc) {
					return false;
				}
			case CompilerUtils.CLASSTYPE_DOUBLE		:	
				try {
					Double.valueOf(value);
					return true;
				} catch (IllegalArgumentException exc) {
					return false;
				}
			case CompilerUtils.CLASSTYPE_BOOLEAN	:	
				try {
					Boolean.valueOf(value);
					return true;
				} catch (IllegalArgumentException exc) {
					return false;
				}
			default :
				throw new UnsupportedOperationException("Class type ["+CompilerUtils.defineClassType(CompilerUtils.fromWrappedClass(clazz))+"] is not supported yet");
		}
	}
}
