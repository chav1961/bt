package chav1961.bt.mnemoed.entities;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class PrimitiveSubscribableValueSource extends PrimitiveValueSource {
	private final String	name;
	
	public PrimitiveSubscribableValueSource(final String name) throws IllegalArgumentException {
		super(ValueSourceType.PRIMITIVE_SUBSCRIBABLE);
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name can't be null or empty"); 
		}
		else {
			this.name = name;
		}
	}
	
	public String getName() {
		return name;
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
		return "PrimitiveSubscribableValueSource [name=" + name + ", getSourceType()=" + getSourceType() + "]";
	}
}
