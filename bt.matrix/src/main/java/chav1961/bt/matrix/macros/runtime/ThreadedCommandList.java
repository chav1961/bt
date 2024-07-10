package chav1961.bt.matrix.macros.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import chav1961.bt.matrix.macros.runtime.CommandList.CommandType;
import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntime;
import chav1961.bt.matrix.macros.runtime.interfaces.MacrosRuntimeCall;
import chav1961.bt.matrix.macros.runtime.interfaces.ThreadedCommandRepo;
import chav1961.bt.matrix.macros.runtime.interfaces.Value;
import chav1961.bt.matrix.macros.runtime.interfaces.Value.ValueType;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;

public class ThreadedCommandList implements ThreadedCommandRepo {
	private static final Object[]		EMPTY = new Object[0];
	
	private final List<LongThrowableFunction<MacrosRuntime>>	commands = new ArrayList<>();

	@FunctionalInterface
	private static interface LongThrowableFunction<T> {
		long apply(T value) throws CalculationException, ContentException;
	}

	@Override
	public void addCommand(final CommandType type) {
		addCommand(type, EMPTY);
	}

	@Override
	public void addCommand(final CommandType type, final Object... parameters) {
		// TODO Auto-generated method stub
		if (type == null) {
			throw new NullPointerException("Command type to add can't be null");
		}
		else {
			switch (type) {
				case ADD			:
					break;
				case AND			:
					commands.add((rt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						if (value1.getType() == ValueType.BOOLEAN && value2.getType() == ValueType.BOOLEAN) {
							rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(boolean.class) && value2.getValue(boolean.class)));
						}
						else {
							throw new CalculationException();
						}
						return 1;
					});
					break;
				case BREAK			:
					break;
				case CALL			:
					checkTypes(parameters, MacrosRuntimeCall.class, int.class);
					final MacrosRuntimeCall	mrc = (MacrosRuntimeCall)parameters[0];
					final int				count = ((Integer)parameters[1]).intValue();
					
					commands.add((rt)->{
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
					
					commands.add((rt)->{
						rt.getProgramStack().pushStackValue(booleanValue);
						return 1;
					});
					break;
				case CONST_CHAR		:
					checkTypes(parameters, char[].class);
					final Value	charValue = Value.Factory.newReadOnlyInstance((char[])parameters[0]);
					
					commands.add((rt)->{
						rt.getProgramStack().pushStackValue(charValue);
						return 1;
					});
					break;
				case CONST_INT		:
					checkTypes(parameters, long.class);
					final Value	intValue = Value.Factory.newReadOnlyInstance(((Number)parameters[0]).longValue());
					
					commands.add((rt)->{
						rt.getProgramStack().pushStackValue(intValue);
						return 1;
					});
					break;
				case CONST_REAL		:
					checkTypes(parameters, double.class);
					final Value	realValue = Value.Factory.newReadOnlyInstance(((Number)parameters[0]).doubleValue()); 
					
					commands.add((rt)->{
						rt.getProgramStack().pushStackValue(realValue);
						return 1;
					});
					break;
				case CONTINUE		:
					break;
				case DIV			:
					commands.add((rt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						if (value1.getType().isNumber() && value2.getType().isNumber() && !value1.getType().isArray() && !value2.getType().isArray()) {
							if (value1.getType() == ValueType.INT && value2.getType() == ValueType.INT) {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(long.class) / value2.getValue(long.class)));
							}
							else {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(double.class) / value2.getValue(double.class)));
							}
						}
						else {
							throw new CalculationException();
						}
						return 1;
					});
					break;
				case DUPLICATE		:
					commands.add((rt)->{
						try {
							rt.getProgramStack().pushStackValue((Value)rt.getProgramStack().getStackValue().clone());
							return 1;
						} catch (CloneNotSupportedException e) {
							throw new CalculationException(e);
						}
					});
					break;
				case EQ				:
					commands.add((rt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(Objects.equals(value1, value2)));
						return 1;
					});
					break;
				case ERROR			:
					break;
				case GE				:
					commands.add((rt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(compareTo(value1, value2) >= 0));
						return 1;
					});
					break;
				case GT				:
					commands.add((rt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(compareTo(value1, value2) > 0));
						return 1;
					});
					break;
				case JUMP			:
					checkTypes(parameters, Number.class);
					final int	jumpLabel = ((Number)parameters[0]).intValue();  
							
					commands.add((rt)->{
						return decodeJump(jumpLabel);
					});
					break;
				case JUMP_FALSE		:
					checkTypes(parameters, Number.class);
					final int	jumpLabelF = ((Number)parameters[0]).intValue();  
							
					commands.add((rt)->{
						if (!Objects.equals(Value.Factory.TRUE, rt.getProgramStack().popStackValue())) {
							return decodeJump(jumpLabelF);
						}
						else {
							return 1;
						}
					});
					break;
				case JUMP_TRUE		:
					checkTypes(parameters, Number.class);
					final int	jumpLabelT = ((Number)parameters[0]).intValue();  
							
					commands.add((rt)->{
						if (Objects.equals(Value.Factory.TRUE, rt.getProgramStack().popStackValue())) {
							return decodeJump(jumpLabelT);
						}
						else {
							return 1;
						}
					});
					break;
				case LE				:
					commands.add((rt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(compareTo(value1, value2) <= 0));
						return 1;
					});
					break;
				case LOAD_INDEX		:
					break;
				case LOAD_VAR		:
					checkTypes(parameters, Number.class);
					final int	varNameId = ((Number)parameters[0]).intValue();  
							
					commands.add((rt)->{
						rt.getProgramStack().pushStackValue(rt.getProgramStack().getVarValue(varNameId));
						return 1;
					});
					break;
				case LT				:
					commands.add((rt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(compareTo(value1, value2) < 0));
						return 1;
					});
					break;
				case MOD			:
					commands.add((rt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						if (value1.getType().isNumber() && value2.getType().isNumber() && !value1.getType().isArray() && !value2.getType().isArray()) {
							if (value1.getType() == ValueType.INT && value2.getType() == ValueType.INT) {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(long.class) % value2.getValue(long.class)));
							}
							else {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(double.class) % value2.getValue(double.class)));
							}
						}
						else {
							throw new CalculationException();
						}
						return 1;
					});
					break;
				case MUL			:
					commands.add((rt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						if (value1.getType().isNumber() && value2.getType().isNumber() && !value1.getType().isArray() && !value2.getType().isArray()) {
							if (value1.getType() == ValueType.INT && value2.getType() == ValueType.INT) {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(long.class) * value2.getValue(long.class)));
							}
							else {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(double.class) * value2.getValue(double.class)));
							}
						}
						else {
							throw new CalculationException();
						}
						return 1;
					});
					break;
				case NE				:
					commands.add((rt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(!Objects.equals(value1, value2)));
						return 1;
					});
					break;
				case NEGATE			:
					commands.add((rt)->{
						final Value	val = rt.getProgramStack().getStackValue();
						
						if (val.getType().isNumber() && !val.getType().isArray()) {
							if (val.getType() == ValueType.INT) {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(val.getValue(long.class)));
							}
							else {
								rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(val.getValue(double.class)));
							}
							return 1;
						}
						else {
							throw new CalculationException();
						}
					});
					break;
				case NOT			:
					commands.add((rt)->{
						if (Objects.equals(rt.getProgramStack().getStackValue(), Value.Factory.TRUE)) {
							rt.getProgramStack().setStackValue(Value.Factory.FALSE);
						}
						else  {
							rt.getProgramStack().setStackValue(Value.Factory.TRUE);
						}
						return 1;
					});
					break;
				case OR				:
					commands.add((rt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						if (value1.getType() == ValueType.BOOLEAN && value2.getType() == ValueType.BOOLEAN) {
							rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(value1.getValue(boolean.class) || value2.getValue(boolean.class)));
						}
						else {
							throw new CalculationException();
						}
						return 1;
					});
					break;
				case POP			:
					commands.add((rt)->{
						rt.getProgramStack().popStackValue();
						return 1;
					});
					break;
				case PRINT			:
					break;
				case STORE_INDEX	:
					break;
				case STORE_VAR		:
					break;
				case SUB			:
					break;
				case WARNING		:
					break;
				default:
					throw new UnsupportedOperationException("Command type ["+type+"] is not supported yet"); 
			}
		}
	}

	private long decodeJump(int jumpLabel) {
		// TODO Auto-generated method stub
		return 0;
	}

	private int compareTo(Value value1, Value value2) {
		// TODO Auto-generated method stub
		return 0;
	}

	private void checkTypes(final Object[] parameters, final Class<?>... awaited) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerForwardLabel(int label) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerBackwardLabel(int label) {
		// TODO Auto-generated method stub
		
	}
	
}
