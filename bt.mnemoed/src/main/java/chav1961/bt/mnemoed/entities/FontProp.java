package chav1961.bt.mnemoed.entities;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class FontProp extends EntityProp {
	private ObjectValueSource		family;
	private PrimitiveValueSource	size;
	private PrimitiveValueSource	style;
	
	public FontProp(final ObjectValueSource family, final PrimitiveValueSource size, final PrimitiveValueSource style) throws NullPointerException {
		if (family == null) {
			throw new NullPointerException("Font family to set can't be null"); 
		}
		else if (size == null) {
			throw new NullPointerException("Font size to set can't be null"); 
		}
		else if (style == null) {
			throw new NullPointerException("Font style to set can't be null"); 
		}
		else {
			this.family = family;
			this.size = size;
			this.style = style;
		}
	}

	public ObjectValueSource getFamily() {
		return family;
	}

	public void setFamily(final ObjectValueSource family) throws NullPointerException {
		if (family == null) {
			throw new NullPointerException("Font family to set can't be null"); 
		}
		else {
			this.family = family;
		}
	}

	public PrimitiveValueSource getSize() {
		return size;
	}

	public void setSize(final PrimitiveValueSource size) throws NullPointerException {
		if (size == null) {
			throw new NullPointerException("Font size to set can't be null"); 
		}
		else {
			this.size = size;
		}
	}

	public PrimitiveValueSource getStyle() {
		return style;
	}

	public void setStyle(final PrimitiveValueSource style) throws NullPointerException {
		if (style == null) {
			throw new NullPointerException("Font style to set can't be null"); 
		}
		else {
			this.style = style;
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
		return "FontProp [family=" + family + ", size=" + size + ", style=" + style + "]";
	}
}
