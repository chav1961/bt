package chav1961.bt.mnemoed.entities;

import java.awt.geom.AffineTransform;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class LocationProp extends AffineEntityProp {
	private PrimitiveValueSource	xLocation;
	private PrimitiveValueSource	yLocation;

	public LocationProp(final PrimitiveValueSource xLocation, final PrimitiveValueSource yLocation) {
		if (xLocation == null) {
			throw new NullPointerException("X location can't be null");
		}
		else if (yLocation == null) {
			throw new NullPointerException("Y Location can't be null");
		}
		else {
			this.xLocation = xLocation;
			this.yLocation = yLocation;
		}
	}

	@Override
	public AffineTransform getAffineTransform() {
		return AffineTransform.getTranslateInstance(0, 0);
	}

	public PrimitiveValueSource getxLocation() {
		return xLocation;
	}

	public void setxLocation(final PrimitiveValueSource xLocation) throws NullPointerException {
		if (xLocation == null) {
			throw new NullPointerException("X location to set can't be null");
		}
		else {
			this.xLocation = xLocation;
		}
	}

	public PrimitiveValueSource getyLocation() {
		return yLocation;
	}

	public void setyLocation(final PrimitiveValueSource yLocation) throws NullPointerException {
		if (xLocation == null) {
			throw new NullPointerException("X location to set can't be null");
		}
		else {
			this.yLocation = yLocation;
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
		return "LocationProp [xLocation=" + xLocation + ", yLocation=" + yLocation + "]";
	}
}
