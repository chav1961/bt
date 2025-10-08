package chav1961.bt.svgeditor.parser;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import chav1961.bt.svgeditor.parser.AbstractCommandProcessor.Content;
import chav1961.bt.svgeditor.primitives.PrimitiveWrapper;
import chav1961.bt.svgeditor.screen.SVGCanvas;
import chav1961.purelib.basic.CharUtils.Mark;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;

public class MoveProcessor extends AbstractCommandProcessor {
	private static enum Action {
		MOVE_ALL,
		MOVE_LAST,
		MOVE_SELECTED
	}

	private Action	action;
	private int		xFrom, yFrom;
	private int		xTo, yTo;
	
	private final Content<?>[]	VARIANT_1 = {
									new Content<Mark>(Mark.class, new Mark(1), (c,v)->action = Action.MOVE_ALL),
									new Content<Integer>(Integer.class, (c,v)->xFrom = v),
									new Content<Integer>(Integer.class, (c,v)->yFrom = v),
									new Content<Integer>(Integer.class, (c,v)->xTo = v),
									new Content<Integer>(Integer.class, (c,v)->yTo = v)
								};
	private final Content<?>[]	VARIANT_2 = {
									new Content<Mark>(Mark.class, new Mark(2), (c,v)->action = Action.MOVE_LAST),
									new Content<Integer>(Integer.class, (c,v)->xFrom = v),
									new Content<Integer>(Integer.class, (c,v)->yFrom = v),
									new Content<Integer>(Integer.class, (c,v)->xTo = v),
									new Content<Integer>(Integer.class, (c,v)->yTo = v)
								};
	private final Content<?>[]	VARIANT_3 = {
									new Content<Mark>(Mark.class, new Mark(3), (c,v)->action = Action.MOVE_SELECTED),
									new Content<Integer>(Integer.class, (c,v)->xFrom = v),
									new Content<Integer>(Integer.class, (c,v)->yFrom = v),
									new Content<Integer>(Integer.class, (c,v)->xTo = v),
									new Content<Integer>(Integer.class, (c,v)->yTo = v)
								};
	private final Content<?>[]	VARIANT_4 = {
									new Content<Mark>(Mark.class, new Mark(1), (c,v)->action = Action.MOVE_ALL),
									new Content<Integer>(Integer.class, (c,v)->xFrom = v),
									new Content<Integer>(Integer.class, (c,v)->yFrom = v),
									new Content<Mark>(Mark.class, new Mark(4), (c,v)->{}),
									new Content<Integer>(Integer.class, (c,v)->xTo = xFrom+v),
									new Content<Integer>(Integer.class, (c,v)->yTo = yFrom+v)
								};
	private final Content<?>[]	VARIANT_5 = {
									new Content<Mark>(Mark.class, new Mark(2), (c,v)->action = Action.MOVE_LAST),
									new Content<Integer>(Integer.class, (c,v)->xFrom = v),
									new Content<Integer>(Integer.class, (c,v)->yFrom = v),
									new Content<Mark>(Mark.class, new Mark(4), (c,v)->{}),
									new Content<Integer>(Integer.class, (c,v)->xTo = xFrom+v),
									new Content<Integer>(Integer.class, (c,v)->yTo = yFrom+v)
								};
	private final Content<?>[]	VARIANT_6 = {
									new Content<Mark>(Mark.class, new Mark(3), (c,v)->action = Action.MOVE_SELECTED),
									new Content<Integer>(Integer.class, (c,v)->xFrom = v),
									new Content<Integer>(Integer.class, (c,v)->yFrom = v),
									new Content<Mark>(Mark.class, new Mark(4), (c,v)->{}),
									new Content<Integer>(Integer.class, (c,v)->xTo = xFrom+v),
									new Content<Integer>(Integer.class, (c,v)->yTo = yFrom+v)
								};
	
	public MoveProcessor(final Object... parameters) throws CommandLineParametersException {
		prepareProcessor(parameters, VARIANT_1, VARIANT_2, VARIANT_3, VARIANT_4, VARIANT_5, VARIANT_6);
	}
	
	@Override
	public void execute(final SVGCanvas canvas) throws CalculationException {
		final List<PrimitiveWrapper>	toMove = new ArrayList<>();
		
		switch (action) {
			case MOVE_ALL			:
				canvas.forEach((item)->toMove.add(item));
				break;
			case MOVE_LAST		:
				break;
			case MOVE_SELECTED	:
				canvas.forEach((item)->{
					if (canvas.isSelected(item)) {
						toMove.add(item);
					}
				});
				break;
			default:
				throw new UnsupportedOperationException("Action ["+action+"] ia not supported yet");
		}
		final AffineTransform	at = new AffineTransform();
		
		if (!toMove.isEmpty()) {
			canvas.beginTransaction(toMove.size() > 1 
					? "Move ["+toMove.size()+"] items" 
					: "Move ["+toMove.get(0).getClass().getSimpleName()+"]");
			at.translate(xTo-xFrom, yTo-yFrom);
			for(PrimitiveWrapper item : toMove) {
				item.setTransform(at);
			}
			canvas.commit();
		}
	}
}
