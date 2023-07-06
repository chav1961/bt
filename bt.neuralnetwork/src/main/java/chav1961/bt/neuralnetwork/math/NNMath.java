package chav1961.bt.neuralnetwork.math;

import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

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
	 */
	public static float random(final RandomType type) {
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
	 * @param source source vector to fill random numbers. Can't be null</p>
	 */
	public static void random(final RandomType type, final float[] source) {
		if (type == null) {
			throw new NullPointerException("Random type can't be null"); 
		}
		else {
			final DoubleSupplier func = type.getFunction();
			
			for(int index = 0; index < source.length; index++) {
				source[index] = (float)func.getAsDouble();
			}
		}
	}
	
	/**
	 * <p>Calculate matrix multiplication</p>
	 * @param left left matrix to multiply. Can't be null
	 * @param leftRow number of rows in left matrix. Must be greater than 0
	 * @param leftCol number of columns in left matrix. Must be greater than 0
	 * @param right right matrix to multiply. Can't be null
	 * @param rightRow number of rows in right matrix. Must be greater than 0
	 * @param rightCol number of columns in right matrix. Must be greater than 0
	 * @return matrix multiplication. Can't be null
	 * @throws NullPointerException any matrix is null
	 * @throws IllegalArgumentException some argument restrictions failed
	 */
	public static float[] matrixMul(final float[] left, final int leftRow, final int leftCol, final float[] right, final int rightRow, final int rightCol) throws NullPointerException, IllegalArgumentException {
		if (left == null) {
			throw new NullPointerException("Left matrix can't be null"); 
		}
		else if (right == null) {
			throw new NullPointerException("Right matrix can't be null"); 
		}
		else if (leftRow <= 0 || leftCol <= 0) {
			throw new IllegalArgumentException("Left matrix dimensions ["+leftRow+"*"+leftCol+"] must be greater than 0"); 
		}
		else if (rightRow <= 0 || rightCol <= 0) {
			throw new IllegalArgumentException("Right matrix dimensions ["+rightRow+"*"+rightCol+"] must be greater than 0"); 
		}
		else if (leftRow * leftCol != left.length) {
			throw new IllegalArgumentException("Number of left matrix items ["+left.length+"] is differ than dimensions typed ["+leftRow+"*"+leftCol+"] = ["+(leftRow*leftCol)+"]"); 
		}
		else if (rightRow * rightCol != right.length) {
			throw new IllegalArgumentException("Number of right matrix items ["+right.length+"] is differ than dimensions typed ["+rightRow+"*"+rightCol+"] = ["+(rightRow*rightCol)+"]"); 
		}
		else if (rightRow != leftCol) {
			throw new IllegalArgumentException("Number of columns in left matrix ["+leftCol+"] is differ than number of rows in right matrix ["+rightRow+"]"); 
		}
		else {
			return matrixMul(left, leftRow, leftCol, right, rightRow, rightCol, new float[leftRow * rightCol]);
		}
	}

	/**
	 * <p>Calculate matrix multiplication</p>
	 * @param left left matrix to multiply. Can't be null
	 * @param leftRow number of rows in left matrix. Must be greater than 0
	 * @param leftCol number of columns in left matrix. Must be greater than 0
	 * @param right right matrix to multiply. Can't be null
	 * @param rightRow number of rows in right matrix. Must be greater than 0
	 * @param rightCol number of columns in right matrix. Must be greater than 0
	 * @param result matrix to store result. Can't be null and must have length = leftRows * rightColumns 
	 * @return result matrix (can be used in chain equations)
	 * @throws NullPointerException any matrix is null
	 * @throws IllegalArgumentException some argument restrictions failed
	 */
	public static float[] matrixMul(final float[] left, final int leftRow, final int leftCol, final float[] right, final int rightRow, final int rightCol, final float[] result)  throws NullPointerException, IllegalArgumentException {
		if (left == null) {
			throw new NullPointerException("Left matrix can't be null"); 
		}
		else if (right == null) {
			throw new NullPointerException("Right matrix can't be null"); 
		}
		else if (result == null) {
			throw new NullPointerException("Matrix to store result can't be null"); 
		}
		else if (leftRow <= 0 || leftCol <= 0) {
			throw new IllegalArgumentException("Left matrix dimensions ["+leftRow+"*"+leftCol+"] must be greater than 0"); 
		}
		else if (rightRow <= 0 || rightCol <= 0) {
			throw new IllegalArgumentException("Right matrix dimensions ["+rightRow+"*"+rightCol+"] must be greater than 0"); 
		}
		else if (leftRow * leftCol != left.length) {
			throw new IllegalArgumentException("Number of left matrix items ["+left.length+"] is differ than dimensions typed ["+leftRow+"*"+leftCol+"] = ["+(leftRow*leftCol)+"]"); 
		}
		else if (rightRow * rightCol != right.length) {
			throw new IllegalArgumentException("Number of right matrix items ["+right.length+"] is differ than dimensions typed ["+rightRow+"*"+rightCol+"] = ["+(rightRow*rightCol)+"]"); 
		}
		else if (rightRow != leftCol) {
			throw new IllegalArgumentException("Number of columns in left matrix ["+leftCol+"] is differ than number of rows in right matrix ["+rightRow+"]"); 
		}
		else if (leftRow * rightCol != result.length) {
			throw new IllegalArgumentException("Number of result matrix items ["+result.length+"] is differ than dimensions typed ["+leftRow+"*"+rightCol+"] = ["+(leftRow*rightCol)+"]"); 
		}
		else {
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
