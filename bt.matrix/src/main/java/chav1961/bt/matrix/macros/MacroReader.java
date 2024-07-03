package chav1961.bt.matrix.macros;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.purelib.basic.exceptions.SyntaxException;

/*
		.version	1.0
		.param	name=char,default="sss",comment=""
		.include <uri>
		.insert  "имя",<параметры>
		qubit	{1,2,3,4,5}
		cbit	{1,2,3}
1:		qreg	{1:1,2:2,3:3}
1:		creg	{1:1,2:3,3:5}
1:		gate	"standard.RX",fi=120,q=1.1
2:		gate	"standard.RX",fi=10,q=1.1
		gate	"standard.RX",fi=10,q=1.2
		gate	"standard.RX",fi=10,q=1.3
3:		gate	"standard.T",q=2
		gate	"standard.CNot",q=1.4,ctrl={1.2,1.3,1.4}
4:		gate	"standard.M",q=1.1,c=1.1
		gate	"standard.M",q=1.2,c=1.2
		gate	"standard.M",q=1.3,c=1.3
		gate	"standard.M",q=1.4,c=1.4
		.var	name=qreg[3]
		.var	name=creg[3]
		.var	name=char[]
		.var	name=bool[]
		.var	name=real[]
		.var	name=complex[]
		.var	name=bool[]
		.set	name=<выражение>
		.if		<выражение>
		.elsif	<выражение>
		.endif
		.while	<выражение>
		.endwhile
		.for	<выражение>..<выражение> [reverse]
		.endfor
		.case	<выражение>
		.of		<выражение>
		.default
		.endcase
		.break
		.continue
		.error	<выражение>
		.warning	<выражение>
		.print	<выражение>
&i:		gate	"&name",ctrl=&list,q=&i.1 
 */

public class MacroReader extends Reader {
	private static final char[][]	MACRO_OPERATORS = {
										".version".toCharArray(),
										".param".toCharArray(),
										".var".toCharArray(),
										".set".toCharArray(),
										".if".toCharArray(),
										".elsif".toCharArray(),
										".endif".toCharArray(),
										".while".toCharArray(),
										".endwhile".toCharArray(),
										".for".toCharArray(),
										".endfor".toCharArray(),
										".case".toCharArray(),
										".of".toCharArray(),
										".default".toCharArray(),
										".endcase".toCharArray(),
										".break".toCharArray(),
										".continue".toCharArray(),
										".error".toCharArray(),
										".warning".toCharArray(),
										".print".toCharArray(),
										".insert".toCharArray(),
										".include".toCharArray()
									};
	private static final AtomicInteger	UNIQUE = new AtomicInteger();
	
	private final NestedReader			nested;
	
	public MacroReader(final Reader nested, final Properties parameters) throws IOException, SyntaxException {
		if (nested == null) {
			throw new NullPointerException("Nested reader can't be null");
		}
		else {
			this.nested = new NestedReader(nested);
		}
	}

	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException {
		return 0;
	}

	@Override
	public void close() throws IOException {
	}

	
}
