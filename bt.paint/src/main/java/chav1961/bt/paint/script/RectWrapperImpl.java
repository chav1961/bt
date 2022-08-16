package chav1961.bt.paint.script;

import java.awt.Rectangle;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.RectWrapper;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class RectWrapperImpl implements RectWrapper {
	private static final Object[]	LEXEMAS = {CharUtils.ArgumentType.signedInt, ',', CharUtils.ArgumentType.signedInt
												,new CharUtils.Choise(new Object[] {"size", new CharUtils.Mark(1), CharUtils.ArgumentType.signedInt, ',', CharUtils.ArgumentType.signedInt}, 
																	  new Object[] {new CharUtils.Optional("to"), CharUtils.ArgumentType.signedInt, ',', CharUtils.ArgumentType.signedInt})
												}; 
	
	private final Rectangle			rect = new Rectangle();
	
	public RectWrapperImpl(final Rectangle rect) {
		if (rect == null) {
			throw new NullPointerException("Rectangle to wrap can't be null"); 
		}
		else {
			this.rect.setBounds(rect);
		}
	}

	public RectWrapperImpl(final String rect) throws PaintScriptException {
		if (rect == null || rect.isEmpty()) {
			throw new IllegalArgumentException("Rectangle string can't be null or empty");
		}
		else {
			try{this.rect.setBounds(parseRect(rect));
			} catch (SyntaxException e) {
				throw new PaintScriptException(e);
			}
		}
	}
	
	@Override
	public Rectangle getRect() {
		return rect;
	}

	@Override
	public RectWrapper setRect(final String rect) throws PaintScriptException {
		if (rect == null || rect.isEmpty()) {
			throw new IllegalArgumentException("Rectangle string can't be null or empty");
		}
		else {
			try{setRect(parseRect(rect));
				return this;
			} catch (SyntaxException e) {
				throw new PaintScriptException(e);
			}
		}
	}

	@Override
	public RectWrapper setRect(final Rectangle rect) throws PaintScriptException {
		if (rect == null) {
			throw new NullPointerException("Rectangle to wrap can't be null"); 
		}
		else {
			this.rect.setBounds(rect);
			return this;
		}
	}
	
	private static Rectangle parseRect(final String rect) throws SyntaxException {
		final Object[]	p = new Object[4];
		final char[]	content = rect.toCharArray();
		final int		pos = CharUtils.tryExtract(content, 0, LEXEMAS);
		
		if (pos < 0) {
			throw new SyntaxException(0,-pos,"Rectangle format error ["+rect+"]"); 
		}
		else {
			CharUtils.extract(content, 0, p, LEXEMAS);
			
			if (p[2] instanceof CharUtils.Mark) {
				return new Rectangle((Integer)p[0], (Integer)p[1], (Integer)p[3], (Integer)p[4]);
			}
			else {
				return new Rectangle((Integer)p[0], (Integer)p[1], (Integer)p[2] - (Integer)p[0], (Integer)p[3] - (Integer)p[1]);
			}
		}
	}

	@Override
	public Class<Rectangle> getContentType() {
		return Rectangle.class;
	}

	@Override
	public Rectangle getContent() throws PaintScriptException {
		return rect;
	}

	@Override
	public void setContent(final Rectangle content) throws PaintScriptException {
		setRect(content);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new RectWrapperImpl(this.rect);
	}
}
