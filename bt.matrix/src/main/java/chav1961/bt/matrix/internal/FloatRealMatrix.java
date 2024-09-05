package chav1961.bt.matrix.internal;

import java.util.Arrays;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.matrix.interfaces.Matrix;

public class FloatRealMatrix implements Matrix {
	private final int		rows;
	private final int		cols;
	private final float[]	content;
	private boolean			completed = true;

	public FloatRealMatrix(final int rows, final int columns) {
		if (rows <= 0) {
			throw new IllegalArgumentException("Rows ["+rows+"] must be greater than 0");
		}
		else if (columns <= 0) {
			throw new IllegalArgumentException("Columns ["+columns+"] must be greater than 0");
		}
		else {
			this.rows = rows;
			this.cols = columns;
			this.content = new float[rows * columns];
		}
	}
	
	@Override
	public void close() throws RuntimeException {
	}

	@Override
	public Type getType() {
		return Type.REAL_FLOAT;
	}

	@Override
	public int numberOfRows() {
		return rows;
	}

	@Override
	public int numberOfColumns() {
		return cols;
	}

	@Override
	public boolean deepEquals(final Matrix another) {
		if (another == this) {
			return true;
		}
		else if (another == null) {
			return false;
		}
		else if (another.getType() != this.getType() || this.numberOfRows() != another.numberOfRows() || this.numberOfColumns() != another.numberOfColumns()) {
			return false;
		}
		else {
			ensureCompleted();
			return Arrays.equals(content, another.extractFloats());
		}
	}

	@Override
	public int[] extractInts() {
		return extractInts(getTotalPiece());
	}

