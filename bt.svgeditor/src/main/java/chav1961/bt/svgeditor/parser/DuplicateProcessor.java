package chav1961.bt.svgeditor.parser;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import chav1961.bt.svgeditor.primitives.LineWrapper;
import chav1961.bt.svgeditor.primitives.PrimitiveWrapper;
import chav1961.bt.svgeditor.screen.SVGCanvas;
import chav1961.purelib.basic.CharUtils.Mark;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;

public class DuplicateProcessor extends AbstractCommandProcessor {
	private static enum Action {
		DUPLICATE_LAST,
		DUPLICATE_SELECTED
	}
	
	private Action	action;
	private int		toX, toY;

	private final Content<?>[]	VARIANT_1 = {
										new Content<Mark>(Mark.class, new Mark(1), (c,v)->{action = Action.DUPLICATE_LAST;}),
										new Content<Integer>(Integer.class,(c,v)->toX = v),
										new Content<Integer>(Integer.class,(c,v)->toY = v),
									};
	private final Content<?>[]	VARIANT_2 = {
										new Content<Mark>(Mark.class, new Mark(2), (c,v)->{action = Action.DUPLICATE_SELECTED;}),
										new Content<Integer>(Integer.class,(c,v)->toX = v),
										new Content<Integer>(Integer.class,(c,v)->toY = v),
									};
	
	public DuplicateProcessor(final Object... parameters) throws CommandLineParametersException {
		prepareProcessor(parameters, VARIANT_1, VARIANT_2);
	}

	@Override
	public void execute(final SVGCanvas canvas) throws CalculationException {
		final List<PrimitiveWrapper>	toDuplicate = new ArrayList<>();
		
		switch (action) {
			case DUPLICATE_LAST		:
				break;
			case DUPLICATE_SELECTED	:
				canvas.forEach((item)->{
					if (canvas.isSelected(item)) {
						toDuplicate.add(item);
					}
				});
				break;
			default:
				throw new UnsupportedOperationException("Action ["+action+"] ia not supported yet");
		}
		if (!toDuplicate.isEmpty()) {
			final List<PrimitiveWrapper>	temp = new ArrayList<>();
			final AffineTransform	at = new AffineTransform();
			
			at.translate(toX, toY);
			for(PrimitiveWrapper item : toDuplicate) {
				try {
					final PrimitiveWrapper	wrapper = (PrimitiveWrapper) item.clone();
					
					wrapper.setTransform(at);
					wrapper.commitChanges();
					temp.add(wrapper);
				} catch (CloneNotSupportedException e) {
					throw new CalculationException(e);
				}
			}
			canvas.add(temp.toArray(new PrimitiveWrapper[temp.size()]));
		}
	}
}
