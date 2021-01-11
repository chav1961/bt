package chav1961.bt.nlp.dictionary;

import java.io.IOException;
import java.io.Reader;

import chav1961.bt.nlp.interfaces.internal.WordDescriptor;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.BitCharSet;
import chav1961.purelib.basic.ExtendedBitCharSet;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class DictionaryManager {
	private static final int		LS_INIT = 0;
	private static final int		LS_WORD = 1;
	private static final int		LS_DESC = 2;
	private static final int		LS_DESC_ITEM = 3;
	private static final String		ALPHABET = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЬЫЪЭЮЯабвгдеёжзийклмнопрстуфхцчшщьыъэюя";
	private static final String		ACUT = "а̀а́е́и́о̀о́у́ы́я́";
	private static final BitCharSet	LETTERS = new ExtendedBitCharSet();
	
	static {
		for (char item : ALPHABET.toCharArray()) {
			LETTERS.add(item);
		}
		for (char item : ACUT.toCharArray()) {
			LETTERS.add(item);
		}
	}
	
	
	private final SyntaxTreeInterface<WordDescriptor>	words = new AndOrTree<>(1,8);
	private final SyntaxTreeInterface<WordDescriptor>	roots = new AndOrTree<>(2,8);
	private final SyntaxTreeInterface<WordDescriptor>	directSuffices = new AndOrTree<>(3,8);
	private final SyntaxTreeInterface<WordDescriptor>	invertedSuffices = new AndOrTree<>(4,8);
	
	private final char[]	buffer = new char[100];

	private int		loaderState = 0;
	private int		count = 0;
	private long	lastWordId;
	public DictionaryManager() {
		
	}
	
	public void load(final Reader reader) throws IOException, SyntaxException {
		try (final LineByLineProcessor	lblp = new LineByLineProcessor((displacement, lineNo, data, from, length) -> processLine(displacement, lineNo, data, from, length))) {
			loaderState = LS_INIT;
			lblp.write(reader);
		}
		System.err.println("Total="+count);
	}
	
	private void processLine(final long displacement, final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
		final int	begin = from;
		int			start = from;
		
//		System.err.println(new String(data,from,length));
		count++;
		if (data[from] != '#') {
loop:		for (;;) {
				switch (loaderState) {
					case LS_DESC_ITEM :
						if (data[from] == '-') {
							endWordSemantic(lastWordId);
							startWordSemantic(lastWordId);
							from++;
						}
						while (data[from] <= ' ' && data[from] != '\n' && data[from] != '\r') {
							from++;
						}
						if (from > start) {
							processDesc(data,begin,from);
							break loop;
						}
						else if (LETTERS.contains(data[from])) {
							endWordSemantic(lastWordId);
							endWord(lastWordId);
							loaderState = LS_WORD;
							continue loop;
						}
						else {
							throw new SyntaxException(lineNo, from-begin, "Unknown lexema");
						}
					case LS_DESC :
						if (data[from] == '-') {
							loaderState = LS_DESC_ITEM;
							from++;
							continue loop;
						}
						else {
							while (data[from] <= ' ' && data[from] != '\n' && data[from] != '\r') {
								from++;
							}
							if (from > start) {
								processDesc(data,begin,from);
								break loop;
							}
							else if (LETTERS.contains(data[from])) {
								endWordSemantic(lastWordId);
								endWord(lastWordId);
								loaderState = LS_WORD;
								continue loop;
							}
							else {
								throw new SyntaxException(lineNo, from-begin, "Unknown lexema");
							}
						}
					case LS_INIT : case LS_WORD :
						while (LETTERS.contains(data[from]) || data[from] == '-') {
							from++;
						}
						if (data[from] == ':') {
							lastWordId = words.placeName(data, start, from, null);
							startWord(lastWordId);
							startWordSemantic(lastWordId);
							loaderState = LS_DESC;
						}
						else {
							throw new SyntaxException(lineNo, from-begin, "(:) missing");
						}
						break loop;
					default : throw new IllegalStateException("Loader state ["+loaderState+"] is not supported yet");
				}
			}
		}
	}

	private void startWord(final long wordId) {
		// TODO Auto-generated method stub
		
	}

	private void startWordSemantic(final long wordId) {
		// TODO Auto-generated method stub
		
	}

	
	private void processDesc(char[] data, int begin, int from) throws SyntaxException {
		// TODO Auto-generated method stub
		int	start;
		
		switch (data[from]) {
			case 'с'	:
				break;
			case 'з'	:
				break;
			case 'о'	:
				if (data[from+1] == ':' ) {
					from += 2;
					
					while (data[from] <= ' ' && data[from] != '\n' && data[from] != '\r') {
						from++;
					}
					start = from;
					while (LETTERS.contains(data[from]) || data[from] == '-') {
						from++;
					}
					if (data[from] > ' ') {
						if (data[from] == '\"') {
							
						}
						else {
							throw new SyntaxException(0,0,"");
						}
					}
					else {
						roots.placeName(data, start, from, null);
					}
				}
				break;
			case 'п'	:
				from += 2;
				while (data[from] <= ' ' && data[from] != '\n' && data[from] != '\r') {
					from++;
				}
				
				from--;
				do{	start = ++from;
					while (LETTERS.contains(data[from]) || data[from] == '*' || data[from] == '?') {
						from++;
					}
					if (from > start) {
						directSuffices.placeName(data, start, from, null);
						for (int index = from, to = 0; index >= start; index--, to++) {
							buffer[to] = data[index];
						}
						invertedSuffices.placeName(buffer, 0, from-start, null);
					}
				} while (data[from] == ';');
				
				if (from > start) {
					directSuffices.placeName(data, start, from, null);
					for (int index = from, to = 0; index >= start; index--, to++) {
						buffer[to] = data[index];
					}
					invertedSuffices.placeName(buffer, 0, from-start, null);
				}
				break;
			case 'ф'	:
				break;
//			default : throw new SyntaxException(lineNo, from-begin, "Unknown line type ["+data[from]+"]");
		}
	}

	private void endWordSemantic(final long wordId) {
		// TODO Auto-generated method stub
		
	}

	
	private void endWord(final long wordId) {
		// TODO Auto-generated method stub
		
	}

}
