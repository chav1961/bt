package chav1961.bt.matrix;

import java.util.Arrays;
import java.util.function.BiConsumer;

import chav1961.bt.matrix.MatrixLib.Command;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.Type;

public class Calculator implements AutoCloseable {
	private final int		stackDepth;
	private final Command[]	commands;
	
	Calculator(final int stackDepth, final Command... commands) {
		this.stackDepth = stackDepth;
		this.commands = commands;
	}
	
	public <T> T calculate(final MatrixImpl... operands) throws CalculationException {
		if (operands == null || Utils.checkArrayContent4Nulls(operands) >= 0) {
			throw new IllegalArgumentException("Operands are null or contain nulls inside");
		}
		else {
			final Stack		stack = new Stack(stackDepth);
			final Matrix[]	temporary = new Matrix[1];
			double[]		val;
			Matrix			mat;
			
			for(Command item : commands) {
				switch (item.op) {
					case ADD			:
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).add(mat), true);
						break;
					case ADD_MATRIX_VAL	:
						val = stack.popValue();
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						stack.pushMatrix(mat.add(castValue(val, mat.getType())), true);
						break;
					case ADD_VAL		:
						stack.pushValue(add(stack.popValue(), stack.popValue()));
						break;
					case ADD_VAL_MATRIX	:
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						val = stack.popValue();
						stack.pushMatrix(mat.add(castValue(val, mat.getType())), true);
						break;
					case DET			:
						stack.pushValue(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).det().doubleValue());
						break;
					case INVERT			:
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).invert(), true);
						break;
					case LOAD_MATRIX	:
						if (item.operand - 1 >= operands.length) {
							throw new CalculationException("Matrix name #"+item.operand+" is missing in the parameters");
						}
						else {
							stack.pushMatrix(operands[(int)item.operand - 1], false);
						}
						break;
					case LOAD_REAL		:
						stack.pushValue(new double[] {Double.longBitsToDouble(item.operand), 0.0});
						break;
					case LOAD_IMAGE		:
						stack.pushValue(new double[] {0.0, Double.longBitsToDouble(item.operand)});
						break;
					case MINUS			:
						stack.pushValue(negate(stack.popValue()));
						break;
					case MUL			:
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).mul(mat), true);
						break;
					case MUL_H			:
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).mulHadamard(mat), true);
						break;
					case MUL_K			:
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).tensorMul(mat), true);
						break;
					case MUL_MATRIX_VAL	:
						val = stack.popValue();
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						stack.pushMatrix(mat.mul(castValue(val, mat.getType())), true);
						break;
					case MUL_VAL		:
						stack.pushValue(mul(stack.popValue(), stack.popValue()));
						break;
					case MUL_VAL_MATRIX	:
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						stack.pushMatrix(mat.mul(castValue(stack.popValue(), mat.getType())), true);
						break;
					case NEGATE			:
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).mul(-1), true);
						break;
//					case POWER			:
//						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).power(Double.longBitsToDouble(item.operand)), true);
//						break;
					case POWER_VAL		:
						stack.pushValue(Math.pow(stack.popValue()[0], Double.longBitsToDouble(item.operand)));
						break;
					case SPOOR			:
						stack.pushValue(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).track().doubleValue());
						break;
					case SUB			:
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).subtract(mat), true);
						break;
					case SUB_MATRIX_VAL	:
						val = stack.popValue();
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						stack.pushMatrix(mat.add(negate(castValue(val, mat.getType()))), true);
						break;
					case SUB_VAL		:
						stack.pushValue(add(negate(stack.popValue()), stack.popValue()));
						break;
					case SUB_VAL_MATRIX	:
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						val = stack.popValue();
						stack.pushMatrix(mat.subtractFrom(castValue(val, mat.getType())), true);
						break;
					case TRANSPOSE		:
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).transpose(), true);
						break;
					default:
						throw new UnsupportedOperationException("Operation ["+item.op+"] is not supported yet");
				}
				if (temporary[0] != null) {
					temporary[0].close();
					temporary[0] = null;
				}
			}
			if (stack.isValue()) {
				return (T)stack.popValue();
			}
			else {
				return (T)stack.popMatrix((f,m)->{});
			}
		}
	}
	
	@Override
	public void close() throws RuntimeException {
	}

	@Override
	public String toString() {
		return "Calculator [commands=" + Arrays.toString(commands) + "]";
	}
	
	private static double[] add(final double[] x, final double[] y) {
		return new double[] {x[0] + y[0], x[1] + y[1]};
	}

	private static double[] mul(final double[] x, final double[] y) {
		return new double[] {x[0]*y[0] - x[1]*y[1], x[0]*y[1] + x[1]*y[0]};
	}
	
	private static double[] negate(final double[] x) {
		final double[] result = x.clone();
		
		for(int index = 0; index < result.length; index++) {
			result[index] = - result[index];
		}
		return result;
	}

	private static double[] castValue(final double[] source, final Type type) {
		if (source.length == type.getNumberOfItems()) {
			return source;
		}
		else if (type.getNumberOfItems() == 1) {
			return new double[] {Math.sqrt(source[0]*source[0]+source[1]*source[1])};
		}
		else {
			return new double[] {source[0], 0.0};
		}
	}
	
	private static class Stack {
		private final Matrix[]		matrices;
		private final double[][]	values;
		private final boolean[]		temp;
		private int					current = -1;
		
		private Stack(final int depth) {
			this.matrices = new Matrix[depth];
			this.values = new double[depth][2];
			this.temp = new boolean[depth];
		}
		
		void pushMatrix(final Matrix matrix, final boolean temporary) {
			current++;
			matrices[current] = matrix;
			temp[current] = temporary;
			values[current] = null;
		}
		
		void pushValue(final double value) {
			pushValue(new double[] {value, 0});
		}

		void pushValue(final double[] value) {
			current++;
			matrices[current] = null;
			temp[current] = false;
			values[current] = value.clone();
		}
		
		boolean isValue() {
			return matrices[current] == null;
		}
		
		boolean isTemporary() {
			return temp[current];
		}
		
		Matrix popMatrix(final BiConsumer<Boolean, Matrix> callback) {
			callback.accept(Boolean.valueOf(isTemporary()), matrices[current]);
			return matrices[current--];
		}
		
		double[] popValue() {
			return values[current--];
		}

		@Override
		public String toString() {
			return "Stack [matrices=" + Arrays.toString(matrices) + ", values=" + Arrays.toString(values) + ", temp=" + Arrays.toString(temp) + ", current=" + current + "]";
		}
	}
}