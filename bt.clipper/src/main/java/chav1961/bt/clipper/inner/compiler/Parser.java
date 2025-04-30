package chav1961.bt.clipper.inner.compiler;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import chav1961.bt.clipper.ClipperRuntime;
import chav1961.bt.clipper.inner.compiler.Lexema.LexType;
import chav1961.bt.clipper.inner.compiler.Lexema.OperType;
import chav1961.bt.clipper.inner.interfaces.ClipperValue;
import chav1961.bt.clipper.inner.streams.PreprocessingXReader;
import chav1961.bt.clipper.inner.vm.ConstantPool;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.streams.char2char.SequenceReader;

public class Parser {
	private static final SyntaxTreeInterface<LexType>	KEYWORDS = new AndOrTree<>();
	private static final SyntaxTreeInterface<OperType>	OPERATORS = new AndOrTree<>();

	static {
		fillKeywords("box",LexType.Box);
		fillKeywords("prompt",LexType.Prompt);
		fillKeywords("say",LexType.Say);
		fillKeywords("get",LexType.Get);
		fillKeywords("to",LexType.To);
		fillKeywords("accept",LexType.Accept);
		fillKeywords("append",LexType.Append);
		fillKeywords("blank",LexType.Blank);
		fillKeywords("average",LexType.Average);
		fillKeywords("begin",LexType.Begin);
		fillKeywords("sequence",LexType.Sequence);
		fillKeywords("end",LexType.End);
		fillKeywords("call",LexType.Call);
		fillKeywords("cancel",LexType.Cancel);
		fillKeywords("clear",LexType.Clear);
		fillKeywords("all",LexType.All);
		fillKeywords("gets",LexType.Gets);
		fillKeywords("memory",LexType.Memory);
		fillKeywords("screen",LexType.Screen);
		fillKeywords("close",LexType.Close);
		fillKeywords("continue",LexType.Continue);
		fillKeywords("copy",LexType.Copy);
		fillKeywords("file",LexType.File);
		fillKeywords("structure",LexType.Structure);
		fillKeywords("extended",LexType.Extended);
		fillKeywords("count",LexType.Count);
		fillKeywords("create",LexType.Create);
		fillKeywords("declare",LexType.Declare);
		fillKeywords("delete",LexType.Delete);
		fillKeywords("dir",LexType.Dir);
		fillKeywords("display",LexType.Display);
		fillKeywords("do",LexType.Do);
		fillKeywords("case",LexType.Case);
		fillKeywords("endcase",LexType.EndCase);
		fillKeywords("while",LexType.While);
		fillKeywords("enddo",LexType.EndDo);
		fillKeywords("eject",LexType.Eject);
		fillKeywords("erase",LexType.Erase);
		fillKeywords("exit",LexType.Exit);
		fillKeywords("external",LexType.External);
		fillKeywords("find",LexType.Find);
		fillKeywords("for",LexType.For);
		fillKeywords("next",LexType.Next);
		fillKeywords("function",LexType.Function);
		fillKeywords("return",LexType.Return);
		fillKeywords("go",LexType.Goto);
		fillKeywords("goto",LexType.Goto);
		fillKeywords("if",LexType.If);
		fillKeywords("elseif",LexType.ElseIf);
		fillKeywords("else",LexType.Else);
		fillKeywords("endif",LexType.Endif);
		fillKeywords("index",LexType.Index);
		fillKeywords("on",LexType.On);
		fillKeywords("input",LexType.Input);
		fillKeywords("join",LexType.Join);
		fillKeywords("with",LexType.With);
		fillKeywords("keyboard",LexType.Keyboard);
		fillKeywords("label",LexType.Label);
		fillKeywords("form",LexType.Form);
		fillKeywords("list",LexType.List);
		fillKeywords("locate",LexType.Locate);
		fillKeywords("loop",LexType.Loop);
		fillKeywords("menu",LexType.Menu);
		fillKeywords("note",LexType.Note);
		fillKeywords("pack",LexType.Pack);
		fillKeywords("parameters",LexType.Parameters);
		fillKeywords("private",LexType.Private);
		fillKeywords("procedure",LexType.Procedure);
		fillKeywords("public",LexType.Public);
		fillKeywords("quit",LexType.Quit);
		fillKeywords("read",LexType.Read);
		fillKeywords("recall",LexType.Recall);
		fillKeywords("reindex",LexType.Reindex);
		fillKeywords("release",LexType.Release);
		fillKeywords("rename",LexType.Rename);
		fillKeywords("replace",LexType.Replace);
		fillKeywords("report",LexType.Report);
		fillKeywords("restore",LexType.Restore);
		fillKeywords("from",LexType.From);
		fillKeywords("run",LexType.Run);
		fillKeywords("save",LexType.Save);
		fillKeywords("seek",LexType.Seek);
		fillKeywords("select",LexType.Select);
		fillKeywords("set",LexType.Set);
		fillKeywords("alternate",LexType.Alternate);
		fillKeywords("bell",LexType.Bell);
		fillKeywords("century",LexType.Century);
		fillKeywords("date",LexType.Date);
		fillKeywords("color",LexType.Color);
		fillKeywords("confirm",LexType.Confirm);
		fillKeywords("console",LexType.Console);
		fillKeywords("decimals",LexType.Decimals);
		fillKeywords("default",LexType.Default);
		fillKeywords("deleted",LexType.Deleted);
		fillKeywords("delimiters",LexType.Delimiters);
		fillKeywords("device",LexType.Device);
		fillKeywords("escape",LexType.Escape);
		fillKeywords("exact",LexType.Exact);
		fillKeywords("filter",LexType.Filter);
		fillKeywords("fixed",LexType.Fixed);
		fillKeywords("format",LexType.Format);
		fillKeywords("intensity",LexType.Intensity);
		fillKeywords("key",LexType.Key);
		fillKeywords("margin",LexType.Margin);
		fillKeywords("message",LexType.Message);
		fillKeywords("order",LexType.Order);
		fillKeywords("path",LexType.Path);
		fillKeywords("print",LexType.Print);
		fillKeywords("printer",LexType.Printer);
		fillKeywords("relation",LexType.Relation);
		fillKeywords("unique",LexType.Unique);
		fillKeywords("skip",LexType.Skip);
		fillKeywords("sort",LexType.Sort);
		fillKeywords("store",LexType.Store);
		fillKeywords("sum",LexType.Sum);
		fillKeywords("text",LexType.Text);
		fillKeywords("endtext",LexType.EndText);
		fillKeywords("total",LexType.Total);
		fillKeywords("type",LexType.Type);
		fillKeywords("update",LexType.Update);
		fillKeywords("use",LexType.Use);
		fillKeywords("wait",LexType.Wait);
		fillKeywords("zap",LexType.ZAP);
		
		OPERATORS.placeName(".not.",OperType.Not);
		OPERATORS.placeName(".and.",OperType.And);
		OPERATORS.placeName(".or.",OperType.Or);
	}
	
