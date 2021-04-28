package chav1961.bt.mnemoed.entities;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class ObjectExpressionValueSource extends ObjectValueSource {
	private final String	expression;
	
	public ObjectExpressionValueSource(final String expression) throws IllegalArgumentException {
		super(ValueSourceType.REF_EXPRESSION);
		if (expression == null || expression.isEmpty()) {
			throw new IllegalArgumentException("Expression to calculate can't be null or empty");
		}
		else {
			this.expression = expression;
		}
	}

	public String getExpression() {
		return expression;
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
		return "ObjectExpressionValueSource [expression=" + expression + ", getSourceType()=" + getSourceType() + "]";
	}
}
