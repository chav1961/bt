package chav1961.bt.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_queue_properties;

import chav1961.bt.matrix.Matrix.Type;
import chav1961.bt.matrix.utils.OpenCLUtils;
import chav1961.bt.matrix.utils.ProgramDescriptor;
import chav1961.bt.matrix.utils.ProgramRepo;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

/*
 * <expr>::=<add>[{'+'|'-'}<add>...]
 * <add>::=<mul>[{'*'|'**'|'***'}<mul>...]
 * <mul>::=['-']<term>['^'<value>]
 * <term>::={<name>['.'{'T'|'inv'|'det'|'sp'}]|<value>|'('<expr>')'}
 * <name>::='%'<number>
 */


/*
 * 
 */


public class MatrixLib implements AutoCloseable {
	private static final char[]		SUFFIX_T = "t".toCharArray();
	private static final int		PREDEF_T = 0;
	private static final char[]		SUFFIX_INV = "inv".toCharArray();
	private static final int		PREDEF_INV = 1;
	private static final char[]		SUFFIX_DET = "det".toCharArray();
	private static final int		PREDEF_DET = 2;
	private static final char[]		SUFFIX_SP = "sp".toCharArray();
	private static final int		PREDEF_SP = 3;
	private static final float[]	PATTERN_FLOAT = new float[] {0.0f};
	private static final float[]	PATTERN_CFLOAT = new float[] {0.0f, 0.0f};
	private static final double[]	PATTERN_DOUBLE = new double[] {0.0};
	private static final double[]	PATTERN_CDOUBLE = new double[] {0.0, 0.0};
									
	private final cl_context 		context;
	private final cl_command_queue 	commandQueue;
	private final ProgramRepo		repo;
	
	private MatrixLib(final Type... typesSupported) {
		final cl_device_id			device[] = new cl_device_id[1]; 
		final cl_context			tempContext = OpenCLUtils.getContext(device);		
		
		  // Create a command queue for the selected device
		final cl_queue_properties	queueProperties = new cl_queue_properties();
		final cl_command_queue 		tempQueue = CL.clCreateCommandQueue(tempContext, device[0], 0L, null);
		this.context = tempContext;
		this.commandQueue = tempQueue;
		this.repo = new ProgramRepo(this.context , typesSupported);
	}
	
	@Override
	public void close() throws EnvironmentException {
		repo.close();
		CL.clReleaseCommandQueue(commandQueue);
		CL.clReleaseContext(context);
	}
	
	public boolean isTypeSupported(final Matrix.Type type) {
		return type == Type.REAL_FLOAT || type == Type.COMPLEX_FLOAT;
	}

	public Matrix getMatrix(final int rows, final int cols) {
		return getMatrix(Type.REAL_FLOAT, rows, cols);
	}	
	
