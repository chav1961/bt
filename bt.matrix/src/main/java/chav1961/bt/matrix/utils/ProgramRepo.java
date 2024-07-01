package chav1961.bt.matrix.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jocl.cl_context;

import chav1961.bt.matrix.Matrix.Type;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;

public class ProgramRepo implements AutoCloseable {
	public static final String		PROGRAM_ZERO_NAME = "zeroMatrixKernel";
	public static final String		PROGRAM_IDENTITY_NAME = "identityMatrixKernel";
	public static final String		PROGRAM_ASSIGN_NAME = "assignMatrixKernel";
	public static final String		PROGRAM_ADD_NAME = "addMatrixKernel";
	public static final String		PROGRAM_SUBTRACT_NAME = "subtractMatrixKernel";
	public static final String		PROGRAM_ADD_SCALAR_NAME = "addScalarKernel";
	public static final String		PROGRAM_SUBTRACT_FROM_SCALAR_NAME = "subtractFromScalarKernel";
	public static final String		PROGRAM_MUL_NAME = "mulMatrixKernel";
	public static final String		PROGRAM_MUL_HADAMARD_NAME = "mulHadamardMatrixKernel";
	public static final String		PROGRAM_MUL_SCALAR_NAME = "mulScalarKernel";
	public static final String		PROGRAM_MUL_TENZOR_NAME = "mulKronekerMatrixKernel";
	public static final String		PROGRAM_TRANSPOSE_NAME = "transposeMatrixKernel";
	public static final String		PROGRAM_POWER_NAME = "powerMatrixKernel";
	public static final String		PROGRAM_TRACK_NAME = "trackMatrixKernel";
	public static final String		PROGRAM_DET_TRIANGLE_NAME = "detTriangleMatrixKernel";
	public static final String		PROGRAM_DET_REDUCE_NAME = "detReduceMatrixKernel";
	public static final String		PROGRAM_INV_DIVIDE1_NAME = "invDivide1MatrixKernel";
	public static final String		PROGRAM_INV_DIVIDE2_NAME = "invDivide2MatrixKernel";
	public static final String		PROGRAM_INV_SUBTRACT_NAME = "invSubtractMatrixKernel";
	
	private static final String		REAL_FLOAT_FILE = "realfloats.txt";
	private static final String		COMPLEX_FLOAT_FILE = "complexfloats.txt";
	private static final String		REAL_DOUBLE_FILE = "realdoubles.txt";
	private static final String		COMPLEX_DOUBLE_FILE = "complexdoubles.txt";
	private static final String[]	PROGRAMS = {
										PROGRAM_ZERO_NAME,
										PROGRAM_IDENTITY_NAME,
										PROGRAM_ASSIGN_NAME,
										PROGRAM_ADD_NAME,
										PROGRAM_SUBTRACT_NAME,
										PROGRAM_ADD_SCALAR_NAME,
										PROGRAM_SUBTRACT_FROM_SCALAR_NAME,
										PROGRAM_MUL_NAME,
										PROGRAM_MUL_HADAMARD_NAME,
										PROGRAM_MUL_SCALAR_NAME,
										PROGRAM_MUL_TENZOR_NAME,
										PROGRAM_TRANSPOSE_NAME,
										PROGRAM_POWER_NAME,
										PROGRAM_TRACK_NAME,
										PROGRAM_DET_TRIANGLE_NAME,
										PROGRAM_DET_REDUCE_NAME,
										PROGRAM_INV_DIVIDE1_NAME,
										PROGRAM_INV_DIVIDE2_NAME,
										PROGRAM_INV_SUBTRACT_NAME
									};

	private final Map<String, ProgramDescriptor[]>	programs = new HashMap<>();
	private final Type[]	reallySupported;
	
	public ProgramRepo(final cl_context context, final Type... typesSupported) throws EnvironmentException {
		final Set<Type>	reallySupportedTypes = new HashSet<>(); 
		final int		numberOfTypes = Type.values().length;

		for (String item : PROGRAMS) {
			programs.put(item, new ProgramDescriptor[numberOfTypes]);
		}
		
		for(Type item : typesSupported) {
			final ProgramItem[]	items;
			final String		fileName;
			
			try{
				switch (item) {
					case COMPLEX_DOUBLE	:
						items = ProgramItem.load(fileName = COMPLEX_DOUBLE_FILE);
						break;
					case COMPLEX_FLOAT	:
						items = ProgramItem.load(fileName = COMPLEX_FLOAT_FILE);
						break;
					case REAL_DOUBLE	:
						items = ProgramItem.load(fileName = REAL_DOUBLE_FILE);
						break;
					case REAL_FLOAT		:
						items = ProgramItem.load(fileName = REAL_FLOAT_FILE);
						break;
					default :
						throw new UnsupportedOperationException("Matrix type ["+item+"] is not supported yet"); 
				}
				compile(context, fileName, item.getProgramSuffix(), programs, items, item.ordinal());
				reallySupportedTypes.add(item);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.reallySupported = reallySupportedTypes.toArray(new Type[reallySupportedTypes.size()]);
	}

	@Override
	public void close() throws EnvironmentException {
		for (Entry<String, ProgramDescriptor[]> item : programs.entrySet()) {
			for(ProgramDescriptor program : item.getValue()) {
				if (program != null) {
					program.close();
				}
			}
		}
	}
	
	public Type[] getSupportedTypes() {
		return reallySupported;
	}
	
	public boolean isTypeSupported(final Type type) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else {
			for(Type item : getSupportedTypes()) {
				if (item == type) {
					return true;
				}
			}
			return false;
		}
	}

	public ProgramDescriptor getProgram(final String programName) {
		return getProgram(Type.REAL_FLOAT, programName);
	}	
	
	public ProgramDescriptor getProgram(final Type type, final String programName) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else if (!isTypeSupported(type)) {
			throw new IllegalArgumentException("Matrix type ["+type+"] is not supported in the repo");
		}
		else if (Utils.checkEmptyOrNullString(programName)) {
			throw new IllegalArgumentException("Program name can't be null or empty");
		}
		else if (!programs.containsKey(programName)) {
			throw new IllegalArgumentException("Program name ["+programName+"] is unknown in the repository");
		}
		else {
			switch (type) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT : case REAL_DOUBLE : case REAL_FLOAT :
					final ProgramDescriptor	desc = programs.get(programName)[type.ordinal()];
					
					if (desc == null) {
						throw new IllegalArgumentException("Program name ["+programName+"] for matrix type ["+type+"] is not supported or illegal");
					}
					else {
						return desc;
					}
				default:
					throw new UnsupportedOperationException("Matrix type ["+type+"] is not supported yet");
			}
		}
	}

	private static void compile(final cl_context context, final String fileName, final String suffix, final Map<String, ProgramDescriptor[]> programs, final ProgramItem[] items, final int ordinal) {
		for(ProgramItem item : items) {
			if (isNameSupported(item.programName)) {
				programs.get(item.programName)[ordinal] = new ProgramDescriptor(context, item.programName+suffix, item.programBody);
			}
			else {
				throw new EnvironmentException("File ["+fileName+"], program name ["+item.programName+"] is not known in the repo");
			}
		}
	}

	private static boolean isNameSupported(final String programName) {
		for(String item : PROGRAMS) {
			if (item.equals(programName)) {
				return true;
			}
		}
		return false;
	}
}
