package chav1961.bt.paint.script.interfaces;

import chav1961.bt.paint.control.Predefines;
import chav1961.bt.paint.interfaces.PaintScriptException;

public interface SystemWrapper extends ImmutablePropertiesWrapper, ConsoleInterface {
	boolean exists(String file) throws PaintScriptException;
	boolean isFile(String file) throws PaintScriptException;
	boolean isDirectory(String file) throws PaintScriptException;
	boolean mkdir(String file) throws PaintScriptException;
	boolean rm(String file) throws PaintScriptException;
	boolean ren(String file, String newFile) throws PaintScriptException;
	String[] list(String fileMask) throws PaintScriptException;
	ImageWrapper loadImage(String file) throws PaintScriptException;
	void storeImage(ImageWrapper image, String file) throws PaintScriptException;
	PropertiesWrapper loadProps(String file) throws PaintScriptException;
	void storeProps(PropertiesWrapper props, final String file) throws PaintScriptException;
	void print(String message) throws PaintScriptException;
}