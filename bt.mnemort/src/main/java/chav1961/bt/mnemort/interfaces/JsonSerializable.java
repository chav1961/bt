package chav1961.bt.mnemort.interfaces;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public interface JsonSerializable {
	void fromJson(JsonStaxParser parser) throws SyntaxException;
	void toJson(JsonStaxPrinter printer) throws PrintingException;
}