	public Matrix getMatrix(final Matrix.Type type, final int rows, final int cols) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else if (!isTypeSupported(type)) {
			throw new IllegalArgumentException("Matrix type ["+type+"] is not supported with this library");
		}
		else if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0");
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of columns ["+cols+"] must be greater than 0");
		}
		else {
			final long				totalSize = 1L * rows * cols;
			final cl_mem			mem = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, totalSize * type.getItemSize() * type.getNumberOfItems(), null, null);

			return new Matrix(this, type, rows, cols, mem);
		}
	}

	public Matrix getZeroMatrix(final int rows, final int cols) {
		return getZeroMatrix(Matrix.Type.REAL_FLOAT, rows, cols);
	}
	
	public Matrix getZeroMatrix(final Matrix.Type type, final int rows, final int cols) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else if (!isTypeSupported(type)) {
			throw new IllegalArgumentException("Matrix type ["+type+"] is not supported with this library");
		}
		else if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0");
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of columns ["+cols+"] must be greater than 0");
		}
		else {
			final long				totalSize = 1L * rows * cols;
			final long				sizeInBytes = totalSize * type.getItemSize() * type.getNumberOfItems();
			final cl_mem			mem = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, sizeInBytes, null, null);

		    switch (type) {
				case COMPLEX_DOUBLE	:
				    CL.clEnqueueFillBuffer(commandQueue, mem, Pointer.to(PATTERN_CDOUBLE), Sizeof.cl_double2, 0,  sizeInBytes, 0, null, null);
					break;
				case COMPLEX_FLOAT	:
				    CL.clEnqueueFillBuffer(commandQueue, mem, Pointer.to(PATTERN_CFLOAT), Sizeof.cl_float2, 0,  sizeInBytes, 0, null, null);
					break;
				case REAL_DOUBLE	:
				    CL.clEnqueueFillBuffer(commandQueue, mem, Pointer.to(PATTERN_DOUBLE), Sizeof.cl_double, 0,  sizeInBytes, 0, null, null);
					break;
				case REAL_FLOAT		:
				    CL.clEnqueueFillBuffer(commandQueue, mem, Pointer.to(PATTERN_FLOAT), Sizeof.cl_float, 0,  sizeInBytes, 0, null, null);
					break;
				default:
					break;
		    }
			CL.clFinish(commandQueue);
	
			return new Matrix(this, type, rows, cols, mem);
		}
	}

	public Matrix getIdentityMatrix(final int rows, final int cols) {
		return getIdentityMatrix(Matrix.Type.REAL_FLOAT, rows, cols);
	}	
	
	public Matrix getIdentityMatrix(final Matrix.Type type, final int rows, final int cols) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else if (!isTypeSupported(type)) {
			throw new IllegalArgumentException("Matrix type ["+type+"] is not supported with this library");
		}
		else if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0");
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of columns ["+cols+"] must be greater than 0");
		}
		else {
			final ProgramDescriptor	desc = repo.getProgram(type, ProgramRepo.PROGRAM_IDENTITY_NAME);
			final long				totalSize = 1L * rows * cols;
			final cl_mem			mem = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, totalSize * type.getItemSize() * type.getNumberOfItems(), null, null);
			final long 				global_work_size[] = new long[]{rows, cols};
		    final long 				local_work_size[] = new long[]{1, 1};

		    // Set arguments
			CL.clSetKernelArg(desc.kernel, 0, Sizeof.cl_int, Pointer.to(new int[] {rows}));
			CL.clSetKernelArg(desc.kernel, 1, Sizeof.cl_mem, Pointer.to(mem));
	        // Execute the kernel
			CL.clEnqueueNDRangeKernel(commandQueue, desc.kernel, 2, null, global_work_size, local_work_size, 0, null, null);
			CL.clFinish(commandQueue);
			
			return new Matrix(this, type, rows, cols, mem);
		}
	}
	
	public Matrix calculate(final String expression, final Matrix... operands) throws SyntaxException, CalculationException {
		if (Utils.checkEmptyOrNullString(expression)) {
			throw new IllegalArgumentException("Expression string cam't be null or empty");
		}
		else if (operands == null || Utils.checkArrayContent4Nulls(operands) >= 0) {
			throw new IllegalArgumentException("Operands list is null or contains nulls inside");
		}
		else {
			try(final Calculator	calc = compile(CharUtils.terminateAndConvert2CharArray(expression, (char)0))) {
				
				return calc.calculate(operands);
			}
		}
	}

	public static Calculator compile(final String expression) throws SyntaxException {
		if (Utils.checkEmptyOrNullString(expression)) {
			throw new IllegalArgumentException("Expression string cam't be null or empty");
		}
		else {
			return compile(CharUtils.terminateAndConvert2CharArray(expression, (char)0));
		}
	}

	public static MatrixLib getInstance(final Type... typesSupported) {
		return new MatrixLib(typesSupported == null || typesSupported.length == 0 ? new Type[] {Type.REAL_FLOAT} :  typesSupported);
	}

	ProgramDescriptor getProgramDescriptor(final Type type, final String programName) {
		return repo.getProgram(type, programName);
	}
	
	cl_context getContext() {
		return context;
	}
	
	cl_command_queue getCommandQueue() {
		return commandQueue;
	}

	static Calculator compile(final char[] expression) throws SyntaxException {
		final Lexema[]	parsed = parse(expression);
		final SyntaxNode<Operation, SyntaxNode<?,?>>	root = new SyntaxNode<>(0, 0, Operation.UNKNOWN, 0, null);
		final int		theEnd = buildTree(OperType.ADD, parsed, 0, root); 

		if (parsed[theEnd].type != LexType.EOF) {
			throw new SyntaxException(0, parsed[theEnd].pos, "Unparsed tail");
		}
		else {
			final List<Command>	temp = new ArrayList<>();
			
			buildCommands(root, temp);
			final Command[]		commands = temp.toArray(new Command[temp.size()]);
			int		depth = 0, maxDepth = 0;
			
			for(Command item : commands) {
				depth += item.op.getStackDelta();
				maxDepth = Math.max(maxDepth, depth);						
			}
			return new Calculator(maxDepth, commands); 
		}
	}
	
	static Lexema[] parse(final char[] source) throws SyntaxException {
		final double[]		forValues = new double[1];
		final List<Lexema>	result =  new ArrayList<>();
		int		from = 0;
		
loop:	for (;;) {
			while (source[from] <= ' ' && source[from] != '\0') {
				from++;
			}
			switch (source[from]) {
				case '\0' :
					result.add(new Lexema(from, LexType.EOF));
					break loop;
				case '(' :
					result.add(new Lexema(from++, LexType.OPEN));
					break;
				case ')' :
					result.add(new Lexema(from++, LexType.CLOSE));
					break;
				case '+' :
					result.add(new Lexema(from++, LexType.PLUS));
					break;
				case '-' :
					result.add(new Lexema(from++, LexType.MINUS));
					break;
				case '.' :
					result.add(new Lexema(from++, LexType.DOT));
					break;
				case '^' :
					result.add(new Lexema(from++, LexType.POWER));
					break;
				case '*' :
					if (source[from + 1] == '*') {
						if (source[from + 2] == '*') {
							result.add(new Lexema(from, LexType.MUL_K));
							from += 3;
						}
						else {
							result.add(new Lexema(from, LexType.MUL_H));
							from += 2;
						}
					}
					else {
						result.add(new Lexema(from++, LexType.MUL));
					}
					break;
				case '%' :
					int		value = 0;
					
					while (source[++from] >= '0' && source[from] <= '9') {
						value = 10 * value + source[from] - '0';
					}
					result.add(new Lexema(from, LexType.NAME, value));
					break;
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					from = CharUtils.parseDouble(source, from, forValues, false);
					while (source[from] <= ' ' && source[from] != '\0') {
						from++;
					}
					if (source[from] == 'i' || source[from] == 'i') {
						result.add(new Lexema(from, LexType.IMAGE_VALUE, Double.doubleToLongBits(forValues[0])));
						from++;
					}
					else {
						result.add(new Lexema(from, LexType.REAL_VALUE, Double.doubleToLongBits(forValues[0])));
					}
					break;
				default :
					if (Character.isJavaIdentifierStart(source[from])) {
						if (CharUtils.compareIgnoreCase(source, from, SUFFIX_T)) {
							result.add(new Lexema(from, LexType.PREDEFINED, PREDEF_T));
							from += SUFFIX_T.length;
						}
						else if (CharUtils.compareIgnoreCase(source, from, SUFFIX_INV)) {
							result.add(new Lexema(from, LexType.PREDEFINED, PREDEF_INV));
							from += SUFFIX_INV.length;
						}
						else if (CharUtils.compareIgnoreCase(source, from, SUFFIX_DET)) {
							result.add(new Lexema(from, LexType.PREDEFINED, PREDEF_DET));
							from += SUFFIX_DET.length;
						}
						else if (CharUtils.compareIgnoreCase(source, from, SUFFIX_SP)) {
							result.add(new Lexema(from, LexType.PREDEFINED, PREDEF_SP));
							from += SUFFIX_SP.length;
						}
						else {
							throw new SyntaxException(0, from, "Unknown lexema");
						}
					}
					else {
						throw new SyntaxException(0, from, "Unknown lexema");
					}
			}
		}
		return result.toArray(new Lexema[result.size()]);
	}

	static int buildTree(final OperType type, final Lexema[] source, int from, final SyntaxNode<Operation, SyntaxNode<?, ?>> node) throws SyntaxException {
		// TODO Auto-generated method stub
		switch (type) {
			case ADD	:
				from = buildTree(OperType.MUL, source, from, node);
				if (source[from].type == LexType.PLUS || source[from].type == LexType.MINUS) {
					final List<SyntaxNode<Operation, SyntaxNode<?, ?>>>	list = new ArrayList<>();
					final List<LexType>	opers = new ArrayList<>();
					
					list.add((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.clone());
					do {
						final SyntaxNode<Operation, SyntaxNode<?, ?>>	right = (SyntaxNode<Operation, SyntaxNode<?, ?>>) node.clone();
						opers.add(source[from].type);
						
						from = buildTree(OperType.MUL, source, from + 1, right);
						list.add(right);
					} while (source[from].type == LexType.PLUS || source[from].type == LexType.MINUS);
					node.type = Operation.ADD;
					node.cargo = opers.toArray(new LexType[opers.size()]);
					node.children = list.toArray(new SyntaxNode[list.size()]);
				}
				break;
			case MUL	:
				from = buildTree(OperType.UNARY, source, from, node);
				if (source[from].type == LexType.MUL || source[from].type == LexType.MUL_H || source[from].type == LexType.MUL_K) {
					final List<SyntaxNode<Operation, SyntaxNode<?, ?>>>	list = new ArrayList<>();
					final List<LexType>	opers = new ArrayList<>();
					
					list.add((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.clone());
					do {
						final SyntaxNode<Operation, SyntaxNode<?, ?>>	right = (SyntaxNode<Operation, SyntaxNode<?, ?>>) node.clone();
						opers.add(source[from].type);
						
						from = buildTree(OperType.UNARY, source, from + 1, right);
						list.add(right);
					} while (source[from].type == LexType.MUL || source[from].type == LexType.MUL_H || source[from].type == LexType.MUL_K);
					node.type = Operation.MUL;
					node.cargo = opers.toArray(new LexType[opers.size()]);
					node.children = list.toArray(new SyntaxNode[list.size()]);
				}
				break;
			case UNARY	:
				if (source[from].type == LexType.PLUS) {
					from = buildTree(OperType.TERM, source, from + 1, node);
				}
				else if (source[from].type == LexType.MINUS) {
					final SyntaxNode<Operation, SyntaxNode<?, ?>>	child = (SyntaxNode<Operation, SyntaxNode<?, ?>>) node.clone();
					
					from = buildTree(OperType.TERM, source, from + 1, child);
					if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) child) == OperandType.VALUE) {
						node.type = Operation.MINUS;
					}
					else {
						node.type = Operation.NEGATE;
					}
					node.cargo = null;
					node.children = new SyntaxNode[] {child};
				}
				else {
					from = buildTree(OperType.TERM, source, from, node);
				}
				if (source[from].type == LexType.POWER) {
					if (source[from + 1].type == LexType.REAL_VALUE) {
						final SyntaxNode<Operation, SyntaxNode<?, ?>>	child = (SyntaxNode<Operation, SyntaxNode<?, ?>>) node.clone();
						
						node.type = Operation.POWER;
						node.cargo = null;
						node.value = source[from + 1].value;
						node.children = new SyntaxNode[] {child};
						from += 2;
					}
					else {
						throw new SyntaxException(0, source[from].pos, "Power (^) requires number at the second operand");
					}
				}
				break;
			case TERM	:
				switch (source[from].type) {
					case REAL_VALUE	:
						node.type = Operation.LOAD_REAL;
						node.value = source[from].value;
						from++;
						break;
					case NAME	:
						final long	index = source[from].value;
						
						if (source[from + 1].type == LexType.DOT) {
							if (source[from + 2].type == LexType.PREDEFINED) {
								final SyntaxNode<Operation, SyntaxNode<?, ?>>	child = (SyntaxNode<Operation, SyntaxNode<?, ?>>) node.clone();

								child.type = Operation.LOAD_MATRIX;
								child.value = index;
								switch ((int)source[from + 2].value) {
									case PREDEF_T	:
										node.type = Operation.TRANSPOSE;
										break;
									case PREDEF_INV	:
										node.type = Operation.INVERT;
										break;
									case PREDEF_DET	:
										node.type = Operation.DET;
										break;
									case PREDEF_SP	:
										node.type = Operation.SPOOR;
										break;
									default :
										throw new UnsupportedOperationException("Predefined code ["+source[from + 2].value+"] is not supported yet");
								}
								node.cargo = null;
								node.value = source[from + 1].value;
								node.children = new SyntaxNode[] {child};
								from += 3;
							}
							else {
								throw new SyntaxException(0, source[from].pos, "Missing predefined name");
							}
						}
						else {
							node.type = Operation.LOAD_MATRIX;
							node.value = index;
							from++;
						}
						break;
					case OPEN	:
						from = buildTree(OperType.ADD, source, from + 1, node);
						if (source[from].type == LexType.CLOSE) {
							from++;
						}
						else {
							throw new SyntaxException(0, source[from].pos, "Missing ')'");
						}
						break;
					default:
						throw new SyntaxException(0, source[from].pos, "Missing operand");
				}
				break;
			default :
				throw new UnsupportedOperationException("Oper type ["+type+"] is not supported yet"); 
		}
		return from;
	}

	static void buildCommands(final SyntaxNode<Operation, SyntaxNode<?, ?>> node, final List<Command> commands) throws SyntaxException {
		switch (node.type) {
			case ADD 		:
				final LexType[]	addOpers = (LexType[])node.cargo;
				
				if (calcOperandType(node) == OperandType.VALUE) {					
					for(int index = 0; index < node.children.length; index++) {
						buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[index], commands);
						if (index > 0) {
							if (addOpers[index-1] == LexType.PLUS) {
								commands.add(new Command(Operation.ADD_VAL, 0));
							}
							else if (addOpers[index-1] == LexType.MINUS) {
								commands.add(new Command(Operation.SUB_VAL, 0));
							}
							else {
								throw new UnsupportedOperationException("Lex type ["+addOpers[index-1]+"] is not supported yet");
							}
						}
					}
				}
				else {
					OperandType	current = calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]);
					
					buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
					for(int index = 1; index < node.children.length; index++) {
						OperandType	next = calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[index]);
						
						buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[index], commands);
						if (current == OperandType.VALUE && next == OperandType.VALUE) {
							commands.add(new Command(addOpers[index - 1] == LexType.PLUS ? Operation.ADD_VAL : Operation.SUB_VAL, 0));
						}
						else if (current == OperandType.VALUE && next == OperandType.MATRIX) {
							commands.add(new Command(addOpers[index - 1] == LexType.PLUS ? Operation.ADD_VAL_MATRIX : Operation.SUB_VAL_MATRIX, 0));
							current = OperandType.MATRIX;
						}
						else if (current == OperandType.MATRIX && next == OperandType.VALUE) {
							commands.add(new Command(addOpers[index - 1] == LexType.PLUS ? Operation.ADD_MATRIX_VAL : Operation.SUB_MATRIX_VAL, 0));
							current = OperandType.MATRIX;
						}
						else {
							commands.add(new Command(addOpers[index - 1] == LexType.PLUS ? Operation.ADD : Operation.SUB, 0));
						}
					}
				}
				break;
			case MUL 		:
				final LexType[]	mulOpers = (LexType[])node.cargo;
				
				if (calcOperandType(node) == OperandType.VALUE) {
					for(int index = 0; index < node.children.length; index++) {
						buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[index], commands);
						if (index > 0) {
							if (mulOpers[index-1] == LexType.MUL) {
								commands.add(new Command(Operation.MUL_VAL, 0));
							}
							else {
								throw new UnsupportedOperationException("Lex type ["+mulOpers[index-1]+"] is not supported yet");
							}
						}
					}
				}
				else {
					OperandType	current = calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]);
				
					buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
					for(int index = 1; index < node.children.length; index++) {
						OperandType	next = calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[index]);
						
						buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[index], commands);
						if (current == OperandType.VALUE && next == OperandType.VALUE) {
							if (mulOpers[index-1] == LexType.MUL) {
								commands.add(new Command(Operation.MUL_VAL, 0));
							}
							else {
								throw new SyntaxException(0, node.col, "This operation is applicable for matrices only");
							}
						}
						else if (current == OperandType.VALUE && next == OperandType.MATRIX) {
							if (mulOpers[index-1] == LexType.MUL) {
								commands.add(new Command(Operation.MUL_VAL_MATRIX, 0));
								current = OperandType.MATRIX;
							}
							else {
								throw new SyntaxException(0, node.col, "This operation is applicable for matrices only");
							}
						}
						else if (current == OperandType.MATRIX && next == OperandType.VALUE) {
							if (mulOpers[index-1] == LexType.MUL) {
								commands.add(new Command(Operation.MUL_MATRIX_VAL, 0));
								current = OperandType.MATRIX;
							}
							else {
								throw new SyntaxException(0, node.col, "This operation is applicable for matrices only");
							}
						}
						else {
							commands.add(new Command(mulOpers[index-1] == LexType.MUL ? Operation.MUL : (mulOpers[index-1] == LexType.MUL_H ? Operation.MUL_H : Operation.MUL_K), 0));
						}
					}
				}
				break;
			case DET		:
				buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					commands.add(new Command(Operation.DET, 0));
				}
				break;
			case INVERT		:
				buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					commands.add(new Command(Operation.INVERT, 0));
				}
				break;
			case LOAD_MATRIX:
				commands.add(new Command(Operation.LOAD_MATRIX, node.value));
				break;
			case LOAD_REAL	:
				commands.add(new Command(Operation.LOAD_REAL, node.value));
				break;
			case MINUS		:
				buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.VALUE) {
					commands.add(new Command(Operation.MINUS, 0));
				}
				else {
					throw new SyntaxException(0, node.col, "Unwaited operand type (value awaited)"); 
				}
				break;
			case NEGATE		:
				buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					commands.add(new Command(Operation.NEGATE, 0));
				}
				else {
					throw new SyntaxException(0, node.col, "Unwaited operand type (matrix awaited)"); 
				}
				break;
			case POWER		:
				buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					commands.add(new Command(Operation.POWER, node.value));
				}
				else {
					commands.add(new Command(Operation.POWER_VAL, node.value));
				}
				break;
			case SPOOR		:
				buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					commands.add(new Command(Operation.SPOOR, 0));
				}
				break;
			case ADD_VAL : case MUL_VAL : case SUB_VAL : case MUL_H : case MUL_K : case SUB :
				throw new SyntaxException(0, node.col, "Unwaited node type"); 
			case TRANSPOSE	:
				buildCommands((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0], commands);
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					commands.add(new Command(Operation.TRANSPOSE, 0));
				}
				break;
			case UNKNOWN	:
				throw new SyntaxException(0, node.col, "Unknown node type"); 
			default :
				throw new UnsupportedOperationException("Node type ["+node.type+"] is not supported yet");
		}
	}
	
	private static OperandType calcOperandType(final SyntaxNode<Operation, SyntaxNode<?, ?>> node) throws SyntaxException {
		switch (node.type) {
			case ADD : case MUL :
				for(SyntaxNode item : node.children) {
					if (calcOperandType(item) == OperandType.MATRIX) {
						return OperandType.MATRIX;
					}
				}
				return OperandType.VALUE;
			case DET		:
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					return OperandType.VALUE;
				}
				else {
					throw new SyntaxException(0, node.col, "Determinant doesn't have matrix parameters"); 
				}
			case INVERT		:
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					return OperandType.MATRIX;
				}
				else {
					throw new SyntaxException(0, node.col, "Inversion doesn't have matrix parameters"); 
				}
			case LOAD_MATRIX:
				return OperandType.MATRIX;
			case LOAD_REAL	:
				return OperandType.VALUE;
			case NEGATE		:
				return calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]);
			case POWER		:
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					return OperandType.MATRIX;
				}
				else {
					throw new SyntaxException(0, node.col, "Power doesn't have matrix parameters"); 
				}
			case SPOOR		:
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					return OperandType.VALUE;
				}
				else {
					throw new SyntaxException(0, node.col, "Spoor doesn't have matrix parameters"); 
				}
			case ADD_VAL : case MUL_VAL : case SUB_VAL : case MUL_H : case MUL_K : case SUB : case MINUS : case POWER_VAL :
				throw new SyntaxException(0, node.col, "Unwaited node type"); 
			case TRANSPOSE	:
				if (calcOperandType((SyntaxNode<Operation, SyntaxNode<?, ?>>) node.children[0]) == OperandType.MATRIX) {
					return OperandType.MATRIX;
				}
				else {
					throw new SyntaxException(0, node.col, "Transpose doesn't have matrix parameters"); 
				}
			case UNKNOWN	:
				throw new SyntaxException(0, node.col, "Unknown node type"); 
			default :
				throw new UnsupportedOperationException("Node type ["+node.type+"] is not supported yet");
		}
	}

	static enum LexType {
		EOF,
		OPEN,
		CLOSE,
		PLUS,
		MINUS,
		MUL,
		MUL_H,
		MUL_K,
		POWER,
		NAME,
		DOT,
		PREDEFINED,
		REAL_VALUE,
		IMAGE_VALUE
	}
	
	static enum OperType {
		ADD,
		MUL,
		UNARY,
		TERM
	}

	static enum Operation {
		LOAD_REAL(1),
		LOAD_IMAGE(1),
		LOAD_MATRIX(1),
		ADD(-1),
		ADD_VAL(-1),
		ADD_VAL_MATRIX(-1),
		ADD_MATRIX_VAL(-1),
		SUB(-1),
		SUB_VAL(-1),
		SUB_VAL_MATRIX(-1),
		SUB_MATRIX_VAL(-1),
		MUL(-1),
		MUL_VAL(-1),
		MUL_VAL_MATRIX(-1),
		MUL_MATRIX_VAL(-1),
		MUL_H(-1),
		MUL_K(-1),
		TRANSPOSE(0),
		INVERT(0),
		DET(0),
		SPOOR(0),
		NEGATE(0),
		MINUS(0),
		POWER(-1),
		POWER_VAL(-1),
		UNKNOWN(0);
		
		private final int	stackDelta;
		
		private Operation(final int stackDelta) {
			this.stackDelta = stackDelta;
		}
		
		public int getStackDelta() {
			return stackDelta;
		}
	}

	static enum OperandType {
		VALUE,
		MATRIX,
		UNKNOWN
	}
	
	static class Lexema {
		final int		pos;
		final LexType	type;
		final long		value;
		
		Lexema(final int pos, final LexType type, final long value) {
			this.pos = pos;
			this.type = type;
			this.value = value;
		}

		Lexema(final int pos, final LexType type) {
			this.pos = pos;
			this.type = type;
			this.value = 0;
		}
		
		@Override
		public String toString() {
			return "Lexema [pos=" + pos + ", type=" + type + ", value=" + value + "]";
		}
	}
	
	static class Command {
		final Operation	op;
		final long		operand;
		
		Command(Operation op, long operand) {
			this.op = op;
			this.operand = operand;
		}
	}
}
