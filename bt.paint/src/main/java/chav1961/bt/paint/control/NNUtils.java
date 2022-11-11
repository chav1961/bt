package chav1961.bt.paint.control;

import java.util.Arrays;
import java.util.Random;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

// https://programforyou.ru/poleznoe/convolutional-network-from-scratch-part-zero-introduction

public class NNUtils {
	public static enum PoolingType {
		MAX_POOLING, AVERAGE_POOLING, SUM_POOLING
	}

	public static enum ActivationType {
		Sigmoid((x)->1/(1 + Math.exp(-1)),(x)->(1-x)*x), 
		TanH((x)->Math.tanh(x),(x)->1-x*x),
		ReLU((x)->Math.max(0,x),(x)->x > 0 ? 1 : 0),
		LeakyReLU((x)->x > 0 ? x : 0.1 * x,(x)->x > 0 ? 1 : 0.1);
		
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
	
	public static float[][] convolution(final float[] source, final int width, final int height, final ConvolutionFilter... filters) {
		if (source == null) {
			throw new NullPointerException("Source vector can't be null"); 
		}
		else if (width <= 1) {
			throw new IllegalArgumentException("Width [" + width + "] must be at least 2"); 
		}
		else if (height <= 1) {
			throw new IllegalArgumentException("Height [" + height + "] must be at least 2"); 
		}
		else {
			final float[][] result = new float[filters.length][];
			
			for(int filterNo = 0; filterNo < filters.length; filterNo++) {
				final ConvolutionFilter 	f = filters[filterNo];
				
				result[filterNo] = new float[0];
			}
			return result;
		}
	}

	public static float[] activation(final float[] source, final int width, final int height, final DoubleUnaryOperator op) {
		if (source == null) {
			throw new NullPointerException("Source vector can't be null"); 
		}
		else if (width <= 1) {
			throw new IllegalArgumentException("Width [" + width + "] must be at least 2"); 
		}
		else if (height <= 1) {
			throw new IllegalArgumentException("Height [" + height + "] must be at least 2"); 
		}
		else if (op == null) {
			throw new NullPointerException("Operator can't be nul"); 
		}
		else {
			final float[] result = new float[source.length];
			
			for(int index = 0; index < result.length; index++) {
				result[index] = (float)op.applyAsDouble(source[index]);
			}
			return result;
		}
	}
	
	public static float[] pooling(final float[] source, final int width, final int height, final int poolingWidth, final int poolingHeight, final PoolingType type) {
		if (source == null) {
			throw new NullPointerException("Source vector can't be null"); 
		}
		else if (width <= 1) {
			throw new IllegalArgumentException("Width [" + width + "] must be at least 2"); 
		}
		else if (height <= 1) {
			throw new IllegalArgumentException("Height [" + height + "] must be at least 2"); 
		}
		else if (poolingWidth <= 1) {
			throw new IllegalArgumentException("Pooling width [" + poolingWidth + "] must be at least 2"); 
		}
		else if (poolingHeight <= 1) {
			throw new IllegalArgumentException("Pooling height [" + poolingHeight + "] must be at least 2"); 
		}
		else if (type == null) {
			throw new NullPointerException("Pooling type can't be null"); 
		}
		else {
			final float[]	base = new float[poolingWidth * poolingHeight];
			final float[]	result = new float[(width / poolingWidth) * (height / poolingHeight)];
			int		target = 0;
			
			for(int y = 0; y < height - poolingHeight; y += poolingHeight) {
				for(int x = 0; x < width - poolingWidth; x += poolingWidth) {
					int index = 0;
					
					for(int deltaY = 0; deltaY < poolingHeight; deltaY++) {
						for(int deltaX = 0; deltaX < poolingWidth; deltaX++) {
							base[index++] = source[(y + deltaY)* width + x + deltaX]; 
						}
					}
					float value;
					
					switch (type) {
						case AVERAGE_POOLING	:
							value = 0;
							
							for(float item : base) {
								value += item;
							}
							value /= base.length;
							break;
						case MAX_POOLING		:
							value = base[0];
							
							for(float item : base) {
								value = Math.max(item, value);
							}
							break;
						case SUM_POOLING		:
							value = 0;
							
							for(float item : base) {
								value += item;
							}
							break;
						default :
							throw new UnsupportedOperationException("Pooling type [" + type + "] is not supported yet"); 
					}
					
					result[target++] = value; 
				}
			}
			
			return result;
		}
	}
	
	public static float[] createRandomMatrix(final int width, final int height) {
		if (width <= 1) {
			throw new IllegalArgumentException("Width [" + width + "] must be at least 2"); 
		}
		else if (height <= 1) {
			throw new IllegalArgumentException("Height [" + height + "] must be at least 2"); 
		}
		else {
			final float[]	result = new float[width * height];
			final Random	rand = new Random(System.nanoTime());
			
			for(int index = 0; index < result.length; index++) {
				result[index] = rand.nextFloat() - 0.5f;
			}
			return result;
		}
	}

	public static float[] forward(final float[] source, final float[] matrix, final int width, final int height, final DoubleUnaryOperator op) {
		if (source == null) {
			throw new NullPointerException("Source vector can't be null"); 
		}
		else if (matrix == null) {
			throw new NullPointerException("Weight matrix can't be null"); 
		}
		else if (width <= 1) {
			throw new IllegalArgumentException("Width [" + width + "] must be at least 2"); 
		}
		else if (height <= 1) {
			throw new IllegalArgumentException("Height [" + height + "] must be at least 2"); 
		}
		else if (op == null) {
			throw new NullPointerException("Operator can't be nul"); 
		}
		else {
			final float[]	result = new float[height];
			
			for (int index = 0; index < height; index++) {
				float  sum = 0;
				
				for(int row = 0; row < width; row++) {
					sum += source[row] * matrix[index * width + row];
				}
				result[index] = (float)op.applyAsDouble(sum);
			}
			return result;
		}
	}
	
