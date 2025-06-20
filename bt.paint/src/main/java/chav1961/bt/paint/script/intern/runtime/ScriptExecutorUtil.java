package chav1961.bt.paint.script.intern.runtime;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import chav1961.bt.paint.control.Predefines;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.ContentWrapper;
import chav1961.bt.paint.script.interfaces.ImageType;
import chav1961.bt.paint.script.interfaces.StrokeWrapper;
import chav1961.bt.paint.script.interfaces.StrokeWrapper.LineCaps;
import chav1961.bt.paint.script.interfaces.StrokeWrapper.LineJoin;
import chav1961.bt.paint.script.interfaces.StrokeWrapper.LineStroke;
import chav1961.bt.paint.script.intern.interfaces.ExecuteScriptCallback;
import chav1961.bt.paint.script.intern.interfaces.PaintScriptListInterface;
import chav1961.bt.paint.script.intern.interfaces.PaintScriptMapInterface;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.ArrayDescriptor;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.ConstantDescriptor;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.DataTypes;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.EntityDescriptor;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.Lexema;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.OperatorPriorities;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.OperatorTypes;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.SyntaxNodeType;
import chav1961.purelib.basic.CSSUtils;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.SequenceIterator;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.sql.SQLUtils;

public class ScriptExecutorUtil {
	public static Object execute(final SyntaxNode node, final SyntaxTreeInterface<EntityDescriptor> names, final Predefines predef, final int depth, final int level) throws PaintScriptException, InterruptedException {
		return execute(node, names, predef, depth, level, (l,n)->{});
	}	

	public static Object execute(final SyntaxNode node, final SyntaxTreeInterface<EntityDescriptor> names, final Predefines predef, final int depth, final int level, final ExecuteScriptCallback callback) throws PaintScriptException, InterruptedException {
		final LocalStack	stack = new LocalStack(names, predef, callback);
		
		return execute(node, names, stack, predef, depth, level, callback);
	}	
	
