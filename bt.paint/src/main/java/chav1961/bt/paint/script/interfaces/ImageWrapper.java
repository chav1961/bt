package chav1961.bt.paint.script.interfaces;

import java.awt.Image;

import chav1961.bt.paint.control.ImageUtils.ProcessType;
import chav1961.bt.paint.interfaces.PaintScriptException;

public interface ImageWrapper {
	public static enum DrawTypes {
		IDENTITY
	}
	
	public static enum SetOptions {
		IDENTITY
	}
	
	Image getImage() throws PaintScriptException;
	ImageWrapper getImage(RectWrapper wrapper) throws PaintScriptException;
	ImageWrapper setImage(RectWrapper wrapper, ImageWrapper image, ImageWrapper.SetOptions... options) throws PaintScriptException;
	String getFormat() throws PaintScriptException;
	ImageWrapper setFormat(String format) throws PaintScriptException;
	ImageWrapper cloneThis() throws PaintScriptException;
	ImageWrapper clear(ColorWrapper color) throws PaintScriptException;
	ImageWrapper convert(ProcessType op, Object... parameters) throws PaintScriptException;
	ImageWrapper draw(DrawTypes op, Object... parameters) throws PaintScriptException;
	ImageWrapper fill(DrawTypes op, Object... parameters) throws PaintScriptException;
	
	static ImageWrapper of(final Image image) throws PaintScriptException {
		return of(image,"png");
	}

	static ImageWrapper of(final Image image, final String format) throws PaintScriptException {
		return null;
	}
}