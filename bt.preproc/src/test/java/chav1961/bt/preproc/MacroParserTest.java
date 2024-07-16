package chav1961.bt.preproc;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.preproc.MacroParser.OperatorType;
import chav1961.bt.preproc.NestedReader.Line;
import chav1961.bt.preproc.runtime.CommandList;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class MacroParserTest {

	@Test
	public void basicTest() throws IOException, SyntaxException {
		try(final NestedReader	rdr = new NestedReader(new StringReader(""))) {
			final MacroParser	mp = new MacroParser(rdr, "#".toCharArray(), "&".toCharArray(), "".toCharArray(), false);
			final CommandList	cl = new CommandList();

			mp.parse(Line.of("#break\n"), OperatorType.BREAK, cl);
		}
	}
}
