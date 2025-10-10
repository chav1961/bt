package chav1961.bt.svgeditor.parser;

import java.util.ArrayList;
import java.util.List;

import chav1961.bt.svgeditor.primitives.PrimitiveWrapper;
import chav1961.bt.svgeditor.screen.SVGCanvas;
import chav1961.purelib.basic.CharUtils.Mark;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;

public abstract class AllLastAndSelectedCommandProcessor extends AbstractCommandProcessor {
	private static enum Action {
		ACTION_ALL,
		ACTION_LAST,
		ACTION_SELECTED
	}

	private Action	action;
	
	private final Content<?>[]	VARIANT_1 = {
									new Content<Mark>(Mark.class, new Mark(1), (c,v)->action = Action.ACTION_ALL),
								};
	private final Content<?>[]	VARIANT_2 = {
									new Content<Mark>(Mark.class, new Mark(2), (c,v)->action = Action.ACTION_LAST),
								};
	private final Content<?>[]	VARIANT_3 = {
									new Content<Mark>(Mark.class, new Mark(3), (c,v)->action = Action.ACTION_SELECTED),
								};

	protected AllLastAndSelectedCommandProcessor(final Object... parameters) throws CommandLineParametersException {
		prepareProcessor(parameters, VARIANT_1, VARIANT_2, VARIANT_3);
	}
	
	@Override
	public void execute(SVGCanvas canvas) throws CalculationException {
		final List<PrimitiveWrapper>	toProcess = new ArrayList<>();
		
		switch (action) {
			case ACTION_ALL			:
				canvas.forEach((item)->toProcess.add(item));
				break;
			case ACTION_LAST		:
				break;
			case ACTION_SELECTED	:
				canvas.forEach((item)->{
					if (canvas.isSelected(item)) {
						toProcess.add(item);
					}
				});
				break;
			default:
				throw new UnsupportedOperationException("Action ["+action+"] ia not supported yet");
		}
		process(canvas, toProcess);
	}
	
	protected abstract void process(final SVGCanvas canvas, final List<PrimitiveWrapper> items);
}