	static Object execute(final SyntaxNode node, final SyntaxTreeInterface<EntityDescriptor> names, final LocalStack stack, final Predefines predef, final int depth, final int level, final ExecuteScriptCallback callback) throws PaintScriptException, InterruptedException {
		callback.process(level, node);
		switch ((SyntaxNodeType)node.getType()) {
			case BREAK		:
				return new CallResult(CallResult.ResultType.BREAK, (int)node.value);
			case CALL		:
				break;
			case CASE		:
				final Object	forCase = calc((SyntaxNode)node.cargo, names, stack, predef, level, callback);
				
				for (int index = 0; index < node.children.length; index += 2) {
					if (compare(forCase, calc(node.children[index], names, stack, predef, level, callback)) == 0) {
						final CallResult 	result = (CallResult)execute(node.children[index + 1], names, stack, predef, depth + 1, level, callback);
						
						if (result.type.isReturnRequired() && result.level > 0) {
							return result;
						}
						else {
							return new CallResult(CallResult.ResultType.ORDINAL);
						}
					}
				}
				return new CallResult(CallResult.ResultType.ORDINAL);
			case CASEDEF	:
				final Object	forCaseDef = calc((SyntaxNode)node.cargo, names, stack, predef, level, callback);
				
				for (int index = 0; index < node.children.length - 1; index += 2) {
					if (compare(forCaseDef, calc(node.children[index], names, stack, predef, level, callback)) == 0) {
						final CallResult 	result = (CallResult)execute(node.children[index + 1], names, stack, predef, depth + 1, level, callback);
						
						if (result.type.isReturnRequired() && result.level > 0) {
							return result;
						}
						else {
							return new CallResult(CallResult.ResultType.ORDINAL);
						}
					}
				}
				final CallResult 	resultDef = (CallResult)execute(node.children[node.children.length - 1], names, stack, predef, depth + 1, level, callback);
				
				if (resultDef.type.isReturnRequired() && resultDef.level > 0) {
					resultDef.level--;
					return resultDef;
				}
				else {
					return new CallResult(CallResult.ResultType.ORDINAL);
				}
			case CONTINUE	:
				return new CallResult(CallResult.ResultType.CONTINUE, (int)node.value);
			case FOR		:
				final EntityDescriptor	forVar = names.getCargo(node.value);
				
forLoop:		for (Object item : buildIterable(node.children[0], node.children[1], node.children[2], names, stack, predef, level, callback)) {
					Array.set(stack.getVar(forVar.id),0,item);

					final CallResult 	result = (CallResult)execute(node.children[3], names, stack, predef, depth + 1, level, callback);
					
					if (result.type.isReturnRequired() && result.level > 0) {
						result.level--;
						switch (result.type) {
							case BREAK : case RETURN :
								return result;
							case CONTINUE	:
								if (result.level > 0) {
									return result;
								}
								else {
									continue forLoop;
								}
							case ORDINAL	:
								break;
							default	:
								throw new UnsupportedOperationException("Result type ["+result.type+"] is not implemented yet"); 
						}
					}
				}
				return new CallResult(CallResult.ResultType.ORDINAL);
			case FOR1		:
				final EntityDescriptor	for1Var = names.getCargo(node.value);
				
for1Loop:		for (Object item : buildIterable(node.children[0], node.children[1], names, stack, predef, level, callback)) {
					Array.set(stack.getVar(for1Var.id),0,item);

					final CallResult 	result = (CallResult)execute(node.children[2], names, stack, predef, depth + 1, level, callback);
					
					if (result.type.isReturnRequired() && result.level > 0) {
						result.level--;
						switch (result.type) {
							case BREAK : case RETURN :
								return result;
							case CONTINUE	:
								if (result.level > 0) {
									return result;
								}
								else {
									continue for1Loop;
								}
							case ORDINAL	:
								break;
							default	:
								throw new UnsupportedOperationException("Result type ["+result.type+"] is not implemented yet"); 
						}
					}
				}
				return new CallResult(CallResult.ResultType.ORDINAL);
			case FORALL		:
				final EntityDescriptor	forallVar = names.getCargo(node.value);
				
forallLoop:		for (Object item : buildSimpleIterable((SyntaxNode)node.cargo, names, stack, predef, level, callback)) {
					Array.set(stack.getVar(forallVar.id),0,item);
					
					final CallResult 	result = (CallResult)execute(node.children[0], names, stack, predef, depth + 1, level, callback);
					
					if (result.type.isReturnRequired() && result.level > 0) {
						result.level--;
						switch (result.type) {
							case BREAK : case RETURN :
								return result;
							case CONTINUE	:
								if (result.level > 0) {
									return result;
								}
								else {
									continue forallLoop;
								}
							case ORDINAL	:
								break;
							default	:
								throw new UnsupportedOperationException("Result type ["+result.type+"] is not implemented yet"); 
						}
					}
				}
				return new CallResult(CallResult.ResultType.ORDINAL);
			case IF			:
				if (convert(calc((SyntaxNode)node.cargo, names, stack, predef, level, callback), boolean.class)) {
					final CallResult 	result = (CallResult)execute(node.children[0], names, stack, predef, depth + 1, level, callback);
					
					if (result.type.isReturnRequired() && result.level > 0) {
						return result;
					}
				}
				else if (node.children.length > 1 && node.children[1] != null) {
					final CallResult 	result = (CallResult)execute(node.children[1], names, stack, predef, depth + 1, level, callback);
					
					if (result.type.isReturnRequired() && result.level > 0) {
						return result;
					}
				}
				return new CallResult(CallResult.ResultType.ORDINAL);
			case RETURN		:
				return new CallResult(CallResult.ResultType.RETURN, depth);
			case RETURN1	:
				return new CallResult(CallResult.ResultType.RETURN, depth, calc((SyntaxNode)node.cargo, names, stack, predef, level, callback));
			case SEQUENCE	:
				for (SyntaxNode item : node.children) {
					final CallResult 	result = (CallResult)execute(item, names, stack, predef, depth, level, callback);
					
					if (result.type.isReturnRequired() && result.level > 0) {
						return result;
					}
				}
				return new CallResult(CallResult.ResultType.ORDINAL);
			case UNTIL		:
untilLoop:		do {final CallResult 	result = (CallResult)execute(node.children[0], names, stack, predef, depth + 1, level, callback);
				
					if (result.type.isReturnRequired() && result.level > 0) {
						result.level--;
						switch (result.type) {
							case BREAK : case RETURN :
								return result;
							case CONTINUE	:
								if (result.level > 0) {
									return result;
								}
								else {
									continue untilLoop;
								}
							case ORDINAL	:
								break;
							default	:
								throw new UnsupportedOperationException("Result type ["+result.type+"] is not implemented yet"); 
						}
					}
				} while (convert(calc((SyntaxNode)node.cargo, names, stack, predef, level, callback), boolean.class));
				
				return new CallResult(CallResult.ResultType.ORDINAL);
			case WHILE		:
whileLoop:		while (convert(calc((SyntaxNode)node.cargo, names, stack, predef, level, callback), boolean.class)) {
					final CallResult 	result = (CallResult)execute(node.children[0], names, stack, predef, depth + 1, level, callback);
					
					if (result.type.isReturnRequired() && result.level > 0) {
						result.level--;
						switch (result.type) {
							case BREAK : case RETURN :
								return result;
							case CONTINUE	:
								if (result.level > 0) {
									return result;
								}
								else {
									continue whileLoop;
								}
							case ORDINAL	:
								break;
							default	:
								throw new UnsupportedOperationException("Result type ["+result.type+"] is not implemented yet"); 
						}
					}
				}
				return new CallResult(CallResult.ResultType.ORDINAL);
			case STRONG_BINARY	:	// Assignment operator
				if ((OperatorTypes)node.cargo == OperatorTypes.ASSIGNMENT) {
					calc(node, names, stack, predef, level, callback);
					return new CallResult(CallResult.ResultType.ORDINAL);
				}
			default:
				throw new UnsupportedOperationException("Node type ["+node.getType()+"] is not supported yet"); 
		}
		return null;
	}

