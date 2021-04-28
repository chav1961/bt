package chav1961.bt.mnemoed.entities;

import java.awt.geom.AffineTransform;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class AngleProp extends AffineEntityProp {
	private PrimitiveValueSource	angle;
	
	public AngleProp(final PrimitiveValueSource angle) throws NullPointerException {
		if (angle == null) {
			throw new NullPointerException("Angle to set can't be null");
		}
		else {
			this.angle = angle;
		}
	}

	@Override
	public AffineTransform getAffineTransform() {
		return AffineTransform.getRotateInstance(0);
	}

	public PrimitiveValueSource getAngle() {
		return angle;
	}

	public void setAngle(final PrimitiveValueSource angle) throws NullPointerException {
		if (angle == null) {
			throw new NullPointerException("Angle to set can't be null");
		}
		else {
			this.angle = angle;
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
		return "AngleProp [angle=" + angle + "]";
	}
}
