package chav1961.bt.paint.script;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.BufferWrapper;
import chav1961.bt.paint.script.interfaces.ImageWrapper;
import chav1961.bt.paint.script.interfaces.ImageWrapper.SetOptions;
import chav1961.bt.paint.script.interfaces.RectWrapper;

public class BufferWrapperImpl implements BufferWrapper {

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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ImageWrapper getImage() throws PaintScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setImage(ImageWrapper image) throws PaintScriptException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ImageWrapper getImage(RectWrapper rect) throws PaintScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setImage(RectWrapper rect, ImageWrapper image, SetOptions... options) throws PaintScriptException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public RectWrapper getSelection() throws PaintScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSelection(RectWrapper rect) throws PaintScriptException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearSelection() throws PaintScriptException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasSelection() throws PaintScriptException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() throws PaintScriptException {
		// TODO Auto-generated method stub
		
	}

}
