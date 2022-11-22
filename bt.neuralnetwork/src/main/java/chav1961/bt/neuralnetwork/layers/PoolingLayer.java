package chav1961.bt.neuralnetwork.layers;

import chav1961.bt.neuralnetwork.interfaces.MatrixNeuralNetworkLayer;

public class PoolingLayer implements MatrixNeuralNetworkLayer {
	private final String		name;
	private final int			width;
	private final int			height;
	private final PoolingType	pooling;
	private final UserDefinedPoolingFunction	call;

	public static interface UserDefinedPoolingFunction {
		void beforeCall();
		void call(int x, int y, float value);
		float afterCall();
	}
	
	public static enum PoolingType {
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

	public PoolingLayer(final String layerName, final int width, final int height, final PoolingType pooling) {
		this(layerName, width, height, pooling, null);
	}	

	public PoolingLayer(final String layerName, final int width, final int height, final UserDefinedPoolingFunction call) {
		this(layerName, width, height, PoolingType.USER_DEFINED_POOLING, call);
	}
	
	protected PoolingLayer(final String layerName, final int width, final int height, final PoolingType pooling, final UserDefinedPoolingFunction call) {
		if (layerName == null || layerName.isEmpty()) {
			throw new IllegalArgumentException("Layer name can't be null"); 
		}
		else if (width <= 1) {
			throw new IllegalArgumentException("Layer filter width ["+width+"] must be greater than 1"); 
		}
		else if (height <= 1) {
			throw new IllegalArgumentException("Layer filter height ["+height+"] must be greater than 1"); 
		}
		else if (pooling == null) {
			throw new NullPointerException("Pooling type can't be null"); 
		}
		else if (pooling == PoolingType.USER_DEFINED_POOLING && call == null) {
			throw new IllegalArgumentException("Pooling type ["+pooling+"] requires user-defined pooling function parameter"); 
		}
		else if (pooling != PoolingType.USER_DEFINED_POOLING && call != null) {
			throw new IllegalArgumentException("Pooling type ["+pooling+"] doesn't support user-defined pooling function"); 
		}
		else {
			this.name = layerName;
			this.width = width;
			this.height = height;
			this.pooling = pooling;
			this.call = call != null ? call : pooling.getPoolingFunction();
		}
	}
	
	
	@Override
	public String getLayerName() {
		return name;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getTargetWidth(final int sourceWidth) {
		return sourceWidth / getWidth();
	}

	@Override
	public int getTargetHeight(final int sourceHeight) {
		return sourceHeight / getHeight();
	}

	@Override
	public float[] process(final int width, final int height, final float[] source) {
		if (width <= 1) {
			throw new IllegalArgumentException("Source matrix width ["+width+"] must be greater than 1"); 
		}
		else if (height <= 1) {
			throw new IllegalArgumentException("Source matrix height ["+height+"] must be greater than 1"); 
		}
		else if (source == null) {
			throw new NullPointerException("Matrix array can't be null"); 
		}
		else if (source.length != width * height) {
			throw new IllegalArgumentException("Matrix size ["+source.length+"] is differ than width*height = ["+(width*height)+"]"); 
		}
		else {
			final int		poolingWidth = getWidth(), poolingHeight = getHeight();
			final float[]	result = new float[getTargetWidth(width) * getTargetHeight(height)];
			int				target = 0;
			
			for(int y = 0; y < height - poolingHeight; y += poolingHeight) {
				for(int x = 0; x < width - poolingWidth; x += poolingWidth) {
					
					call.beforeCall();
					for(int deltaY = 0; deltaY < poolingHeight; deltaY++) {
						for(int deltaX = 0; deltaX < poolingWidth; deltaX++) {
							call.call(deltaX,  deltaY, source[(y + deltaY)* width + x + deltaX]);
						}
					}
					result[target++] = call.afterCall(); 
				}
			}
			
			return result;
		}
	}

	@Override
	public String toString() {
		return "PoolingLayer [name=" + name + ", width=" + width + ", height=" + height + ", pooling=" + pooling + ", call=" + call + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((call == null) ? 0 : call.hashCode());
		result = prime * result + height;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pooling == null) ? 0 : pooling.hashCode());
		result = prime * result + width;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PoolingLayer other = (PoolingLayer) obj;
		if (call == null) {
			if (other.call != null) return false;
		} else if (!call.equals(other.call)) return false;
		if (height != other.height) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (pooling != other.pooling) return false;
		if (width != other.width) return false;
		return true;
	}
}
