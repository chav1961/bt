package chav1961.bt.mnemoed.entities;

import java.io.IOException;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public class FontProp extends EntityProp {
	private ObjectValueSource		family;
	private PrimitiveValueSource	size;
	private PrimitiveValueSource	style;
	
	public FontProp(final ObjectValueSource family, final PrimitiveValueSource size, final PrimitiveValueSource style) throws NullPointerException {
		if (family == null) {
			throw new NullPointerException("Font family to set can't be null"); 
		}
		else if (size == null) {
			throw new NullPointerException("Font size to set can't be null"); 
		}
		else if (style == null) {
			throw new NullPointerException("Font style to set can't be null"); 
		}
		else {
			this.family = family;
			this.size = size;
			this.style = style;
		}
	}

	public ObjectValueSource getFamily() {
		return family;
	}

	public void setFamily(final ObjectValueSource family) throws NullPointerException {
		if (family == null) {
			throw new NullPointerException("Font family to set can't be null"); 
		}
		else {
			this.family = family;
		}
	}

	public PrimitiveValueSource getSize() {
		return size;
	}

	public void setSize(final PrimitiveValueSource size) throws NullPointerException {
		if (size == null) {
			throw new NullPointerException("Font size to set can't be null"); 
		}
		else {
			this.size = size;
		}
	}

	public PrimitiveValueSource getStyle() {
		return style;
	}

	public void setStyle(final PrimitiveValueSource style) throws NullPointerException {
		if (style == null) {
			throw new NullPointerException("Font style to set can't be null"); 
		}
		else {
			this.style = style;
		}
	}

	@Override
	public void upload(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Stax printer can't be null");
		}
		else {
			printer.startArray();
			printer.startObject().name(getArgType(family.getClass()).name());
			family.upload(printer);
			printer.endObject();
			printer.startObject().name(getArgType(size.getClass()).name());
			size.upload(printer);
			printer.endObject();
			printer.startObject().name(getArgType(style.getClass()).name());
			style.upload(printer);
			printer.endObject();
			printer.endArray();
		}
	}

	@Override
	public void download(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser == null) {
			throw new NullPointerException("Stax parser can't be null");
		}
		else {
			if (parser.current() == JsonStaxParserLexType.START_ARRAY) {
				parser.next();
				setFamily(parseObjectValueSource(parser));
				if (parser.current() == JsonStaxParserLexType.LIST_SPLITTER) {
					parser.next();
					setSize(parsePrimitiveValueSource(parser));
				}
				else {
					throw new SyntaxException(parser.row(), parser.col(), "',' is missing");
				}
				if (parser.current() == JsonStaxParserLexType.LIST_SPLITTER) {
					parser.next();
					setStyle(parsePrimitiveValueSource(parser));
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
	}
	
	@Override
	public String toString() {
		return "FontProp [family=" + family + ", size=" + size + ", style=" + style + "]";
	}
}
