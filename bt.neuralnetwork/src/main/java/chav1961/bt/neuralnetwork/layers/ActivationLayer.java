package chav1961.bt.neuralnetwork.layers;

import java.util.function.DoubleUnaryOperator;

import chav1961.bt.neuralnetwork.interfaces.ActivationType;
import chav1961.bt.neuralnetwork.interfaces.MatrixNeuralNetworkLayer;

public class ActivationLayer implements MatrixNeuralNetworkLayer {
	private final String				name;
	private final ActivationType		type;
	private final DoubleUnaryOperator	function;
	private final DoubleUnaryOperator	delta;
	
	public ActivationLayer(final String layerName, final ActivationType activationType) {
		this(layerName, activationType, null, null);
	}

	public ActivationLayer(final String layerName, final DoubleUnaryOperator function, final DoubleUnaryOperator delta) {
		this(layerName, ActivationType.UserDefined, function, delta);
	}
	
	protected ActivationLayer(final String layerName, final ActivationType activationType, final DoubleUnaryOperator function, final DoubleUnaryOperator delta) {
		if (layerName == null || layerName.isEmpty()) {
			throw new IllegalArgumentException("Layer name can't be null"); 
		}
		else if (activationType == null) {
			throw new NullPointerException("Activation type can't be null"); 
		}
		else if (activationType == ActivationType.UserDefined && (function == null || delta == null)) {
			throw new IllegalArgumentException("Activation type ["+activationType+"] requires user-defined function and delta parameter"); 
		}
		else if (activationType != ActivationType.UserDefined && (function != null || delta != null)) {
			throw new IllegalArgumentException("Activation type ["+activationType+"] doesn't support user-defined function and delta parameter"); 
		}
		else {
			this.name = layerName;
			this.type = activationType;
			this.function = function != null ? function : activationType.getFunction();
			this.delta = delta != null ? delta : activationType.getDelta();
		}
	}
	
	@Override
	public String getLayerName() {
		return name;
	}

	@Override
	public int getWidth() {
		return 1;
	}

	@Override
	public int getHeight() {
		return 1;
	}

	@Override
	public int getTargetWidth(final int sourceWidth) {
		return sourceWidth;
	}

	@Override
	public int getTargetHeight(final int sourceHeight) {
		return sourceHeight;
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
			final DoubleUnaryOperator	f = function;
			final float[]	result = new float[source.length];
			
			for(int index = 0, maxIndex = source.length; index < maxIndex; index++) {
				result[index] = (float)f.applyAsDouble(source[index]);
			}
			return result;
		}		
	}

	@Override
	public String toString() {
		return "ActivationLayer [name=" + name + ", type=" + type + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delta == null) ? 0 : delta.hashCode());
		result = prime * result + ((function == null) ? 0 : function.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ActivationLayer other = (ActivationLayer) obj;
		if (delta == null) {
			if (other.delta != null) return false;
		} else if (!delta.equals(other.delta)) return false;
		if (function == null) {
			if (other.function != null) return false;
		} else if (!function.equals(other.function)) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (type != other.type) return false;
		return true;
	}
}
