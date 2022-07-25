package chav1961.bt.paint.script;

import java.net.URI;

import chav1961.bt.paint.script.interfaces.CanvasWrapper;
import chav1961.bt.paint.script.interfaces.ClipboardWrapper;
import chav1961.bt.paint.script.interfaces.SystemWrapper;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class Predefines implements AutoCloseable {
	public static final String		PREDEF_SYSTEM = "system";
	public static final String		PREDEF_CLIPBOARD = "clipboard";
	public static final String		PREDEF_CANVAS = "canvas";
	public static final String		PREDEF_ARG = "arg";
	private static final String[]	NAMES = {PREDEF_SYSTEM, PREDEF_CLIPBOARD, PREDEF_CANVAS, PREDEF_ARG};
	
	private final SystemWrapper		sw;
	private final ClipboardWrapper	cbw;
	private final CanvasWrapper		cw;
	private final String[]			args;
	
	public Predefines(final String[] args, final LoggerFacade logger, final FileSystemInterface fs, final URI homeDir) {
		this.sw = new SystemWrapperImpl(logger, fs, homeDir);
		this.cbw = ClipboardWrapper.singleton;
		this.cw = new CanvasWrapperImpl();
		this.args = args;
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public String[] getPredefinedNames() {
		return NAMES;
	}
	
	public <T> T getPredefined(final long predefinedId, final Class<T> awaited) {
		return null;
	}

	public <T> T getPredefined(final String predefinedName, final Class<T> awaited) {
		if (predefinedName == null || predefinedName.isEmpty()) {
			throw new IllegalArgumentException("Predefined name can't be null or empty");
		}
		else if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null");
		}
		else {
			switch (predefinedName) {
				case PREDEF_SYSTEM 		: return awaited.cast(sw); 
				case PREDEF_CLIPBOARD	: return awaited.cast(cbw);
				case PREDEF_CANVAS 		: return awaited.cast(cw);
				case PREDEF_ARG			: return awaited.cast(args);
				default :
					throw new IllegalArgumentException("Predefined name ["+predefinedName+"] not found");
			}
		}
	}

}
