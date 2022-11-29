package chav1961.bt.neuralnetwork.layers;

import java.util.Arrays;

import chav1961.bt.neuralnetwork.interfaces.MatrixNeuralNetworkLayer;

public class ConvolutionLayer implements MatrixNeuralNetworkLayer {
	private final String	name;
	private final int		width;
	private final int		height;
	private final int		stride;
	private final int		padding;
	private float[]			filter;
	
	public ConvolutionLayer(final String layerName, final int width, final int height, final int stride, final int padding, final float... filter) {
		if (layerName == null || layerName.isEmpty()) {
			throw new IllegalArgumentException("Layer name can't be null"); 
		}
		else if (width <= 1) {
			throw new IllegalArgumentException("Layer filter width ["+width+"] must be greater than 1"); 
		}
		else if (height <= 1) {
			throw new IllegalArgumentException("Layer filter height ["+height+"] must be greater than 1"); 
		}
		else if (stride <= 0) {
			throw new IllegalArgumentException("Layer filter stride ["+stride+"] must be greater than 0"); 
		}
		else if (padding < 0) {
			throw new IllegalArgumentException("Layer filter padding ["+padding+"] must be greater or equals 0"); 
		}
		else if (filter == null) {
			throw new NullPointerException("Filter array can't be null"); 
		}
		else if (filter.length != width*height) {
			throw new IllegalArgumentException("Layer filter size ["+filter.length+"] is differ than width*height = ["+(width*height)+"]"); 
		}
		else {
			this.name = layerName;
			this.width = width;
			this.height = height;
			this.stride = stride;
			this.padding = padding;
			this.filter = filter.clone();
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
		if (sourceWidth <= 0) {
			throw new IllegalArgumentException("Source width ["+sourceWidth+"] must be positive"); 
		}
		else {
			return (sourceWidth - getWidth() + 2 * padding) / stride + 1;
		}
	}

	@Override
	public int getTargetHeight(final int sourceHeight) {
		if (sourceHeight <= 0) {
			throw new IllegalArgumentException("Source height ["+sourceHeight+"] must be positive"); 
		}
		else {
			return (sourceHeight - getHeight() + 2 * padding) / stride + 1;
		}
	}
	
	@Override
	public float[] process(final int width, final int height, final float... source) {
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
			final int 		newWidth = getTargetWidth(width), newHeight = getTargetHeight(height);
			final int		filterWidth = getWidth();
			final int		step = stride;
			final int		deltaXStart = -getWidth() / 2, deltaXEnd = getWidth() / 2; 
			final int		deltaYStart = -getHeight() / 2, deltaYEnd = getHeight() / 2; 
			final int		xStart = 0 + getWidth() / 2 - padding, xEnd = width - getWidth() / 2 + padding; 
			final int		yStart = 0 + getHeight() / 2 - padding, yEnd = height - getHeight() / 2 + padding; 
			final float[]	result = new float[newWidth * newHeight];
			final float[]	temp = filter;
			int				target = 0;

			for (int y = yStart; y < yEnd; y += step) {
				for (int x = xStart; x < xEnd; x += step) {
					float	sum = 0;
					
					for (int deltaY = deltaYStart; deltaY < deltaYEnd; deltaY++) {
						for (int deltaX = deltaXStart; deltaX < deltaXEnd; deltaX++) {
							sum += source[(y + deltaY) * width + (x + deltaX)] * temp[(deltaY - deltaYStart) * filterWidth + (deltaX - deltaXStart)];
						}
					}
					result[target++] = sum;
				}
			}
			return result;
		}
	}

	@Override
	public String toString() {
		return "ConvolutionLayer [name=" + name + ", width=" + width + ", height=" + height + ", stride=" + stride + ", padding=" + padding + ", filter=" + Arrays.toString(filter) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(filter);
		result = prime * result + height;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + padding;
		result = prime * result + stride;
		result = prime * result + width;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		ConvolutionLayer other = (ConvolutionLayer) obj;
		if (!Arrays.equals(filter, other.filter)) return false;
		if (height != other.height) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (padding != other.padding) return false;
		if (stride != other.stride) return false;
		if (width != other.width) return false;
		return true;
	}
}
