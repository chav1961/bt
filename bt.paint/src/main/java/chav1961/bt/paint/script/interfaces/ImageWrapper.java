package chav1961.bt.paint.script.interfaces;

import java.awt.image.BufferedImage;

import chav1961.bt.paint.control.ImageUtils.DrawingType;
import chav1961.bt.paint.control.ImageUtils.ProcessType;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.ImageWrapperImpl;

public interface ImageWrapper extends ContentWrapper<BufferedImage>{
	public static enum SetOptions {
		IDENTITY
	}
	
	BufferedImage getImage() throws PaintScriptException;
	ImageWrapper getImage(RectWrapper wrapper) throws PaintScriptException;
	ImageWrapper setImage(BufferedImage image) throws PaintScriptException;
	ImageWrapper setImage(RectWrapper wrapper, ImageWrapper image, ImageWrapper.SetOptions... options) throws PaintScriptException;
	String getFormat() throws PaintScriptException;
	ImageWrapper setFormat(String format) throws PaintScriptException;
	String getName();
	ImageWrapper setName(String name);
	int getType() throws PaintScriptException;
	ImageWrapper setType(int type) throws PaintScriptException;
	ImageWrapper cloneThis() throws PaintScriptException;
	ImageWrapper clear(ColorWrapper color) throws PaintScriptException;
	ImageWrapper convert(ProcessType op, Object... parameters) throws PaintScriptException;
	ImageWrapper draw(DrawingType op, Object... parameters) throws PaintScriptException;
	ImageWrapper fill(DrawingType op, Object... parameters) throws PaintScriptException;
	
	static ImageWrapper of(BufferedImage image) {
		return new ImageWrapperImpl(image);
	}
}