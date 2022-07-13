package chav1961.bt.paint.script.interfaces;

public interface SystemWrapper extends ImmutablePropertiesWrapper {
	ImageWrapper loadImage(String file) throws ScriptException;
	void storeImage(ImageWrapper image, String file) throws ScriptException;
	PropertiesWrapper loadProps(String file) throws ScriptException;
	void storeProps(PropertiesWrapper props, final String file) throws ScriptException;
	void print(String message) throws ScriptException;
}