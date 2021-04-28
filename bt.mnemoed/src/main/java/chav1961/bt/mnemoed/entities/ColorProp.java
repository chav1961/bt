package chav1961.bt.mnemoed.entities;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class ColorProp extends EntityProp {
	private ObjectValueSource	color;

	public ColorProp(final ObjectValueSource color) throws NullPointerException {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			this.color = color;
		}
	}

	public ObjectValueSource getColor() {
		return color;
	}

	public void setColor(final ObjectValueSource color) throws NullPointerException {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			this.color = color;
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
		return "ColorProp [color=" + color + "]";
	}
}
