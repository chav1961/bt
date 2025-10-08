package chav1961.bt.svgeditor.parser;

import java.io.IOException;

import chav1961.bt.svgeditor.internal.AppWindow;
import chav1961.bt.svgeditor.screen.SVGCanvas;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.ui.swing.SwingUtils;

public class HelpProcessor extends AbstractCommandProcessor {
	public HelpProcessor(final Object... parameters) throws CommandLineParametersException {
	}

	@Override
	public void execute(final SVGCanvas canvas) throws CalculationException {
		try {
			SwingUtils.showCreoleHelpWindow(
					SwingUtils.getNearestOwner(canvas, AppWindow.class), 
					canvas.getLocalizer(), 
					"chav1961.bt.svgeditor.Application.help.commands");
		} catch (IOException e) {
			throw new CalculationException(e);
		}
	}

}
