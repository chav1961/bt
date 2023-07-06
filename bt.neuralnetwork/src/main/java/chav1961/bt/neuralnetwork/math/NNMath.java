package chav1961.bt.neuralnetwork.math;

import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

import chav1961.purelib.matrix.interfaces.FloatMatrix;

/**
 * 
 *
 */
public class NNMath {
	private static final double	INV_SQRT_PI = 1 / Math.sqrt(2 * Math.PI); 
	
	/**
	 * <p>This enumeration describes random distribution type</p> 
	 */
	public static enum RandomType {
		UNIFORM(()->Math.random()),
		NORMAL(()->{
			final double	rnd = Math.random();
			return INV_SQRT_PI * Math.exp(-0.5*rnd*rnd);
		});
		
		private final DoubleSupplier	ds;
		
		private RandomType(final DoubleSupplier ds) {
			this.ds = ds;
		}
		
		public DoubleSupplier getFunction() {
			return ds;
		}
	}
	
	/**
	 * <p>Generate random number with the given distribution</p>
	 * @param type distribution type. Can't be null
	 * @return random generated
	 * @throws NullPointerException on any argument is null
	 */
	public static float random(final RandomType type) throws NullPointerException {
		if (type == null) {
			throw new NullPointerException("Random type can't be null"); 
		}
		else {
			return (float)type.getFunction().getAsDouble();
		}
	}

	/**
	 * <p>Fill vector with random numbers</p>
	 * @param type distribution type. Can't be null
	 * @param source source vector to fill random numbers. Can't be null
	 * @throws NullPointerException on any argument is null
	 */
	public static void random(final RandomType type, final float[] source) throws NullPointerException {
		if (type == null) {
			throw new NullPointerException("Random type can't be null"); 
		}
		else if (source == null) {
			throw new NullPointerException("Matrix to fill can't be null"); 
		}
		else {
			final DoubleSupplier func = type.getFunction();
			final DoubleUnaryOperator  duo = (v)->func.getAsDouble();
			
			function(source, duo, source);
		}
	}
	
	/**
	 * <p>Calculate matrix multiplication</p>
	 * @param left left matrix to multiply. Can't be null
	 * @param leftLines number of lines in left matrix. Must be greater than 0
	 * @param leftCols number of rows in left matrix. Must be greater than 0
	 * @param right right matrix to multiply. Can't be null
	 * @param rightLines number of lines in right matrix. Must be greater than 0
	 * @param rightCols number of columns in right matrix. Must be greater than 0
	 * @return matrix multiplication. Can't be null
	 * @throws NullPointerException any matrix is null
	 * @throws IllegalArgumentException some argument restrictions failed
	 */
	public static float[] matrixMul(final float[] left, final int leftLines, final int leftCols, final float[] right, final int rightLines, final int rightCols) throws NullPointerException, IllegalArgumentException {
		if (left == null) {
			throw new NullPointerException("Left matrix can't be null"); 
		}
		else if (right == null) {
			throw new NullPointerException("Right matrix can't be null"); 
		}
		else if (leftCols <= 0 || leftLines <= 0) {
			throw new IllegalArgumentException("Left matrix dimensions ["+leftCols+"*"+leftLines+"] must be greater than 0"); 
		}
		else if (rightLines <= 0 || rightCols <= 0) {
			throw new IllegalArgumentException("Right matrix dimensions ["+rightLines+"*"+rightCols+"] must be greater than 0"); 
		}
		else if (leftCols * leftLines != left.length) {
			throw new IllegalArgumentException("Number of left matrix items ["+left.length+"] is differ than dimensions typed ["+leftCols+"*"+leftLines+"] = ["+(leftCols*leftLines)+"]"); 
		}
		else if (rightLines * rightCols != right.length) {
			throw new IllegalArgumentException("Number of right matrix items ["+right.length+"] is differ than dimensions typed ["+rightLines+"*"+rightCols+"] = ["+(rightLines*rightCols)+"]"); 
		}
		else if (rightLines != leftCols) {
			throw new IllegalArgumentException("Number of columns in left matrix ["+leftLines+"] is differ than number of lines in right matrix ["+rightCols+"]"); 
		}
		else {
			return matrixMul(left, leftLines, leftCols, right, rightLines, rightCols, new float[leftLines * rightCols]);
		}
	}

