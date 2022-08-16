package chav1961.bt.paint.script;

import java.awt.Dimension;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.SizeWrapper;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class SizeWrapperImpl implements SizeWrapper {
	private static final Object[]	LEXEMAS = {CharUtils.ArgumentType.signedInt, ',', CharUtils.ArgumentType.signedInt}; 
	
	private final Dimension	size = new Dimension();
	
	public SizeWrapperImpl(final Dimension size) {
		if (size == null) {
			throw new NullPointerException("Size to wrap can't be null"); 
		}
		else {
			this.size.setSize(size);
		}
	}
	
	public SizeWrapperImpl(final String size) throws PaintScriptException {
		if (size == null || size.isEmpty()) {
			throw new IllegalArgumentException("Size string can't be null or empty");
		}
		else {
			try{this.size.setSize(parseSize(size));
			} catch (SyntaxException e) {
				throw new PaintScriptException(e);
			}
		}
	}
	
	@Override
	public Class<Dimension> getContentType() {
		return Dimension.class;
	}

	@Override
	public Dimension getContent() throws PaintScriptException {
		return getSize();
	}

	@Override
	public void setContent(Dimension content) throws PaintScriptException {
		setSize(content);
	}

	@Override
	public Dimension getSize() {
		return size;
	}

	@Override
	public SizeWrapper setSize(final String size) throws PaintScriptException {
		if (size == null || size.isEmpty()) {
			throw new NullPointerException("Size to set can't be null or empty"); 
		}
		else {
			try{this.size.setSize(parseSize(size));
				return this;
			} catch (SyntaxException e) {
				throw new PaintScriptException(e);
			}
		}
	}

	@Override
	public SizeWrapper setSize(final Dimension size) throws PaintScriptException {
		if (size == null) {
			throw new NullPointerException("Size to set can't be null"); 
		}
		else {
			this.size.setSize(size);
			return this;
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new SizeWrapperImpl(size);
	}

	private static Dimension parseSize(final String size) throws SyntaxException {
		final Object[]	p = new Object[4];
		final char[]	content = size.toCharArray();
		final int		pos = CharUtils.tryExtract(content, 0, LEXEMAS);
		
		if (pos < 0) {
			throw new SyntaxException(0,-pos,"Size format error ["+size+"]"); 
		}
		else {
			CharUtils.extract(content, 0, p, LEXEMAS);
			
			return new Dimension((Integer)p[0], (Integer)p[1]);
		}
	}
}
