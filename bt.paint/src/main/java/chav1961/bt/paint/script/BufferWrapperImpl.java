package chav1961.bt.paint.script;

import chav1961.bt.paint.script.interfaces.BufferWrapper;
import chav1961.bt.paint.script.interfaces.ImageWrapper;
import chav1961.bt.paint.script.interfaces.ImageWrapper.SetOptions;
import chav1961.bt.paint.script.interfaces.RectWrapper;
import chav1961.bt.paint.script.interfaces.ScriptException;

public class BufferWrapperImpl implements BufferWrapper {

	@Override
	public void open() throws ScriptException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() throws ScriptException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasImage() throws ScriptException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ImageWrapper getImage() throws ScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setImage(ImageWrapper image) throws ScriptException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ImageWrapper getImage(RectWrapper rect) throws ScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setImage(RectWrapper rect, ImageWrapper image, SetOptions... options) throws ScriptException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public RectWrapper getSelection() throws ScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSelection(RectWrapper rect) throws ScriptException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearSelection() throws ScriptException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasSelection() throws ScriptException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() throws ScriptException {
		// TODO Auto-generated method stub
		
	}

}