	public static Lexema[] parse(final Reader reader, final ClipperRuntime runtime, final boolean usePreprocessor, final boolean keepComments) throws SyntaxException {
		final List<Lexema>	result = new ArrayList<>();
		
		try {
			final LineByLineProcessor	proc = new LineByLineProcessor(new LblpCallback(result, runtime, keepComments));
			proc.write(usePreprocessor ? new PreprocessingXReader(new SequenceReader(new StringReader(""),reader), null, null) : reader);
			proc.flush();
		} catch (SyntaxException | IOException e) {
		} finally {
			result.add(new Lexema(0,0,LexType.EOF));
		}
		return result.toArray(new Lexema[result.size()]);
	}

	public static int buildEntity(final Lexema[] content, final int from, final SyntaxNode<Enum<?>, SyntaxNode<?,?>> root) {
		switch (content[from].getType()) {
			case Function 	:
				return buildFunction(content, from, root);
			case Procedure 	:
				return buildProcedure(content, from, root);
			default :
				return buildMain(content, from, root);
		}
	}
	
	private static int buildFunction(final Lexema[] content, final int from, final SyntaxNode<Enum<?>, SyntaxNode<?, ?>> root) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static int buildProcedure(final Lexema[] content, final int from, final SyntaxNode<Enum<?>, SyntaxNode<?, ?>> root) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static int buildMain(final Lexema[] content, final int from, final SyntaxNode<Enum<?>, SyntaxNode<?, ?>> root) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static void fillKeywords(final String keyword, final LexType type) {
		fillKeywords(keyword, type, Integer.MAX_VALUE);
	}
	
	private static void fillKeywords(final String keyword, final LexType type, final int trunc) {
		KEYWORDS.placeName(keyword.toUpperCase(),type);
		KEYWORDS.placeName(keyword.toLowerCase(),type);
		if (keyword.length() > trunc) {
			KEYWORDS.placeName(keyword.toUpperCase().substring(0,trunc),type);
			KEYWORDS.placeName(keyword.toLowerCase().substring(0,trunc),type);
		}
	}
	
	static class LblpCallback implements LineByLineProcessorCallback {
		private final List<Lexema>		result;
		private final ClipperRuntime	runtime;
		private final boolean			keepCommments;
		private final long[]			forNumbers = new long[2];
		private final int[]				forStrings = new int[2]; 
		boolean 						inComment = false;
		
		LblpCallback(final List<Lexema> result, final ClipperRuntime runtime, final boolean keepComments) {
			this.result = result;
			this.runtime = runtime;
			this.keepCommments = keepComments;
		}
		
