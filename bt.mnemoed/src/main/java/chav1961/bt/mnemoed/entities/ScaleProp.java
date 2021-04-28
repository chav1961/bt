package chav1961.bt.mnemoed.entities;

import java.awt.geom.AffineTransform;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class ScaleProp extends AffineEntityProp {
	private PrimitiveValueSource	xScale;
	private PrimitiveValueSource	yScale;
	
	public ScaleProp(final PrimitiveValueSource xScale, final PrimitiveValueSource yScale) throws NullPointerException {
		if (xScale == null) {
			throw new NullPointerException("X scale can't be null");
		}
		else if (yScale == null) {
			throw new NullPointerException("Y scale can't be null");
		}
		else {
			this.xScale = xScale;
			this.yScale = yScale;
		}
	}

	@Override
	public AffineTransform getAffineTransform() {
		return AffineTransform.getScaleInstance(0, 0);
	}
	
	public PrimitiveValueSource getXScale() {
		return xScale;
	}

	public void setXScale(final PrimitiveValueSource xScale) throws NullPointerException {
		if (xScale == null) {
			throw new NullPointerException("X scale to set can't be null");
		}
		else {
			this.xScale = xScale;
		}
	}

	public PrimitiveValueSource getYScale() {
		return yScale;
	}

	public void setYScale(final PrimitiveValueSource yScale) throws NullPointerException {
		if (yScale == null) {
			throw new NullPointerException("Y scale to set can't be null");
		}
		else {
			this.yScale = yScale;
		}
	}

	@Override
	public void upload(final JsonStaxPrinter printer) throws PrintingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void download(final JsonStaxParser parser) throws SyntaxException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String toString() {
		return "ScaleProp [xScale=" + xScale + ", yScale=" + yScale + "]";
	}
}
