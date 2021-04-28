package chav1961.bt.mnemoed.interfaces;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public interface JsonSerialzable {
	void upload(JsonStaxPrinter printer) throws PrintingException;
	void download(JsonStaxParser parser) throws SyntaxException;
}
