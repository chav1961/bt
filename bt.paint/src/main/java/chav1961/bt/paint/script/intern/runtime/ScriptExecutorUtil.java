package chav1961.bt.paint.script.intern.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import chav1961.bt.paint.control.Predefines;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.OperatorTypes;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.SyntaxNodeType;
import chav1961.purelib.basic.SequenceIterator;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

class ScriptExecutorUtil {
	@FunctionalInterface
	static interface Callback {
		void process(SyntaxNode node) throws InterruptedException;
	}
	
	static <T> Object execute(final SyntaxNode node, final SyntaxTreeInterface<T> names, final Predefines predef, final int depth, final Callback callback) throws PaintScriptException, InterruptedException {
		callback.process(node);
		switch ((SyntaxNodeType)node.getType()) {
			case BREAK		:
				return new CallResult(CallResult.ResultType.BREAK, (int)node.cargo);
			case CALL		:
				break;
			case CASE		:
				final Object	forCase = calc((SyntaxNode)node.cargo, names, predef, callback);
				
				for (int index = 0; index < node.children.length; index += 2) {
					if (compare(forCase, calc(node.children[2*index], names, predef, callback)) == 0) {
						final CallResult 	result = (CallResult)execute(node.children[3], names, predef, depth + 1, callback);
						
						if (result.type.isReturnRequired() && result.level > 0) {
							result.level--;
							return result;
						}
						else {
							return new CallResult(CallResult.ResultType.ORDINAL);
						}
					}
				}
				return new CallResult(CallResult.ResultType.ORDINAL);
			case CASEDEF	:
				break;
			case CONTINUE	:
				return new CallResult(CallResult.ResultType.CONTINUE, (int)node.cargo);
			case FOR		:
				final Object	forVar = names.getCargo(node.value);
				
				for (Object item : buildIterable(node.children[0], node.children[1], node.children[2], names, predef, callback)) {
					// Assign
					final CallResult 	result = (CallResult)execute(node.children[3], names, predef, depth + 1, callback);
					
					if (result.type.isReturnRequired() && result.level > 0) {
						result.level--;
						return result;
					}
				}
				return new CallResult(CallResult.ResultType.ORDINAL);
			case FOR1		:
				final Object	for1Var = names.getCargo(node.value);
				
				for (Object item : buildIterable(node.children[0], node.children[1], names, predef, callback)) {
					// Assign
					final CallResult 	result = (CallResult)execute(node.children[2], names, predef, depth + 1, callback);
					
					if (result.type.isReturnRequired() && result.level > 0) {
						result.level--;
						return result;
					}
				}
				return new CallResult(CallResult.ResultType.ORDINAL);
			case FORALL		:
				final Object	forallVar = names.getCargo(node.value);
				
				for (Object item : buildSimpleIterable((SyntaxNode)node.cargo, names, predef, callback)) {
					// Assign
					final CallResult 	result = (CallResult)execute(node.children[0], names, predef, depth + 1, callback);
					
					if (result.type.isReturnRequired() && result.level > 0) {
						result.level--;
						return result;
					}
				}
				return new CallResult(CallResult.ResultType.ORDINAL);
			case IF			:
				if (convert(calc((SyntaxNode)node.cargo, names, predef, callback), boolean.class)) {
					final CallResult 	result = (CallResult)execute(node.children[0], names, predef, depth + 1, callback);
					
					if (result.type.isReturnRequired() && result.level > 0) {
						result.level--;
						return result;
					}
				}
				else if (node.children[1] != null) {
					final CallResult 	result = (CallResult)execute(node.children[1], names, predef, depth + 1, callback);
					
					if (result.type.isReturnRequired() && result.level > 0) {
						result.level--;
						return result;
					}
				}
				return new CallResult(CallResult.ResultType.ORDINAL);
			case RETURN		:
				return new CallResult(CallResult.ResultType.RETURN, depth);
			case RETURN1	:
				return new CallResult(CallResult.ResultType.RETURN, depth, calc((SyntaxNode)node.cargo, names, predef, callback));
			case SEQUENCE	:
				for (SyntaxNode item : node.children) {
					final CallResult 	result = (CallResult)execute(item, names, predef, depth, callback);
					
					if (result.type.isReturnRequired() && result.level > 0) {
						result.level--;
						return result;
					}
				}
				return new CallResult(CallResult.ResultType.ORDINAL);
			case UNTIL		:
				do {final CallResult 	result = (CallResult)execute(node.children[0], names, predef, depth + 1, callback);
				
					if (result.type.isReturnRequired() && result.level > 0) {
						result.level--;
						return result;
					}
				} while (convert(calc((SyntaxNode)node.cargo, names, predef, callback), boolean.class));
				
				return new CallResult(CallResult.ResultType.ORDINAL);
			case WHILE		:
				while (convert(calc((SyntaxNode)node.cargo, names, predef, callback), boolean.class)) {
					final CallResult 	result = (CallResult)execute(node.children[0], names, predef, depth + 1, callback);
					
					if (result.type.isReturnRequired() && result.level > 0) {
						result.level--;
						return result;
					}
				}
				
				return new CallResult(CallResult.ResultType.ORDINAL);
			default:
				throw new UnsupportedOperationException("Node type ["+node.getType()+"] is not supported yet"); 
		}
		return null;
	}

