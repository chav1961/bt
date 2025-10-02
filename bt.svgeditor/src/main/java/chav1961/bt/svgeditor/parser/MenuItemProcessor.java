package chav1961.bt.svgeditor.parser;

import chav1961.bt.svgeditor.screen.SVGCanvas;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MenuItemProcessor extends AbstractCommandProcessor {
	private final String	action;
	
	public MenuItemProcessor(final String menuItem) {
		if (Utils.checkEmptyOrNullString(menuItem)) {
			throw new IllegalArgumentException("Menu item can be neither null nor empty");
		}
		else {
			this.action = menuItem;
		}
	}

	@Override
	public void execute(final SVGCanvas canvas) throws CalculationException {
		Component	c = canvas;
		
		while (c != null) {
			final Class<Component>	cl = (Class<Component>) c.getClass();
			
			for (Method m : cl.getDeclaredMethods()) {
				if (m.isAnnotationPresent(OnAction.class) && m.getAnnotation(OnAction.class).value().equals(action)) {
					m.setAccessible(true);
					try {
						m.invoke(c);
					} catch (InvocationTargetException exc) {
						SwingUtils.getNearestLogger(canvas).message(Severity.error, exc.getCause(), exc.getCause().getLocalizedMessage());
					} catch (IllegalAccessException exc) {
						SwingUtils.getNearestLogger(canvas).message(Severity.error, exc, exc.getLocalizedMessage());
					}
					return;
				}
			}
			c = c.getParent();
		}
		throw new IllegalStateException("Action string ["+action+"] not found anywhere");
	}

}
