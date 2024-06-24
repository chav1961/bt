package chav1961.bt.matrix;

import java.util.Arrays;
import java.util.function.BiConsumer;

import chav1961.bt.matrix.MatrixLib.Command;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;

public class Calculator implements AutoCloseable {
	private final int		stackDepth;
	private final Command[]	commands;
	
	private Calculator(final int stackDepth, final Command... commands) {
		this.stackDepth = stackDepth;
		this.commands = commands;
	}
	
	public Matrix calculate(final Matrix... operands) throws CalculationException {
		if (operands == null || Utils.checkArrayContent4Nulls(operands) >= 0) {
			throw new IllegalArgumentException("Operands are null or contain nulls inside");
		}
		else {
			final Stack		stack = new Stack(stackDepth);
			final Matrix[]	temporary = new Matrix[1];
			double			val;
			Matrix			mat;
			
			for(Command item : commands) {
				switch (item.op) {
					case ADD			:
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).add(mat), true);
						break;
					case ADD_MATRIX_VAL	:
						val = stack.popValue();
						
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).add(val), true);
						break;
					case ADD_VAL		:
						stack.pushValue(stack.popValue() + stack.popValue());
						break;
					case ADD_VAL_MATRIX	:
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).add(stack.popValue()), true);
						break;
					case DET			:
						stack.pushValue(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).det());
					case INVERT			:
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).inv(), true);
						break;
					case LOAD_MATRIX	:
						if (item.operand >= operands.length) {
							throw new CalculationException("Matrix name #"+item.operand+" is missing in the parameters");
						}
						else {
							stack.pushMatrix(operands[(int)item.operand], false);
						}
						break;
					case LOAD_VALUE		:
						stack.pushValue(item.operand);
						break;
					case MINUS			:
						stack.pushValue(-stack.popValue());
						break;
					case MUL			:
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).mul(mat), true);
						break;
					case MUL_H			:
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).mulH(mat), true);
						break;
					case MUL_K			:
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).mulK(mat), true);
						break;
					case MUL_MATRIX_VAL	:
						val = stack.popValue();
						
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).mul(val), true);
						break;
					case MUL_VAL		:
						stack.pushValue(stack.popValue() * stack.popValue());
						break;
					case MUL_VAL_MATRIX	:
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).mul(stack.popValue()), true);
						break;
					case NEGATE			:
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).mul(-1), true);
						break;
					case POWER			:
						break;
					case POWER_VAL		:
						val = stack.popValue();
						
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).power(val), true);
						break;
					case SPOOR			:
						stack.pushValue(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).track());
						break;
					case SUB			:
						mat = stack.popMatrix((f,m)->{if (f) temporary[0] = m;});
						
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).sub(mat), true);
						break;
					case SUB_MATRIX_VAL	:
						val = stack.popValue();
						
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).add(-val), true);
						break;
					case SUB_VAL		:
						stack.pushValue(- stack.popValue() + stack.popValue());
						break;
					case SUB_VAL_MATRIX	:
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).add(-stack.popValue()), true);
						break;
					case TRANSPOSE		:
						stack.pushMatrix(stack.popMatrix((f,m)->{if (f) temporary[0] = m;}).trans(), true);
						break;
					default:
						throw new UnsupportedOperationException("Operation ["+item.op+"] is not supported yet");
				}
				if (temporary[0] != null) {
					temporary[0].close();
					temporary[0] = null;
				}
			}
			return stack.popMatrix((f,m)->{});
		}
	}
	
	@Override
	public void close() throws RuntimeException {
	}

	@Override
	public String toString() {
		return "Calculator [commands=" + Arrays.toString(commands) + "]";
	}
	
	private static class Stack {
		private final Matrix[]	matrices;
		private final double[]	values;
		private final boolean[]	temp;
		private int				current = -1;
		
		private Stack(final int depth) {
			this.matrices = new Matrix[depth];
			this.values = new double[depth];
			this.temp = new boolean[depth];
		}
		
		void pushMatrix(final Matrix matrix, final boolean temporary) {
			current++;
			matrices[current] = matrix;
			temp[current] = temporary;
			values[current] = 0;
		}
		
		void pushValue(final long value) {
			pushValue(Double.longBitsToDouble(value));
		}

		void pushValue(final double value) {
			current++;
			matrices[current] = null;
			temp[current] = false;
			values[current] = value;
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
		
		double popValue() {
			return values[current--];
		}

		@Override
		public String toString() {
			return "Stack [matrices=" + Arrays.toString(matrices) + ", values=" + Arrays.toString(values) + ", temp=" + Arrays.toString(temp) + ", current=" + current + "]";
		}
	}
}