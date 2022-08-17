package chav1961.bt.paint.script.interfaces;

import java.awt.BasicStroke;
import java.awt.Stroke;

import chav1961.bt.paint.control.ImageUtils;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.StrokeWrapperImpl;
import chav1961.purelib.basic.exceptions.SyntaxException;

public interface StrokeWrapper extends ContentWrapper<BasicStroke> {
	public static enum LineStroke {
		SOLID, 
		DASHED, 
		DOTTED;
		
		public static LineStroke valueOf(final float[] dashArray) {
			if (dashArray == null || dashArray.length == 0) {
				return SOLID;
			}
			else {
				float	minVal = dashArray[0], maxVal = minVal;
				
				for (float item : dashArray) {
					minVal = Math.min(minVal, item);
					maxVal = Math.max(minVal, item);
				}
				return maxVal / minVal > 2 ? DASHED : DOTTED;
			}
		}
	}

	public static enum LineCaps {
		BUTT(BasicStroke.CAP_BUTT), 
		ROUND(BasicStroke.CAP_ROUND), 
		SQUARE(BasicStroke.CAP_SQUARE);
		
		private final int	capsType;
		
		private LineCaps(final int capsType) {
			this.capsType = capsType;
		}
		
		public int getCapsType() {
			return capsType;
		}
		
		public static LineCaps valueOf(final int capsType) {
			for (LineCaps item : values()) {
				if (item.getCapsType() == capsType) {
					return item;
				}
			}
			throw new IllegalArgumentException("Caps type ["+capsType+"] is not found");
		}
	}

	public static enum LineJoin {
		MITER(BasicStroke.JOIN_MITER), 
		ROUND(BasicStroke.JOIN_ROUND), 
		BEVEL(BasicStroke.JOIN_BEVEL);

		private final int	joinType;
		
		private LineJoin(final int joinType) {
			this.joinType = joinType;
		}
		
		public int getJoinType() {
			return joinType;
		}
	
		public static LineJoin valueOf(final int joinType) {
			for (LineJoin item : values()) {
				if (item.getJoinType() == joinType) {
					return item;
				}
			}
			throw new IllegalArgumentException("Join type ["+joinType+"] is not found");
		}
	}
	
	BasicStroke getStroke();
	StrokeWrapper setWidth(int width) throws PaintScriptException;
	StrokeWrapper setStyle(LineStroke style) throws PaintScriptException;
	StrokeWrapper setCaps(LineCaps style) throws PaintScriptException;
	StrokeWrapper setJoin(LineJoin style) throws PaintScriptException;
	StrokeWrapper setStroke(BasicStroke stroke);
	StrokeWrapper setStroke(String stroke) throws PaintScriptException;

	static StrokeWrapper of(final BasicStroke stroke) {
		return new StrokeWrapperImpl(stroke);
	}
	
	static StrokeWrapper of(final String stroke) throws PaintScriptException {
		if (stroke == null || stroke.isEmpty()) {
			throw new IllegalArgumentException("Stroke string can't be null or empty");
		}
		else {
			try{
				return new StrokeWrapperImpl(ImageUtils.buildStroke(stroke));
			} catch (SyntaxException e) {
				throw new PaintScriptException(e);
			}
		}
	}
}