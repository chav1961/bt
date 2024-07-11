package chav1961.bt.matrix.macros.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import chav1961.bt.matrix.macros.runtime.CommandList.CommandType;
import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;
import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntimeCall;
import chav1961.bt.matrix.macros.runtime.interfaces.ThreadedCommandRepo;
import chav1961.bt.matrix.macros.runtime.interfaces.Value;
import chav1961.bt.matrix.macros.runtime.interfaces.Value.ValueType;
import chav1961.bt.matrix.macros.runtime.interfaces.ValueArray;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.cdb.CompilerUtils;

public class ThreadedCommandList implements ThreadedCommandRepo {
	private static final Object[]		EMPTY = new Object[0];
	
	private final List<LongThrowableFunction>	commands = new ArrayList<>();

	private static interface InternalRuntime {
		int decodeJump(int label);
	}
	
	@FunctionalInterface
	private static interface LongThrowableFunction {
		int apply(MacrosRuntime rt, InternalRuntime irt) throws CalculationException, ContentException;
	}

	@Override
	public void reset() {
		commands.clear();
	}
	
	@Override
	public ThreadedCommandRepo addCommand(final CommandType type) {
		return addCommand(type, EMPTY);
	}

	@Override
	public ThreadedCommandRepo addCommand(final CommandType type, final Object... parameters) {
		if (type == null) {
			throw new NullPointerException("Command type to add can't be null");
		}
		else {
			switch (type) {
				case ADD			:
					commands.add((rt, irt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						if (value1.getType().isNumber() && value2.getType().isNumber() && !value1.getType().isArray() && !value2.getType().isArray()) {
							if (value1.getType() == ValueType.INT && value2.getType() == ValueType.INT) {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(long.class).longValue() + value2.getValue(long.class).longValue()));
							}
							else {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(double.class).doubleValue() + value2.getValue(double.class).doubleValue()));
							}
						}
						else if (value1.getType() == ValueType.STRING) {
							rt.getProgramStack().setStackValue(concat(RuntimeUtils.convert(value2, ValueType.STRING), value1));
						}
						else if (value2.getType() == ValueType.STRING) {
							rt.getProgramStack().setStackValue(concat(value2, RuntimeUtils.convert(value1, ValueType.STRING)));
						}
						else {
							throw new CalculationException("ADD operation for non-number values or CONCAT failed");
						}
						return 1;
					});
					break;
				case AND			:
					commands.add((rt, irt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						if (value1.getType() == ValueType.BOOLEAN && value2.getType() == ValueType.BOOLEAN) {
							rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(boolean.class) && value2.getValue(boolean.class)));
						}
						else {
							throw new CalculationException("AND operation for non-boolean values");
						}
						return 1;
					});
					break;
				case CALL			:
					checkTypes(parameters, MacrosRuntimeCall.class, int.class);
					final MacrosRuntimeCall	mrc = (MacrosRuntimeCall)parameters[0];
					final int				count = ((Integer)parameters[1]).intValue();
					
					commands.add((rt, irt)->{
						final Value[]	parm = new Value[count];
						
						for(int index = parm.length - 1; index > 0; index--) {
							parm[index] = rt.getProgramStack().popStackValue();
						}
						rt.getProgramStack().pushStackValue(mrc.process(rt, parm));
						return 1;
					});
					break;
				case CONST_BOOLEAN	:
					checkTypes(parameters, boolean.class);
					final Value	booleanValue = Value.Factory.newReadOnlyInstance(((Boolean)parameters[0]).booleanValue());
					
					commands.add((rt, irt)->{
						rt.getProgramStack().pushStackValue(booleanValue);
						return 1;
					});
					break;
				case CONST_CHAR		:
					if (parameters[0] instanceof CharSequence) {
						final Value	charValue = Value.Factory.newReadOnlyInstance((CharSequence)parameters[0]);
						
						commands.add((rt, irt)->{
							rt.getProgramStack().pushStackValue(charValue);
							return 1;
						});
					}
					else if (parameters[0] instanceof char[]) {
						final Value	charValue = Value.Factory.newReadOnlyInstance((char[])parameters[0]);
						
						commands.add((rt, irt)->{
							rt.getProgramStack().pushStackValue(charValue);
							return 1;
						});
					}
					else {
						throw new IllegalArgumentException("Parameter #[0] must have type ["+CharSequence.class.getCanonicalName()+"] or ["+char[].class.getCanonicalName()+"] but has type ["+parameters[0].getClass().getCanonicalName()+"]");
					}
					break;
				case CONST_INT		:
					checkTypes(parameters, Number.class);
					final Value	intValue = Value.Factory.newReadOnlyInstance(((Number)parameters[0]).longValue());
					
					commands.add((rt, irt)->{
						rt.getProgramStack().pushStackValue(intValue);
						return 1;
					});
					break;
				case CONST_REAL		:
					checkTypes(parameters, Number.class);
					final Value	realValue = Value.Factory.newReadOnlyInstance(((Number)parameters[0]).doubleValue()); 
					
					commands.add((rt, irt)->{
						rt.getProgramStack().pushStackValue(realValue);
						return 1;
					});
					break;
				case DIV			:
					commands.add((rt, irt)->{
						final Value	value2 = rt.getProgramStack().popStackValue(), value1 = rt.getProgramStack().getStackValue();   
								
						if (value1.getType().isNumber() && value2.getType().isNumber() && !value1.getType().isArray() && !value2.getType().isArray()) {
							if (value1.getType() == ValueType.INT && value2.getType() == ValueType.INT) {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(long.class).longValue() / value2.getValue(long.class).longValue()));
							}
							else {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(double.class).doubleValue() / value2.getValue(double.class).doubleValue()));
							}
						}
						else {
							throw new CalculationException("DIV operation for non-number values");
						}
						return 1;
					});
					break;
				case DUPLICATE		:
					commands.add((rt, irt)->{
						try {
							rt.getProgramStack().pushStackValue((Value)rt.getProgramStack().getStackValue().clone());
							return 1;
						} catch (CloneNotSupportedException e) {
							throw new CalculationException(e);
						}
					});
					break;
				case EQ				:
					commands.add((rt, irt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(Objects.equals(value1, value2)));
						return 1;
					});
					break;
				case GE				:
					commands.add((rt, irt)->{
						final Value	value2 = rt.getProgramStack().popStackValue(), value1 = rt.getProgramStack().getStackValue();   

						if (!value1.getType().isArray() && !value2.getType().isArray()) {
							rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(compareTo(value1, value2) >= 0));
							return 1;
						}
						else {
							throw new CalculationException("GE operation for arrays");
						}
					});
					break;
				case GT				:
					commands.add((rt, irt)->{
						final Value	value2 = rt.getProgramStack().popStackValue(), value1 = rt.getProgramStack().getStackValue();   
								
						if (!value1.getType().isArray() && !value2.getType().isArray()) {
							rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(compareTo(value1, value2) > 0));
							return 1;
						}
						else {
							throw new CalculationException("GT operation for arrays");
						}
					});
					break;
				case LE				:
					commands.add((rt, irt)->{
						final Value	value2 = rt.getProgramStack().popStackValue(), value1 = rt.getProgramStack().getStackValue();   
								
						if (!value1.getType().isArray() && !value2.getType().isArray()) {
							rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(compareTo(value1, value2) <= 0));
							return 1;
						}
						else {
							throw new CalculationException("LE operation for arrays");
						}
					});
					break;
				case LOAD_INDEX		:
					checkTypes(parameters, Number.class);
					final int	indexVarNameId = ((Number)parameters[0]).intValue();
					
					commands.add((rt, irt)->{
						final Value	varVal = rt.getProgramStack().getVarValue(indexVarNameId);
						final int	index = rt.getProgramStack().popStackValue().getValue(long.class).intValue();
						
						if (varVal.getType().isArray()) {
							final Value	indexValue;
							
							switch (varVal.getType().getComponentType()) {
								case BOOLEAN	:
									indexValue = Value.Factory.newReadOnlyInstance(((ValueArray)varVal).getValue(index, boolean.class).booleanValue());
									break;
								case INT		:
									indexValue = Value.Factory.newReadOnlyInstance(((ValueArray)varVal).getValue(index, long.class).longValue());
									break;
								case REAL		:
									indexValue = Value.Factory.newReadOnlyInstance(((ValueArray)varVal).getValue(index, double.class).doubleValue());
									break;
								case STRING		:
									indexValue = Value.Factory.newReadOnlyInstance(((ValueArray)varVal).getValue(index, char[].class));
									break;
								default :
									throw new UnsupportedOperationException("Component type ["+varVal.getType().getComponentType()+"] is not supported yet"); 
							}
							rt.getProgramStack().pushStackValue(indexValue);
						}
						else {
							throw new CalculationException("LOAD_INDEX for non-arrays");
						}
						return 1;
					});
					break;
				case LOAD_VAR		:
					checkTypes(parameters, Number.class);
					final int	varNameId = ((Number)parameters[0]).intValue();  
							
					commands.add((rt, irt)->{
						rt.getProgramStack().pushStackValue(rt.getProgramStack().getVarValue(varNameId));
						return 1;
					});
					break;
				case LT				:
					commands.add((rt, irt)->{
						final Value	value2 = rt.getProgramStack().popStackValue(), value1 = rt.getProgramStack().getStackValue();   
								
						if (!value1.getType().isArray() && !value2.getType().isArray()) {
							rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(compareTo(value1, value2) < 0));
							return 1;
						}
						else {
							throw new CalculationException("LT operation for arrays");
						}
					});
					break;
				case MOD			:
					commands.add((rt, irt)->{
						final Value	value2 = rt.getProgramStack().popStackValue(), value1 = rt.getProgramStack().getStackValue();   
								
						if (value1.getType().isNumber() && value2.getType().isNumber() && !value1.getType().isArray() && !value2.getType().isArray()) {
							if (value1.getType() == ValueType.INT && value2.getType() == ValueType.INT) {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(long.class).longValue() % value2.getValue(long.class).longValue()));
							}
							else {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(double.class).doubleValue() % value2.getValue(double.class).doubleValue()));
							}
						}
						else {
							throw new CalculationException("MOD operation for non-number values");
						}
						return 1;
					});
					break;
				case MUL			:
					commands.add((rt, irt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						if (value1.getType().isNumber() && value2.getType().isNumber() && !value1.getType().isArray() && !value2.getType().isArray()) {
							if (value1.getType() == ValueType.INT && value2.getType() == ValueType.INT) {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(long.class).longValue() * value2.getValue(long.class).longValue()));
							}
							else {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(double.class).doubleValue() * value2.getValue(double.class).doubleValue()));
							}
						}
						else {
							throw new CalculationException("MUL operation for non-number values");
						}
						return 1;
					});
					break;
				case NE				:
					commands.add((rt, irt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(!Objects.equals(value1, value2)));
						return 1;
					});
					break;
				case NEGATE			:
					commands.add((rt, irt)->{
						final Value	val = rt.getProgramStack().getStackValue();
						
						if (val.getType().isNumber() && !val.getType().isArray()) {
							if (val.getType() == ValueType.INT) {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(-val.getValue(long.class).longValue()));
							}
							else {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(-val.getValue(double.class).doubleValue()));
							}
							return 1;
						}
						else {
							throw new CalculationException("NEGATE operation for non-number values");
						}
					});
					break;
				case NOT			:
					commands.add((rt, irt)->{
						if (rt.getProgramStack().getStackValue().getType() != ValueType.BOOLEAN) {
							throw new CalculationException("NOT operation for non-boolean values");
						}
						else if (Objects.equals(rt.getProgramStack().getStackValue(), Value.Factory.TRUE)) {
							rt.getProgramStack().setStackValue(Value.Factory.FALSE);
						}
						else  {
							rt.getProgramStack().setStackValue(Value.Factory.TRUE);
						}
						return 1;
					});
					break;
				case OR				:
					commands.add((rt, irt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						if (value1.getType() == ValueType.BOOLEAN && value2.getType() == ValueType.BOOLEAN) {
							rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(boolean.class) || value2.getValue(boolean.class)));
						}
						else {
							throw new CalculationException("AND operation for non-boolean values");
						}
						return 1;
					});
					break;
				case POP			:
					commands.add((rt, irt)->{
						rt.getProgramStack().popStackValue();
						return 1;
					});
					break;
				case SUB			:
					commands.add((rt, irt)->{
						final Value	value2 = rt.getProgramStack().popStackValue(), value1 = rt.getProgramStack().getStackValue();   
								
						if (value1.getType().isNumber() && value2.getType().isNumber() && !value1.getType().isArray() && !value2.getType().isArray()) {
							if (value1.getType() == ValueType.INT && value2.getType() == ValueType.INT) {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(long.class).longValue() - value2.getValue(long.class).longValue()));
							}
							else {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(double.class).doubleValue() - value2.getValue(double.class).doubleValue()));
							}
						}
						else {
							throw new CalculationException("SUB operation for non-number values");
						}
						return 1;
					});
					break;
				case BREAK : case CONTINUE : case ERROR : case JUMP : case JUMP_FALSE : case JUMP_TRUE : case PRINT : case STORE_INDEX : case STORE_VAR : case WARNING :
					throw new IllegalArgumentException("Commadn type ["+type+"] can't be used in this instance");
				default:
					throw new UnsupportedOperationException("Command type ["+type+"] is not supported yet"); 
			}
			return this;
		}
	}

	@Override
	public CommandRepoExecutor build() {
		final LongThrowableFunction[]	cmds = commands.toArray(new LongThrowableFunction[commands.size()]);
		reset();
		
		return new CommandRepoExecutor() {
			@Override
			public void execute(final MacrosRuntime t) throws CalculationException, ContentException {
				int	address = 0;
				
				while (address >= 0 && address < cmds.length) {
					address += cmds[address].apply(t, null);
				}
			}
		};
	}

	private static Value concat(final Value left, final Value right) throws ContentException {
		final char[]	leftChar = left.getValue(char[].class);
		final char[]	rightChar = right.getValue(char[].class);
		final char[]	result = new char[leftChar.length + rightChar.length];
		
		System.arraycopy(leftChar, 0, result, 0, leftChar.length);
		System.arraycopy(rightChar, 0, result, leftChar.length, rightChar.length);
		return Value.Factory.newReadOnlyInstance(result);
	}
	
	private static int compareTo(final Value value1, final Value value2) throws CalculationException, ContentException {
		if (value1 == null && value2 == null || value1 == value2) {
			return 0;
		}
		else if (value1 == null || value2 == null) {
			throw new CalculationException("COMPARE operation for nulls");
		}
		else if (value1.getType() == value2.getType()) {
			return value1.compareTo(value2);
		}
		else if (value1.getType() == ValueType.STRING) {
				return compareTo(value1, RuntimeUtils.convert(value2, ValueType.STRING));
		}
		else if (value2.getType() == ValueType.STRING) {
			return compareTo(RuntimeUtils.convert(value1, ValueType.STRING), value2);
		}
		else if (value1.getType() == ValueType.INT && value1.getType() == ValueType.REAL) {
			return compareTo(RuntimeUtils.convert(value1, ValueType.REAL), value2);
		}
		else if (value1.getType() == ValueType.REAL && value1.getType() == ValueType.INT) {
			return compareTo(value1, RuntimeUtils.convert(value2, ValueType.REAL));
		}
		else {
			return compareTo(RuntimeUtils.convert(value1, ValueType.STRING), RuntimeUtils.convert(value2, ValueType.STRING));
		}
	}

	private void checkTypes(final Object[] parameters, final Class<?>... awaited) {
		if (parameters.length != awaited.length) {
			throw new IllegalArgumentException("Number of advanced parameters ["+parameters.length+"] is differ to awaited number of ones ["+awaited.length+"]");
		}
		else {
			for(int index = 0; index < parameters.length; index++) {
				if (parameters[index] == null) {
					throw new NullPointerException("Advanced parameter #["+index+"] is null");
				}
				else if (awaited[index].isPrimitive()) {
					final Class<?>	wrapper = CompilerUtils.toWrappedClass(awaited[index]);
					
					if (!wrapper.isInstance(parameters[index])) {
						throw new IllegalArgumentException("Parameter #["+index+"] must have type ["+wrapper.getCanonicalName()+"] but has type ["+parameters[index].getClass().getCanonicalName()+"]");
					}
				}
				else if (!awaited[index].isInstance(parameters[index])) {
					throw new IllegalArgumentException("Parameter #["+index+"] must have type ["+awaited[index].getCanonicalName()+"] but has type ["+parameters[index].getClass().getCanonicalName()+"]");
				}
			}
		}
	}
}
