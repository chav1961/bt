package chav1961.bt.neuralnetwork.interfaces;

import chav1961.bt.neuralnetwork.layers.PoolingLayer;
import chav1961.bt.neuralnetwork.layers.PoolingLayer.UserDefinedPoolingFunction;

public enum PoolingType {
	SUM_POOLING(new UserDefinedPoolingFunction() {
				float	sum;
		
				@Override
				public void beforeCall() {
					sum = 0;
				}
	
				@Override
				public void call(final int x, final int y, final float value) {
					sum += value;
				}
	
				@Override
				public float afterCall() {
					return sum;
				}
			}),
	MAX_POOLING(new UserDefinedPoolingFunction() {
				float 	max;
				
				@Override
				public void beforeCall() {
					max = -Float.MAX_VALUE;
				}
	
				@Override
				public void call(final int x, final int y, final float value) {
					max = Math.max(max, value);
				}
	
				@Override
				public float afterCall() {
					return Math.max(max, 0);
				}
			}),
	AVERAGE_POOLING(new UserDefinedPoolingFunction() {
				float	sum;
				int		count;
				
				@Override
				public void beforeCall() {
					sum = 0;
					count = 0;
				}
	
				@Override
				public void call(final int x, final int y, final float value) {
					sum += value;
					count++;
				}
	
				@Override
				public float afterCall() {
					return count == 0 ? 0 : sum/ count;
				}
			}),
	USER_DEFINED_POOLING(null);
	
	private final UserDefinedPoolingFunction	udpf;
	
	private PoolingType(final UserDefinedPoolingFunction udpf) {
		this.udpf = udpf;
	}
	
	public UserDefinedPoolingFunction getPoolingFunction() {
		return udpf;
	}
}