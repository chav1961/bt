package chav1961.bt.mnemoed.entities;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class ObjectConstantValueSource<T> extends ObjectValueSource {
	private T	value;
	
	public ObjectConstantValueSource(final T value) throws NullPointerException {
		super(ValueSourceType.REF_CONST);
		if (value == null) {
			throw new NullPointerException("Constant value can't be null"); 
		}
		else {
			this.value = value;
		}
	}
	
	public T getObjectValue() {
		return value;
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
		return "ObjectConstantValueSource [value=" + value + ", getSourceType()=" + getSourceType() + "]";
	}

}