	static <T> Object calc(final SyntaxNode node, final SyntaxTreeInterface<T> names, final Predefines predef, final Callback callback) throws PaintScriptException, InterruptedException {
		callback.process(node);
		switch ((SyntaxNodeType)node.getType()) {
			case ACCESS			:
				break;
			case BINARY			:
				switch ((OperatorTypes)node.cargo) {
					case BIT_AND	:
						long	bitAndValue = Long.MIN_VALUE;
						
						for (SyntaxNode item : node.children) {
							final Number	current = convert(calc(item, names, predef, callback), Long.class);
							
							bitAndValue &= current.longValue();
						}
						return Long.valueOf(bitAndValue);
					case BIT_OR		:
						long	bitOrValue = 0;
						
						for (SyntaxNode item : node.children) {
							final Number	current = convert(calc(item, names, predef, callback), Long.class);
							
							bitOrValue |= current.longValue();
						}
						return Long.valueOf(bitOrValue);
					case BIT_XOR	:
						long	bitXOrValue = 0;
						
						for (SyntaxNode item : node.children) {
							final Number	current = convert(calc(item, names, predef, callback), Long.class);
							
							bitXOrValue ^= current.longValue();
						}
						return Long.valueOf(bitXOrValue);
					case BOOL_AND	:
						for (SyntaxNode item : node.children) {
							if (!convert(calc(item, names, predef, callback), boolean.class)) {
								return Boolean.valueOf(false);
							}
						}
						return Boolean.valueOf(true);
					case BOOL_OR	:
						for (SyntaxNode item : node.children) {
							if (convert(calc(item, names, predef, callback), boolean.class)) {
								return Boolean.valueOf(true);
							}
						}
						return Boolean.valueOf(false);
					case MUL		:
						break;
					case DIV		:
						break;
					case MOD		:
						break;
					case ADD		:
						break;
					case SUB		:
						break;
					default:
						break;
				}
				break;
			case CALL			:
				break;
			case CONSTANT		:
				return node.cargo;
			case STRONG_BINARY	:
				final Object	leftValue, rightValue;
				
				switch ((OperatorTypes)node.cargo) {
					case ASSIGNMENT	:
						rightValue = calc(node.children[1], names, predef, callback);
						leftValue = calc(node.children[0], names, predef, callback);
						return new CallResult(CallResult.ResultType.ORDINAL);
					case EQ			:
						leftValue = calc(node.children[0], names, predef, callback);
						rightValue = convert(calc(node.children[0], names, predef, callback), leftValue.getClass());
						return Boolean.valueOf(compare(leftValue, rightValue) == 0);
					case GE			:
						leftValue = calc(node.children[0], names, predef, callback);
						rightValue = convert(calc(node.children[0], names, predef, callback), leftValue.getClass());
						return Boolean.valueOf(compare(leftValue, rightValue) >= 0);
					case GT			:
						leftValue = calc(node.children[0], names, predef, callback);
						rightValue = convert(calc(node.children[0], names, predef, callback), leftValue.getClass());
						return Boolean.valueOf(compare(leftValue, rightValue) > 0);
					case LE			:
						leftValue = calc(node.children[0], names, predef, callback);
						rightValue = convert(calc(node.children[0], names, predef, callback), leftValue.getClass());
						return Boolean.valueOf(compare(leftValue, rightValue) <= 0);
					case LT			:
						leftValue = calc(node.children[0], names, predef, callback);
						rightValue = convert(calc(node.children[0], names, predef, callback), leftValue.getClass());
						return Boolean.valueOf(compare(leftValue, rightValue) < 0);
					case NE			:
						leftValue = calc(node.children[0], names, predef, callback);
						rightValue = convert(calc(node.children[0], names, predef, callback), leftValue.getClass());
						return Boolean.valueOf(compare(leftValue, rightValue) != 0);
					case IN			:
						leftValue = calc(node.children[0], names, predef, callback);
						for (Object item : buildSimpleIterable(node.children[1], names, predef, callback)) {
							if (compare(leftValue, convert(item, (leftValue.getClass()))) == 0) {
								return Boolean.valueOf(true);
							}
						}
						return Boolean.valueOf(false);
					default:
						throw new UnsupportedOperationException("Operator type ["+node.cargo+"] is not supported here"); 
				}
			case UNARY			:
				switch ((OperatorTypes)node.cargo) {
					case ADD		:
						return calc((SyntaxNode)node.cargo, names, predef, callback);
					case BIT_INV	:
						return Long.valueOf(~convert(calc((SyntaxNode)node.cargo, names, predef, callback), long.class));
					case BOOL_NOT	:
						return Boolean.valueOf(!convert(calc((SyntaxNode)node.cargo, names, predef, callback), boolean.class));
					case INC		:
						break;
					case DEC		:
						break;
					case SUB		:
						break;
					default:
						throw new UnsupportedOperationException("Operator type ["+node.cargo+"] is not supported here"); 
				}
				break;
			default:
				throw new UnsupportedOperationException("Node type ["+node.getType()+"] is not supported yet"); 
		}
		return null;
	}	