	/**
	 * <p>Calculate matrix multiplication</p>
	 * @param left left matrix to multiply. Can't be null
	 * @param leftLines number of lines in left matrix. Must be greater than 0
	 * @param leftCols number of columns in left matrix. Must be greater than 0
	 * @param right right matrix to multiply. Can't be null
	 * @param rightLines number of lines in right matrix. Must be greater than 0
	 * @param rightCols number of columns in right matrix. Must be greater than 0
	 * @param result matrix to store result. Can't be null and must have length = leftRows * rightColumns 
	 * @return result matrix (can be used in chain equations)
	 * @throws NullPointerException any matrix is null
	 * @throws IllegalArgumentException some argument restrictions failed
	 */
	public static float[] matrixMul(final float[] left, final int leftLines, final int leftCols, final float[] right, final int rightLines, final int rightCols, final float[] result)  throws NullPointerException, IllegalArgumentException {
		if (left == null) {
			throw new NullPointerException("Left matrix can't be null"); 
		}
		else if (right == null) {
			throw new NullPointerException("Right matrix can't be null"); 
		}
		else if (result == null) {
			throw new NullPointerException("Matrix to store result can't be null"); 
		}
		else if (leftLines <= 0 || leftCols <= 0) {
			throw new IllegalArgumentException("Left matrix dimensions ["+leftLines+"*"+leftCols+"] must be greater than 0"); 
		}
		else if (rightLines <= 0 || rightCols <= 0) {
			throw new IllegalArgumentException("Right matrix dimensions ["+rightLines+"*"+rightCols+"] must be greater than 0"); 
		}
		else if (leftLines * leftCols != left.length) {
			throw new IllegalArgumentException("Number of left matrix items ["+left.length+"] is differ than dimensions typed ["+leftLines+"*"+leftCols+"] = ["+(leftLines*leftCols)+"]"); 
		}
		else if (rightLines * rightCols != right.length) {
			throw new IllegalArgumentException("Number of right matrix items ["+right.length+"] is differ than dimensions typed ["+rightLines+"*"+rightCols+"] = ["+(rightLines*rightCols)+"]"); 
		}
		else if (rightLines != leftCols) {
			throw new IllegalArgumentException("Number of columns in left matrix ["+leftCols+"] is differ than number of lines in right matrix ["+rightLines+"]"); 
		}
		else if (leftLines * rightCols != result.length) {
			throw new IllegalArgumentException("Number of result matrix items ["+result.length+"] is differ than dimensions typed ["+leftLines+"*"+rightCols+"] = ["+(leftLines*rightCols)+"]"); 
		}
		else {
			for(int i = 0; i < leftLines; i++) {
				for(int j = 0; j < rightCols; j++) {
					double sum = 0;
					
					for(int k = 0; k < leftCols; k++) {
						sum += left[i * leftCols + k] * right[k * rightCols + j];
					}
					result[i * rightCols + j] = (float)sum;
				}
			}
		    return result;
		}
	}

	/**
	 * <p>Calculate matrix sum</p>
	 * @param left left matrix to sum. Can't be null
	 * @param row number of rows in both matrices. Must be greater than 0
	 * @param col number of columns in both matrices. Must be greater than 0
	 * @param right right matrix to sum. Can't be null
	 * @return matrix sum. Can't be null
	 * @throws NullPointerException any matrix is null
	 * @throws IllegalArgumentException some argument restrictions failed
	 */
	public static float[] matrixAdd(final float[] left, final int row, final int col, final float[] right)  throws NullPointerException, IllegalArgumentException {
		if (left == null) {
			throw new NullPointerException("Left matrix can't be null"); 
		}
		else if (right == null) {
			throw new NullPointerException("Right matrix can't be null"); 
		}
		else if (row <= 0 || col <= 0) {
			throw new IllegalArgumentException("Matrix dimensions ["+row+"*"+col+"] must be greater than 0"); 
		}
		else if (row * col != left.length) {
			throw new IllegalArgumentException("Number of left matrix items ["+left.length+"] is differ than dimensions typed ["+row+"*"+col+"] = ["+(row*col)+"]"); 
		}
		else if (row * col != right.length) {
			throw new IllegalArgumentException("Number of right matrix items ["+right.length+"] is differ than dimensions typed ["+row+"*"+col+"] = ["+(row*col)+"]"); 
		}
		else {
			return matrixAdd(left, row, col, right, new float[row * col]);
		}
	}

