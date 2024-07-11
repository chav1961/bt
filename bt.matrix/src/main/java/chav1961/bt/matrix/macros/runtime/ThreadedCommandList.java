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
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.cdb.CompilerUtils;

public class ThreadedCommandList implements ThreadedCommandRepo {
	private static final Object[]		EMPTY = new Object[0];
	
	private final List<LongThrowableFunction>	commands = new ArrayList<>();
	private final List<int[]>			labels = new ArrayList<>();

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
		labels.clear();
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
					break;
				case AND			:
					commands.add((rt, irt)->{
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
				case CONTINUE		:
					break;
				case DIV			:
					commands.add((rt, irt)->{
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
				case ERROR			:
					break;
				case GE				:
					commands.add((rt, irt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(compareTo(value1, value2) >= 0));
						return 1;
					});
					break;
				case GT				:
					commands.add((rt, irt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(compareTo(value1, value2) > 0));
						return 1;
					});
					break;
				case JUMP			:
					checkTypes(parameters, Number.class);
					final int	jumpLabel = ((Number)parameters[0]).intValue();  
							
					commands.add((rt, irt)->{
						return irt.decodeJump(jumpLabel);
					});
					break;
				case JUMP_FALSE		:
					checkTypes(parameters, Number.class);
					final int	jumpLabelF = ((Number)parameters[0]).intValue();  
							
					commands.add((rt, irt)->{
						if (!Objects.equals(Value.Factory.TRUE, rt.getProgramStack().popStackValue())) {
							return irt.decodeJump(jumpLabelF);
						}
						else {
							return 1;
						}
					});
					break;
				case JUMP_TRUE		:
					checkTypes(parameters, Number.class);
					final int	jumpLabelT = ((Number)parameters[0]).intValue();  
							
					commands.add((rt, irt)->{
						if (Objects.equals(Value.Factory.TRUE, rt.getProgramStack().popStackValue())) {
							return irt.decodeJump(jumpLabelT);
						}
						else {
							return 1;
						}
					});
					break;
				case LE				:
					commands.add((rt, irt)->{
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
							
					commands.add((rt, irt)->{
						rt.getProgramStack().pushStackValue(rt.getProgramStack().getVarValue(varNameId));
						return 1;
					});
					break;
				case LT				:
					commands.add((rt, irt)->{
						final Value	value1 = rt.getProgramStack().popStackValue(), value2 = rt.getProgramStack().getStackValue();   
								
						rt.getProgramStack().setStackValue(Value.Factory.newReadOnlyInstance(compareTo(value1, value2) < 0));
						return 1;
					});
					break;
				case MOD			:
					commands.add((rt, irt)->{
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
					commands.add((rt, irt)->{
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
					commands.add((rt, irt)->{
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
					commands.add((rt, irt)->{
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
					commands.add((rt, irt)->{
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
			return this;
		}
	}

	@Override
	public void registerForwardLabel(final int label) {
		labels.add(new int[] {label, commands.size() - 1});
	}

	@Override
	public void registerBackwardLabel(final int label) {
		labels.add(new int[] {label, commands.size() - 1});
	}

	@Override
	public CommandRepoExecutor build() {
		final LongThrowableFunction[]	cmds = commands.toArray(new LongThrowableFunction[commands.size()]);
		final int[][]					lab = labels.toArray(new int[labels.size()][]);
		final InternalRuntime			irt = new InternalRuntime() {
											@Override
											public int decodeJump(final int label) {
										        int low = 0, high = lab.length - 1;

										        while (low <= high) {
										            int mid = (low + high) >>> 1;
										            int midVal = lab[mid][0];

										            if (midVal < label) {
										                low = mid + 1;
										            }
										            else if (midVal > label) {
										                high = mid - 1;
										            }
										            else {
										                return lab[mid][1];
										            }
										        }
										        throw new IllegalArgumentException("Label ["+label+"] not found");
											}
										};
		Arrays.sort(lab, (i1,i2)->i1[0]-i2[0]);

		reset();
		return new CommandRepoExecutor() {
			@Override
			public void execute(final MacrosRuntime t) throws CalculationException, ContentException {
				int	address = 0;
				
				while (address >= 0 && address < cmds.length) {
					address = cmds[address].apply(t, irt);
				}
			}
		};
	}
	
	private static int compareTo(Value value1, Value value2) {
		// TODO Auto-generated method stub
		return 0;
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
