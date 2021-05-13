package chav1961.bt.mnemoed.interfaces;

import java.io.IOException;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public interface JsonSerializable {
	void upload(JsonStaxPrinter printer) throws IOException, PrintingException;
	void download(JsonStaxParser parser) throws IOException, SyntaxException;
}
