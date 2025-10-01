package chav1961.bt.svgeditor.parser;

import chav1961.bt.svgeditor.primitives.LineWrapper;

import chav1961.bt.svgeditor.screen.SVGCanvas;
import chav1961.purelib.basic.CharUtils.Mark;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;

public class LineProcessor extends AbstractCommandProcessor {
	private int		fromX, fromY;
	private int		toX, toY;

	private final Content<?>[]	VARIANT_1 = {
										new Content<Integer>(Integer.class,(c,v)->fromX = v),
										new Content<Integer>(Integer.class,(c,v)->fromY = v),
										new Content<Integer>(Integer.class,(c,v)->toX = v),
										new Content<Integer>(Integer.class,(c,v)->toY = v)
									};
	private final Content<?>[]	VARIANT_2 = {
										new Content<Integer>(Integer.class,(c,v)->fromX = v),
										new Content<Integer>(Integer.class,(c,v)->fromY = v),
										new Content<Mark>(Mark.class, new Mark(1), (c,v)->{}),
										new Content<Integer>(Integer.class,(c,v)->toX = fromX+v),
										new Content<Integer>(Integer.class,(c,v)->toY = fromY+v)
									};
	
	public LineProcessor(final Object... parameters) throws CommandLineParametersException {
		prepareProcessor(parameters, VARIANT_1, VARIANT_2);
	}

	@Override
	public void execute(final SVGCanvas canvas) throws CalculationException {
		canvas.add(new LineWrapper(fromX, fromY, toX, toY));
	}
}
