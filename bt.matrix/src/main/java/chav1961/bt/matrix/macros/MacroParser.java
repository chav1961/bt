package chav1961.bt.matrix.macros;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import chav1961.bt.matrix.macros.InternalParser.LexType;
import chav1961.bt.matrix.macros.InternalParser.Lexema;
import chav1961.bt.matrix.macros.InternalParser.Priority;
import chav1961.bt.matrix.macros.NestedReader.Line;
import chav1961.bt.matrix.macros.runtime.CommandList;
import chav1961.bt.matrix.macros.runtime.CommandList.CommandType;
import chav1961.bt.matrix.macros.runtime.interfaces.Value;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class MacroParser {
	private static final char[][]	MACRO_OPERATORS = {
										"param".toCharArray(),
										"var".toCharArray(),
										"set".toCharArray(),
										"if".toCharArray(),
										"elsif".toCharArray(),
										"endif".toCharArray(),
										"while".toCharArray(),
										"endwhile".toCharArray(),
										"for".toCharArray(),
										"endfor".toCharArray(),
										"case".toCharArray(),
										"of".toCharArray(),
										"default".toCharArray(),
										"endcase".toCharArray(),
										"break".toCharArray(),
										"continue".toCharArray(),
										"error".toCharArray(),
										"warning".toCharArray(),
										"print".toCharArray(),
										"include".toCharArray(),
										"call".toCharArray()
									};
	
	private final NestedReader		rdr;
	private final char[]			macroPrefix;
	private final char[]			substPrefix;
	private final char[]			substSuffix;
	private final boolean			suffixIsMandatory;
	private final InternalParser	ip;
	private final StringBuilder		sbTemp = new StringBuilder();
	private final CommandList		cl;
	private ParseStack				stack = new ParseStack(null);
	
	static enum LineType {
		ORDINAL,
		SUBSTITUTION,
		OPERATOR
	}

	static enum OperatorType {
		PARAM("param", LineType.OPERATOR), 
		VAR("var", LineType.OPERATOR),
		SET("set", LineType.OPERATOR),
		IF("if", LineType.OPERATOR),
		ELSE("else", LineType.OPERATOR),
		ENDIF("endif", LineType.OPERATOR),
		WHILE("while", LineType.OPERATOR),
		ENDWHILE("endwhile", LineType.OPERATOR),
		FOR("for", LineType.OPERATOR),
		ENDFOR("endfor", LineType.OPERATOR),
		CASE("case", LineType.OPERATOR),
		OF("of", LineType.OPERATOR),
		DEFAULT("default", LineType.OPERATOR),
		ENDCASE("endcase", LineType.OPERATOR),
		BREAK("break", LineType.OPERATOR),
		CONTINUE("continue", LineType.OPERATOR),
		ERROR("error", LineType.OPERATOR),
		WARNING("warning", LineType.OPERATOR),
		PRINT("print", LineType.OPERATOR),
		INCLUDE("include", LineType.OPERATOR),
		CALL("call", LineType.OPERATOR),
		SUBST("", LineType.SUBSTITUTION),
		ORDINAL("", LineType.ORDINAL);
		
		private final char[]	mnemonics;
		private final LineType	lineType;
		
		private OperatorType(final String mnemonics, final LineType lineType) {
			this.mnemonics = mnemonics.toCharArray();
			this.lineType = lineType;
		}
		
		public char[] getMnemonics() {
			return mnemonics;
		}
		
		public LineType getLineType() {
			return lineType;
		}
	}
	
	public MacroParser(final NestedReader rdr, final char[] macroPrefix, final char[] substPrefix, final char[] substSuffix, final boolean suffixIsMandatory) {
		if (rdr == null) {
			throw new NullPointerException("Reader can't be null");
		}
		else if (!isArrayValid(macroPrefix)) {
			throw new IllegalArgumentException("Macro prefix can't be null or empty char array");
		}
		else if (!isArrayValid(substPrefix)) {
			throw new IllegalArgumentException("Substitution prefix can't be null or empty char array");
		}
		else if (!isArrayValid(substSuffix)) {
			throw new IllegalArgumentException("Substitution suffix can't be null or empty char array");
		}
		else {
			this.rdr = rdr;
			this.macroPrefix = macroPrefix;
			this.substPrefix = substPrefix;
			this.substSuffix = substSuffix;
			this.suffixIsMandatory = suffixIsMandatory;
			this.ip = new InternalParser(macroPrefix, substPrefix, substSuffix, suffixIsMandatory);
			this.cl = new CommandList();
		}
	}
	
	public boolean next(final Line line) throws IOException {
		if (!rdr.next(line)) {
			return false;
		}
		else {
			try {
				final OperatorType	type = classifyLine(line);
				int		from = 0;
				
				switch (type) {
					case BREAK		:
						from = parseBreak(ip.parseMacroLine(line), cl);
						break;
					case CALL		:
						parseCall(line);
						break;
					case CASE		:
						from = parseCase(ip.parseMacroLine(line), cl);
						break;
					case CONTINUE	:
						from = parseContinue(ip.parseMacroLine(line), cl);
						break;
					case DEFAULT	:
						from = parseDefault(ip.parseMacroLine(line), cl);
						break;
					case ELSE		:
						from = parseElse(ip.parseMacroLine(line), cl);
						break;
					case ENDCASE	:
						from = parseEndCase(ip.parseMacroLine(line), cl);
						break;
					case ENDFOR		:
						from = parseEndFor(ip.parseMacroLine(line), cl);
						break;
					case ENDIF		:
						from = parseEndIf(ip.parseMacroLine(line), cl);
						break;
					case ENDWHILE	:
						from = parseEndWhile(ip.parseMacroLine(line), cl);
						break;
					case ERROR		:
						from = parseError(ip.parseMacroLine(line), cl);
						break;
					case FOR		:
						from = parseFor(ip.parseMacroLine(line), cl);
						break;
					case IF			:
						from = parseIf(ip.parseMacroLine(line), cl);
						break;
					case INCLUDE	:
						from = parseInclude(ip.parseMacroLine(line), cl);
						break;
					case OF			:
						from = parseOf(ip.parseMacroLine(line), cl);
						break;
					case ORDINAL	:
						addOrdinal(line, cl);
						break;
					case PARAM		:
						from = parseParam(ip.parseMacroLine(line), cl);
						break;
					case PRINT		:
						from = parsePrint(ip.parseMacroLine(line), cl);
						break;
					case SET		:
						from = parseSet(ip.parseMacroLine(line), cl);
						break;
					case SUBST		:
						processSubstitution(line);
						break;
					case VAR		:
						from = parseVar(ip.parseMacroLine(line), cl);
						break;
					case WARNING	:
						from = parseWarning(ip.parseMacroLine(line), cl);
						break;
					case WHILE		:
						from = parseWhile(ip.parseMacroLine(line), cl);
						break;
					default:
						throw new UnsupportedOperationException("Line type ["+type.getLineType()+"] is not supported yet");
				}
				return refreshLine(line);
			} catch (SyntaxException e) {
				throw new IOException(e);
			}
		}
	}
	
	private void parseCall(final Line line) {
		// TODO Auto-generated method stub
		
	}

	
	
	private int parseFor(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		// TODO Auto-generated method stub
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_FOR) {
			throw new SyntaxException(0, 0, ".for awaited");
		}
		else if (src.get(++from).type != LexType.NAME) {
			throw new SyntaxException(0, 0, "loop var name awaiting");
		}
		else {
			final ForDescriptor	fd = new ForDescriptor(stack.createForwardLabel(), stack.createBackwardLabel());
			
			fd.varName = (int) src.get(from++).value;
			if (src.get(from).type == LexType.COLON) {
				if (src.get(++from).type != LexType.NAME) {
					throw new SyntaxException(0, 0, "var name type awaiting");
				}
				else {
					fd.isLocalVariable = true;
					fd.varType = (int) src.get(from++).value;
				}
			}
			else {
				fd.isLocalVariable = false;
				fd.varType = -1;
			}
			if (src.get(from).type == LexType.ASSIGN) {
				
			}
			else if (src.get(from).type == LexType.KWD_IN) {
				
			}
			else {
				throw new SyntaxException(0, 0, "Neither '=' not 'in' was detected");
			}
			stack.push(OperatorType.FOR, fd);
			
			return from;
		}		
	}


	private int parseEndFor(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		// TODO Auto-generated method stub
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_ENDFOR) {
			throw new SyntaxException(0, 0, ".endfor awaited");
		}
		else if (stack.getTopType() != OperatorType.FOR) {
			throw new SyntaxException(0, 0, ".endfor doesn't have appropriative .for");
		}
		else {
			final ForDescriptor	fd = (ForDescriptor) stack.getTopCargo()[0];
			
			return from;
		}		
	}
	
	private int parseIf(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_IF) {
			throw new SyntaxException(0, 0, ".if awaited");
		}
		else {
			final IfDescriptor	id = new IfDescriptor();
			
			id.totalForward = stack.createForwardLabel();
			stack.push(OperatorType.IF, id);
			from = parseExpression(src, from + 1, Priority.OR, cl);
			cl.addCommand(CommandType.JUMP_FALSE, id.totalForward);
			return from;
		}		
	}

	private int parseElse(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_ELSE) {
			throw new SyntaxException(0, 0, ".else awaited");
		}
		else if (stack.getTopType() != OperatorType.IF) {
			throw new SyntaxException(0, 0, ".else without .if");
		}
		else {
			final IfDescriptor	id = (IfDescriptor) stack.getTopCargo()[0];
			
			if (id.elseDetected) {
				throw new SyntaxException(0, 0, "duplicate .else");
			}
			else {
				final int	elseForward = id.totalForward;
				
				id.elseDetected = true;
				id.totalForward = stack.createForwardLabel();
		
				cl.addCommand(CommandType.JUMP, id.totalForward);
				cl.registerForwardLabel(elseForward);
				return from;
			}
		}
	}

	private int parseEndIf(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_ENDIF) {
			throw new SyntaxException(0, 0, ".endif awaited");
		}
		else if (stack.getTopType() != OperatorType.IF) {
			throw new SyntaxException(0, 0, ".endif doesn't have appropriative .if");
		}
		else {
			final IfDescriptor	id = (IfDescriptor) stack.getTopCargo()[0];
			
			stack.pop(OperatorType.IF);
			cl.registerForwardLabel(id.totalForward);
			return from;
		}
	}

	private int parseCase(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_CASE) {
			throw new SyntaxException(0, 0, ".case awaited");
		}
		else {
			stack.push(OperatorType.CASE, new CaseDescriptor(stack.createForwardLabel()));
			from = parseExpression(src, from + 1, Priority.OR, cl);
			return from;
		}		
	}

	private int parseOf(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_OF) {
			throw new SyntaxException(0, 0, ".of awaited");
		}
		else if (stack.getTopType() != OperatorType.CASE) {
			throw new SyntaxException(0, 0, ".of outside .case");
		}
		else {
			final CaseDescriptor	desc = (CaseDescriptor) stack.getTopCargo()[0];
			
			if (desc.defaultDetected) {
				throw new SyntaxException(0, 0, ".of after .default in .case");
			}
			else {
				if (desc.ofDetected) {
					cl.addCommand(CommandType.JUMP, desc.totalForward);
					cl.registerForwardLabel(desc.nextForward);
				}
				desc.ofDetected = true;
				desc.nextForward = stack.createForwardLabel();
				cl.addCommand(CommandType.DUPLICATE);
				from = parseExpression(src, from + 1, Priority.OR, cl);
				cl.addCommand(CommandType.EQ);
				cl.addCommand(CommandType.JUMP_FALSE, desc.nextForward);
				return from;
			}
		}		
	}
	
	private int parseDefault(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_DEFAULT) {
			throw new SyntaxException(0, 0, ".default awaited");
		}
		else if (stack.getTopType() != OperatorType.CASE) {
			throw new SyntaxException(0, 0, ".endcase doesn't have appropriative .case earlier");
		}
		else {
			final CaseDescriptor	desc = (CaseDescriptor) stack.getTopCargo()[0];
			
			if (!desc.ofDetected) {
				throw new SyntaxException(0, 0, ".default without any .of in .case");
			}
			else if (desc.defaultDetected) {
				throw new SyntaxException(0, 0, "duplicate .default in .case");
			}
			else {
				cl.addCommand(CommandType.JUMP, desc.totalForward);
				cl.registerForwardLabel(desc.nextForward);
				desc.defaultDetected = true;
				return from;
			}
		}		
	}
	
	private int parseEndCase(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_ENDCASE) {
			throw new SyntaxException(0, 0, ".endcase awaited");
		}
		else if (stack.getTopType() != OperatorType.CASE) {
			throw new SyntaxException(0, 0, ".endcase doesn't have appropriative .case earlier");
		}
		else {
			final CaseDescriptor	desc = (CaseDescriptor) stack.getTopCargo()[0];
			
			if (!desc.ofDetected) {
				throw new SyntaxException(0, 0, "empty .case");
			}
			else {
				cl.registerForwardLabel(desc.totalForward);
				cl.addCommand(CommandType.POP);
				stack.pop(OperatorType.CASE);
			}
			return from;
		}		
	}
	
	private int parseWhile(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_WHILE) {
			throw new SyntaxException(0, 0, ".while awaited");
		}
		else {
			final int	backward = stack.createBackwardLabel(), forward = stack.createBackwardLabel();
			
			stack.push(OperatorType.WHILE, forward, backward);
			cl.registerBackwardLabel(backward);
			from = parseExpression(src, from + 1, Priority.OR, cl);
			cl.addCommand(CommandType.JUMP_FALSE, forward);
			return from;
		}
	}

	private int parseEndWhile(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_ENDWHILE) {
			throw new SyntaxException(0, 0, ".endwhile awaited");
		}
		else if (stack.getTopType() != OperatorType.WHILE) {
			throw new SyntaxException(0, 0, ".endwhile doesn't have appropriative .while earlier");
		}
		else {
			final Object[]	cargo = stack.getTopCargo();
			
			cl.addCommand(CommandType.JUMP, ((Integer)cargo[1]).intValue());
			cl.registerForwardLabel(((Integer)cargo[0]).intValue());
			stack.pop(OperatorType.WHILE);
			return from + 1;
		}
	}
	
	private int parseParam(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_PARAM) {
			throw new SyntaxException(0, 0, ".param awaited");
		}
		else if (src.get(++from).type != LexType.NAME) {
			throw new SyntaxException(0, 0, "parameter name awaited");
		}
		else {
			final int	name = (int) src.get(from++).value;
			
			if (src.get(from).type != LexType.COLON) {
				throw new SyntaxException(0, 0, "Missing ':'");
			}
			else {
				final int		type = (int) src.get(from++).value;
				final boolean	isArray;
				
				if (src.get(from).type == LexType.ARRAY) {
					isArray = true;
					from++;
				}
				else {
					isArray = false;
				}
				// add var
				if (src.get(from).type == LexType.ASSIGN) {
					from = parseExpression(src, from + 1, Priority.OR, cl);
				}
				return from;
			}
		}
	}

	
	private int parseVar(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_VAR) {
			throw new SyntaxException(0, 0, ".var awaited");
		}
		else if (src.get(++from).type != LexType.NAME) {
			throw new SyntaxException(0, 0, "var name awaited");
		}
		else {
			final int	name = (int) src.get(from++).value;
			
			if (src.get(from).type != LexType.COLON) {
				throw new SyntaxException(0, 0, "Missing ':'");
			}
			else {
				final int		type = (int) src.get(from++).value;
				final boolean	isArray;
				
				if (src.get(from).type == LexType.ARRAY) {
					isArray = true;
					from++;
				}
				else {
					isArray = false;
				}
				// add var
				if (src.get(from).type == LexType.ASSIGN) {
					from = parseExpression(src, from + 1, Priority.OR, cl);
				}
				return from;
			}
		}
	}
	
	private int parseBreak(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_BREAK) {
			throw new SyntaxException(0, 0, ".break awaited");
		}
		else {
			final int	depth;
			
			if (src.get(from + 1).type == LexType.CONSTANT_I) {
				depth = (int) src.get(from + 1).value;
				from = 2;
			}
			else {
				depth = 1;
				from = 1;
			}
			cl.addCommand(CommandType.BREAK, depth);
		}
		return from;
	}
	
	private int parseContinue(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_CONTINUE) {
			throw new SyntaxException(0, 0, ".continue awaited");
		}
		else {
			final int	depth;
			
			if (src.get(from + 1).type == LexType.CONSTANT_I) {
				depth = (int) src.get(from + 1).value;
				from = 2;
			}
			else {
				depth = 1;
				from = 1;
			}
			cl.addCommand(CommandType.CONTINUE, depth);
		}
		return from;
	}

	private int parseError(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_ERROR) {
			throw new SyntaxException(0, 0, ".error awaited");
		}
		else {
			from = parseExpression(src, from +  1, Priority.ADD, cl);
			cl.addCommand(CommandType.ERROR);
		}
		return from;
	}

	private int parseWarning(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_WARNING) {
			throw new SyntaxException(0, 0, ".warning awaited");
		}
		else {
			from = parseExpression(src, from +  1, Priority.ADD, cl);
			cl.addCommand(CommandType.WARNING);
		}
		return from;
	}

	private int parsePrint(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_WARNING) {
			throw new SyntaxException(0, 0, ".warning awaited");
		}
		else {
			from = parseExpression(src, from +  1, Priority.ADD, cl);
			cl.addCommand(CommandType.WARNING);
		}
		return from;
	}

	private int parseInclude(final List<Lexema> src, final CommandList cl) throws SyntaxException, IOException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_INCLUDE) {
			throw new SyntaxException(0, 0, ".include awaited");
		}
		else if (src.get(from + 1).type == LexType.CONSTANT_C) {
			final URI	uri = URI.create(new String((char[])src.get(from + 1).content));
			
			rdr.pushSource(uri.toURL());
			return from;
		}
		else {
			throw new SyntaxException(from, from, "Missing URL");
		}
	}

	private int parseSet(final List<Lexema> src, final CommandList cl) throws SyntaxException {
		int	from = 0;
		
		if (src.get(from).type != LexType.KWD_SET) {
			throw new SyntaxException(0, 0, ".set awaited");
		}
		else if (src.get(from + 1).type != LexType.NAME) {
			throw new SyntaxException(0, 0, "var name is missing");
		}
		else {
			final int		varName = (int) src.get(from++).value;
			final boolean	hasIndex;
			
			if (src.get(from).type == LexType.OPENB) {
				from = parseExpression(src, from + 1, Priority.ADD, cl);
				if (src.get(from).type == LexType.CLOSEB) {
					from++;
				}
				else {
					throw new SyntaxException(0, 0, "Missing ']'");
				}
				hasIndex = true;
			}
			else {
				hasIndex=  false;
			}
			if (src.get(from).type == LexType.ASSIGN) {
				from = parseExpression(src, from + 1, Priority.OR, cl);
				if (hasIndex) {
					cl.addCommand(CommandType.STORE_INDEX);
				}
				else {
					cl.addCommand(CommandType.STORE_VAR);
				}
			}
			else {
				throw new SyntaxException(0, 0, "Missing '='");
			}
			return from;
		}
	}

	private int parseExpression(final List<Lexema> lex, int from, final Priority prty, final CommandList commands) throws SyntaxException {
		switch (prty) {
			case OR		:
				from = parseExpression(lex, from, Priority.AND, commands);
				if (lex.get(from).type == LexType.OR) {
					do {from = parseExpression(lex, from + 1, Priority.AND, commands);
						commands.addCommand(CommandType.OR);
					} while (lex.get(from).type == LexType.OR);
				}
				break;
			case AND	:
				from = parseExpression(lex, from, Priority.NOT, commands);
				if (lex.get(from).type == LexType.AND) {
					do {from = parseExpression(lex, from + 1, Priority.NOT, commands);
						commands.addCommand(CommandType.AND);
					} while (lex.get(from).type == LexType.AND);
				}
				break;
			case NOT	:
				if (lex.get(from).type == LexType.NOT) {
					from = parseExpression(lex, from + 1, Priority.COMPARE, commands);
					commands.addCommand(CommandType.NOT);
				}
				break;
			case COMPARE:
				from = parseExpression(lex, from, Priority.MUL, commands);
				if (lex.get(from).type.getPriority() == Priority.COMPARE) {
					LexType	lastType = lex.get(from).type; 
					from = parseExpression(lex, from + 1, Priority.MUL, commands);
					switch (lastType) {
						case EQ :
							commands.addCommand(CommandType.EQ);
							break;
						case GT :
							commands.addCommand(CommandType.GT);
							break;
						case GE :
							commands.addCommand(CommandType.GE);
							break;
						case LT :
							commands.addCommand(CommandType.LT);
							break;
						case LE :
							commands.addCommand(CommandType.LE);
							break;
						case NE :
							commands.addCommand(CommandType.NE);
							break;
						default :
							throw new UnsupportedOperationException();
					}
				}
				break;
			case MUL	:
				from = parseExpression(lex, from, Priority.ADD, commands);
				if (lex.get(from).type.getPriority() == Priority.MUL) {
					LexType	lastType = lex.get(from).type; 
					do {
						from = parseExpression(lex, from + 1, Priority.ADD, commands);
						switch (lastType) {
							case MUL :
								commands.addCommand(CommandType.MUL);
								break;
							case DIV :
								commands.addCommand(CommandType.DIV);
								break;
							case MOD :
								commands.addCommand(CommandType.MOD);
								break;
							default :
								throw new UnsupportedOperationException();
						}
					} while ((lastType = lex.get(from).type).getPriority() == Priority.MUL);
				}
				break;
			case ADD	:
				from = parseExpression(lex, from, Priority.UNARY, commands);
				if (lex.get(from).type.getPriority() == Priority.ADD) {
					LexType	lastType = lex.get(from).type; 
					do {
						from = parseExpression(lex, from + 1, Priority.UNARY, commands);
						switch (lastType) {
							case ADD :
								commands.addCommand(CommandType.ADD);
								break;
							case SUB :
								commands.addCommand(CommandType.SUB);
								break;
							default :
								throw new UnsupportedOperationException();
						}
					} while ((lastType = lex.get(from).type).getPriority() == Priority.ADD);
				}
				break;
			case UNARY	:
				if (lex.get(from).type == LexType.SUB) {
					from = parseExpression(lex, from + 1, Priority.COMPARE, commands);
					commands.addCommand(CommandType.NEGATE);
				}
				else if (lex.get(from).type == LexType.ADD) {
					from = parseExpression(lex, from + 1, Priority.COMPARE, commands);
				}
				else {
					from = parseExpression(lex, from, Priority.COMPARE, commands);
				}
				break;
			case TERM	:
				switch (lex.get(from).type) {
					case CONSTANT_B	:
						commands.addCommand(CommandType.CONST_BOOLEAN, Value.Factory.newReadOnlyInstance(lex.get(from).value != 0));
						from++;
						break;
					case CONSTANT_I	:
						commands.addCommand(CommandType.CONST_INT, Value.Factory.newReadOnlyInstance(lex.get(from).value));
						from++;
						break;
					case CONSTANT_R	:
						commands.addCommand(CommandType.CONST_REAL, Value.Factory.newReadOnlyInstance(Double.longBitsToDouble(lex.get(from).value)));
						from++;
						break;
					case CONSTANT_C :
						commands.addCommand(CommandType.CONST_CHAR, Value.Factory.newReadOnlyInstance((char[])lex.get(from).content));
						from++;
						break;
					case NAME		:
						from = parseRightName(lex, from, commands);
						break;
					case OPEN		:
						from = parseExpression(lex, from + 1, Priority.OR, commands);
						if (lex.get(from).type == LexType.CLOSE) {
							from++;
						}
						else {
							throw new SyntaxException(0, from, "Missing ')'");
						}
						break;
					default :
						throw new SyntaxException(0, from, "Operand is missing");
				}
				break;
			case ASSIGN: case UNKNOWN :
				break;
			default:
				throw new UnsupportedOperationException("Priority ["+prty+"] is not supported yet");
		}
		return from;
	}
	
	private int parseRightName(final List<Lexema> lex, int from, final CommandList commands) throws SyntaxException {
		for(;;) {
			final int	currentLex = from++;
			
			switch (lex.get(from).type) {
				case OPENB 	:
					from = parseExpression(lex, from + 1, Priority.OR, commands);
					if (lex.get(from).type == LexType.CLOSEB) {
						commands.addCommand(CommandType.LOAD_INDEX);
						from++;
					}
					else {
						throw new SyntaxException(0, from, "Missing ']'"); 
					}
					break;
				case OPEN 	:
					int	argCount = 0;
					do {
						from = parseExpression(lex, from + 1, Priority.OR, commands);
						argCount++;
					} while (lex.get(from).type == LexType.LIST);
					if (lex.get(from).type == LexType.CLOSE) {
						commands.addCommand(CommandType.CALL, lex.get(currentLex).value, argCount);
						from++;
					}
					else {
						throw new SyntaxException(0, from, "Missing ')'"); 
					}
				default :
					commands.addCommand(CommandType.LOAD_VAR);
					from++;
					break;
			}
			if (lex.get(from).type != LexType.DOT) {
				break;
			}
			else {
				from++;
			}
		}
		return from;
	}
	
	private static boolean isArrayValid(final char[] toTest) {
		return toTest != null && toTest.length > 0;
	}

	private OperatorType classifyLine(final Line line) {
		final char[]	source = line.content;
		final char		startOperator = macroPrefix[0];
		final char		startSubst = substPrefix[0];
		
		for(int index = line.from, maxIndex = line.from + line.len; index < maxIndex; index++) {
			if (source[index] == startOperator && MacroUtils.equals(source, index, macroPrefix)) {
				for(OperatorType item : OperatorType.values()) {
					if (item.getLineType() == LineType.OPERATOR && MacroUtils.equals(source, index + macroPrefix.length, item.getMnemonics())) {
						return item;
					}
				}
			}
			else if (source[index] == startSubst && MacroUtils.equals(source, index, substPrefix) && isSubstVariableHere(source, index)) {
				return OperatorType.SUBST;
			}
		}
		return OperatorType.ORDINAL;
	}

	private void addOrdinal(final Line line, final CommandList cl) {
		cl.addCommand(CommandType.CONST_CHAR, Value.Factory.newReadOnlyInstance(Arrays.copyOfRange(line.content, line.from, line.from + line.len)));
		cl.addCommand(CommandType.PRINT);
	}

	private void processSubstitution(final Line line) {
		// TODO Auto-generated method stub
		sbTemp.setLength(0);
		
		
	}

	private boolean refreshLine(Line line) {
		// TODO Auto-generated method stub
		return false;
	}

	
	
	
	private int skipBlank(final char[] source, int from) {
		while (source[from] <= ' ' && source[from] != '\n') {
			from++;
		}
		return from;
	}

	private int skipName(final char[] source, int from) {
		if (Character.isJavaIdentifierStart(source[from])) {
			while (Character.isJavaIdentifierPart(source[from])) {
				from++;
			}
		}
		return from;
	}

	private int skipKeyword(final char[] source, int from, final OperatorType operator) {
		from = skipBlank(source, from);
		from += macroPrefix.length;
		from += operator.getMnemonics().length;
		return from;
	}
	
	
	private boolean isSubstVariableHere(final char[] source, final int from) {
		int index = from + substPrefix.length;
		
		if (!Character.isJavaIdentifierStart(source[index])) {
			return false;
		}
		else {
			while (Character.isJavaIdentifierPart(source[index])) {
				index++;
			}
			if (!suffixIsMandatory || MacroUtils.equals(source, index, substSuffix)) {
				return true;
			}
			else {
				return false;
			}
		}
	}

	private static class IfDescriptor {
		int			totalForward;
		boolean		elseDetected = false;
	}
	
	private static class CaseDescriptor {
		final int	totalForward;
		int			nextForward;
		boolean		ofDetected = false;
		boolean		defaultDetected = false;
		
		public CaseDescriptor(int totalForward) {
			this.totalForward = totalForward;
		}
	}
	
	private static class ForDescriptor {
		final int	totalForward;
		final int	nextBackward;
		int			varName;
		int			varType;
		boolean		isLocalVariable;
		
		public ForDescriptor(int totalForward, int nextBackward) {
			this.totalForward = totalForward;
			this.nextBackward = nextBackward;
		}
		
		
	}
}
