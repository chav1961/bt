package chav1961.bt.matrix.macros;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chav1961.bt.matrix.macros.NestedReader.Line;
import chav1961.purelib.basic.CharUtils;
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
										"insert".toCharArray(),
										"include".toCharArray(),
										"call".toCharArray()
									};
	
	private final NestedReader	rdr;
	private final char[]		macroPrefix;
	private final char[]		substPrefix;
	private final char[]		substSuffix;
	private final boolean		suffixIsMandatory;
	private final StringBuilder	sbTemp = new StringBuilder();
	private ParseStack			stack = new ParseStack(null);
	
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
		ELSIF("elsif", LineType.OPERATOR),
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
		INSERT("insert", LineType.OPERATOR),
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
		}
	}
	
	public boolean next(final Line line) throws IOException {
		if (!rdr.next(line)) {
			return false;
		}
		else {
			final OperatorType	type = classifyLine(line);
			
			switch (type) {
				case BREAK		:
					parseBreak(line);
					break;
				case CALL		:
					parseCall(line);
					break;
				case CASE		:
					parseCase(line);
					break;
				case CONTINUE	:
					parseContinue(line);
					break;
				case DEFAULT	:
					parseDefault(line);
					break;
				case ELSIF		:
					parseElsif(line);
					break;
				case ENDCASE	:
					parseEndCase(line);
					break;
				case ENDFOR		:
					parseEndFor(line);
					break;
				case ENDIF		:
					parseEndIf(line);
					break;
				case ENDWHILE	:
					parseEndWhile(line);
					break;
				case ERROR		:
					parseError(line);
					break;
				case FOR		:
					parseFor(line);
					break;
				case IF			:
					parseIf(line);
					break;
				case INCLUDE	:
					parseInclude(line);
					break;
				case INSERT		:
					parseInsert(line);
					break;
				case OF			:
					parseOf(line);
					break;
				case ORDINAL	:
					addOrdinal(line);
					break;
				case PARAM		:
					parseParam(line);
					break;
				case PRINT		:
					parsePrint(line);
					break;
				case SET		:
					parseSet(line);
					break;
				case SUBST		:
					processSubstitution(line);
					break;
				case VAR		:
					parseVar(line);
					break;
				case WARNING	:
					parseWarning(line);
					break;
				case WHILE		:
					parseWhile(line);
					break;
				default:
					throw new UnsupportedOperationException("Line type ["+type.getLineType()+"] is nto supported yet");
			}
			return refreshLine(line);
		}
	}
	
	private void parseWhile(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseWarning(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseVar(final Line line) {
	}

	private void parseSet(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parsePrint(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseParam(final Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseOf(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseInsert(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseInclude(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseIf(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseFor(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseError(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseEndWhile(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseEndIf(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseEndFor(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseEndCase(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseElsif(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseDefault(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseContinue(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseCase(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseCall(Line line) {
		// TODO Auto-generated method stub
		
	}

	private void parseBreak(Line line) {
		// TODO Auto-generated method stub
		
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

	private void addOrdinal(final Line line) {
		// TODO Auto-generated method stub
		
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
	
}
