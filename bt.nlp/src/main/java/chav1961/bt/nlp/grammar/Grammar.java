package chav1961.bt.nlp.grammar;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.charsource.StringCharSource;
import chav1961.purelib.streams.interfaces.CharacterSource;


public class Grammar {
	
	
	public Grammar(final String rdr) throws SyntaxException {
		if (rdr == null) {
			throw new NullPointerException("Reader can't be null");
		}
		else {
			processGrammar(CharUtils.terminateAndConvert2CharArray(rdr, InternalUtils.EOF));
		}
	}

	private void processGrammar(final char[] src) throws SyntaxException {
		// TODO Auto-generated method stub
		final SyntaxTreeInterface<Object>	names = new AndOrTree<>();
		final InternalUtils.Lexema[]		list = InternalUtils.parseLex(src, names);
	}

	
	
	
}
