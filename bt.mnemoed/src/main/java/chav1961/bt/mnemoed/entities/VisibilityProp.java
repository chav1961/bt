package chav1961.bt.mnemoed.entities;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class VisibilityProp extends EntityProp {
	private PrimitiveValueSource visibility;

	public VisibilityProp(final PrimitiveValueSource visibility) throws NullPointerException {
		if (visibility == null) {
			throw new NullPointerException("Visibility to set can't be null");
		}
		else {
			this.visibility = visibility;
		}
	}

	public PrimitiveValueSource getVisibility() {
		return visibility;
	}

	public void setVisibility(final PrimitiveValueSource visibility) throws NullPointerException {
		if (visibility == null) {
			throw new NullPointerException("Visibility to set can't be null");
		}
		else {
			this.visibility = visibility;
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
		return "VisibilityProp [visibility=" + visibility + "]";
	}
}
