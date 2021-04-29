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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ObjectConstantValueSource other = (ObjectConstantValueSource) obj;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "ObjectConstantValueSource [value=" + value + ", getSourceType()=" + getSourceType() + "]";
	}

}
