package chav1961.bt.mnemoed.entities;

import java.io.IOException;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

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
	public void upload(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Stax printer can't be null");
		}
		else {
			printer.startObject().name(getArgType(background.getClass()).name());
			background.upload(printer);
			printer.endObject();
		}
	}

	@Override
	public void download(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser == null) {
			throw new NullPointerException("Stax parser can't be null");
		}
		else {
			setBackground(parseObjectValueSource(parser));
		}
	}
	
	@Override
	public String toString() {
		return "BackgroundProp [background=" + background + "]";
	}
}
