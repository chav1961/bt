package chav1961.bt.svgeditor.parser;

import java.awt.geom.Rectangle2D;

import chav1961.bt.svgeditor.parser.AbstractCommandProcessor.Content;
import chav1961.bt.svgeditor.primitives.PrimitiveWrapper;
import chav1961.bt.svgeditor.screen.SVGCanvas;
import chav1961.purelib.basic.CharUtils.Mark;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;

public class SelectionProcessor extends AbstractCommandProcessor {
	private int		ulX, ulY;
	private int		drX, drY;
	private int		ptX, ptY;
	private int		dist = -1;

	private static interface Tester {
		boolean isApplicable(SelectionProcessor proc, SVGCanvas canvas, PrimitiveWrapper item);
	}
	
	private enum Action {
		RESET_ALL((p,c,i)->true),
		SELECT_ALL((p,c,i)->true),
		SELECT_WINDOW((p,c,i)->i.isInside(p.getRectangle())),
		SELECT_CROSSING((p,c,i)->true),
		SELECT_LAST((p,c,i)->true),
		SELECT_NEAREST((p,c,i)->true),
		APPEND_WINDOW((p,c,i)->true),
		APPEND_CROSSING((p,c,i)->true),
		APPEND_LAST((p,c,i)->true),
		APPEND_NEAREST((p,c,i)->true),
		REMOVE_WINDOW((p,c,i)->true),
		REMOVE_CROSSING((p,c,i)->true),
		REMOVE_LAST((p,c,i)->true),
		REMOVE_NEAREST((p,c,i)->true);
		
		private final Tester	tester;
		
		private Action(final Tester tester) {
			this.tester = tester;
		}
		
		public boolean isApplicable(final SelectionProcessor proc, final SVGCanvas canvas, final PrimitiveWrapper item) {
			return tester.isApplicable(proc, canvas, item);
		}
	}
	
	private Action	action;
	
	private final Content<?>[]	VARIANT_1 = {
									new Content<Mark>(Mark.class, new Mark(1), (c,v)->action = Action.RESET_ALL),
								};
	private final Content<?>[]	VARIANT_2 = {
									new Content<Mark>(Mark.class, new Mark(2), (c,v)->action = Action.SELECT_ALL),
								};
	private final Content<?>[]	VARIANT_3 = {
									new Content<Mark>(Mark.class, new Mark(5), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(6), (c,v)->action = Action.SELECT_WINDOW),
									new Content<Integer>(Integer.class, (c,v)->ulX = v),
									new Content<Integer>(Integer.class, (c,v)->ulY = v),
									new Content<Integer>(Integer.class, (c,v)->drX = v),
									new Content<Integer>(Integer.class, (c,v)->drY = v)
								};
	private final Content<?>[]	VARIANT_4 = {
									new Content<Mark>(Mark.class, new Mark(5), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(6), (c,v)->action = Action.SELECT_WINDOW),
									new Content<Integer>(Integer.class, (c,v)->ulX = v),
									new Content<Integer>(Integer.class, (c,v)->ulY = v),
									new Content<Mark>(Mark.class, new Mark(10), (c,v)->{}),
									new Content<Integer>(Integer.class, (c,v)->drX = ulX + v),
									new Content<Integer>(Integer.class, (c,v)->drY = ulY + v)
								};
	private final Content<?>[]	VARIANT_5 = {
									new Content<Mark>(Mark.class, new Mark(5), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(7), (c,v)->action = Action.SELECT_CROSSING),
									new Content<Integer>(Integer.class, (c,v)->ulX = v),
									new Content<Integer>(Integer.class, (c,v)->ulY = v),
									new Content<Integer>(Integer.class, (c,v)->drX = v),
									new Content<Integer>(Integer.class, (c,v)->drY = v)
								};
	private final Content<?>[]	VARIANT_6 = {
									new Content<Mark>(Mark.class, new Mark(5), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(7), (c,v)->action = Action.SELECT_CROSSING),
									new Content<Integer>(Integer.class, (c,v)->ulX = v),
									new Content<Integer>(Integer.class, (c,v)->ulY = v),
									new Content<Mark>(Mark.class, new Mark(10), (c,v)->{}),
									new Content<Integer>(Integer.class, (c,v)->drX = ulX + v),
									new Content<Integer>(Integer.class, (c,v)->drY = ulY + v)
								};
	private final Content<?>[]	VARIANT_7 = {
									new Content<Mark>(Mark.class, new Mark(5), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(8), (c,v)->action = Action.SELECT_LAST),
								};
	private final Content<?>[]	VARIANT_8 = {
									new Content<Mark>(Mark.class, new Mark(5), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(9), (c,v)->action = Action.SELECT_NEAREST),
									new Content<Integer>(Integer.class, (c,v)->ptX = v),
									new Content<Integer>(Integer.class, (c,v)->ptY = v),
									new Content<Integer>(Integer.class, (c,v)->dist = v)
								};
	private final Content<?>[]	VARIANT_9 = {
									new Content<Mark>(Mark.class, new Mark(5), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(9), (c,v)->action = Action.SELECT_NEAREST),
									new Content<Integer>(Integer.class, (c,v)->ptX = v),
									new Content<Integer>(Integer.class, (c,v)->ptY = v)
								};

