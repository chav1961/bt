package chav1961.bt.clipper.inner.compiler;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.bt.clipper.ClipperRuntime;
import chav1961.bt.clipper.inner.compiler.Lexema.LexType;
import chav1961.bt.clipper.inner.compiler.Lexema.OperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.bt.clipper.inner.vm.ConstantPool;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

public class Parser {
	private static final SyntaxTreeInterface<LexType>	KEYWORDS = new AndOrTree<>();

	static {
		KEYWORDS.placeName("SSS",LexType.Builtin);
	}
	
	public static Lexema[] parse(final Reader reader, final ClipperRuntime runtime) throws SyntaxException {
		final List<Lexema>	result = new ArrayList<>();
		
		try {
			final LineByLineProcessor	proc = new LineByLineProcessor(new LblpCallback(result, runtime));
			proc.write(reader);
			proc.flush();
		} catch (SyntaxException | IOException e) {
		} finally {
			result.add(new Lexema(0,0,LexType.EOF));
		}
		return result.toArray(new Lexema[result.size()]);
	}

	public static SyntaxNode<Enum<?>, SyntaxNode<?,?>> buildTree(final Lexema[] content) {
		return null;
	}
	
	public static byte[] toPCode(final SyntaxNode<Enum<?>, SyntaxNode<?,?>> tree, final ConstantPool cp) {
		return null;
	}
	
	static class LblpCallback implements LineByLineProcessorCallback {
		private final List<Lexema>		result;
		private final ClipperRuntime	runtime;
		private final long[]			forNumbers = new long[2];
		private final int[]				forStrings = new int[2]; 
		boolean 						inComment = false;
		
		LblpCallback(final List<Lexema> result, final ClipperRuntime runtime) {
			this.result = result;
			this.runtime = runtime;
		}
		
		@Override
		public void processLine(long displacement, int lineNo, char[] data, int from, int length) throws IOException, SyntaxException {
			final int	start = from, to = from + length;
			
			if (inComment) {
				for (; from < to; from++) {
					if (data[from] == '*' && data[from + 1] == '/') {
						inComment = false;
						processLine(displacement+from-start+2, lineNo, data, from+2, length - (to - from + 2));
						return;
					}
				}
			}
			else {
				for (;;) {
					from = CharUtils.skipBlank(data, from, true);
					final int 	col = from - start;
					
					switch (data[from]) {
						case '\r' : case '\n' :
							return;
						case '.' : 
							result.add(new Lexema(lineNo, col, LexType.Dot));
							from++;
							break;
						case ':' : 
							result.add(new Lexema(lineNo, col, LexType.Colon));
							from++;
							break;
						case ';' : 
							result.add(new Lexema(lineNo, col, LexType.Semicolon));
							from++;
							break;
						case ',' : 
							result.add(new Lexema(lineNo, col, LexType.Div));
							from++;
							break;
						case '(' : 
							result.add(new Lexema(lineNo, col, LexType.OpenB));
							from++;
							break;
						case ')' : 
							result.add(new Lexema(lineNo, col, LexType.CloseB));
							from++;
							break;
						case '[' : 
							result.add(new Lexema(lineNo, col, LexType.OpenBB));
							from++;
							break;
						case ']' : 
							result.add(new Lexema(lineNo, col, LexType.CloseBB));
							from++;
							break;
						case '{' : 
							if (data[from+1] == '|') {
								result.add(new Lexema(lineNo, col, LexType.OpenXB));
								from += 2;
							}
							else {
								result.add(new Lexema(lineNo, col, LexType.OpenFB));
								from++;
							}
							break;
						case '}' : 
							result.add(new Lexema(lineNo, col, LexType.CloseFB));
							from++;
							break;
						case '+' :
							result.add(new Lexema(lineNo, col, OperType.Add));
							from++;
							break;
						case '-' :
							result.add(new Lexema(lineNo, col, OperType.Sub));
							from++;
							break;
						case '*' :
							result.add(new Lexema(lineNo, col, OperType.Mul));
							from++;
							break;
						case '/' :
							if (data[from+1] == '/') {
								return;
							}
							else if (data[from+1] == '*') {
								inComment = true;
								processLine(displacement+from-start+2, lineNo, data, from+2, length - (to - from + 2));
								return;
							}
							else {
								result.add(new Lexema(lineNo, col, OperType.Div));
								from++;
							}
							break;
						case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
							from = CharUtils.parseNumber(data, from, forNumbers, CharUtils.PREF_ANY, false);
							result.add(new Lexema(lineNo, col, addConstant(runtime.getConstantPool(), forNumbers)));
							break;
						case '\"' : 
							from = CharUtils.parseUnescapedString(data, from, '\"', true, forStrings) + 1;
							result.add(new Lexema(lineNo, col, addConstant(runtime.getConstantPool(), data,  forStrings)));
							break;
						default :
							if (Character.isJavaIdentifierStart(data[from])) {
								from = CharUtils.skipBlank(data, CharUtils.parseName(data, from, forStrings), true);
								if (data[from] == '(') {
									final long	id = runtime.getBuiltinsTree().seekName(data, forStrings[0], forStrings[1]);
									
									if (id >= 0) {
										result.add(new Lexema(lineNo, col, LexType.Builtin, id));
									}
									else {
										final long	kw = KEYWORDS.seekName(data, forStrings[0], forStrings[1]);
										
										if (id >= 0) {
											result.add(new Lexema(lineNo, col, KEYWORDS.getCargo(id)));
										}
										else {
											result.add(new Lexema(lineNo, col, LexType.Function, runtime.getDistionary().placeOrChangeName(data, forStrings[0], forStrings[1], null)));
										}
									}
								}
								else {
									final long	kw = KEYWORDS.seekName(data, forStrings[0], forStrings[1]);
									
									if (kw >= 0) {
										result.add(new Lexema(lineNo, col, KEYWORDS.getCargo(kw)));
									}
									else {
										result.add(new Lexema(lineNo, col, LexType.Name, runtime.getDistionary().placeOrChangeName(data, forStrings[0], forStrings[1], null)));
									}
								}
							}
							else {
								from++;
							}
					}
				}
			}
		}
		
		private ClipperValue addConstant(final ConstantPool pool, final long[] numbers) {
			return null;
		}
		
		private ClipperValue addConstant(final ConstantPool pool, final char[] data, final int[] ranges) {
			return null;
		}
	}
}