	static Object calc(final SyntaxNode node, final SyntaxTreeInterface<EntityDescriptor> names, final LocalStack stack, final Predefines predef, final int level, final ExecuteScriptCallback callback) throws PaintScriptException, InterruptedException {
		callback.process(level, node);
		switch ((SyntaxNodeType)node.getType()) {
			case ACCESS			:
				Object	accVal = stack.getVar(node.value);
				
				if (accVal == null) {
					throw new PaintScriptException(new SyntaxException(node.row, node.col, "Null value inside...")); 
				}
				if (node.children != null && node.children.length > 0) {
					accVal = processAccess(accVal, node, names, stack, predef, level, callback);
				}
				else if (accVal.getClass().isArray()) {
					accVal = Array.get(accVal, 0);
				}
				return accVal;
			case BINARY			:
				final Object	infix = calc(node.children[0], names, stack, predef, level, callback);
				
				switch (((OperatorTypes[])node.cargo)[0].getInfixPriority()) {
					case ADDITION	:
						if (infix instanceof char[]) {
							final StringBuilder	sb = new StringBuilder();
							
							sb.append((char[])infix);
							
							for (int index = 1; index < node.children.length; index++) {
								if (((OperatorTypes[])node.cargo)[index - 1] == OperatorTypes.ADD) {
									sb.append((char[])calc(node.children[index], names, stack, predef, level, callback));
								}
								else {
									sb.append(new String((char[])calc(node.children[index], names, stack, predef, level, callback)).trim());
								}
							}
							return sb.toString().toCharArray();
						}
						else if (infix instanceof Double) {
							double	addOp = ((Double)infix).doubleValue();
							
							for (int index = 1; index < node.children.length; index++) {
								if (((OperatorTypes[])node.cargo)[index - 1] == OperatorTypes.ADD) {
									addOp += ((Double)calc(node.children[index], names, stack, predef, level, callback));
								}
								else {
									addOp -= ((Double)calc(node.children[index], names, stack, predef, level, callback));
								}
							}
							return Double.valueOf(addOp);
						}
						else if (infix instanceof Long) {
							long	addOp = ((Long)infix).longValue();
							
							for (int index = 1; index < node.children.length; index++) {
								if (((OperatorTypes[])node.cargo)[index - 1] == OperatorTypes.ADD) {
									addOp += ((Long)calc(node.children[index], names, stack, predef, level, callback));
								}
								else {
									addOp -= ((Long)calc(node.children[index], names, stack, predef, level, callback));
								}
							}
							return Long.valueOf(addOp);
						}
						else {
							throw new UnsupportedOperationException();
						}
					case BIT_AND	:
						long	bitAndOp = ((Long)infix).longValue();
						
						for (int index = 1; index < node.children.length; index++) {
							bitAndOp &= ((Long)calc(node.children[index], names, stack, predef, level, callback));
						}
						return Long.valueOf(bitAndOp);
					case BIT_OR		:
						long	bitOrOp = ((Long)infix).longValue();
						
						for (int index = 1; index < node.children.length; index++) {
							if (((OperatorTypes[])node.cargo)[index - 1] == OperatorTypes.BIT_OR) {
								bitOrOp |= ((Long)calc(node.children[index], names, stack, predef, level, callback));
							}
							else {
								bitOrOp ^= ((Long)calc(node.children[index], names, stack, predef, level, callback));
							}
						}
						return Long.valueOf(bitOrOp);
					case BOOL_AND	:
						if (!((Boolean)infix)) {
							return Boolean.valueOf(false);
						}
						else {
							for (int index = 1; index < node.children.length; index++) {
								if (!((Boolean)calc(node.children[index], names, stack, predef, level, callback))) {
									return Boolean.valueOf(false);
								}
							}
							return Boolean.valueOf(true);
						}
					case BOOL_OR	:
						if (((Boolean)infix)) {
							return Boolean.valueOf(true);
						}
						else {
							for (int index = 1; index < node.children.length; index++) {
								if (((Boolean)calc(node.children[index], names, stack, predef, level, callback))) {
									return Boolean.valueOf(true);
								}
							}
							return Boolean.valueOf(false);
						}
					case MULTIPLICATION:
						if (infix instanceof Double) {
							double	mulOp = ((Double)infix).doubleValue();
							
							for (int index = 1; index < node.children.length; index++) {
								if (((OperatorTypes[])node.cargo)[index - 1] == OperatorTypes.MUL) {
									mulOp *= ((Double)calc(node.children[index], names, stack, predef, level, callback));
								}
								else if (((OperatorTypes[])node.cargo)[index - 1] == OperatorTypes.DIV) {
									mulOp /= ((Double)calc(node.children[index], names, stack, predef, level, callback));
								}
								else {
									mulOp %= ((Double)calc(node.children[index], names, stack, predef, level, callback));
								}
							}
							return Double.valueOf(mulOp);
						}
						else if (infix instanceof Long) {
							long	mulOp = ((Long)infix).longValue();
							
							for (int index = 1; index < node.children.length; index++) {
								if (((OperatorTypes[])node.cargo)[index - 1] == OperatorTypes.MUL) {
									mulOp *= ((Long)calc(node.children[index], names, stack, predef, level, callback));
								}
								else if (((OperatorTypes[])node.cargo)[index - 1] == OperatorTypes.DIV) {
									mulOp /= ((Long)calc(node.children[index], names, stack, predef, level, callback));
								}
								else {
									mulOp %= ((Long)calc(node.children[index], names, stack, predef, level, callback));
								}
							}
							return Long.valueOf(mulOp);
						}
						else {
							throw new UnsupportedOperationException();
						}
					default:
						throw new UnsupportedOperationException();
				}
			case CALL			:
				break;
			case CONSTANT		:
				switch (((ConstantDescriptor)node.cargo).getDataType()) {
					case INT 	:
						return Long.valueOf(((ConstantDescriptor)node.cargo).longContent);
					case REAL 	:
						return Double.valueOf(Double.longBitsToDouble(((ConstantDescriptor)node.cargo).longContent));
					case BOOL	:
						return Boolean.valueOf(((ConstantDescriptor)node.cargo).longContent != 0);
					case STR	:
						return ((ConstantDescriptor)node.cargo).charContent;
					default :
						throw new UnsupportedOperationException();
				}
			case STRONG_BINARY	:
				final Object	leftValue, rightValue;
				
				switch ((OperatorTypes)node.cargo) {
					case ASSIGNMENT	:
						final Class<?> leftType = calcType(node.children[0]);
						
						rightValue = convert(calc(node.children[1], names, stack, predef, level, callback), leftType);
						processAccess(rightValue, node.children[0], names, stack, predef, level, callback);
//						leftValue = calc(node.children[0], names, stack, predef, level, callback);
//						Array.set(leftValue, 0, rightValue);
						return new CallResult(CallResult.ResultType.ORDINAL);
					case EQ			:
						leftValue = calc(node.children[0], names, stack, predef, level, callback);
						rightValue = convert(calc(node.children[1], names, stack, predef, level, callback), leftValue.getClass());
						return Boolean.valueOf(compare(leftValue, rightValue) == 0);
					case GE			:
						leftValue = calc(node.children[0], names, stack, predef, level, callback);
						rightValue = convert(calc(node.children[1], names, stack, predef, level, callback), leftValue.getClass());
						return Boolean.valueOf(compare(leftValue, rightValue) >= 0);
					case GT			:
						leftValue = calc(node.children[0], names, stack, predef, level, callback);
						rightValue = convert(calc(node.children[1], names, stack, predef, level, callback), leftValue.getClass());
						return Boolean.valueOf(compare(leftValue, rightValue) > 0);
					case LE			:
						leftValue = calc(node.children[0], names, stack, predef, level, callback);
						rightValue = convert(calc(node.children[1], names, stack, predef, level, callback), leftValue.getClass());
						return Boolean.valueOf(compare(leftValue, rightValue) <= 0);
					case LT			:
						leftValue = calc(node.children[0], names, stack, predef, level, callback);
						rightValue = convert(calc(node.children[1], names, stack, predef, level, callback), leftValue.getClass());
						return Boolean.valueOf(compare(leftValue, rightValue) < 0);
					case NE			:
						leftValue = calc(node.children[0], names, stack, predef, level, callback);
						rightValue = convert(calc(node.children[1], names, stack, predef, level, callback), leftValue.getClass());
						return Boolean.valueOf(compare(leftValue, rightValue) != 0);
					case IN			:
						leftValue = calc(node.children[0], names, stack, predef, level, callback);
						for (Object item : buildSimpleIterable(node.children[1], names, stack, predef, level, callback)) {
							if (compare(leftValue, convert(item, (leftValue.getClass()))) == 0) {
								return Boolean.valueOf(true);
							}
						}
						return Boolean.valueOf(false);
					default:
						throw new UnsupportedOperationException("Operator type ["+node.cargo+"] is not supported here"); 
				}
			case PREFIX			:
				final Object	prefix = calc(node.children[0], names, stack, predef, level, callback);
				
				switch ((OperatorTypes)node.cargo) {
					case ADD		:
						return prefix;
					case BIT_INV	:
						return Long.valueOf(~convert(prefix, long.class));
					case BOOL_NOT	:
						return Boolean.valueOf(!convert(prefix, boolean.class));
					case INC		:
						throw new UnsupportedOperationException();
					case DEC		:
						throw new UnsupportedOperationException();
					case SUB		:
						if (prefix instanceof Long) {
							return -convert(prefix, long.class);
						}
						else {
							return -convert(prefix, double.class);
						}
					default:
						throw new UnsupportedOperationException("Operator type ["+node.cargo+"] is not supported here"); 
				}
			case SUFFIX			:
				final Object	suffix = calc(node.children[0], names, stack, predef, level, callback);
				
				try{switch ((OperatorTypes)node.cargo) {
						case TO_REAL		:
							return SQLUtils.convert(double.class, suffix);
						case TO_INT		:
							return SQLUtils.convert(long.class, suffix);
						case TO_STR		:
							return SQLUtils.convert(String.class, suffix).toCharArray();
						case TO_BOOL		:
							return SQLUtils.convert(boolean.class, suffix);
						default:
							throw new UnsupportedOperationException("Operator type ["+node.cargo+"] is not supported here"); 
					}
				} catch (ContentException exc) {
					throw new PaintScriptException(exc); 
				}
			case CONSTRUCTOR	:
				switch (((ArrayDescriptor)node.cargo).dataType) {
					case ARRAY	:
						final PaintScriptListInterface	psli = PaintScriptListInterface.Factory.newInstance(((ArrayDescriptor)node.cargo).contentType);
						
						psli.append(node.children.length);
						for (int index = 0; index < node.children.length; index++) {
							psli.set(index, calc(node.children[index], names, stack, predef, level, callback));
						}
						return psli;
					case COLOR	:
						if (node.children.length == 3) {
							final Number	red = (Number)calc(node.children[0], names, stack, predef, level, callback);
							final Number	green = (Number)calc(node.children[1], names, stack, predef, level, callback);
							final Number	blue = (Number)calc(node.children[2], names, stack, predef, level, callback);
							
							return new Color(red.intValue(), green.intValue(), blue.intValue());
						}
						else if (node.children.length == 1) {
							final Number	val = (Number)calc(node.children[0], names, stack, predef, level, callback);
							
							return new Color(val.intValue());
						}
						break;
					case FONT	:
						if (node.children.length == 3) {
							final char[]	family = (char[])calc(node.children[0], names, stack, predef, level, callback);
							final Number	size = (Number)calc(node.children[1], names, stack, predef, level, callback);
							final Number	style = (Number)calc(node.children[2], names, stack, predef, level, callback);
							
							return new Font(new String(family), style.intValue(), size.intValue());
						}
						else if (node.children.length == 2) {
							final char[]	family = (char[])calc(node.children[0], names, stack, predef, level, callback);
							final Number	size = (Number)calc(node.children[1], names, stack, predef, level, callback);
							
							return new Font(new String(family), 0, size.intValue());
						}
						else if (node.children.length == 1) {
							final char[]	descr = (char[])calc(node.children[0], names, stack, predef, level, callback);
							
							return Font.decode(new String(descr));
						}
						break;
					case FUNC:
						break;
					case IMAGE	:
						if (node.children.length == 3) {
							final Number	width = (Number)calc(node.children[0], names, stack, predef, level, callback);
							final Number	height = (Number)calc(node.children[1], names, stack, predef, level, callback);
							final char[]	type = (char[])calc(node.children[2], names, stack, predef, level, callback);
							
							return new BufferedImage(width.intValue(), height.intValue(), ImageType.valueOf(new String(type)).getType());
						}
						else if (node.children.length == 0) {
							return new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
						}
						break;
					case MAP	:
						final PaintScriptMapInterface	psmi = PaintScriptMapInterface.Factory.newInstance(((ArrayDescriptor)node.cargo).contentType);
						
						for (int index = 0; index < node.children.length; index+= 2) {
							psmi.set((char[])calc(node.children[index], names, stack, predef, level, callback), calc(node.children[index + 1], names, stack, predef, level, callback));
						}
						return psmi;
					case POINT	:
						if (node.children.length == 2) {
							final Number	x = (Number)calc(node.children[0], names, stack, predef, level, callback);
							final Number	y = (Number)calc(node.children[1], names, stack, predef, level, callback);
							
							return new Point(x.intValue(), y.intValue());
						}
						break;
					case PROC:
						break;
					case RECT	:
						if (node.children.length == 4) {
							final Number	x = (Number)calc(node.children[0], names, stack, predef, level, callback);
							final Number	y = (Number)calc(node.children[1], names, stack, predef, level, callback);
							final Number	width = (Number)calc(node.children[2], names, stack, predef, level, callback);
							final Number	height = (Number)calc(node.children[3], names, stack, predef, level, callback);
							
							return new Rectangle(x.intValue(), y.intValue(), width.intValue(), height.intValue());
						}
						else if (node.children.length == 2) {
							final Point		p = (Point)calc(node.children[0], names, stack, predef, level, callback);
							final Dimension	s = (Dimension)calc(node.children[1], names, stack, predef, level, callback);
							
							return new Rectangle(p, s);
						}
						break;
					case SIZE	:
						if (node.children.length == 2) {
							final Number	x = (Number)calc(node.children[0], names, stack, predef, level, callback);
							final Number	y = (Number)calc(node.children[1], names, stack, predef, level, callback);
							
							return new Dimension(x.intValue(), y.intValue());
						}
						break;
					case STROKE:
						if (node.children.length == 4) {
							final char[]		style = (char[])calc(node.children[0], names, stack, predef, level, callback);
							final Number		width = (Number)calc(node.children[1], names, stack, predef, level, callback);
							final char[]		caps = (char[])calc(node.children[2], names, stack, predef, level, callback);
							final char[]		join  = (char[])calc(node.children[3], names, stack, predef, level, callback);
							final StrokeWrapper	sw = StrokeWrapper.of(new BasicStroke());
							
							sw.setStyle(LineStroke.valueOf(new String(style)));
							sw.setWidth(width.intValue());
							sw.setCaps(LineCaps.valueOf(new String(caps)));
							sw.setJoin(LineJoin.valueOf(new String(join)));
							return sw.getStroke();
						}
						break;
					case STRUCTURE:
						break;
					case TRANSFORM	:
						if (node.children.length == 1) {
							final char[]	transform = (char[])calc(node.children[0], names, stack, predef, level, callback);
							
							try{return CSSUtils.asTransform(new String(transform));
							} catch (SyntaxException exc) {
								throw new PaintScriptException(exc.getLocalizedMessage(), exc); 
							}
						}
						else if (node.children.length == 0) {
							return new AffineTransform();
						}
						break;
					case UNKNOWN:
						break;
					default:
						throw new UnsupportedOperationException("Data type ["+node.cargo+"] is not supported yet"); 
				}
				break;
			default:
				throw new UnsupportedOperationException("Node type ["+node.getType()+"] is not supported yet"); 
		}
		return null;
	}	

