package chav1961.bt.mnemoed.entities;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class BackgroundProp extends EntityProp {
	private ObjectValueSource	background;

	public BackgroundProp(final ObjectValueSource background) throws NullPointerException {
		if (background == null) {
			throw new NullPointerException("Background to set can't be null");
		}
		else {
			this.background = background;
		}
	}

	public ObjectValueSource getBackground() {
		return background;
	}

	public void setBackground(final ObjectValueSource background) throws NullPointerException {
		if (background == null) {
			throw new NullPointerException("Background to set can't be null");
		}
		else {
			this.background = background;
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
		return "BackgroundProp [background=" + background + "]";
	}
}
