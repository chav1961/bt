package chav1961.bt.paint.script.interfaces;

import java.awt.BasicStroke;
import java.awt.Stroke;

import chav1961.bt.paint.interfaces.PaintScriptException;

public interface StrokeWrapper {
	public static enum LineStroke {
		SOLID, DASHED, DOTTED;
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
	}
	
	Stroke getStroke();
	StrokeWrapper setWidth(int width) throws PaintScriptException;
	StrokeWrapper setStyle(LineStroke style) throws PaintScriptException;
	StrokeWrapper setCaps(LineCaps style) throws PaintScriptException;
	StrokeWrapper setJoin(LineJoin style) throws PaintScriptException;
	StrokeWrapper setStroke(Stroke stroke);
	StrokeWrapper setStroke(String stroke) throws PaintScriptException;

	static StrokeWrapper of(final Stroke stroke) {
		return null;
	}
	
	static StrokeWrapper of(final String stroke) throws PaintScriptException {
		return null;
	}
}