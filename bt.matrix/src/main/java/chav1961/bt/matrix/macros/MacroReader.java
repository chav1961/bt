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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import chav1961.bt.matrix.macros.NestedReader.Line;
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
	public static final String		SETTING_INSERT_NL_AFTER_POP = "insertNLAfterPop";
	
	private static final AtomicInteger	UNIQUE = new AtomicInteger();
	
	private final Function<String, Object> 				parameters;
	private final BiFunction<String, Object, Object>	settings;
	private final NestedReader	nested;
	private final Line			line = new Line();

	public MacroReader(final Reader nested, final Function<String, Object> parameters, final BiFunction<String, Object, Object> settings) throws IOException, SyntaxException {
		if (nested == null) {
			throw new NullPointerException("Nested reader can't be null");
		}
		else if (parameters == null) {
			throw new NullPointerException("Parameters can't be null");
		}
		else if (settings == null) {
			throw new NullPointerException("Settings can't be null");
		}
		else {
			this.parameters = parameters;
			this.settings = settings;
			this.nested = new NestedReader(nested, getSetting(SETTING_INSERT_NL_AFTER_POP, boolean.class, false));
		}
	}	
	
	public MacroReader(final Reader nested, final Map<String, Object> parameters, final Map<String, Object> settings) throws IOException, SyntaxException {
		if (nested == null) {
			throw new NullPointerException("Nested reader can't be null");
		}
		else if (parameters == null) {
			throw new NullPointerException("Parameters can't be null");
		}
		else if (settings == null) {
			throw new NullPointerException("Settings can't be null");
		}
		else {
			this.parameters = new Function<String,Object>(){
									@Override
									public Object apply(String name) {
										return parameters.get(name);
									}
								};
			this.settings = new BiFunction<String, Object, Object>() {
								@Override
								public Object apply(final String name, final Object defaultValue) {
									if (settings.containsKey(name)) {
										return settings.get(name);
									}
									else {
										return defaultValue;
									}
								}
							};
			this.nested = new NestedReader(nested, getSetting(SETTING_INSERT_NL_AFTER_POP, boolean.class, false));
		}
	}

	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException {
		return 0;
	}

	@Override
	public void close() throws IOException {
	}

	private <T> T getSetting(final String name, final Class<T> awaited) {
		return getSetting(name, awaited, null);
	}
	
	private <T> T getSetting(final String name, final Class<T> awaited, final T defaultValue) {
		return awaited.cast(settings.apply(name, defaultValue));
	}
	
}
