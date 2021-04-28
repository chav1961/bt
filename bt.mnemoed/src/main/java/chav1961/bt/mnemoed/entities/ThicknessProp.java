package chav1961.bt.mnemoed.entities;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class ThicknessProp extends EntityProp {
	private PrimitiveValueSource	thickness;

	public ThicknessProp(final PrimitiveValueSource thickness) throws NullPointerException {
		if (thickness == null) {
			throw new NullPointerException("Thickness to set can't be null"); 
		}
		else {
			this.thickness = thickness;
		}
	}

	public PrimitiveValueSource getThickness() {
		return thickness;
	}

	public void setThickness(final PrimitiveValueSource thickness) throws NullPointerException {
		if (thickness == null) {
			throw new NullPointerException("Thickness to set can't be null"); 
		}
		else {
			this.thickness = thickness;
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
		return "ThicknessProp [thickness=" + thickness + "]";
	}
}
