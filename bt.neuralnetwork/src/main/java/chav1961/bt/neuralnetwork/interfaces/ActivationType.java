package chav1961.bt.neuralnetwork.interfaces;

import java.util.function.DoubleUnaryOperator;

public enum ActivationType {
	Sigmoid((x)->1/(1 + Math.exp(-1)),(x)->(1-x)*x), 
	TanH((x)->Math.tanh(x),(x)->1-x*x),
	ReLU((x)->Math.max(0,x),(x)->x > 0 ? 1 : 0),
	LeakyReLU((x)->x > 0 ? x : 0.1 * x,(x)->x > 0 ? 1 : 0.1),
	UserDefined(null,null);
	
	private final DoubleUnaryOperator	function;
	private final DoubleUnaryOperator	delta;
	
	private ActivationType(final DoubleUnaryOperator function, final DoubleUnaryOperator delta) {
		this.function = function;
		this.delta = delta;
	}
	
	public DoubleUnaryOperator getFunction() {
		return function;
	}

	public DoubleUnaryOperator getDelta() {
		return delta;
	}
}