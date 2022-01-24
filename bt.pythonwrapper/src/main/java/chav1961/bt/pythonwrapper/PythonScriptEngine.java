package chav1961.bt.pythonwrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;

import javax.script.ScriptEngineFactory;

import org.python.util.PythonInterpreter;

import chav1961.purelib.basic.AbstractScriptEngine;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;

public class PythonScriptEngine extends AbstractScriptEngine {
	private final GrowableCharArray 	gca = new GrowableCharArray(false);

	PythonScriptEngine(final ScriptEngineFactory factory) {
		super(factory);
	}

	@Override
	protected void processLineInternal(final long displacement, final int lineNo, final char[] data, final int from, final int length) throws IOException, SyntaxException {
		gca.append(data, from, from + length);
		gca.append('\n');
	}

	@Override
	protected void afterCompile(final Reader reader, final OutputStream os) throws IOException {
	    try(final PythonInterpreter 	pyInterp = new PythonInterpreter()) {
	    	
	    	pyInterp.exec(new String(gca.extract()));
	    } finally {
	    	gca.length(0);
	    }
	}

}
