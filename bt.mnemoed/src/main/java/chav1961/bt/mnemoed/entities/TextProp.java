package chav1961.bt.mnemoed.entities;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class TextProp extends EntityProp {
	private ObjectValueSource	text;
	private ObjectValueSource	align;

	public TextProp(final ObjectValueSource text, final ObjectValueSource align) throws NullPointerException {
		if (text == null) {
			throw new NullPointerException("Text to set can't be null");
		}
		else if (align == null) {
			throw new NullPointerException("Text alignment to set can't be null");
		}
		else {
			this.text = text;
			this.align = align;
		}
	}

	public ObjectValueSource getText() {
		return text;
	}

	public void setText(final ObjectValueSource text) throws NullPointerException {
		if (text == null) {
			throw new NullPointerException("Text to set can't be null");
		}
		else {
			this.text = text;
		}
	}

	public ObjectValueSource getAlign() {
		return align;
	}

	public void setAlign(ObjectValueSource align) throws NullPointerException {
		if (align == null) {
			throw new NullPointerException("Text alignment to set can't be null");
		}
		else {
			this.align = align;
		}
	}

	@Override
	public void upload(final JsonStaxPrinter printer) throws PrintingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void download(final JsonStaxParser parser) throws SyntaxException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String toString() {
		return "TextProp [text=" + text + ", align=" + align + "]";
	}
}
