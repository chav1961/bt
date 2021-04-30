package chav1961.bt.mnemoed.entities;

import java.awt.geom.AffineTransform;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public class LocationProp extends AffineEntityProp {
	private PrimitiveValueSource	xLocation;
	private PrimitiveValueSource	yLocation;

	public LocationProp(final PrimitiveValueSource xLocation, final PrimitiveValueSource yLocation) {
		if (xLocation == null) {
			throw new NullPointerException("X location can't be null");
		}
		else if (yLocation == null) {
			throw new NullPointerException("Y Location can't be null");
		}
		else {
			this.xLocation = xLocation;
			this.yLocation = yLocation;
		}
	}

	@Override
	public AffineTransform getAffineTransform() {
		return AffineTransform.getTranslateInstance(0, 0);
	}

	public PrimitiveValueSource getxLocation() {
		return xLocation;
	}

	public void setxLocation(final PrimitiveValueSource xLocation) throws NullPointerException {
		if (xLocation == null) {
			throw new NullPointerException("X location to set can't be null");
		}
		else {
			this.xLocation = xLocation;
		}
	}

	public PrimitiveValueSource getyLocation() {
		return yLocation;
	}

	public void setyLocation(final PrimitiveValueSource yLocation) throws NullPointerException {
		if (xLocation == null) {
			throw new NullPointerException("X location to set can't be null");
		}
		else {
			this.yLocation = yLocation;
		}
	}

	@Override
	public void upload(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Stax printer can't be null");
		}
		else {
			printer.startArray();
			printer.startObject().name(getArgType(xLocation.getClass()).name());
			xLocation.upload(printer);
			printer.endObject();
			printer.startObject().name(getArgType(yLocation.getClass()).name());
			yLocation.upload(printer);
			printer.endObject();
			printer.endArray();
		}
	}

	@Override
	public void download(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser.current() == JsonStaxParserLexType.START_ARRAY) {
			parser.next();
			setxLocation(parsePrimitiveValueSource(parser));
			if (parser.current() == JsonStaxParserLexType.LIST_SPLITTER) {
				parser.next();
				setyLocation(parsePrimitiveValueSource(parser));
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
		return "LocationProp [xLocation=" + xLocation + ", yLocation=" + yLocation + "]";
	}
}