	private static <T> Iterable<Number> buildIterable(final SyntaxNode initial, final SyntaxNode terminal, final SyntaxTreeInterface<T> names, final Predefines predef, final Callback callback) throws PaintScriptException, InterruptedException {
		final int	initialValue = convert(calc(initial, names, predef, callback), int.class);
		final int	terminalValue = convert(calc(terminal, names, predef, callback), int.class);
		
		if (terminalValue < initialValue) {
			throw new PaintScriptException("For loop error: terminal < initial and step = 1"); 
		}
		else {
			return new Iterable<Number>() {
				@Override
				public Iterator<Number> iterator() {
					return new Iterator<Number>() {
						private int	current = initialValue;
	
						@Override
						public boolean hasNext() {
							return current <= terminalValue;
						}
	
						@Override
						public Integer next() {
							return Integer.valueOf(current++);
						}
					};
				}
			};
		}
	}

	private static <T> Iterable<Number> buildIterable(final SyntaxNode initial, final SyntaxNode terminal, final SyntaxNode step, final SyntaxTreeInterface<T> names, final Predefines predef, final Callback callback) throws PaintScriptException, InterruptedException {
		final int	initialValue = convert(calc(initial, names, predef, callback), int.class);
		final int	terminalValue = convert(calc(terminal, names, predef, callback), int.class);
		final int 	stepValue = convert(calc(step, names, predef, callback), int.class);

		if (terminalValue < initialValue && stepValue >= 0) {
			throw new PaintScriptException("For loop error: terminal < initial and step >= 0"); 
		}
		else if (terminalValue > initialValue && stepValue <= 0) {
			throw new PaintScriptException("For loop error: terminal > initial and step <= 0"); 
		}
		else {
			return new Iterable<Number>() {
				@Override
				public Iterator<Number> iterator() {
					return new Iterator<Number>() {
						private int	current = initialValue;
	
						@Override
						public boolean hasNext() {
							return current <= terminalValue;
						}
	
						@Override
						public Number next() {
							return Integer.valueOf(current++);
						}
					};
				}
			};
		}
	}
	
	private static <T> Iterable<Object> buildSimpleIterable(final SyntaxNode node, final SyntaxTreeInterface<T> names, final Predefines predef, final Callback callback) throws InterruptedException, PaintScriptException {
		final List<Object>	result = new ArrayList<>();

		callback.process(node);
		switch ((SyntaxNodeType)node.getType()) {
			case LIST			:
				for (SyntaxNode item : node.children) {
					result.add(calc(item, names, predef, callback));
				}
				break;
			default :
				result.add(calc(node, names, predef, callback));
		}
		return result;
	}

	private static <T> void buildRangedIterable(final SyntaxNode node, final SyntaxTreeInterface<T> names, final Predefines predef, final List<Number[]> result, final Callback callback) throws InterruptedException, PaintScriptException {
		callback.process(node);
		switch ((SyntaxNodeType)node.getType()) {
			case LIST			:
				for (SyntaxNode item : node.children) {
					final Number	listValue = convert(calc(item, names, predef, callback), Number.class);
					
					result.add(new Number[] {listValue, listValue});
				}
				break;
			case RANGE			:
				result.add(new Number[] {convert(calc(node.children[0], names, predef, callback), Number.class), convert(calc(node.children[1], names, predef, callback), Number.class)});
				break;
			default :
				final Number	value = convert(calc(node, names, predef, callback), Number.class);
				
				result.add(new Number[] {value, value});
				break;
		}
	}

	private static int compare(final Object left, final Object right) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	private static <T> T convert(final Object instance, final Class<T> awaited) {
		return null;
	}
	
	private static class CallResult {
		private static enum ResultType {
			ORDINAL(false), 
			BREAK(true), 
			CONTINUE(true), 
			RETURN(true);
			
			private final boolean	returnRequired;
			
			private ResultType(final boolean returnRequired) {
				this.returnRequired = returnRequired;
			}
			
			private boolean isReturnRequired() {
				return returnRequired;
			}
		}
		
		final ResultType	type;
		int					level;
		Object				value;

		public CallResult(final ResultType type) {
			this(type, 0);
		}

		public CallResult(final ResultType type, final int level) {
			this.type = type;
			this.level = level;
		}

		public CallResult(final ResultType type, final int level, final Object value) {
			this.type = type;
			this.level = level;
			this.value = value;
		}
	}
}
