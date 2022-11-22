package chav1961.bt.neuralnetwork;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

// https://programforyou.ru/poleznoe/convolutional-network-from-scratch-part-zero-introduction

public class NeuralNetworkUtils {
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

	public static BufferedImage scaleTemplate(final BufferedImage source, final float scale) {
		if (source == null) {
			throw new NullPointerException("Source image can't be null"); 
		}
		else {
			final int			newWidth = (int) (source.getWidth() * scale);
			final int			newHeight = (int) (source.getHeight() * scale);
			final BufferedImage	result = new BufferedImage(newWidth, newHeight, source.getType());
			final Graphics2D	g2d = (Graphics2D) result.getGraphics();

			g2d.drawImage(source, 0, 0, newWidth-1, newHeight-1, 0, 0, source.getWidth()-1, source.getHeight()-1, null);
			g2d.dispose();
			return result;
		}
	}
	
	public static BufferedImage rotateTemplate(final BufferedImage source, final float angle) {
		if (source == null) {
			throw new NullPointerException("Source image can't be null"); 
		}
		else {
			final int			newWidth = (int) (source.getWidth() * (1 + Math.abs(Math.cos(angle))));
			final int			newHeight = (int) (source.getHeight() * (1 + Math.abs(Math.sin(angle))));
			final BufferedImage	result = new BufferedImage(newWidth, newHeight, source.getType());
			final Graphics2D	g2d = (Graphics2D) result.getGraphics();

			g2d.setTransform(AffineTransform.getRotateInstance(angle));
			g2d.drawImage(source, 0, 0, newWidth-1, newHeight-1, 0, 0, source.getWidth()-1, source.getHeight()-1, null);
			g2d.dispose();
			return result;
		}
	}
	
	public static BufferedImage fillTemplateByPerlinNoise(final BufferedImage source, final int octaves, final float amplitude) {
		if (source == null) {
			throw new NullPointerException("Source image can't be null"); 
		}
		else if (octaves <= 0) {
			throw new IllegalArgumentException("Octaves value ["+octaves+"] must be positive"); 
		}
		else if (amplitude <= 0) {
			throw new IllegalArgumentException("Amplitude value ["+amplitude+"] must be positive"); 
		}
		else {
			final BufferedImage	result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
			final Perlin2D		p2d = new Perlin2D((int) System.nanoTime());
			
			for(int y = 0, maxY = source.getHeight(); y < maxY; y++) {
				for(int x = 0, maxX = source.getWidth(); x < maxX; x++) {
					int	color = source.getRGB(x, y);
					int noise = (int) (p2d.getNoise(x, y, octaves) * amplitude);
					
					color = (color & 0xFF000000) | (addNoise(((color & 0xFF0000) >> 16), noise) << 16) | (addNoise(((color & 0xFF00) >> 8), noise) << 8) | addNoise((color & 0xFF00), noise); 
					
					result.setRGB(x, y, color);
				}
			}
			return result;
		}
	}
	
	public static NeuralNetworkDetector learn(final BufferedImage image, final float scaleFrom, final float scaleTo, final float angleFrom, final float angleTo) {
		if (image == null) {
			throw new NullPointerException("Image to learn can't be null");
		}
		else {
			final float	scaleStep = (float) ((Math.log(scaleTo) - Math.log(scaleFrom)) / 10);
			final float angleStep = (angleTo - angleFrom) / 10;
			
			for (float angle = angleFrom; angle < angleTo; angle += angleStep) {
				final BufferedImage	rotated = rotateTemplate(image, angle);
						
				for (float scale = scaleFrom; scaleFrom < scaleTo; scale *= Math.exp(scaleStep)) {
					learn(fillTemplateByPerlinNoise(scaleTemplate(rotated, scale),1,0.1f));
				}
			}
			return null;
		}
	}
	
	private static void learn(BufferedImage fillTemplateByPerlinNoise) {
		// TODO Auto-generated method stub
		
	}

	private static int addNoise(final int color, final int noise) {
		return Math.max(255, Math.min(0, color+noise));
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
	
	private static class Perlin2D {
		private static final float[][]	GRADIENTS = {{1,0},{-1,0},{0,1},{0,-1}};  
	    
		final byte[] 					permutationTable;

	    public Perlin2D() {
	    	this(0);
	    }	    
	    
	    public Perlin2D(final int seed) {
	    	final Random 	rand = new Random(seed);
	        
	    	this.permutationTable = new byte[1024];
	    	for (int index = 0; index < permutationTable.length; index++) {
	    		this.permutationTable[index] = (byte)rand.nextInt();
	    	}
	    }

	    public float getNoise(final float fx, final float fy) {
	        final int 		left = (int)Math.floor(fx);
	        final int 		top = (int)Math.floor(fy);
	        final float[] 	topLeftGradient = getPseudoRandomGradient(left, top);
	        final float[] 	topRightGradient = getPseudoRandomGradient(left+1, top);
	        final float[] 	bottomLeftGradient = getPseudoRandomGradient(left, top+1);
	        final float[] 	bottomRightGradient = getPseudoRandomGradient(left+1, top+1);
	        final float 	pointInQuadX = fx - left;
	        final float		pointInQuadY = fy - top;
	        final float[] 	distanceToTopLeft = new float[]{pointInQuadX, pointInQuadY};
	        final float[] 	distanceToTopRight = new float[]{pointInQuadX-1, pointInQuadY};
	        final float[] 	distanceToBottomLeft = new float[]{pointInQuadX, pointInQuadY-1};
	        final float[] 	distanceToBottomRight = new float[]{pointInQuadX-1, pointInQuadY-1};
	        final float 	tx1 = dot(distanceToTopLeft, topLeftGradient);
	        final float 	tx2 = dot(distanceToTopRight, topRightGradient);
	        final float 	bx1 = dot(distanceToBottomLeft, bottomLeftGradient);
	        final float 	bx2 = dot(distanceToBottomRight, bottomRightGradient);
	        final float 	pointInQuadXCurve = qunticCurve(pointInQuadX);
	        final float 	pointInQuadYCurve = qunticCurve(pointInQuadY);
	        final float 	tx = lerp(tx1, tx2, pointInQuadXCurve);
	        final float 	bx = lerp(bx1, bx2, pointInQuadXCurve);
	        final float 	tb = lerp(tx, bx, pointInQuadYCurve);

	        return tb;
	    }

	    public float getNoise(final float fx, final float fy, final int octaves) {
	    	return getNoise(fx, fy, octaves, 0.5f);
	    }	    
	    
	    public float getNoise(final float fx, final float fy, final int octaves, final float persistence) {
	    	float currentFx = fx, currentFy = fy;
	        float amplitude = 1;
	        float max = 0;
	        float result = 0;

	        for(int index = 0; index < octaves; index++) {
	            max += amplitude;
	            result += getNoise(currentFx, currentFy) * amplitude;
	            amplitude *= persistence;
	            currentFx *= 2;
	            currentFy *= 2;
	        }

	        return result/max;
	    }

	    private float[] getPseudoRandomGradient(final int x, final int y) {
	        final int index = (int)(((x * 1836311903L) ^ (y * 2971215073L) + 4807526976L) & 1023);
	        
	        return GRADIENTS[permutationTable[index] & 0x03];
	    }

	    private static float qunticCurve(final float t) {
	        return t * t * t * (t * (t * 6 - 15) + 10);
	    }

	    private static float lerp(final float a, final float b, final float t) {
	        return a + (b - a) * t;
	    }

	    private static float dot(final float[] a, final float[] b) {
	        return a[0] * b[0] + a[1] * b[1];
	    }
	}
}