	private final Content<?>[]	VARIANT_10 = {
									new Content<Mark>(Mark.class, new Mark(3), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(6), (c,v)->action = Action.APPEND_WINDOW),
									new Content<Integer>(Integer.class, (c,v)->ulX = v),
									new Content<Integer>(Integer.class, (c,v)->ulY = v),
									new Content<Integer>(Integer.class, (c,v)->drX = v),
									new Content<Integer>(Integer.class, (c,v)->drY = v)
								};
	private final Content<?>[]	VARIANT_11 = {
									new Content<Mark>(Mark.class, new Mark(3), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(6), (c,v)->action = Action.APPEND_WINDOW),
									new Content<Integer>(Integer.class, (c,v)->ulX = v),
									new Content<Integer>(Integer.class, (c,v)->ulY = v),
									new Content<Mark>(Mark.class, new Mark(10), (c,v)->{}),
									new Content<Integer>(Integer.class, (c,v)->drX = ulX + v),
									new Content<Integer>(Integer.class, (c,v)->drY = ulY + v)
								};
	private final Content<?>[]	VARIANT_12 = {
									new Content<Mark>(Mark.class, new Mark(3), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(7), (c,v)->action = Action.APPEND_CROSSING),
									new Content<Integer>(Integer.class, (c,v)->ulX = v),
									new Content<Integer>(Integer.class, (c,v)->ulY = v),
									new Content<Integer>(Integer.class, (c,v)->drX = v),
									new Content<Integer>(Integer.class, (c,v)->drY = v)
								};
	private final Content<?>[]	VARIANT_13 = {
									new Content<Mark>(Mark.class, new Mark(3), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(7), (c,v)->action = Action.APPEND_CROSSING),
									new Content<Integer>(Integer.class, (c,v)->ulX = v),
									new Content<Integer>(Integer.class, (c,v)->ulY = v),
									new Content<Mark>(Mark.class, new Mark(10), (c,v)->{}),
									new Content<Integer>(Integer.class, (c,v)->drX = ulX + v),
									new Content<Integer>(Integer.class, (c,v)->drY = ulY + v)
								};
	private final Content<?>[]	VARIANT_14 = {
									new Content<Mark>(Mark.class, new Mark(3), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(8), (c,v)->action = Action.APPEND_LAST),
								};
	private final Content<?>[]	VARIANT_15 = {
									new Content<Mark>(Mark.class, new Mark(3), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(9), (c,v)->action = Action.APPEND_NEAREST),
									new Content<Integer>(Integer.class, (c,v)->ptX = v),
									new Content<Integer>(Integer.class, (c,v)->ptY = v),
									new Content<Integer>(Integer.class, (c,v)->dist = v)
								};
	private final Content<?>[]	VARIANT_16 = {
									new Content<Mark>(Mark.class, new Mark(3), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(9), (c,v)->action = Action.APPEND_NEAREST),
									new Content<Integer>(Integer.class, (c,v)->ptX = v),
									new Content<Integer>(Integer.class, (c,v)->ptY = v)
								};

	private final Content<?>[]	VARIANT_17 = {
									new Content<Mark>(Mark.class, new Mark(4), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(6), (c,v)->action = Action.REMOVE_WINDOW),
									new Content<Integer>(Integer.class, (c,v)->ulX = v),
									new Content<Integer>(Integer.class, (c,v)->ulY = v),
									new Content<Integer>(Integer.class, (c,v)->drX = v),
									new Content<Integer>(Integer.class, (c,v)->drX = v)
								};
	private final Content<?>[]	VARIANT_18 = {
									new Content<Mark>(Mark.class, new Mark(4), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(6), (c,v)->action = Action.REMOVE_WINDOW),
									new Content<Integer>(Integer.class, (c,v)->ulX = v),
									new Content<Integer>(Integer.class, (c,v)->ulY = v),
									new Content<Mark>(Mark.class, new Mark(10), (c,v)->{}),
									new Content<Integer>(Integer.class, (c,v)->drX = ulX + v),
									new Content<Integer>(Integer.class, (c,v)->drY = ulY + v)
								};
	private final Content<?>[]	VARIANT_19 = {
									new Content<Mark>(Mark.class, new Mark(4), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(7), (c,v)->action = Action.REMOVE_CROSSING),
									new Content<Integer>(Integer.class, (c,v)->ulX = v),
									new Content<Integer>(Integer.class, (c,v)->ulY = v),
									new Content<Integer>(Integer.class, (c,v)->drX = v),
									new Content<Integer>(Integer.class, (c,v)->drY = v)
								};
	private final Content<?>[]	VARIANT_20 = {
									new Content<Mark>(Mark.class, new Mark(4), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(7), (c,v)->action = Action.REMOVE_CROSSING),
									new Content<Integer>(Integer.class, (c,v)->ulX = v),
									new Content<Integer>(Integer.class, (c,v)->ulY = v),
									new Content<Mark>(Mark.class, new Mark(10), (c,v)->{}),
									new Content<Integer>(Integer.class, (c,v)->drX = ulX + v),
									new Content<Integer>(Integer.class, (c,v)->drY = ulY + v)
								};
	private final Content<?>[]	VARIANT_21 = {
									new Content<Mark>(Mark.class, new Mark(4), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(8), (c,v)->action = Action.REMOVE_LAST),
								};
	private final Content<?>[]	VARIANT_22 = {
									new Content<Mark>(Mark.class, new Mark(4), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(9), (c,v)->action = Action.REMOVE_NEAREST),
									new Content<Integer>(Integer.class, (c,v)->ptX = v),
									new Content<Integer>(Integer.class, (c,v)->ptY = v),
									new Content<Integer>(Integer.class, (c,v)->dist = v)
								};
	private final Content<?>[]	VARIANT_23 = {
									new Content<Mark>(Mark.class, new Mark(4), (c,v)->{}),
									new Content<Mark>(Mark.class, new Mark(9), (c,v)->action = Action.REMOVE_NEAREST),
									new Content<Integer>(Integer.class, (c,v)->ptX = v),
									new Content<Integer>(Integer.class, (c,v)->ptY = v)
								};
	