	public static float calculateNNError(final float[] source, final float[] awaited, final DoubleBinaryOperator op) {
		if (source == null) {
			throw new NullPointerException("Source vector can't be null"); 
		}
		else if (awaited == null) {
			throw new NullPointerException("Awaited vector can't be null"); 
		}
		else if (op == null) {
			throw new NullPointerException("Operator can't be nul"); 
		}
		else if (source.length != awaited.length) {
			throw new IllegalArgumentException("Length of source vector [" + source.length + "] is differ from the length of awaited vector [" + awaited.length + "]"); 
		}
		else {
			float	sum = 0;
			
			for(int index = 0; index < source.length; index++) {
				sum += op.applyAsDouble(source[index], awaited[index]); 
			}
			return sum;
		}
	}

	public static float[] calculateNNDelta(final float[] source, final float[] awaited, final DoubleUnaryOperator op) {
		if (source == null) {
			throw new NullPointerException("Source vector can't be null"); 
		}
		else if (awaited == null) {
			throw new NullPointerException("Awaited vector can't be null"); 
		}
		else if (op == null) {
			throw new NullPointerException("Operator can't be nul"); 
		}
		else if (source.length != awaited.length) {
			throw new IllegalArgumentException("Length of source vector [" + source.length + "] is differ from the length of awaited vector [" + awaited.length + "]"); 
		}
		else {
			final float[]	result = new float[source.length];
			
			for(int index = 0; index < source.length; index++) {
				result[index] = (float)op.applyAsDouble(source[index] - awaited[index]); 
			}
			return result;
		}
	}	

	public static float[] calculateNNDelta(final float[] source, final float[] matrix, final int width, final int height, final DoubleUnaryOperator op) {
		if (source == null) {
			throw new NullPointerException("Source vector can't be null"); 
		}
		else if (op == null) {
			throw new NullPointerException("Operator can't be nul"); 
		}
		else if (source.length != width) {
			throw new IllegalArgumentException("Length of source vector [" + source.length + "] is differ from the width [" + width+ "]"); 
		}
		else {
			final float[]	result = new float[height];

			for(int index = 0; index < result.length; index++) {
				float 	sum = 0;
				
				for(int fromIndex = 0; index < source.length; index++) {
					sum += (float)op.applyAsDouble(source[fromIndex] * matrix[fromIndex * height + index]); 
				}
				result[index] = sum; 
			}			
			return result;
		}
	}	
	
	public static float[] calculateNNWeights(final float[] source, final float[] matrix, final int width, final int height, final DoubleUnaryOperator op) {
		if (source == null) {
			throw new NullPointerException("Source vector can't be null"); 
		}
		else if (op == null) {
			throw new NullPointerException("Operator can't be nul"); 
		}
		else if (source.length != width) {
			throw new IllegalArgumentException("Length of source vector [" + source.length + "] is differ from the width [" + width+ "]"); 
		}
		else {
			final float[]	result = new float[height];

			for(int index = 0; index < result.length; index++) {
				float 	sum = 0;
				
				for(int fromIndex = 0; index < source.length; index++) {
					sum += (float)op.applyAsDouble(source[fromIndex] * matrix[fromIndex * height + index]); 
				}
				result[index] = sum; 
			}			
			return result;
		}
	}	
	
	public static class ConvolutionFilter {
		private final String	filterName;
		private final int		filterWidth;
		private final int		filterHeight;
		private final int		filterStride;
		private final int		padding;
		private final float[]	coefficients;

		protected ConvolutionFilter(final String filterName, int filterSize, float... coefficients) {
			this(filterName, filterSize, filterSize, 1, 0, coefficients);
		}
		
		protected ConvolutionFilter(final String filterName, int filterSize, int filterStride, int padding, float... coefficients) {
			this(filterName, filterSize, filterSize, filterStride, padding, coefficients);
		}
		
		protected ConvolutionFilter(final String filterName, int filterWidth, int filterHeight, int filterStride, int padding, float... coefficients) {
			if (filterName == null) {
				throw new NullPointerException("Filter name can't be null");
			}
			else if (filterWidth <= 1) {
				throw new IllegalArgumentException("Filter width [" + filterWidth + "] must be at least 2");
			}
			else if (filterHeight <= 1) {
				throw new IllegalArgumentException("Filter height [" + filterHeight + "] must be at least 2");
			}
			else if (filterStride <= 0) {
				throw new IllegalArgumentException("Filter stride [" + filterStride + "] must be positive");
			}
			else if (padding < 0) {
				throw new IllegalArgumentException("Padding [" + padding + "] can't be negative");
			}
			else if (coefficients == null) {
				throw new NullPointerException("Coefficients can't be null");
			}
			else if (coefficients.length != filterWidth * filterHeight) {
				throw new IllegalArgumentException("Size of coefficient matrix is [" + coefficients.length + "], must be [" + (filterWidth * filterHeight) + "]");
			}
			else {
				this.filterName = filterName;
				this.filterWidth = filterWidth;
				this.filterHeight = filterHeight;
				this.filterStride = filterStride;
				this.padding = padding;
				this.coefficients = coefficients;
			}
		}

		@Override
		public String toString() {
			return "ConvolutionFilter [filterName=" + filterName + ", filterWidth=" + filterWidth + ", filterHeight="
					+ filterHeight + ", filterStride=" + filterStride + ", padding=" + padding + ", coefficients="
					+ Arrays.toString(coefficients) + "]";
		}
	}
}
