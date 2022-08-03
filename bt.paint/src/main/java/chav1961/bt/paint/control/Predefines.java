package chav1961.bt.paint.control;

import chav1961.bt.paint.script.interfaces.CanvasWrapper;
import chav1961.bt.paint.script.interfaces.ClipboardWrapper;
import chav1961.bt.paint.script.interfaces.SystemWrapper;

public class Predefines implements AutoCloseable {
	public static final String		PREDEF_SYSTEM = "system";
	public static final String		PREDEF_CLIPBOARD = "clipboard";
	public static final String		PREDEF_CANVAS = "canvas";
	public static final String		PREDEF_ARG = "arg";
	public static final String		PREDEF_LIB = "lib";
	private static final String[]	NAMES = {PREDEF_SYSTEM, PREDEF_CLIPBOARD, PREDEF_CANVAS, PREDEF_ARG, PREDEF_LIB};
	
	private final String[]			args;
	private final ClipboardWrapper	cbw = ClipboardWrapper.singleton;
	private SystemWrapper			sw;
	private CanvasWrapper			cw;

	public Predefines(final String[] args) {
		this.args = args;
	}

	@Override
	public void close() throws RuntimeException {
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

	public <T> void putPredefined(final String predefinedName, final T instance) {
		if (predefinedName == null || predefinedName.isEmpty()) {
			throw new IllegalArgumentException("Predefined name can't be null or empty");
		}
		else if (instance == null) {
			throw new NullPointerException("Instance can't be null");
		}
		else {
			switch (predefinedName) {
				case PREDEF_SYSTEM 		:
					if (instance instanceof SystemWrapper) {
						this.sw = (SystemWrapper)instance;
					}
					else {
						throw new IllegalArgumentException("Instance for ["+PREDEF_SYSTEM+"] must implements "+SystemWrapper.class.getCanonicalName()); 
					}
					break;
				case PREDEF_CLIPBOARD	: 
					throw new UnsupportedOperationException("Assignment for ["+PREDEF_CLIPBOARD+"] is not suported");
				case PREDEF_CANVAS 		: 
					if (instance instanceof CanvasWrapper) {
						this.cw = (CanvasWrapper)instance;
					}
					else {
						throw new IllegalArgumentException("Instance for ["+PREDEF_CANVAS+"] must implements "+CanvasWrapper.class.getCanonicalName()); 
					}
					break;
				case PREDEF_ARG			: 
					throw new UnsupportedOperationException("Assignment for ["+PREDEF_ARG+"] is not suported");
				default :
					throw new IllegalArgumentException("Predefined name ["+predefinedName+"] not found");
			}
		}
	}
}