	@Override
	public int[] extractInts(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			final float[]	source = this.content;
			final int[]		result = new int[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = (int)source[(y0 + y)*numberOfColumns() + (x0 + x)];
				}
			}
			return result;
		}
	}

	@Override
	public long[] extractLongs() {
		return extractLongs(getTotalPiece());
	}

	@Override
	public long[] extractLongs(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			final float[]	source = this.content;
			final long[]	result = new long[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = (long)source[(y0 + y)*numberOfColumns() + (x0 + x)];
				}
			}
			return result;
		}
	}

	@Override
	public float[] extractFloats() {
		ensureCompleted();
		return content;
	}

	@Override
	public float[] extractFloats(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			final float[]	source = this.content;
			final float[]	result = new float[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = source[(y0 + y)*numberOfColumns() + (x0 + x)];
				}
			}
			return result;
		}
	}

	@Override
	public double[] extractDoubles() {
		return extractDoubles(getTotalPiece());
	}

	@Override
	public double[] extractDoubles(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			final float[]	source = this.content;
			final double[]	result = new double[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = (double)source[(y0 + y)*numberOfColumns() + (x0 + x)];
				}
			}
			return result;
		}
	}

	@Override
	public Matrix assign(final int... content) {
		return assign(getTotalPiece(), content);
	}

	@Override
	public Matrix assign(final Piece piece, final int... content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			final float[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[(y0 + y)*numberOfColumns() + (x0 + x)] = (float)content[where++];
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(final long... content) {
		return assign(getTotalPiece(), content);
	}

	@Override
	public Matrix assign(final Piece piece, final long... content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			final float[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[(y0 + y)*numberOfColumns() + (x0 + x)] = (float)content[where++];
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			ensureCompleted();
			System.arraycopy(content, 0, this.content, 0, Math.min(content.length, this.content.length));
			return this;
		}		
	}

	@Override
	public Matrix assign(final Piece piece, final float... content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			final float[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[(y0 + y)*numberOfColumns() + (x0 + x)] = content[where++];
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(final double... content) {
		return assign(getTotalPiece(), content);
	}

	@Override
	public Matrix assign(final Piece piece, final double... content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			final float[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[(y0 + y)*numberOfColumns() + (x0 + x)] = (float)content[where++];
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else if (content.getType() == this.getType()) {
			return assign(content.extractFloats());
		}
		else {
			return assign(getTotalPiece(), content);
		}
	}

	@Override
	public Matrix assign(final Piece piece, final Matrix content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			return assign(piece, content.extractFloats());
		}
	}

	@Override
	public Matrix fill(final int value) {
		return fill((float)value);
	}

	@Override
	public Matrix fill(final Piece piece, final int value) {
		return fill(piece, (float)value);
	}

	@Override
	public Matrix fill(final long value) {
		return fill((float)value);
	}

	@Override
	public Matrix fill(final Piece piece, final long value) {
		return fill(piece, (float)value);
	}

	@Override
	public Matrix fill(final float value) {
		Utils.fillArray(content, value);
		return this;
	}

	@Override
	public Matrix fill(final Piece piece, final float value) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			final float[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[(y0 + y)*numberOfColumns() + (x0 + x)] = value;
				}
			}
			return this;
		}
	}

	@Override
	public Matrix fill(final float real, final float image) {
		throw new UnsupportedOperationException("Complex assignment is not supported for real matrix");
	}

	@Override
	public Matrix fill(final Piece piece, final float real, final float image) {
		throw new UnsupportedOperationException("Complex assignment is not supported for real matrix");
	}

	@Override
	public Matrix fill(final double value) {
		return fill((float)value);
	}

	@Override
	public Matrix fill(final Piece piece, final double value) {
		return fill(piece, (float)value);
	}

	@Override
	public Matrix fill(final double real, final double image) {
		throw new UnsupportedOperationException("Complex assignment is not supported for real matrix");
	}

	@Override
	public Matrix fill(Piece piece, double real, double image) {
		throw new UnsupportedOperationException("Complex assignment is not supported for real matrix");
	}

	@Override
	public Matrix cast(final Type type) {
		if (type == null) {
			throw new NullPointerException("Cast type can't be null");
		}
		else if (type == this.getType()) {
			return this;
		}
		else {
			switch (type) {
				case COMPLEX_DOUBLE	:
					break;
				case COMPLEX_FLOAT	:
					break;
				case REAL_DOUBLE	:
					break;
				case REAL_FLOAT		:
					return this;
				case REAL_INT		:
					break;
				case REAL_LONG		:
					break;
				default:
					break;
			}
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Override
	public Matrix add(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] += content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix add(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] += content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix add(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] += content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix add(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] += content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix add(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to add real and complex matrices");
				case REAL_DOUBLE	:
					return add(content.extractDoubles());
				case REAL_FLOAT		:
					return add(content.extractFloats());
				case REAL_INT		:
					return add(content.extractInts());
				case REAL_LONG		:
					return add(content.extractLongs());
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix addValue(final int value) {
		return addValue((float)value);
	}

	@Override
	public Matrix addValue(final long value) {
		return addValue((float)value);
	}

	@Override
	public Matrix addValue(final float value) {
		final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] + value; 
		}
		return result;
	}

	@Override
	public Matrix addValue(float real, float image) {
		throw new UnsupportedOperationException("Complex addition is not supported for real matrix");
	}

	@Override
	public Matrix addValue(final double value) {
		return addValue((float)value);
	}

	@Override
	public Matrix addValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex addition is not supported for real matrix");
	}

	@Override
	public Matrix subtract(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] -= content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix subtract(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] -= content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix subtract(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] -= content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix subtract(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] -= content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix subtract(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to subtract real and complex matrices");
				case REAL_DOUBLE	:
					return subtract(content.extractDoubles());
				case REAL_FLOAT		:
					return subtract(content.extractFloats());
				case REAL_INT		:
					return subtract(content.extractInts());
				case REAL_LONG		:
					return subtract(content.extractLongs());
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix subtractValue(final int value) {
		return subtractValue((float)value);
	}

	@Override
	public Matrix subtractValue(final long value) {
		return subtractValue((float)value);
	}

	@Override
	public Matrix subtractValue(final float value) {
		final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] - value; 
		}
		return result;
	}

	@Override
	public Matrix subtractValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex subtraction is not supported for real matrix");
	}

	@Override
	public Matrix subtractValue(final double value) {
		return subtractValue((float)value);
	}

	@Override
	public Matrix subtractValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex subtraction is not supported for real matrix");
	}

	@Override
	public Matrix subtractFrom(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] - target[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix subtractFrom(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] - target[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix subtractFrom(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] - target[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix subtractFrom(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (float)(content[index] - target[index]); 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix subtractFrom(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to subtract real and complex matrices");
				case REAL_DOUBLE	:
					return subtractFrom(content.extractDoubles());
				case REAL_FLOAT		:
					return subtractFrom(content.extractFloats());
				case REAL_INT		:
					return subtractFrom(content.extractInts());
				case REAL_LONG		:
					return subtractFrom(content.extractLongs());
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix subtractFromValue(final int value) {
		return subtractFromValue((float)value);
	}

	@Override
	public Matrix subtractFromValue(final long value) {
		return subtractFromValue((float)value);
	}

	@Override
	public Matrix subtractFromValue(final float value) {
		final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = value - source[index]; 
		}
		return result;
	}

	@Override
	public Matrix subtractFromValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex subtraction is not supported for real matrix");
	}

	@Override
	public Matrix subtractFromValue(final double value) {
		return subtractFromValue((float)value);
	}

	@Override
	public Matrix subtractFromValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex subtraction is not supported for real matrix");
	}

	@Override
	public Matrix mul(Matrix content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulInv(Matrix content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulInvFrom(Matrix content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix mulValue(final int value) {
		return mulValue((float)value);
	}

	@Override
	public Matrix mulValue(final long value) {
		return mulValue((float)value);
	}

	@Override
	public Matrix mulValue(final float value) {
		final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] * value; 
		}
		return result;
	}

	@Override
	public Matrix mulValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex multiplication is not supported for real matrix");
	}

	@Override
	public Matrix mulValue(final double value) {
		return mulValue((float)value);
	}

	@Override
	public Matrix mulValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex multiplication is not supported for real matrix");
	}

	@Override
	public Matrix divValue(final int value) {
		return divValue((float)value);
	}

	@Override
	public Matrix divValue(final long value) {
		return divValue((float)value);
	}

	@Override
	public Matrix divValue(final float value) {
		final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		final float				inv = 1 / value;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] * inv; 
		}
		return result;
	}

	@Override
	public Matrix divValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex division is not supported for real matrix");
	}

	@Override
	public Matrix divValue(final double value) {
		return divValue((float)value);
	}

	@Override
	public Matrix divValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex division is not supported for real matrix");
	}

	@Override
	public Matrix divFromValue(final int value) {
		return divFromValue((float)value);
	}

	@Override
	public Matrix divFromValue(final long value) {
		return divFromValue((float)value);
	}

	@Override
	public Matrix divFromValue(final float value) {
		final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = value / source[index]; 
		}
		return result;
	}

	@Override
	public Matrix divFromValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex division is not supported for real matrix");
	}

	@Override
	public Matrix divFromValue(final double value) {
		return divFromValue((float)value);
	}

	@Override
	public Matrix divFromValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex division is not supported for real matrix");
	}

	@Override
	public Matrix mulHadamard(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] *= content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulHadamard(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] *= content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulHadamard(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] *= content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulHadamard(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] *= content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulHadamard(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE	:
					return mulHadamard(content.extractDoubles());
				case REAL_FLOAT		:
					return mulHadamard(content.extractFloats());
				case REAL_INT		:
					return mulHadamard(content.extractInts());
				case REAL_LONG		:
					return mulHadamard(content.extractLongs());
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix mulInvHadamard(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] /= content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulInvHadamard(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] /= content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulInvHadamard(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] /= content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulInvHadamard(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] /= content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulInvHadamard(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE	:
					return mulInvHadamard(content.extractDoubles());
				case REAL_FLOAT		:
					return mulInvHadamard(content.extractFloats());
				case REAL_INT		:
					return mulInvHadamard(content.extractInts());
				case REAL_LONG		:
					return mulInvHadamard(content.extractLongs());
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix mulInvFromHadamard(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] / target[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulInvFromHadamard(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] / target[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulInvFromHadamard(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] / target[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulInvFromHadamard(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			ensureCompleted();
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (float)(content[index] / target[index]); 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulInvFromHadamard(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE	:
					return mulInvHadamard(content.extractDoubles());
				case REAL_FLOAT		:
					return mulInvHadamard(content.extractFloats());
				case REAL_INT		:
					return mulInvHadamard(content.extractInts());
				case REAL_LONG		:
					return mulInvHadamard(content.extractLongs());
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix tensorMul(Matrix content) {
		if (content == null) {
			throw new NullPointerException("Matrix content can't be null");
		}
		else {
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new UnsupportedOperationException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE	:
					break;
				case REAL_FLOAT		:
					break;
				case REAL_INT		:
					break;
				case REAL_LONG		:
					break;
				default:
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix invert() {
		if (numberOfRows() != numberOfColumns()) {
			throw new IllegalStateException("Only square matrix can be inverted");
		}
		else {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Override
	public Matrix transpose() {
		final FloatRealMatrix	result = new FloatRealMatrix(numberOfColumns(), numberOfRows());
		final float[]			source = this.content;
		final float[]			target = result.content;
		final int				rows = numberOfRows(), cols = numberOfColumns();  
		
		for(int y = 0; y < rows; y++) {
			for(int x = 0; x < cols; x++) {
				target[x*rows + y] = source[y*cols + x]; 
			}
		}
		return result;
	}

	@Override
	public Matrix aggregate(AggregateDirection dir, AggregateType aggType) {
		if (dir == null) {
			throw new NullPointerException("Aggregate direction can't be null");
		}
		else if (aggType == null) {
			throw new NullPointerException("Aggregate type can't be null");
		}
		else {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Override
	public Number det() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number track() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toHumanReadableString() {
		final StringBuilder	sb = new StringBuilder();
		
		sb.append("Matrix: type=").append(getType()).append(", size=").append(numberOfRows()).append('x').append(numberOfColumns()).append(":\n");
		for(int y = 0; y < numberOfRows(); y++) {
			for(int x = 0; x < numberOfColumns(); x++) {
				sb.append(String.format(" %1$15e",content[y*numberOfColumns()+x]));
			}
		}
		return sb.toString();
	}

	@Override
	public Matrix done() {
		completed = true;
		return this;
	}

	private Piece getTotalPiece() {
		return Piece.of(0, 0, numberOfRows(), numberOfColumns());
	}
	
	private void ensureCompleted() {
		if (!completed) {
			throw new IllegalStateException("Matrix is not completed after previous operations. Call done() method before");
		}
	}
}
