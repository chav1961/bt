package chav1961.bt.mnemoed.entities;

import java.io.IOException;

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
	public void upload(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Stax printer can't be null");
		}
		else {
			printer.startObject().name(getArgType(thickness.getClass()).name());
			thickness.upload(printer);
			printer.endObject();
		}
	}

	@Override
	public void download(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser == null) {
			throw new NullPointerException("Stax parser can't be null");
		}
		else {
			setThickness(parsePrimitiveValueSource(parser));
		}
	}
	
	@Override
	public String toString() {
		return "ThicknessProp [thickness=" + thickness + "]";
	}
}
