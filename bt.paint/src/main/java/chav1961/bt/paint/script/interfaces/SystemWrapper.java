package chav1961.bt.paint.script.interfaces;

import chav1961.bt.paint.interfaces.PaintScriptException;

public interface SystemWrapper extends ImmutablePropertiesWrapper {
	ImageWrapper loadImage(String file) throws PaintScriptException;
	void storeImage(ImageWrapper image, String file) throws PaintScriptException;
	PropertiesWrapper loadProps(String file) throws PaintScriptException;
	void storeProps(PropertiesWrapper props, final String file) throws PaintScriptException;
	void print(String message) throws PaintScriptException;
}