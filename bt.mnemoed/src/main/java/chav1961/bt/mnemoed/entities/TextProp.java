package chav1961.bt.mnemoed.entities;

import java.io.IOException;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public class TextProp extends EntityProp {
	private ObjectValueSource	text;
	private ObjectValueSource	align;

	public TextProp(final ObjectValueSource text, final ObjectValueSource align) throws NullPointerException {
		if (text == null) {
			throw new NullPointerException("Text to set can't be null");
		}
		else if (align == null) {
			throw new NullPointerException("Text alignment to set can't be null");
		}
		else {
			this.text = text;
			this.align = align;
		}
	}

	public ObjectValueSource getText() {
		return text;
	}

	public void setText(final ObjectValueSource text) throws NullPointerException {
		if (text == null) {
			throw new NullPointerException("Text to set can't be null");
		}
		else {
			this.text = text;
		}
	}

	public ObjectValueSource getAlign() {
		return align;
	}

	public void setAlign(ObjectValueSource align) throws NullPointerException {
		if (align == null) {
			throw new NullPointerException("Text alignment to set can't be null");
		}
		else {
			this.align = align;
		}
	}

	@Override
	public void upload(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Stax printer can't be null");
		}
		else {
			printer.startArray();
			printer.startObject().name(getArgType(text.getClass()).name());
			text.upload(printer);
			printer.endObject();
			printer.startObject().name(getArgType(align.getClass()).name());
			align.upload(printer);
			printer.endObject();
			printer.endArray();
		}
	}

	@Override
	public void download(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser.current() == JsonStaxParserLexType.START_ARRAY) {
			parser.next();
			setText(parseObjectValueSource(parser));
			if (parser.current() == JsonStaxParserLexType.LIST_SPLITTER) {
				parser.next();
				setAlign(parseObjectValueSource(parser));
			}
			else {
				throw new SyntaxException(parser.row(), parser.col(), "',' is missing");
			}
			if (parser.current() == JsonStaxParserLexType.END_ARRAY) {
				parser.next();
			}
			else {
				throw new SyntaxException(parser.row(), parser.col(), "']' is missing");
			}
		}
		else {
			throw new SyntaxException(parser.row(), parser.col(), "'[' is missing");
		}
	}
	
	@Override
	public String toString() {
		return "TextProp [text=" + text + ", align=" + align + "]";
	}
}
