package chav1961.bt.svgeditor.parser;

import java.util.ArrayList;
import java.util.List;

import chav1961.bt.svgeditor.parser.AbstractCommandProcessor.Content;
import chav1961.bt.svgeditor.primitives.PrimitiveWrapper;
import chav1961.bt.svgeditor.screen.SVGCanvas;
import chav1961.purelib.basic.CharUtils.Mark;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;

public class DeleteProcessor extends AbstractCommandProcessor {
	private static enum Action {
		DELETE_ALL,
		DELETE_LAST,
		DELETE_SELECTED
	}

	private Action	action;
	
	private final Content<?>[]	VARIANT_1 = {
									new Content<Mark>(Mark.class, new Mark(1), (c,v)->action = Action.DELETE_ALL),
								};
	private final Content<?>[]	VARIANT_2 = {
									new Content<Mark>(Mark.class, new Mark(2), (c,v)->action = Action.DELETE_LAST),
								};
	private final Content<?>[]	VARIANT_3 = {
									new Content<Mark>(Mark.class, new Mark(3), (c,v)->action = Action.DELETE_SELECTED),
								};
	
	public DeleteProcessor(final Object... parameters) throws CommandLineParametersException {
		prepareProcessor(parameters, VARIANT_1, VARIANT_2, VARIANT_3);
	}
	
	@Override
	public void execute(final SVGCanvas canvas) throws CalculationException {
		final List<PrimitiveWrapper>	toDelete = new ArrayList<>();
		
		switch (action) {
			case DELETE_ALL			:
				canvas.forEach((item)->toDelete.add(item));
				break;
			case DELETE_LAST		:
				break;
			case DELETE_SELECTED	:
				canvas.forEach((item)->{
					if (canvas.isSelected(item)) {
						toDelete.add(item);
					}
				});
				break;
			default:
				throw new UnsupportedOperationException("Action ["+action+"] ia not supported yet");
		}
		for(PrimitiveWrapper item : toDelete) {
			canvas.delete(item);
		}
	}

}
