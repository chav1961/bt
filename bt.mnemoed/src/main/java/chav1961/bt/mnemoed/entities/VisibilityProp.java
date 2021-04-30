package chav1961.bt.mnemoed.entities;

import java.io.IOException;

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
	public void upload(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Stax printer can't be null");
		}
		else {
			printer.startObject().name(getArgType(visibility.getClass()).name());
			visibility.upload(printer);
			printer.endObject();
		}
	}

	@Override
	public void download(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser == null) {
			throw new NullPointerException("Stax parser can't be null");
		}
		else {
			setVisibility(parsePrimitiveValueSource(parser));
		}
	}
	
	@Override
	public String toString() {
		return "VisibilityProp [visibility=" + visibility + "]";
	}
}
