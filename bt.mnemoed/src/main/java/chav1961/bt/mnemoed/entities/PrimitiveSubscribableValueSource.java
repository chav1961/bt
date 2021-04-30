package chav1961.bt.mnemoed.entities;

import java.io.IOException;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public final  class PrimitiveSubscribableValueSource extends PrimitiveValueSource {
	private String	name;
	
	public PrimitiveSubscribableValueSource(final String name) throws IllegalArgumentException {
		super(ValueSourceType.PRIMITIVE_SUBSCRIBABLE);
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name can't be null or empty"); 
		}
		else {
			this.name = name;
		}
	}
	
	public String getName() {
		return name;
	}

	@Override
	public void upload(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Stax printer can't be null"); 
		}
		else {
			printer.startObject().name("name").value(name).endObject();
		}
	}

	@Override
	public void download(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser == null) {
			throw new NullPointerException("Stax printer can't be null");
		}
		else {
			if (parser.current() == JsonStaxParserLexType.START_OBJECT) {
				JsonStaxParserLexType	lexType;
				
				do {lexType = parser.next();
					if (lexType == JsonStaxParserLexType.NAME) {
						switch (parser.name()) {
							case "name" :
								if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER && parser.next() == JsonStaxParserLexType.STRING_VALUE) {
									name = parser.stringValue();
									lexType = parser.next();
								}
								else {
									throw new SyntaxException(parser.row(), parser.col(), "Structure corruption (integer awaited)");
								}
								break;
							default :
								throw new SyntaxException(parser.row(), parser.col(), "Unsupported name. Only 'name' is valid here");
						}
					}
					else {
						throw new SyntaxException(parser.row(), parser.col(), "field name is missing");
					}
				} while (lexType == JsonStaxParserLexType.LIST_SPLITTER);
				if (lexType == JsonStaxParserLexType.END_OBJECT) {
					parser.next();
				}
				else {
					throw new SyntaxException(parser.row(), parser.col(), "'}' is missing");
				}
			}
			else {
				throw new SyntaxException(parser.row(), parser.col(), "'{' is missing");
			}
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PrimitiveSubscribableValueSource other = (PrimitiveSubscribableValueSource) obj;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "PrimitiveSubscribableValueSource [name=" + name + ", getSourceType()=" + getSourceType() + "]";
	}
}
