package chav1961.bt.mnemoed.entities;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class PrimitiveExpressionValueSource extends PrimitiveValueSource {
	private final String	expression;
	
	public PrimitiveExpressionValueSource(final String expression) throws IllegalArgumentException {
		super(ValueSourceType.PRIMITIVE_EXPRESSION);
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PrimitiveExpressionValueSource other = (PrimitiveExpressionValueSource) obj;
		if (expression == null) {
			if (other.expression != null) return false;
		} else if (!expression.equals(other.expression)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "PrimitiveExpressionValueSource [expression=" + expression + ", getSourceType()=" + getSourceType() + "]";
	}
}
