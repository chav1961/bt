package chav1961.bt.mnemoed.entities;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class URIProp extends EntityProp {
	private ObjectValueSource	uri;
	
	public URIProp(final ObjectValueSource uri) throws NullPointerException {
		if (uri == null) {
			throw new NullPointerException("Uri to set can't be null"); 
		}
		else {
			this.uri = uri;
		}
	}

	public ObjectValueSource getUri() {
		return uri;
	}

	public void setUri(final ObjectValueSource uri) throws NullPointerException {
		if (uri == null) {
			throw new NullPointerException("Uri to set can't be null"); 
		}
		else {
			this.uri = uri;
		}
	}

	@Override
	public void upload(JsonStaxPrinter printer) throws PrintingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void download(JsonStaxParser parser) throws SyntaxException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString() {
		return "URIProp [uri=" + uri + "]";
	}
}
