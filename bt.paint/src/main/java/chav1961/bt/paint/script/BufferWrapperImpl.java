package chav1961.bt.paint.script;

import java.awt.image.BufferedImage;

import chav1961.bt.paint.control.ImageEditPanel;
import chav1961.bt.paint.control.ImageUtils;
import chav1961.bt.paint.control.ImageUtils.ProcessType;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.BufferWrapper;
import chav1961.bt.paint.script.interfaces.ImageWrapper;
import chav1961.bt.paint.script.interfaces.ImageWrapper.SetOptions;
import chav1961.bt.paint.script.interfaces.RectWrapper;

public class BufferWrapperImpl implements BufferWrapper {
	private BufferedImage	image = null;
	private RectWrapper		selection = null;
	
	public BufferWrapperImpl() {
	}

	@Override
	public void open() throws PaintScriptException {
		// TODO Auto-generated method stub
	}

	@Override
	public void clear() throws PaintScriptException {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean hasImage() throws PaintScriptException {
		return image != null;
	}

	@Override
	public ImageWrapper getImage() throws PaintScriptException {
		return ImageWrapper.of(image);
	}

	@Override
	public void setImage(final ImageWrapper image) throws PaintScriptException {
		if (image == null) {
			throw new NullPointerException("Image to set can't be null");
		}
		else {
			this.image = (BufferedImage) image.getImage();
		}
	}

	@Override
	public ImageWrapper getImage(final RectWrapper rect) throws PaintScriptException {
		if (rect == null) {
			throw new NullPointerException("Rect to get image can't be null");
		}
		else if (hasImage()) {
			return ImageWrapper.of(ImageUtils.process(ProcessType.CROP, image, null, rect));
		}
		else {
			throw new IllegalStateException("Buffer has no image to call this method");
		}
	}

	@Override
	public void setImage(RectWrapper rect, ImageWrapper image, SetOptions... options) throws PaintScriptException {
		if (rect == null) {
			throw new NullPointerException("Rect to insert image can't be null");
		}
		else if (hasImage()) {
			ImageUtils.process(ProcessType.INSERT, image.getImage(), null, rect);
		}
		else {
			throw new IllegalStateException("Buffer has no image to call this method");
		}
	}

	@Override
	public RectWrapper getSelection() throws PaintScriptException {
		return selection;
	}

	@Override
	public void setSelection(final RectWrapper rect) throws PaintScriptException {
		if (rect == null) {
			throw new NullPointerException("Rect to set can;t be null");
		}
		else {
			selection = RectWrapper.of(rect.getRect());
		}
	}

	@Override
	public void clearSelection() throws PaintScriptException {
		selection = null;
	}

	@Override
	public boolean hasSelection() throws PaintScriptException {
		return selection != null;
	}

	@Override
	public void close() throws PaintScriptException {
		// TODO Auto-generated method stub
	}
}
