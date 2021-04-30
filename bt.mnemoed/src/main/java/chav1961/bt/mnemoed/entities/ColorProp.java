package chav1961.bt.mnemoed.entities;

import java.io.IOException;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

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
	public void upload(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Stax printer can't be null");
		}
		else {
			printer.startObject().name(getArgType(color.getClass()).name());
			color.upload(printer);
			printer.endObject();
		}
	}

	@Override
	public void download(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser == null) {
			throw new NullPointerException("Stax parser can't be null");
		}
		else {
			setColor(parseObjectValueSource(parser));
		}
	}
	
	@Override
	public String toString() {
		return "ColorProp [color=" + color + "]";
	}
}