	private static Object processAccess(final Object value, final SyntaxNode node, final SyntaxTreeInterface<EntityDescriptor> names, final LocalStack stack, final Predefines predef, final int level, final ExecuteScriptCallback callback) throws PaintScriptException, InterruptedException {
		// TODO Auto-generated method stub
		if (node.getType() == SyntaxNodeType.ACCESS) {
			Object 	accTarget = stack.getVar(node.value);
			
			if (accTarget == null) {
				throw new PaintScriptException(new SyntaxException(node.row, node.col, "Null value inside...")); 
			}
			else if (node.children.length > 0) {
				for (SyntaxNode<SyntaxNodeType, ?> item : node.children) {
					switch (item.getType()) {
						case CALL				:
							if (accTarget instanceof ContentWrapper[]) {
								accTarget = ((ContentWrapper[])accTarget)[0].getContent();
							}
							if (item.children.length > 0) {
								final Object[]		parameters = new Object[item.children.length];
								final Class<?>[]	types = ((Method)item.cargo).getParameterTypes();
								
								for (int index = 0; index < types.length; index++) {
									parameters[index] = convert(calc(item.children[index], names, stack, predef, level, callback), types[index]);
								}
								try{accTarget = ((Method)item.cargo).invoke(accTarget, parameters);
								} catch (IllegalAccessException exc) {
									throw new PaintScriptException(new SyntaxException(node.row, node.col, "Method call error: "+exc.getLocalizedMessage(), exc)); 
								} catch (InvocationTargetException exc) {
									throw new PaintScriptException(new SyntaxException(node.row, node.col, "Method call error: "+exc.getTargetException().getLocalizedMessage(), exc.getTargetException())); 
								}
							}
							else {
								try{accTarget = ((Method)item.cargo).invoke(accTarget);
								} catch (IllegalAccessException | InvocationTargetException exc) {
									throw new PaintScriptException(new SyntaxException(node.row, node.col, "Method call error: "+exc.getLocalizedMessage(), exc)); 
								}
							}
							break;
						case GET_FIELD			:
							if (accTarget instanceof ContentWrapper[]) {
								accTarget = ((ContentWrapper[])accTarget)[0].getContent();
							}
							if (item.children.length > 0) {
								throw new UnsupportedOperationException();
							}
							else {
								try{accTarget = ((Field)item.cargo).get(accTarget);
								} catch (IllegalAccessException exc) {
									throw new PaintScriptException(new SyntaxException(node.row, node.col, "Field get error: "+exc.getLocalizedMessage(), exc)); 
								}
							}
							break;
						case GET_FIELD_INDEX	:
							throw new UnsupportedOperationException();
						case GET_VAR_INDEX		:
							throw new UnsupportedOperationException();
						case SET_FIELD			:
							throw new UnsupportedOperationException();
						case SET_FIELD_INDEX	:
							throw new UnsupportedOperationException();
						case SET_VAR_INDEX		:
							if (accTarget instanceof PaintScriptListInterface[]) {
								final long	index = convert(calc(item.children[0], names, stack, predef, level, callback), long.class);
								
								((PaintScriptListInterface[])accTarget)[0].set((int)index, value);
							}
							else if (accTarget instanceof PaintScriptMapInterface[]) {
								final char[]	index = convert(calc(item.children[0], names, stack, predef, level, callback), char[].class);
								
								((PaintScriptMapInterface[])accTarget)[0].set(index, value);
							}
							break;
						default:
							throw new UnsupportedOperationException();
					}
				}
			}
			else {
				final Class<?>	cl = accTarget.getClass();
				
				if (cl.isArray()) {
					if (ContentWrapper.class.isAssignableFrom(cl.getComponentType())) {
						((ContentWrapper)Array.get(accTarget, 0)).setContent(value);
					}
					else {
						if (cl.getComponentType().isAssignableFrom(value.getClass())) {
							Array.set(accTarget, 0, value);
						}
						else if (cl.getComponentType().isPrimitive() && !value.getClass().isPrimitive()) {
							final Object assignValue = value.getClass().isArray() ? Array.get(value, 0) : value; 
							
							switch (CompilerUtils.defineClassType(cl.getComponentType())) {
								case CompilerUtils.CLASSTYPE_BYTE	:
									Array.setByte(accTarget, 0, ((Number)assignValue).byteValue());
									break;
								case CompilerUtils.CLASSTYPE_SHORT	:
									Array.setShort(accTarget, 0, ((Number)assignValue).shortValue());
									break;
								case CompilerUtils.CLASSTYPE_CHAR	:	
									Array.setChar(accTarget, 0, ((Character)assignValue).charValue());
									break;
								case CompilerUtils.CLASSTYPE_INT	:	
									Array.setInt(accTarget, 0, ((Number)assignValue).intValue());
									break;
								case CompilerUtils.CLASSTYPE_LONG	:	
									Array.setLong(accTarget, 0, ((Number)assignValue).longValue());
									break;
								case CompilerUtils.CLASSTYPE_FLOAT	:	
									Array.setFloat(accTarget, 0, ((Number)assignValue).floatValue());
									break;
								case CompilerUtils.CLASSTYPE_DOUBLE	:	
									Array.setDouble(accTarget, 0, ((Number)assignValue).doubleValue());
									break;
								case CompilerUtils.CLASSTYPE_BOOLEAN:
									Array.setBoolean(accTarget, 0, ((Boolean)assignValue).booleanValue());
									break;
								default :
									throw new UnsupportedAddressTypeException(); 
							}
						}
						else {
							throw new UnsupportedOperationException();
						}
					}
				}
				else {
					throw new UnsupportedOperationException();
				}
			}
			return accTarget;
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

	private static Iterable<Number> buildIterable(final SyntaxNode initial, final SyntaxNode terminal, final SyntaxTreeInterface<EntityDescriptor> names, final LocalStack stack, final Predefines predef, final int level, final ExecuteScriptCallback callback) throws PaintScriptException, InterruptedException {
		final int	initialValue = convert(calc(initial, names, stack, predef, level, callback), int.class);
		final int	terminalValue = convert(calc(terminal, names, stack, predef, level, callback), int.class);
		
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

	private static Iterable<Number> buildIterable(final SyntaxNode initial, final SyntaxNode terminal, final SyntaxNode step, final SyntaxTreeInterface<EntityDescriptor> names, final LocalStack stack, final Predefines predef, final int level, final ExecuteScriptCallback callback) throws PaintScriptException, InterruptedException {
		final int	initialValue = convert(calc(initial, names, stack, predef, level, callback), int.class);
		final int	terminalValue = convert(calc(terminal, names, stack, predef, level, callback), int.class);
		final int 	stepValue = convert(calc(step, names, stack, predef, level, callback), int.class);

		if (terminalValue < initialValue && stepValue >= 0) {
			throw new PaintScriptException("For loop error: terminal < initial and step >= 0"); 
		}
		else if (terminalValue > initialValue && stepValue <= 0) {
			throw new PaintScriptException("For loop error: terminal > initial and step <= 0"); 
		}
		else if (stepValue == 0) {
			throw new PaintScriptException("For loop error: step == 0"); 
		}
		else {
			return new Iterable<Number>() {
				@Override
				public Iterator<Number> iterator() {
					return new Iterator<Number>() {
						private int	current = initialValue;
	
						@Override
						public boolean hasNext() {
							return stepValue > 0 ? current <= terminalValue : current >= terminalValue;
						}
	
						@Override
						public Number next() {
							return Integer.valueOf(current += stepValue);
						}
					};
				}
			};
		}
	}
	
	private static Iterable<Object> buildSimpleIterable(final SyntaxNode node, final SyntaxTreeInterface<EntityDescriptor> names, final LocalStack stack, final Predefines predef, final int level, final ExecuteScriptCallback callback) throws InterruptedException, PaintScriptException {
		final List<Object>	result = new ArrayList<>();

		callback.process(level, node);
		switch ((SyntaxNodeType)node.getType()) {
			case LIST			:
				for (SyntaxNode item : node.children) {
					result.add(calc(item, names, stack, predef, level, callback));
				}
				break;
			default :
				result.add(calc(node, names, stack, predef, level, callback));
		}
		return result;
	}

	private static void buildRangedIterable(final SyntaxNode node, final SyntaxTreeInterface<EntityDescriptor> names, final LocalStack stack, final Predefines predef, final List<Number[]> result, final int level, final ExecuteScriptCallback callback) throws InterruptedException, PaintScriptException {
		callback.process(level, node);
		switch ((SyntaxNodeType)node.getType()) {
			case LIST			:
				for (SyntaxNode item : node.children) {
					final Number	listValue = convert(calc(item, names, stack, predef, level, callback), Number.class);
					
					result.add(new Number[] {listValue, listValue});
				}
				break;
			case RANGE			:
				result.add(new Number[] {convert(calc(node.children[0], names, stack, predef, level, callback), Number.class), convert(calc(node.children[1], names, stack, predef, level, callback), Number.class)});
				break;
			default :
				final Number	value = convert(calc(node, names, stack, predef, level, callback), Number.class);
				
				result.add(new Number[] {value, value});
				break;
		}
	}

	private static int compare(final Object left, final Object right) throws PaintScriptException {
		if ((left instanceof Long) && (right instanceof Long)) {
			return ((Long)left).compareTo((Long)right);
		}
		else if ((left instanceof Double) && (right instanceof Double)) {
			return ((Long)left).compareTo((Long)right);
		}
		else if ((left instanceof char[]) && (right instanceof char[])) {
			return CharUtils.compareTo((char[])left,(char[])right);
		}
		else if ((left instanceof Boolean) && (right instanceof Boolean)) {
			return ((Boolean)left ? 1 : 0) - ((Boolean)right ? 1 : 0); 
		}
		else {
			throw new PaintScriptException("Comparison error: incompatible types ["+left.getClass().getCanonicalName()+"] and ["+right.getClass().getCanonicalName()+"]");
		}
	}

	private static <T> T convert(final Object instance, final Class<T> awaited) throws PaintScriptException {
		if (instance == null) {
			return null;
		}
		else if (awaited.isPrimitive()) {
			return (T)convert(instance, CompilerUtils.toWrappedClass(awaited));
		}
		else if (awaited.isInstance(instance)) {
			return (T)instance;
		}
		else {
			try{
				return SQLUtils.convert(awaited, instance);
			} catch (ContentException exc) {
				throw new PaintScriptException("Conversion error: "+exc.getMessage(), exc);
			}
		}
	}

	private static void insertConversions(final SyntaxNode node, final Class<?> awaited) {
		switch ((SyntaxNodeType)node.getType()) {
			case BREAK		:	case CASE		:	case CASEDEF	:	case CONTINUE	:
			case FOR		:	case FOR1		:	case FORALL		:	case IF			:
			case RETURN1	:	case RETURN		:	case SEQUENCE	:	case UNTIL		:
			case WHILE		:
				throw new IllegalArgumentException("Node type ["+node.getType()+"] can't be used for conversion");
			case ACCESS		:
				if ((Class<?>)node.cargo != awaited) {
					addConversion(node, awaited);
				}
				break;
			case BINARY		:
				final Class<?>	expr = calcType(node);
				
				for (SyntaxNode item : node.children) {
					insertConversions(node, expr);
				}
				if (expr != awaited) {
					addConversion(node, awaited);
				}
				break;
			case CALL		:
				break;
			case CONSTANT	:
				if (((Lexema)node.cargo).getDataType().getRightValueClassAssociated() != awaited) {
					convertConstant(node, awaited);
				}
				break;
			case LIST		:	case RANGE		:
				for (SyntaxNode item : node.children) {
					insertConversions(node, awaited);
				}
				break;
			case PREFIX		:	case SUFFIX		:
				if (((OperatorTypes)node.cargo).hasStrongReturnedType()) {
					if (((OperatorTypes)node.cargo).getReturnedType().getRightValueClassAssociated() != awaited) {
						addConversion(node, awaited);
					}
				}
				else if (calcType(node.children[0]) != awaited) {
					addConversion(node, awaited);
				}
				break;
			case ROOT		:
				break;
			case STRONG_BINARY:
				if (((OperatorTypes)node.cargo).hasStrongReturnedType()) {
					final Class<?>	strongLeft = calcType(node.children[0]);
					
					insertConversions(node.children[1], strongLeft);
					if (strongLeft != awaited) {
						addConversion(node, awaited);
					}
				}
				else {
					for (SyntaxNode item : node.children) {
						insertConversions(node, awaited);
					}
				}
				break;
			case SUBSTITUTION:
				if (awaited != char[].class) {
					addConversion(node, awaited);
				}
				break;
			default:
				throw new UnsupportedOperationException("Node type ["+node.getType()+"] is not supported yet");
		}
	}	
	
	private static Class<?> calcType(final SyntaxNode node) {
		switch ((SyntaxNodeType)node.getType()) {
			case BREAK		:	case CASE		:	case CASEDEF	:	case CONTINUE	:
			case FOR		:	case FOR1		:	case FORALL		:	case IF			:
			case RETURN1	:	case RETURN		:	case SEQUENCE	:	case UNTIL		:
			case WHILE		:
				return CallResult.class;
			case ACCESS		:
				return ((Class<?>[])node.cargo)[((Class<?>[])node.cargo).length - 1];
			case BINARY		:
				final Set<Class<?>>		binaryCollection = new HashSet<>();
				final OperatorTypes[]	ops = (OperatorTypes[])node.cargo;
				
				binaryCollection.add(calcType(node.children[0]));
				for (int index = 0; index < ops.length; index++) {
					if (ops[index].hasStrongReturnedType()) {
						binaryCollection.add(ops[index].getReturnedType().getRightValueClassAssociated());
					}
					else {
						binaryCollection.add(calcType(node.children[index + 1]));
					}
				}
				if (binaryCollection.size() == 1) {
					return extractDataType(binaryCollection);
				}
				else {
					return reduceDataType(binaryCollection);
				}
			case CALL		:
				break;
			case CONSTANT	:
				return ((Lexema)node.cargo).getDataType().getRightValueClassAssociated();
			case LIST		:	case RANGE		:
				final Set<Class<?>>	listCollection = new HashSet<>(); 
				
				for (SyntaxNode item : node.children) {
					listCollection.add(calcType(item));
				}
				if (listCollection.size() == 1) {
					return extractDataType(listCollection);
				}
				else {
					return reduceDataType(listCollection);
				}
			case PREFIX		:	case SUFFIX		:
				if (((OperatorTypes)node.cargo).hasStrongReturnedType()) {
					return ((OperatorTypes)node.cargo).getReturnedType().getRightValueClassAssociated();
				}
				else {
					return calcType(node.children[0]); 
				}
			case ROOT		:
				return Object.class;
			case STRONG_BINARY:
				if (((OperatorTypes)node.cargo).hasStrongReturnedType()) {
					return ((OperatorTypes)node.cargo).getReturnedType().getRightValueClassAssociated();
				}
				else {
					final Set<Class<?>>	strongCollection = new HashSet<>(); 
					
					for (SyntaxNode item : node.children) {
						strongCollection.add(calcType(item));
					}
					if (strongCollection.size() == 1) {
						return extractDataType(strongCollection);
					}
					else {
						return reduceDataType(strongCollection);
					}
				}
			case SUBSTITUTION:
				return char[].class;
			default:
				throw new UnsupportedOperationException("Node type ["+node.getType()+"] is not supported yet");
		}
		return null;
	}
	
	private static Class<?> reduceDataType(final Set<Class<?>> collection) {
		if (collection.contains(Long.class) && collection.contains(Double.class)) {
			collection.remove(Long.class);
		}
		return extractDataType(collection);
	}

	private static Class<?> extractDataType(final Set<Class<?>> collection) {
		for (Class<?> item : collection) {
			return item;
		}
		throw new IllegalArgumentException("Collection can't be empty!"); 
	}

	private static void convertConstant(final SyntaxNode node, final Class<?> awaited) {
		addConversion(node, awaited);
	}

	private static void addConversion(final SyntaxNode node, final Class<?> awaited) {
		final SyntaxNode	child = (SyntaxNode) node.clone();
		
		node.children = new SyntaxNode[] {child};
		node.type = SyntaxNodeType.SUFFIX;
		if (awaited == Long.class) {
			node.cargo = OperatorTypes.TO_INT;
		}
		else if (awaited == Double.class) {
			node.cargo = OperatorTypes.TO_REAL;
		}
		else if (awaited == char[].class) {
			node.cargo = OperatorTypes.TO_STR;
		}
		else if (awaited == Boolean.class) {
			node.cargo = OperatorTypes.TO_BOOL;
		}
		else if (awaited == Color.class) {
			node.cargo = OperatorTypes.TO_COLOR;
		}
		else if (awaited == Font.class) {
			node.cargo = OperatorTypes.TO_FONT;
		}
		else if (awaited == Point.class) {
			node.cargo = OperatorTypes.TO_POINT;
		}
		else if (awaited == Rectangle.class) {
			node.cargo = OperatorTypes.TO_RECT;
		}
		else if (awaited == Dimension.class) {
			node.cargo = OperatorTypes.TO_SIZE;
		}
		else if (awaited == Stroke.class) {
			node.cargo = OperatorTypes.TO_STROKE;
		}
		else if (awaited == AffineTransform.class) {
			node.cargo = OperatorTypes.TO_TRANSFORM;
		}
		else {
			throw new UnsupportedOperationException(); 
		}
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
