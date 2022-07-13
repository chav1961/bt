package chav1961.bt.paint.script.interfaces;

import java.awt.Image;

public interface ImageWrapper {
	public static enum ConversionTypes {
		CROP, ROTATE, MIRROR_H, MIRROR_V, RESIZE, SCALE, GRAY, TRANSPARENCY
	}

	public static enum DrawTypes {
		IDENTITY
	}
	
	public static enum SetOptions {
		IDENTITY
	}
	
	Image getImage() throws ScriptException;
	ImageWrapper getImage(RectWrapper wrapper) throws ScriptException;
	ImageWrapper setImage(RectWrapper wrapper, ImageWrapper image, ImageWrapper.SetOptions... options) throws ScriptException;
	String getFormat() throws ScriptException;
	ImageWrapper setFormat(String format) throws ScriptException;
	ImageWrapper cloneThis() throws ScriptException;
	ImageWrapper clear(ColorWrapper color) throws ScriptException;
	ImageWrapper convert(ConversionTypes op, Object... parameters) throws ScriptException;
	ImageWrapper draw(DrawTypes op, Object... parameters) throws ScriptException;
	ImageWrapper fill(DrawTypes op, Object... parameters) throws ScriptException;
	
	static ImageWrapper of(final Image image) throws ScriptException {
		return of(image,"png");
	}

	static ImageWrapper of(final Image image, final String format) throws ScriptException {
		return null;
	}
}