		@Override
		public void processLine(long displacement, int lineNo, char[] data, int from, int length) throws IOException, SyntaxException {
			final int	start = from, to = from + length;
			boolean		sameFirst = true;
			
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
				boolean	endOp = true;
				
				for (;;) {
					from = CharUtils.skipBlank(data, from, true);
					final int 	col = from - start;
					
					switch (data[from]) {
						case '\r' : case '\n' :
							if (endOp) {
								result.add(new Lexema(lineNo, col, LexType.EndOp));
							}
							return;
						case ':' : 
							result.add(new Lexema(lineNo, col, LexType.Colon));
							from++;
							break;
						case ';' :		// Multi-line operators concatenated with semicolons 
							endOp = false;
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
						case '&' : 
							result.add(new Lexema(lineNo, col, LexType.Macros));
							from++;
							break;
						case '{' : 
							if (data[from+1] == '|') {
								result.add(new Lexema(lineNo, col, LexType.Const));
								from += 2;
							}
							else {
								result.add(new Lexema(lineNo, col, LexType.OpenFB));
								from++;
							}
							break;
						case '|' : 
							result.add(new Lexema(lineNo, col, LexType.Vert));
							from++;
							break;
						case '}' : 
							result.add(new Lexema(lineNo, col, LexType.CloseFB));
							from++;
							break;
						case '.' :
							if ((data[from + 1] == 'f' || data[from + 1] == 'F') && data[from + 2] == '.') {
								result.add(new Lexema(lineNo, col, LexType.Const, ConstantPool.FALSE_ID));
								from +=3;
							}
							else if ((data[from + 1] == 't' || data[from + 1] == 'T') && data[from + 2] == '.') {
								result.add(new Lexema(lineNo, col, LexType.Const, ConstantPool.TRUE_ID));
								from +=3;
							}
							else {
								forStrings[0] = from++;
								while (data[from] > ' ' && data[from] != '.') {
									from++;
								}
								forStrings[1] = from++;
								final long	id = OPERATORS.seekName(data, forStrings[0], forStrings[0]);

								if (id > 0) {
									result.add(new Lexema(lineNo, col, OPERATORS.getCargo(id)));
								}
								else {
									result.add(new Lexema(lineNo, col, LexType.Dot));
									from = forStrings[0] + 1;
								}
							}
							break;
						case '+' :
							result.add(new Lexema(lineNo, col, OperType.Add));
							from++;
							break;
						case '-' :
							result.add(new Lexema(lineNo, col, OperType.Sub));
							from++;
							break;
						case '%' :
							result.add(new Lexema(lineNo, col, OperType.Mod));
							from++;
							break;
						case '$' :
							result.add(new Lexema(lineNo, col, OperType.Contains));
							from++;
							break;
						case '*' :
							if (sameFirst) {	// Comment
								if (keepCommments) {
									result.add(new Lexema(lineNo, col, LexType.Comment));
								}
								return;
							}
							else if (data[from + 1] == '*') {
								result.add(new Lexema(lineNo, col, OperType.Power));
								from += 2;
							}
							else {
								result.add(new Lexema(lineNo, col, OperType.Mul));
								from++;
							}
							break;
						case '/' :
							if (data[from+1] == '*') {
								if (keepCommments) {
									result.add(new Lexema(lineNo, col, LexType.Comment));
								}
								inComment = true;
								processLine(displacement+from-start+2, lineNo, data, from+2, length - (to - from + 2));
								return;
							}
							else {
								result.add(new Lexema(lineNo, col, OperType.Div));
								from++;
							}
							break;
						case '?' :
							if (data[from + 1] == '?') {
								result.add(new Lexema(lineNo, col, LexType.ToConsole2));
								from += 2;
							}
							else {
								result.add(new Lexema(lineNo, col, LexType.ToConsole));
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
						case '\'' : 
							from = CharUtils.parseUnescapedString(data, from, '\'', true, forStrings) + 1;
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
											final LexType	type = KEYWORDS.getCargo(id);
											
											if (type != LexType.Note) {
												result.add(new Lexema(lineNo, col, type));
											}
											else {
												if (keepCommments) {
													result.add(new Lexema(lineNo, col, LexType.Comment));
												}
												return;
											}
										}
										else {
											result.add(new Lexema(lineNo, col, LexType.Function, runtime.getDistionary().placeOrChangeName(data, forStrings[0], forStrings[1], null)));
										}
									}
								}
								else {
									final long	kw = KEYWORDS.seekName(data, forStrings[0], forStrings[1]);
									
									if (kw >= 0) {
										final LexType	type = KEYWORDS.getCargo(kw);
										
										if (type != LexType.Note) {
											result.add(new Lexema(lineNo, col, type));
										}
										else {
											if (keepCommments) {
												result.add(new Lexema(lineNo, col, LexType.Comment));
											}
											return;
										}
									}
									else {
										result.add(new Lexema(lineNo, col, LexType.Name, runtime.getDistionary().placeOrChangeName(data, forStrings[0], forStrings[1], null)));
									}
								}
							}
							else {
								from++;
							}
						sameFirst = false;
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