	/**
	 * <p>Calculate matrix sum</p>
	 * @param left left matrix to sum. Can't be null
	 * @param row number of rows in both matrices. Must be greater than 0
	 * @param col number of columns in both matrices. Must be greater than 0
	 * @param right right matrix to sum. Can't be null
	 * @param result matrix to store result. Can't be null and must have the same size as left and right matrices 
	 * @return result matrix (can be used in chain equations)
	 * @throws NullPointerException any matrix is null
	 * @throws IllegalArgumentException some argument restrictions failed
	 */
	public static float[] matrixAdd(final float[] left, final int row, final int col, final float[] right, final float[] result)  throws NullPointerException, IllegalArgumentException {
		if (left == null) {
			throw new NullPointerException("Left matrix can't be null"); 
		}
		else if (right == null) {
			throw new NullPointerException("Right matrix can't be null"); 
		}
		else if (result == null) {
			throw new NullPointerException("Result matrix can't be null"); 
		}
		else if (row <= 0 || col <= 0) {
			throw new IllegalArgumentException("Matrix dimensions ["+row+"*"+col+"] must be greater than 0"); 
		}
		else if (row * col != left.length) {
			throw new IllegalArgumentException("Number of left matrix items ["+left.length+"] is differ than dimensions typed ["+row+"*"+col+"] = ["+(row*col)+"]"); 
		}
		else if (row * col != right.length) {
			throw new IllegalArgumentException("Number of right matrix items ["+right.length+"] is differ than dimensions typed ["+row+"*"+col+"] = ["+(row*col)+"]"); 
		}
		else if (row * col != result.length) {
			throw new IllegalArgumentException("Number of result matrix items ["+result.length+"] is differ than dimensions typed ["+row+"*"+col+"] = ["+(row*col)+"]"); 
		}
		else {
			for(int index = 0; index < left.length; index++) {
				result[index] = left[index] + right[index];
			}
			return result;
		}		
	}
	
	/**
	 * <p>Apply function to matrix content</p>
	 * @param source matrix to apply function to. Can't be null
	 * @param func function to apply. Can't be null
	 * @return matrix with values applied. Can't be null
	 * @throws NullPointerException any matrix is null
	 * @throws IllegalArgumentException some argument restrictions failed
	 */
	public static float[] function(final float[] source, final DoubleUnaryOperator func) throws NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Source matrix can't be null"); 
		}
		else if (func == null) {
			throw new NullPointerException("Function can't be null"); 
		}
		else {
			return function(source, func, new float[source.length]);
		}
	}

	/**
	 * <p>Apply function to matrix content</p>
	 * @param source matrix to apply function to. Can't be null
	 * @param func function to apply. Can't be null
	 * @param result matrix to store values. Can't be null and must have the same size as source matrix
	 * @return result matrix (can be used in chain equations)
	 * @throws NullPointerException any matrix is null
	 * @throws IllegalArgumentException some argument restrictions failed
	 */
	public static float[] function(final float[] source, final DoubleUnaryOperator func, final float[] result) throws NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Source matrix can't be null"); 
		}
		else if (func == null) {
			throw new NullPointerException("Function can't be null"); 
		}
		else if (result == null) {
			throw new NullPointerException("Result matrix can't be null"); 
		}
		else if (source.length != result.length) {
			throw new IllegalArgumentException("Result matrix size ["+result.length+"] is differ than source ["+source.length+"]"); 
		}
		else {
			for(int index = 0; index < source.length; index++) {
				result[index] = (float)func.applyAsDouble(source[index]);
			}
			return result;
		}
	}
	
	/**
	 * <p>Calculate softmax(vector).</p>
	 * @param source source vector to calculate. Can't be null</p>
	 * @return new vector with calculated values. Can't be null
	 */
	public static float[] softmax(final float[] source) {
		if (source == null) {
			throw new NullPointerException("Source vector can't be null");
		}
		else {
			final float[] 	result = new float[source.length];
			double 			sum = 0;
			
			for(int index = 0; index < source.length; index++) {
				final double	e = Math.exp(source[index]);
				
				result[index] = (float)e;
				sum += e;
			}
			sum = 1 / sum;
			for(int index = 0; index < source.length; index++) {
				result[index] *= sum;
			}
			return result;
		}
	}

	/**
	 * <p>Calculate softmax(vector) and place it into source vector.</p>
	 * @param source source vector to calculate. Can't be null. Will be changed after calculation</p>
	 */
	public static void softmaxInPlace(final float[] source) {
		if (source == null) {
			throw new NullPointerException("Source vector can't be null");
		}
		else {
			double 			sum = 0;
			
			for(int index = 0; index < source.length; index++) {
				final double	e = Math.exp(source[index]);
				
				source[index] = (float)e;
				sum += e;
			}
			sum = 1 / sum;
			for(int index = 0; index < source.length; index++) {
				source[index] *= sum;
			}
		}
	}
}
