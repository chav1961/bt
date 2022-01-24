package chav1961.bt.pythonwrapper;

import java.util.Arrays;
import javax.script.ScriptEngine;

import chav1961.purelib.basic.AbstractScriptEngineFactory;
import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.MimeParseException;

public class PythonEngineFactory extends AbstractScriptEngineFactory {
	public static final String		ENGINE_FULL_NAME = "Jithon (Python interpreter) wrapper";
	public static final String		ENGINE_VERSION = PureLibSettings.CURRENT_VERSION;
	public static final String		LANG_NAME = "Python interpreter";
	public static final String		LANG_VERSION = PureLibSettings.CURRENT_VERSION;
	
	public PythonEngineFactory() throws MimeParseException {
		super(ENGINE_FULL_NAME,ENGINE_VERSION,Arrays.asList(new MimeType("text","plain")),LANG_NAME,LANG_VERSION,Arrays.asList("py"));
	}

	@Override
	public String getMethodCallSyntax(final String obj, final String m, final String... args) {
		final StringBuilder	sb = new StringBuilder(obj+"."+m);
		
		if (args.length == 0) {
			return sb.toString();
		}
		else {
			char prefix = '(';
			
			for (String item : args) {
				sb.append(prefix).append(item);
				prefix = ',';
			}
			return sb.append(')').toString();
		}
	}

	@Override
	public String getOutputStatement(final String toDisplay) {
		return "print("+toDisplay+")";
	}

	@Override
	public String getProgram(final String... statements) {
		final StringBuilder	sb = new StringBuilder();
		
		for (String item : statements) {
			sb.append(item).append('\n');
		}
		return sb.toString();
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return null;//
	}
}
