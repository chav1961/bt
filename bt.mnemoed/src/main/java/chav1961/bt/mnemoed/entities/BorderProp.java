package chav1961.bt.mnemoed.entities;

import java.awt.geom.AffineTransform;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public class BorderProp extends AffineEntityProp {
	private ObjectValueSource	border;
	
	public BorderProp(final ObjectValueSource border) throws NullPointerException {
		if (border == null) {
			throw new NullPointerException("Border to set can't be null");
		}
		else {
			this.border = border;
		}
	}

	@Override
	public AffineTransform getAffineTransform() {
		return null;
	}

	public ObjectValueSource getBorder() {
		return border;
	}

	public void setBorder(final ObjectValueSource border) throws NullPointerException {
		if (border == null) {
			throw new NullPointerException("Border to set can't be null");
		}
		else {
			this.border = border;
		}
	}

	@Override
	public void upload(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Stax printer can't be null");
		}
		else {
			printer.startObject().name(getArgType(border.getClass()).name());
			border.upload(printer);
			printer.endObject();
		}
	}

	@Override
	public void download(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser == null) {
			throw new NullPointerException("Stax parser can't be null");
		}
		else {
			setBorder(parseObjectValueSource(parser));
		}
	}
	
	@Override
	public String toString() {
		return "BorderProp [border=" + border + "]";
	}
}
