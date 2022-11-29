package chav1961.bt.neuralnetwork.interfaces;

public interface MatrixNeuralNetworkLayer extends NeuralNetworkLayer {
	int getWidth();
	int getHeight();
	int getTargetWidth(int sourceWidth);
	int getTargetHeight(int sourceHeight);
	float[] process(int width, int height, float... source);
}