	public SelectionProcessor(final Object... parameters) throws CommandLineParametersException {
		prepareProcessor(parameters, VARIANT_1, VARIANT_2, VARIANT_3, VARIANT_4, VARIANT_5
								, VARIANT_6, VARIANT_7, VARIANT_8, VARIANT_9, VARIANT_10
								, VARIANT_11, VARIANT_12, VARIANT_13, VARIANT_14, VARIANT_15
								, VARIANT_16, VARIANT_17, VARIANT_18, VARIANT_19, VARIANT_20
								, VARIANT_21, VARIANT_22, VARIANT_23);
	}
	
	@Override
	public void execute(final SVGCanvas canvas) throws CalculationException {
		switch (action) {
			case APPEND_CROSSING	:
				canvas.forEach((item)->{
					if (action.isApplicable(SelectionProcessor.this, canvas, item)) {
						canvas.setSelected(item, true);
					}
				});
				break;
			case APPEND_LAST		:
				canvas.forEach((item)->{
					if (action.isApplicable(SelectionProcessor.this, canvas, item)) {
						canvas.setSelected(item, true);
					}
				});
				break;
			case APPEND_NEAREST		:
				canvas.forEach((item)->{
					if (action.isApplicable(SelectionProcessor.this, canvas, item)) {
						canvas.setSelected(item, true);
					}
				});
				break;
			case APPEND_WINDOW		:
				canvas.forEach((item)->{
					if (action.isApplicable(SelectionProcessor.this, canvas, item)) {
						canvas.setSelected(item, true);
					}
				});
				break;
			case REMOVE_CROSSING	:
				canvas.forEach((item)->{
					if (action.isApplicable(SelectionProcessor.this, canvas, item)) {
						canvas.setSelected(item, false);
					}
				});
				break;
			case REMOVE_LAST		:
				canvas.forEach((item)->{
					if (action.isApplicable(SelectionProcessor.this, canvas, item)) {
						canvas.setSelected(item, false);
					}
				});
				break;
			case REMOVE_NEAREST		:
				canvas.forEach((item)->{
					if (action.isApplicable(SelectionProcessor.this, canvas, item)) {
						canvas.setSelected(item, false);
					}
				});
				break;
			case REMOVE_WINDOW		:
				canvas.forEach((item)->{
					if (action.isApplicable(SelectionProcessor.this, canvas, item)) {
						canvas.setSelected(item, false);
					}
				});
				break;
			case RESET_ALL			:
				canvas.forEach((item)->canvas.setSelected(item, false));
				break;
			case SELECT_ALL			:
				canvas.forEach((item)->canvas.setSelected(item, true));
				break;
			case SELECT_CROSSING	:
				canvas.forEach((item)->canvas.setSelected(item, action.isApplicable(SelectionProcessor.this, canvas, item)));
				break;
			case SELECT_LAST		:
				canvas.forEach((item)->canvas.setSelected(item, action.isApplicable(SelectionProcessor.this, canvas, item)));
				break;
			case SELECT_NEAREST		:
				canvas.forEach((item)->canvas.setSelected(item, action.isApplicable(SelectionProcessor.this, canvas, item)));
				break;
			case SELECT_WINDOW		:
				canvas.forEach((item)->canvas.setSelected(item, action.isApplicable(SelectionProcessor.this, canvas, item)));
				break;
			default:
				throw new UnsupportedOperationException("Action ["+action+"] is not supported yet");
		}
	}
	
	private Rectangle2D getRectangle() {
		return new Rectangle2D.Double(Math.min(ulX, drX), Math.min(ulY, drY), Math.max(ulX, drX) - Math.min(ulX, drX), Math.max(ulY, drY) - Math.min(ulY, drY)); 
	}
}
