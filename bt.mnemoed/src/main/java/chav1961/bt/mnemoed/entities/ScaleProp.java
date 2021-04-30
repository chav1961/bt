package chav1961.bt.mnemoed.entities;

import java.awt.geom.AffineTransform;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public class ScaleProp extends AffineEntityProp {
	private PrimitiveValueSource	xScale;
	private PrimitiveValueSource	yScale;
	
	public ScaleProp(final PrimitiveValueSource xScale, final PrimitiveValueSource yScale) throws NullPointerException {
		if (xScale == null) {
			throw new NullPointerException("X scale can't be null");
		}
		else if (yScale == null) {
			throw new NullPointerException("Y scale can't be null");
		}
		else {
			this.xScale = xScale;
			this.yScale = yScale;
		}
	}

	@Override
	public AffineTransform getAffineTransform() {
		return AffineTransform.getScaleInstance(0, 0);
	}
	
	public PrimitiveValueSource getXScale() {
		return xScale;
	}

	public void setXScale(final PrimitiveValueSource xScale) throws NullPointerException {
		if (xScale == null) {
			throw new NullPointerException("X scale to set can't be null");
		}
		else {
			this.xScale = xScale;
		}
	}

	public PrimitiveValueSource getYScale() {
		return yScale;
	}

	public void setYScale(final PrimitiveValueSource yScale) throws NullPointerException {
		if (yScale == null) {
			throw new NullPointerException("Y scale to set can't be null");
		}
		else {
			this.yScale = yScale;
		}
	}

	@Override
	public void upload(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Stax printer can't be null");
		}
		else {
			printer.startArray();
			printer.startObject().name(getArgType(xScale.getClass()).name());
			xScale.upload(printer);
			printer.endObject();
			printer.startObject().name(getArgType(yScale.getClass()).name());
			yScale.upload(printer);
			printer.endObject();
			printer.endArray();
		}
	}

	@Override
	public void download(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser.current() == JsonStaxParserLexType.START_ARRAY) {
			parser.next();
			setXScale(parsePrimitiveValueSource(parser));
			if (parser.current() == JsonStaxParserLexType.LIST_SPLITTER) {
				parser.next();
				setYScale(parsePrimitiveValueSource(parser));
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
		return "ScaleProp [xScale=" + xScale + ", yScale=" + yScale + "]";
	}
}
